/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 * 
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always. 
 * 
 * If you intend to use this software in other ways than in kalypso 
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree, 
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.gml.binding.commons;

import java.util.Date;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.xml.NS;
import org.kalypsodeegree.model.feature.binding.IFeatureWrapperCollection;
import org.kalypsodeegree.model.geometry.GM_Object;

/**
 * Binds the common:StatusCollection type.
 * 
 * @author Gernot Belger
 */
public interface IStatusCollection extends IFeatureWrapperCollection<IGeoStatus>
{
  public static final QName QNAME = new QName( NS.COMMON, "StatusCollection" );

  public static final QName QNAME_PROP_STATUS_MEMBER = new QName( NS.COMMON, "statusMember" );

  /**
   * This function creates a new geo status and adds it to the collection.
   * 
   * @param status
   *          The values will be copied from this status.
   * @return The new geo status.
   */
  public IGeoStatus createGeoStatus( IStatus status );

  /**
   * This function creates a new geo status and adds it to the collection.
   * 
   * @param status
   *          The values will be copied from this status.
   * @param location
   *          The location, or <code>null</code> if not applicable.
   * @param time
   *          The time, or <code>null</code> if not applicable.
   * @return The new geo status.
   */
  public IGeoStatus createGeoStatus( IStatus status, GM_Object location, Date time );

  /**
   * This function creates a new geo status and adds it to the collection.
   * 
   * @param geoStatus
   *          The values will be copied from this geo status.
   * @return The new geo status.
   */
  public IGeoStatus createGeoStatus( IGeoStatus geoStatus );

  /**
   * This function creates a new geo status and adds it to the collection.
   * 
   * @param severity
   *          The severity; one of <code>OK</code>, <code>ERROR</code>, <code>INFO</code>, <code>WARNING</code>, or
   *          <code>CANCEL</code>.
   * @param pluginId
   *          The unique identifier of the relevant plug-in.
   * @param code
   *          The plug-in-specific status code, or <code>OK</code>.
   * @param message
   *          A human-readable message, localized to the current locale.
   * @param exception
   *          A low-level exception, or <code>null</code> if not applicable.
   * @param location
   *          The location, or <code>null</code> if not applicable.
   * @param time
   *          The time, or <code>null</code> if not applicable.
   * @return The new geo status.
   */
  public IGeoStatus createGeoStatus( int severity, String pluginId, int code, String message, Throwable exception, GM_Object location, Date time );
}