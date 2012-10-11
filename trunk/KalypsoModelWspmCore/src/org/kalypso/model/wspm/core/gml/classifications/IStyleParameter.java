/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.model.wspm.core.gml.classifications;

import javax.xml.namespace.QName;

import org.kalypso.model.wspm.core.IWspmNamespaces;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Gernot Belger
 */
public interface IStyleParameter extends Feature
{
  QName FEATURE_STYLE_PARAMETER = new QName( IWspmNamespaces.NS_WSPM_CLASSIFICATIONS, "StyleParameter" ); //$NON-NLS-1$

  QName PROPERTY_KEY = new QName( IWspmNamespaces.NS_WSPM_CLASSIFICATIONS, "key" ); //$NON-NLS-1$

  QName PROPERTY_VALUE = new QName( IWspmNamespaces.NS_WSPM_CLASSIFICATIONS, "value" ); //$NON-NLS-1$

  String getKey( );

  void setKey( String key );

  String getValue( );

  void setValue( String value );
}