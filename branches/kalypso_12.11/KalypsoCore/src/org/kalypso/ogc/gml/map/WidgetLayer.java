/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
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
package org.kalypso.ogc.gml.map;

import java.awt.Graphics;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.ogc.gml.widgets.IWidget;
import org.kalypso.ogc.gml.widgets.WidgetManager;
import org.kalypsodeegree.graphics.transformation.GeoTransform;

/**
 * @author kurzbach
 */
public class WidgetLayer implements IMapLayer
{
  private final IMapPanel m_panel;

  private final WidgetManager m_widgetManager;

  public WidgetLayer( final MapPanel mapPanel, final WidgetManager widgetManager )
  {
    m_panel = mapPanel;
    m_widgetManager = widgetManager;

  }

  @Override
  public String getLabel( )
  {
    return "Widgets"; //$NON-NLS-1$
  }

  @Override
  public IMapPanel getMapPanel( )
  {
    return m_panel;
  }

  @Override
  public void paint( final Graphics g, final GeoTransform world2screen, final IProgressMonitor monitor )
  {
    final IWidget[] widgets = m_widgetManager.getWidgets();
    ArrayUtils.reverse( widgets );
    monitor.beginTask( getLabel(), widgets.length );
    for( final IWidget widget : widgets )
    {
      widget.paint( g, world2screen, new SubProgressMonitor( monitor, 1 ) );
      ProgressUtilities.worked( monitor, 0 );
    }
  }

  @Override
  public void dispose( )
  {
    // nothing to do
  }

}
