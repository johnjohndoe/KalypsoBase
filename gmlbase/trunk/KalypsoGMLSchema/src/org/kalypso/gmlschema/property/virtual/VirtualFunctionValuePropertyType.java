/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.gmlschema.property.virtual;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.property.IPropertyContentType;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.types.ITypeHandler;

/**
 * Default implementation of {@link IVirtualFunctionValuePropertyType}
 * bases on the {@link ITypeHandler} associated with the property
 * q-name
 * 
 * @author Congo Patrice 
 */
public class VirtualFunctionValuePropertyType 
                  implements IVirtualFunctionValuePropertyType
{

  private final ITypeHandler handler;
  private final QName typeQName;
  
  private IRestriction[] restrictions = {};
  private int maxOccurs = 1;
  private int minOccurs = 0;
  private boolean isList = false;
  private boolean isNillable = false;
  private String defaultValue = null;
  private String fixed = null;
  private boolean hasDefault = false;
  private boolean isFixed = false;
  private boolean isNullable = false;
  
  /**
   * Creates a new {@link VirtualFunctionPropertyType} 
   * for the given value handler and the property q-name
   * @param handler handler for the property value 
   * @param propertyQName the q-name of the virtual property 
   */
  public VirtualFunctionValuePropertyType(
                            ITypeHandler handler, 
                            QName propertyQName)
                            throws IllegalArgumentException
  {
    if(handler == null || propertyQName == null)
    {
      String message = 
        String.format( 
            "%s %s",
            handler==null?"null":handler.toString(),
            propertyQName==null?"null":propertyQName.toString());
      throw new IllegalArgumentException(message);
    }
    this.handler = handler;
    this.typeQName = propertyQName;
  }

  /**
   * @see org.kalypso.gmlschema.property.virtual.IVirtualFunctionPropertyType#isGeometry()
   */
  public boolean isGeometry( )
  {
    return handler.isGeometry();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getMaxOccurs()
   */
  public int getMaxOccurs( )
  {
    return this.maxOccurs;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getMinOccurs()
   */
  public int getMinOccurs( )
  {
    return this.minOccurs;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getName()
   */
  public String getName( )
  {
    return typeQName.getLocalPart();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getQName()
   */
  public QName getQName( )
  {
    return typeQName;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isList()
   */
  public boolean isList( )
  {
    return this.isList;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isNillable()
   */
  public boolean isNillable( )
  {
    return this.isNillable;
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( int initializeRun ) throws GMLSchemaException
  {
    
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  public Object getAdapter( Class adapter )
  {
    return null;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getDefault()
   */
  public String getDefault( )
  {
    return this.defaultValue;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getFixed()
   */
  public String getFixed( )
  {
    return this.fixed;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getPropertyContentType()
   */
  public IPropertyContentType getPropertyContentType( )
  {
    return null;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getRestriction()
   */
  public IRestriction[] getRestriction( )
  {
    return this.restrictions;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getTypeHandler()
   */
  public ITypeHandler getTypeHandler( )
  {
    return this.handler;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getValueClass()
   */
  public Class getValueClass( )
  {
    return handler.getValueClass();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getValueQName()
   */
  public QName getValueQName( )
  {
    return handler.getTypeName();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#hasDefault()
   */
  public boolean hasDefault( )
  {
    return this.hasDefault;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#hasRestriction()
   */
  public boolean hasRestriction( )
  {
    if(restrictions == null)
    {
      return false;
    }
    else
    {
      return restrictions.length>0;
    }
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isFixed()
   */
  public boolean isFixed( )
  {
    return this.isFixed;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isNullable()
   */
  public boolean isNullable( )
  {
    return this.isNullable;
  }

}
