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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * A collection of geo status objects.
 * 
 * @author Gernot Belger
 */
public class StatusCollection extends Feature_Impl implements IStatusCollection
{
  private final IFeatureBindingCollection<IGeoStatus> m_statusCollection = new FeatureBindingCollection<IGeoStatus>( this, IGeoStatus.class, QNAME_PROP_STATUS_MEMBER );

  public StatusCollection( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  /**
   * @see org.kalypsodeegree_impl.gml.binding.commons.IStatusCollection#createGeoStatus(org.eclipse.core.runtime.IStatus)
   */
  @Override
  public IGeoStatus createGeoStatus( final IStatus status )
  {
    return createGeoStatus( status, null, null );
  }

  /**
   * @see org.kalypsodeegree_impl.gml.binding.commons.IStatusCollection#createGeoStatus(org.eclipse.core.runtime.IStatus,
   *      org.kalypsodeegree.model.geometry.GM_Object, java.util.Date)
   */
  @Override
  public IGeoStatus createGeoStatus( final IStatus status, final GM_Object location, final Date time )
  {
    if( status == null )
      return null;

    if( !status.isMultiStatus() )
      return createGeoStatus( status.getSeverity(), status.getPlugin(), status.getCode(), status.getMessage(), status.getException(), location, time );

    return createMultiGeoStatus( status, location, time );
  }

  /**
   * @see org.kalypsodeegree_impl.gml.binding.commons.IStatusCollection#createGeoStatus(org.kalypsodeegree_impl.gml.binding.commons.IGeoStatus)
   */
  @Override
  public IGeoStatus createGeoStatus( final IGeoStatus geoStatus )
  {
    if( geoStatus == null )
      return null;

    if( !geoStatus.isMultiStatus() )
      return createGeoStatus( geoStatus.getSeverity(), geoStatus.getPlugin(), geoStatus.getCode(), geoStatus.getMessage(), geoStatus.getException(), geoStatus.getLocation(), geoStatus.getTime() );

    return createMultiGeoStatus( geoStatus );
  }

  /**
   * @see org.kalypsodeegree_impl.gml.binding.commons.IStatusCollection#createGeoStatus(int, java.lang.String, int,
   *      java.lang.String, java.lang.Throwable, org.kalypsodeegree.model.geometry.GM_Object, java.util.Date)
   */
  @Override
  public IGeoStatus createGeoStatus( final int severity, final String pluginId, final int code, final String message, final Throwable exception, final GM_Object location, final Date time )
  {
    /* Add a new feature. */
    final IGeoStatus geoStatus = m_statusCollection.addNew( IGeoStatus.QNAME );

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

  /**
   * @see org.kalypsodeegree_impl.gml.binding.commons.IStatusCollection#contains(org.eclipse.core.runtime.IStatus)
   */
  @Override
  public boolean contains( final IStatus simulationStatus )
  {
    return m_statusCollection.contains( simulationStatus );
  }

  /**
   * @see org.kalypsodeegree_impl.gml.binding.commons.IStatusCollection#isEmpty()
   */
  @Override
  public boolean isEmpty( )
  {
    return m_statusCollection.isEmpty();
  }

  private IGeoStatus createMultiGeoStatus( final IStatus status, final GM_Object location, final Date time )
  {
    /* Add a new feature. */
    final IGeoStatus multiGeoStatus = m_statusCollection.addNew( IGeoStatus.QNAME );

    /* Set its properties. */
    multiGeoStatus.setSeverity( status.getSeverity() );
    multiGeoStatus.setPlugin( status.getPlugin() );
    multiGeoStatus.setCode( status.getCode() );
    multiGeoStatus.setMessage( status.getMessage() );
    multiGeoStatus.setException( status.getException() );
    multiGeoStatus.setLocation( location );

    /* If a time was provided, use it. Otherwise set the current time. */
    if( time != null )
      multiGeoStatus.setTime( time );
    else
      multiGeoStatus.setTime( new Date() );

    /* Get the children. */
    final IStatus[] children = status.getChildren();
    for( final IStatus element : children )
      addToMultiGeoStatus( multiGeoStatus, element, location, time );

    return multiGeoStatus;
  }

  private void addToMultiGeoStatus( final IGeoStatus parent, final IStatus status, final GM_Object location, final Date time )
  {
    /* If the given status is no multi status, simply add a new geo status and return. */
    final IFeatureBindingCollection<IGeoStatus> childrenCollection = parent.getChildrenCollection();
    if( !status.isMultiStatus() )
    {
      /* Add a new feature. */
      final IGeoStatus geoStatus = childrenCollection.addNew( IGeoStatus.QNAME );

      /* Set its properties. */
      geoStatus.setSeverity( status.getSeverity() );
      geoStatus.setPlugin( status.getPlugin() );
      geoStatus.setCode( status.getCode() );
      geoStatus.setMessage( status.getMessage() );
      geoStatus.setException( status.getException() );
      geoStatus.setLocation( location );

      /* If a time was provided, use it. Otherwise set the current time. */
      if( time != null )
        geoStatus.setTime( time );
      else
        geoStatus.setTime( new Date() );

      return;
    }

    /* Add a new feature. */
    final IGeoStatus multiGeoStatus = childrenCollection.addNew( IGeoStatus.QNAME );

    /* Set its properties. */
    multiGeoStatus.setSeverity( status.getSeverity() );
    multiGeoStatus.setPlugin( status.getPlugin() );
    multiGeoStatus.setCode( status.getCode() );
    multiGeoStatus.setMessage( status.getMessage() );
    multiGeoStatus.setException( status.getException() );
    multiGeoStatus.setLocation( location );

    /* If a time was provided, use it. Otherwise set the current time. */
    if( time != null )
      multiGeoStatus.setTime( time );
    else
      multiGeoStatus.setTime( new Date() );

    /* Get the children. */
    final IStatus[] children = status.getChildren();
    for( final IStatus element : children )
      addToMultiGeoStatus( multiGeoStatus, element, location, time );
  }

  private IGeoStatus createMultiGeoStatus( final IGeoStatus geoStatus )
  {
    /* Add a new feature. */
    final IGeoStatus multiGeoStatus = m_statusCollection.addNew( IGeoStatus.QNAME );

    /* Set its properties. */
    multiGeoStatus.setSeverity( geoStatus.getSeverity() );
    multiGeoStatus.setPlugin( geoStatus.getPlugin() );
    multiGeoStatus.setCode( geoStatus.getCode() );
    multiGeoStatus.setMessage( geoStatus.getMessage() );
    multiGeoStatus.setException( geoStatus.getException() );
    multiGeoStatus.setLocation( geoStatus.getLocation() );

    /* If a time was provided, use it. Otherwise set the current time. */
    final Date time = geoStatus.getTime();
    if( time != null )
      multiGeoStatus.setTime( time );
    else
      multiGeoStatus.setTime( new Date() );

    /* Get the children. */
    final IStatus[] children = geoStatus.getChildren();
    for( final IStatus element : children )
      addToMultiGeoStatus( multiGeoStatus, (IGeoStatus) element );

    return multiGeoStatus;
  }

  private void addToMultiGeoStatus( final IGeoStatus parent, final IGeoStatus geoStatus )
  {
    final IFeatureBindingCollection<IGeoStatus> childrenCollection = parent.getChildrenCollection();

    /* If the given geo status is no multi geo status, simply add a new geo status and return. */
    if( !geoStatus.isMultiStatus() )
    {
      /* Add a new feature. */
      final IGeoStatus children = childrenCollection.addNew( IGeoStatus.QNAME );

      /* Set its properties. */
      children.setSeverity( geoStatus.getSeverity() );
      children.setPlugin( geoStatus.getPlugin() );
      children.setCode( geoStatus.getCode() );
      children.setMessage( geoStatus.getMessage() );
      children.setException( geoStatus.getException() );
      children.setLocation( geoStatus.getLocation() );

      /* If a time was provided, use it. Otherwise set the current time. */
      final Date time = geoStatus.getTime();
      if( time != null )
        children.setTime( time );
      else
        children.setTime( new Date() );

      return;
    }

    /* Add a new feature. */
    final IGeoStatus multiGeoStatus = childrenCollection.addNew( IGeoStatus.QNAME );

    /* Set its properties. */
    multiGeoStatus.setSeverity( geoStatus.getSeverity() );
    multiGeoStatus.setPlugin( geoStatus.getPlugin() );
    multiGeoStatus.setCode( geoStatus.getCode() );
    multiGeoStatus.setMessage( geoStatus.getMessage() );
    multiGeoStatus.setException( geoStatus.getException() );
    multiGeoStatus.setLocation( geoStatus.getLocation() );

    /* If a time was provided, use it. Otherwise set the current time. */
    final Date time = geoStatus.getTime();
    if( time != null )
      multiGeoStatus.setTime( time );
    else
      multiGeoStatus.setTime( new Date() );

    /* Get the children. */
    final IStatus[] children = geoStatus.getChildren();
    for( final IStatus element : children )
      addToMultiGeoStatus( multiGeoStatus, (IGeoStatus) element );
  }

  @Override
  public final IStatus toStatus( )
  {
    final List<IStatus> children = new ArrayList<IStatus>();

    final FeatureList list = m_statusCollection.getFeatureList();
    for( final Object object : list )
    {
      if( !(object instanceof GeoStatus) )
        continue;

      final GeoStatus status = (GeoStatus) object;
      children.add( status );
    }

    if( children.isEmpty() )
      return null;
    else if( children.size() == 1 )
      return children.get( 0 );

    return StatusUtilities.createStatus( children, "Multistatus" );
  }

  public final IFeatureBindingCollection<IGeoStatus> getStati( )
  {
    return m_statusCollection;
  }

}