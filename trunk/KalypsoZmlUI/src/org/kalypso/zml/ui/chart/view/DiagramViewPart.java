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
package org.kalypso.zml.ui.chart.view;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.contribs.eclipse.ui.forms.ToolkitUtils;
import org.kalypso.ui.repository.view.RepositoryExplorerPart;
import org.kalypso.zml.core.base.selection.ZmlSelectionBuilder;

/**
 * Diagram QuickView.
 *
 * @author Dirk Kuch
 */
public class DiagramViewPart extends ViewPart implements ISelectionChangedListener, IPartListener
{
  public static final String ID = "org.kalypso.zml.ui.chart.view.DiagramViewPart"; //$NON-NLS-1$

  private ZmlDiagramChartPartComposite m_chartPart;

  @Override
  public void createPartControl( final Composite parent )
  {
    final FormToolkit toolkit = ToolkitUtils.createToolkit( parent );

    final Composite base = toolkit.createComposite( parent, SWT.RIGHT | SWT.EMBEDDED | SWT.BORDER );
    GridLayoutFactory.fillDefaults().spacing( 0, 0 ).applyTo( base );

    m_chartPart.createControl( base );

    getSite().getPage().addPartListener( this );
  }

  @Override
  public void dispose( )
  {
    getSite().getPage().removePartListener( this );
  }

  @Override
  public void init( final IViewSite site ) throws PartInitException
  {
    super.init( site );

    m_chartPart = new ZmlDiagramChartPartComposite( this, getClass().getResource( "templates/diagram.kod" ) ); //$NON-NLS-1$
    m_chartPart.init( site );
  }

  @Override
  public void setFocus( )
  {
    m_chartPart.setFocus();
  }

  @Override
  public void selectionChanged( final SelectionChangedEvent event )
  {
    m_chartPart.setSelection( ZmlSelectionBuilder.getSelection( event ) );
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