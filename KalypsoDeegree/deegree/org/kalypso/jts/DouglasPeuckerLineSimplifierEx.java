package org.kalypso.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;

/**
 * Extended version of {@link DouglasPeuckerLineSimplifierEx} from JTS, using an arbitrary distance function.<br/>
 * E.g. this allow to us the z coordinates in the distance function as well.
 */
public class DouglasPeuckerLineSimplifierEx
{
  public static Coordinate[] simplify( final Coordinate[] pts, final ISegmentDistance distanceFunction )
  {
    final DouglasPeuckerLineSimplifierEx simp = new DouglasPeuckerLineSimplifierEx( pts, distanceFunction );
    return simp.simplify();
  }

  private final Coordinate[] m_pts;

  private boolean[] m_usePt;

  private final ISegmentDistance m_distanceFunction;

  public DouglasPeuckerLineSimplifierEx( final Coordinate[] pts, final ISegmentDistance distanceFunction )
  {
    m_pts = pts;
    m_distanceFunction = distanceFunction;
  }

  public Coordinate[] simplify( )
  {
    m_usePt = new boolean[m_pts.length];
    for( int i = 0; i < m_pts.length; i++ )
    {
      m_usePt[i] = true;
    }

    simplifySection( 0, m_pts.length - 1 );

    final CoordinateList coordList = new CoordinateList();
    for( int i = 0; i < m_pts.length; i++ )
    {
      if( m_usePt[i] )
        coordList.add( new Coordinate( m_pts[i] ), true );
    }
    return coordList.toCoordinateArray();
  }

  private void simplifySection( final int i, final int j )
  {
    if( (i + 1) == j )
      return;

    double maxDistance = -1.0;
    int maxIndex = i;
    for( int k = i + 1; k < j; k++ )
    {
      final double distance = m_distanceFunction.distance( m_pts[i], m_pts[j], m_pts[k] );
      if( distance > maxDistance )
      {
        maxDistance = distance;
        maxIndex = k;
      }
    }

    if( maxDistance <= m_distanceFunction.getMaxTolerance() )
    {
      for( int k = i + 1; k < j; k++ )
      {
        m_usePt[k] = false;
      }
    }
    else
    {
      simplifySection( i, maxIndex );
      simplifySection( maxIndex, j );
    }
  }
}