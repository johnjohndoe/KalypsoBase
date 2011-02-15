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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.kalypso.shape.dbf.DBFField;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.dbf.DBaseFile;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.geometry.SHPEnvelope;
import org.kalypso.shape.shp.SHPException;
import org.kalypso.shape.shp.SHPFile;
import org.kalypso.shape.shx.SHXFile;
import org.kalypso.shape.shx.SHXRecord;

/**
 * Class representing an ESRI Shape File.
 * <p>
 * This is a modification of the <tt>ShapeFile</tt> class within the shpapi package of sfcorba2java project performed by
 * the EXSE-Working group of of the geogr. institute of the university of Bonn
 * (http://www.giub.uni-bonn.de/exse/results/welcome.html).
 * <p>
 * ------------------------------------------------------------------------
 * </p>
 * 
 * @version 17.10.2001
 * @author Andreas Poth
 */
public class ShapeFile
{
  private final String m_filePath;

  private final DBaseFile m_dbf;

  private final SHXFile m_shx;

  /**
   * aggregated Instance-variables
   */
  private final SHPFile m_shp;

// private final RTree m_rti;

  /**
   * Create a new shape file and opens it for writing.<br>
   */
  public static ShapeFile create( final String basePath, final ShapeType shapeType, final Charset charset, final DBFField[] fields ) throws IOException, DBaseException
  {
    SHPFile.create( new File( basePath + ".shp" ), shapeType ).close();
    SHXFile.create( new File( basePath + ".shx" ), shapeType ).close();
    DBaseFile.create( new File( basePath + ".dbf" ), fields, charset ).close();

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

    m_shp = new SHPFile( new File( filePath + ".shp" ), mode );
    m_shx = new SHXFile( new File( filePath + ".shx" ), mode );
    m_dbf = new DBaseFile( new File( filePath + ".dbf" ), mode, charset );
// m_rti = initRTreeFile( filePath + ".rti" );
  }

// private static RTree initRTreeFile( final String filePath )
// {
// try
// {
// return new RTree( filePath );
// }
// catch( final RTreeException e )
// {
// // Ignore: in most cases, the file is not present
// // e.printStackTrace();
// return null;
// }
// }

  public void close( ) throws IOException
  {
    m_shp.close();
    m_shx.close();
    m_dbf.close();

// if( m_rti != null )
// {
// try
// {
// m_rti.close();
// }
// catch( final RTreeException e )
// {
// final String msg = String.format( "Failed to close RTree" );
// throw new IOException( msg, e );
// }
// }
  }

  /**
   * returns the number of records within a shape-file <BR>
   */
  public int getNumRecords( )
  {
    return m_shx.getNumRecords();
  }

  /**
   * returns the minimum bounding rectangle of all geometries <BR>
   * within the shape-file
   */
  public SHPEnvelope getFileMBR( )
  {
    final SHPEnvelope mbr = m_shp.getMBR();

// final double xmin = mbr.west;
// final double xmax = mbr.east;
// final double ymin = mbr.south;
// final double ymax = mbr.north;
//
// return GeometryFactory.createGM_Envelope( xmin, ymin, xmax, ymax, null );
    return mbr;
  }

  /**
   * returns the minimum bound rectangle of RecNo'th Geometrie <BR>
   */
  public SHPEnvelope getEnvelope( final int recordIndex ) throws IOException
  {
    final SHXRecord record = m_shx.getRecord( recordIndex );
    return m_shp.getEnvelope( record );
  }

  /**
   * returns RecNo'th Geometrie <BR>
   */
  public ISHPGeometry getShape( final int recordIndex ) throws IOException
  {
    final SHXRecord record = m_shx.getRecord( recordIndex );
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

  public DBFField[] getFields( )
  {
    return m_dbf.getFields();
  }

  public void addFeature( final ISHPGeometry shape, final Object[] data ) throws IOException, DBaseException, SHPException
  {
    m_dbf.addRecord( data );
    final int numRecords = m_shx.getNumRecords();
    final SHXRecord record = m_shp.addShape( shape, numRecords );

    final SHPEnvelope mbr = shape.getEnvelope();
    m_shx.addRecord( record, mbr );
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