/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.featureview.control;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.om.table.command.ToolbarCommandUtils;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractToolbarFeatureControl extends AbstractFeatureControl
{
  private ToolBarManager m_toolbar;

  private EmbeddedToolbarExecutionListener m_executionListener;

  public AbstractToolbarFeatureControl( final IPropertyType ftp, final boolean showToolbar, final int toolbarStyle )
  {
    super( ftp );

    init( showToolbar, toolbarStyle );
  }

  public AbstractToolbarFeatureControl( final Feature feature, final IPropertyType ftp, final boolean showToolbar, final int toolbarStyle )
  {
    super( feature, ftp );

    init( showToolbar, toolbarStyle );
  }

  private void init( final boolean showToolbar, final int style )
  {
    if( showToolbar )
      m_toolbar = new ToolBarManager( style );
    else
      m_toolbar = null;
  }

  public void addToolbarItem( final String commandId, final int style )
  {
    final IServiceLocator serviceLocator = PlatformUI.getWorkbench();

    if( m_toolbar != null )
    {
      final CommandContributionItemParameter commandParams = new CommandContributionItemParameter( serviceLocator, commandId + "_item", commandId, style );//$NON-NLS-1$
      m_toolbar.add( new CommandContributionItem( commandParams ) );
      m_toolbar.update( true );
    }
  }

  protected void addToolbarItems( final String uri )
  {
    final IServiceLocator serviceLocator = PlatformUI.getWorkbench();
    final IMenuService service = (IMenuService)serviceLocator.getService( IMenuService.class );

    if( m_toolbar != null )
      service.populateContributionManager( m_toolbar, "toolbar:" + uri ); //$NON-NLS-1$
  }

  protected ToolBarManager getToolbarManager( )
  {
    return m_toolbar;
  }

  protected void hookExecutionListener( final TableViewer tableViewer, final ToolBarManager toolBar )
  {
    final IWorkbench serviceLocator = PlatformUI.getWorkbench();

    m_executionListener = new EmbeddedToolbarExecutionListener( toolBar, serviceLocator );
    m_executionListener.addContextVariable( ToolbarCommandUtils.ACTIVE_TUPLE_RESULT_TABLE_VIEWER_NAME, tableViewer );
    m_executionListener.addContextVariable( ToolbarCommandUtils.ACTIVE_TUPLE_RESULT_FEATURE_CONTROL_NAME, AbstractToolbarFeatureControl.this );
  }

  @Override
  public void dispose( )
  {
    final IWorkbench serviceLocator = PlatformUI.getWorkbench();
    if( m_executionListener != null )
      m_executionListener.dispose();

    if( getToolbarManager() != null )
    {
      final IMenuService service = (IMenuService)serviceLocator.getService( IMenuService.class );
      service.releaseContributions( getToolbarManager() );
    }

    super.dispose();
  }

}
