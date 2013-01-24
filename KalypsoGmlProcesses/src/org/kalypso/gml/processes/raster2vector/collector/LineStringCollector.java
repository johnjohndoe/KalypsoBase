package org.kalypso.gml.processes.raster2vector.collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kalypso.gml.processes.raster2vector.LinkedCoordinate;
import org.kalypso.gml.processes.raster2vector.LinkedCoordinateException;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * Sammelt LineStrings
 * 
 * @author belger
 */
public class LineStringCollector implements SegmentCollector
{
  private final GeometryFactory m_gf;

  private final double[] m_grenzen;

  private final boolean m_bSimple;

  private final List<CollectorDataProvider> m_dataList = new ArrayList<CollectorDataProvider>();

  public LineStringCollector( final GeometryFactory gf, final double[] grenzen, final boolean bSimple )
  {
    m_grenzen = grenzen;
    m_gf = gf;
    m_bSimple = bSimple;
  }

  /**
   * @see org.kalypso.gml.processes.raster2vector.collector.SegmentCollector#addSegment(int,
   *      org.kalypso.gml.processes.raster2vector.LinkedCoordinate,
   *      org.kalypso.gml.processes.raster2vector.LinkedCoordinate)
   */
  @Override
  public void addSegment( final int index, final LinkedCoordinate lc0, final LinkedCoordinate lc1 ) throws LinkedCoordinateException
  {
    try
    {
      if( m_bSimple )
      {
        final Double id = new Double( m_dataList.size() );
        final Double grenze = new Double( m_grenzen[index] );
        final String name = grenze.toString();

        final GM_Object gmGeo = JTSAdapter.wrap( m_gf.createLineString( new Coordinate[] { lc0.crd, lc1.crd } ) );
        if( gmGeo instanceof GM_Curve )
        {
          final CollectorDataProvider dataProvider = new CollectorDataProvider( gmGeo, new Double[] { grenze }, id, new String[] { name } );
          m_dataList.add( dataProvider );
        }
      }
      else
      {
        lc0.link( lc1 );

        if( !lc0.isCircle() )
          return;

        final Collection<LineString> newStrings = lc0.getLineStrings( m_gf );

        for( final LineString lineString : newStrings )
        {
          final GM_Object gmGeo = JTSAdapter.wrap( lineString );

          final Double id = new Double( m_dataList.size() );
          final Double grenze = new Double( m_grenzen[index] );
          final String name = "" + m_grenzen[index]; //$NON-NLS-1$

          if( gmGeo instanceof GM_Curve )
          {
            final CollectorDataProvider dataProvider = new CollectorDataProvider( gmGeo, new Double[] { grenze }, id, new String[] { name } );
            m_dataList.add( dataProvider );
          }
        }
      }
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
    }
  }

  /**
   * @see com.bce.gis.operation.raster2vector.SegmentCollector#getFeatures()
   */
  @Override
  public CollectorDataProvider[] getData( )
  {
    return m_dataList.toArray( new CollectorDataProvider[m_dataList.size()] );
  }

  /**
   * @see org.kalypso.gml.processes.raster2vector.collector.SegmentCollector#finish()
   */
  @Override
  public void finish( )
  {
// nothing to do
  }

  /**
   * @see org.kalypso.gml.processes.raster2vector.collector.SegmentCollector#interpolate(com.vividsolutions.jts.geom.Coordinate,
   *      com.vividsolutions.jts.geom.Coordinate, int)
   */
  @Override
  public LinkedCoordinate interpolate( final Coordinate c1, final Coordinate c2, final int index )
  {
    double zFaktor = -1.0;
    boolean bBorder = false;

    double z1 = c1.z;
    if( Double.isNaN( z1 ) )
    {
      z1 = -1000.0;
      bBorder = true;
    }

    double z2 = c2.z;
    if( Double.isNaN( z2 ) )
    {
      z2 = -1000.0;
      bBorder = true;
    }

    final double value = m_grenzen[index];

    if( (z1 <= value && value < z2) || (z2 <= value && value < z1) )
      zFaktor = (value - z1) / (z2 - z1);

    if( zFaktor < 0 || zFaktor >= 1 )
      return null;

    final double x = zFaktor * (c2.x - c1.x) + c1.x;
    final double y = zFaktor * (c2.y - c1.y) + c1.y;
    return new LinkedCoordinate( new Coordinate( x, y, value ), bBorder );

  }
}