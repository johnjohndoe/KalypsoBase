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
package org.kalypso.model.wspm.core.profil.sobek.parser;

import java.io.IOException;
import java.io.LineNumberReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;

/**
 * Helper code for parsing sobek files.
 * 
 * @author Gernot Belger
 */
public final class SobekParsing
{
  private SobekParsing( )
  {
    throw new UnsupportedOperationException();
  }

  public static CoreException throwError( final String message )
  {
    return doThrow( IStatus.ERROR, message );
  }

  private static CoreException doThrow( final int severity, final String message )
  {
    final IStatus status = new Status( severity, KalypsoModelWspmCorePlugin.getID(), message );
    return new CoreException( status );
  }

  public static void searchForEndToken( final String token, final SobekLineParser line, final LineNumberReader reader ) throws IOException, CoreException
  {
    SobekLineParser currentLine = line;
    while( true )
    {
      final String nextToken = currentLine.nextTokenOrNull();
      if( nextToken == null )
        currentLine = new SobekLineParser( reader );

      if( token.toLowerCase().equals( nextToken ) )
        return;
    }
  }
}