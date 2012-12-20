/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.java.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.internal.resources.PlatformURLResourceConnection;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.io.RunAfterCloseOutputStream;

/**
 * <p>
 * Erzeugt aus einem String eine URL
 * </p>
 * <p>
 * Davor kann noch eine Token-Ersetzung stattfinden
 * </p>
 * TODO: untersuchen warum es auch org.kalypso.contribs.java.net.UrlUtilities gibt??? Marc.
 * 
 * @author belger
 */
@SuppressWarnings( "restriction" )
public class UrlResolver implements IUrlResolver
{
  public static final String PROJECT_PROTOCOLL = "project:"; //$NON-NLS-1$

  private final Properties m_replaceTokenMap = new Properties();

  private final UrlUtilities m_urlUtilities = new UrlUtilities();

  /**
   * <p>
   * Löst eine URL relativ zu einer anderen auf.
   * </p>
   * <p>
   * Also handles the pseudo protocol 'project:'. If project: ist specified in relativeURL, it tries to guess the project from the baseURL (e.g. the baseURL must be of the form platfrom:/resource/).
   * It then replaces project: by 'platform:/resource/ <projectname>/
   * </p>
   * 
   * @param baseURL
   * @param relativeURL
   * @throws MalformedURLException
   */
  @Override
  public URL resolveURL( final URL baseURL, final String relativeURL ) throws MalformedURLException
  {
    if( relativeURL.startsWith( PROJECT_PROTOCOLL ) )
    {
      if( baseURL == null )
        throw new MalformedURLException( "Cannot process protocol 'project:' without a valid base URL as context" ); //$NON-NLS-1$

      final IProject project = ResourceUtilities.findProjectFromURL( baseURL );
      if( project == null )
        throw new MalformedURLException( "Protocol 'project:' need a resource url as context" + "\n\turl=" + baseURL + "\n\trelativeURL=" + relativeURL ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      final String projectURL = PlatformURLResourceConnection.RESOURCE_URL_STRING + "/" + project.getName(); //$NON-NLS-1$
      final String relPath = relativeURL.substring( PROJECT_PROTOCOLL.length() + 1 ); //$NON-NLS-1$

      return new URL( projectURL + "/" + relPath ); //$NON-NLS-1$
    }
    else if( relativeURL.startsWith( "REMOTE=" ) ) //$NON-NLS-1$
    {
      // TODO Replace with "project:" case.
      // TODO This case here does not work anymore, because of variable resolving.
      /* @hack scenario data manager - project database global gml fragment */
      if( relativeURL.contains( "${PROJECT}" ) ) //$NON-NLS-1$
      {
        final IProject project = ResourceUtilities.findProjectFromURL( baseURL );
        final String myUrl = relativeURL.replaceAll( "\\$\\{PROJECT\\}", project.getName() ); //$NON-NLS-1$

        return new URL( myUrl.substring( 7 ) );
      }

      return new URL( relativeURL.substring( 7 ) );
    }

    if( relativeURL.startsWith( "/" ) )
    {
      return new URL( baseURL, relativeURL.substring( 1 ) );
    }

    return new URL( baseURL, relativeURL );
  }

  @Override
  public final Iterator<Entry<Object, Object>> getReplaceEntries( )
  {
    return m_replaceTokenMap.entrySet().iterator();
  }

  @Override
  public void addReplaceToken( final String key, final String value )
  {
    m_replaceTokenMap.setProperty( key, value );
  }

  /**
   * If URL denotes a location within the workspace, special handling is done. Else, we rely on {@link UrlUtilities}.
   */
  @Override
  public OutputStreamWriter createWriter( final URL url ) throws IOException
  {
    try
    {
      return m_urlUtilities.createWriter( url );
    }
    catch( final UnknownServiceException e )
    {
      final IFile file = ResourceUtilities.findFileFromURL( url );
      if( file != null )
      {
        final IPath path = file.getLocation();
        final File realFile = path.toFile();

        final Runnable runnable = new Runnable()
        {
          @Override
          public void run( )
          {
            try
            {
              file.refreshLocal( IResource.DEPTH_ONE, null );
            }
            catch( final CoreException ce )
            {
              // maybe there is better error handling than that?
              ce.printStackTrace();
            }
          }
        };

        if( !realFile.exists() )
        {
          realFile.createNewFile();
        }
        final OutputStream os = new RunAfterCloseOutputStream( new FileOutputStream( realFile ), runnable );

        String charset;
        try
        {
          charset = file.getCharset();
        }
        catch( final CoreException ce )
        {
          ce.printStackTrace();

          charset = null;
        }

        if( charset == null )
          return new OutputStreamWriter( os );

        return new OutputStreamWriter( os, charset );
      }

      throw e;
    }
  }

  /**
   * Ausnahmebehandlung von Platform URLs. In diesem Fall anhand der Workbench das encoding bestimmen.
   * 
   * @see org.kalypso.contribs.java.net.IUrlResolver#createReader(java.net.URL)
   */
  @Override
  public InputStreamReader createReader( final URL url ) throws IOException
  {
    try
    {
      final IFile file = ResourceUtilities.findFileFromURL( url );
      if( file != null )
      {
        final InputStream is = file.getContents();
        final String charset = file.getCharset();
        return new InputStreamReader( is, charset );
      }
    }
    catch( final CoreException e )
    {
      throw new IOException( e.getMessage() );
    }

    // wenn alles nichts hilfe, auf Standardzeug zurückgreifen
    return m_urlUtilities.createReader( url );
  }

  /**
   * Builds a path of the form 'project:/<projectRelativePath>'
   */
  public static String createProjectPath( final IPath absolutePath )
  {
    Assert.isTrue( absolutePath.isAbsolute() );

    final IPath pathWithoutProject = absolutePath.removeFirstSegments( 1 );
    return UrlResolver.PROJECT_PROTOCOLL + IPath.SEPARATOR + pathWithoutProject.toPortableString(); //$NON-NLS-1$
  }
}
