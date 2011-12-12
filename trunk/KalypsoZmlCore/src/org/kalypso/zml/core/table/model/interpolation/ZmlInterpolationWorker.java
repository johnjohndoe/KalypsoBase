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
package org.kalypso.zml.core.table.model.interpolation;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.table.model.IZmlModelColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlInterpolationWorker implements ICoreRunnableWithProgress
{
  private final ITimeseriesObservation m_observation;

  public ZmlInterpolationWorker( final IObservation observation )
  {
    this( new TimeseriesObservation( observation, AxisUtils.findValueAxis( observation.getAxes() ) ) );
  }

  public ZmlInterpolationWorker( final IZmlModelColumn column )
  {
    this( new TimeseriesObservation( column.getObservation(), column.getValueAxis() ) );
  }

  public ZmlInterpolationWorker( final ITimeseriesObservation observation )
  {
    m_observation = observation;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      final ITupleModel values = m_observation.getValues( null );
      final int size = values.size();

      m_observation.startTransaction();

      final boolean setLastValidValue = ZmlInterpolation.isSetLastValidValue( m_observation.getMetadataList() );
      final Double defaultValue = ZmlInterpolation.getDefaultValue( m_observation.getMetadataList() );

      final FindStuetzstellenVisitor visitor = new FindStuetzstellenVisitor();
      m_observation.accept( visitor, null );

      final Integer[] stuetzstellen = visitor.getStuetzstellen();
      if( ArrayUtils.isEmpty( stuetzstellen ) )
      {
        ZmlInterpolation.fillValue( m_observation, 0, size, defaultValue );
        return Status.OK_STATUS;
      }

      // set all values 0 before first stuetzstelle
      if( stuetzstellen[0] > 0 )
        ZmlInterpolation.fillValue( m_observation, 0, stuetzstellen[0], defaultValue );

      for( int index = 0; index < stuetzstellen.length - 1; index++ )
      {
        final Integer stuetzstelle1 = stuetzstellen[index];
        final Integer stuetzstelle2 = stuetzstellen[index + 1];
        ZmlInterpolation.interpolate( m_observation, stuetzstelle1, stuetzstelle2 );
      }

      // set all values 0 after last stuetzstelle
      final Integer last = stuetzstellen[stuetzstellen.length - 1];
      if( last != size - 1 )
      {
        if( setLastValidValue )
        {
          final Object lastValue = m_observation.getValue( last );
          ZmlInterpolation.fillValue( m_observation, last + 1, size, (Double) lastValue );
        }
        else
          ZmlInterpolation.fillValue( m_observation, last + 1, size, defaultValue );
      }

      m_observation.stopTransaction();

      return Status.OK_STATUS;
    }
    catch( final SensorException e )
    {
      e.printStackTrace();

      throw new CoreException( StatusUtilities.createExceptionalErrorStatus( "(Re)Interpolating values failed", e ) );
    }
  }
}