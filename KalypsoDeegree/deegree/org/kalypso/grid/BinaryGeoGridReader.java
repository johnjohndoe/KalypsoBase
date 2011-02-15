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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.deegree.model.spatialschema.ByteUtils;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author barbarins
 */
public class BinaryGeoGridReader extends AbstractDelegatingGeoGrid
{
  private static Integer BLOCK_SIZE = 1024 * 1024 / 2;

  protected byte[] m_blockData;

  protected BufferedInputStream m_gridStream;

  protected int m_scale;

  private int m_linesTotal;

  private int m_lineLen;

  private int m_blockStart;

  private int m_blockEnd;

  private int m_linesInBlock;

  private int m_amountBlocks;

  public BinaryGeoGridReader( final IGeoGrid inputGrid, final URL pUrl ) throws IOException
  {
    super( inputGrid );

    File fileFromUrl = ResourceUtilities.findJavaFileFromURL( pUrl );
    /* Tries to find a file from the given url. */
    if( fileFromUrl == null )
      fileFromUrl = FileUtils.toFile( pUrl );

    m_gridStream = new BufferedInputStream( new FileInputStream( fileFromUrl ) );

    // skip header
    /* Read header */
    m_gridStream.skip( 12 );
    byte[] lScaleBuff = new byte[4];
    read( lScaleBuff, 1 );
    m_scale = ByteUtils.readBEInt( lScaleBuff, 0 );

    try
    {
      m_linesTotal = getSizeY();
      m_lineLen = getSizeX() * 4;
    }
    catch( GeoGridException e )
    {
      e.printStackTrace();
    }

    // block_size is set to "optimal" size of the buffer from start on
    m_linesInBlock = (BLOCK_SIZE / m_lineLen);

    if( m_linesInBlock >= m_linesTotal )
      m_linesInBlock = m_linesTotal;

    if( m_linesInBlock == 0 )
      m_linesInBlock = 1;

    m_amountBlocks = m_linesTotal / m_linesInBlock;
    if( m_linesTotal % m_linesInBlock != 0 )
      m_amountBlocks++;

    BLOCK_SIZE = m_linesInBlock * m_lineLen;

    m_blockData = new byte[BLOCK_SIZE];

    readNextBlock();
    m_blockStart = 0;
    m_blockEnd = m_linesInBlock - 1;
  }

  public int getScale( )
  {
    return m_scale;
  }

  public void close( )
  {
    try
    {
      m_gridStream.close();
    }
    catch( IOException e )
    {
      e.printStackTrace();
    }
  }

  @Override
  public void dispose( )
  {
    super.dispose();
    close();
  }

  private void read( byte[] blockData, int items ) throws IOException
  {
    m_gridStream.read( blockData, 0, items * 4 );
  }

  private void readNextBlock( )
  {
    try
    {
      m_gridStream.read( m_blockData, 0, BLOCK_SIZE );
    }
    catch( IOException e )
    {
      e.printStackTrace();
    }

    m_blockStart += m_linesInBlock;
    m_blockEnd += m_linesInBlock;
  }

  @Override
  public double getValue( final int x, final int y )
  {
    if( y < m_blockStart )
      return Double.NaN;

    while( y > m_blockEnd )
    {
      readNextBlock();
    }

    final int index = (y - m_blockStart) * (m_lineLen / 4) + x;
    final int z = ByteUtils.readBEInt( m_blockData, index * 4 );

    if( z == Integer.MIN_VALUE /* NO_DATA */)
      return Double.NaN;

    final BigDecimal decimal = new BigDecimal( BigInteger.valueOf( z ), m_scale );
    final double value = decimal.doubleValue();

    return value;
  }

  @Override
  public IGeoWalkingStrategy getWalkingStrategy( )
  {
    return new OptimizedGeoGridWalkingStrategy();
  }

  @Override
  public double getValue( final Coordinate crd ) throws GeoGridException
  {
    final GeoGridCell cell = GeoGridUtilities.cellFromPosition( this, crd );
    return getValue( cell.x, cell.y );
  }

}
