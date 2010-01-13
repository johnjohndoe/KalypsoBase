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

import org.eclipse.core.runtime.IStatus;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.binding.FeatureWrapperCollection;
import org.kalypsodeegree.model.geometry.GM_Object;

/**
 * A collection of geo status objects.
 * 
 * @author Gernot Belger
 */
public class StatusCollection extends FeatureWrapperCollection<IGeoStatus> implements IStatusCollection
{
  /**
   * The constructor.
   * 
   * @param featureCol
   */
  public StatusCollection( Feature featureCol )
  {
    super( featureCol, IGeoStatus.class, QNAME_PROP_STATUS_MEMBER );
  }

  /**
   * @see org.kalypsodeegree_impl.gml.binding.commons.IStatusCollection#createGeoStatus(org.eclipse.core.runtime.IStatus)
   */
  public IGeoStatus createGeoStatus( IStatus status )
  {
    return createGeoStatus( status, null, null );
  }

  /**
   * @see org.kalypsodeegree_impl.gml.binding.commons.IStatusCollection#createGeoStatus(org.eclipse.core.runtime.IStatus,
   *      org.kalypsodeegree.model.geometry.GM_Object, java.util.Date)
   */
  public IGeoStatus createGeoStatus( IStatus status, GM_Object location, Date time )
  {
    if( status == null )
      return null;

    return createGeoStatus( status.getSeverity(), status.getPlugin(), status.getCode(), status.getMessage(), status.getException(), location, time );
  }

  /**
   * @see org.kalypsodeegree_impl.gml.binding.commons.IStatusCollection#createGeoStatus(org.kalypsodeegree_impl.gml.binding.commons.IGeoStatus)
   */
  public IGeoStatus createGeoStatus( IGeoStatus geoStatus )
  {
    if( geoStatus == null )
      return null;

    return createGeoStatus( geoStatus.getSeverity(), geoStatus.getPlugin(), geoStatus.getCode(), geoStatus.getMessage(), geoStatus.getException(), geoStatus.getLocation(), geoStatus.getTime() );
  }

  /**
   * @see org.kalypsodeegree_impl.gml.binding.commons.IStatusCollection#createGeoStatus(int, java.lang.String, int,
   *      java.lang.String, java.lang.Throwable, org.kalypsodeegree.model.geometry.GM_Object, java.util.Date)
   */
  public IGeoStatus createGeoStatus( int severity, String pluginId, int code, String message, Throwable exception, GM_Object location, Date time )
  {
    /* Add a new feature. */
    IGeoStatus geoStatus = addNew( IGeoStatus.QNAME );

    /* Set its properties. */
    geoStatus.setSeverity( severity );
    geoStatus.setPlugin( pluginId );
    geoStatus.setCode( code );
    geoStatus.setMessage( message );
    geoStatus.setException( exception );
    geoStatus.setLocation( location );

    /* If a time was provided, use it. Otherwise set the current time. */
    if( time != null )
      geoStatus.setTime( time );
    else
      geoStatus.setTime( new Date() );

    return geoStatus;
  }
}