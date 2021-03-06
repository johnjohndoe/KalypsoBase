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

import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.timeseries.wq.WQException;

/**
 * The Wechmann Function. Performs conversion from W to Q and from Q to W according to the Wechmann parameters.
 * <p>
 * The Wechmann Function is defined as follows:
 * 
 * <pre>
 * Q = exp( ln( K1 ) + ln( W - W1 ) * K2 )
 * 
 * having:
 * 
 * Q = computed runoff (in m�/s)
 * W = current waterlevel (in cm at the Gauge) oder auf Deutsch (cm am Pegel)
 * 
 * and
 * 
 * the Wechmann Parameters: ln(K1), W1,  K2
 * </pre>
 * 
 * @author schlienger
 */
public final class WechmannFunction
{
  private WechmannFunction( )
  {
    // not to be instanciated
  }

  /**
   * @see WechmannFunction#computeQ(double, double, double, double)
   */
  public static double computeQ( final WechmannParams wp, final double w ) throws WQException
  {
    return computeQ( wp.getLNK1(), w, wp.getW1(), wp.getK2() );
  }

  /**
   * Computes the Q using the following function:
   * 
   * <pre>
   * Q = exp( ln( K1 ) + ln( W - W1 ) * K2 )
   * </pre>
   * 
   * @throws WQException
   *           if (W - W1) < 0
   */
  public static double computeQ( final double lnk1, final double w, final double w1, final double k2 ) throws WQException
  {
    if( w - w1 < 0 )
    {
      final String msg = String.format( "W < W1, ln( W - W1 ) nicht m�glich. Werte sind: W=%f  W1=%f. QGrenz wird ersetzt durch: %f", w, w1, Double.MAX_VALUE ); //$NON-NLS-1$
      throw new WQException( msg );
    }

    return Math.exp( lnk1 + Math.log( w - w1 ) * k2 );
  }

  /**
   * @see WechmannFunction#computeW(double, double, double, double)
   */
  public static double computeW( final WechmannParams wp, final double q ) throws WQException
  {
    return computeW( wp.getW1(), q, wp.getLNK1(), wp.getK2() );
  }

  /**
   * Computes the W using the following function:
   * 
   * <pre>
   *               ln( Q ) - ln( K1 )
   * W = W1 + exp( ------------------ )
   *                       K2
   * </pre>
   * 
   * @throws WQException
   *           when (K2 = 0) or (Q < 0)
   */
  public static double computeW( final double w1, final double q, final double lnk1, final double k2 ) throws WQException
  {
    if( k2 == 0 )
      throw new WQException( Messages.getString( "org.kalypso.ogc.sensor.timeseries.wq.wechmann.WechmannFunction.1" ) ); //$NON-NLS-1$
    if( q < 0 )
      throw new WQException( Messages.getString( "org.kalypso.ogc.sensor.timeseries.wq.wechmann.WechmannFunction.2" ) ); //$NON-NLS-1$

    return w1 + Math.exp( (Math.log( q ) - lnk1) / k2 );
  }
}