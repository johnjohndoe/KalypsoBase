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
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.eclipse.core.runtime.Platform;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.basics.IInitialize;
import org.kalypso.gmlschema.basics.QualifiedElement;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.visitor.FindSubstitutesGMLSchemaVisitor;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.ElementReference;

/**
 * representation of a feature definition from xml schema
 * 
 * @author doemming
 */
public class FeatureType extends QualifiedElement implements IDetailedFeatureType
{
  private final HashMap<IPropertyType, Integer> m_positionMap = new HashMap<IPropertyType, Integer>();

  private final HashMap<String, IPropertyType> m_localPartMap = new HashMap<String, IPropertyType>();

  private final HashMap<QName, IPropertyType> m_qNameMap = new HashMap<QName, IPropertyType>();

  // will initilized in init()
  private IFeatureContentType m_featureContentType = null;

  // will initilized in init()
  private FeatureType m_substituteFT = null;

  private int m_defaultGeometryPosition = -1;

  private IValuePropertyType[] m_geoPTs = null;

  public FeatureType( final GMLSchema gmlSchema, final Element element )
  {
    super( gmlSchema, element );
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( int initializeRun )
  {
    switch( initializeRun )
    {
      case IInitialize.INITIALIZE_RUN_FIRST:
        initFirst();
        break;
      case IInitialize.INITIALIZE_RUN_GEOMETRY:
        initGeometry();
        break;
      case IInitialize.INITIALIZE_RUN_DEPRECATED:
        initDeprecated();
        break;
    }
  }

  /**
   * init map for fast finding IPropertyTypes
   */
  private void initDeprecated( )
  {
    final IPropertyType[] pts = m_featureContentType.getProperties();
    for( int i = 0; i < pts.length; i++ )
    {
      final IPropertyType pt = pts[i];
      m_localPartMap.put( pt.getName(), pt );
      m_qNameMap.put( pt.getQName(), pt );
    }
  }

  private void initFirst( )
  {
    final ComplexTypeReference complexTypeReference = GMLSchemaUtilities.getComplexTypeReferenceFor( m_gmlSchema, m_element );
    final ComplexType complexType = complexTypeReference.getComplexType();
    final GMLSchema schema = complexTypeReference.getGMLSchema();
    m_featureContentType = (IFeatureContentType) schema.getBuildedObjectFor( complexType );

    // initialize substitution group
    final QName substitutionGroup = m_element.getSubstitutionGroup();
    if( substitutionGroup != null )
    {
      final ElementReference substitutesReference = (ElementReference) m_gmlSchema.resolveReference( substitutionGroup );
      final GMLSchema substiututesGMLSchema = substitutesReference.getGMLSchema();
      final Element substitutesElement = substitutesReference.getElement();
      final FeatureType ft = (FeatureType) substiututesGMLSchema.getBuildedObjectFor( substitutesElement );
      m_substituteFT = ft;
    }
    else
      m_substituteFT = null;
  }

  /**
   * initialize geometryinformation after
   */
  private void initGeometry( )
  {
    final IPropertyType[] properties = m_featureContentType.getProperties();
    final List<IValuePropertyType> list = new ArrayList<IValuePropertyType>();
    for( int i = 0; i < properties.length; i++ )
    {
      final IPropertyType pt = properties[i];
      m_positionMap.put( pt, new Integer( i ) );
      if( pt instanceof IValuePropertyType )
      {
        final IValuePropertyType vpt = (IValuePropertyType) pt;
        // check for defaultgeometryPosition
        if( vpt.isGeometry() )
        {
          list.add( vpt );
          if( m_defaultGeometryPosition < 0 )
            m_defaultGeometryPosition = i;
        }
      }
    }
    m_geoPTs = list.toArray( new IValuePropertyType[list.size()] );
  }

  public IFeatureContentType getFeatureContentType( )
  {
    return m_featureContentType;
  }

  public IFeatureType[] getSubstituts( GMLSchema contextSchema, final boolean includeAbstract, final boolean inclusiveThis )
  {
    if( m_substituteFT == null ) // no substitutiongroup
    {
      if( inclusiveThis ) // return this ?
      {
        if( !includeAbstract && isAbstract() ) // abstract-behaviour ?
          return new FeatureType[0];
        return new FeatureType[] { this };
      }
      return new FeatureType[0];
    }
    if( contextSchema == null )
      contextSchema = getGMLSchema();
    // query for it
    final FindSubstitutesGMLSchemaVisitor visitor = new FindSubstitutesGMLSchemaVisitor( contextSchema, this, includeAbstract, inclusiveThis );
    contextSchema.accept( visitor );
    return visitor.getSubstitutes();
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#isAbstract()
   */
  public boolean isAbstract( )
  {
    return m_element.isSetAbstract();
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getSubstitutionGroupFT()
   */
  public FeatureType getSubstitutionGroupFT( )
  {
    return m_substituteFT;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getProperties()
   */
  public IPropertyType[] getProperties( )
  {
    return m_featureContentType.getProperties();
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getProperties(int)
   */
  public IPropertyType getProperties( int position )
  {
    return getProperties()[position];
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getSizeOfProperties()
   */
  public int getSizeOfProperties( )
  {
    return getProperties().length;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getProperty(javax.xml.namespace.QName)
   */
  public IPropertyType getProperty( final QName qName )
  {
    return m_qNameMap.get( qName );
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getProperty(java.lang.String)
   * @deprecated use getProperty(IPropertyType)
   */
  @Deprecated
  public IPropertyType getProperty( final String propNameLocalPart )
  {
    return m_localPartMap.get( propNameLocalPart );
  }

  public int getDefaultGeometryPropertyPosition( )
  {
    return m_defaultGeometryPosition;
  }

  public int getPropertyPosition( IPropertyType propertyType )
  {
    return m_positionMap.get( propertyType ).intValue();
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getAllGeomteryProperties()
   */
  public IValuePropertyType[] getAllGeomteryProperties( )
  {
    return m_geoPTs;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getDefaultGeometryProperty()
   */
  public IValuePropertyType getDefaultGeometryProperty( )
  {
    if( m_geoPTs.length > 0 )
      return m_geoPTs[0];
    return null;
  }

  @Override
  public String toString( )
  {
    final StringBuffer b = new StringBuffer( super.toString() );
    final IPropertyType[] properties = getProperties();
    for( int i = 0; i < properties.length; i++ )
    {
      final IPropertyType pt = properties[i];
      b.append( "\n    - " ).append( pt.getQName() );
    }
    return b.toString();
  }

}
