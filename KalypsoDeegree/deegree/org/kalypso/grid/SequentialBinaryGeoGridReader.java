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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.deegree.model.spatialschema.ByteUtils;
import org.eclipse.core.runtime.Assert;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;

/**
 * @author barbarins
 *
 */
public class SequentialBinaryGeoGridReader extends AbstractDelegatingGeoGrid
{
  private BufferedInputStream m_gridStream;
  private int m_scale;

  public SequentialBinaryGeoGridReader( final IGeoGrid inputGrid, final URL pUrl) throws IOException
  {
    super(inputGrid);
    
    /* Tries to find a file from the given url. */
    File fileFromUrl = ResourceUtilities.findJavaFileFromURL( pUrl );
    if( fileFromUrl == null )
      fileFromUrl = FileUtils.toFile( pUrl );

    m_gridStream = new BufferedInputStream( new FileInputStream(fileFromUrl) );
    
    
    // TODO: Dispose and/or flush?
    
    
    // skip header
    /* Read header */
    m_gridStream.skip( 12 );
    byte[] lScaleBuff = new byte[4];
    m_gridStream.read(lScaleBuff, 0, 4);
    m_scale = ByteUtils.readBEInt( lScaleBuff , 0 )  ;

    
  }

  public void read( byte[] blockData, long l ) throws IOException
  {
    m_gridStream.read( blockData, 0, (int) l );
  }

  public int getScale( )
  {
    return m_scale;
  }

  public void close( ) throws IOException
  {
    m_gridStream.close();
  }

}
