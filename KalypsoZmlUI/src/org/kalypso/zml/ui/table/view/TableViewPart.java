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
package org.kalypso.zml.ui.table.view;

import java.net.URL;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.contribs.eclipse.swt.layout.Layouts;
import org.kalypso.contribs.eclipse.ui.forms.ToolkitUtils;
import org.kalypso.ui.repository.view.RepositoryExplorerPart;
import org.kalypso.zml.core.base.IMultipleZmlSourceElement;
import org.kalypso.zml.core.base.selection.ZmlSelectionBuilder;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableComposite;
import org.kalypso.zml.ui.table.context.IZmlTableSource;

/**
 * Table QuickView.
 * 
 * @author Dirk Kuch
 */
public class TableViewPart extends ViewPart implements ISelectionChangedListener, IPartListener
{
  public static final String ID = "org.kalypso.zml.ui.table.view.TableViewPart"; //$NON-NLS-1$

  protected TableComposite m_tableComposite;

  private TableViewPartListener m_partListener;

  @Override
  public void init( final IViewSite site ) throws PartInitException
  {
    super.init( site );

    m_partListener = new TableViewPartListener( this, site );
  }

  @Override
  public void createPartControl( final Composite parent )
  {
    final FormToolkit toolkit = ToolkitUtils.createToolkit( parent );

    final Composite base = toolkit.createComposite( parent, SWT.RIGHT | SWT.EMBEDDED | SWT.BORDER );
    final GridLayout layout = Layouts.createGridLayout();
    layout.verticalSpacing = 0;
    base.setLayout( layout );

    final URL template = getClass().getResource( "templates/base.kot" ); //$NON-NLS-1$

    m_tableComposite = new TableComposite( base, toolkit, template );
    m_tableComposite.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    m_partListener.activate( new IZmlTableSource()
    {
      @Override
      public IZmlTable getTable( )
      {
        return m_tableComposite.getTable();
      }

      @Override
      public IZmlTableComposite getComposite( )
      {
        return m_tableComposite.getTableComposite();
      }
    } );

    getSite().getPage().addPartListener( this );
  }

  @Override
  public void dispose( )
  {
    getSite().getPage().removePartListener( this );
  }

  @Override
  public void setFocus( )
  {
    m_tableComposite.setFocus();
  }

  @Override
  public void selectionChanged( final SelectionChangedEvent event )
  {
    final IMultipleZmlSourceElement[] sources = ZmlSelectionBuilder.getSelection( event );
    m_tableComposite.setSelection( sources );
  }

  @Override
  public void partActivated( final IWorkbenchPart part )
  {
    if( part != null && part instanceof RepositoryExplorerPart )
      ((RepositoryExplorerPart) part).addSelectionChangedListener( this );
  }

  @Override
  public void partBroughtToTop( final IWorkbenchPart part )
  {
  }

  @Override
  public void partClosed( final IWorkbenchPart part )
  {
    if( part != null && part instanceof RepositoryExplorerPart )
      ((RepositoryExplorerPart) part).removeSelectionChangedListener( this );
  }

  @Override
  public void partDeactivated( final IWorkbenchPart part )
  {
    if( part != null && part instanceof RepositoryExplorerPart )
      ((RepositoryExplorerPart) part).removeSelectionChangedListener( this );
  }

  @Override
  public void partOpened( final IWorkbenchPart part )
  {
  }
}