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
package org.kalypso.utils.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.osgi.framework.Bundle;

/**
 * A logger that writes live to a file.
 * 
 * @author Holger Albert
 */
public class FileLog implements ILog
{
  /**
   * The date format.
   */
  private DateFormat m_df;

  /**
   * The log file. May not be null.
   */
  private File m_file;

  /**
   * The constructor.
   * 
   * @param file
   *          The log file. May not be null.
   */
  public FileLog( File file )
  {
    /* The log file may not be null. */
    Assert.isNotNull( file );

    m_file = file;
    m_df = DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.SHORT );
  }

  /**
   * @see org.eclipse.core.runtime.ILog#addLogListener(org.eclipse.core.runtime.ILogListener)
   */
  @Override
  public void addLogListener( ILogListener listener )
  {
  }

  /**
   * @see org.eclipse.core.runtime.ILog#removeLogListener(org.eclipse.core.runtime.ILogListener)
   */
  @Override
  public void removeLogListener( ILogListener listener )
  {
  }

  /**
   * @see org.eclipse.core.runtime.ILog#getBundle()
   */
  @Override
  public Bundle getBundle( )
  {
    return null;
  }

  /**
   * @see org.eclipse.core.runtime.ILog#log(org.eclipse.core.runtime.IStatus)
   */
  @Override
  public void log( IStatus status )
  {
    /* Append one line. */
    appendLine( m_file, status );
  }

  /**
   * This function appends one line to the file. From a multi status only the parent message will be appended.
   * 
   * @param file
   *          The log file.
   * @param status
   *          The status.
   */
  private void appendLine( File file, IStatus status )
  {
    BufferedWriter writer = null;

    try
    {
      writer = new BufferedWriter( new FileWriter( file, true ) );
      writer.write( String.format( "%S - %s%n", m_df.format( new Date() ), status.getMessage() ) );
    }
    catch( Exception ex )
    {
      ex.printStackTrace();
    }
    finally
    {
      IOUtils.closeQuietly( writer );
    }
  }
}