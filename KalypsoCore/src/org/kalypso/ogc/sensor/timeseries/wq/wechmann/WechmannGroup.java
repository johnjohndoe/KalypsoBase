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
package org.kalypso.ogc.sensor.timeseries.wq.wechmann;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.wq.IWQConverter;
import org.kalypso.ogc.sensor.timeseries.wq.WQException;

/**
 * A List of WechmannSets sorted according to the date of validity of the WechmannSet objects. You can call the iterator
 * to step through the list in the ascending order of date of validity.
 * 
 * @author schlienger
 */
public class WechmannGroup implements IWQConverter
{
  private final SortedMap<Date, WechmannSet> m_map = new TreeMap<Date, WechmannSet>();

  /**
   * @param wsets
   */
  public WechmannGroup( final WechmannSet[] wsets )
  {
    for( final WechmannSet wset : wsets )
      m_map.put( wset.getValidity(), wset );
  }

  /**
   * @return Iterator on the WechmannSet objects
   */
  public Iterator<WechmannSet> iterator( )
  {
    return m_map.values().iterator();
  }

  /**
   * Returns the WechmannSet that is valid for the given date.
   * 
   * @throws WQException
   */
  public WechmannSet getFor( final Date d ) throws WQException
  {
    final Date[] dates = m_map.keySet().toArray( new Date[0] );
    int i = Arrays.binarySearch( dates, d );

    if( i < 0 )
      i = -i - 2;

    if( i < 0 )
      throw new WQException( Messages.getString( "org.kalypso.ogc.sensor.timeseries.wq.wechmann.WechmannGroup.0" ) ); //$NON-NLS-1$

    return m_map.get( dates[i] );
  }

  @Override
  public double computeW( final ITupleModel model, final Integer index, final double q ) throws WQException, SensorException
  {
    final IAxis dateAxis = AxisUtils.findDateAxis( model.getAxes() );
    final Date date = (Date) model.get( index, dateAxis );

    final WechmannParams params = getFor( date ).getForQ( q );
    final double e = getValue( model, index, ITimeseriesConstants.TYPE_WECHMANN_E );

    return WechmannFunction.computeW( params, q ) + e;
  }

  private double getValue( final ITupleModel model, final Integer index, final String type )
  {
    try
    {
      final IAxis axis = AxisUtils.findAxis( model.getAxes(), type );
      if( Objects.isNull( axis ) )
        return 0.0;

      return (Double) model.get( index, axis );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();

      return 0.0;
    }
  }

  /**
   * Returns 0.0, if W is too big for current validity
   */
  @Override
  public double computeQ( final ITupleModel model, final Integer index, final double w ) throws WQException, SensorException
  {
    final IAxis dateAxis = AxisUtils.findDateAxis( model.getAxes() );
    final Date date = (Date) model.get( index, dateAxis );

    final double e = getValue( model, index, ITimeseriesConstants.TYPE_WECHMANN_E );
    final double w2 = w - e;

    final WechmannParams params = getFor( date ).getForW( w2 );
    if( params == null )
      return 0.0;

    return WechmannFunction.computeQ( params, w2 );
  }

  @Override
  public String getFromType( )
  {
    // HARDCODED: Wechman always converts from W to Q?
    return ITimeseriesConstants.TYPE_WATERLEVEL;
  }

  /**
   * @see org.kalypso.ogc.sensor.timeseries.wq.IWQConverter#getToType()
   */
  @Override
  public String getToType( )
  {
    // HARDCODED: Wechman always converts from W to Q?
    return ITimeseriesConstants.TYPE_RUNOFF;
  }
}