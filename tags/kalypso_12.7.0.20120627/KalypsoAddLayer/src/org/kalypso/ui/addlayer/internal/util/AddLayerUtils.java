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
package org.kalypso.ui.addlayer.internal.util;

import java.net.URL;

import org.apache.commons.httpclient.URIException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.net.UrlResolver;
import org.kalypso.ogc.gml.IKalypsoLayerModell;

/**
 * @author Gernot Belger
 */
public final class AddLayerUtils
{
  private AddLayerUtils( )
  {
    throw new UnsupportedOperationException();
  }

  public static String makeRelativeOrProjectRelative( final IPath mapPath, final IPath path )
  {
    if( path == null )
      return null;

    if( mapPath == null )
      return path.toPortableString();

    final IPath mapFolderPath = mapPath.removeLastSegments( 1 );

    final IPath relativeStylePath = path.makeRelativeTo( mapFolderPath );

    /* No '..': path is nicely relative to map */
    if( !"..".equals( relativeStylePath.segment( 0 ) ) ) //$NON-NLS-1$
      return relativeStylePath.toPortableString();

    /* map and path are in same project, use project: style notation */
    final IPath projectPath = mapPath.uptoSegment( 1 );
    if( projectPath.isPrefixOf( path ) )
      return UrlResolver.createProjectPath( path );

    try
    {
      /* No in same project: make platform path */
      return ResourceUtilities.createURLSpec( path );
    }
    catch( final URIException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  public static IPath getPathForMap( final IKalypsoLayerModell mapModell )
  {
    final URL mapContext = mapModell.getContext();
    final IFile mapFile = ResourceUtilities.findFileFromURL( mapContext );
    if( mapFile == null )
      return null;

    return mapFile.getFullPath();
  }
}