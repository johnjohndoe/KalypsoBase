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
package org.kalypso.ogc.sensor.timeseries;

import java.util.Date;

import org.eclipse.core.runtime.Assert;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.status.KalypsoStati;

/**
 * Allows to add multiple tupple models (timeseries) with linear factors.
 * 
 * @author Gernot Belger
 * @author Holger Albert
 */
public class TuppleModelsLinearAdd
{
  private final String m_sourceValueType;

  private final IAxis m_targetDateAxis;

  private final IAxis m_targetValueAxis;

  private final IAxis m_targetStatusAxis;

  public TuppleModelsLinearAdd( final String sourceValueType, final IAxis targetDateAxis, final IAxis targetValueAxis, final IAxis targetStatusAxis )
  {
    Assert.isLegal( targetDateAxis.getDataClass() == Date.class );
    Assert.isLegal( targetValueAxis.getDataClass() == Double.class );
    Assert.isLegal( targetStatusAxis.getDataClass() == Integer.class );

    m_sourceValueType = sourceValueType;
    m_targetDateAxis = targetDateAxis;
    m_targetValueAxis = targetValueAxis;
    m_targetStatusAxis = targetStatusAxis;
  }

  /**
   * This function adds weights to the tupple models.
   * 
   * @param tuppleModels
   *          The tupple models.
   * @param weights
   *          The weights.
   * @return A new combined tupple model. ATTENTION: Make sure your axes of the observation are in the same order as the
   *         axes of this tupple model. DATE, VALUE and STATUS.
   */
  public ITupleModel addWeighted( final ITupleModel[] tuppleModels, final double[] weights ) throws SensorException
  {
    Assert.isLegal( tuppleModels.length > 0 );
    Assert.isLegal( tuppleModels.length == weights.length );

    final ITupleModel firstTuppleModel = tuppleModels[0];

    /* Sanity check and gather value axes */
    final IAxis[] valueAxes = new IAxis[tuppleModels.length];
    for( int i = 0; i < tuppleModels.length; i++ )
    {
      final ITupleModel values = tuppleModels[i];
      if( values.size() != firstTuppleModel.size() )
        throw new SensorException( "The observations in the list must have a equal number of elements ..." );

      final IAxis[] axisList = values.getAxes();
      valueAxes[i] = ObservationUtilities.findAxisByType( axisList, m_sourceValueType );
    }

    /* Create a new observation using the given targetAxes. */
    /* The other observations should have the same type of axes. */
    final IAxis[] targetAxes = new IAxis[] { m_targetDateAxis, m_targetValueAxis, m_targetStatusAxis };
    final SimpleTupleModel combinedTuppleModel = new SimpleTupleModel( targetAxes );
    final int combinedDatePosition = 0;
    final int combinedValuePosition = 1;
    final int combinedStatusPosition = 2;

    for( int i = 0; i < firstTuppleModel.size(); i++ )
    {
      double sum = 0.0;
      for( int j = 0; j < tuppleModels.length; j++ )
      {
        /* Get the weight. */
        final double weight = weights[j];
        if( weight == 0.0 )
          continue;

        /* Get the values of the observation. */
        final ITupleModel tuppleModel = tuppleModels[j];

        /* Get the rainfall axis. */
        /* The date will be taken later from the first observation. */
        /* The status bit will be set to ok later. */
        final IAxis valueAxis = valueAxes[j];

        /* Multiply the values of the current observation. */
        final Double value = (Double) tuppleModel.get( i, valueAxis );

        /* Weight the value. */
        final double weightedValue = value.doubleValue() * weight;

        /* Add to the sum. */
        sum = sum + weightedValue;
      }

      /* The list for the values. */
      final Object[] values = new Object[3];

      /* Add the first date to the new observation. */
      final Date firstDate = (Date) firstTuppleModel.get( i, m_targetDateAxis );
      values[combinedDatePosition] = new Date( firstDate.getTime() );

      /* Add the summarised values to the new observation. */
      values[combinedValuePosition] = new Double( sum );

      /* Add the status bit to the new observation. */
      values[combinedStatusPosition] = new Integer( KalypsoStati.BIT_OK );

      /* Add a new row. */
      combinedTuppleModel.addTuple( values );
    }

    return combinedTuppleModel;
  }
}
