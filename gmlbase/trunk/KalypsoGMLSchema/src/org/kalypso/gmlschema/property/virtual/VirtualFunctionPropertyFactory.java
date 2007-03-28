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
package org.kalypso.gmlschema.property.virtual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleContentDocument.SimpleContent;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IDetailedFeatureType;
import org.kalypso.gmlschema.feature.IFeatureContentType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.gmlschema.xml.ElementReference;

/**
 * Provides mechanism to create a 
 * @author Patrice Congo
 */
public class VirtualFunctionPropertyFactory
{
  public static final QName QNAME_FUNCTION = new QName( NS.KALYPSO_APPINFO, "functionId" );

  public static final QName QNAME_PROPERTY = new QName( NS.KALYPSO_APPINFO, "property" );

  public static final QName QNAME_NAME = new QName( NS.KALYPSO_APPINFO, "name" );

  public static final QName QNAME_VALUE = new QName( NS.KALYPSO_APPINFO, "value" );
  
  public static final QName QNAME_IS_VIRTUAL = new QName( NS.KALYPSO_APPINFO, "isVirtual" );
  
  public static final QName QNAME_IS_GEOMETRY = 
                              new QName( NS.KALYPSO_APPINFO, "isGeometry" );
  //AbstractGeometryType
  public static final QName QNAME_GML_GEOMETRY = 
                              new QName( NS.GML3, "_Geometry" );
  
  //kapp:valueType
  public static final QName QNAME_VALUE_TYPE  = 
                new QName( NS.KALYPSO_APPINFO, "valueType" );
  
  private VirtualFunctionPropertyFactory()
  {
    
  }
  
  
  public static final Collection<IVirtualFunctionPropertyType> createVirtualFunctionPropertyTypes( 
                                                      IDetailedFeatureType featureType)
                                                      throws  IllegalArgumentException,
                                                              GMLSchemaException
  {
    if(featureType == null)
    {
      throw new IllegalArgumentException("Parameter featureType must not be null");
    }
    Map<QName, IVirtualFunctionPropertyType> vftpMap= 
        new HashMap<QName, IVirtualFunctionPropertyType>();
    
//    List<IVirtualFunctionPropertyType> vfptList = 
//                      new ArrayList<IVirtualFunctionPropertyType>();
    final IFeatureContentType featureContentType = 
                                    featureType.getFeatureContentType();
    //return local before base function properties
    final XmlObject[] funcProps = featureContentType.collectFunctionProperties();
    
      try
      {
        for( final XmlObject funcProp : funcProps )
        {
          final XmlCursor funcCursor = funcProp.newCursor();
          final IVirtualFunctionPropertyType pt = 
                         createFromCursor( featureType, funcCursor );
          if(pt!=null)
          {
            QName name = pt.getQName();
            if(!vftpMap.containsKey( name))
            {
              vftpMap.put( name, pt );
            }
//            vfptList.add( pt);
          }
        }
      }
      catch (GMLSchemaException e) 
      {
        e.printStackTrace();
        throw e;
      }
      catch( final Throwable th )
      {
        th.printStackTrace();
        String message = 
          String.format( 
                  "Exception while computig the virtual properties of %s ", 
                  getPrefixedName( featureType.getQName() )  ); 
        throw new GMLSchemaException(message,th);
      }
      
      return vftpMap.values();    
  }
  
  /**
   * @return 
   */
  static public final IVirtualFunctionPropertyType createFromCursor(
                                                    IFeatureType featureType,
                                                    XmlCursor funcCursor)
                                                    throws GMLSchemaException
  {
//    final String property = funcCursor.getAttributeText( QNAME_PROPERTY );
    final boolean isVirtual = getBooleanKAppProperty( QNAME_IS_VIRTUAL, funcCursor );
    if(!isVirtual)
    {
      return null;
    }
    
    final QName propertyQName = getPropertyAsQName( funcCursor, QNAME_PROPERTY );
    
    
    //this supposed that getProperty only returns non virtual property
    if(featureType.getProperty( propertyQName )!=null)
    {
      String message = 
        String.format( 
            "Feature must not have a regular and a virtual property with the same qname:%s",  
            getPrefixedName( propertyQName ));
      throw new GMLSchemaException(message);
    }
    
    //check value type
    try
    {
      QName valueTypeQName = getPropertyAsQName( funcCursor, QNAME_VALUE_TYPE );
      if(valueTypeQName!=null)
      {
        System.out.println("isGeometry="+isGeometry( featureType, valueTypeQName ));
        IMarshallingTypeHandler typeHandler = getTypeHandler( valueTypeQName );
        if(typeHandler!=null)
        {
          System.out.println("FEATURETYPE="+featureType.getQName());
          System.out.println("VALUE_CLASS"+typeHandler.getValueClass());
          System.out.println("VALUE_QANME="+typeHandler.getTypeName());
          System.out.println("VALUE_IS_GEO="+typeHandler.isGeometry());
          System.out.println();
          IVirtualFunctionValuePropertyType valProp=
            new VirtualFunctionValuePropertyType(typeHandler, propertyQName);
          return valProp;
        }
        else
        {
          throw new GMLSchemaException("Value-Type not supported:"+valueTypeQName);
        }
          
      }
      else
      {
        System.out.println("Type handler is null");
        final boolean isGeometry = false;//getBooleanKAppProperty( QNAME_IS_GEOMETRY, funcCursor );  
        final IVirtualFunctionPropertyType vfpt = getPropertyForVirtualFunc( propertyQName, isGeometry );
        return vfpt;
      }
    }
    catch (GMLSchemaException e) 
    {
      throw e;
    }
    catch(Throwable th)
    {
     th.printStackTrace();
     throw new GMLSchemaException(
         "Exception while creating virtual property type for "+featureType.getQName(),th);
    }
  
    
  }
  
  
  public static final IMarshallingTypeHandler getTypeHandler(QName valueQName)
  {
    ITypeRegistry<IMarshallingTypeHandler> typeRegistry = 
              MarshallingTypeRegistrySingleton.getTypeRegistry();
    IMarshallingTypeHandler typeHandlerForTypeName = 
        typeRegistry.getTypeHandlerForTypeName( valueQName );
    return typeHandlerForTypeName;
    
  }
  
  public static final Class getValueClass(QName valueQName)
  {
    Class cls = null;
    ITypeRegistry<IMarshallingTypeHandler> typeRegistry = 
              MarshallingTypeRegistrySingleton.getTypeRegistry();
    IMarshallingTypeHandler typeHandlerForTypeName = 
        typeRegistry.getTypeHandlerForTypeName( valueQName );
    if(typeHandlerForTypeName !=null)
    {
      cls = typeHandlerForTypeName.getValueClass();
    }
    return cls;
  }
  
  public static final boolean isGeometry(IFeatureType featureType, QName valueTypeQName)
  {
    try
    {
        System.out.println("valueQName="+valueTypeQName);
        GMLSchema schema = (GMLSchema) featureType.getGMLSchema();
        QName curQName=valueTypeQName;
        ElementReference elementReference;
          for(
              elementReference = schema.resolveElementReference( curQName );
              elementReference!=null;
              elementReference = schema.resolveElementReference( curQName ))
          {
          
            
            if(elementReference!=null)
            {
              
              Element element = elementReference.getElement();
              if(element !=null)
              {
                QName substitutionGroup = element.getSubstitutionGroup();
                System.out.println("elementReference="+element.getName()+
                              "\n\tSUBG="+substitutionGroup);
                if(substitutionGroup==null)
                {
                  return false;
                }
                else if(QNAME_GML_GEOMETRY.equals( substitutionGroup ))
                {
                  return true;
                }
                else
                {
                  //continue looking
                  curQName=substitutionGroup;
                }
              }
            }
        }
        return false;
    }
    catch(Throwable th)
    {
     th.printStackTrace(); 
     return false;
    }
  }
  
  public static final String getPrefixedName(QName qname)
                                    throws IllegalArgumentException 
  {
    if(qname == null)
    {
      throw new IllegalArgumentException("Argument qname must not be null:"+qname);
    }
    return qname.getPrefix()+":"+qname.getLocalPart();
  }
  
  public static final QName getPropertyAsQName(
                                  XmlCursor cursor,
                                  QName propertyQName)
                                  throws GMLSchemaException
  {
    String strQName = cursor.getAttributeText( propertyQName );
    if(strQName == null)
    {
      return null;
    }
    
    String[] qnameParts = strQName.split( ":" );
    if(qnameParts.length!=2)
    {
      String message = 
        String.format( 
            "Property[%s] must have the form \"<prefix>:<local name>\" but is \"%s\"", 
                getPrefixedName( propertyQName ),qnameParts );
      throw new GMLSchemaException(message);
    }
    String propertyNamespace = cursor.namespaceForPrefix( qnameParts[0] );
    String propertyLocalPart = qnameParts[1];
    
    QName propertyValueQName = new QName( propertyNamespace, propertyLocalPart );
    
    return propertyValueQName;
  }
  
  public static final QName makeQName(
                              String strQName, 
                              XmlCursor cursor)
  {
    String[] qnameParts = strQName.split( ":" );
    String propertyNamespace = cursor.namespaceForPrefix( qnameParts[0] );
    String propertyLocalPart = qnameParts[1];

    QName propertyQName = new QName( propertyNamespace, propertyLocalPart );
    return propertyQName;
  }
  
  private static final IVirtualFunctionPropertyType getPropertyForVirtualFunc( final QName propertyQName, boolean isGeometry )
  {
    return new VirtualFunctionPropertyType(propertyQName,isGeometry);
  }

  
  /**
   * @param propQName name of the property to get
   * @param cursor the xml cursor that provide acces to the element
   *    to get the property from 
   */
  private static final boolean getBooleanKAppProperty(QName propQName, XmlCursor cursor )
  {
    String boolText = cursor.getAttributeText( propQName );
    if( boolText == null )
    {
      return false;
    }
    
    return Boolean.parseBoolean( boolText );
  }
}
