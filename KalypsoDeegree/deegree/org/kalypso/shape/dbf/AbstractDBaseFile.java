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
package org.kalypso.shape.dbf;

import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.Charset;

import org.kalypso.shape.FileMode;

/**
 * Abstract implementation that only reads the file header and abstracts some basic properties.<br/>
 * Can be used for random access or serial reading of a dbase file.<br/>
 * <br/>
 * DBase III File. see http://www.clicketyclick.dk/databases/xbase/format/dbf.html<br>
 * The datatypes of the dBase file and their representation as java types: <br>
 * dBase-type dBase-type-ID java-type <br>
 * character "C" String <br>
 * float "F" Float <br>
 * number "N" Double <br>
 * logical "L" String <br>
 * memo "M" String <br>
 * date "D" Date <br>
 * binary "B" ByteArrayOutputStream<br>
 * <br>
 * Original Author: Andreas Poth
 */
abstract class AbstractDBaseFile implements Closeable
{
  private final DataInput m_input;

  private final DBFHeader m_header;

  private final FileMode m_fileMode;

  private final Charset m_charset;

  /**
   * Constructs this class and reads the header from the input. The input must be positioned at the start of the file.
   */
  AbstractDBaseFile( final DataInput input, final FileMode mode, final Charset charset ) throws IOException, DBaseException
  {
    m_input = input;
    m_charset = charset;

    m_header = DBFHeader.read( input, charset );
    m_fileMode = mode;
  }

  DataInput getInput( )
  {
    return m_input;
  }

  FileMode getFileMode( )
  {
    return m_fileMode;
  }

  Charset getCharset( )
  {
    return m_charset;
  }

  DBFHeader getHeader( )
  {
    return m_header;
  }

  /**
   * method: getRecordNum() <BR>
   * Get the number of records in the table
   */
  public int getNumRecords( )
  {
    return m_header.getNumRecords();
  }

  public IDBFField[] getFields( )
  {
    return m_header.getFields().getFields();
  }

  // FIXME
  public int getIndex( final String field )
  {
    final IDBFField[] fields = getFields();
    for( int i = 0; i < fields.length; i++ )
    {
      final IDBFField dbfField = fields[i];
      if( dbfField.getName().equalsIgnoreCase( field ) )
        return i;
    }

    return -1;
  }

  // FIXME
  /**
   * Returns the index (with regard to the array return by {@link #getFields()} of the field with a specfic name.
   */
  public int findFieldIndex( final String fieldName )
  {
    final IDBFField[] fields = getFields();
    for( int i = 0; i < fields.length; i++ )
    {
      if( fieldName.equals( fields[i].getName() ) )
        return i;
    }

    return -1;
  }
}
