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
package org.kalypso.ui.addlayer.internal.wms;

import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.java.util.IModelObject;

/**
 * @author Gernot
 */
public interface ICapabilitiesData extends IModelObject
{
  String PROPERTY_ADDRESS = "address"; //$NON-NLS-1$

  /* Boolean property, true, if address can be parsed as URL */
  String PROPERTY_VALID_ADDRESS = "validAddress"; //$NON-NLS-1$

  String PROPERTY_IMAGE_PROVIDER = "imageProvider"; //$NON-NLS-1$

  String PROPERTY_LOAD_STATUS = "loadStatus"; // //$NON-NLS-1$

  String PROPERTY_CAPABILITIES = "capabilities"; //$NON-NLS-1$

  // <element ref="wms:Title"/>
  String PROPERTY_TITLE = "title"; // //$NON-NLS-1$

  // <element ref="wms:Abstract" minOccurs="0"/>
  String PROPERTY_ABSTRACT = "abstract"; // //$NON-NLS-1$


  // TODO: <element ref="wms:KeywordList" minOccurs="0"/>

  // TODO: <element ref="wms:OnlineResource"/>
  // TODO: <element ref="wms:ContactInformation" minOccurs="0"/>
  // TODO: <element ref="wms:Fees" minOccurs="0"/>
  // TODO: <element ref="wms:AccessConstraints" minOccurs="0"/>
  // TODO: <element ref="wms:LayerLimit" minOccurs="0"/>
  // TODO: <element ref="wms:MaxWidth" minOccurs="0"/>
  // TODO: <element ref="wms:MaxHeight" minOccurs="0"/>

  String getAddress( );

  boolean getValidAddress( );

  String getImageProvider( );

  IStatus getLoadStatus( );

  void setImageProvider( String providerID );

  WMSCapabilities getCapabilities( );

  void loadCapabilities( );

  String getTitle( );

  String getAbstract( );

}
