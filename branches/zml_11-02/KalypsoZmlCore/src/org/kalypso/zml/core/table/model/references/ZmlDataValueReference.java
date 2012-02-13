/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.core.table.model.references;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.zml.core.table.binding.DataColumn;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;

/**
 * @author Dirk Kuch
 */
public class ZmlDataValueReference extends AbstractZmlCell implements IZmlModelValueCell
{

  public ZmlDataValueReference( final IZmlModelRow row, final IZmlModelColumn column, final int tupleModelIndex )
  {
    super( row, column, tupleModelIndex );
  }

  @Override
  public Date getIndexValue( )
  {
    try
    {
      final DataColumn type = getColumn().getDataColumn();
      final IAxis[] axes = getColumn().getAxes();
      final IAxis axis = AxisUtils.findAxis( axes, type.getIndexAxis() );

      return (Date) getColumn().get( getModelIndex(), axis );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

    return getRow().getIndex();
  }

  @Override
  public Number getValue( ) throws SensorException
  {
    return (Number) getColumn().get( getModelIndex(), getColumn().getValueAxis() );
  }

  @Override
  public void doUpdate( final Number value, final String source, final Integer status ) throws SensorException
  {
    getColumn().doUpdate( getModelIndex(), value, source, status );
  }

  public String getIdentifier( )
  {
    return getColumn().getIdentifier();
  }

  @Override
  public Integer getStatus( ) throws SensorException
  {
    final IAxis axis = getColumn().getStatusAxis();
    if( axis == null )
      return null;

    final Object value = getColumn().get( getModelIndex(), axis );
    if( value instanceof Number )
      return ((Number) value).intValue();

    return null;
  }

  @Override
  public String getDataSource( ) throws SensorException
  {
    final MetadataList metadata = getColumn().getMetadata();
    final IAxis axis = AxisUtils.findDataSourceAxis( getColumn().getAxes(), getColumn().getValueAxis() );

    final Object objIndex = getColumn().get( getModelIndex(), axis );
    if( objIndex instanceof Number )
    {
      final Number index = (Number) objIndex;

      final DataSourceHandler handler = new DataSourceHandler( metadata );
      return handler.getDataSourceIdentifier( index.intValue() );
    }

    return null;
  }

  @Override
  public String getHref( )
  {
    final IObservation observation = getColumn().getObservation();
    final String href = observation.getHref();

    try
    {
      final IFile resource = ResourceUtilities.findFileFromPath( new Path( href ) );
      if( resource != null )
        return resource.getProjectRelativePath().toOSString();
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }

    return href;
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof ZmlDataValueReference )
    {
      try
      {
        final ZmlDataValueReference other = (ZmlDataValueReference) obj;

        final EqualsBuilder builder = new EqualsBuilder();
        builder.append( getColumn().getIdentifier(), other.getColumn().getIdentifier() );
        builder.append( getModelIndex(), getModelIndex() );
        builder.append( getValue(), other.getValue() );

        return builder.isEquals();
      }
      catch( final SensorException e )
      {
        e.printStackTrace();
      }
    }

    return super.equals( obj );
  }

  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( getClass().getName() );
    builder.append( getColumn().getIdentifier() );
    builder.append( getModelIndex() );

    return builder.toHashCode();
  }
}
