package org.kalypso.gml.processes.raster2vector.collector;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.gml.processes.i18n.Messages;
import org.kalypso.gml.processes.raster2vector.LinkedCoordinate;
import org.kalypso.gml.processes.raster2vector.LinkedCoordinateException;
import org.kalypso.gml.processes.raster2vector.ringtree.RingTree;
import org.kalypso.gml.processes.raster2vector.ringtree.RingTreeElement;
import org.kalypso.gml.processes.raster2vector.ringtree.RingTreeWalker;
import org.kalypso.jts.CoordOrientation;
import org.kalypso.jts.CoordOrientationException;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.algorithm.PointInRing;
import com.vividsolutions.jts.algorithm.SIRtreePointInRing;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Sammelt LineStrings und kombiniert Sie als Polygone
 * 
 * @author belger
 */
public class PolygonCollector implements SegmentCollector, RingTreeWalker
{
  private final List<CollectorDataProvider> m_dataList = new ArrayList<CollectorDataProvider>();

  private final GeometryFactory m_gf;

  private final Interval[] m_intervals;

  private final boolean m_bSimple;

  private final RingTree m_tree = new RingTree();

  public PolygonCollector( final GeometryFactory gf, final double[] grenzen, final boolean bSimple )
  {
    m_gf = gf;
    m_bSimple = bSimple;

    m_intervals = new Interval[grenzen.length + 1];
    m_intervals[0] = new Interval( Double.MIN_VALUE, grenzen[0] );
    for( int i = 0; i < grenzen.length - 1; i++ )
      m_intervals[i + 1] = new Interval( grenzen[i], grenzen[i + 1] );
    m_intervals[grenzen.length] = new Interval( grenzen[grenzen.length - 1], Double.MAX_VALUE );

  }

  /**
   * @see com.bce.gis.operation.raster2vector.SegmentCollector#addSegment(int,
   *      com.bce.gis.operation.raster2vector.LinkedCoordinate, com.bce.gis.operation.raster2vector.LinkedCoordinate,
   *      com.vividsolutions.jts.geom.Coordinate, com.vividsolutions.jts.geom.Coordinate)
   */
  public void addSegment( final int index, final LinkedCoordinate lc0, final LinkedCoordinate lc1, final Coordinate nearC0, final Coordinate nearC1 ) throws LinkedCoordinateException
  {
    lc0.link( lc1 );

    if( !lc0.isCircle() )
      return;

    try
    {
      final Coordinate[] crds = lc0.getAsRing();

      if( m_bSimple )
      {
        final LinearRing lr = m_gf.createLinearRing( crds );
        final Polygon poly = m_gf.createPolygon( lr, new LinearRing[] {} );
        appendFeature( index, poly );
      }
      else
      {
        final PointInRing pir = new SIRtreePointInRing( m_gf.createLinearRing( crds ) );
        Coordinate innerCrd = null;
        if( pir.isInside( nearC0 ) )
          innerCrd = nearC0;
        else if( pir.isInside( nearC1 ) )
          innerCrd = nearC1;
        else
          System.out.println( Messages.getString("org.kalypso.gml.processes.raster2vector.collector.PolygonCollector.0") ); //$NON-NLS-1$

        CoordOrientation.orient( crds, CoordOrientation.TYPE.NEGATIV );

        // REMARK: We remove z because a) it has no meaning for the polygons (they represent a range) and b) we get
        // strange
        // problems (ring not closed, as first and last coordinates has different z's)
        final Coordinate[] crdsNoZ = new Coordinate[crds.length];
        for( int i = 0; i < crdsNoZ.length; i++ )
          crdsNoZ[i] = new Coordinate( crds[i].x, crds[i].y );

        final LinearRing lr = m_gf.createLinearRing( crdsNoZ );
        final Polygon polygon = m_gf.createPolygon( lr, null );
        m_tree.insertElement( new RingTreeElement( lr, polygon, index, innerCrd ) );
      }
    }
    catch( final CoordOrientationException coe )
    {
      coe.printStackTrace();
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
    }
  }

  /**
   * @param index
   * @param poly
   * @throws GM_Exception
   */
  private void appendFeature( final int index, final Polygon poly ) throws GM_Exception
  {
    final GM_Object gmGeo = JTSAdapter.wrap( poly );

    final Double id = new Double( m_dataList.size() );
    final String name = m_intervals[index].toString();

    final Double von = new Double( Math.max( -9999.99, m_intervals[index].getMin() ) );
    final Double bis = new Double( Math.min( 9999.99, m_intervals[index].getMax() ) );
    final Double volumen = Double.NaN;

    CollectorDataProvider dataProvider = new CollectorDataProvider( gmGeo, new Double[] { von, bis, volumen }, id, new String[] { name } );
    m_dataList.add( dataProvider );
  }

  /**
   * @see com.bce.gis.operation.raster2vector.collector.ringtree.RingTreeWalker#operate(com.bce.gis.operation.raster2vector.collector.ringtree.RingTreeElement)
   */
  public void operate( final RingTreeElement element )
  {
    final Polygon[] p = element.getAsPolygon();

    if( p != null )
    {
      try
      {
        final int intIndex = element.index + 1;

        final double value = element.innerCrd.z;
        if( !Double.isNaN( value ) )
        {
          for( Polygon polygon : p )
            appendFeature( intIndex, polygon );
        }
      }
      catch( final GM_Exception gme )
      {
        gme.printStackTrace();
      }
    }
  }

  private static class Interval
  {
    private final double m_min;

    private final double m_max;

    public double getMin( )
    {
      return m_min;
    }

    public double getMax( )
    {
      return m_max;
    }

    public Interval( final double min, final double max )
    {
      m_min = min;
      m_max = max;
    }

    public boolean contains( final double value )
    {
      if( m_min == Double.MIN_VALUE )
        return value < m_max;
      else if( m_max == Double.MAX_VALUE )
        return m_min <= value;
      else
        return m_min <= value && value < m_max;
    }

    @Override
    public String toString( )
    {
      final StringBuffer str = new StringBuffer();
      if( m_min == Double.MIN_VALUE )
        str.append( "-Inf" ); //$NON-NLS-1$
      else
        str.append( m_min );

      str.append( " - " ); //$NON-NLS-1$

      if( m_max == Double.MAX_VALUE )
        str.append( "Inf" ); //$NON-NLS-1$
      else
        str.append( m_max );

      return str.toString();
    }
  }

  /**
   * @see org.kalypso.grid.processes.raster2vector.SegmentCollector#getData()
   */
  public CollectorDataProvider[] getData( )
  {
    if( !m_bSimple )
      m_tree.walk( this );

    return m_dataList.toArray( new CollectorDataProvider[m_dataList.size()] );
  }

  /**
   * @see org.kalypso.grid.processes.raster2vector.collector.ringtree.RingTreeWalker#getResult()
   */
  public Object getResult( )
  {
    // TODO Auto-generated method stub
    return null;
  }
}
