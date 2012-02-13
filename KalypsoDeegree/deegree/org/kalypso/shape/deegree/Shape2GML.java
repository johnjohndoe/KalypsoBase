/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.kalypso.commons.java.net.UrlUtilities;
import org.kalypso.commons.xml.XmlTypes;
import org.kalypso.contribs.eclipse.core.runtime.TempFileUtilities;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaCatalog;
import org.kalypso.gmlschema.GMLSchemaFactory;
import org.kalypso.gmlschema.KalypsoGMLSchemaPlugin;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.dbf.FieldType;
import org.kalypso.shape.dbf.IDBFField;
import org.kalypsodeegree.KalypsoDeegreePlugin;

/**
 * FIXME: move to KalypsoCore
 *
 * @author albert
 */
public final class Shape2GML
{
  public static final String SHP_NAMESPACE_URI = "org.kalypso.shape";

  private Shape2GML( )
  {
    throw new UnsupportedOperationException();
  }

  public static IFeatureType createFeatureType( final String key, final ShapeType shapeType, final IDBFField[] fields )
  {
    final String customNamespaceURI = "org.kalypso.shape.custom_" + key;

    final StringBuilder elementsString = new StringBuilder();

    final IValuePropertyType[] fieldTypes = new IValuePropertyType[fields.length];

    for( int i = 0; i < fields.length; i++ )
    {
      final IDBFField field = fields[i];

      final IMarshallingTypeHandler th = findTypeHandler( field );

      final String name = field.getName();
      fieldTypes[i] = GMLSchemaFactory.createValuePropertyType( new QName( customNamespaceURI, name ), th, 1, 1, false );

      final String elementFragment = String.format( "<xs:element name=\"%s\" type=\"xs:%s\"/>%n", name, th.getTypeName().getLocalPart() );
      elementsString.append( elementFragment );
    }

    final IValuePropertyType geomType = createGeometryPropertyType( shapeType, customNamespaceURI );
    final String schemaFragment = elementsString.toString();

    return createTemporaryFeatureType( key, customNamespaceURI, geomType, fieldTypes, schemaFragment );
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

  private static IValuePropertyType createGeometryPropertyType( final ShapeType shapeType, final String customNamespaceURI )
  {
    final ITypeRegistry<IMarshallingTypeHandler> registry = MarshallingTypeRegistrySingleton.getTypeRegistry();

    final QName geometryType = GenericShapeDataFactory.findGeometryType( shapeType );
    final IMarshallingTypeHandler geoTH = registry.getTypeHandlerForTypeName( geometryType );

    return GMLSchemaFactory.createValuePropertyType( new QName( customNamespaceURI, "GEOM" ), geoTH, 1, 1, false );
  }

  private static IFeatureType createTemporaryFeatureType( final String key, final String customNamespaceURI, final IValuePropertyType geomType, final IValuePropertyType[] fieldTypes, final String elementsString )
  {
    try
    {
      final String geomTag = geomType.getTypeHandler().getTypeName().getLocalPart();
      final String geometryPropertyTypeString = "gml:" + geomTag;

      // TODO: comment! Why is this all needed etc.?
      final URL resource = Shape2GML.class.getResource( "resources/shapeCustomTemplate.xsd" );

      String schemaString = UrlUtilities.toString( resource, "UTF-8" );
      schemaString = schemaString.replaceAll( Pattern.quote( "${CUSTOM_NAMESPACE_SUFFIX}" ), key );
      schemaString = schemaString.replaceAll( Pattern.quote( "${CUSTOM_FEATURE_GEOMETRY_PROPERTY_TYPE}" ), geometryPropertyTypeString );
      schemaString = schemaString.replaceAll( Pattern.quote( "${CUSTOM_FEATURE_PROPERTY_ELEMENTS}" ), elementsString );

      final File tempFile = TempFileUtilities.createTempFile( KalypsoDeegreePlugin.getDefault(), "temporaryCustomSchemas", "customSchema", ".xsd" );
      tempFile.deleteOnExit();

      // TODO: why write this file to disk? Why not directly parse the schema from it and add the schema to the cache?
      FileUtils.writeStringToFile( tempFile, schemaString, "UTF8" );

      final GMLSchemaCatalog catalog = KalypsoGMLSchemaPlugin.getDefault().getSchemaCatalog();
      final GMLSchema schema = catalog.getSchema( "3.1.1", tempFile.toURI().toURL() );

      /* Combine types into one array */
      final IPropertyType[] propertyTypes = new IPropertyType[fieldTypes.length + 1];
      System.arraycopy( fieldTypes, 0, propertyTypes, 0, fieldTypes.length );
      propertyTypes[fieldTypes.length] = geomType;

      final QName memberQName = new QName( customNamespaceURI, "featureMember" );

      return GMLSchemaFactory.createFeatureType( memberQName, propertyTypes, schema, new QName( SHP_NAMESPACE_URI, "_Shape" ) );
    }
    catch( final IOException e )
    {
      // This should never happen, as we always are allowed to access plugin state.
      // Encapsulate into runtime exception, so we do not need to catch it everywhere.
      e.printStackTrace();
      throw new IllegalStateException( e );
    }
  }
}