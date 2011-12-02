package org.kalypso.ogc.gml.serialize.test;

import java.io.File;
import java.io.FilenameFilter;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.commons.xml.XmlTypes;
import org.kalypso.gmlschema.GMLSchemaFactory;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.ogc.gml.serialize.ShapeSerializer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.io.shpapi.ShapeConst;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.tools.GMLConstants;

public class WriteShapeTest extends TestCase
{

  public void testWriteShape( ) throws Exception
  {
    /* Create feature type which describes what data the shape file contains */
    final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();

    final IMarshallingTypeHandler doubleTypeHandler = typeRegistry.getTypeHandlerForTypeName( XmlTypes.XS_DOUBLE ); //$NON-NLS-1$
    final IMarshallingTypeHandler stringTypeHandler = typeRegistry.getTypeHandlerForTypeName( XmlTypes.XS_STRING ); //$NON-NLS-1$
    final IMarshallingTypeHandler pointTypeHandler = typeRegistry.getTypeHandlerForTypeName( GMLConstants.QN_POINT );

    final QName shapeTypeQName = new QName( "anyNS", "shapeType" ); //$NON-NLS-1$ //$NON-NLS-2$

    final IValuePropertyType doubleType = GMLSchemaFactory.createValuePropertyType( new QName( "anyNS", "aNumber" ), doubleTypeHandler, 1, 1, false ); //$NON-NLS-1$ //$NON-NLS-2$
    final IValuePropertyType stringType = GMLSchemaFactory.createValuePropertyType( new QName( "anyNS", "aString" ), stringTypeHandler, 1, 1, false ); //$NON-NLS-1$ //$NON-NLS-2$
    final IValuePropertyType pointType = GMLSchemaFactory.createValuePropertyType( new QName( "anyNS", "aGeometry" ), pointTypeHandler, 1, 1, false ); //$NON-NLS-1$ //$NON-NLS-2$

    final IPropertyType[] properties = new IPropertyType[] { pointType, doubleType, stringType };
    final IFeatureType shapeFT = GMLSchemaFactory.createFeatureType( shapeTypeQName, properties );

    /* Create the shape root feature, we need it to create the children. */
    final Feature shapeRootFeature = ShapeSerializer.createWorkspaceRootFeature( shapeFT, ShapeConst.SHAPE_TYPE_POINT );
    final GMLWorkspace workspace = shapeRootFeature.getWorkspace();
    final IRelationType shapeParentRelation = (IRelationType) shapeRootFeature.getFeatureType().getProperty( ShapeSerializer.PROPERTY_FEATURE_MEMBER );

    /* Now create some features of this type */
    final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
    for( int i = 0; i < 5; i++ )
    {
      final double aDouble = i * Math.PI;
      final String aString = "Item Number: " + i; //$NON-NLS-1$
      final GM_Point aPoint = GeometryFactory.createGM_Point( i * 4.3, i * 2.1, crs );

      final Object[] data = new Object[] { aPoint, aDouble, aString };
      final Feature feature = FeatureFactory.createFeature( shapeRootFeature, shapeParentRelation, "FeatureID" + i, shapeFT, data ); //$NON-NLS-1$
      workspace.addFeatureAsComposition( shapeRootFeature, shapeParentRelation, -1, feature );
    }

    final File shapeFile = FileUtilities.createNewUniqueFile( "shapetest", FileUtilities.TMP_DIR );

    ShapeSerializer.serialize( workspace, shapeFile.getAbsolutePath(), null );

    System.out.println( "Wrote shapeFile to:" + shapeFile.getAbsolutePath() );

    final IOFileFilter prefixFileFilter = FileFilterUtils.prefixFileFilter( shapeFile.getName() );
    final File[] shapeParts = shapeFile.getParentFile().listFiles( (FilenameFilter) prefixFileFilter );
    for( final File part : shapeParts )
      part.delete();
  }
}