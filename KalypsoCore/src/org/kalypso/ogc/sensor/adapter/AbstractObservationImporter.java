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
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.IMetadataConstants;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;

/**
 * @author Gernot Belger
 * @author Dirk Kuch
 */
public abstract class AbstractObservationImporter implements INativeObservationAdapter, IExecutableExtension
{
  private String m_axisTypeValue;

  List<NativeObservationDataSet> m_datasets = new ArrayList<>();

  private int m_errorCount = 0;

  private String m_id;

  private IObservation m_observation;

  private String m_title;

  private static final int MAX_NO_OF_ERRORS = 30;

  protected void addDataSet( final NativeObservationDataSet dataset )
  {
    m_datasets.add( dataset );
  }

  @Deprecated
  @Override
  public final IAxis[] createAxis( final String valueType )
  {
    final IAxis dateAxis = TimeseriesUtils.createDefaultAxis( ITimeseriesConstants.TYPE_DATE, true );
    final IAxis valueAxis = TimeseriesUtils.createDefaultAxis( valueType );

    return new IAxis[] { dateAxis, valueAxis };
  }

  protected ITupleModel createTuppelModel( final String valueType )
  {
    final IAxis[] axis = createAxis( valueType );
    final Object[][] tupelData = new Object[m_datasets.size()][2];

    for( int index = 0; index < m_datasets.size(); index++ )
    {
      final NativeObservationDataSet dataSet = m_datasets.get( index );

      tupelData[index][0] = dataSet.getDate();
      tupelData[index][1] = dataSet.getValue();
    }

    return new SimpleTupleModel( axis, tupelData );
  }

  /**
   * Implemented for backwards compatibility, falls back to
   * {@link #importTimeseries(File, TimeZone, m_axisTypeValue, boolean)}.
   * 
   * @see org.kalypso.ogc.sensor.adapter.INativeObservationAdapter#createObservationFromSource(java.io.File,
   *      java.util.TimeZone, boolean)
   */
  @Deprecated
  @Override
  public final IStatus doImport( final File file, final TimeZone timeZone, final boolean continueWithErrors )
  {
    return doImport( file, timeZone, m_axisTypeValue, continueWithErrors );
  }

  /**
   * @see org.kalypso.ogc.sensor.adapter.INativeObservationAdapter#doImport(java.io.File, java.util.TimeZone,
   *      java.lang.String, boolean)
   */
  @Override
  public IStatus doImport( final File source, final TimeZone timeZone, final String valueType, final boolean continueWithErrors )
  {
    final IStatusCollector stati = new StatusCollector( KalypsoCorePlugin.getID() );

    try
    {
      parse( source, timeZone, continueWithErrors, stati );
    }
    catch( final Exception ex )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), ex.getMessage() );
      stati.add( status );
    }

    final MetadataList metadata = new MetadataList();
    metadata.put( IMetadataConstants.MD_ORIGIN, source.getAbsolutePath() );

    final ITupleModel model = createTuppelModel( valueType );
    setObservation( new SimpleObservation( source.getAbsolutePath(), source.getName(), metadata, model ) );

    return StatusUtilities.createStatus( stati, "Observation Import" );
  }

  protected abstract void parse( File source, TimeZone timeZone, boolean continueWithErrors, IStatusCollector stati ) throws Exception;

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