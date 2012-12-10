package com.bce.gis.operation.raster2vector.collector;

import java.util.Collection;

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

import com.bce.gis.operation.raster2vector.LinkedCoordinate;
import com.bce.gis.operation.raster2vector.LinkedCoordinateException;
import com.bce.gis.operation.raster2vector.SegmentCollector;
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
  private static final IValuePropertyType GRENZEN_PROP = GMLSchemaFactory.createValuePropertyType( new QName( FEATURE_BASE_ID, "hoehe" ), DOUBLE_HANDLER, 0, 1, false ); //$NON-NLS-1$

  private static final IFeatureType FT = GMLSchemaFactory.createFeatureType( QNAME_SHAPE_FEATURE, new IPropertyType[] { SHAPE_PROP, ID_PROP, BEZ_PROP, GRENZEN_PROP } );

  private final GeometryFactory m_gf;

  private final double[] m_grenzen;

  private final boolean m_bSimple;

  private final Feature m_fc = ShapeSerializer.createShapeRootFeature( FT );

  public LineStringCollector( final GeometryFactory gf, final double[] grenzen, final boolean bSimple )
  {
    m_grenzen = grenzen;
    m_gf = gf;
    m_bSimple = bSimple;
  }

  /**
   * @see com.bce.gis.operation.raster2vector.SegmentCollector#addSegment(int,
   *      com.bce.gis.operation.raster2vector.LinkedCoordinate, com.bce.gis.operation.raster2vector.LinkedCoordinate,
   *      com.vividsolutions.jts.geom.Coordinate, com.vividsolutions.jts.geom.Coordinate)
   */
  @Override
  public void addSegment( final int index, final LinkedCoordinate lc0, final LinkedCoordinate lc1, final Coordinate nearC0, final Coordinate nearC1 ) throws LinkedCoordinateException
  {
    try
    {
      final Object property = m_fc.getProperty( ShapeSerializer.PROPERTY_FEATURE_MEMBER );
      final FeatureList list = (FeatureList) property;

      if( m_bSimple )
      {
        final Double id = new Double( list.size() );
        // final Double id = new Double( list.getSize() );
        final Double grenze = new Double( m_grenzen[index] );
        final String name = grenze.toString();

        final GM_Object gmGeo = JTSAdapter.wrap( m_gf.createLineString( new Coordinate[] { lc0.crd, lc1.crd } ) );
        final Feature feature = FeatureFactory.createFeature( list.getParentFeature(), list.getParentFeatureTypeProperty(), "" + list.size(), FT, new Object[] { gmGeo, id, name, grenze } ); //$NON-NLS-1$
        // m_fc.appendFeature( FF.createFeature( "" + m_fc.getSize(), FT, new Object[] { gmGeo, id, name, grenze } ) );
        list.add( feature );
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

          final Double id = new Double( list.size() );
          // final Double id = new Double( m_fc.getSize() );
          final Double grenze = new Double( m_grenzen[index] );
          final String name = "" + m_grenzen[index]; //$NON-NLS-1$

          final Feature feature = FeatureFactory.createFeature( m_fc, m_fc.getParentRelation(), "" + list.size(), FT, new Object[] { gmGeo, id, name, grenze } ); //$NON-NLS-1$
          list.add( feature );
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
  public Feature getFeatures( )
  {
    return m_fc;
  }
}