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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.xml.ElementReference;
import org.kalypso.gmlschema.xml.ElementWithOccurs;

/**
 * Representation of a feature content definition from xml schema
 * 
 * @author doemming
 */
public abstract class FeatureContentType implements IFeatureContentType
{
  private final GMLSchema m_gmlSchema;

  private final ComplexType m_complexType;

  private IPropertyType[] m_pt = null;

  private final QName m_qName;

  public FeatureContentType( final GMLSchema gmlSchema, final ComplexType complexType )
  {
    m_gmlSchema = gmlSchema;
    m_complexType = complexType;
    final String targetNamespace = gmlSchema.getTargetNamespace();
    final String name = m_complexType.getName();
    if( name != null )
      m_qName = new QName( targetNamespace, name );
    else
      m_qName = new QName( "anonymous" ); //$NON-NLS-1$
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
   * @see org.kalypso.gmlschema.builder.IInitialize#init(int)
   */
  public void init( final int initializeRun )
  {
// nothing to do
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#getProperties()
   */
  public IPropertyType[] getProperties( )
  {
    if( m_pt == null )
    {
      try
      {
        final Map<QName, IPropertyType> propertyTypes = new LinkedHashMap<QName, IPropertyType>();

        final List<ElementWithOccurs> sequence = getSequence();
        for( final ElementWithOccurs elementWithOccurs : sequence )
        {
          final Element elementItem = elementWithOccurs.getElement();
          Object buildedObject = getGMLSchema().getBuildedObjectFor( elementItem );

          if( buildedObject == null )
          {
            // everything should be registered for the local element, nothing for the referenced element
            final QName ref = elementItem.getRef();
            if( ref != null )
            {
              final ElementReference reference = m_gmlSchema.resolveElementReference( ref );
              if( reference != null )
              {
                final Element element = reference.getElement();
                final GMLSchema schema = (GMLSchema) reference.getGMLSchema();
                buildedObject = schema.getBuildedObjectFor( element );
              }
            }
          }

          if( buildedObject instanceof IPropertyType )
          {
            final IPropertyType pt = (IPropertyType) buildedObject;
            propertyTypes.put( pt.getQName(), pt );
          }
        }

        m_pt = propertyTypes.values().toArray( new IPropertyType[propertyTypes.size()] );
      }
      catch( final GMLSchemaException e )
      {
        e.printStackTrace();
      }
    }

    return m_pt;
  }

  public ComplexType getComplexType( )
  {
    return m_complexType;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#collectFunctionProperties()
   */
  public XmlObject[] collectFunctionProperties( )
  {
    final String namespaceDecl = "declare namespace xs='" + NS.XSD_SCHEMA + "' " + "declare namespace kapp" + "='" + NS.KALYPSO_APPINFO + "' "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    final String xpath = "xs:annotation/xs:appinfo/kapp:functionProperty"; //$NON-NLS-1$

    final String fullXpath = namespaceDecl + xpath;

    return getComplexType().selectPath( fullXpath );
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return m_qName + "\n" + m_complexType; //$NON-NLS-1$
  }

}
