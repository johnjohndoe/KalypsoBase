package com.bce.gis.operation.hmo2fli;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.kalypso.grid.IGeoValueProvider;

import com.bce.util.progressbar.Progressable;
import com.vividsolutions.jts.algorithm.PointInRing;
import com.vividsolutions.jts.algorithm.SimplePointInRing;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * <p>
 * Diese Operation sucht zu einer Coordinate alle überdeckenden Ringe einer Geometry raus.
 * </p>
 * <p>
 * Ist die Geometry keine Collection, wird die gleiche Geometry zurückgegeben, wenn sie die Coordinate enthält und ein
 * LinearRing ist
 * </p>
 * <p>
 * Ist die Geometry eine Collection, werden alle LinearRing's zurückgegeben, die die Coordinate enthalten
 * </p>
 * 
 * @author belger
 */
public class GetRingsOperation implements IGeoValueProvider
{
  private final SpatialIndex m_index;

  public GetRingsOperation( final LinearRing[] lrs, final Progressable pm )
  {
    m_index = new STRtree();

    final int num = lrs.length;

    if( pm != null )
      pm.reset( 0, num );

    for( int i = 0; i < num; i++ )
    {
      m_index.insert( lrs[i].getEnvelopeInternal(), lrs[i] );

      if( pm != null )
        pm.setCurrent( i );
    }

    // den Index abschliessen, kann sehr lange dauern
    ((STRtree) m_index).build();
  }

  @SuppressWarnings("unchecked") //$NON-NLS-1$
  public final LinearRing[] getAllContaining( final Coordinate c )
  {
    final Vector<LinearRing> results = new Vector<LinearRing>();

    final List envs = m_index.query( new Envelope( c ) );

    for( final Iterator it = envs.iterator(); it.hasNext(); )
    {
      final LinearRing lr = (LinearRing) it.next();
      final PointInRing pir = new SimplePointInRing( lr );

      if( pir.isInside( c ) )
        results.add( lr );
    }

    return results.toArray( new LinearRing[results.size()] );
  }

  public final double getValue( final Coordinate c )
  {
    final LinearRing[] rings = getAllContaining( c );
    return rings.length == 0 ? Double.NaN : new HeightFromTriangle( rings[0] ).getHeight( c );
  }
}
