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
package org.kalypso.commons.java.nio.file;

import java.nio.file.Path;

/**
 * @author Holger Albert
 */
public class PathUtilities
{
  private PathUtilities( )
  {
  }

  public static Path findPathToSegment( final Path path, final String segmentName )
  {
    final int nameCount = path.getNameCount();
    for( int i = 0; i < nameCount; i++ )
    {
      final Path fileNamePath = path.getName( i );
      final String fileName = fileNamePath.toString();
      if( !fileName.equals( segmentName ) )
        continue;

      final Path subpath = path.subpath( 0, i + 1 );
      return path.getRoot().resolve( subpath );
    }

    return null;
  }
}