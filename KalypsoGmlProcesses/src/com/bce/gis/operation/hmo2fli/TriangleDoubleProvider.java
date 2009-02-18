package com.bce.gis.operation.hmo2fli;

import java.util.List;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.kalypso.gml.processes.schemata.GmlProcessesUrlCatalog;
import org.kalypso.grid.IGeoValueProvider;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.algorithm.PointInRing;
import com.vividsolutions.jts.algorithm.SimplePointInRing;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

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
public class TriangleDoubleProvider implements IGeoValueProvider
{
  private FeatureList m_triangleList;

  public final QName QN_GEOM_TRIANGLE;

  public TriangleDoubleProvider( final FeatureList triangleList )
  {
    m_triangleList = triangleList;
    QN_GEOM_TRIANGLE = new QName( GmlProcessesUrlCatalog.NS_MESH, "triangle" ); //$NON-NLS-1$
  }

  @SuppressWarnings("unchecked") //$NON-NLS-1$
  private final LinearRing[] getAllContaining( final Coordinate c ) throws GM_Exception
  {
    final Vector<LinearRing> results = new Vector<LinearRing>();

    final GM_Position position = GeometryFactory.createGM_Position( c.x, c.y );
    final List list = m_triangleList.query( position, null );

    for( final Object listItem : list )
    {
      final Feature feature = (Feature) listItem;
      final GM_Surface surface = (GM_Surface) feature.getProperty( QN_GEOM_TRIANGLE );

      final Geometry geometry = JTSAdapter.export( surface );
      if( geometry instanceof Polygon )
      {
        final Polygon poly = (Polygon) geometry;
        final LinearRing exteriorRing = (LinearRing) poly.getExteriorRing();

        final PointInRing pir = new SimplePointInRing( exteriorRing );
        if( pir.isInside( c ) )
          results.add( exteriorRing );
      }
    }

    // final List envs = m_index.query( new Envelope( c ) );
    //
    // for( final Iterator it = envs.iterator(); it.hasNext(); )
    // {
    // final LinearRing lr = (LinearRing) it.next();
    // final PointInRing pir = new SimplePointInRing( lr );
    //
    // if( pir.isInside( c ) )
    // results.add( lr );
    // }

    return results.toArray( new LinearRing[results.size()] );
  }

  public final double getValue( final Coordinate c )
  {
    try
    {
      final LinearRing[] rings = getAllContaining( c );
      return rings.length == 0 ? Double.NaN : new HeightFromTriangle( rings[0] ).getHeight( c );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      return Double.NaN;
    }
  }
}
