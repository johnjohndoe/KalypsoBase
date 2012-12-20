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
package org.kalypsodeegree_impl.model.geometry;

import gnu.trove.TIntProcedure;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Range;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.jts.JTSUtilities;
import org.kalypso.transformation.transformer.GeoTransformerException;
import org.kalypsodeegree.model.elevation.ElevationException;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_PolyhedralSurface;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree.model.geometry.MinMaxSurfacePatchVisitor;

import com.infomatiq.jsi.Rectangle;

/**
 * @author Gernot Belger
 */
final class GM_TriangulatedSurface_Impl extends GM_PolyhedralSurface_Impl<GM_Triangle> implements GM_TriangulatedSurface
{
  private GM_Triangle m_lastHit = null;

  private final boolean m_hasStatistics = false;

  private Range<BigDecimal> m_minMax;

  public GM_TriangulatedSurface_Impl( final String crs ) throws GM_Exception
  {
    this( new ArrayList<GM_Triangle>(), crs );
  }

  public GM_TriangulatedSurface_Impl( final List<GM_Triangle> items, final String crs ) throws GM_Exception
  {
    super( items, crs );
  }

  @Override
  public void dispose( )
  {
    // nothing to do...
  }

  @Override
  protected GM_PolyhedralSurface<GM_Triangle> createCloneInstance( ) throws GM_Exception
  {
    return new GM_TriangulatedSurface_Impl( getCoordinateSystem() );
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == GM_Triangle[].class )
    {
      return toArray( new GM_Triangle[size()] );
    }
    return super.getAdapter( adapter );
  }

  @Override
  public double getValue( final GM_Point location )
  {
    try
    {
      final GM_Point transformedLocation = (GM_Point)location.transform( getCoordinateSystem() );
      final GM_Position position = transformedLocation.getPosition();
      return getValue( position );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return Double.NaN;
    }
  }

  @Override
  public double getValue( final GM_Position position )
  {
    final GM_Triangle triangle = getTriangle( position );
    if( triangle == null )
      return Double.NaN;

    return triangle.getValue( position );
  }

  @Override
  public GM_Triangle getTriangle( final GM_Position position )
  {
    // Protect against concurrent access
    final GM_Triangle lastHit = m_lastHit;
    if( lastHit != null )
    {
      if( lastHit.contains( position ) )
        return lastHit;
    }

    final Rectangle searchEnv = JTSUtilities.toRectangle( position.getX(), position.getY() );

    final GM_Triangle[] result = new GM_Triangle[] { null };

    final TIntProcedure ip = new TIntProcedure()
    {
      @Override
      public boolean execute( final int value )
      {
        final GM_Triangle triangle = get( value );
        if( triangle.contains( position ) )
        {
          result[0] = triangle;
          return false;
        }

        return true;
      }
    };

    getIndex().intersects( searchEnv, ip );

    m_lastHit = result[0];
    return result[0];
  }

  /**
   * Must override, else the wrong type is created.
   */
  @Override
  public GM_Object transform( final String targetCRS ) throws GeoTransformerException
  {
    try
    {
      /* If the target is the same coordinate system, do not transform. */
      final String sourceCRS = getCoordinateSystem();
      if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
        return this;

      final int cnt = size();
      final GM_Triangle[] triangles = new GM_Triangle[cnt];
      for( int i = 0; i < cnt; i++ )
        triangles[i] = (GM_Triangle)get( i ).transform( targetCRS );

      return GeometryFactory.createGM_TriangulatedSurface( triangles, targetCRS );
    }
    catch( final GM_Exception e )
    {
      throw new GeoTransformerException( e );
    }
  }

  @Override
  public double getElevation( final GM_Point location )
  {
    return getValue( location );
  }

  @Override
  public GM_Envelope getBoundingBox( )
  {
    return getEnvelope();
  }

  @Override
  public double getMinElevation( ) throws ElevationException
  {
    buildStatistics();
    return m_minMax.getMinimum().doubleValue();
  }

  @Override
  public double getMaxElevation( ) throws ElevationException
  {
    buildStatistics();
    return m_minMax.getMaximum().doubleValue();
  }

  private void buildStatistics( ) throws ElevationException
  {
    if( m_hasStatistics )
      return;
    final MinMaxSurfacePatchVisitor<GM_Triangle> minMaxVisitor = new MinMaxSurfacePatchVisitor<>();
    final GM_Envelope maxBox = getEnvelope();
    try
    {
      acceptSurfacePatches( maxBox, minMaxVisitor, new NullProgressMonitor() );
    }
    catch( final CoreException e )
    {
      throw new ElevationException( e.getStatus().getMessage(), e );
    }

    final BigDecimal min = minMaxVisitor.getMin();
    final BigDecimal max = minMaxVisitor.getMax();

    m_minMax = Range.between( min, max );
  }
}