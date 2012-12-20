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
package org.kalypso.grid;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.Assert;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A {@link IGeoGrid} implementation based on the Kalypso Binary-Grid-Format.
 * <p>
 * The grid is accessed on demand, so the memory consumption of this implementation is very low.
 * </p>
 * <p>
 * Use one of the tow factory methods {@link #openGrid(URL, Coordinate, Coordinate, Coordinate)} ord {@link #createGrid(File, int, int, int, Coordinate, Coordinate, Coordinate)} to instantiate this
 * class.
 * </p>
 * <p>
 * Format description:
 * </p>
 * 
 * <pre>
 *  Version:        Version Number (Currently 0)
 *  SizeX:          Grid Size in horizontal direction
 *  SizeY:          Grid Size in vertical direction
 *  Scale:          Number of fraction digits of the cell values
 *  /Cell-Values/:  SizeX times SizeY cell values.
 *  Min:            Minimum value
 *  Max:            Maximum value
 * </pre>
 * <p>
 * All values are encoded as lower endian integers (4 bytes).
 * </p>
 * 
 * @author Dejan Antanaskovic
 * @author Thomas Jung
 * @author Gernot Belger
 */
public class BinaryGeoGrid extends AbstractGeoGrid implements IWriteableGeoGrid
{
  public static final int NO_DATA = Integer.MIN_VALUE;

  /* number of lines buffered when reading */
  private static final int BUFFER_LINES = 5;

  /* Buffer for reading integers. */
  private final ByteBuffer m_readBuffer;

  /* The file position corresponding to the current read buffer, -1 if buffer contains no current data */
  private long m_bufferPosition = -1;

  /* Buffer for writing integers. */
  private final ByteBuffer m_writeBuffer;

  private FileChannel m_channel;

  /* If set, this file will be deleted on dispose. Used if grid is hold in temporary file. */
  private final File m_binFile;

  private final BinaryGeoGridHeader m_header;

  private Integer m_unscaledMin;

  private Integer m_unscaledMax;

  /**
   * Opens an existing grid for read-only access.<br>
   * Dispose the grid after it is no more needed in order to release the given resource.
   * 
   * @param writeable
   *          If <code>true</code>, the grid is opened for writing and a {@link IWriteableGeoGrid} is returned.
   */
  public static BinaryGeoGrid openGrid( final URL url, final Coordinate origin, final Coordinate offsetX, final Coordinate offsetY, final String sourceCRS, final boolean writeable ) throws IOException
  {
    /* Tries to find a file from the given url. */
    File fileFromUrl = ResourceUtilities.findJavaFileFromURL( url );
    File binFile = null;
    if( fileFromUrl == null )
      fileFromUrl = FileUtils.toFile( url );

    if( fileFromUrl == null )
    {
      /*
       * If url cannot be converted to a file, write its contents to a temporary file which will be deleted after the
       * grid gets disposed.
       */
      fileFromUrl = File.createTempFile( "local", ".bin" );
      fileFromUrl.deleteOnExit();
      FileUtils.copyURLToFile( url, fileFromUrl );
      binFile = fileFromUrl; // set in order to delete on dispose
    }

    FileChannel channel;
    if( writeable )
      channel = FileChannel.open( fileFromUrl.toPath(), StandardOpenOption.WRITE, StandardOpenOption.READ );
    else
      channel = FileChannel.open( fileFromUrl.toPath(), StandardOpenOption.READ );

    return new BinaryGeoGrid( channel, binFile, origin, offsetX, offsetY, sourceCRS );
  }

  /**
   * Crates a new grid file with the given size and scale.<br>
   * The grid is then opened in write mode, so its values can then be set.<br>
   * The grid must be disposed afterwards in order to flush the written information. *
   * 
   * @param fillGrid
   *          If set to <code>true</code>, the grid will be initially filled with no-data values. Else, the grid values
   *          are undetermined.
   */
  public static BinaryGeoGrid createGrid( final File file, final int sizeX, final int sizeY, final int scale, final Coordinate origin, final Coordinate offsetX, final Coordinate offsetY, final String sourceCRS, final boolean fillGrid ) throws GeoGridException
  {
    try
    {
      final FileChannel channel = FileChannel.open( file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE );
      return new BinaryGeoGrid( channel, sizeX, sizeY, scale, origin, offsetX, offsetY, sourceCRS, fillGrid );
    }
    catch( final IOException e )
    {
      throw new GeoGridException( "Could not find binary grid file: " + file.getAbsolutePath(), e );
    }
  }

  /**
   * @param binFile
   *          If set, this file will be deleted on dispose
   */
  protected BinaryGeoGrid( final FileChannel channel, final File binFile, final Coordinate origin, final Coordinate offsetX, final Coordinate offsetY, final String sourceCRS ) throws IOException
  {
    super( origin, offsetX, offsetY, sourceCRS );

    m_binFile = binFile;
    m_channel = channel;

    /* Read header */
    m_channel.position( 0 );

    m_header = BinaryGeoGridHeader.read( m_channel );

    m_readBuffer = ByteBuffer.allocate( 4 * getSizeX() * BUFFER_LINES );
    m_readBuffer.order( ByteOrder.BIG_ENDIAN );

    /* mark buffer as not writable */
    // TODO: prohibits that a grid may be opened for reading AND writing; we should give this information from outside
    m_writeBuffer = null;

    /* Read statistical data */
    readStatistically();
  }

  /**
   * @param fillGrid
   *          If set to <code>true</code>, the grid will be initially filled with no-data values. Else, the grid values
   *          are undetermined.
   */
  public BinaryGeoGrid( final FileChannel channel, final int sizeX, final int sizeY, final int scale, final Coordinate origin, final Coordinate offsetX, final Coordinate offsetY, final String sourceCRS, final boolean fillGrid ) throws GeoGridException
  {
    super( origin, offsetX, offsetY, sourceCRS );

    m_readBuffer = ByteBuffer.allocate( 4 * sizeX * BUFFER_LINES );
    m_readBuffer.order( ByteOrder.BIG_ENDIAN );

    /* create write buffer, also marks this grid as writable */
    m_writeBuffer = ByteBuffer.allocate( 4 );
    m_writeBuffer.order( ByteOrder.BIG_ENDIAN );

    try
    {
      m_channel = channel;
      m_binFile = null;

      m_header = new BinaryGeoGridHeader( sizeX, sizeY, scale );

      m_unscaledMin = null;
      m_unscaledMax = null;

      /* Initialize grid */
      // m_randomAccessFile.setLength( HEADER_SIZE + sizeX * sizeY * 4 + 2 * 4 );
      m_channel.truncate( BinaryGeoGridHeader.HEADER_SIZE + sizeX * sizeY * 4 + 2 * 4 );

      /* Read header */
      m_channel.position( 0 );
      m_header.write( m_channel );

      /* Set everything to non-data */
      if( fillGrid )
      {
        final ByteBuffer buffer = ByteBuffer.allocate( sizeX * 4 );
        for( int y = 0; y < sizeY; y++ )
        {
          buffer.rewind();
          for( int x = 0; x < sizeX; x++ )
            buffer.putInt( NO_DATA );
          m_channel.write( buffer );
        }
      }

      /* Read statistical data */
      saveStatistically();
    }
    catch( final IOException e )
    {
      throw new GeoGridException( "Failed to initiate random access file", e );
    }
  }

  @Override
  public int getSizeX( )
  {
    return m_header.getSizeX();
  }

  @Override
  public int getSizeY( )
  {
    return m_header.getSizeY();
  }

  @Override
  public double getValue( final int x, final int y ) throws GeoGridException
  {
    if( m_channel == null )
      return Double.NaN;

    try
    {
      /* try fetch from buffer */
      if( m_bufferPosition != -1 )
      {
        final long position = calculatePosition( x, y );
        /* is current position within buffered data? */
        if( position >= m_bufferPosition && position < m_bufferPosition + m_readBuffer.capacity() )
        {
          /* hit! fetch from buffer */
          final long bufferPosition = position - m_bufferPosition;

          final int intVal = m_readBuffer.getInt( (int)bufferPosition );

          return scaleValue( intVal );
        }
      }

      /* read values into buffer at the current position (x/y) and return its first value */
      m_readBuffer.rewind();

      m_bufferPosition = seekValue( x, y );
      m_channel.read( m_readBuffer );

      final int intVal = m_readBuffer.getInt( 0 );

      return scaleValue( intVal );
    }
    catch( final IOException e )
    {
      final String msg = String.format( "Could not access grid-value %d/%d", x, y );
      throw new GeoGridException( msg, e );
    }
  }

  /**
   * Sets the value of a grid cell. The given value is scaled to the scale of this grid.
   * 
   * @throws DoubleGridException
   *           If the grid is not opened for write access.
   * @see org.kalypso.gis.doubleraster.grid.DoubleGrid#getValue(int, int)
   */
  @Override
  public void setValue( final int x, final int y, final double value ) throws GeoGridException
  {
    try
    {
      if( Double.isNaN( value ) )
        m_writeBuffer.putInt( 0, NO_DATA );
      else
      {
        final int unscaledValue = unscaleValue( value );

        /* update min/max */
        /* REMARK: statistic data written on close... */
        updateStatistics( unscaledValue );

        m_writeBuffer.putInt( 0, unscaledValue );
      }

      m_writeBuffer.rewind();

      seekValue( x, y );
      m_channel.write( m_writeBuffer );

    }
    catch( final IOException e )
    {
      final String msg = String.format( "Could not write grid-value %d/%d", x, y );
      throw new GeoGridException( msg, e );
    }
  }

  private void updateStatistics( final int unscaledValue )
  {
    /* update min */
    if( m_unscaledMin == null )
      m_unscaledMin = unscaledValue;
    else
      m_unscaledMin = Math.min( m_unscaledMin, unscaledValue );

    /* update max */
    if( m_unscaledMax == null )
      m_unscaledMax = unscaledValue;
    else
      m_unscaledMax = Math.max( m_unscaledMax, unscaledValue );
  }

  private long seekValue( final int x, final int y ) throws IOException
  {
    Assert.isTrue( x >= 0 && x < getSizeX() );
    Assert.isTrue( y >= 0 && y < getSizeY() );

    final long pos = calculatePosition( x, y );

    m_channel.position( pos );

    return pos;
  }

  private long calculatePosition( final int x, final int y )
  {
    final long pos = y * getSizeX() * 4 + x * 4 + BinaryGeoGridHeader.HEADER_SIZE;
    return pos;
  }

  @Override
  public void dispose( )
  {
    try
    {
      close();
    }
    catch( final IOException | GeoGridException e )
    {
      // We eat this exception, it should rarely occur and as this file is nor more used should be no error for the user
      e.printStackTrace();
    }

    /* if bin file is marked for deletion on close, do it now */
    if( m_binFile != null )
      m_binFile.delete();
  }

  @Override
  public void close( ) throws IOException, GeoGridException
  {
    if( m_channel != null )
    {
      if( isWriteEnabled() )
        saveStatistically();

      m_channel.close();
      m_channel = null;
    }
  }

  private boolean isWriteEnabled( )
  {
    return m_writeBuffer != null;
  }

  /**
   * Returns the minimum of all values of this grid.
   */
  @Override
  public BigDecimal getMin( )
  {
    if( m_unscaledMin == null )
      return BigDecimal.valueOf( Double.MAX_VALUE );

    final BigInteger bigInt = new BigInteger( m_unscaledMin.toString() );
    return new BigDecimal( bigInt, m_header.getScale() );
  }

  /**
   * Returns the maximum of all values of this grid.
   */
  @Override
  public BigDecimal getMax( )
  {
    if( m_unscaledMax == null )
      return BigDecimal.valueOf( -Double.MAX_VALUE );

    final BigInteger bigInt = new BigInteger( m_unscaledMax.toString() );
    return new BigDecimal( bigInt, m_header.getScale() );
  }

  /**
   * Gets the statistically values of this grid.
   * 
   * @throws IOException
   *           If the file position is not valid.
   */
  private void readStatistically( ) throws IOException
  {
    final long pos = BinaryGeoGridHeader.HEADER_SIZE + getSizeX() * getSizeY() * 4;

    final ByteBuffer buffer = ByteBuffer.allocate( 8 );

    m_channel.position( pos );
    m_channel.read( buffer );

    m_unscaledMin = buffer.getInt( 0 );
    m_unscaledMax = buffer.getInt( 4 );
  }

  private void saveStatistically( ) throws GeoGridException
  {
    final BigDecimal min = getMin();
    final BigDecimal max = getMax();

    try
    {
      m_unscaledMin = unscaleValue( min.doubleValue() );
      m_unscaledMax = unscaleValue( max.doubleValue() );

      /* directly write into buffer */
      final long pos = BinaryGeoGridHeader.HEADER_SIZE + getSizeX() * getSizeY() * 4;

      final ByteBuffer buffer = ByteBuffer.allocate( 8 );

      buffer.putInt( m_unscaledMin );
      buffer.putInt( m_unscaledMax );

      m_channel.position( pos );
      buffer.rewind();
      m_channel.write( buffer );
    }
    catch( final IOException e )
    {
      throw new GeoGridException( "Failed to set statistical data", e );
    }
  }

  private double scaleValue( final int value )
  {
    if( value == NO_DATA )
      return Double.NaN;

    return value / m_header.getScaleFactor();
  }

  private int unscaleValue( final double doubleValue )
  {
    return (int)(doubleValue * m_header.getScaleFactor());
  }
}