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
package org.kalypso.gmlschema.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang.ObjectUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.eclipse.core.runtime.Platform;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.KalypsoGMLSchemaPlugin;
import org.kalypso.gmlschema.annotation.DefaultAnnotation;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.xml.ElementReference;

/**
 * This is a FeatureType created programatically.
 *
 * @author doemming, kurzbach
 */
public class CustomFeatureType implements IFeatureType
{
  private final QName m_qName;

  private final HashMap<QName, IPropertyType> m_qNameMap = new HashMap<QName, IPropertyType>();

  private final HashMap<String, IPropertyType> m_locatPartMap = new HashMap<String, IPropertyType>();

  private final HashMap<IPropertyType, Integer> m_positionMap = new HashMap<IPropertyType, Integer>();

  private final IPropertyType[] m_properties;

  private final IValuePropertyType[] m_allGeometryPTS;

  private final int m_defaultGeometryPosition;

  private final IGMLSchema m_schema;

  private final DefaultAnnotation m_annotation;

  private IFeatureType m_substitutionGroupFT = null;

  /**
   * The {@link CustomFeatureType} may be backed by a real schema if that is needed. However in some cases it is ok to
   * provide an {@link org.kalypso.gmlschema.EmptyGMLSchema} in the constructor. Note/TODO: for substitution types it is
   * necessary at the time to use GMLSchema instead of the IGMLSchema interface.
   */
  public CustomFeatureType( final IGMLSchema schema, final QName qName, final IPropertyType[] pts, final QName substitutionGroup )
  {
    this( schema, qName, pts );
    if( substitutionGroup != null )
    {
      try
      {
        if( !(schema instanceof GMLSchema) )
          return;

        final ElementReference substitutesReference = ((GMLSchema)schema).resolveElementReference( substitutionGroup );
        if( substitutesReference == null )
          return;
        final GMLSchema substiututesGMLSchema = substitutesReference.getGMLSchema();
        final Element substitutesElement = substitutesReference.getElement();
        if( substiututesGMLSchema.getTargetNamespace().equals( NS.GML2 ) && "_Object".equals( substitutesElement.getName() ) ) //$NON-NLS-1$
          return;
        final FeatureType ft = (FeatureType) substiututesGMLSchema.getBuildedObjectFor( substitutesElement );
        m_substitutionGroupFT = ft;
      }
      catch( final GMLSchemaException e )
      {
        KalypsoGMLSchemaPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
        throw new IllegalStateException( e );
      }

    }
  }

  /**
   * Use this constructor if no substitution type is given. This might result in some serious errors so it is suggested
   * to use the other constructor.
   *
   * @deprecated
   */
  @Deprecated
  public CustomFeatureType( final IGMLSchema schema, final QName qName, final IPropertyType[] pts )
  {
    m_schema = schema;
    m_qName = qName;
    m_annotation = new DefaultAnnotation( Platform.getNL(), qName.getLocalPart() );
    int geoPos = -1;
    final List<IValuePropertyType> col = new ArrayList<IValuePropertyType>();
    for( int i = 0; i < pts.length; i++ )
    {
      final IPropertyType pt = pts[i];
      m_qNameMap.put( pt.getQName(), pt );
      m_locatPartMap.put( pt.getQName().getLocalPart(), pt );
      m_positionMap.put( pt, new Integer( i ) );
      if( pt instanceof IValuePropertyType )
      {
        final IValuePropertyType vpt = (IValuePropertyType) pt;
        if( vpt.isGeometry() )
        {
          col.add( vpt );
          if( geoPos < 0 )
          {
            geoPos = i;
          }
        }
      }
    }
    m_allGeometryPTS = col.toArray( new IValuePropertyType[col.size()] );
    m_defaultGeometryPosition = geoPos;
    m_properties = pts;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getName()
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  public String getName( )
  {
    return m_qName.getLocalPart();
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
    return m_properties[position];
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getSizeOfProperties()
   */
  public int getSizeOfProperties( )
  {
    return m_properties.length;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getProperty(javax.xml.namespace.QName)
   */
  public IPropertyType getProperty( final QName qname )
  {
    return m_qNameMap.get( qname );
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getProperty(java.lang.String)
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  public IPropertyType getProperty( final String propNameLocalPart )
  {
    return m_locatPartMap.get( propNameLocalPart );
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#isAbstract()
   */
  public boolean isAbstract( )
  {
    return false;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getSubstitutionGroupFT()
   */
  public IFeatureType getSubstitutionGroupFT( )
  {
    return m_substitutionGroupFT;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getQName()
   */
  public QName getQName( )
  {
    return m_qName;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getNamespace()
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  public String getNamespace( )
  {
    return m_qName.getNamespaceURI();
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getDefaultGeometryPropertyPosition()
   */
  public int getDefaultGeometryPropertyPosition( )
  {
    return m_defaultGeometryPosition;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getPropertyPosition(org.kalypso.gmlschema.property.IPropertyType)
   */
  public int getPropertyPosition( final IPropertyType pt )
  {
    final Integer pos = m_positionMap.get( pt );

    if( pos == null )
      return -1;

    return pos;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getAllGeomteryProperties()
   */
  public IValuePropertyType[] getAllGeomteryProperties( )
  {
    return m_allGeometryPTS;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getDefaultGeometryProperty()
   */
  public IValuePropertyType getDefaultGeometryProperty( )
  {
    return m_allGeometryPTS[0];
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( final int initializeRun )
  {
    // nothing to init
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getGMLSchema()
   */
  public IGMLSchema getGMLSchema( )
  {
    return m_schema;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return m_qName.toString();
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getAnnotation()
   */
  public IAnnotation getAnnotation( )
  {
    return m_annotation;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof IFeatureType )
      return ObjectUtils.equals( m_qName, ((IFeatureType) obj).getQName() );

    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    return ObjectUtils.hashCode( m_qName );
  }
}
