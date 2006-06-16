package com.bce.gis.operation.raster2vector;

import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureTypeProperty;
import org.deegree.model.geometry.GM_Object;
import org.deegree_impl.model.feature.FeatureFactory;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Interface für die verschiedenen Ergebnisse des Raster2Lines algorythmus
 * 
 * @author belger
 */
public interface SegmentCollector
{
  public static final FeatureFactory FF = new FeatureFactory();

  public static final String FEATURE_BASE_ID = SegmentCollector.class.getName();

  public static final FeatureTypeProperty SHAPE_PROP = FF.createFeatureTypeProperty( FEATURE_BASE_ID + ".Shape", GM_Object.class.getName(), false );

  public static final FeatureTypeProperty ID_PROP = FF.createFeatureTypeProperty( ".ID", Double.class.getName(), false );

  public static final FeatureTypeProperty BEZ_PROP = FF.createFeatureTypeProperty( FEATURE_BASE_ID + ".Name", String.class.getName(), false );

  public void addSegment( final int index, final LinkedCoordinate lc0, final LinkedCoordinate lc1, final Coordinate nearC0, final Coordinate nearC1 ) throws LinkedCoordinateException;

  public FeatureCollection getFeatures( );
}
