/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.gmlschema.feature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.ElementWithOccurs;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.KalypsoGmlSchemaTracing;
import org.kalypso.gmlschema.basics.IInitialize;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.xml.ElementReference;

/**
 * Representation of a feature content definition from xml schema
 * 
 * @author doemming
 */
public abstract class FeatureContentType implements IFeatureContentType
{
  private final GMLSchema m_gmlSchema;

  private final ComplexType m_complexType;

  protected IPropertyType[] m_pt = null;

  private final QName m_qName;

  public FeatureContentType( final GMLSchema gmlSchema, ComplexType complexType )
  {
    m_gmlSchema = gmlSchema;
    m_complexType = complexType;
    final String targetNamespace = gmlSchema.getTargetNamespace();
    final String name = m_complexType.getName();
    if( name != null )
      m_qName = new QName( targetNamespace, name );
    else
      m_qName = new QName( "anonymous" );
  }

  public QName getQName( )
  {
    return m_qName;
  }

  public GMLSchema getGMLSchema( )
  {
    return m_gmlSchema;
  }

  public abstract List<ElementWithOccurs> getSequence( ) throws GMLSchemaException;

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( int initializeRun ) throws GMLSchemaException
  {
    switch( initializeRun )
    {
      case IInitialize.INITIALIZE_RUN_FIRST:

        final List<IPropertyType> propertyTypes = new ArrayList<IPropertyType>();

        final List<ElementWithOccurs> sequence = getSequence();
        for( Iterator<ElementWithOccurs> iter = sequence.iterator(); iter.hasNext(); )
        {
          GMLSchema schema = getGMLSchema();
          final Element elementItem = iter.next().getElement();
          Object buildedObject = schema.getBuildedObjectFor( elementItem );

          final Element element;
          if( buildedObject == null )
          {
            // everything should be registered for the local element, nothing for the referenced element
            final QName ref = elementItem.getRef();
            if( ref == null )
            {
              // final String message = "no typeHandler for " + elementItem.getType();
              // System.out.println( message );
              // System.out.println();
            }
            else
            {
              final ElementReference reference = m_gmlSchema.resolveElementReference( ref );
              if( reference != null )
              {
                element = reference.getElement();
                schema = reference.getGMLSchema();
                buildedObject = schema.getBuildedObjectFor( element );
              }
              else
              {
                if( KalypsoGmlSchemaTracing.traceSchemaParsing() )
                  System.out.println( "debug" );
              }
            }
          }
          // why are alle buildedObjects of type IPropertyType ?
          if( buildedObject != null && buildedObject instanceof IPropertyType )
            propertyTypes.add( (IPropertyType) buildedObject );
          // else
          // System.out.println( "no valid property found for " + elementItem );
        }
        m_pt = propertyTypes.toArray( new IPropertyType[propertyTypes.size()] );
        break;
    }
  }

  public ComplexType getComplexType( )
  {
    return m_complexType;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#getProperty(javax.xml.namespace.QName)
   */
  public IPropertyType getProperty( QName qName )
  {
    final IPropertyType[] properties = getProperties();
    for( int i = 0; i < properties.length; i++ )
    {
      final IPropertyType pt = properties[i];
      if( pt.getQName().equals( qName ) )
        return pt;
    }
    return null;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#collectFunctionProperties()
   */
  public XmlObject[] collectFunctionProperties( )
  {
    final String namespaceDecl = "declare namespace xs='" + NS.XSD_SCHEMA + "' " + "declare namespace kapp" + "='" + NS.KALYPSO_APPINFO + "' ";
    final String xpath = "xs:annotation/xs:appinfo/kapp:functionProperty";

    final String fullXpath = namespaceDecl + xpath;

    return getComplexType().selectPath( fullXpath );
  }

}
