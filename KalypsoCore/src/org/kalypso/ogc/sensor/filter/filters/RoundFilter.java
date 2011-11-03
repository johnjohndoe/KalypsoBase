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
package org.kalypso.ogc.sensor.filter.filters;

import java.math.BigDecimal;
import java.net.URL;

import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.proxy.AutoProxyFactory;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.zml.filters.RoundFilterType;

/**
 * @author Holger Albert
 * @author Gernot Belger
 */
public class RoundFilter extends AbstractObservationFilter
{
  private final int m_factor;

  private final int m_mode;

  private final String m_type;

  private IObservation m_baseobservation = null;

  public RoundFilter( final RoundFilterType filter )
  {
    m_factor = filter.getFactor();
    m_mode = toBigDecimalRoundingConstant( filter.getMode() );
    m_type = filter.getAxisType();
    m_baseobservation = null;
  }

  public RoundFilter( final int factor, final String mode, final String type )
  {
    m_factor = factor;
    m_mode = toBigDecimalRoundingConstant( mode );
    m_type = type;
    m_baseobservation = null;
  }

  private static int toBigDecimalRoundingConstant( final String mode )
  {
    if( "ROUND_CEILING".equals( mode ) ) //$NON-NLS-1$
      return BigDecimal.ROUND_CEILING;
    else if( "ROUND_DOWN".equals( mode ) ) //$NON-NLS-1$
      return BigDecimal.ROUND_DOWN;
    else if( "ROUND_FLOOR".equals( mode ) ) //$NON-NLS-1$
      return BigDecimal.ROUND_FLOOR;
    else if( "ROUND_HALF_DOWN".equals( mode ) ) //$NON-NLS-1$
      return BigDecimal.ROUND_HALF_DOWN;
    else if( "ROUND_HALF_EVEN".equals( mode ) ) //$NON-NLS-1$
      return BigDecimal.ROUND_HALF_EVEN;
    else if( "ROUND_HALF_UP".equals( mode ) ) //$NON-NLS-1$
      return BigDecimal.ROUND_HALF_UP;
    else if( "ROUND_UNNECESSARY".equals( mode ) ) //$NON-NLS-1$
      return BigDecimal.ROUND_UNNECESSARY;
    else if( "ROUND_UP".equals( mode ) ) //$NON-NLS-1$
      return BigDecimal.ROUND_UP;

    throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.RoundFilter.0" ) + mode ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter#initFilter(java.lang.Object,
   *      org.kalypso.ogc.sensor.IObservation, java.net.URL)
   */
  @Override
  public void initFilter( final Object dummy, final IObservation baseObs, final URL context ) throws SensorException
  {
    m_baseobservation = baseObs;

    super.initFilter( dummy, baseObs, context );
  }

  /**
   * @see org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter#appendSettings(org.kalypso.ogc.sensor.metadata.MetadataList)
   */
  @Override
  protected void appendSettings( final MetadataList metadata )
  {
  }

  /**
   * @see org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter#getValues(org.kalypso.ogc.sensor.request.IRequest)
   */
  @Override
  public ITupleModel getValues( final IRequest request ) throws SensorException
  {
    /* Create a proxied observation. */
    final IObservation proxiedObservation = AutoProxyFactory.getInstance().proxyObservation( m_baseobservation );
    final ITupleModel proxiedValues = proxiedObservation.getValues( request );

    /* Get all non-virtual Double-Axises. */
    final IAxis proxiedAxis = ObservationUtilities.findAxisByTypeNoEx( proxiedValues.getAxes(), m_type );
    if( proxiedAxis == null )
      throw new SensorException( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.RoundFilter.1" ) + m_type ); //$NON-NLS-1$

    /* Apply the filter. */
    final int valueCount = proxiedValues.size();
    for( int j = 0; j < valueCount; j++ )
    {
      final Double proxiedValue = (Double) proxiedValues.get( j, proxiedAxis );
      if( proxiedValue != null && !proxiedValue.isNaN() )
      {
        final double factoredValue = proxiedValue.doubleValue() / m_factor;
        final BigDecimal decimal = new BigDecimal( factoredValue );
        final BigDecimal roundedDecimal = decimal.setScale( 0, m_mode );
        final double newValue = roundedDecimal.doubleValue() * m_factor;
        proxiedValues.set( j, proxiedAxis, new Double( newValue ) );
      }
    }

    final ITupleModel orgValues = m_baseobservation.getValues( request );
    final SimpleTupleModel simpleTuppleModel = new SimpleTupleModel( orgValues );
    return simpleTuppleModel;
  }

  /**
   * @see org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter#setValues(org.kalypso.ogc.sensor.ITupleModel)
   */
  @Override
  public void setValues( final ITupleModel values )
  {
    throw new UnsupportedOperationException( getClass().getName() + Messages.getString( "org.kalypso.ogc.sensor.filter.filters.RoundFilter.2" ) ); //$NON-NLS-1$
  }
}