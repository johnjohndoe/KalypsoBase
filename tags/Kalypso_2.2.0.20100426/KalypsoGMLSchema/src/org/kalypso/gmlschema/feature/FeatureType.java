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
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.KalypsoGmlSchemaExtensions;
import org.kalypso.gmlschema.annotation.AnnotationUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.builder.IInitialize;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.property.virtual.IFunctionPropertyType;
import org.kalypso.gmlschema.property.virtual.VirtualFunctionPropertyFactory;
import org.kalypso.gmlschema.property.virtual.VirtualFunctionValueWrapperPropertyType;
import org.kalypso.gmlschema.property.virtual.VirtualFunctionWrapperRelationType;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.ElementReference;
import org.kalypso.gmlschema.xml.QualifiedElement;

/**
 * representation of a feature definition from xml schema
 * 
 * @author Andreas von Dömming
 */
@SuppressWarnings("deprecation")
public class FeatureType extends QualifiedElement implements IDetailedFeatureType
{
  private final static QName QNAME_LOCATION = new QName( NS.GML3, "location" ); //$NON-NLS-1$

  private final HashMap<IPropertyType, Integer> m_positionMap = new HashMap<IPropertyType, Integer>();

  private final HashMap<String, IPropertyType> m_localPartMap = new HashMap<String, IPropertyType>();

  private final HashMap<QName, IPropertyType> m_qNameMap = new HashMap<QName, IPropertyType>();

  // will be initialized in init()
  private IFeatureContentType m_featureContentType = null;

  // will be initialized in init()
  private IFeatureType m_substituteFT = null;

  private IPropertyType[] m_properties = null;

  private IValuePropertyType m_defaultGeometry = null;

  private IValuePropertyType[] m_geoPTs = null;

  private final IAnnotation m_annotation;

  public FeatureType( final GMLSchema gmlSchema, final Element element )
  {
    super( gmlSchema, element, QualifiedElement.createQName( gmlSchema, element ) );

    final QName qname = getQName();
    final IAnnotation annotation = AnnotationUtilities.annotationFromProperties( gmlSchema.getResourceBundle(), new QName[] { qname }, qname.getLocalPart() );
    if( annotation == null )
      m_annotation = AnnotationUtilities.annotationForElement( element.getAnnotation(), element.getName(), true );
    else
      m_annotation = annotation;
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
        break;
      }
      case IInitialize.INITIALIZE_RUN_GEOMETRY:
      {
        initSecond();
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

  private void initFirst( ) throws GMLSchemaException
  {
    final ComplexTypeReference complexTypeReference = GMLSchemaUtilities.getComplexTypeReferenceFor( getGMLSchema(), getElement() );
    final ComplexType complexType = complexTypeReference.getComplexType();
    final GMLSchema schema = complexTypeReference.getGMLSchema();
    m_featureContentType = (IFeatureContentType) schema.getBuildedObjectFor( complexType );

    // initialize substitution group
    final QName substitutionGroup = getElement().getSubstitutionGroup();
    if( substitutionGroup != null )
    {
      final ElementReference substitutesReference = getGMLSchema().resolveElementReference( substitutionGroup );
      if( substitutesReference == null )
        throw new GMLSchemaException( "Could not find substitution reference: " + substitutionGroup ); //$NON-NLS-1$

      final GMLSchema substiututesGMLSchema = substitutesReference.getGMLSchema();
      final Element substitutesElement = substitutesReference.getElement();
      if( substiututesGMLSchema.getTargetNamespace().equals( NS.GML2 ) && "_Object".equals( substitutesElement.getName() ) ) //$NON-NLS-1$
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

  private void initSecond( ) throws GMLSchemaException
  {
    final IPropertyType[] properties = m_featureContentType.getProperties();

    final Map<QName, IPropertyType> propertyTypes = new LinkedHashMap<QName, IPropertyType>();
    for( final IPropertyType propertyType : properties )
    {
      // REMARK+HACK: somewhat of a hack, but necessary to have unique properties
      // for each feature type.
      final IPropertyType localClone = propertyType.cloneForFeatureType( this );
      localClone.init( IInitialize.INITIALIZE_RUN_FIRST );
      localClone.init( IInitialize.INITIALIZE_RUN_GEOMETRY );
      localClone.init( IInitialize.INITIALIZE_RUN_DEPRECATED );

      propertyTypes.put( propertyType.getQName(), localClone );
    }

    addVirtualProperties( propertyTypes );

    m_properties = propertyTypes.values().toArray( new IPropertyType[propertyTypes.size()] );
  }

  /**
   * init map for fast finding IPropertyTypes
   */
  private void initDeprecated( )
  {
    final IPropertyType[] pts = getProperties();
    for( final IPropertyType pt : pts )
    {
      m_localPartMap.put( pt.getName(), pt );
      m_qNameMap.put( pt.getQName(), pt );
    }
  }

  /**
   * initialize geometry information after
   */
  private void initGeometry( )
  {
    final List<IValuePropertyType> list = new ArrayList<IValuePropertyType>();

    final IPropertyType[] properties = getProperties();
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

    if( m_defaultGeometry == null )
      m_defaultGeometry = locationProp;

    m_geoPTs = list.toArray( new IValuePropertyType[list.size()] );
  }

  // TODO: remove; change GmlSchemaContentProvider for that
  @Deprecated
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
  public IFeatureType getSubstitutionGroupFT( )
  {
    return m_substituteFT;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getProperties()
   */
  public IPropertyType[] getProperties( )
  {
    return m_properties;
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
    return m_geoPTs;
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
    if( m_properties != null )
    {
      final IPropertyType[] properties = getProperties();
      for( final IPropertyType pt : properties )
        b.append( "\n    - " ).append( pt.getQName() ); //$NON-NLS-1$
    }
    return b.toString();
  }

  /**
   * Creates and adds all 'virtual' properties to the given list.
   */
  private void addVirtualProperties( final Map<QName, IPropertyType> propertyTypes ) throws GMLSchemaException
  {
    try
    {
      // First add all properties defined via annotations
      final XmlObject[] funcProps = m_featureContentType.collectFunctionProperties();
      for( final XmlObject funcProp : funcProps )
      {
        final IFunctionPropertyType pt = VirtualFunctionPropertyFactory.createFromCursor( this, funcProp );
        addAndCheck( propertyTypes, pt );
      }

      // Second, add all properties defined via extension-points
      final IFunctionPropertyType[] extensionVpts = KalypsoGmlSchemaExtensions.createVirtualPropertyTypes( this );
      for( final IFunctionPropertyType pt : extensionVpts )
        addAndCheck( propertyTypes, pt );
    }
    catch( final GMLSchemaException e )
    {
      throw e;
    }
  }

  /**
   * Puts the property type into the map; If a real property with the same name exists it is checked if they correspond,
   * else an exception is thrown.
   */
  private void addAndCheck( final Map<QName, IPropertyType> propertyTypes, final IFunctionPropertyType virtualPt )
  {
    final QName name = virtualPt.getQName();
    if( !propertyTypes.containsKey( name ) )
    {
      // new property is pure virtual; just add it and return
      propertyTypes.put( name, virtualPt );
      return;
    }

    // If a real property with that name exists, we replace the virtual one with a wrapper to the real property. If a
    // valueQName was defined in
    // the virtual definition it is ignored an replaces by the real one.
    final IPropertyType realPt = propertyTypes.get( name );

    if( realPt instanceof IValuePropertyType )
    {
      final VirtualFunctionValueWrapperPropertyType wrappedPt = new VirtualFunctionValueWrapperPropertyType( (IValuePropertyType) realPt, virtualPt.getFunctionId(), virtualPt.getFunctionProperties() );
      propertyTypes.put( name, wrappedPt );
    }
    else if( realPt instanceof IRelationType )
    {
      final VirtualFunctionWrapperRelationType wrappedPt = new VirtualFunctionWrapperRelationType( (IRelationType) realPt, virtualPt.getFunctionId(), virtualPt.getFunctionProperties() );
      propertyTypes.put( name, wrappedPt );
    }
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getAnnotation()
   */
  public IAnnotation getAnnotation( )
  {
    return m_annotation;
  }

}
