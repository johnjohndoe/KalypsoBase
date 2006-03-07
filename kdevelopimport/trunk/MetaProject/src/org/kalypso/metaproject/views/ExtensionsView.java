/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/
package org.kalypso.metaproject.views;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.metaproject.ImageProvider;

/**
 * This is the extensions-view of the plugins available in the current workspace.
 * 
 * @author schlienger
 */
public class ExtensionsView extends ViewPart
{
  private Action m_actionRefresh;
  private Action m_actionCollapse;

  private SashForm m_sashForm;
  private TreeViewer m_viewer;

  private Action m_actionDump;
  private ExtensionsTreeContentProvider m_extsTCP;

  /**
   * This is a callback that will allow us to create the viewer and initialize it.
   */
  @Override
  public void createPartControl( Composite parent )
  {
    m_sashForm = new SashForm( parent, SWT.HORIZONTAL );

    m_viewer = new TreeViewer( m_sashForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
    m_extsTCP = new ExtensionsTreeContentProvider();
    m_viewer.setContentProvider( m_extsTCP );
    m_viewer.setLabelProvider( new ExtensionsLabelProvider() );

    refreshPluginsList();

    makeActions();
    hookContextMenu();
    contributeToActionBars();
  }

  private void hookContextMenu()
  {
    MenuManager menuMgr = new MenuManager();
    menuMgr.setRemoveAllWhenShown( true );
    menuMgr.addMenuListener( new IMenuListener()
    {
      public void menuAboutToShow( IMenuManager manager )
      {
        ExtensionsView.this.fillContextMenu( manager );
      }
    } );
    Menu menu = menuMgr.createContextMenu( m_viewer.getControl() );
    m_viewer.getControl().setMenu( menu );
    getSite().registerContextMenu( menuMgr, m_viewer );
  }

  private void contributeToActionBars()
  {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalPullDown( bars.getMenuManager() );
    fillLocalToolBar( bars.getToolBarManager() );
  }

  private void fillLocalPullDown( IMenuManager manager )
  {
    manager.add( m_actionRefresh );
    manager.add( new Separator() );
    manager.add( m_actionCollapse );
    manager.add( new Separator() );
    manager.add( m_actionDump );
  }

  private void fillContextMenu( IMenuManager manager )
  {
    manager.add( m_actionRefresh );
    manager.add( new Separator() );
    manager.add( m_actionCollapse );
    manager.add( new Separator() );
    manager.add( m_actionDump );

    manager.add( new Separator() );
    // Other plug-ins can contribute there actions here
    manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
  }

  private void fillLocalToolBar( IToolBarManager manager )
  {
    manager.add( m_actionRefresh );
    manager.add( new Separator() );
    manager.add( m_actionCollapse );
    manager.add( new Separator() );
    manager.add( m_actionDump );
    manager.add( new Separator() );
  }

  private void makeActions()
  {
    m_actionRefresh = new Action( "Refresh" )
    {
      @Override
      public void run()
      {
        refreshPluginsList();
      }
    };
    m_actionRefresh.setToolTipText( "Refresh Projects List" );
    m_actionRefresh.setImageDescriptor( ImageProvider.IMAGE_REFRESH );

    m_actionCollapse = new Action( "Collapse All" )
    {
      @Override
      public void run()
      {
        m_viewer.collapseAll();
      }
    };
    m_actionCollapse.setImageDescriptor( ImageProvider.IMAGE_COLLAPSE );

    m_actionDump = new Action( "Dump to clipboard" )
    {
      @Override
      public void run()
      {
        final String dump = dumpTree();

        final StringSelection stsel = new StringSelection( dump );
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents( stsel, stsel );
      }
    };
    m_actionDump.setToolTipText( "Copy the whole tree in the clipboard" );
    m_actionDump.setImageDescriptor( ImageProvider.IMAGE_COPY );
  }

  private void refreshPluginsList()
  {
    final IExtensionRegistry reg = Platform.getExtensionRegistry();
    final String[] namespaces = reg.getNamespaces();

    final Comparator<String> cmp = new Comparator<String>()
    {
      public int compare( String o1, String o2 )
      {
        return o1.compareTo( o2 );
      }
    };
    
    Arrays.sort( namespaces, cmp );
    m_viewer.setInput( namespaces );
  }

  private String dumpTree()
  {
    final ITreeContentProvider cp = (ITreeContentProvider)m_viewer.getContentProvider();
    final Object[] objs = cp.getElements( m_viewer.getInput() );
    final StringBuffer bf = new StringBuffer();
    for( int i = 0; i < objs.length; i++ )
      dumpTreeInternal( cp, bf, objs[i], "" );

    return bf.toString();
  }

  private void dumpTreeInternal( final ITreeContentProvider cp, final StringBuffer bf, final Object obj,
      final String separator )
  {
    bf.append( separator ).append( obj.toString() ).append( "\n" );

    final Object[] children = cp.getChildren( obj );
    if( children != null )
      for( int i = 0; i < children.length; i++ )
        dumpTreeInternal( cp, bf, children[i], separator + "\t" );
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  @Override
  public void setFocus()
  {
    m_viewer.getControl().setFocus();
  }
}