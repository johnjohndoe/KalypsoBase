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

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.ExtensionType;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.ElementWithOccurs;

/**
 * representation of a feature content definition from xml schema that is defined by extension.
 * 
 * @author doemming
 */
public class FeatureContentTypeFromExtension extends FeatureContentType
{
  private final ExtensionType m_extension;

  private FeatureContentType m_extensionBase = null;

  private IPropertyType[] m_fullProps = null;

  public FeatureContentTypeFromExtension( final GMLSchema schema, final ComplexType complexType, final ExtensionType extension )
  {
    super( schema, complexType );
    m_extension = extension;
  }

  /**
   * @see org.kalypso.gmlschema.feature.FeatureContentType#getSequence()
   */
  @Override
  public List<ElementWithOccurs> getSequence( ) throws GMLSchemaException
  {
    return GMLSchemaUtilities.collectElements( getGMLSchema(), m_extension, null, null );
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#getProperties()
   */
  @Override
  public IPropertyType[] getProperties( )
  {
    if( m_fullProps == null )
    {
      final IPropertyType[] propsFromExtension = getBase().getProperties();

      final IPropertyType[] directProperties = getDirectProperties();

      m_fullProps = new IPropertyType[directProperties.length + propsFromExtension.length];

      System.arraycopy( propsFromExtension, 0, m_fullProps, 0, propsFromExtension.length );
      System.arraycopy( directProperties, 0, m_fullProps, propsFromExtension.length, directProperties.length );

// int n = 0;
// for( final IPropertyType element : propsFromExtension )
// {
// props[n] = element;
// n++;
// }
//
// for( final IPropertyType element : m_pt )
// {
// props[n] = element;
// n++;
// }
    }
    return m_fullProps;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#getBase()
   */
  @Override
  public IFeatureContentType getBase( )
  {
    if( m_extensionBase == null )
    {
      try
      {
        final QName base = m_extension.getBase();
        final ComplexTypeReference reference = getGMLSchema().resolveComplexTypeReference( base );
        final GMLSchema schema = (GMLSchema) reference.getGMLSchema();
        final ComplexType complexType = reference.getComplexType();
        m_extensionBase = schema.getFeatureContentTypeFor( complexType );
      }
      catch( final GMLSchemaException e )
      {
        e.printStackTrace();
      }
    }

    return m_extensionBase;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#getDerivationType()
   */
  @Override
  public int getDerivationType( )
  {
    return DERIVATION_BY_EXTENSION;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#getDirectProperties()
   */
  @Override
  public IPropertyType[] getDirectProperties( )
  {
    return super.getProperties();
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#collectFunctionProperties()
   */
  @Override
  public XmlObject[] collectFunctionProperties( )
  {
    final XmlObject[] myObjects = super.collectFunctionProperties();
    final XmlObject[] baseObjects = getBase().collectFunctionProperties();

    final XmlObject[] allObjects = new XmlObject[myObjects.length + baseObjects.length];

    System.arraycopy( myObjects, 0, allObjects, 0, myObjects.length );
    System.arraycopy( baseObjects, 0, allObjects, myObjects.length, baseObjects.length );

    // TODO: consider the case when function-properties get overridden.
    // At the moment we have 'myObject' before 'allObjects', so new definitions get found first.

    return allObjects;
  }
}
