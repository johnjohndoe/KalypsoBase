/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.graphics.displayelements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.displayelements.PointDisplayElement;
import org.kalypsodeegree.graphics.sld.Graphic;
import org.kalypsodeegree.graphics.sld.PointSymbolizer;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.graphics.sld.PointSymbolizer_Impl;
import org.kalypsodeegree_impl.graphics.sld.Symbolizer_Impl.UOM;

/**
 * DisplayElement that encapsulates a point geometry (<tt>GM_Point</tt>) and a <tt>PointSymbolizer</tt>.
 * <p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */
class PointDisplayElement_Impl extends GeometryDisplayElement_Impl implements PointDisplayElement
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -2979559276151855757L;

  private transient static Image defaultImg = new BufferedImage( 7, 7, BufferedImage.TYPE_INT_ARGB );

  static
  {
    final Graphics g = defaultImg.getGraphics();
    g.setColor( Color.LIGHT_GRAY );
    g.fillRect( 0, 0, 9, 9 );
    g.dispose();
  }

  /**
   * Creates a new PointDisplayElement_Impl object.
   *
   * @param feature
   * @param geometry
   */
  PointDisplayElement_Impl( final Feature feature, final GM_Point[] points )
  {
    this( feature, points, new PointSymbolizer_Impl() );
  }

  /**
   * Creates a new PointDisplayElement_Impl object.
   *
   * @param feature
   * @param geometry
   * @param symbolizer
   */
  PointDisplayElement_Impl( final Feature feature, final GM_Point[] points, final PointSymbolizer symbolizer )
  {
    super( feature, points, symbolizer );
  }

  /**
   * Renders the DisplayElement to the submitted graphic context.
   */
  @Override
  public void paint( final Graphics g, final GeoTransform projection, final IProgressMonitor monitor )
  {
    try
    {
      final GM_Point[] points = (GM_Point[]) getGeometry();
      final PointSymbolizer symbolizer = (PointSymbolizer) getSymbolizer();
      final Feature feature = getFeature();

      drawPoints( g, projection, symbolizer, feature, points );
    }
    catch( final FilterEvaluationException e )
    {
      e.printStackTrace();
    }
  }

  protected static void drawPoints( final Graphics g, final GeoTransform projection, final PointSymbolizer symbolizer, final Feature feature, final GM_Point[] points ) throws FilterEvaluationException
  {
    final UOM uom = symbolizer.getUom();
    final Graphic graphic = symbolizer.getGraphic();
    /* No graphic -> no paint... maybe we should paint some default? */
    if( graphic == null )
      return;

    final double rotation = graphic.getRotation( feature );

    final Graphics2D g2D = (Graphics2D) g;

    for( final GM_Point point : points )
      drawPoint( g2D, point, projection, feature, graphic, rotation, uom );
  }

  /**
   * Renders one point to the submitted graphic context considering the given projection.
   */
  private static void drawPoint( final Graphics2D g, final GM_Point point, final GeoTransform projection, final Feature feature, final Graphic graphic, final double rotation, final UOM uom ) throws FilterEvaluationException
  {
    final GM_Position source = point.getPosition();
    // why plus 0.5?
    final int x = (int) Math.ceil( projection.getDestX( source.getX() ) );
    final int y = (int) Math.ceil( projection.getDestY( source.getY() ) );

    final int size = graphic == null ? defaultImg.getWidth( null ) : graphic.getNormalizedSize( feature, uom, projection );

    /* Center graphics context on middle of excepted image and rotate according to rotation. */
    final int halfSize = size >> 1;
    final int x_ = x - halfSize;
    final int y_ = y - halfSize;

    g.translate( x, y );
    g.rotate( Math.toRadians( rotation ) );
    g.translate( -halfSize, -halfSize );

    if( graphic == null )
      g.drawImage( defaultImg, x_, y_, null );
    else
      graphic.paintAwt( g, size, feature );

    g.translate( halfSize, halfSize );
    g.rotate( -Math.toRadians( rotation ) );
    g.translate( -x, -y );
  }
}