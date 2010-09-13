/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.timeseries.interpolation;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.timeseries.interpolation.worker.AbstractInterpolationWorker;
import org.kalypso.ogc.sensor.timeseries.interpolation.worker.IInterpolationFilter;

/**
 * InterpolationFilter. This is a simple yet tricky interpolation filter. It steps through the time and eventually
 * interpolates the values at t, using the values at t-1 and t+1.
 * <p>
 * This filter can also deal with Kalypso Status Axes. In that case it does not perform an interpolation, but uses the
 * policy defined in KalypsoStatusUtils. When no status is available, it uses the default value for the status provided
 * in the constructor.
 * 
 * @author schlienger
 */
public class InterpolationFilter extends AbstractObservationFilter implements IInterpolationFilter
{
  private final int m_calField;

  private final int m_amount;

  private final boolean m_fill;

  private final String m_defValue;

  private final Integer m_defaultStatus;

  private final boolean m_fillLastWithValid;

  /**
   * Constructor.
   * 
   * @param calendarField
   *          which field of the date will be used for stepping through the time series
   * @param amount
   *          amount of time for the step
   * @param forceFill
   *          when true, fills the model with defaultValue when no base value
   * @param defaultValue
   *          default value to use when filling absent values
   * @param defaultStatus
   *          value of the default status when base status is absent or when status-interpolation cannot be proceeded
   * @param fillLastWithValid
   *          when true, the last tuples of the model get the last valid tuple from the original, not the default one
   */
  public InterpolationFilter( final int calendarField, final int amount, final boolean forceFill, final String defaultValue, final int defaultStatus, final boolean fillLastWithValid )
  {
    m_calField = calendarField;
    m_amount = amount;
    m_fill = forceFill;
    m_fillLastWithValid = fillLastWithValid;
    m_defaultStatus = new Integer( defaultStatus );
    m_defValue = defaultValue;
  }

  public InterpolationFilter( final int calendarField, final int amount, final boolean forceFill, final String defaultValue, final int defaultStatus )
  {
    this( calendarField, amount, forceFill, defaultValue, defaultStatus, false );
  }

  @Override
  public IObservation getObservation( )
  {
    return super.getObservation();
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getValues(org.kalypso.ogc.sensor.request.IRequest)
   */
  @Override
  public ITupleModel getValues( final IRequest request ) throws SensorException
  {
    try
    {
      final AbstractInterpolationWorker worker = AbstractInterpolationWorker.createWorker( this, request );
      final IStatus status = worker.execute( new NullProgressMonitor() );
      if( !status.isOK() )
        throw new SensorException( status.getMessage(), status.getException() );

      return worker.getInterpolatedModel();
    }
    catch( SensorException se )
    {
      throw se;
    }
    catch( final Exception ex )
    {
      throw new SensorException( "Creating interpolated model failed.", ex );
    }
  }

  /**
   * @see org.kalypso.ogc.sensor.timeseries.interpolation.IInterpolationFilter#isFilled()
   */
  @Override
  public boolean isFilled( )
  {
    return m_fill;
  }

  /**
   * @see org.kalypso.ogc.sensor.timeseries.interpolation.IInterpolationFilter#getDefaultStatus()
   */
  @Override
  public Integer getDefaultStatus( )
  {
    return m_defaultStatus;
  }

  /**
   * @see org.kalypso.ogc.sensor.timeseries.interpolation.IInterpolationFilter#getDefaultValue()
   */
  @Override
  public String getDefaultValue( )
  {
    return m_defValue;
  }

  /**
   * @see org.kalypso.ogc.sensor.timeseries.interpolation.IInterpolationFilter#getCalendarField()
   */
  @Override
  public int getCalendarField( )
  {
    return m_calField;
  }

  /**
   * @see org.kalypso.ogc.sensor.timeseries.interpolation.IInterpolationFilter#getCalendarAmnount()
   */
  @Override
  public int getCalendarAmnount( )
  {
    return m_amount;
  }

  /**
   * @see org.kalypso.ogc.sensor.timeseries.interpolation.IInterpolationFilter#isLastFilledWithValid()
   */
  @Override
  public boolean isLastFilledWithValid( )
  {
    return m_fillLastWithValid;
  }

  /**
   * @see org.kalypso.ogc.sensor.timeseries.interpolation.worker.IInterpolationFilter#getMetaData()
   */
  @Override
  public MetadataList getMetaDataList( )
  {
    return super.getMetadataList();
  }

}