package com.bce.gis.io.zweidm;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.gml.processes.i18n.Messages;

import com.vividsolutions.jts.algorithm.RobustCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * <p>
 * Writes a set of linear rings into a 2dm mesh file.
 * </p>
 * <p>
 * Each linear ring must be a closed triangle, which will be oriented counter clock wise before writing.
 * </p>
 * 
 * @author Gernot Belger
 */
public class ZweiDMWriter
{
  private final LinearRing[] m_rings;

  public ZweiDMWriter( final LinearRing[] rings )
  {
    m_rings = rings;
  }

  public void write( final Writer w )
  {
    final PrintWriter pw = new PrintWriter( w );

    // write head
    pw.println( "MESH2D" ); //$NON-NLS-1$
    pw.println( "T1" ); //$NON-NLS-1$
    pw.println( "T2" ); //$NON-NLS-1$
    pw.println( "T3" ); //$NON-NLS-1$

    // write triangles
    // IMPORTANT: use linked hashmap, so the orering of insertion stays the same
    final LinkedHashMap<Coordinate, Integer> crdSet = new LinkedHashMap<Coordinate, Integer>( m_rings.length * 3 );
    for( int i = 0; i < m_rings.length; i++ )
    {
      final LinearRing ring = m_rings[i];

      pw.print( "E3T " ); //$NON-NLS-1$
      pw.print( i + 1 );
      pw.print( " " ); //$NON-NLS-1$

      final Coordinate[] coordinates = ring.getCoordinates();
      if( coordinates == null || coordinates.length != 4 )
        throw new IllegalArgumentException( Messages.getString("com.bce.gis.io.zweidm.ZweiDMWriter.6") + ring ); //$NON-NLS-1$

      if( !RobustCGAlgorithms.isCCW( coordinates ) )
        ArrayUtils.reverse( coordinates );

      for( int j = 0; j < coordinates.length - 1; j++ )
      {
        final Coordinate crd = coordinates[j];

        final int nodeIndex;
        if( crdSet.containsKey( crd ) )
          nodeIndex = crdSet.get( crd );
        else
        {
          nodeIndex = crdSet.size() + 1;
          crdSet.put( crd, nodeIndex );
        }

        pw.print( nodeIndex );
        pw.print( " " ); //$NON-NLS-1$
      }

      pw.println( "1" ); //$NON-NLS-1$
    }

    // write nodes
    for( final Map.Entry<Coordinate, Integer> entry : crdSet.entrySet() )
    {
      final Coordinate crd = entry.getKey();
      final int nodeIndex = entry.getValue();

      pw.print( "ND " ); //$NON-NLS-1$
      pw.print( nodeIndex );
      pw.print( " " ); //$NON-NLS-1$
      pw.printf( Locale.ENGLISH, "%.3f", crd.x ); //$NON-NLS-1$
      pw.print( " " ); //$NON-NLS-1$
      pw.printf( Locale.ENGLISH, "%.3f", crd.y ); //$NON-NLS-1$
      pw.print( " " ); //$NON-NLS-1$
      pw.printf( Locale.ENGLISH, "%.2f", crd.z ); //$NON-NLS-1$
      pw.println();
    }
  }

}
