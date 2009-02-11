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
package org.kalypso.contribs.eclipse.ui.progress;

import java.text.DateFormat;
import java.util.Date;

/**
 * This class calculates the estimated time based on start, end time, items done and max items.
 * 
 * @author Holger Albert
 */
public class EstimatedTime
{
  /**
   * The maximum number of items.
   */
  private int m_maxItems;

  /**
   * The number of items, whose are already done.
   */
  private int m_itemsDone;

  /**
   * The start time.
   */
  private long m_start;

  /**
   * The end time.
   */
  private long m_end;

  /**
   * This date formatter will be used to format the time string returned.
   */
  private DateFormat m_df;

  /**
   * The constructor.
   * 
   * @param maxItems
   *          The maximum number of items.
   */
  public EstimatedTime( int maxItems )
  {
    m_maxItems = maxItems;
    m_itemsDone = 0;
    m_start = -1;
    m_end = -1;

    m_df = DateFormat.getTimeInstance( DateFormat.LONG );
  }

  /**
   * This function increases the items done.
   * 
   * @param done
   *          The number of items done.
   */
  public void increaseItemsDone( int done )
  {
    if( done <= 0 )
      return;

    m_itemsDone = m_itemsDone + done;
    if( m_itemsDone > m_maxItems )
      m_itemsDone = m_maxItems;
  }

  /**
   * This function sets the start time of the process.
   */
  public void setStart( )
  {
    m_start = System.currentTimeMillis();
  }

  /**
   * This function sets the end time (of the currently worked process).
   */
  public void setEnd( )
  {
    m_end = System.currentTimeMillis();
  }

  /**
   * This function returns the estimated time left
   * 
   * @return The estimated time left.
   */
  private long getEstimatedTimeLeft( )
  {
    if( m_maxItems <= 0 || m_itemsDone <= 0 || m_start == -1 || m_end == -1 )
      return -1;

    long worked = m_end - m_start;
    long workTimeItem = worked / m_itemsDone;

    return workTimeItem * (m_maxItems - m_itemsDone);
  }

  /**
   * This function returns the estimated time.
   * 
   * @return The estimated time.
   */
  public String getEstimatedTime( )
  {
    long estimatedTimeLeft = getEstimatedTimeLeft();
    if( estimatedTimeLeft <= 0 )
      return "Unknown ...";

    Date estimatedTime = new Date( estimatedTimeLeft );

    return m_df.format( estimatedTime );
  }

  /**
   * This function returns the exceeded time.
   * 
   * @return The exceeded time.
   */
  public String getExceededTime( )
  {
    if( m_start == -1 || m_end == -1 )
      return "Unknown";

    Date exceededTime = new Date( m_end - m_start );

    return m_df.format( exceededTime );
  }
}