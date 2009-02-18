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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.builder.IInitialize;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.virtual.IVirtualFunctionPropertyType;
import org.kalypso.gmlschema.property.virtual.IVirtualFunctionValuePropertyType;
import org.kalypso.gmlschema.property.virtual.VirtualFunctionPropertyFactory;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.ElementReference;
import org.kalypso.gmlschema.xml.QualifiedElement;

/**
 * representation of a feature definition from xml schema
 * 
 * @author doemming
 */
public class FeatureType extends QualifiedElement implements IDetailedFeatureType
{
  // TODO: do we really need this, smells bad!
  private final HashMap<IPropertyType, Integer> m_positionMap = new HashMap<IPropertyType, Integer>();

  /** virtual properties are also added to this map */
  private final HashMap<String, IPropertyType> m_localPartMap = new HashMap<String, IPropertyType>();

  private final HashMap<QName, IPropertyType> m_qNameMap = new HashMap<QName, IPropertyType>();

  // TODO: this is SO very ugly! Put virtual properties into list of all properties and mark them virtual
  private final Map<QName, IVirtualFunctionPropertyType> virtualPropertyTypes = new HashMap<QName, IVirtualFunctionPropertyType>();

  // will initilized in init()
  private IFeatureContentType m_featureContentType = null;

  // will initilized in init()
  private FeatureType m_substituteFT = null;

  private IValuePropertyType m_defaultGeometry = null;

  private IValuePropertyType[] m_geoPTs = null;

  // TODO: this extra virtual stuff is SO bad! Dont ever again do such a thing!
  private IVirtualFunctionValuePropertyType[] virtualFuncGeoTypes;

  public FeatureType( final GMLSchema gmlSchema, final Element element )
  {
    super( gmlSchema, element );
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( final int initializeRun ) throws GMLSchemaException
  {
    switch( initializeRun )
    {
      case IInitialize.INITIALIZE_RUN_FIRST:
      {
        initFirst();
        initVirtualPropertyTypes();
        break;
      }
      case IInitialize.INITIALIZE_RUN_GEOMETRY:
      {
        initGeometry();
        break;
      }
      case IInitialize.INITIALIZE_RUN_DEPRECATED:
      {
        initDeprecated();
        break;
      }
    }
  }

  /**
   * Get all function properties that declare themselve virtual. This method must be invoke after to regular properties
   * of the feature have been collected since
   * {@link VirtualFunctionPropertyFactory#createVirtualFunctionPropertyTypes(IDetailedFeatureType)} uses
   * {@link #getProperty(QName)} to checks whether a regular property of the given name already exists
   * 
   * @see {@link VirtualFunctionPropertyFactory#createVirtualFunctionPropertyTypes(IDetailedFeatureType)}
   */
  private final void initVirtualPropertyTypes( ) throws IllegalArgumentException, GMLSchemaException
  {
    final Collection<IVirtualFunctionPropertyType> vfptList = VirtualFunctionPropertyFactory.createVirtualFunctionPropertyTypes( this );
    for( final IVirtualFunctionPropertyType vfpt : vfptList )
    {
      final QName propertyQName = vfpt.getQName();
      virtualPropertyTypes.put( propertyQName, vfpt );
      // !Changing deprecate store here
      m_localPartMap.put( propertyQName.getLocalPart(), vfpt );
    }

    initVirtualFuncGeometry();
  }

  /**
   * init map for fast finding IPropertyTypes
   */
  private void initDeprecated( )
  {
    final IPropertyType[] pts = m_featureContentType.getProperties();
    for( final IPropertyType pt : pts )
    {
      m_localPartMap.put( pt.getName(), pt );
      m_qNameMap.put( pt.getQName(), pt );
    }
  }

  private void initFirst( ) throws GMLSchemaException
  {
    final ComplexTypeReference complexTypeReference = GMLSchemaUtilities.getComplexTypeReferenceFor( getGMLSchema(), getElement() );
    final ComplexType complexType = complexTypeReference.getComplexType();
    final GMLSchema schema = complexTypeReference.getGMLSchema();
    m_featureContentType = (IFeatureContentType) schema.getBuildedObjectFor( complexType );
    // if( m_featureContentType == null )
    // System.out.println( "debug" );
    // initialize substitution group
    final QName substitutionGroup = getElement().getSubstitutionGroup();
    if( substitutionGroup != null )
    {
      final ElementReference substitutesReference = getGMLSchema().resolveElementReference( substitutionGroup );
      if( substitutesReference == null )
        throw new GMLSchemaException( "Could not find substitution reference: " + substitutionGroup );
      final GMLSchema substiututesGMLSchema = substitutesReference.getGMLSchema();
      final Element substitutesElement = substitutesReference.getElement();
      if( substiututesGMLSchema.getTargetNamespace().equals( NS.GML2 ) && "_Object".equals( substitutesElement.getName() ) )
        m_substituteFT = null;
      else
      {
        final FeatureType ft = (FeatureType) substiututesGMLSchema.getBuildedObjectFor( substitutesElement );
        m_substituteFT = ft;
      }
    }
    else
      m_substituteFT = null;
  }

  private final static QName QNAME_LOCATION = new QName( NS.GML3, "location" );

  /**
   * initialize geometryinformation after
   */
  private void initGeometry( )
  {
    // TODO: refactor this stuff, smells very bad....
    final IPropertyType[] properties = m_featureContentType.getProperties();
    final List<IValuePropertyType> list = new ArrayList<IValuePropertyType>();
    // REMARK: used to get position for gml:location property. We cannot access the position via getProperty(), because
    // this stuff is not yet initialized.
    IValuePropertyType locationProp = null;
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

          if( m_defaultGeometry == null && !vpt.getQName().equals( QNAME_LOCATION ) )
            m_defaultGeometry = vpt;

          if( vpt.getQName().equals( QNAME_LOCATION ) )
            locationProp = vpt;
        }
      }
    }

    // Look for default geometry in virtual property types
    for( final IVirtualFunctionPropertyType virtualPropertyType : virtualPropertyTypes.values() )
    {
      if( virtualPropertyType.isGeometry() )
      {
        if( m_defaultGeometry == null )
          m_defaultGeometry = (IValuePropertyType) virtualPropertyType;
      }
    }

    if( m_defaultGeometry == null )
      m_defaultGeometry = locationProp;

    m_geoPTs = list.toArray( new IValuePropertyType[list.size()] );
  }

  public IFeatureContentType getFeatureContentType( )
  {
    return m_featureContentType;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#isAbstract()
   */
  public boolean isAbstract( )
  {
    return getElement().isSetAbstract();
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
  public IPropertyType getProperties( final int position )
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
    IPropertyType propertyType = m_qNameMap.get( qName );
    if( propertyType == null )
    {
      propertyType = virtualPropertyTypes.get( qName );
    }
    return propertyType;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getProperty(java.lang.String)
   * @deprecated use getProperty(IPropertyType)
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  public IPropertyType getProperty( final String propNameLocalPart )
  {
    final IPropertyType propertyType = m_localPartMap.get( propNameLocalPart );
    return propertyType;
  }

  /**
   * Returns -1 if the property does not exist.
   * 
   * @see org.kalypso.gmlschema.feature.IFeatureType#getPropertyPosition(org.kalypso.gmlschema.property.IPropertyType)
   */
  public int getPropertyPosition( final IPropertyType propertyType )
  {
    final Integer pos = m_positionMap.get( propertyType );
    return pos == null ? -1 : pos.intValue();
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getAllGeomteryProperties()
   */
  public IValuePropertyType[] getAllGeomteryProperties( )
  {
    final int lengthNonVir = m_geoPTs == null ? 0 : m_geoPTs.length;
    final int lengthVir = virtualFuncGeoTypes == null ? 0 : virtualFuncGeoTypes.length;

    final IValuePropertyType[] allGeo = new IValuePropertyType[lengthNonVir + lengthVir];
    if( lengthNonVir > 0 )
    {
      System.arraycopy( m_geoPTs, 0, allGeo, 0, lengthNonVir );
    }

    if( lengthVir > 0 )
    {
      // System.out.println("Adding Virtual Geo for:"+getQName()+ " "+virtualFuncGeoTypes[0].getQName());
      System.arraycopy( virtualFuncGeoTypes, 0, allGeo, lengthNonVir, lengthVir );
    }
    return allGeo;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getDefaultGeometryProperty()
   */
  public IValuePropertyType getDefaultGeometryProperty( )
  {
    return m_defaultGeometry;
  }

  @Override
  public String toString( )
  {
    final StringBuffer b = new StringBuffer( super.toString() );
    if( m_featureContentType != null )
    {
      final IPropertyType[] properties = getProperties();
      for( final IPropertyType pt : properties )
      {
        b.append( "\n    - " ).append( pt.getQName() );
      }
    }
    return b.toString();
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getVirtualProperty(javax.xml.namespace.QName)
   */
  public IPropertyType getVirtualProperty( final QName propQName )
  {
    if( propQName == null )
    {
      throw new IllegalArgumentException( "Parameter propQName must not be null" );
    }
    return virtualPropertyTypes.get( propQName );
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#isVirtualProperty(org.kalypso.gmlschema.property.IPropertyType)
   */
  public boolean isVirtualProperty( final IPropertyType pt )
  {
    if( pt == null )
    {
      throw new IllegalArgumentException( "parameter pt must not be null" );
    }
    final QName name = pt.getQName();
    if( name == null )
    {
      throw new IllegalArgumentException( "Qname or parameter qt must not be null" );
    }
    return virtualPropertyTypes.containsKey( name );
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getVirtualGeometryProperties()
   */
  public IVirtualFunctionPropertyType[] getVirtualGeometryProperties( )
  {
    return virtualFuncGeoTypes;
  }

  private final void initVirtualFuncGeometry( ) throws GMLSchemaException
  {
    final List<IVirtualFunctionValuePropertyType> geoTypes = new ArrayList<IVirtualFunctionValuePropertyType>();
    for( final IVirtualFunctionPropertyType type : virtualPropertyTypes.values() )
    {
      if( type.isGeometry() )
      {
        if( type instanceof IVirtualFunctionValuePropertyType )
        {
          geoTypes.add( (IVirtualFunctionValuePropertyType) type );
        }
        else
        {
          throw new GMLSchemaException( "Found a virtual geometry which is not a value property:" + type );
        }
      }
      else
      {
// System.out.println( "Novirtual geo:" + type.getQName() );
      }
    }
    final IVirtualFunctionValuePropertyType[] geoTypeArray = new IVirtualFunctionValuePropertyType[geoTypes.size()];
    this.virtualFuncGeoTypes = geoTypes.toArray( geoTypeArray );
  }
}
