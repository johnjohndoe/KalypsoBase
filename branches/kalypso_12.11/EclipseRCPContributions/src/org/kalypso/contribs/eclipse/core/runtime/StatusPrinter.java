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
package org.kalypso.contribs.eclipse.core.runtime;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;

/**
 * Prints a {@link IStatus} and all of it's children into a {@link PrintWriter}.
 * 
 * @author Gernot Belger
 */
public class StatusPrinter
{
  public static int DEFAULT_INDENTATION_STEP = 2;

  private final DateFormat m_df = DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.SHORT );

  private final int m_indentation;

  private final PrintWriter m_pw;

  private int m_indentationStep = DEFAULT_INDENTATION_STEP;

  public static String toString( final IStatus status )
  {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter( sw );

    final StatusPrinter printer = new StatusPrinter( 0, pw );
    printer.print( status );

    pw.close();

    return sw.toString();
  }

  /**
   * @param indentation
   *          Number of space to put in front of each log message.
   */
  public StatusPrinter( final int indentation, final PrintWriter pw )
  {
    m_indentation = indentation;
    m_pw = pw;
  }

  /**
   * Overwrites the default indentation step (i.e. the amount of indentation added on each level).
   */
  public void setIndentationStep( final int indentationStep )
  {
    m_indentationStep = indentationStep;
  }

  public boolean print( final IStatus status )
  {
    final String severity = StatusUtilities.getLocalizedSeverity( status );

    if( status instanceof IStatusWithTime )
    {
      final Date time = ((IStatusWithTime)status).getTime();
      m_pw.print( m_df.format( time ) );
      m_pw.print( ' ' );
    }

    m_pw.print( StringUtils.repeat( ' ', m_indentation ) );
    m_pw.print( severity );
    m_pw.print( ": " );
    m_pw.println( status.getMessage() );

    final Throwable exception = status.getException();
    if( exception != null )
      m_pw.format( " (Exception: %s)", exception.getLocalizedMessage() );

    final IStatus[] children = status.getChildren();
    final StatusPrinter childrenPrinter = getChildPrinter();
    for( final IStatus child : children )
      childrenPrinter.print( child );

    m_pw.println();
    m_pw.flush();

    return true;
  }

  public StatusPrinter getChildPrinter( )
  {
    return new StatusPrinter( m_indentation + m_indentationStep, m_pw );
  }
}