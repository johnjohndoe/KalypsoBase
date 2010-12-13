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
package org.kalypso.ogc.sensor.timeseries.wq;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.AbstractTupleModel;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;

/**
 * The WQTuppleModel computes W, Q, V, etc. on the fly, depending on the underlying axis type. It also manages the
 * status information for the generated axis, depending on the success of the computation.
 * 
 * @author schlienger
 */
public class WQTuppleModel extends AbstractTupleModel
{
  private static final Double ZERO = new Double( 0 );

  private final ITupleModel m_model;

  private final IAxis m_dateAxis;

  /** source axis from the underlying model */
  private final IAxis m_srcAxis;

  /** source-status axis from the underlying model [Important: this one is optional and can be null] */
  private final IAxis m_srcStatusAxis;

  /** generated axis */
  private final IAxis m_destAxis;

  /** generated status axis */
  private final IAxis m_destStatusAxis;

  /** backs the generated values for the dest axis */
  private final Map<Integer, Number> m_values = new HashMap<Integer, Number>();

  /** backs the stati for the dest status axis */
  private final Map<Integer, Number> m_stati = new HashMap<Integer, Number>();

  private final IWQConverter m_converter;

  private final int m_destAxisPos;

  private final int m_destStatusAxisPos;

  /**
   * Creates a <code>WQTuppleModel</code> that can generate either W or Q on the fly. It needs an existing model from
   * whitch the values of the given type are fetched.
   * <p>
   * If it bases on a TimeserieConstants.TYPE_RUNOFF it can generate TYPE_WATERLEVEL values and vice versa.
   * 
   * @param model
   *          base model delivering values of the given type
   * @param axes
   *          axes of this WQ-model, usually the same as model plus destAxis
   * @param srcAxis
   *          source axis from which values are read
   * @param srcStatusAxis
   *          source status axis [optional, can be null]
   * @param destAxis
   *          destination axis for which values are computed
   * @param destStatusAxis
   *          status axis for the destAxis (destination axis)
   * @param destStatusAxisPos
   *          position of the axis in the array
   * @param destAxisPos
   *          position of the axis in the array
   */
  public WQTuppleModel( final ITupleModel model, final IAxis[] axes, final IAxis dateAxis, final IAxis srcAxis, final IAxis srcStatusAxis, final IAxis destAxis, final IAxis destStatusAxis, final IWQConverter converter, final int destAxisPos, final int destStatusAxisPos )
  {
    super( axes );

    m_destAxisPos = destAxisPos;
    m_destStatusAxisPos = destStatusAxisPos;
    mapAxisToPos( destAxis, destAxisPos );
    mapAxisToPos( destStatusAxis, destStatusAxisPos );

    m_model = model;
    m_converter = converter;

    m_dateAxis = dateAxis;
    m_srcAxis = srcAxis;
    m_srcStatusAxis = srcStatusAxis;
    m_destAxis = destAxis;
    m_destStatusAxis = destStatusAxis;
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#getCount()
   */
  @Override
  public int size( ) throws SensorException
  {
    return m_model.size();
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#getElement(int, org.kalypso.ogc.sensor.IAxis)
   */
  @Override
  public Object get( final int index, final IAxis axis ) throws SensorException
  {
    final boolean bDestAxis = axis.equals( m_destAxis );

    if( bDestAxis || KalypsoStatusUtils.equals( axis, m_destStatusAxis ) )
    {
      final Integer objIndex = Integer.valueOf( index );
      if( !m_values.containsKey( objIndex ) )
      {
        final Number[] res = read( index );
        m_values.put( objIndex, res[0] );
        m_stati.put( objIndex, res[1] );
      }

      if( bDestAxis )
        return m_values.get( objIndex );

      return m_stati.get( objIndex );
    }

    return m_model.get( index, axis );
  }

  private Number[] read( final int index ) throws SensorException
  {
    final Number srcValue = (Number) m_model.get( index, m_srcAxis );
    final Number srcStatus =  m_srcStatusAxis == null ? KalypsoStati.BIT_OK : (Number) m_model.get( index, m_srcStatusAxis );
    if( srcValue == null || srcStatus == null )
      return new Number[] { null, null };

    final Date d = (Date) m_model.get( index, m_dateAxis );
    try
    {
      final String type = m_destAxis.getType();
      if( type.equals( m_converter.getFromType() ) )
      {
        final double q = srcValue.doubleValue();
        return new Number[] { m_converter.computeW( d, q ), KalypsoStati.STATUS_DERIVATED | srcStatus.intValue() };
      }
      else if( type.equals( m_converter.getToType() ) )
      {
        final double w = srcValue.doubleValue();
        final double q = m_converter.computeQ( d, w );

        return new Number[] { q, KalypsoStati.STATUS_DERIVATED | srcStatus.intValue() };
      }

      return new Number[] { ZERO, KalypsoStati.STATUS_DERIVATION_ERROR };
    }
    catch( final WQException e )
    {
      return new Number[] { ZERO, KalypsoStati.STATUS_DERIVATION_ERROR };
    }
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#setElement(int, java.lang.Object, org.kalypso.ogc.sensor.IAxis)
   */
  @Override
  public void set( final int index, final IAxis axis, final Object element ) throws SensorException
  {
    final Integer objIndex = new Integer( index );
    m_values.remove( objIndex );
    m_stati.remove( objIndex );
    if( axis.equals( m_destAxis ) )
    {
      final Date d = (Date) m_model.get( index, m_dateAxis );

      Double value = null;
      Integer status = null;
      try
      {
        final String type = axis.getType();
        if( m_converter == null )
        {
          value = ZERO;
          status = KalypsoStati.STATUS_CHECK;
        }
        else if( type.equals( m_converter.getFromType() ) )
        {
          final double w = ((Number) element).doubleValue();
          value = new Double( m_converter.computeQ( d, w ) );
          status = KalypsoStati.STATUS_USERMOD;
        }
        else if( type.equals( m_converter.getToType() ) )
        {
          final double q = ((Number) element).doubleValue();
          value = new Double( m_converter.computeW( d, q ) );
          status = KalypsoStati.STATUS_USERMOD;
        }
        else
        {
          value = ZERO;
          status = KalypsoStati.STATUS_CHECK;
        }

        // if( type.equals( TimeserieConstants.TYPE_WATERLEVEL ) )
        // {
        // final double w = ( (Number)element ).doubleValue();
        // value = new Double( m_converter.computeQ( d, w ) );
        // }
        // else
        // {
        // final double q = ( (Number)element ).doubleValue();
        // value = new Double( m_converter.computeW( d, q ) );
        // }
        //
        // status = KalypsoStati.STATUS_USERMOD;
      }
      catch( final WQException e )
      {
        // Logger.getLogger( getClass().getName() ).warning( "WQ-Konvertierungsproblem: " + e.getLocalizedMessage() );

        value = ZERO;
        status = KalypsoStati.STATUS_CHECK;
      }

      m_model.set( index, m_srcAxis, value );
      if( m_srcStatusAxis != null )
        m_model.set( index, m_srcStatusAxis, status );
    }
    // TODO: ich glaube Gernot hatte geschrieben:
    // "besser wäre eigentlich equals, aber das klappt bei status achsen nicht" und hatte == statt equals() benutzt. Ich
    // bin der Meinung es sollte doch mit equals() klappen.
    else if( axis.equals( m_destStatusAxis ) )
    {
      // einfach ignorieren

      // Alte Kommentare:
      // TODO: Marc: dieser Fall hat noch gefehlt. Bisher gabs einfach ne exception, wenn
      // man versucht, die destStatusAxis zu beschreiben
      // Was soll man tun?
      // Dieser Fehler tauchte im tranPoLinFilter auf
    }
    else
    {
      m_model.set( index, axis, element );
    }
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#indexOf(java.lang.Object, org.kalypso.ogc.sensor.IAxis)
   */
  @Override
  public int indexOf( final Object element, final IAxis axis ) throws SensorException
  {
    if( axis.equals( m_destAxis ) )
      return -1; // indexOf only makes sense for key axes

    return m_model.indexOf( element, axis );
  }

  public IAxis getDestStatusAxis( )
  {
    return m_destStatusAxis;
  }

  public IAxis getDestAxis( )
  {
    return m_destAxis;
  }

  public IAxis getSrcAxis( )
  {
    return m_srcAxis;
  }

  public IAxis getSrcStatusAxis( )
  {
    return m_srcStatusAxis;
  }

  public IAxis getDateAxis( )
  {
    return m_dateAxis;
  }

  /**
   * Creates a TuppleModel from a potential WQTuppleModel for storing the values back in the original observation.
   * 
   * @throws SensorException
   */
  public static ITupleModel reverse( final ITupleModel values, final IAxis[] axes ) throws SensorException
  {
    final SimpleTupleModel stm = new SimpleTupleModel( axes );

    for( int i = 0; i < values.size(); i++ )
    {
      final Object[] tupple = new Object[axes.length];

      // straighforward: simply take the values for the axes of the original
      // observation, not the generated W/Q
      for( int j = 0; j < axes.length; j++ )
        tupple[stm.getPosition( axes[j] )] = values.get( i, axes[j] );

      stm.addTuple( tupple );
    }

    return stm;
  }

  public IWQConverter getConverter( )
  {
    return m_converter;
  }

  /**
   * @return the base model
   */
  public ITupleModel getBaseModel( )
  {
    return m_model;
  }

  public int getDestAxisPos( )
  {
    return m_destAxisPos;
  }

  public int getDestStatusAxisPos( )
  {
    return m_destStatusAxisPos;
  }
}