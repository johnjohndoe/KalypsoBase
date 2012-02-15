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

import org.kalypso.grid.GeoGridException;

class ReaderThread extends Thread
{
  private final ParallelBinaryGridProcessor m_manager;

  private ParallelBinaryGridProcessorBean m_bean;

  private GeoGridException m_exception;

  public ReaderThread( final ParallelBinaryGridProcessor manager )
  {
    m_manager = manager;
  }

  @Override
  public void run( )
  {
    while( true )
    {
      try
      {
        m_bean = m_manager.getNextDatasetForReading();
        if( m_bean == null )
          return;

        operate();
      }
      // FIXME: error handling!
      catch( final GeoGridException e )
      {
        e.printStackTrace();
        m_exception = e;
      }
      catch( final IOException e )
      {
        e.printStackTrace();
        m_exception = new GeoGridException( e.getLocalizedMessage(), e );
      }
    }
  }

  private final void operate( ) throws GeoGridException
  {
    final int size = m_bean.getSize();
    for( int i = 0; i < size; i++ )
    {
      final double value = m_manager.getValue( i, m_bean );
      m_bean.setValue( i, value );
    }

    m_manager.beanFinished( m_bean );
  }

  public GeoGridException getException( )
  {
    return m_exception;
  }
}