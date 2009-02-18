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

import org.eclipse.core.runtime.IAdaptable;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.builder.IInitialize;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.virtual.IVirtualFunctionPropertyType;

/**
 * @author doemming
 */
public interface IFeatureType extends IInitialize, IAdaptable
{
  /**
   * @return the name of the FeatureType
   * @deprecated TODO change to getNameLocalPart
   */
  @Deprecated
  public String getName( );

  public IGMLSchema getGMLSchema( );

  /**
   * returns the properties of this feature type
   */
  public IPropertyType[] getProperties( );

  /** @TODO rename to 'getProperty' ? */
  public IPropertyType getProperties( int position );

  public int getSizeOfProperties( );

  /**
   * To get a feature property of virtual function property given its {@link QName}
   * 
   * @return a property or virtual function property of this feature type identified by its q-name, or <code>null</code>
   *         if unknown property
   */
  public IPropertyType getProperty( QName qname );

  /**
   * To get a feature property of virtual function property given its name
   * 
   * @return a property or virtual function property of this feature type identified by its name, or <code>null</code>
   *         if unknown property
   * @deprecated use getProperty(QName qname)
   */
  @Deprecated
  public IPropertyType getProperty( String nameLocalPart );

  /**
   * @return true if is abstract
   */
  public boolean isAbstract( );

  public IFeatureType getSubstitutionGroupFT( );

  public QName getQName( );

  /**
   * @return namespace
   * @deprecated use getQName
   */
  @Deprecated
  public String getNamespace( );

  // TODO: remove this method from interface, declare it private in implementation
  public int getPropertyPosition( final IPropertyType propertyType );

  /**
   * To get all geometry properties including virtual function geometry property.
   * 
   * @return all geometry properties including the ones are array
   */
  public IValuePropertyType[] getAllGeomteryProperties( );

  /**
   * To get the default geometry. This method may return a virtual function geometry property as default geometry
   * 
   * @return the default geometry property
   */
  public IValuePropertyType getDefaultGeometryProperty( );

  /**
   * To get the virtual function property given its {@link QName}
   * 
   * @return the virtual property of the given {@link QName} or null if there is no such virtual property
   */
  public IPropertyType getVirtualProperty( QName propQName );

  /**
   * Checks if a property type is a virtual property
   * 
   * @return true if the given property type is virtual property
   */
  public boolean isVirtualProperty( IPropertyType pt );

  /**
   * To get all virtual function geometry property
   * 
   * @return all virtual function geometry property as array
   */
  public IVirtualFunctionPropertyType[] getVirtualGeometryProperties( );
}
