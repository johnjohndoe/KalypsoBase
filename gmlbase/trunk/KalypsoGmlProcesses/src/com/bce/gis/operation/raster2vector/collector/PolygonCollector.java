package com.bce.gis.operation.raster2vector.collector;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.GMLSchemaFactory;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.ogc.gml.serialize.ShapeSerializer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.bce.ext.jts.CoordOrientation;
import com.bce.ext.jts.CoordOrientationException;
import com.bce.gis.operation.raster2vector.LinkedCoordinate;
import com.bce.gis.operation.raster2vector.LinkedCoordinateException;
import com.bce.gis.operation.raster2vector.SegmentCollector;
import com.bce.gis.operation.raster2vector.collector.ringtree.RingTree;
import com.bce.gis.operation.raster2vector.collector.ringtree.RingTreeElement;
import com.bce.gis.operation.raster2vector.collector.ringtree.RingTreeWalker;
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
  public static final IValuePropertyType FROM_PROP = GMLSchemaFactory.createValuePropertyType( new QName( FEATURE_BASE_ID, "von" ), DOUBLE_HANDLER, 0, 1, false ); //$NON-NLS-1$

  public static final IValuePropertyType TO_PROP = GMLSchemaFactory.createValuePropertyType( new QName( FEATURE_BASE_ID, "bis" ), DOUBLE_HANDLER, 0, 1, false ); //$NON-NLS-1$

  public static final IValuePropertyType VOLUME_PROP = GMLSchemaFactory.createValuePropertyType( new QName( FEATURE_BASE_ID, "vol_summe" ), DOUBLE_HANDLER, 0, 1, false ); //$NON-NLS-1$

  private static final IFeatureType FT = GMLSchemaFactory.createFeatureType( QNAME_SHAPE_FEATURE, new IPropertyType[] { SHAPE_PROP, ID_PROP, BEZ_PROP, FROM_PROP, TO_PROP, VOLUME_PROP } );

  private final GeometryFactory m_gf;

  private final Interval[] m_intervals;

  private final double[] m_grenzen;

  private final boolean m_bSimple;

  private final RingTree m_tree = new RingTree();

  private final Feature m_fc = ShapeSerializer.createShapeRootFeature( FT );

  private final FeatureList m_list;

  public PolygonCollector( final GeometryFactory gf, final double[] grenzen, final boolean bSimple )
  {
    m_gf = gf;
    m_bSimple = bSimple;
    m_grenzen = grenzen;

    m_intervals = new Interval[grenzen.length + 1];
    m_intervals[0] = new Interval( Double.MIN_VALUE, grenzen[0] );
    for( int i = 0; i < grenzen.length - 1; i++ )
      m_intervals[i + 1] = new Interval( grenzen[i], grenzen[i + 1] );
    m_intervals[grenzen.length] = new Interval( grenzen[grenzen.length - 1], Double.MAX_VALUE );

    final Object property = m_fc.getProperty( ShapeSerializer.PROPERTY_FEATURE_MEMBER );
    m_list = (FeatureList) property;
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
          System.out.println( "Kann nicht sein" ); //$NON-NLS-1$

        CoordOrientation.orient( crds, CoordOrientation.TYPE.NEGATIV );

        final LinearRing lr = m_gf.createLinearRing( crds );

        m_tree.insertElement( new RingTreeElement( lr, index, innerCrd ) );
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

    final Double id = new Double( m_list.size() );
    final String name = m_intervals[index].toString();

    final Double von = new Double( Math.max( -9999.99, m_intervals[index].min ) );
    final Double bis = new Double( Math.min( 9999.99, m_intervals[index].max ) );
    final Double volumen = Double.NaN;

    final Feature feature = FeatureFactory.createFeature( m_list.getParentFeature(), m_list.getParentFeatureTypeProperty(), "" + m_list.size(), FT, new Object[] { gmGeo, id, name, von, bis, volumen } ); //$NON-NLS-1$
    m_list.add( feature );
  }

  /**
   * @see com.bce.gis.operation.raster2vector.SegmentCollector#getFeatures()
   */
  public Feature getFeatures( )
  {
    if( !m_bSimple )
      m_tree.walk( this );

    return m_fc;
  }

  public Object getResult( )
  {
    return m_fc;
  }

  /**
   * @see com.bce.gis.operation.raster2vector.collector.ringtree.RingTreeWalker#operate(com.bce.gis.operation.raster2vector.collector.ringtree.RingTreeElement)
   */
  public void operate( final RingTreeElement element )
  {
    final Polygon p = element.getAsPolygon( m_gf );

    if( p != null )
    {
      try
      {
        final int intIndex = getIndexForElement( element );

        if( intIndex > 0 )
          appendFeature( intIndex, p );
      }
      catch( final GM_Exception gme )
      {
        gme.printStackTrace();
      }
    }
  }

  private int getIndexForElement( final RingTreeElement element )
  {
    final int index = element.index;
    if( element.hasChildren() )
    {
      final RingTreeElement child = element.getFirstChild();

      if( child.index == index - 1 || child.index == index + 1 )
      {
        final double grenze0 = m_grenzen[index];
        final double grenze1 = m_grenzen[child.index];

        for( int i = 0; i < m_intervals.length; i++ )
        {
          if( (m_intervals[i].min == grenze0 && m_intervals[i].max == grenze1) || (m_intervals[i].min == grenze1 && m_intervals[i].max == grenze0) )
            return i;
        }
      }
      else
      {
        final int childIndex = getIndexForElement( child );
        if( childIndex == -1 )
          return index + 1;

        if( m_grenzen[index] == m_intervals[childIndex].max )
          return index + 1;

        return index;
      }
    }
    else
    {
      // anhand der inneren Coordinate rausfinden, zu welcher Klasse es gehört
      final double value = element.innerCrd.z;
      if( Double.isNaN( value ) )
        return -1;

      for( int i = 0; i < m_intervals.length; i++ )
      {
        if( m_intervals[i].contains( value ) )
          return i;
      }
    }

    return -1;
  }

  private static class Interval
  {
    public final double min;

    public final double max;

    public Interval( final double min, final double max )
    {
      this.min = min;
      this.max = max;
    }

    public boolean contains( final double value )
    {
      if( min == Double.MIN_VALUE )
        return value < max;
      else if( max == Double.MAX_VALUE )
        return min <= value;
      else
        return min <= value && value < max;
    }

    @Override
    public String toString( )
    {
      final StringBuffer str = new StringBuffer();
      if( min == Double.MIN_VALUE )
        str.append( "-Inf" ); //$NON-NLS-1$
      else
        str.append( min );

      str.append( " - " ); //$NON-NLS-1$

      if( max == Double.MAX_VALUE )
        str.append( "Inf" ); //$NON-NLS-1$
      else
        str.append( max );

      return str.toString();
    }
  }
}
