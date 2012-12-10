/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.contribs.java.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Decorates an {@link java.io.OutputStream}and runs a {@link java.lang.Runnable}when the {@link #close()}Methdo is
 * called.}
 * 
 * @author belger
 */
public class RunAfterCloseOutputStream extends OutputStream
{
  private final OutputStream m_os;

  private final Runnable m_runAfterClose;

  public RunAfterCloseOutputStream( final OutputStream os, final Runnable runAfterClose )
  {
    m_os = os;
    m_runAfterClose = runAfterClose;
  }

  /**
   * @see java.io.OutputStream#close()
   */
  @Override
  public void close() throws IOException
  {
    try
    {
      super.close();

      m_os.close();
    }
    finally
    {
      m_runAfterClose.run();
    }
  }

  /**
   * All write methods are overwritten, to use performance improvements of decorated streeam.
   * 
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public void write( final int b ) throws IOException
  {
    m_os.write( b );
  }

  /**
   * All write methods are overwritten, to use performance improvements of decorated streeam.
   * 
   * @see java.io.OutputStream#write(byte[])
   */
  @Override
  public void write( byte[] b ) throws IOException
  {
    m_os.write( b );
  }

  /**
   * All write methods are overwritten, to use performance improvements of decorated streeam.
   * 
   * @see java.io.OutputStream#write(byte[], int, int)
   */
  @Override
  public void write( byte[] b, int off, int len ) throws IOException
  {
    m_os.write( b, off, len );
  }

}
