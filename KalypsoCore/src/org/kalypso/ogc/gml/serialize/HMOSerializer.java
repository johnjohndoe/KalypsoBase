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
package org.kalypso.ogc.gml.serialize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

/**
 * @author felipe maximino
 */
public class HMOSerializer
{
  private Formatter m_formatter;

  private final File m_hmoFile;

  public HMOSerializer( final File hmoFile ) throws IOException
  {
    m_hmoFile = hmoFile;
    init();
  }

  public void init( ) throws IOException
  {
    if( m_hmoFile.exists() )
    {
      m_hmoFile.delete();
    }

    m_formatter = null;
    try
    {
      m_formatter = new Formatter( m_hmoFile );
    }
    catch( final FileNotFoundException e )
    {
      if( m_formatter != null )
      {
        finish();
      }

      throw e;
    }
  }

  public void formatPoint( final int id, final double x, final double y, final double z )
  {
    m_formatter.format( Locale.US, "P:%10d%20.7f%20.7f%20.7f%n", id, x, y, z ); //$NON-NLS-1$
  }

  public void finish( )
  {
    m_formatter.flush();
    m_formatter.close();
  }

  public void formatTriangle( final int i, final int nodeID1, final int nodeID2, final int nodeID3 )
  {
    m_formatter.format( "D:%10d%10d%10d%10d%n", i, nodeID1, nodeID2, nodeID3 ); //$NON-NLS-1$
  }
}