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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Helper class for {@link org.eclipse.core.runtime.IPath}.
 *
 * @author Gernot Belger
 */
public class PathUtils
{
  private PathUtils( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  /**
   * Creates a relativ path from one context-path and a (possible) relative child-path.<br>
   * If the context path is a prefix of the child, simply the child-path minus the context path is returned.<br>
   * If the two pathes have a common prefix, the relativ path is created by prefixing the postfix of this relative part
   * by as many '../' as needed in order to make it relative.<br>
   * Example: makeRelativ( "/Kollau-Erg/Basis/models", "/Kollau-Erg/Basis/grids/6040.asc9946.bin" ) returns
   * "../grids/6040.asc9946.bin".
   */
  public static IPath makeRelativ( final IPath context, final IPath child )
  {
    if( context.isPrefixOf( child ) )
      return child.removeFirstSegments( context.segmentCount() );

    final IPath relativUp = makeRelativ( context.removeLastSegments( 1 ), child );
    if( relativUp != null )
      return new Path( ".." ).append( relativUp );

    return null;
  }

}
