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
package org.kalypso.gmlschema.property;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.Assert;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;

/**
 * @author Andreas von Dömming
 */
public class CustomPropertyContentType implements IPropertyContentType
{
  private final IMarshallingTypeHandler m_typeHandler;

  private final QName m_qname;

  public CustomPropertyContentType( final QName qname, final IMarshallingTypeHandler typeHandler )
  {
    Assert.isNotNull( typeHandler );
    
    m_qname = qname;
    m_typeHandler = typeHandler;
  }

  public CustomPropertyContentType( final IMarshallingTypeHandler typeHandler )
  {
    this( null, typeHandler );
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyContentType#getValueQName()
   */
  public QName getValueQName( )
  {
    if( m_qname != null )
      return m_qname;

    return m_typeHandler.getTypeName();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyContentType#getTypeObject()
   */
  public Object getTypeObject( )
  {
    return null;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyContentType#isGeometry()
   */
  public boolean isGeometry( )
  {
    return m_typeHandler.isGeometry();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyContentType#getValueClass()
   */
  public Class< ? > getValueClass( )
  {
    return m_typeHandler.getValueClass();
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( final int initializeRun )
  {
    // nothing to init
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyContentType#getTypeHandler()
   */
  public IMarshallingTypeHandler getTypeHandler( )
  {
    return m_typeHandler;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyContentType#getGmlSchema()
   */
  public IGMLSchema getGmlSchema( )
  {
    return null;
  }

}
