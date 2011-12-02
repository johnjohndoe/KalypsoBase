package com.bce.gis.operation.raster2vector;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.XmlTypes;
import org.kalypso.gmlschema.GMLSchemaFactory;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Object;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Interface für die verschiedenen Ergebnisse des Raster2Lines algorythmus
 * 
 * @author belger
 */
public interface SegmentCollector
{
  public static final String FEATURE_BASE_ID = SegmentCollector.class.getName();

  public static final ITypeRegistry<IMarshallingTypeHandler> registry = MarshallingTypeRegistrySingleton.getTypeRegistry();

  public static final IMarshallingTypeHandler GM_OBJECT_HANDLER = registry.getTypeHandlerForClassName( GM_Object.class );

  public static final QName QNAME_SHAPE_FEATURE = new QName( FEATURE_BASE_ID, "FeatureTypeID" ); //$NON-NLS-1$

  public static final IValuePropertyType SHAPE_PROP = GMLSchemaFactory.createValuePropertyType( new QName( FEATURE_BASE_ID, "Shape" ), GM_OBJECT_HANDLER, 0, 1, false ); //$NON-NLS-1$

  public static final IMarshallingTypeHandler DOUBLE_HANDLER = registry.getTypeHandlerForTypeName( XmlTypes.XS_DOUBLE ); //$NON-NLS-1$

  public static final IValuePropertyType ID_PROP = GMLSchemaFactory.createValuePropertyType( new QName( "ID" ), DOUBLE_HANDLER, 0, 1, false ); //$NON-NLS-1$

  public static final IMarshallingTypeHandler STRING_HANDLER = registry.getTypeHandlerForTypeName( XmlTypes.XS_STRING ); //$NON-NLS-1$

  public static final IValuePropertyType BEZ_PROP = GMLSchemaFactory.createValuePropertyType( new QName( FEATURE_BASE_ID, "Name" ), STRING_HANDLER, 0, 1, false ); //$NON-NLS-1$

  public void addSegment( final int index, final LinkedCoordinate lc0, final LinkedCoordinate lc1, final Coordinate nearC0, final Coordinate nearC1 ) throws LinkedCoordinateException;

  /**
   * @return The root feature of the shape.
   */
  public Feature getFeatures( );
}