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
import java.awt.Polygon;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypsodeegree.graphics.displayelements.DisplayElement;
import org.kalypsodeegree.graphics.displayelements.DisplayElementDecorator;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_AbstractSurfacePatch;
import org.kalypsodeegree.model.geometry.ISurfacePatchVisitable;
import org.kalypsodeegree.model.geometry.ISurfacePatchVisitor;

/**
 * Provide display mechanism for terrain elevation models
 *
 * @author Madanagopal
 * @author Patrice Congo
 */
public class SurfacePatchVisitableDisplayElement<P extends GM_AbstractSurfacePatch> implements DisplayElementDecorator
{
  public interface IVisitorFactory<P2 extends GM_AbstractSurfacePatch>
  {
    ISurfacePatchVisitor<P2> createVisitor( final Graphics g, final GeoTransform projection );
  }

  private final ISurfacePatchVisitable<P> m_surfacePatchVisitable;

  private final Feature m_feature;

  private DisplayElement m_decorated;

  private final IVisitorFactory<P> m_visitorFactory;

  public SurfacePatchVisitableDisplayElement( final Feature feature, final ISurfacePatchVisitable<P> surfacePatchVisitable, final IVisitorFactory<P> visitorFactory )
  {
    Assert.isNotNull( surfacePatchVisitable );

    m_visitorFactory = visitorFactory;
    m_feature = feature;
    m_surfacePatchVisitable = surfacePatchVisitable;
  }

  @Override
  public Feature getFeature( )
  {
    return m_feature;
  }

  @Override
  public void paint( final Graphics g, final GeoTransform projection, final IProgressMonitor monitor ) throws CoreException
  {
    if( m_decorated != null )
    {
      m_decorated.paint( g, projection, monitor );
    }

    try
    {
      final ISurfacePatchVisitor<P> visitor = m_visitorFactory.createVisitor( g, projection );
      m_surfacePatchVisitable.acceptSurfacePatches( projection.getSourceRect(), visitor, monitor );
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
    }
  }

  public static final Polygon areaFromRing( final GeoTransform projection, final float width, final GM_Position[] ex )
  {
    final int[] x = new int[ex.length];
    final int[] y = new int[ex.length];

    int k = 0;
    for( final GM_Position element : ex )
    {
      final GM_Position position = projection.getDestPoint( element );
      final int xx = (int) (position.getX() + 0.5);
      final int yy = (int) (position.getY() + 0.5);

      if( k > 0 && k < ex.length - 1 )
      {
        if( distance( xx, yy, x[k - 1], y[k - 1] ) > width )
        {
          x[k] = xx;
          y[k] = yy;
          k++;
        }
      }
      else
      {
        x[k] = xx;
        y[k] = yy;
        k++;
      }
    }

    return new Polygon( x, y, k - 1 );
  }

  public static final double distance( final double x1, final double y1, final double x2, final double y2 )
  {
    return Math.sqrt( (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) );
  }

  @Override
  public void setDecorated( final DisplayElement decorated )
  {
    m_decorated = decorated;
  }
}