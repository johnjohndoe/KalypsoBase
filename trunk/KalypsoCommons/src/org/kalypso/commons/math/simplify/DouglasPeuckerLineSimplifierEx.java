package org.kalypso.commons.math.simplify;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Extended version of {@link DouglasPeuckerLineSimplifierEx} from JTS, using an arbitrary distance function on arbitrary coordinate objects<br/>
 * E.g. this allow to us the z coordinates in the distance function as well.
 */
public class DouglasPeuckerLineSimplifierEx<COORD>
{
  public static <COORD> COORD[] simplify( final COORD[] pts, final ISegmentDistance<COORD> distanceFunction )
  {
    final DouglasPeuckerLineSimplifierEx<COORD> simp = new DouglasPeuckerLineSimplifierEx<>( pts, distanceFunction );
    return simp.simplify();
  }

  private final COORD[] m_pts;

  private final boolean[] m_usePt;

  private final ISegmentDistance<COORD> m_distanceFunction;

  public DouglasPeuckerLineSimplifierEx( final COORD[] pts, final ISegmentDistance<COORD> distanceFunction )
  {
    m_pts = pts;
    m_distanceFunction = distanceFunction;

    m_usePt = new boolean[m_pts.length];
    for( int i = 0; i < m_pts.length; i++ )
      m_usePt[i] = true;
  }

  public COORD[] simplify( )
  {
    if( m_pts.length < 3 )
      return m_pts;

    simplifySection( 0, m_pts.length - 1 );

    final Collection<COORD> coordList = new ArrayList<>();

    for( int i = 0; i < m_pts.length; i++ )
    {
      if( m_usePt[i] )
        coordList.add( m_pts[i] );
    }

    final Class< ? extends COORD> clazz = (Class< ? extends COORD>)m_pts[0].getClass();
    final COORD[] coordArray = (COORD[])Array.newInstance( clazz, coordList.size() );
    return coordList.toArray( coordArray );
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