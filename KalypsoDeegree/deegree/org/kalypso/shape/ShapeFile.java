/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypso.shape;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.dbf.DBaseFile;
import org.kalypso.shape.dbf.IDBFField;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.geometry.SHPEnvelope;
import org.kalypso.shape.shp.SHPException;
import org.kalypso.shape.shp.SHPFile;
import org.kalypso.shape.shx.SHXFile;
import org.kalypso.shape.shx.SHXRecord;

/**
 * Class representing an ESRI Shape File.
 * <p>
 * This is a modification of the <tt>ShapeFile</tt> class within the shpapi package of sfcorba2java project performed by the EXSE-Working group of of the geogr. institute of the university of Bonn
 * (http://www.giub.uni-bonn.de/exse/results/welcome.html).
 * <p>
 * ------------------------------------------------------------------------
 * </p>
 * 
 * @version 17.10.2001
 * @author Andreas Poth
 */
public class ShapeFile implements Closeable
{
  public static final String EXTENSION_DBF = ".dbf"; //$NON-NLS-1$

  public static final String EXTENSION_SHX = ".shx"; //$NON-NLS-1$

  public static final String EXTENSION_SHP = ".shp"; //$NON-NLS-1$

  private final String m_filePath;

  private final DBaseFile m_dbf;

  private SHXFile m_shx;

  /**
   * aggregated Instance-variables
   */
  private final SHPFile m_shp;

  /**
   * Create a new shape file and opens it for writing.<br>
   */
  public static ShapeFile create( final String basePath, final ShapeType shapeType, final Charset charset, final IDBFField[] fields ) throws IOException, DBaseException
  {
    SHPFile.create( new File( basePath + EXTENSION_SHP ), shapeType ).close();
    SHXFile.create( new File( basePath + EXTENSION_SHX ), shapeType ).close();
    DBaseFile.create( new File( basePath + EXTENSION_DBF ), fields, charset ).close();

    return new ShapeFile( basePath, charset, FileMode.WRITE );
  }

  /**
   * Open ShapeFile for reading.
   * 
   * @param filePath
   *          absolute filePath to the .shp file.
   */
  public ShapeFile( final String filePath, final Charset charset, final FileMode mode ) throws IOException, DBaseException
  {
    m_filePath = filePath;

    m_shp = new SHPFile( new File( filePath + EXTENSION_SHP ), mode );
    m_dbf = new DBaseFile( new File( filePath + EXTENSION_DBF ), mode, charset );
  }

  @Override
  public void close( ) throws IOException
  {
    if( m_shp != null )
      m_shp.close();

    if( m_dbf != null )
      m_dbf.close();

    if( m_shx != null )
      m_shx.close();
  }

  /**
   * Lazily open and read the shx file
   */
  private SHXFile getSHX( ) throws IOException
  {
    final FileMode mode = m_shp.getMode();

    if( m_shx == null )
      m_shx = new SHXFile( new File( m_filePath + EXTENSION_SHX ), mode );

    return m_shx;
  }

  /**
   * returns the number of records within a shape-file <BR>
   */
  public int getNumRecords( ) throws IOException
  {
    return getSHX().getNumRecords();
  }

  /**
   * returns the minimum bounding rectangle of all geometries <BR>
   * within the shape-file
   */
  public SHPEnvelope getFileMBR( )
  {
    return m_shp.getMBR();
  }

  /**
   * returns the minimum bound rectangle of RecNo'th Geometrie <BR>
   */
  public SHPEnvelope getEnvelope( final int recordIndex ) throws IOException
  {
    final SHXRecord record = getSHX().getRecord( recordIndex );
    return m_shp.getEnvelope( record );
  }

  /**
   * returns RecNo'th geometry
   */
  public ISHPGeometry getShape( final int recordIndex ) throws IOException
  {
    final SHXRecord record = getSHX().getRecord( recordIndex );
    return m_shp.getShape( record );
  }

  /**
   * returns a row of the dBase-file <BR>
   * associated to the shape-file <BR>
   */
  public Object[] getRow( final int rowNo ) throws DBaseException, IOException
  {
    return m_dbf.getRecord( rowNo );
  }

  /**
   * Reads a row of data of the shape file and writes it into the given container.
   * 
   * @return <code>false</code>, if the row is marked as deleted.
   */
  public boolean readRow( final int rowNo, final Object[] container ) throws DBaseException, IOException
  {
    return m_dbf.readRecord( rowNo, container );
  }

  public IDBFField[] getFields( )
  {
    return m_dbf.getFields();
  }

  public Object getRowValue( final int rowNo, final String fieldName ) throws DBaseException, IOException
  {
    return m_dbf.getValue( rowNo, fieldName );
  }

  public void addFeature( final ISHPGeometry shape, final Object[] data ) throws IOException, DBaseException, SHPException
  {
    m_dbf.addRecord( data );

    final SHXFile shx = getSHX();

    final int numRecords = shx.getNumRecords();
    final SHXRecord record = m_shp.addShape( shape, numRecords );

    final SHPEnvelope mbr = shape.getEnvelope();
    shx.addRecord( record, mbr );
  }

  public ShapeType getShapeType( )
  {
    return m_shp.getShapeType();
  }

  public String getFileBase( )
  {
    return m_filePath;
  }
}