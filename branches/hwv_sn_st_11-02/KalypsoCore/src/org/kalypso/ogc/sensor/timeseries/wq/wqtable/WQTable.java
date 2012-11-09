package org.kalypso.ogc.sensor.timeseries.wq.wqtable;

import java.text.DateFormat;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.kalypso.commons.math.LinearEquation;
import org.kalypso.commons.math.LinearEquation.SameXValuesException;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.timeseries.wq.WQException;

/**
 * WQTable
 * 
 * @author schlienger
 */
public class WQTable
{
  private static final WQException CANNOT_INTERPOLATE_EXCEPTION = new WQException( Messages.getString( "org.kalypso.ogc.sensor.timeseries.wq.wqtable.WQTable.0" ) ); //$NON-NLS-1$

  private final TreeSet<WQPair> m_qSortedPairs;

  private final TreeSet<WQPair> m_wSortedPairs;

  private final LinearEquation m_eq = new LinearEquation();

  private final Date m_validity;

  private int m_offset;

  /**
   * Creates a WQTable with a default offset of 0
   * 
   * @param validity
   *          date up from which this table is valid
   */
  public WQTable( final Date validity, final double[][] table )
  {
    this( validity, 0, table );
  }

  /**
   * Creates a WQTable
   * 
   * @param validity
   *          date up from which this table is valid
   * @param offset
   *          offset used for W, before conversion W = W + offset
   */
  public WQTable( final Date validity, final int offset, final double[][] table )
  {
    this( validity, offset, WQPair.convert2pairs( table ) );
  }

  /**
   * Creates a WQTable with a default offset of 0
   * 
   * @param validity
   *          date up from which this table is valid
   */
  public WQTable( final Date validity, final double[] w, final double[] q )
  {
    this( validity, 0, w, q );
  }

  /**
   * Creates a WQTable
   * 
   * @param validity
   *          date up from which this table is valid
   * @param offset
   *          offset used for W, before conversion W = W + offset
   */
  public WQTable( final Date validity, final int offset, final double[] w, final double[] q )
  {
    this( validity, offset, WQPair.convert2pairs( w, q ) );
  }

  /**
   * Creates a WQTable with a default offset of 0
   * 
   * @param validity
   *          date up from which this table is valid
   */
  public WQTable( final Date validity, final Number[] w, final Number[] q )
  {
    this( validity, 0, w, q );
  }

  /**
   * Creates a WQTable
   * 
   * @param validity
   *          date up from which this table is valid
   * @param offset
   *          offset used for W, before conversion W = W + offset
   */
  public WQTable( final Date validity, final int offset, final Number[] w, final Number[] q )
  {
    this( validity, offset, WQPair.convert2pairs( w, q ) );
  }

  /**
   * Creates a WQTable
   * 
   * @param validity
   *          date up from which this table is valid
   * @param offset
   *          offset used for W, before conversion W = W + offset
   */
  public WQTable( final Date validity, final int offset, final WQPair[] wqpairs )
  {
    m_validity = validity;
    m_offset = offset;

    m_qSortedPairs = new TreeSet<WQPair>( WQPairComparator.Q_COMPARATOR );
    m_wSortedPairs = new TreeSet<WQPair>( WQPairComparator.W_COMPARATOR );

    for( final WQPair wqpair : wqpairs )
    {
      m_qSortedPairs.add( wqpair );
      m_wSortedPairs.add( wqpair );
    }
  }

  public double getWFor( final double q ) throws WQException
  {
    final WQPair p = new WQPair( 0, q );
    final SortedSet<WQPair> headSet = m_qSortedPairs.headSet( p );
    final SortedSet<WQPair> tailSet = m_qSortedPairs.tailSet( p );

    try
    {
      // FIXME: extrapolate
      if( headSet.isEmpty() || tailSet.isEmpty() )
        return getInterpolatedWFor( q );

      final WQPair p1 = headSet.last();
      final WQPair p2 = tailSet.first();

      m_eq.setPoints( p1.getW(), p1.getQ(), p2.getW(), p2.getQ() );
    }
    catch( final SameXValuesException e )
    {
      throw new WQException( Messages.getString( "org.kalypso.ogc.sensor.timeseries.wq.wqtable.WQTable.1" ) + q, e ); //$NON-NLS-1$
    }

    return m_eq.computeX( q );
  }

  private double getInterpolatedWFor( final double q ) throws WQException, SameXValuesException
  {
    final WQPair[] pairs = m_qSortedPairs.toArray( new WQPair[] {} );
    if( pairs.length < 2 )
      throw CANNOT_INTERPOLATE_EXCEPTION; // should exception be thrown or a value returned?

    final WQPair p1 = pairs[0];
    final WQPair p2 = pairs[1];

    final WQPair pm = pairs[pairs.length - 2];
    final WQPair pn = pairs[pairs.length - 1];

    if( q < p1.getQ() )
    {
      m_eq.setPoints( p2.getW(), p2.getQ(), p1.getW(), p1.getQ() );
    }
    else if( q == p1.getQ() )
    {
      return p1.getW();
    }
    else if( q > pn.getQ() )
    {
      m_eq.setPoints( pm.getW(), pm.getQ(), pn.getW(), pn.getQ() );
    }
    else if( q == pn.getQ() )
    {
      return pn.getW();
    }
    else
      throw CANNOT_INTERPOLATE_EXCEPTION;

    return m_eq.computeX( q );
  }

  public double getQFor( final double w ) throws WQException
  {
    final WQPair p = new WQPair( w, 0 );
    final SortedSet<WQPair> headSet = m_wSortedPairs.headSet( p );
    final SortedSet<WQPair> tailSet = m_wSortedPairs.tailSet( p );

    try
    {
      if( headSet.isEmpty() || tailSet.isEmpty() )
        return getInterpolatedQFor( w ); //

      final WQPair p1 = headSet.last();
      final WQPair p2 = tailSet.first();

      m_eq.setPoints( p1.getW(), p1.getQ(), p2.getW(), p2.getQ() );
    }
    catch( final SameXValuesException e )
    {
      throw new WQException( Messages.getString( "org.kalypso.ogc.sensor.timeseries.wq.wqtable.WQTable.2" ) + w, e ); //$NON-NLS-1$
    }

    return m_eq.computeY( w );
  }

  private double getInterpolatedQFor( final double w ) throws WQException, SameXValuesException
  {
    final WQPair[] pairs = m_qSortedPairs.toArray( new WQPair[] {} );
    if( pairs.length < 2 )
      throw CANNOT_INTERPOLATE_EXCEPTION; // should exception be thrown or a value returned?

    final WQPair p1 = pairs[0];
    final WQPair p2 = pairs[1];

    final WQPair pm = pairs[pairs.length - 2];
    final WQPair pn = pairs[pairs.length - 1];

    if( w <= p1.getW() )
    {
      m_eq.setPoints( p2.getW(), p2.getQ(), p1.getW(), p1.getQ() );
    }
    else if( w > pn.getW() )
    {
      m_eq.setPoints( pm.getW(), pm.getQ(), pn.getW(), pn.getQ() );
    }
    else
      throw CANNOT_INTERPOLATE_EXCEPTION;

    return m_eq.computeY( w );
  }

  public Date getValidity( )
  {
    return m_validity;
  }

  public int getOffset( )
  {
    return m_offset;
  }

  public void setOffset( final int offset )
  {
    m_offset = offset;
  }

  @Override
  public String toString( )
  {
    final DateFormat df = DateFormat.getDateTimeInstance();
    return Messages.getString( "org.kalypso.ogc.sensor.timeseries.wq.wqtable.WQTable.3", df.format( m_validity ), m_offset );
  }

  public WQPair[] getPairs( )
  {
    return m_wSortedPairs.toArray( new WQPair[m_wSortedPairs.size()] );
  }

  public Double getQMax( )
  {
    if( m_qSortedPairs.isEmpty() )
      return null;

    return m_qSortedPairs.last().getQ();
  }

  public Double getQMin( )
  {
    if( m_qSortedPairs.isEmpty() )
      return null;

    return m_qSortedPairs.first().getQ();
  }

  public Double getWMin( )
  {
    if( m_qSortedPairs.isEmpty() )
      return null;

    return m_qSortedPairs.first().getW();
  }

  public Double getWMax( )
  {
    if( m_qSortedPairs.isEmpty() )
      return null;

    return m_qSortedPairs.last().getW();
  }

}
