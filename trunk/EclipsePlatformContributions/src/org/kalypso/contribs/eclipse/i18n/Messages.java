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
package org.kalypso.contribs.eclipse.i18n;

import java.util.IllegalFormatException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Nico Schrage
 */
public final class Messages
{
  private static final String BUNDLE_NAME = "org.kalypso.contribs.eclipse.i18n.messages"; //$NON-NLS-1$

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

  private static final Object[] NO_ARGS = new Object[0];

  private Messages( )
  {
  }

/*
 * java reflections needs this method-signatur
 */
  public static String getString( final String key )
  {
    return getString( key, NO_ARGS );
  }

  public static String getString( final String key, final Object... args )
  {
    String formatStr = ""; //$NON-NLS-1$
    try
    {
      formatStr = RESOURCE_BUNDLE.getString( key );
      if( args.length == 0 )
        return formatStr;

      return String.format( formatStr, args );
    }
    catch( final MissingResourceException e )
    {
      return '!' + key + '!';
    }
    catch( final IllegalFormatException e )
    {
      e.printStackTrace();
      return '!' + formatStr + '!';
    }
  }
}