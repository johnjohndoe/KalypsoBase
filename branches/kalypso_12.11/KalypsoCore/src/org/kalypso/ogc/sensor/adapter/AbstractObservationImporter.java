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
package org.kalypso.ogc.sensor.adapter;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollector;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.IMetadataConstants;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHelper;

/**
 * @author Gernot Belger
 * @author Dirk Kuch
 */
public abstract class AbstractObservationImporter implements INativeObservationAdapter, IExecutableExtension
{
  public static final String MISSING_VALUE_POSTFIX = "/missing.value"; //$NON-NLS-1$

  private static final int MAX_NO_OF_ERRORS = 30;

  private String m_axisTypeValue;

  private int m_errorCount = 0;

  private String m_id;

  private IObservation m_observation;

  private String m_title;

  private String m_defaultExtension;

  @Override
  public String getDefaultExtension( )
  {
    return m_defaultExtension;
  }

  @Deprecated
  @Override
  public IAxis[] createAxis( final String valueType )
  {
    final IAxis dateAxis = TimeseriesUtils.createDefaultAxis( ITimeseriesConstants.TYPE_DATE, true );
    final IAxis valueAxis = TimeseriesUtils.createDefaultAxis( valueType );
    final IAxis statusAxis = KalypsoStatusUtils.createStatusAxisFor( valueAxis, true );
    final IAxis dataSrcAxis = DataSourceHelper.createSourceAxis( valueAxis );

    return new IAxis[] { dateAxis, valueAxis, statusAxis, dataSrcAxis };
  }

  protected ITupleModel createTuppelModel( final MetadataList metadata, final String valueType, final List<NativeObservationDataSet> datasets )
  {
    final IAxis[] axes = createAxis( valueType );
    final SimpleTupleModel model = new SimpleTupleModel( axes );

    final DataSourceHandler handler = new DataSourceHandler( metadata );

    final IAxis base = AxisUtils.findAxis( axes, valueType );

    final int dateAxis = ArrayUtils.indexOf( axes, AxisUtils.findDateAxis( axes ) );
    final int valueAxis = ArrayUtils.indexOf( axes, base );
    final int statusAxis = ArrayUtils.indexOf( axes, AxisUtils.findStatusAxis( axes, base ) );
    final int dataSourceAxis = ArrayUtils.indexOf( axes, AxisUtils.findDataSourceAxis( axes, base ) );

    for( final NativeObservationDataSet dataset : datasets )
    {
      final Date date = dataset.getDate();
      final Double value = dataset.getValue();
      final int status = dataset.getStatus();
      final String source = dataset.getSource();
      final int dataSource = handler.addDataSource( source, source );

      final Object[] data = new Object[axes.length];
      data[dateAxis] = date;
      data[valueAxis] = value;
      data[statusAxis] = status;
      data[dataSourceAxis] = dataSource;

      model.addTuple( data );
    }

    return model;
  }

  /**
   * Implemented for backwards compatibility, falls back to {@link #importTimeseries(File, TimeZone, m_axisTypeValue, boolean)}.
   * 
   * @see org.kalypso.ogc.sensor.adapter.INativeObservationAdapter#createObservationFromSource(java.io.File, java.util.TimeZone, boolean)
   */
  @Deprecated
  @Override
  public final IStatus doImport( final File file, final TimeZone timeZone, final boolean continueWithErrors )
  {
    return doImport( file, timeZone, m_axisTypeValue, continueWithErrors );
  }

  @Override
  public IStatus doImport( final File source, final TimeZone timeZone, final String valueType, final boolean continueWithErrors )
  {
    final IStatusCollector stati = new StatusCollector( KalypsoCorePlugin.getID() );

    try
    {
      final List<NativeObservationDataSet> datasets = parse( source, timeZone, continueWithErrors, stati );
      if( datasets != null )
      {
        final MetadataList metadata = new MetadataList();
        metadata.put( IMetadataConstants.MD_ORIGIN, source.getAbsolutePath() );

        final ITupleModel model = createTuppelModel( metadata, valueType, datasets );
        setObservation( new SimpleObservation( source.getAbsolutePath(), source.getName(), metadata, model ) );
      }

      return stati.asMultiStatus( Messages.getString( "AbstractObservationImporter_1" ) ); //$NON-NLS-1$
    }
    catch( final CoreException e )
    {
      return e.getStatus();
    }
    catch( final IOException e )
    {
      return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), Messages.getString("AbstractObservationImporter.0"), e ); //$NON-NLS-1$
    }
    catch( final Exception ex )
    {
      return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), ex.getMessage(), ex );
    }
  }

  protected abstract List<NativeObservationDataSet> parse( File source, TimeZone timeZone, boolean continueWithErrors, IStatusCollector stati ) throws CoreException, IOException;

  @Override
  public final String getAxisTypeValue( )
  {
    return m_axisTypeValue;
  }

  protected int getErrorCount( )
  {
    return m_errorCount;
  }

  @Override
  public String getId( )
  {
    return m_id;
  }

  protected int getMaxErrorCount( )
  {
    return MAX_NO_OF_ERRORS;
  }

  @Override
  public IObservation getObservation( )
  {
    return m_observation;
  }

  @Override
  public final void setInitializationData( final IConfigurationElement config, final String propertyName, final Object data )
  {
    m_id = config.getAttribute( "id" ); //$NON-NLS-1$
    m_defaultExtension = config.getAttribute( "extension" ); //$NON-NLS-1$
    m_title = config.getAttribute( "label" ); //$NON-NLS-1$
    m_axisTypeValue = config.getAttribute( "axisType" ); //$NON-NLS-1$
  }

  protected void setObservation( final IObservation observation )
  {
    m_observation = observation;
  }

  protected void tickErrorCount( )
  {
    m_errorCount++;
  }

  @Override
  public final String toString( )
  {
    return m_title;
  }
}