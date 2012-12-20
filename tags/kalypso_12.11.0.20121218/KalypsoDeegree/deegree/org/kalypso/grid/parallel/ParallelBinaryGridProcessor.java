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
package org.kalypso.grid.parallel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kalypso.grid.GeoGridException;

/**
 * @author barbarins
 */
public class ParallelBinaryGridProcessor
{
  class StopWriterException extends Exception
  {
  }

  private final static int THREADS_AMOUNT = Runtime.getRuntime().availableProcessors();

  private final SequentialBinaryGeoGridReader m_reader;

  private final SequentialBinaryGeoGridWriter m_writer;

  // FIXME: probably this should be a queue
  private final List<ParallelBinaryGridProcessorBean> m_beans;

  private int m_nextBlockToBeWritten = 0;

  private int m_lastBlockToBeWritten = 0;

  private final WriterThread m_writerWorker;

  private final Object m_lock = new Object();

  public ParallelBinaryGridProcessor( final SequentialBinaryGeoGridReader inputGridReader, final SequentialBinaryGeoGridWriter outputGridWriter )
  {
    m_reader = inputGridReader;
    m_writer = outputGridWriter;
    m_writerWorker = new WriterThread( this, m_lock );
    m_beans = new ArrayList<>( m_reader.getBlocksAmount() + 1 );
  }

  // FIXME: implement progress monitor for this operation
  public void calculate( ) throws IOException, GeoGridException
  {
    try
    {
      /* Start reader and writer threads */
      // REMARK: keep one thread free for the writer
      final ReaderThread[] readerWorkers = new ReaderThread[Math.max( 1, THREADS_AMOUNT - 1 )];
      for( int i = 0; i < readerWorkers.length; i++ )
      {
        readerWorkers[i] = new ReaderThread( this );
        readerWorkers[i].start();
      }

      m_writerWorker.start();

      /* Wait for all reader threads to end */
      for( final ReaderThread job : readerWorkers )
        job.join();

      /* Check for exception in worker */
      for( final ReaderThread job : readerWorkers )
      {
        job.join();
        if( job.getException() != null )
        {
          // TODO: check: does interrupt always clear the writer-thread?
          m_writerWorker.interrupt();
          throw job.getException();
        }
      }

      /* Wait for writer to end its work */
      synchronized( m_writerWorker )
      {
        m_writerWorker.notify();
        m_writerWorker.join();
      }
    }
    catch( final InterruptedException e )
    {
      e.printStackTrace();
    }
    finally
    {
      m_writer.close();
    }
  }

  /**
   * Called from reader thread to work on the next dataset. The read thread is responsible for the bean until it it
   * finished.
   */
  synchronized ParallelBinaryGridProcessorBean getNextDatasetForReading( ) throws IOException
  {
    // REMARK: using scale of written-grid here
    final ParallelBinaryGridProcessorBean bean = m_reader.getNextBlock();

    if( bean != null )
    {
      m_beans.add( bean );

      m_lastBlockToBeWritten++;

      synchronized( m_lock )
      {
        m_lock.notify();
      }
    }
    return bean;
  }

  void write( final ParallelBinaryGridProcessorBean bean ) throws IOException
  {
    m_writer.write( bean );
  }

  public synchronized ParallelBinaryGridProcessorBean getNextDatasetForWriting( ) throws StopWriterException
  {
    if( m_nextBlockToBeWritten >= m_reader.getBlocksAmount() )
      throw new StopWriterException();

    if( m_beans.isEmpty() )
      return null;

    if( m_nextBlockToBeWritten >= m_lastBlockToBeWritten )
      return null;

    if( m_beans.get( m_nextBlockToBeWritten ).m_done == false )
      return null;

    // System.out.println( "Fetching block for writing: " + m_nextBlockToBeWritten );

    final ParallelBinaryGridProcessorBean toBeWritten = m_beans.get( m_nextBlockToBeWritten );
    // REMARK: set to null, so not all blocks are in memory
    m_beans.set( m_nextBlockToBeWritten, null );
    m_nextBlockToBeWritten++;
    return toBeWritten;
  }

  public void beanFinished( final ParallelBinaryGridProcessorBean bean )
  {
    bean.m_done = true;

    synchronized( m_lock )
    {
      m_lock.notify();
    }
  }

  double getValue( final int k, final ParallelBinaryGridProcessorBean bean ) throws GeoGridException
  {
    return m_reader.getValue( k, bean );
  }
}