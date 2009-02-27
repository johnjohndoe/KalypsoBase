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
package org.kalypso.swtchart.chart.axis;

import java.util.Calendar;
import java.util.Comparator;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.kalypso.swtchart.chart.axis.IAxisConstants.DIRECTION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.POSITION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.PROPERTY;
import org.kalypso.swtchart.chart.axis.registry.IAxisRegistry;
import org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer;
import org.kalypso.swtchart.logging.Logger;

/**
 * @author schlienger
 * @author burtscher
 *
 * Abstract implementation of IAxis - implements some
 * methods which are equal for all concrete IAxis-classes
 */
public abstract class AbstractAxis<T> implements IAxis<T>
{
  protected IAxisRegistry m_registry = null;

  private final String m_id;

  private final String m_label;

  private final PROPERTY m_prop;

  private final POSITION m_pos;

  private final DIRECTION m_dir;

  private final Comparator<T> m_dataComparator;

  // TODO: consider using a IDataRange instead
  private T m_min;

  private T m_max;

  private final Class< ? > m_dataClass;

  /**
   * Uses a ComparableComparator as dataComparator
   */
  @SuppressWarnings("unchecked")
  public AbstractAxis( final String id, final String label, final PROPERTY prop, final POSITION pos, final DIRECTION dir, final Class< ? > dataClass )
  {
    this( id, label, prop, pos, dir, new ComparableComparator(), dataClass );
  }

  public AbstractAxis( final String id, final String label, final PROPERTY prop, final POSITION pos, final DIRECTION dir, final Comparator<T> dataComparator, final Class< ? > dataClass )
  {
    m_id = id;
    m_label = label;
    m_prop = prop;
    m_pos = pos;
    m_dir = dir;
    m_dataComparator = dataComparator;
    m_dataClass = dataClass;
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#getDataClass()
   */
  public Class< ? > getDataClass( )
  {
    return m_dataClass;
  }

  /**
   * TODO: consider to move the dataComparater to the range class
   */
  public Comparator<T> getDataComparator( )
  {
    return m_dataComparator;
  }
  
  /**
   * @see org.kalypso.swtchart.axis.IAxis#setRegistry(org.kalypso.swtchart.axis.registry.IAxisRegistry)
   */
  public void setRegistry( final IAxisRegistry axisRegistry )
  {
    m_registry = axisRegistry;
  }


  public IAxisRegistry  getRegistry( )
  {
    return m_registry;
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#getRenderer()
   */
  @SuppressWarnings("unchecked")
  public IAxisRenderer<T> getRenderer( )
  {
    if( m_registry == null )
      throw new IllegalStateException( "Registry is null" );

    return m_registry.getRenderer( this );
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#getIdentifier()
   */
  public String getIdentifier( )
  {
    return m_id;
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#getLabel()
   */
  public String getLabel( )
  {
    return m_label;
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#getProperty()
   */
  public PROPERTY getProperty( )
  {
    return m_prop;
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#getPosition()
   */
  public POSITION getPosition( )
  {
    return m_pos;
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#getDirection()
   */
  public DIRECTION getDirection( )
  {
    return m_dir;
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#isInverted()
   */
  public boolean isInverted( )
  {
    return getDirection() == DIRECTION.NEGATIVE;
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#getMin()
   */
  public T getFrom( )
  {
    return m_min;
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#setMin(T)
   */
  public void setFrom( final T min )
  {
//    if( m_max != null && m_dataComparator.compare( min, m_max ) == 0 )
//      throw new IllegalArgumentException( "min == max" );

    if (min instanceof Calendar)
    {
      Calendar minC=(Calendar) min;
      Logger.logInfo(Logger.TOPIC_LOG_GENERAL, "CalendarAxis Offset: "+(minC.getTimeInMillis() % (1000*60*60*24)));
    }
    m_min = min;
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#getMax()
   */
  public T getTo( )
  {
    return m_max;
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#setMax(T)
   */
  public void setTo( final T max )
  {
//    if( m_min != null && m_dataComparator.compare( max, m_min ) == 0 )
//      throw new IllegalArgumentException( "max == min" );

    m_max = max;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return m_label + " " + "{" + m_id + " " + m_pos + " " + m_dir + "}";
  }
}
