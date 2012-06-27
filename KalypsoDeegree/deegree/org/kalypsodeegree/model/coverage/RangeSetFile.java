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
package org.kalypsodeegree.model.coverage;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;

/**
 * Binding class for gml:File from coverages.xsd.<br/>
 * Named as 'RangeSetFile' in order to avoid confusion with {@link java.io.File}.
 *
 * @author Gernot Belger
 */
public class RangeSetFile
{
  // REMARK: rangeParameters are not implemented
  // <element ref="gml:rangeParameters"/>

  public static final QName FILE_ELEMENT = new QName( NS.GML3, "File" ); //$NON-NLS-1$

  public static final QName PROPERTY_FILENAME = new QName( NS.GML3, "fileName" ); //$NON-NLS-1$

  public static final QName PROPERTY_FILESTRUCTURE = new QName( NS.GML3, "fileStructure" ); //$NON-NLS-1$

  public static final QName PROPERTY_MIMETYPE = new QName( NS.GML3, "mimeType" ); //$NON-NLS-1$

  public static final QName PROPERTY_COMPRESSION = new QName( NS.GML3, "compression" ); //$NON-NLS-1$

  private String m_fileName;

  private final String m_fileStructure = "Record Interleaved"; //$NON-NLS-1$

  private String m_mimeType;

  private String m_compression;

  public RangeSetFile( final String fileName )
  {
    m_fileName = fileName;
  }

  public String getMimeType( )
  {
    return m_mimeType;
  }

  public void setMimeType( final String mimeType )
  {
    m_mimeType = mimeType;
  }

  public String getCompression( )
  {
    return m_compression;
  }

  public void setCompression( final String compression )
  {
    m_compression = compression;
  }

  public String getFileName( )
  {
    return m_fileName;
  }

  public void setFileName( final String fileName )
  {
    m_fileName = fileName;
  }

  public String getFileStructure( )
  {
    return m_fileStructure;
  }
}
