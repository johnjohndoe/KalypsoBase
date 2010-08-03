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
package org.kalypso.ogc.sensor.timeseries.forecast;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.AbstractTupleModel;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;

/**
 * A multiple tuple model which consists of different combined sub models <br>
 * <br>
 * TODO: die Reihenfolge der models[] sollte von auﬂerhalb der Klasse entschieden werden. Somit kann der "client"
 * entscheiden ob:
 * <ul>
 * <li>die models[] sortiert sind
 * <li>die models[] eine "andere" Reihenfolge haben
 * </ul>
 * <p>
 * 
 * @author schlienger
 */
public class MultipleTupleModel extends AbstractTupleModel
{
  private final SimpleTupleModel m_model;

  private static final Comparator<ITupleModel> COMPARATOR = new Comparator<ITupleModel>()
  {
    @Override
    public int compare( final ITupleModel t1, final ITupleModel t2 )
    {
      final IAxis dateAxis1 = ObservationUtilities.findAxisByClass( t1.getAxisList(), Date.class );
      final IAxis dateAxis2 = ObservationUtilities.findAxisByClass( t2.getAxisList(), Date.class );

      Date date1 = null;
      Date date2 = null;

      boolean statusDate1 = false;
      boolean statusDate2 = false;
      try
      {
        date1 = (Date) t1.getElement( 0, dateAxis1 );
        if( date1 != null )
          statusDate1 = true;
      }
      catch( final Throwable t )
      {
        // do nothing
      }
      try
      {
        date2 = (Date) t2.getElement( 0, dateAxis2 );
        if( date2 != null )
          statusDate2 = true;
      }
      catch( final Throwable t )
      {
        // do nothing
      }

      if( !statusDate1 && statusDate2 )
        return -1;
      if( !statusDate1 && !statusDate2 )
        return 0;
      if( statusDate1 && !statusDate2 )
        return 1;

      return date1.compareTo( date2 );
    }
  };

  public MultipleTupleModel( final ITupleModel[] models ) throws SensorException
  {
    super( getAxisList( models ) );

    // let them sort, so order does not matter (TODO siehe Klassenkommentar)
    Arrays.sort( models, COMPARATOR );

    final CombineTupleModelsWorker worker = new CombineTupleModelsWorker( models, getAxisList() );
    worker.execute( new NullProgressMonitor() );

    m_model = worker.getCombinedModel();
  }

  private static IAxis[] getAxisList( final ITupleModel[] models )
  {
    for( final ITupleModel model : models )
    {
      // TODO: *grmml* first model defines axes of result model?
      return model.getAxisList();
    }

    return new IAxis[] {};
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#getCount()
   */
  @Override
  public int getCount( )
  {
    return m_model.getCount();
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#getElement(int, org.kalypso.ogc.sensor.IAxis)
   */
  @Override
  public Object getElement( final int index, final IAxis axis ) throws SensorException
  {
    return m_model.getElement( index, axis );
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#setElement(int, java.lang.Object, org.kalypso.ogc.sensor.IAxis)
   */
  @Override
  public void setElement( final int index, final Object element, final IAxis axis ) throws SensorException
  {
    m_model.setElement( index, element, axis );
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#indexOf(java.lang.Object, org.kalypso.ogc.sensor.IAxis)
   */
  @Override
  public int indexOf( final Object element, final IAxis axis ) throws SensorException
  {
    return m_model.indexOf( element, axis );
  }
}