/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.ogc.gml.map.widgets.advanced.selection.delegates;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.Assert;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidget;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetDataProvider;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Ring;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;

/**
 * @author Dirk Kuch
 */
public class RemovePolygonDelegate extends AbstractAdvancedSelectionWidgetDelegate
{
  private Image m_imgCursor;

  public RemovePolygonDelegate( final IAdvancedSelectionWidget widget, final IAdvancedSelectionWidgetDataProvider provider )
  {
    super( widget, provider );
  }

  /**
   * @see org.kalypso.planer.client.ui.gui.widgets.measures.aw.IAdvancedSelectionWidgetDelegate#leftReleased(java.awt.Point)
   */
  @Override
  public void leftReleased( final Point p )
  {
    super.leftReleased( p );

    try
    {
      final GM_Point point = getWidget().getCurrentGmPoint();
      final Feature[] features = getDataProvider().query( point, 0.1, getWidget().getEditMode() );

      getDataProvider().post( features, getWidget().getEditMode() );
    }
    catch( final Exception e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

  }

  /**
   * @see org.kalypso.planer.client.ui.gui.widgets.measures.aw.AbstractAdvancedSelectionWidgetDelegate#highlightUnderlying(org.kalypsodeegree.model.feature.Feature,
   *      java.awt.Graphics)
   */
  @Override
  protected void highlightUnderlying( final Feature feature, final Graphics g )
  {
    final GM_Surface<GM_SurfacePatch> surface = (GM_Surface<GM_SurfacePatch>) getDataProvider().resolveGeometry( feature );

    final Color originalColor = g.getColor();
    g.setColor( new Color( 255, 0, 0, 128 ) );

    final GM_Ring ring = surface.getSurfaceBoundary().getExteriorRing();
    final GM_Position[] positions = ring.getPositions();

    int[] x_positions = new int[] {};
    int[] y_positions = new int[] {};

    for( final GM_Position position : positions )
    {
      final Point awt = MapUtilities.retransform( getWidget().getIMapPanel(), position );
      x_positions = ArrayUtils.add( x_positions, Double.valueOf( awt.getX() ).intValue() );
      y_positions = ArrayUtils.add( y_positions, Double.valueOf( awt.getY() ).intValue() );
    }

    Assert.isTrue( x_positions.length == y_positions.length );
    g.fillPolygon( x_positions, y_positions, x_positions.length );

    g.setColor( originalColor );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetDelegate#getCursor()
   */
  @Override
  public Cursor getCursor( )
  {
    try
    {
      if( m_imgCursor == null )
        m_imgCursor = ImageIO.read( RemovePolygonDelegate.class.getResourceAsStream( "images/cursor_remove.png" ) );

      return super.getCursor( m_imgCursor );
    }
    catch( final IOException e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return null;
  }
}
