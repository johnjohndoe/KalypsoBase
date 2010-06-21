/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.grid;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author barbarins
 */
public class ParallelBinaryGridProcessor
{
  private final static int THREADS_AMOUNT = Runtime.getRuntime().availableProcessors();

  protected class CalculationWriterThread extends Thread
  {
    private int m_nextBlockToBeWritten = 0;

    private int m_lastBlockToBeWritten = 0;

    // lets try without synchronizing ...
    public void incBlocksToProcessCount( )
    {
      m_lastBlockToBeWritten++;
    }

    public CalculationWriterThread( )
    {
    }

    @Override
    public void run( )
    {

      synchronized( this )
      {
        try
        {
          while( m_nextBlockToBeWritten < m_reader.getBlocksAmount() )
          {
            this.wait();

            if( m_beans.isEmpty() == false )
            {
              for( ; m_nextBlockToBeWritten < m_lastBlockToBeWritten; m_nextBlockToBeWritten++ )
              {
                if( m_beans.get( m_nextBlockToBeWritten ).m_done == false )
                  break;

                m_writer.write( m_beans.get( m_nextBlockToBeWritten ) );
                m_beans.set( m_nextBlockToBeWritten, null );
              }
            }
          }
        }
        catch( IOException e )
        {
          e.printStackTrace();
        }
        catch( InterruptedException e )
        {
          e.printStackTrace();
        }

      }
    }
  }

  protected class CalculationJob extends Thread
  {
    private ParallelBinaryGridProcessor m_manager;

    private ParallelBinaryGridProcessorBean m_bean;

    public CalculationJob( final ParallelBinaryGridProcessor pManager )
    {
      m_manager = pManager;
    }

    @Override
    public void run( )
    {
      while( true )
      {
        m_bean = m_manager.getNextDataset();
        if( m_bean == null )
          break;
        operate();
      }
    }

    private final void operate( )
    {
      for( int k = 0; k < m_bean.m_itemsInBlock; k++ )
      {
        final double value = m_reader.getValue( k, m_bean );
        m_writer.setValue( value, k, m_bean );
      }
      m_bean.m_done = true;
    }
  }

  SequentialBinaryGeoGridReader m_reader;

  SequentialBinaryGeoGridWriter m_writer;

  protected static ArrayList<ParallelBinaryGridProcessorBean> m_beans;// = new

  private final static CalculationJob[] m_jobs = new CalculationJob[THREADS_AMOUNT];

  private final CalculationWriterThread m_writer_thread;

  public ParallelBinaryGridProcessor( SequentialBinaryGeoGridReader inputGridReader, SequentialBinaryGeoGridWriter outputGridWriter )
  {
    m_reader = inputGridReader;
    m_writer = outputGridWriter;
    m_writer_thread = new CalculationWriterThread();

  }

  public IGeoGrid getGrid( )
  {
    return m_reader.getDelegate();
  }

  public void calculate( ) throws IOException
  {

    m_beans = new ArrayList<ParallelBinaryGridProcessorBean>( m_reader.getBlocksAmount() + 1 );

    try
    {
      for( int i = 0; i < (m_jobs.length); i++ )
      {
        m_jobs[i] = new CalculationJob( this );
        m_jobs[i].start();
      }

      m_writer_thread.start();

      for( int i = 0; i < (m_jobs.length); i++ )
      {
        m_jobs[i].join();
      }

      synchronized( m_writer_thread )
      {
        m_writer_thread.notify();
        m_writer_thread.join();
      }

      m_writer.close();
    }
    catch( InterruptedException e )
    {
      e.printStackTrace();
    }
  }

  public synchronized ParallelBinaryGridProcessorBean getNextDataset( )
  {
    ParallelBinaryGridProcessorBean lBean = m_reader.getNextBlock();

    if( lBean != null )
    {
      m_beans.add( lBean );
      synchronized( m_writer_thread )
      {
        m_writer_thread.incBlocksToProcessCount();
        m_writer_thread.notify();
      }
    }
    return lBean;
  }

}
