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
package org.kalypso.contribs.eclipse.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;

/**
 * @author Dirk Kuch
 */
public class StringFileReplacer implements ICoreRunnableWithProgress
{
  private final IFile m_src;

  private final IFile m_destination;

  private final Map<String, String> m_replacements;

  public StringFileReplacer( final IFile src, final IFile destination, final Map<String, String> replacements )
  {
    m_src = src;
    m_destination = destination;
    m_replacements = replacements;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      final BufferedReader in = new BufferedReader( new InputStreamReader( m_src.getContents(), "UTF8" ) ); //$NON-NLS-1$
      if( in == null )
        return StatusUtilities.createErrorStatus( String.format( "Couldn't find src file: %s", m_src.getFullPath().toString() ) );

      final StringBuffer content = new StringBuffer( "" ); //$NON-NLS-1$
      String line;

      while( (line = in.readLine()) != null )
        content.append( line + System.getProperty( "line.separator", "\n" ) );

      in.close();

      String text = content.toString();
      final Set<Entry<String, String>> entrySet = m_replacements.entrySet();

      for( final Entry<String, String> entry : entrySet )
        text = text.replaceAll( entry.getKey(), entry.getValue() );

      final BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( m_destination.getLocation().toFile() ), "UTF8" ) ); //$NON-NLS-1$
      writer.write( text );
      writer.close();

      m_destination.refreshLocal( IResource.DEPTH_ONE, monitor );
    }
    catch( final Exception e )
    {
      throw new CoreException( StatusUtilities.createErrorStatus( "Replacing strings failed", e ) );
    }

    return Status.OK_STATUS;
  }
}
