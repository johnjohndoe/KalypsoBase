/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Bj�rnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universit�t Hamburg-Harburg, Institut f�r Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.afgui.internal.handlers;

import java.util.Properties;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.mapmodel.ActivateThemeJob;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.MapModellHelper;
import org.kalypso.ui.views.map.MapView;

/**
 * Activates a given theme in the current map view.
 * 
 * @author Stefan Kurzbach
 */
public class ThemeContextHandler extends AbstractHandler
{
  private final String m_featureType;

  private ActivateThemeJob m_activateThemeJob;

  public ThemeContextHandler( final Properties properties )
  {
    m_featureType = properties.getProperty( KalypsoContextHandlerFactory.PARAM_INPUT );

    Assert.isNotNull( m_featureType, Messages.getString( "org.kalypso.afgui.handlers.ThemeContextHandler.0" ) ); //$NON-NLS-1$
  }

  @Override
  public void dispose( )
  {
    if( m_activateThemeJob != null )
      m_activateThemeJob.dispose();

    super.dispose();
  }

  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext)event.getApplicationContext();

    final Shell shell = (Shell)context.getVariable( ISources.ACTIVE_SHELL_NAME );
    final IWorkbenchWindow window = (IWorkbenchWindow)context.getVariable( ISources.ACTIVE_WORKBENCH_WINDOW_NAME );
    final IWorkbenchPage activePage = window == null ? null : window.getActivePage();

    // TODO: do not program against a fixed view id; better try to adapt part to MapPanel instead: this works for every
    // part containing a map-panel
    final IViewPart view = activePage == null ? null : activePage.findView( MapView.ID );

    if( m_featureType != null && view != null && view instanceof MapView )
    {
      final MapView mapView = (MapView)view;
      final IMapPanel mapPanel = mapView.getMapPanel();

      MapModellHelper.waitForAndErrorDialog( shell, mapPanel, Messages.getString( "org.kalypso.afgui.handlers.ThemeContextHandler.1" ), Messages.getString( "org.kalypso.afgui.handlers.ThemeContextHandler.2" ) ); //$NON-NLS-1$ //$NON-NLS-2$

      final IMapModell mapModell = mapPanel.getMapModell();

      if( mapModell == null )
        return Status.CANCEL_STATUS;

      final String featureType = m_featureType;
      m_activateThemeJob = new ActivateThemeJob( mapModell, Messages.getString( "org.kalypso.afgui.handlers.ThemeContextHandler.3" ), featureType ); //$NON-NLS-1$
      m_activateThemeJob.setRule( mapView.getSchedulingRule().getActivateLayerSchedulingRule() );
      m_activateThemeJob.setUser( true );
      m_activateThemeJob.schedule();
    }

    return Status.OK_STATUS;
  }
}
