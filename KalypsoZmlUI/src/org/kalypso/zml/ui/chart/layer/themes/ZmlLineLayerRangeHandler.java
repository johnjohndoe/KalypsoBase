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
package org.kalypso.zml.ui.chart.layer.themes;

import java.util.Date;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IAxisRange;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.DataOperatorHelper;

/**
 * @author Dirk Kuch
 */
public class ZmlLineLayerRangeHandler
{
  private final ZmlLineLayer m_layer;

  private final IDataOperator<Date> m_dateDataOperator = new DataOperatorHelper().getDataOperator( Date.class );

  private final IDataOperator<Number> m_numberDataOperator = new DataOperatorHelper().getDataOperator( Number.class );

  public ZmlLineLayerRangeHandler( final ZmlLineLayer layer )
  {
    m_layer = layer;
  }

  public IDataRange<Number> getDomainRange( )
  {
    try
    {
      final ITupleModel model = m_layer.getDataHandler().getModel();
      if( model == null )
        return null;

      final org.kalypso.ogc.sensor.IAxis dateAxis = AxisUtils.findDateAxis( model.getAxes() );
      final IAxisRange range = model.getRange( dateAxis );
      if( range == null )
        return null;

      final Date min = (Date) range.getLower();
      final Date max = (Date) range.getUpper();

      return new DataRange<Number>( getDateDataOperator().logicalToNumeric( min ), getDateDataOperator().logicalToNumeric( max ) );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );

      return null;
    }
  }

  public IDataOperator<Date> getDateDataOperator( )
  {
    return m_dateDataOperator;
  }

  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    try
    {
      final ITupleModel model = m_layer.getDataHandler().getModel();
      if( model == null )
        return null;

      final IAxis axis = m_layer.getDataHandler().getValueAxis();
      if( axis == null )
        return null;

      if( Objects.isNull( AxisUtils.findAxis( model.getAxes(), axis.getType() ) ) )
        return null;

      if( domainIntervall == null )
      {
        final IAxisRange range = model.getRange( axis );
        if( range == null )
          return null;

        final IDataRange<Number> numRange = new DataRange<Number>( getNumberDataOperator().logicalToNumeric( (Number) range.getLower() ), getNumberDataOperator().logicalToNumeric( (Number) range.getUpper() ) );

        return numRange;
      }
      else
      {
        final org.kalypso.ogc.sensor.IAxis dateAxis = AxisUtils.findDateAxis( model.getAxes() );

        Number minValue = null;
        Number maxValue = null;
        for( int i = 0; i < model.size(); i++ )
        {

          final Object domainValue = model.get( i, dateAxis );

          if( domainValue == null )
            continue;
          if( minValue == null && ((Date) domainValue).getTime() > domainIntervall.getMin().longValue() )
          {
            minValue = (Number) model.get( i - 1, axis );
          }
          if( maxValue == null && ((Date) domainValue).getTime() > domainIntervall.getMax().longValue() )
          {
            maxValue = (Number) model.get( i, axis );
          }
        }

        return new DataRange<Number>( getNumberDataOperator().logicalToNumeric( minValue ), getNumberDataOperator().logicalToNumeric( maxValue ) );
      }
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );

      return null;
    }
  }

  public IDataOperator<Number> getNumberDataOperator( )
  {
    return m_numberDataOperator;
  }

}
