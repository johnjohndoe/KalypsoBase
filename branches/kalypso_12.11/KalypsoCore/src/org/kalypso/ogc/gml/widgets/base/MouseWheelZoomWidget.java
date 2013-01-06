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
package org.kalypso.ogc.gml.widgets.base;

import java.awt.event.MouseWheelEvent;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.KalypsoCorePreferences;
import org.kalypso.ogc.gml.command.ChangeExtentCommand;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.MapPanelUtilities;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Point;

/**
 * @author Andreas von Dömming
 */
public class MouseWheelZoomWidget extends AbstractWidget
{

  public MouseWheelZoomWidget( final String name, final String toolTip )
  {
    super( name, toolTip );
  }

  public MouseWheelZoomWidget( )
  {
    super( "Mouse Wheel Zoom Widget", "" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void finish( )
  {
  }

  @Override
  public WIDGET_TYPE getType( )
  {
    return WIDGET_TYPE.eBackground;
  }

  @Override
  public void mouseWheelMoved( final MouseWheelEvent e )
  {
    final IMapPanel mapPanel = getMapPanel();
    if( Objects.isNull( mapPanel ) )
      return;

    final int wheelRotation = e.getWheelRotation();

    final boolean in = wheelRotation > 0 ? false : true;

    GM_Envelope boundingBox = mapPanel.getBoundingBox();
    if( boundingBox == null )
      return;

    final boolean panToMousePosition = KalypsoCorePreferences.isMapKeepPositionOnWheel();
    final boolean invertWheelDirection = KalypsoCorePreferences.invertMapWheelZoom();
    final boolean direction = invertWheelDirection ? !in : in;

    final GM_Point mousePosition = panToMousePosition ? MapUtilities.transform( mapPanel, e.getPoint() ) : boundingBox.getCenter();

    for( int i = 0; i < Math.abs( wheelRotation ); i++ )
      boundingBox = MapPanelUtilities.calcZoomInBoundingBox( boundingBox, mousePosition.getPosition(), direction );

    getCommandTarget().postCommand( new ChangeExtentCommand( mapPanel, boundingBox ), null );

    e.consume();
  }
}