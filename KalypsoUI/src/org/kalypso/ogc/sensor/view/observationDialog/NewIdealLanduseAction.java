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
package org.kalypso.ogc.sensor.view.observationDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.java.util.ValueIterator;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.TimeserieUtils;
import org.kalypso.ogc.sensor.view.ObservationViewerDialog;

/**
 * @author Gernot Belger
 */
public class NewIdealLanduseAction extends AbstractObservationAction
{
  public NewIdealLanduseAction( final ObservationViewerDialog dialog )
  {
    super( dialog );
  }

  /**
   * @see org.kalypso.ogc.sensor.view.observationDialog.AbstractObservationAction#getLabel()
   */
  @Override
  protected String getLabel( )
  {
    return Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewerDialog.6" ); //$NON-NLS-1$ 
  }

  /**
   * @see org.kalypso.ogc.sensor.view.observationDialog.AbstractObservationAction#getTooltip()
   */
  @Override
  protected String getTooltip( )
  {
    return Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewerDialog.7" ); //$NON-NLS-1$ 
  }

  /**
   * @see org.kalypso.ogc.sensor.view.observationDialog.AbstractObservationAction#run()
   */
  @Override
  protected IStatus run( )
  {
    final String[] axisTypes = getDialog().getAxisTypes();
    final IAxis[] axis = TimeserieUtils.createDefaultAxes( axisTypes, true );

    final String name = Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewerDialog.5" ); //$NON-NLS-1$
    final Calendar startDate = Calendar.getInstance();
    startDate.set( 2000, 11, 15 );
    final Calendar idealMonth = Calendar.getInstance();
    idealMonth.setTimeInMillis( 30 * 24 * 60 * 60 * 1000 );
    final Object intervall = new Date( idealMonth.getTimeInMillis() );
    final Object min = new Date( startDate.getTimeInMillis() );
    final int months = 12;

    final Object[][] values = new Object[months][axis.length];
    final Iterator< ? > iterator = new ValueIterator( min, intervall, months );
    for( int row = 0; row < months; row++ )
    {
      values[row][0] = iterator.next();
      for( int ax = 1; ax < axis.length; ax++ )
        values[row][ax] = new Double( 0 );
    }
    final ITupleModel model = new SimpleTupleModel( axis, values );
    getDialog().setInput( new SimpleObservation( null, name, new MetadataList(), model ) );

    return Status.OK_STATUS;
  }

}
