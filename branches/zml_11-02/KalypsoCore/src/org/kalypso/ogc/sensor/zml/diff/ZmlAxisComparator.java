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
package org.kalypso.ogc.sensor.zml.diff;

import java.util.Date;

import org.kalypso.commons.diff.IDiffComparator;
import org.kalypso.commons.diff.IDiffLogger;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;

/**
 * @author Monika Thuel
 */
public class ZmlAxisComparator
{
  private boolean m_result = false;

  private final double m_tolerance = 0.01;

  private final ITupleModel m_values1;

  private final IAxis m_dateAxis1;

  private final IAxis m_valueAxis1;

  private final ITupleModel m_values2;

  private final IAxis m_dateAxis2;

  private final IAxis m_valueAxis2;

  private double m_maxValue1 = Double.NEGATIVE_INFINITY;

  private double m_minValue1 = Double.POSITIVE_INFINITY;

  private double m_maxValue2 = Double.NEGATIVE_INFINITY;

  private double m_minValue2 = Double.POSITIVE_INFINITY;

  private double m_maxDelta = Double.NEGATIVE_INFINITY;

  private int m_diffCount = 0;

  private final IDiffLogger m_logger;

  private double m_differenceAll = 0.0;

  public ZmlAxisComparator( final ITupleModel values1, final IAxis dateAxis1, final IAxis valueAxis1, final ITupleModel values2, final IAxis dateAxis2, final IAxis valueAxis2, final IDiffLogger logger )
  {
    m_values1 = values1;
    m_dateAxis1 = dateAxis1;
    m_valueAxis1 = valueAxis1;
    m_values2 = values2;
    m_dateAxis2 = dateAxis2;
    m_valueAxis2 = valueAxis2;
    m_logger = logger;
  }

  public boolean compare( ) throws SensorException
  {
    final int size = m_values1.size();

    for( int i = 0; i < size; i++ )
    {
      final boolean doStop = compareAtIndex( i );
      if( doStop )
        return true;
    }

    return m_result;
  }

  private boolean compareAtIndex( final int index ) throws SensorException
  {
    final Date date1 = (Date) m_values1.get( index, m_dateAxis1 );
    final Date date2 = (Date) m_values2.get( index, m_dateAxis2 );

    if( !date1.equals( date2 ) )
    {
      m_logger.log( IDiffComparator.DIFF_CONTENT, Messages.getString( "org.kalypso.ogc.sensor.zml.diff.ZMLDiffComparator.5" ) + date1 + " : " + date2 ); //$NON-NLS-1$ //$NON-NLS-2$
      return true;
    }

    final double value1 = ((Double) m_values1.get( index, m_valueAxis1 )).doubleValue();
    final double value2 = ((Double) m_values2.get( index, m_valueAxis2 )).doubleValue();

    final double delta = Math.abs( value1 - value2 );

    m_differenceAll += delta;

    m_maxDelta = Math.max( delta, m_maxDelta );
    m_maxValue1 = Math.max( value1, m_maxValue1 );
    m_maxValue2 = Math.max( value2, m_maxValue2 );
    m_minValue1 = Math.min( value1, m_minValue1 );
    m_minValue2 = Math.min( value2, m_minValue2 );

    if( delta > m_tolerance )
    {
      m_result = true;
      m_diffCount++;
    }

    return false;
  }

  public void logDifferences( final IDiffLogger logger )
  {
    if( m_result )
    {
      logger.log( IDiffComparator.DIFF_CONTENT, Messages.getString( "org.kalypso.ogc.sensor.zml.diff.ZMLDiffComparator.7" ) + m_diffCount ); //$NON-NLS-1$
      logger.log( IDiffComparator.DIFF_CONTENT, Messages.getString( "org.kalypso.ogc.sensor.zml.diff.ZMLDiffComparator.8" ) + m_maxDelta ); //$NON-NLS-1$
      logger.log( IDiffComparator.DIFF_CONTENT, Messages.getString( "org.kalypso.ogc.sensor.zml.diff.ZMLDiffComparator.9" ) + m_differenceAll + Messages.getString( "org.kalypso.ogc.sensor.zml.diff.ZMLDiffComparator.10" ) + m_tolerance //$NON-NLS-1$ //$NON-NLS-2$
          + Messages.getString( "org.kalypso.ogc.sensor.zml.diff.ZMLDiffComparator.11" ) ); //$NON-NLS-1$
    }

    if( m_minValue1 != m_minValue2 )
      logger.log( IDiffComparator.DIFF_CONTENT, Messages.getString( "org.kalypso.ogc.sensor.zml.diff.ZMLDiffComparator.12" ) + m_minValue1 + Messages.getString( "org.kalypso.ogc.sensor.zml.diff.ZMLDiffComparator.13" ) + m_minValue2 ); //$NON-NLS-1$ //$NON-NLS-2$
    if( m_maxValue1 != m_maxValue2 )
      logger.log( IDiffComparator.DIFF_CONTENT, Messages.getString( "org.kalypso.ogc.sensor.zml.diff.ZMLDiffComparator.14" ) + m_maxValue1 + Messages.getString( "org.kalypso.ogc.sensor.zml.diff.ZMLDiffComparator.15" ) + m_maxValue2 ); //$NON-NLS-1$ //$NON-NLS-2$
  }
}