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

import org.eclipse.core.runtime.IAdaptable;
import org.kalypso.gmlschema.basics.IInitialize;

/**
 * Instances of this interface represent the properties of a {@link org.kalypso.gmlschema.feature.IFeatureType}.
 * 
 * @author doemming
 */
public interface IPropertyType extends IInitialize, IAdaptable
{
  public final static int UNBOUND_OCCURENCY = -1;

  public int getMinOccurs( );

  public int getMaxOccurs( );

  public boolean isList( );

  public QName getQName( );

  public boolean isNillable( );

  /**
   * @deprecated use getQName()
   * @return local part of qualified name
   */
  @Deprecated
  public String getName( );

}
