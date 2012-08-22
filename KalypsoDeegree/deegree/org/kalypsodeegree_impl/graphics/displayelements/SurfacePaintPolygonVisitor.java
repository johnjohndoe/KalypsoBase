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

import java.awt.Graphics;
import java.awt.Graphics2D;

import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree.model.geometry.ISurfacePatchVisitor;
import org.kalypsodeegree_impl.graphics.sld.awt.FillPainter;
import org.kalypsodeegree_impl.graphics.sld.awt.StrokePainter;

/**
 * TODO: adapt this code for planar polygons (at least quadrilaterals) -> no: instead wrap the visitor, so it iterates
 * over triangles created by splitting up the quadrupels. Else, the quadrupels are not cirrectly painted.
 * 
 * @author Gernot Belger
 * @author Thomas Jung
 */
public class SurfacePaintPolygonVisitor implements ISurfacePatchVisitor<GM_Triangle>
{
  private final ColorMapConverter m_colorModel;

  private final TrianglePainter m_painter;

  public SurfacePaintPolygonVisitor( final Graphics gc, final ColorMapConverter colorModel )
  {
    m_painter = new TrianglePainter( (Graphics2D) gc );
    m_colorModel = colorModel;
  }

  @Override
  public boolean visit( final GM_Triangle polygon )
  {
    paintTriangle( polygon );
    return true;
  }

  private void paintTriangle( final GM_Triangle polygon )
  {
    final int numOfClasses = m_colorModel.getNumOfClasses();

    final GM_Position[] positions = polygon.getExteriorRing();

    /* loop over all classes */
    for( int currentClass = 0; currentClass < numOfClasses; currentClass++ )
    {
      final double startValue = m_colorModel.getFrom( currentClass );
      final double endValue = m_colorModel.getTo( currentClass );

      final StrokePainter strokePainter = m_colorModel.getLinePainter( currentClass );
      final FillPainter fillPainter = m_colorModel.getFillPolygonPainter( currentClass );
      final GeoTransform world2Screen = fillPainter.getWorld2Screen();

      m_painter.paint( positions, startValue, endValue, strokePainter, fillPainter, world2Screen );
    }
  }
}