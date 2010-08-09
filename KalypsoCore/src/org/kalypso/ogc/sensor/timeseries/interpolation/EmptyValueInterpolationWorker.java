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
package org.kalypso.ogc.sensor.timeseries.interpolation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;

/**
 * @author Dirk Kuch
 */
public class EmptyValueInterpolationWorker extends AbstractInterpolationWorker implements ICoreRunnableWithProgress
{

  public EmptyValueInterpolationWorker( final IInterpolationFilter filter, final ITupleModel values, final DateRange dateRange )
  {
    super( filter, values, dateRange );
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final List<IStatus> statis = new ArrayList<IStatus>();

    // no values, and fill is not set, so return
    if( isFilled() )
    {
      final IAxis[] axes = getBaseModel().getAxisList();

      final SimpleTupleModel interpolated = new SimpleTupleModel( axes );

      if( getDateRange() != null )
      {
        try
        {
          final Calendar calendar = Calendar.getInstance();
          calendar.setTime( getDateRange().getFrom() );

          final IAxis dateAxis = ObservationUtilities.findAxisByClass( axes, Date.class );
          final IAxis[] valueAxes = ObservationUtilities.findAxesByClasses( axes, new Class[] { Number.class, Boolean.class } );
          final Object[] defaultValues = parseDefaultValues( valueAxes );

          while( calendar.getTime().compareTo( getDateRange().getTo() ) <= 0 )
          {
            fillWithDefault( dateAxis, valueAxes, defaultValues, interpolated, calendar );
          }
        }
        catch( final SensorException e )
        {
          final String msg = String.format( "Interpolating values failed" );
          statis.add( StatusUtilities.createErrorStatus( msg, e ) );
        }
      }

      setInterpolatedModel( interpolated );
    }
    else
    {
      setInterpolatedModel( getBaseModel() );
    }

    return StatusUtilities.createStatus( statis, "Interpolating values" );
  }

}
