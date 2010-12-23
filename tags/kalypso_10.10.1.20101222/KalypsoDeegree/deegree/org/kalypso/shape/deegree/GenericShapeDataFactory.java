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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.builder.GeometryPropertyBuilder;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.shape.IShapeData;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.dbf.DBFField;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.dbf.FieldType;
import org.kalypso.shape.dbf.IDBFValue;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree.model.geometry.GM_MultiPoint;
import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * A {@link org.kalypso.shape.IShapeDataProvider} that simulates the old ShapeSerializer behaviour.
 * 
 * @author Gernot Belger
 */
public class GenericShapeDataFactory
{
  public static IShapeData createDefaultData( final List<Feature> features, final Charset charset, final String coordinateSystem )
  {
    if( features.isEmpty() )
      return new FeatureShapeData( features, new IDBFValue[0], null, charset, new GM_Object2Shape( ShapeType.NULL, coordinateSystem ) );

    final IFeatureType type = findLeastCommonType( features );

    final ShapeType shapeType = findShapeType( type );
    final GMLXPath geometry = findGeometry( type );
    final IDBFValue[] fields = findFields( type );

    final GM_Object2Shape shapeConverter = new GM_Object2Shape( shapeType, coordinateSystem );

    return new FeatureShapeData( features, fields, geometry, charset, shapeConverter );
  }

  public static IDBFValue[] findFields( final IFeatureType type )
  {
    final Collection<IDBFValue> fields = new ArrayList<IDBFValue>();

    final IPropertyType[] ftp = type.getProperties();
    for( final IPropertyType element : ftp )
    {
      try
      {
        final DBFField field = findField( element );
        if( field != null )
        {
          final QName qName = element.getQName();
          final GMLXPath path = new GMLXPath( qName );
          fields.add( new FeatureValue( type, field, path ) );
        }
      }
      catch( final DBaseException e )
      {
        // should never happen
        e.printStackTrace();
      }
    }

    return fields.toArray( new IDBFValue[fields.size()] );
  }

  private static DBFField findField( final IPropertyType property ) throws DBaseException
  {
    if( !(property instanceof IValuePropertyType) )
      return null;

    if( property instanceof GeometryPropertyBuilder )
      return null;

    if( property.isList() )
      return null;

    final String fieldName = findFieldName( property );

    final IValuePropertyType vpt = (IValuePropertyType) property;
    final Class< ? > clazz = vpt.getValueClass();

    if( clazz == Integer.class )
      return new DBFField( fieldName, FieldType.N, (byte) 20, (byte) 0 );

    if( clazz == Byte.class )
      return new DBFField( fieldName, FieldType.N, (byte) 4, (byte) 0 );

    if( clazz == Character.class )
      return new DBFField( fieldName, FieldType.C, (byte) 1, (byte) 0 );

    if( clazz == Float.class )
      // TODO: Problem: reading/writing a shape will change the precision/size of the column!
      return new DBFField( fieldName, FieldType.N, (byte) 30, (byte) 10 );

    if( (clazz == Double.class) || (clazz == Number.class) )
      return new DBFField( fieldName, FieldType.N, (byte) 30, (byte) 10 );

    if( clazz == BigDecimal.class )
      return new DBFField( fieldName, FieldType.N, (byte) 30, (byte) 10 );

    if( clazz == String.class )
      return new DBFField( fieldName, FieldType.C, (byte) 127, (byte) 0 );

    if( clazz == Date.class )
      return new DBFField( fieldName, FieldType.D, (byte) 12, (byte) 0 );

    if( clazz == Long.class || clazz == BigInteger.class )
      return new DBFField( fieldName, FieldType.N, (byte) 30, (byte) 0 );

    if( clazz == Boolean.class )
      return new DBFField( fieldName, FieldType.L, (byte) 1, (byte) 0 );

    return null;
  }

  private static String findFieldName( final IPropertyType property )
  {
    final String localPart = property.getQName().getLocalPart();
    final int pos = localPart.lastIndexOf( '.' );
    if( pos < 0 )
      return localPart;

    return localPart.substring( pos + 1 );
  }

  public static GMLXPath findGeometry( final IFeatureType type )
  {
    final IValuePropertyType property = type.getDefaultGeometryProperty();
    if( property == null )
      return null;

    return new GMLXPath( property.getQName() );
  }

  public static ShapeType findShapeType( final IFeatureType type )
  {
    final IValuePropertyType property = type.getDefaultGeometryProperty();
    if( property == null )
      return ShapeType.NULL;

    final Class< ? > valueClass = property.getValueClass();

    // take the default geometry of the first feature to get the shape type.
    if( GM_Point.class.isAssignableFrom( valueClass ) )
      return ShapeType.POINT;

    if( GM_Curve.class.isAssignableFrom( valueClass ) )
      return ShapeType.POLYLINE;

    if( GM_Surface.class.isAssignableFrom( valueClass ) )
      return ShapeType.POLYGON;

    if( GM_MultiPoint.class.isAssignableFrom( valueClass ) )
      return ShapeType.POINT;

    if( GM_MultiCurve.class.isAssignableFrom( valueClass ) )
      return ShapeType.POLYLINE;

    if( GM_MultiSurface.class.isAssignableFrom( valueClass ) )
      return ShapeType.POLYGON;

    return ShapeType.NULL;
  }

  public static IFeatureType findLeastCommonType( final Feature[] features )
  {
    return findLeastCommonType( Arrays.asList( features ) );
  }

  public static IFeatureType findLeastCommonType( final List<Feature> features )
  {
    return features.get( 0 ).getFeatureType();
  }

}
