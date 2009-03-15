/*--------------- Kalypso-Header --------------------------------------------------------------------

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

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.outline;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.util.Util;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.jface.viewers.ITooltipProvider;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.command.EnableThemeCommand;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.util.command.JobExclusiveCommandTarget;

/**
 * @author Gernot Belger
 */
@SuppressWarnings("restriction")
public class GisMapOutlineViewer implements ISelectionProvider, ICommandTarget
{
  private final GisMapOutlineContentProvider m_contentProvider;

  private final GisMapOutlineLabelProvider m_labelProvider;

  private CheckboxTreeViewer m_viewer;

  protected IMapModell m_mapModel;

  private ICommandTarget m_commandTarget;

  /**
   * @param showStyle
   *          If this parameter is set, the name of single styles of a theme is added to the theme name. For multiple
   *          styles of a theme, this is not necessary, because their level will be displayed in the outline then.
   */
  public GisMapOutlineViewer( final ICommandTarget commandTarget, final IMapModell mapModel )
  {
    m_labelProvider = new GisMapOutlineLabelProvider();
    m_contentProvider = new GisMapOutlineContentProvider( m_labelProvider );
    setMapModel( mapModel );
    m_commandTarget = commandTarget;
  }

  public void dispose( )
  {
    m_contentProvider.dispose();
  }

  public void createControl( final Composite parent )
  {
    final CheckboxTreeViewer viewer = new CheckboxTreeViewer( parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION );
    m_viewer = viewer;
    viewer.setContentProvider( m_contentProvider );
    viewer.setLabelProvider( m_labelProvider );

    final GisMapOutlineLabelProvider labelProvider = m_labelProvider;
    m_viewer.addCheckStateListener( new ICheckStateListener()
    {
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        final Object data = event.getElement();

        // Prevent de-selection of gray-checked elements; they are here interpreted as disabled
        if( labelProvider.isGrayed( data ) && !event.getChecked() )
        {
          viewer.setChecked( data, true );
          return;
        }

        if( data instanceof IKalypsoTheme )
        {
          final IKalypsoTheme theme = (IKalypsoTheme) data;
          final ICommand command = new EnableThemeCommand( theme, event.getChecked() );
          postCommand( command, null );
        }
      }
    } );

    final Tree tree = m_viewer.getTree();
    tree.addMouseMoveListener( new MouseMoveListener()
    {
      public void mouseMove( final MouseEvent e )
      {
        final TreeItem item = tree.getItem( new Point( e.x, e.y ) );
        final String text;
        if( item == null )
          text = null; // remove tooltip
        else
        {
          final Object data = item.getData();
          final ITooltipProvider ttProvider = (ITooltipProvider) Util.getAdapter( data, ITooltipProvider.class );
          text = ttProvider == null ? null : ttProvider.getTooltip( data );
        }
        tree.setToolTipText( text );
      }
    } );

    /* Allow the tree item to draw themselves. */
    tree.addListener( SWT.EraseItem, new Listener()
    {
      public void handleEvent( final Event event )
      {
        final Composite c = (Composite) event.widget;

        if( (event.detail & SWT.SELECTED) == 0 )
          return; /* item not selected */

        final Color leftColor = c.getDisplay().getSystemColor( SWT.COLOR_TITLE_BACKGROUND_GRADIENT );
        final Color rightColor = c.getDisplay().getSystemColor( SWT.COLOR_WHITE );
        final Color fontColor = c.getDisplay().getSystemColor( SWT.COLOR_BLACK );

        final GC gc = event.gc;
        final int clientWidth = event.width;

        final Color oldForeground = gc.getForeground();
        final Color oldBackground = gc.getBackground();
        gc.setForeground( leftColor );
        gc.setBackground( rightColor );
        gc.fillGradientRectangle( 0, event.y, clientWidth, event.height, false );
        gc.setForeground( oldForeground );
        gc.setBackground( oldBackground );
        event.detail &= ~SWT.SELECTED;

        gc.setForeground( fontColor );
      }
    } );

    m_viewer.setInput( m_mapModel );
  }

  /**
   * @return control
   * @see org.eclipse.jface.viewers.Viewer#getControl()
   */
  public Control getControl( )
  {
    return m_viewer.getControl();
  }

  /**
   * @see org.kalypso.ogc.gml.mapmodel.IMapModellView#setMapModell(org.kalypso.ogc.gml.mapmodel.IMapModell)
   */
  public void setMapModel( final IMapModell model )
  {
    m_mapModel = model;

    final CheckboxTreeViewer viewer = m_viewer;
    if( viewer != null && !viewer.getControl().isDisposed() )
    {
      viewer.getControl().getDisplay().asyncExec( new Runnable()
      {
        public void run( )
        {
          if( !viewer.getControl().isDisposed() )
            viewer.setInput( model );
        }
      } );
    }
  }


  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void addSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_viewer.addSelectionChangedListener( listener );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  public ISelection getSelection( )
  {
    if( m_viewer == null )
      return null;

    return m_viewer.getSelection();
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void removeSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_viewer.removeSelectionChangedListener( listener );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  public void setSelection( final ISelection selection )
  {
    m_viewer.setSelection( selection );
  }

  /**
   * @see org.kalypso.commons.command.ICommandTarget#postCommand(org.kalypso.commons.command.ICommand,
   *      java.lang.Runnable)
   */
  public void postCommand( final ICommand command, final Runnable runnable )
  {
    if( m_commandTarget != null )
      m_commandTarget.postCommand( command, runnable );
  }

  public void setCommandTarget( final JobExclusiveCommandTarget commandTarget )
  {
    m_commandTarget = commandTarget;
  }

  public void expandTree( )
  {
    m_viewer.expandAll();
  }

  public void collapseTree( )
  {
    m_viewer.collapseAll();
  }

  /**
   * @return <code>true</code> if the viewer is compact view state.
   * @see #setCompact(boolean)
   */
  public boolean isCompact( )
  {
    return m_contentProvider.isCompact();
  }

  public void setCompact( final boolean compact )
  {
    m_contentProvider.setCompact( compact );
  }
}