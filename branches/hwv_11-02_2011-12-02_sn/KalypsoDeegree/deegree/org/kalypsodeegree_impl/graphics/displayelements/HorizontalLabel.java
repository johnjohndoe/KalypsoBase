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

import org.kalypsodeegree.graphics.sld.Halo;
import org.kalypsodeegree.model.feature.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * This is a horizontal label with style information and screen coordinates, ready to be rendered to the view.
 * <p>
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */
class HorizontalLabel extends AbstractLabel
{
  private static final GeometryFactory GF = new GeometryFactory();

  HorizontalLabel( final String caption, final Font font, final Color color, final LineMetrics metrics, final Feature feature, final Halo halo, final int x, final int y, final Dimension bounds, final double anchorPoint[], final double[] displacement )
  {
    super( caption, font, color, metrics, feature, halo, bounds );

    final int width = getWidth();
    final int height = getHeight();

    final int dx = (int) (-anchorPoint[0] * width + displacement[0] + 0.5);
    final int dy = (int) (anchorPoint[1] * height - displacement[1] + 0.5);

    final Coordinate[] coordinates = new Coordinate[5];

    coordinates[0] = new Coordinate( x + dx, y + dy );
    coordinates[1] = new Coordinate( x + dx + width, y + dy );
    coordinates[2] = new Coordinate( x + dx + width, y + dy - height );
    coordinates[3] = new Coordinate( x + dx, y + dy - height );
    coordinates[4] = coordinates[0];

    final LinearRing ring = GF.createLinearRing( coordinates );
    final Polygon boundary = GF.createPolygon( ring, null );
    setBoundary( boundary );
  }

  @Override
  public void paint( final Graphics2D g )
  {
    doPaint( g );
  }
}