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
import java.util.List;
import java.util.TimeZone;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceProxyObservation;
import org.kalypso.ogc.sensor.zml.ZmlFactory;

/**
 * @author Dejan Antanaskovic, <a href="mailto:dejan.antanaskovic@tuhh.de">dejan.antanaskovic@tuhh.de</a>
 */
public class NativeObservationZmlAdapter extends AbstractObservationImporter
{
  @Override
  public IStatus doImport( final File source, final TimeZone timeZone, final String valueType, final boolean continueWithErrors )
  {
    try
    {
      final IObservation observation = ZmlFactory.parseXML( source.toURI().toURL() );

      /* Enforce parameter type */
      final IObservation obsWithRightType = fixParameterType( observation, valueType );

      final IAxis valueAxis = AxisUtils.findAxis( obsWithRightType.getAxes(), valueType );
      final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( obsWithRightType.getAxes(), valueAxis );

      if( Objects.isNull( dataSourceAxis ) )
      {
        final String dataSource = source.getAbsolutePath();
        setObservation( new DataSourceProxyObservation( obsWithRightType, dataSource, dataSource, KalypsoStati.BIT_OK ) );
      }
      else
        setObservation( obsWithRightType );

      return new Status( IStatus.OK, KalypsoCorePlugin.getID(), Messages.getString( "NativeObservationZmlAdapter_0" ) ); //$NON-NLS-1$
    }
    catch( final Exception e )
    {
      return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), e.getMessage() );
    }
  }

  private IObservation fixParameterType( final IObservation observation, final String valueType ) throws SensorException
  {
    final IAxis valueAxis = AxisUtils.findValueAxis( observation.getAxes() );
    if( valueAxis == null )
    {
      // TODO: can this ever happen?
      return observation;
    }

    if( valueAxis.getType().equals( valueType ) )
      return observation;

    return ObservationUtilities.forceParameterType( observation, valueType );
  }

  @Override
  protected List<NativeObservationDataSet> parse( final File source, final TimeZone timeZone, final boolean continueWithErrors, final IStatusCollector stati )
  {
    throw new UnsupportedOperationException();
  }
}