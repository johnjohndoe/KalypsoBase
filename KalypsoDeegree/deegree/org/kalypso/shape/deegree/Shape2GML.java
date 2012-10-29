/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.shape.deegree;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Formatter;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.kalypso.commons.xml.XmlTypes;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.gmlschema.GMLSchemaCatalog;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.KalypsoGMLSchemaPlugin;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.shape.ShapeFile;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.dbf.FieldType;
import org.kalypso.shape.dbf.IDBFField;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.gml.binding.shape.AbstractShape;
import org.kalypsodeegree_impl.gml.binding.shape.ShapeCollection;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;

import com.google.common.base.Charsets;

/**
 * @author Holer Albert
 * @author Gernot Belger
 */
public final class Shape2GML
{
  private Shape2GML( )
  {
    throw new UnsupportedOperationException();
  }

  public static ShapeCollection convertShp2Gml( final String featureTypeKey, final ShapeFile shape, final String shapeSRS, final IProgressMonitor monitor ) throws Exception
  {
    // System.out.format( "%s: %s%n", shape.getFileBase(), featureTypeKey );

    final int count = shape.getNumRecords();

    monitor.beginTask( "Converting shape entries", count );

    final IFeatureType featureType = createFeatureType( featureTypeKey, shape );

    final GMLWorkspace workspace = createShapeWorkspace( shape.getShapeType(), featureType );
    final ShapeCollection collection = (ShapeCollection)workspace.getRootFeature();

    final IRelationType listRelation = (IRelationType)collection.getFeatureType().getProperty( ShapeCollection.MEMBER_FEATURE_LOCAL );

    final int fieldCount = shape.getFields().length;

    for( int i = 0; i < count; i++ )
    {
      ProgressUtilities.workedModulo( monitor, i, count, 100, "reading feature %d / %d" );

      /* directly create feature from row values -> we know the feature type fits the specification */
      final String featureId = Integer.toString( i );

      /* get values */
      final Object[] properties = new Object[fieldCount];
      if( shape.readRow( i, properties ) )
      {
        /* convert geometry */
        final ISHPGeometry geometry = shape.getShape( i );
        final GM_Object geom = SHP2GM_Object.transform( shapeSRS, geometry );

        /* Build properties including geometry, we know that geom is at pos 0 */
        final Object[] propertiesWithGeom = new Object[fieldCount + 1];
        propertiesWithGeom[0] = geom;
        System.arraycopy( properties, 0, propertiesWithGeom, 1, fieldCount );

        final AbstractShape newFeature = (AbstractShape)FeatureFactory.createFeature( collection, listRelation, featureId, featureType, propertiesWithGeom );

        /* add to workspace */
        workspace.addFeatureAsComposition( collection, listRelation, -1, newFeature );
      }
    }

    return collection;
  }

  public static IFeatureType createFeatureType( final String key, final ShapeFile shape )
  {
    final ShapeType shapeType = shape.getShapeType();
    final IDBFField[] fields = shape.getFields();
    return createFeatureType( key, shapeType, fields );
  }

  /**
   * REMARK: key: was formerly (in dbase file) m_suffix = "" + m_fname.hashCode()
   */
  public static IFeatureType createFeatureType( final String key, final ShapeType shapeType, final IDBFField[] fields )
  {
    try
    {
      final String targetNamespace = ShapeCollection.SHP_NAMESPACE_URI + ".custom_" + key;

      /* geometry property: overwrites the one defined in '_Shape' */
      final String geomTypeName = getGeometryName( shapeType );

      /* The value properties */
      final String propertyElementsSchemaFragment = buildValueElementsDefinition( fields );

      /* Read schema template and replace placeholders */
      final URL resource = Shape2GML.class.getResource( "resources/shapeCustomTemplate.xsd" );

      String schemaString = IOUtils.toString( resource, Charsets.UTF_8.name() );
      schemaString = schemaString.replaceAll( Pattern.quote( "${CUSTOM_NAMESPACE_SUFFIX}" ), key );
      schemaString = schemaString.replaceAll( Pattern.quote( "${CUSTOM_FEATURE_PROPERTY_ELEMENTS}" ), propertyElementsSchemaFragment );
      schemaString = schemaString.replaceAll( Pattern.quote( "${GEOM_TYPE_NAME}" ), geomTypeName );

      final File tempFile = createTempFile( KalypsoDeegreePlugin.getDefault(), "temporaryCustomSchemas", "customSchema", ".xsd" );
      tempFile.deleteOnExit();

      // TODO: why write this file to disk? Why not directly parse the schema from it and add the schema to the cache?
      FileUtils.writeStringToFile( tempFile, schemaString, Charsets.UTF_8.name() );

      /* read schema via catalog and retrieve the feature type from the freshly loaded schema */
      final GMLSchemaCatalog catalog = KalypsoGMLSchemaPlugin.getDefault().getSchemaCatalog();
      final IGMLSchema schema = catalog.getSchema( null, tempFile.toURI().toURL() ); //$NON-NLS-1$

      final QName memberFeatureType = new QName( targetNamespace, "Shape" ); //$NON-NLS-1$
      return schema.getFeatureType( memberFeatureType );
    }
    catch( final IOException e )
    {
      // This should never happen, as we always are allowed to access plugin state.
      // Encapsulate into runtime exception, so we do not need to catch it everywhere.
      e.printStackTrace();
      throw new IllegalStateException( e );
    }
  }

  /**
   * Create a temp file in the subDirName of the plugin's state location (where files can be created, deleted, etc.).
   * Uses File.createTempFile() so as written in the File javadoc, you should call .deleteOnExit() on the returned file
   * instance to make it a real 'temp' file.
   */
  private static File createTempFile( final Plugin plugin, final String subDirName, String prefix, final String suffix ) throws IOException
  {
    if( prefix.length() < 3 )
      prefix += "___";

    final IPath path = plugin.getStateLocation();
    final File dir = new File( path.toFile(), subDirName );
    if( !dir.exists() )
      dir.mkdir();

    // TODO as org.kalypso.commons.java.io.FileUtilities.validateName() should be moved to JavaApiContribs and could be
    // used here
    final String cleanPrefix = prefix.replaceAll( "[\\\\/:\\*\\?\"<>|]", "_" );

    return File.createTempFile( cleanPrefix, suffix, dir );
  }

  private static String buildValueElementsDefinition( final IDBFField[] fields )
  {
    final Formatter formatter = new Formatter();

    for( final IDBFField field : fields )
    {
      final IMarshallingTypeHandler th = findTypeHandler( field );

      final String name = field.getName();
      final String type = th.getTypeName().getLocalPart();

      formatter.format( "<element name='%s' type='%s'/>%n", name, type );
    }

    return formatter.toString();
  }

  private static IMarshallingTypeHandler findTypeHandler( final IDBFField field )
  {
    final ITypeRegistry<IMarshallingTypeHandler> registry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    final FieldType type = field.getType();
    switch( type )
    {
      case C:
        return registry.getTypeHandlerForTypeName( XmlTypes.XS_STRING );

      case D:
        return registry.getTypeHandlerForTypeName( XmlTypes.XS_DATE );

      case F:
      case N:
        final short decimal = field.getDecimalCount();
        final short size = field.getLength();
        if( decimal == 0 )
        {
          if( size < 10 )
            return registry.getTypeHandlerForTypeName( XmlTypes.XS_INT );

          return registry.getTypeHandlerForTypeName( XmlTypes.XS_LONG );
        }

        if( size < 8 )
          return registry.getTypeHandlerForTypeName( XmlTypes.XS_FLOAT );

        return registry.getTypeHandlerForTypeName( XmlTypes.XS_DOUBLE );

      case L:
        return registry.getTypeHandlerForTypeName( XmlTypes.XS_BOOLEAN );

      case M:
        return registry.getTypeHandlerForTypeName( XmlTypes.XS_STRING );

        // case B:
        // final IMarshallingTypeHandler byteArrayOutputStreamTH = registry.getTypeHandlerForClassName(
        // ByteArrayOutputStream.class );

      default:
        throw new IllegalStateException();
    }
  }

  private static String getGeometryName( final ShapeType shapeType )
  {
    switch( shapeType )
    {
      case NULL:
        return "Null"; //$NON-NLS-1$

      case POINT:
      case POINTM:
      case POINTZ:
        return "Point"; //$NON-NLS-1$

      case MULTIPOINT:
      case MULTIPOINTM:
      case MULTIPOINTZ:
        return "MultiPoint"; //$NON-NLS-1$

      case POLYLINE:
      case POLYLINEM:
      case POLYLINEZ:
        return "Polyline";

      case POLYGON:
      case POLYGONM:
      case POLYGONZ:
        return "Polygon";
    }

    throw new UnsupportedOperationException();
  }

  private static GMLWorkspace createShapeWorkspace( final ShapeType shapeFileType, final IFeatureType elementType ) throws GMLSchemaException
  {
    final String customNamespace = elementType.getQName().getNamespaceURI();
    final QName concreteCollectionQName = new QName( customNamespace, "ShapeCollection" ); //$NON-NLS-1$

    final GMLWorkspace workspace = FeatureFactory.createGMLWorkspace( concreteCollectionQName, null, null );
    final ShapeCollection collection = (ShapeCollection)workspace.getRootFeature();

    collection.setShapeType( shapeFileType );

    return workspace;
  }
}