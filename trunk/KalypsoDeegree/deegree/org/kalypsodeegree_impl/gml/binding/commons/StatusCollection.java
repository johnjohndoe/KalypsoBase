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
import org.kalypso.contribs.eclipse.core.runtime.IStatusWithTime;
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
  private final IFeatureBindingCollection<IGeoStatus> m_statusCollection = new FeatureBindingCollection<>( this, IGeoStatus.class, QNAME_PROP_STATUS_MEMBER );

  public StatusCollection( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  @Override
  public IGeoStatus createGeoStatus( final IStatus status )
  {
    final GM_Object location = status instanceof IGeoStatus ? ((IGeoStatus)status).getLocation() : null;
    final Date time = status instanceof IStatusWithTime ? ((IStatusWithTime)status).getTime() : null;

    return createGeoStatus( status, location, time );
  }

  @Override
  public IGeoStatus createGeoStatus( final IStatus status, final GM_Object location, final Date time )
  {
    if( status == null )
      return null;

    if( !status.isMultiStatus() )
      return createGeoStatus( status.getSeverity(), status.getPlugin(), status.getCode(), status.getMessage(), status.getException(), location, time );

    return createMultiGeoStatus( status, location, time );
  }

  @Override
  public IGeoStatus createGeoStatus( final IGeoStatus geoStatus )
  {
    if( geoStatus == null )
      return null;

    if( !geoStatus.isMultiStatus() )
      return createGeoStatus( geoStatus.getSeverity(), geoStatus.getPlugin(), geoStatus.getCode(), geoStatus.getMessage(), geoStatus.getException(), geoStatus.getLocation(), geoStatus.getTime() );

    return createMultiGeoStatus( geoStatus );
  }

  @Override
  public IGeoStatus createGeoStatus( final int severity, final String pluginId, final int code, final String message, final Throwable exception, final GM_Object location, final Date time )
  {
    /* Add a new feature. */
    final IGeoStatus geoStatus = m_statusCollection.addNew( IGeoStatus.QNAME );

    /* Set the values of the status. */
    setStatusValues( severity, pluginId, code, message, exception, location, time, geoStatus );

    return geoStatus;
  }

  @Override
  public boolean contains( final IStatus simulationStatus )
  {
    return m_statusCollection.contains( simulationStatus );
  }

  @Override
  public boolean isEmpty( )
  {
    return m_statusCollection.isEmpty();
  }

  @Override
  public IFeatureBindingCollection<IGeoStatus> getStatusList( )
  {
    return m_statusCollection;
  }

  private IGeoStatus createMultiGeoStatus( final IStatus status, final GM_Object location, final Date time )
  {
    /* Add a new feature. */
    final IGeoStatus multiGeoStatus = m_statusCollection.addNew( IGeoStatus.QNAME );

    /* Copy the values of the status. */
    copyStatusValues( status, location, time, multiGeoStatus );

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

      /* Copy the values of the status. */
      copyStatusValues( status, location, time, geoStatus );

      return;
    }

    /* Add a new feature. */
    final IGeoStatus multiGeoStatus = childrenCollection.addNew( IGeoStatus.QNAME );

    /* Copy the values of the status. */
    copyStatusValues( status, location, time, multiGeoStatus );

    /* Get the children. */
    final IStatus[] children = status.getChildren();
    for( final IStatus element : children )
      addToMultiGeoStatus( multiGeoStatus, element, location, time );
  }

  private IGeoStatus createMultiGeoStatus( final IGeoStatus geoStatus )
  {
    /* Add a new feature. */
    final IGeoStatus multiGeoStatus = m_statusCollection.addNew( IGeoStatus.QNAME );

    /* Copy the values of the status. */
    copyStatusValues( geoStatus, multiGeoStatus );

    /* Get the children. */
    final IStatus[] children = geoStatus.getChildren();
    for( final IStatus element : children )
      addToMultiGeoStatus( multiGeoStatus, (IGeoStatus)element );

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

      /* Copy the values of the status. */
      copyStatusValues( geoStatus, children );

      return;
    }

    /* Add a new feature. */
    final IGeoStatus multiGeoStatus = childrenCollection.addNew( IGeoStatus.QNAME );

    /* Copy the values of the status. */
    copyStatusValues( geoStatus, multiGeoStatus );

    /* Get the children. */
    final IStatus[] children = geoStatus.getChildren();
    for( final IStatus element : children )
      addToMultiGeoStatus( multiGeoStatus, (IGeoStatus)element );
  }

  @Override
  public final IStatus toStatus( )
  {
    final List<IStatus> children = new ArrayList<>();

    final FeatureList list = m_statusCollection.getFeatureList();
    for( final Object object : list )
    {
      if( !(object instanceof GeoStatus) )
        continue;

      final GeoStatus status = (GeoStatus)object;
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

  private void setStatusValues( final int severity, final String pluginId, final int code, final String message, final Throwable exception, final GM_Object location, final Date time, final IGeoStatus geoStatus )
  {
    /* Set its properties. */
    geoStatus.setSeverity( severity );
    geoStatus.setPlugin( pluginId );
    geoStatus.setCode( code );
    geoStatus.setMessage( message );
    geoStatus.setException( exception );
    geoStatus.setLocation( location );
    geoStatus.setTime( time );
  }

  private void copyStatusValues( final IStatus status, final GM_Object location, final Date time, final IGeoStatus geoStatus )
  {
    /* Set its properties. */
    geoStatus.setSeverity( status.getSeverity() );
    geoStatus.setPlugin( status.getPlugin() );
    geoStatus.setCode( status.getCode() );
    geoStatus.setMessage( status.getMessage() );
    geoStatus.setException( status.getException() );
    geoStatus.setLocation( location );

    /* Handle a status with time priorized. */
    if( status instanceof IStatusWithTime )
      geoStatus.setTime( ((IStatusWithTime)status).getTime() );
    else
      geoStatus.setTime( time );
  }

  private void copyStatusValues( final IGeoStatus status, final IGeoStatus geoStatus )
  {
    /* Set its properties. */
    geoStatus.setSeverity( status.getSeverity() );
    geoStatus.setPlugin( status.getPlugin() );
    geoStatus.setCode( status.getCode() );
    geoStatus.setMessage( status.getMessage() );
    geoStatus.setException( status.getException() );
    geoStatus.setLocation( status.getLocation() );
    geoStatus.setTime( status.getTime() );
  }
}