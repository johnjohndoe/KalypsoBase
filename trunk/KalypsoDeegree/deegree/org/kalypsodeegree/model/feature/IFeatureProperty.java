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
package org.kalypsodeegree.model.feature;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.property.IPropertyType;

/**
 * Correspondents to FeatureProperty of deegree2.<br/>
 * the interface describes a property entry of a feature
 *
 * @author Gernot Belger
 */
public interface IFeatureProperty
{
  /**
   * returns the qualified name of the property
   */
  QName getName( );

  /**
   * returns the value of the property
   */
  Object getValue( );

// /**
// * returns the value of the property; if the value is null the passed defaultValuewill be returned
// *
// * @param defaultValue
// */
// Object getValue( Object defaultValue );

  /**
   * sets the value of the property
   */
  void setValue( Object value );

  /**
   * Returns the instance of the Feature a Feature property is assigned to.
   */
  Feature getOwner( );

  /* Kalypso Additions */

  /**
   * Returns the IPropertyType that corresponds to this property.
   */
  IPropertyType getPropertyType( );
}
