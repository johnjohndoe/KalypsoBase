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
package org.kalypso.contribs.java.i18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

/**
 * Wraps a {@link java.util.ResourceBundle} and provides the standard accessor method for a translated string.
 *
 * @author Gernot Belger
 */
public class I18NBundle
{
  private final ResourceBundle m_bundle;

  public I18NBundle( final ResourceBundle bundle )
  {
    m_bundle = bundle;
  }

  /**
   * Translates a potential i18n string to its value. If the string starts with '%s' it is reslved against the resource
   * bundle; else it will be returned as is.
   */
  public String translate( final String i18nString )
  {
    if( StringUtils.isEmpty( i18nString ) )
      return StringUtils.EMPTY;

    if( StringUtils.isEmpty( i18nString ) )
      return StringUtils.EMPTY;

    if( !(i18nString.charAt( 0 ) == '%') )
      return i18nString;

    final String key = i18nString.substring( 1 );
    if( m_bundle == null )
      return String.format( "Missing resource bundle for key '%s'", key );

    try
    {
      return m_bundle.getString( key );
    }
    catch( final MissingResourceException e )
    {
      return String.format( "!%s!", key ); //$NON-NLS-1$
    }
  }
}