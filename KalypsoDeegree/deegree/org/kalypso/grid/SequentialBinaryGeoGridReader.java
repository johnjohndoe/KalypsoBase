package org.kalypso.grid;

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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.deegree.model.spatialschema.ByteUtils;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;

/**
 * @author barbarins
 */
public abstract class SequentialBinaryGeoGridReader extends AbstractDelegatingGeoGrid
{
  protected static Integer BLOCK_SIZE = 1024 * 1024 / 2;

  protected BufferedInputStream m_gridStream;

  protected int m_scale;

  private int m_linesTotal;

  protected static int m_amountBlocks;

  protected static int m_linesInBlock;

  protected static int m_linesRead = 0;

  protected static long m_lineLen = 0;

  public int getBlocksAmount( )
  {
    return m_amountBlocks;
  }

  public SequentialBinaryGeoGridReader( final IGeoGrid inputGrid, final URL pUrl ) throws IOException
  {
    super( inputGrid );

    /* Tries to find a file from the given url. */
    File fileFromUrl = ResourceUtilities.findJavaFileFromURL( pUrl );
    if( fileFromUrl == null )
      fileFromUrl = FileUtils.toFile( pUrl );

    m_gridStream = new BufferedInputStream( new FileInputStream( fileFromUrl ) );

    // skip header
    /* Read header */
    m_gridStream.skip( 12 );
    final byte[] lScaleBuff = new byte[4];
    read( lScaleBuff, 1 );
    m_scale = ByteUtils.readBEInt( lScaleBuff, 0 );

    m_linesRead = 0;

    try
    {
      m_linesTotal = getSizeY();

      final long linesPerThread = m_linesTotal / 8;
      m_lineLen = getSizeX() * 4;

      // block_size is set to "optimal" size of the buffer from start on
      m_linesInBlock = (int) (BLOCK_SIZE / m_lineLen);

      if( m_linesInBlock >= m_linesTotal )
        m_linesInBlock = (int) linesPerThread;

      if( m_linesInBlock == 0 )
        m_linesInBlock = 1;

      m_amountBlocks = m_linesTotal / m_linesInBlock;
      if( m_linesTotal % m_linesInBlock != 0 )
        m_amountBlocks++;

      BLOCK_SIZE = m_linesInBlock * (int) m_lineLen;

    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

  }

  public void read( final byte[] blockData, final int items ) throws IOException
  {
    m_gridStream.read( blockData, 0, items * 4 );
  }

  public int getScale( )
  {
    return m_scale;
  }

  public void close( ) throws IOException
  {
    m_gridStream.close();
  }

  protected ParallelBinaryGridProcessorBean createNewBean( )
  {
    return new ParallelBinaryGridProcessorBean( BLOCK_SIZE );
  }

  public ParallelBinaryGridProcessorBean getNextBlock( )
  {
    if( m_linesRead >= m_linesTotal )
    {
      return null;
    }

    final ParallelBinaryGridProcessorBean lBean = createNewBean();

    if( m_linesRead + m_linesInBlock <= m_linesTotal )
    {
      lBean.m_itemsInBlock = (int) (m_linesInBlock * m_lineLen) / 4;
    }
    else
    {
      lBean.m_itemsInBlock = (int) ((m_linesTotal - m_linesRead) * m_lineLen) / 4;
    }
    lBean.m_startPosY = m_linesRead;
    try
    {
      read( lBean.m_blockData, lBean.m_itemsInBlock );
    }
    catch( final IOException e )
    {
      e.printStackTrace();
      return null;
    }
    m_linesRead += m_linesInBlock;
    return lBean;
  }

  public abstract double getValue( final int k, final ParallelBinaryGridProcessorBean bean ) throws GeoGridException, Exception;

}
