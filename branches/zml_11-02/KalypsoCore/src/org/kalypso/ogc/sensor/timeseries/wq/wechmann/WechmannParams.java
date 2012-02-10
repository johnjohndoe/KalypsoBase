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

/**
 * Wechmann Parameters
 * 
 * @author schlienger
 */
public final class WechmannParams
{
  /** Konstante W1 */
  private final double m_w1;

  /** Konstante LNK1 */
  private final double m_lnk1;

  /** Konstante K2 */
  private final double m_k2;

  /**
   * obere Wasserstandsgrenze in cm. When no WGR value is defined, this class supposes Double.MAX_VALUE is used
   */
  private final double m_wgr;

  /**
   * this is the Q computed from WGR using the Wechmann function. This Q is also stored as a member of this class
   * because it is used when convert Q to W.
   * <p>
   * this is a computed value, it is not serialized.
   */
  private double m_q4wgr;

  private double m_q4w1;

  /**
   * Creates the parameters with a WGR value of Double.MAX_VALUE. Use this constructor when the WGR value is not
   * defined, thus the parameters are valid for all possible W values.
   */
  public WechmannParams( final double w1, final double lnk1, final double k2 )
  {
    this( w1, lnk1, k2, Double.MAX_VALUE );
  }

  /**
   * Creates the parameters. They will be used until the Waterlevel reaches WGR.
   * 
   * @param W1
   *          Konstante
   * @param LNK1
   *          Konstante
   * @param K2
   *          Konstante
   * @param WGR
   *          obere Wasserstandsgrenze in cm
   */
  public WechmannParams( final double w1, final double lnk1, final double k2, final double wgr )
  {
    m_w1 = w1;
    m_lnk1 = lnk1;
    m_k2 = k2;
    m_wgr = wgr;

    try
    {
      m_q4wgr = WechmannFunction.computeQ( lnk1, wgr, w1, k2 );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      m_q4wgr = Double.MAX_VALUE;
    }

    try
    {
      m_q4w1 = WechmannFunction.computeQ( lnk1, w1 + 0.01, w1, k2 );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      m_q4w1 = 0.0;
    }

  }

  /**
   * @return K2
   */
  public double getK2( )
  {
    return m_k2;
  }

  /**
   * @return LN(K1)
   */
  public double getLNK1( )
  {
    return m_lnk1;
  }

  /**
   * @return W1
   */
  public double getW1( )
  {
    return m_w1;
  }

  /**
   * @return WGR
   */
  public double getWGR( )
  {
    return m_wgr;
  }

  /**
   * @return true if WGR was defined once object was constructed
   */
  public boolean hasWGR( )
  {
    return Double.compare( m_wgr, Double.MAX_VALUE ) != 0;
  }

  /**
   * @return the corresponding Q-value to the WGR
   */
  public double getQ4WGR( )
  {
    return m_q4wgr;
  }

  /**
   * @return the corresponding Q-value to the w1
   */
  public double getQ4W1( )
  {
    return m_q4w1;
  }
}