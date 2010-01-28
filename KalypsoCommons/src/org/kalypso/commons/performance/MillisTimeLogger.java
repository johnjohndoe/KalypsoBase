/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.commons.performance;

import javax.xml.datatype.DatatypeFactory;

/**
 * A small helper class wich shows usthe duraction of some code.
 * <p>
 * Usage:
 * </p>
 * <p>
 * The class remebers its time of construction
 * </p>
 * <p>
 * Call #showElapsedTime( String ) to print a message, indicating how long it took since construction
 * </p>.
 *
 * @author Belger + Maximino
 */
public class MillisTimeLogger
{
  private DatatypeFactory m_factory = null;

  private final long m_constructionTime;

  private long m_lastCall;
  
  private long m_currentInterimMillis;
  
  private long m_currentTotalMillis;

  public MillisTimeLogger( )
  {
    this( null );
  }

  /**
   * Create a time logger and start measuring. Prints the given message.
   */
  public MillisTimeLogger( final String message )
  {
    m_constructionTime = System.currentTimeMillis();
    
    m_lastCall = m_constructionTime;
  }

  public long takeInterimTime( )
  {
    final long current = System.currentTimeMillis();

    m_currentInterimMillis = current - m_lastCall;
    m_currentTotalMillis = current - m_constructionTime;
    
    m_lastCall = current;
    
    return m_currentInterimMillis;
  }
  
  public long getTotalTime( )
  {
	  return m_currentTotalMillis;
  }
  
  public void printTotalTime( String msg )
  {
	  System.out.print( msg );
	  System.out.println( getTotalTime() );
  }
}
