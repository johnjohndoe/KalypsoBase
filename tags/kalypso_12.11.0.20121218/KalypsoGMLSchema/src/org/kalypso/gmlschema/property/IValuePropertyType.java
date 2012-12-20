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
package org.kalypso.gmlschema.property;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;

/**
 * Represents a value property type, whatever this is....<br>
 * TODO: it is not really clear, what a value property type IS, so this interface is quite problematic...<br>
 * TODO: we should probably refaktor at least in order to distinguish simple and complex content types.
 * 
 * @author Andreas von Dömming
 */
public interface IValuePropertyType extends IPropertyType
{
  public boolean hasRestriction( );

  public IRestriction[] getRestriction( );

  public boolean isFixed( );

  public boolean isNullable( );

  /**
   * initialize properties with this value and never change
   * 
   * @return default value
   */
  public String getFixed( );

  public boolean hasDefault( );

  public boolean isGeometry( );

  /**
   * if nothing specified initialize properties with this value
   * 
   * @return default value
   */
  public String getDefault( );

  /**
   * @return java class that instanciates the value
   */
  public Class< ? > getValueClass( );

  // TODO: which one is it? the gui or marshalling type handler??
  // is this really necessary? we still have ITypeRegistry.getTypeHandlerFor( IPropertyType )
  public IMarshallingTypeHandler getTypeHandler( );

  /**
   * @return qname of XML type
   */
  public QName getValueQName( );
}
