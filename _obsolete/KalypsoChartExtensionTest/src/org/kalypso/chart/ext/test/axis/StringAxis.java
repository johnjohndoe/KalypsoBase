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
package org.kalypso.chart.ext.test.axis;

import java.util.Comparator;
import java.util.List;

import org.kalypso.chart.ext.base.axis.AbstractAxis;
import org.kalypso.chart.framework.model.data.IDataOperator;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.PROPERTY;
import org.kalypso.chart.framework.model.mapper.component.IAxisComponent;

/**
 * @author alibu
 * 
 */
public class StringAxis extends AbstractAxis<String>
{

  private IDataRange<Number> m_numericRange;

  private List<String> m_values;

  private final IDataOperator<String> m_dataOperator = new StringOperator();

  public StringAxis( String id, String label, final PROPERTY prop, final POSITION pos, final DIRECTION dir, final Comparator<String> dataComparator, final Class< ? > dataClass, final List<String> values )
  {
    this( id, label, prop, pos, dir );
    m_values = values;

  }

  private StringAxis( String id, String label, final PROPERTY prop, final POSITION pos, final DIRECTION dir )
  {
    super( id, label, prop, pos, dir, (new String()).getClass() );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#getNumericRange()
   */
  public IDataRange<Number> getNumericRange( )
  {
    return m_numericRange;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#logicalToScreen(java.lang.Object)
   */
  public int logicalToScreen( String value )
  {
    if( m_values.contains( value ) )
    {
      int size = m_values.size();

      return size;

    }
    else
    {
      return Integer.MAX_VALUE;
    }
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#numericToScreen(java.lang.Number)
   */
  public int numericToScreen( Number value )
  {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#screenToNumeric(int)
   */
  public Number screenToNumeric( int value )
  {
    if( getRegistry() == null )
      return Double.NaN;

    final IAxisComponent comp = getRegistry().getComponent( this );
    if( comp == null )
      return Double.NaN;

    return normalizedToLogical( comp.screenToNormalized( value ) );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#setNumericRange(org.kalypso.chart.framework.model.data.IDataRange)
   */
  public void setNumericRange( IDataRange<Number> range )
  {
    m_numericRange = range;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IMapper#getDataOperator()
   */
  public IDataOperator<String> getDataOperator( )
  {
    return m_dataOperator;
  }

  public double logicalToNormalized( final Number value )
  {
    final IDataRange<Number> dataRange = getNumericRange();

    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();

    final double norm = (value.doubleValue() - dataRange.getMin().doubleValue()) / r;

    return norm;
  }

  public Number normalizedToLogical( final double value )
  {
    final IDataRange<Number> dataRange = getNumericRange();

    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();

    final double logical = value * r + dataRange.getMin().doubleValue();

    return logical;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#logicalToScreen(T)
   */
  public int logicalToScreen( final Number value )
  {
    if( value == null )
      return 0;
    if( getRegistry() == null )
      return 0;

    final IAxisComponent comp = getRegistry().getComponent( this );
    if( comp == null )
      return 0;

    return comp.normalizedToScreen( logicalToNormalized( value ) );
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#screenToLogical(int)
   */
  public String screenToLogical( final int value )
  {
    if( getRegistry() == null )
      return null;

    final IAxisComponent comp = getRegistry().getComponent( this );
    if( comp == null )
      return null;

    Number normalizedToLogical = normalizedToLogical( comp.screenToNormalized( value ) );
    int intVal = normalizedToLogical.intValue();
    if( intVal < m_values.size() )
      return m_values.get( intVal );
    else
      return null;

  }

  public int zeroToScreen( )
  {
    return logicalToScreen( 0.0 );
  }

}
