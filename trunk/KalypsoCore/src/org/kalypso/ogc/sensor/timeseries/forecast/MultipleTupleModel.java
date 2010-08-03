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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.AbstractTupleModel;
import org.kalypso.ogc.sensor.impl.DefaultAxis;
import org.kalypso.ogc.sensor.metadata.ITimeserieConstants;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.TimeserieUtils;

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
  private final ITupleModel m_model;

  private static final Comparator<ITupleModel> COMPARATOR = new Comparator<ITupleModel>()
  {
    @Override
    public int compare( final ITupleModel model1, final ITupleModel model2 )
    {
      final Date date1 = getDate( model1 );
      final Date date2 = getDate( model2 );

      if( date1 == null && date2 != null )
        return -1;
      else if( date1 != null && date2 == null )
        return 1;
      else if( date1 == null && date2 == null )
        return 0;

      return date1.compareTo( date2 );
    }

    private Date getDate( final ITupleModel model )
    {
      try
      {
        final IAxis dateAxis = ObservationUtilities.findAxisByClass( model.getAxisList(), Date.class );

        return (Date) model.getElement( 0, dateAxis );
      }
      catch( final Throwable t )
      {
        // do nothing
      }

      return null;
    }
  };

  public MultipleTupleModel( final ITupleModel[] models )
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
      // *grmml* first model defines axes of result model?
      final IAxis[] axes = model.getAxisList();
      if( AxisUtils.findDataSourceAxis( axes ) == null )
      {
        final DefaultAxis dataSourceAxis = new DefaultAxis( TimeserieUtils.getName( ITimeserieConstants.TYPE_DATA_SRC ), ITimeserieConstants.TYPE_DATA_SRC, "", Integer.class, false );
        return (IAxis[]) ArrayUtils.add( axes, dataSourceAxis );
      }

      return axes;
    }

    return new IAxis[] {};
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#getCount()
   */
  @Override
  public int getCount( ) throws SensorException
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