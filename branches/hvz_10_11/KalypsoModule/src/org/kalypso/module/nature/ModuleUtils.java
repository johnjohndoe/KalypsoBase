/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.module.nature;

import org.osgi.framework.Version;

/**
 * @author Gernot Belger
 */
public final class ModuleUtils
{
  private ModuleUtils( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  /** Parses the module version from its perferences. */
  public static Version parseVersion( final String property )
  {
    try
    {
      return Version.parseVersion( property );
    }
    catch( final IllegalArgumentException e )
    {
      e.printStackTrace();
      return Version.emptyVersion;
    }
  }

  public static Version removeQualifier( final Version version )
  {
    if( version == null )
      return null;

    final String qualifier = version.getQualifier();
    if( "qualifier".equals( qualifier ) ) //$NON-NLS-1$
      return new Version( version.getMajor(), version.getMinor(), version.getMicro() );

    return version;
  }

}
