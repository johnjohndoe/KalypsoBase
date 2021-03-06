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
package org.kalypso.ogc.sensor.timeseries.interpolation.worker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;

/**
 * "Interpolates" / fills an empty observation
 *
 * @author Dirk Kuch
 */
public class EmptyValueInterpolationWorker extends AbstractInterpolationWorker
{

  public EmptyValueInterpolationWorker( final IInterpolationFilter filter, final ITupleModel values, final DateRange dateRange )
  {
    super( filter, values, dateRange );
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final List<IStatus> statis = new ArrayList<>();

    // no values, and fill is not set, so return
    if( isFilled() )
    {

      if( getDateRange() != null )
      {
        try
        {
          final Calendar calendar = Calendar.getInstance( KalypsoCorePlugin.getDefault().getTimeZone() );
          calendar.setTime( getDateRange().getFrom() );

          final IAxis dateAxis = getDateAxis();
          final IAxis[] valueAxes = getValueAxes();

          final LocalCalculationStack stack = new LocalCalculationStack();
          for( final IAxis valueAxis : valueAxes )
          {
            final LocalCalculationStackValue value = new LocalCalculationStackValue( valueAxis );
            stack.add( value );
          }

          while( calendar.getTime().compareTo( getDateRange().getTo() ) <= 0 )
          {
            addDefaultTupple( dateAxis, stack, calendar );
          }
        }
        catch( final SensorException e )
        {
          final String msg = String.format( Messages.getString( "EmptyValueInterpolationWorker_0" ) ); //$NON-NLS-1$
          statis.add( StatusUtilities.createErrorStatus( msg, e ) );
        }
      }
    }

    return StatusUtilities.createStatus( statis, Messages.getString( "EmptyValueInterpolationWorker_1" ) ); //$NON-NLS-1$
  }
}