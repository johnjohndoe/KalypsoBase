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
package org.kalypso.grid.parallel;

import java.io.IOException;

import org.kalypso.grid.parallel.ParallelBinaryGridProcessor.StopWriterException;

class WriterThread extends Thread
{
  private final ParallelBinaryGridProcessor m_manager;

  private final Object m_lock;

  public WriterThread( final ParallelBinaryGridProcessor manager, final Object lock )
  {
    m_manager = manager;
    m_lock = lock;
  }

  @Override
  public void run( )
  {
    try
    {
      while( !isInterrupted() )
      {
        final ParallelBinaryGridProcessorBean bean = m_manager.getNextDatasetForWriting();
        if( bean == null )
        {
          synchronized( m_lock )
          {
            m_lock.wait();
          }
        }
        else
          m_manager.write( bean );
      }
    }
    catch( final IOException e )
    {
      e.printStackTrace();
    }
    catch( final InterruptedException e )
    {
      e.printStackTrace();
    }
    catch( final StopWriterException e )
    {
      // thrown by manager to stop this thread
    }
  }
}