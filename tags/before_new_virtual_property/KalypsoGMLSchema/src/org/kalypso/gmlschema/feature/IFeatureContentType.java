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

import org.apache.xmlbeans.XmlObject;
import org.kalypso.gmlschema.basics.IInitialize;
import org.kalypso.gmlschema.property.IPropertyType;

/**
 * Representation of a feature content definition from xml schema 
 * 
 * @author doemming
 */
public interface IFeatureContentType extends IInitialize
{
  public final static int DERIVATION_NONE = 0;

  public final static int DERIVATION_BY_RESTRICTION = 1;

  public final static int DERIVATION_BY_EXTENSION = 2;

  /**
   * @return all properties
   */
  public IPropertyType[] getProperties();

  public IFeatureContentType getBase();

  public int getDerivationType();

  /**
   * @param name
   * @return property by name
   */
  public IPropertyType getProperty( final QName name );

  public IPropertyType[] getDirectProperties();

  public QName getQName();
  
  public XmlObject[] collectFunctionProperties();
}
