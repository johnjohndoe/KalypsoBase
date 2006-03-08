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

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.ExplicitGroup;
import org.apache.xmlbeans.impl.xb.xsdschema.ExtensionType;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.basics.IInitialize;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.xml.ComplexTypeReference;

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

  public FeatureContentTypeFromExtension( GMLSchema schema, ComplexType complexType, ExtensionType extension )
  {
    super( schema, complexType );
    m_extension = extension;
  }

  /**
   * @see org.kalypso.gmlschema.feature.FeatureContentType#getSequence()
   */
  @Override
  public ExplicitGroup getSequence( )
  {
    return m_extension.getSequence();
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  @Override
  public void init( int initializeRun )
  {
    switch( initializeRun )
    {
      case IInitialize.INITIALIZE_RUN_FIRST:
        final QName base = m_extension.getBase();
        final ComplexTypeReference reference = (ComplexTypeReference) getGMLSchema().resolveReference( base );
        final GMLSchema schema = reference.getGMLSchema();
        final ComplexType complexType = reference.getComplexType();
        m_extensionBase = schema.getFeatureContentTypeFor( complexType );
        break;
    }
    super.init( initializeRun );
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#getProperties()
   */
  public IPropertyType[] getProperties( )
  {
    if( m_fullProps == null )
    {
      final IPropertyType[] propsFromExtension = m_extensionBase.getProperties();
      final IPropertyType[] props = new IPropertyType[m_pt.length + propsFromExtension.length];
      int n = 0;

      for( int i = 0; i < propsFromExtension.length; i++ )
      {
        props[n] = propsFromExtension[i];
        n++;
      }
      for( int i = 0; i < m_pt.length; i++ )
      {
        props[n] = m_pt[i];
        n++;
      }
      m_fullProps = props;
    }
    return m_fullProps;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#getBase()
   */
  public IFeatureContentType getBase( )
  {
    return m_extensionBase;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#getDerivationType()
   */
  public int getDerivationType( )
  {
    return DERIVATION_BY_EXTENSION;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#getDirectProperties()
   */
  public IPropertyType[] getDirectProperties( )
  {
    return m_pt;
  }
}
