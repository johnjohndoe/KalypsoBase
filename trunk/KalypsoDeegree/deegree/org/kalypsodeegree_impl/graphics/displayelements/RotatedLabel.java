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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;

import org.kalypsodeegree.graphics.sld.Halo;
import org.kalypsodeegree.model.feature.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * This is a rotated label with style information and screen coordinates, ready to be rendered to the view.
 * <p>
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */
class RotatedLabel extends AbstractLabel
{
  private static final GeometryFactory GF = new GeometryFactory();

  private final double m_rotation;

  RotatedLabel( final String caption, final Font font, final Color color, final LineMetrics metrics, final Feature feature, final Halo halo, final int x, final int y, final Dimension bounds, final double rotation, final double anchorPoint[], final double[] displacement )
  {
    super( caption, font, color, metrics, feature, halo, bounds );

    m_rotation = rotation;

    final int width = getWidth();
    final int height = getHeight();

    final Coordinate[] coordinates = new Coordinate[5];

    coordinates[0] = new Coordinate( x, y );
    coordinates[1] = new Coordinate( x + width, y );
    coordinates[2] = new Coordinate( x + width, y - height );
    coordinates[3] = new Coordinate( x, y - height );

    // transform all vertices of the boundary
    for( int i = 0; i < coordinates.length - 1; i++ )
    {
      final Coordinate point = transformPoint( coordinates[i], coordinates[0], rotation, anchorPoint[0], anchorPoint[1], bounds, displacement[0], displacement[1] );
      coordinates[i] = point;
    }
    coordinates[4] = coordinates[0];

    final LinearRing ring = GF.createLinearRing( coordinates );
    final Polygon boundary = GF.createPolygon( ring, null );
    setBoundary( boundary );
  }

  public double getRotation( )
  {
    return m_rotation;
  }

  @Override
  public void paint( final Graphics2D g )
  {
    // get the current transform
    final AffineTransform saveAT = g.getTransform();

    final Polygon bounds = getBounds();
    final Coordinate[] coordinates = bounds.getCoordinates();
    final int anchorX = (int) coordinates[0].x;
    final int anchorY = (int) coordinates[0].y;

    // perform transformation
    final AffineTransform transform = new AffineTransform();
    transform.rotate( m_rotation, anchorX, anchorY );
    g.setTransform( transform );

    doPaint( g );

    // restore original transform
    g.setTransform( saveAT );
  }

  private static Coordinate transformPoint( final Coordinate crd, final Coordinate t, final double rotation, final double anchorPointX, final double anchorPointY, final Dimension bounds, final double displacementX, final double displacementY )
  {
    final double cos = Math.cos( rotation );
    final double sin = Math.sin( rotation );
    final double dx = -anchorPointX * bounds.width;
    final double dy = anchorPointY * bounds.height - displacementY;

    final double x = crd.x;
    final double y = crd.y;
    final double tx = t.x;
    final double ty = t.y;

    final double m00 = cos;
    final double m01 = -sin;
    final double m02 = cos * dx - sin * dy + tx - tx * cos + ty * sin;
    final double m10 = sin;
    final double m11 = cos;
    final double m12 = sin * dx + cos * dy + ty - tx * sin - ty * cos;

    final double pointX = m00 * x + m01 * y + m02 + 0.5 + displacementX;
    final double pointY = m10 * x + m11 * y + m12 + 0.5;

    return new Coordinate( pointX, pointY );
  }
}