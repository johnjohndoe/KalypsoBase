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
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.ExplicitGroup;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.basics.IInitialize;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.xml.ElementReference;

/**
 * representation of a feature content definition from xml schema
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

  public abstract ExplicitGroup getSequence( );

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( int initializeRun )
  {
    switch( initializeRun )
    {
      case IInitialize.INITIALIZE_RUN_FIRST:

        final ExplicitGroup sequence = getSequence();
        final List<IPropertyType> propertyTypes = new ArrayList<IPropertyType>();
        // final List relationTypes = new ArrayList();
        if( sequence != null )
        {
          final Element[] elementArray = sequence.getElementArray();
          for( int i = 0; i < elementArray.length; i++ )
          {
            final QName ref = elementArray[i].getRef();
            final Element element;
            final GMLSchema schema;
            if( ref == null )
            {
              element = elementArray[i];
              schema = getGMLSchema();
            }
            else
            {
              final ElementReference reference = (ElementReference) m_gmlSchema.resolveReference( ref );
              element = reference.getElement();
              schema = reference.getGMLSchema();
            }
            final Object buildedObject = schema.getBuildedObjectFor( element );
            // why are alle buildedObjects of type IPropertyType ? 
            propertyTypes.add( (IPropertyType) buildedObject );
          }
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
}
