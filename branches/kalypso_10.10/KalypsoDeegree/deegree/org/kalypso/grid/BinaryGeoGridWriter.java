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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import org.apache.commons.lang.NotImplementedException;
import org.deegree.model.spatialschema.ByteUtils;
import org.kalypsodeegree.model.geometry.GM_Surface;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * FIXME: why is everything buffered twice? first we buffer into a local buffer, and then we use a BufferdOutputStream
 * which buffers in the same manner -> this makes no real sense. If it DOES make sense: please COMMENT! <br/>
 * FIXME: half the interface is not implemented and returns null -> at least throw NotImplementedException in this case!<br/>
 * 
 * @author barbarins
 */
public class BinaryGeoGridWriter implements IWriteableGeoGrid
{
  private static Integer BLOCK_SIZE = 1024 * 1024 / 2;

  protected byte[] m_blockData;

  private final BufferedOutputStream m_gridStream;

  private BigDecimal m_max;

  private BigDecimal m_min;

  private final int m_scale;

  private final int m_linesTotal;

  private final int m_lineLen;

  private int m_blockStart;

  private int m_blockEnd;

  private int m_linesInBlock;

  private int m_amountBlocks;

  private final int m_sizeX;

  private final int m_sizeY;

  private int m_itemsInBlock;

  public BinaryGeoGridWriter( final String outputCoverageFileName, final int sizeX, final int sizeY, final int scale ) throws IOException
  {
    // init values
    m_min = BigDecimal.valueOf( Double.MAX_VALUE );
    m_max = BigDecimal.valueOf( -Double.MAX_VALUE );

    m_sizeX = sizeX;
    m_sizeY = sizeY;

    m_scale = scale;

    // .. init file
    m_gridStream = new BufferedOutputStream( new FileOutputStream( outputCoverageFileName ) );

    // ...
    writeInt( 0 ); // version
    writeInt( sizeX );
    writeInt( sizeY );
    writeInt( scale );

    m_linesTotal = getSizeY();
    m_lineLen = getSizeX() * 4;

    // block_size is set to "optimal" size of the buffer from start on

    // FIXME: division does not always return an even result -> must be handles correctly!
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

    m_blockStart = 0;
    m_blockEnd = m_linesInBlock - 1;
    m_itemsInBlock = m_linesInBlock * m_lineLen / 4;

    writeNaN( m_blockData, m_itemsInBlock );

  }

  @Override
  public void setMax( final BigDecimal max )
  {
    m_max = m_max.max( max );
  }

  @Override
  public void setMin( final BigDecimal min )
  {
    m_min = m_min.min( min );
  }

  public final void writeInt( final int v ) throws IOException
  {
    final byte[] lBuff = new byte[4];

    ByteUtils.writeBEInt( lBuff, 0, v );
    m_gridStream.write( lBuff, 0, 4 ); // Version number
  }

  public void close( )
  {

    try
    {
      // FIXME: wrong! last block gets eventually not written!
      // There are examples with blockEnd > linesTotal -> what to do in this case!?
      if( m_blockEnd <= m_linesTotal )
      {
        flushBlock();
      }
      writeInt( m_min.setScale( m_scale, BigDecimal.ROUND_HALF_UP ).unscaledValue().intValue() );
      writeInt( m_max.setScale( m_scale, BigDecimal.ROUND_HALF_UP ).unscaledValue().intValue() );
      m_gridStream.close();
    }
    catch( final IOException e )
    {
      e.printStackTrace();
    }
  }

  public int getScale( )
  {
    return m_scale;
  }

  public void write( final byte[] blockData, final int items ) throws IOException
  {
    m_gridStream.write( blockData, 0, items * 4 );
  }

  private void writeNaN( final byte[] blockData, final int items )
  {
    for( int i = 0; i < items * 4; i += 4 )
      // write the result back into the buffer
      ByteUtils.writeBEInt( blockData, i, BinaryGeoGrid.NO_DATA );
  }

  protected void flushBlock( )
  {
    try
    {
      write( m_blockData, m_itemsInBlock );
    }
    catch( final IOException e )
    {
      e.printStackTrace();
    }
    m_blockStart += m_linesInBlock;
    m_blockEnd += m_linesInBlock;
    if( m_blockEnd > m_linesTotal )
      m_itemsInBlock = (m_linesTotal - m_blockStart + 1) * m_lineLen / 4;

    writeNaN( m_blockData, m_itemsInBlock );
  }

  @Override
  public void setValue( final int x, final int y, final double value )
  {
    if( y < m_blockStart )
      return;

    if( y > m_blockEnd )
    {
      flushBlock();
    }

    final int index = (y - m_blockStart) * (m_lineLen / 4) + x;

    int intVal;
    if( !Double.isNaN( value ) )
    {
      final BigDecimal scaled = BigDecimal.valueOf( value ).setScale( m_scale, BigDecimal.ROUND_HALF_UP );
      intVal = scaled.unscaledValue().intValue();

      final BigDecimal minmax = new BigDecimal( value ).setScale( 4, BigDecimal.ROUND_HALF_UP );

      m_min = m_min.min( minmax );
      m_max = m_max.max( minmax );
    }
    else
    {
      intVal = BinaryGeoGrid.NO_DATA;
    }

    // write the result back into the buffer
    ByteUtils.writeBEInt( m_blockData, index * 4, intVal );
  }

  @Override
  public void dispose( )
  {
    close();
  }

  @Override
  public double getValue( final int x, final int y )
  {
    throw new NotImplementedException();
  }

  @Override
  public IGeoWalkingStrategy getWalkingStrategy( )
  {
    return new OptimizedGeoGridWalkingStrategy();
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getMax()
   */

  @Override
  public BigDecimal getMax( )
  {
    return m_max;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getMin()
   */

  @Override
  public BigDecimal getMin( )
  {
    return m_min;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getSizeX()
   */

  @Override
  public int getSizeX( )
  {
    return m_sizeX;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getSizeY()
   */

  @Override
  public int getSizeY( )
  {
    return m_sizeY;
  }

  /**
   * @see org.kalypso.grid.IWriteableGeoGrid#saveStatistically()
   */

  @Override
  public void saveStatistically( )
  {

  }

  /**
   * @see org.kalypso.grid.IWriteableGeoGrid#setStatistically(java.math.BigDecimal, java.math.BigDecimal)
   */

  @Override
  public void setStatistically( final BigDecimal min, final BigDecimal max )
  {

    // FIXME: this is nonsense!
    close();
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getCell(int, int, java.lang.String)
   */

  @Override
  public GM_Surface< ? > getCell( final int x, final int y, final String targetCRS )
  {
    return null;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getEnvelope()
   */

  @Override
  public Envelope getEnvelope( )
  {
    return null;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getOffsetX()
   */

  @Override
  public Coordinate getOffsetX( )
  {
    return null;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getOffsetY()
   */

  @Override
  public Coordinate getOffsetY( )
  {
    return null;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getOrigin()
   */

  @Override
  public Coordinate getOrigin( )
  {
    return null;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getSourceCRS()
   */

  @Override
  public String getSourceCRS( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getSurface(java.lang.String)
   */
  @Override
  public GM_Surface< ? > getSurface( final String targetCRS )
  {
    return null;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getValueChecked(int, int)
   */

  @Override
  public double getValueChecked( final int x, final int y )
  {
    throw new NotImplementedException();
  }

  /**
   * @see org.kalypso.grid.IGeoValueProvider#getValue(com.vividsolutions.jts.geom.Coordinate)
   */

  @Override
  public double getValue( final Coordinate crd )
  {
    throw new NotImplementedException();
  }
}
