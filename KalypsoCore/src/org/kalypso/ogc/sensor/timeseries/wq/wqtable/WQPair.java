package org.kalypso.ogc.sensor.timeseries.wq.wqtable;

import org.kalypso.core.i18n.Messages;

/**
 * WQPair
 * 
 * @author schlienger
 */
public final class WQPair
{
  private final double m_w;

  private final double m_q;

  public WQPair( final double w, final double q )
  {
    m_w = w;
    m_q = q;
  }

  public double getQ( )
  {
    return m_q;
  }

  public double getW( )
  {
    return m_w;
  }

  @Override
  public String toString( )
  {
    return "W= " + m_w + " Q= " + m_q; //$NON-NLS-1$ //$NON-NLS-2$
  }

  public static void convert2doubles( final WQPair[] pairs, final double[] w, final double[] q )
  {
    for( int i = 0; i < pairs.length; i++ )
    {
      w[i] = pairs[i].getW();
      q[i] = pairs[i].getQ();
    }
  }

  public static WQPair[] convert2pairs( final double[][] table )
  {
    final WQPair[] pairs = new WQPair[table.length];
    for( int i = 0; i < table.length; i++ )
    {
      pairs[i] = new WQPair( table[i][0], table[i][1] );
    }

    return pairs;
  }

  public static WQPair[] convert2pairs( final double[] w, final double[] q )
  {
    if( w.length != q.length )
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.sensor.timeseries.wq.wqtable.WQPair.2" ) ); //$NON-NLS-1$

    final WQPair[] pairs = new WQPair[w.length];
    for( int i = 0; i < w.length; i++ )
    {
      pairs[i] = new WQPair( w[i], q[i] );
    }

    return pairs;
  }

  public static WQPair[] convert2pairs( final Number[] w, final Number[] q )
  {
    if( w.length != q.length )
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.sensor.timeseries.wq.wqtable.WQPair.3" ) ); //$NON-NLS-1$

    final WQPair[] pairs = new WQPair[w.length];
    for( int i = 0; i < w.length; i++ )
    {
      pairs[i] = new WQPair( w[i].doubleValue(), q[i].doubleValue() );
    }

    return pairs;
  }
}
