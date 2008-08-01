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
package org.kalypso.contribs.java.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author kuch
 */
public class MyPrintStream
{

  private PrintStream m_stream;

  private java.io.DataOutputStream m_fileStream = null;

  public MyPrintStream( File output, PrintStream stream )
  {
    m_stream = stream;
    try
    {
      m_fileStream = new java.io.DataOutputStream( new BufferedOutputStream( new FileOutputStream( output ) ) );
    }
    catch( FileNotFoundException e )
    {
      e.printStackTrace();
    }

  }

  public void print( String message )
  {
    if( m_stream != null )
      m_stream.print( message );

    try
    {
      if( m_fileStream != null )
        m_fileStream.writeUTF( message );
    }
    catch( IOException e )
    {
      e.printStackTrace();
    }

  }

  public void println( String message )
  {
    if( m_stream != null )
      m_stream.println( message );

    try
    {
      if( m_fileStream != null )
        m_fileStream.writeUTF( message + "\n" );
    }
    catch( IOException e )
    {
      e.printStackTrace();
    }

  }

  public void dispose( )
  {
    try
    {
      m_fileStream.close();
    }
    catch( IOException e )
    {
      e.printStackTrace();
    }

    m_fileStream = null;
    m_stream = null;
  }
}
