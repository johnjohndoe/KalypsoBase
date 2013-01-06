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
package org.kalypso.core;

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.kalypso.core.i18n.Messages;
import org.kalypso.core.preferences.IKalypsoCorePreferences;

/**
 * Accessor to preferences of this plug-in.
 * 
 * @author Gernot Belger
 */
public class KalypsoCorePreferences implements IKalypsoCorePreferences
{
  public static IPreferenceStore getStore( )
  {
    return KalypsoCorePlugin.getDefault().getPreferenceStore();
  }

  public static void initalizeDefaults( )
  {
    getStore().setDefault( PREFERENCE_MAP_KEEP_POSITION_ON_MOUSE_WHEEL, true );
    getStore().setDefault( PREFERENCE_MAP_INVERT_MOUSE_WHEEL_ZOOM, false );
  }

  /**
   * Gets the kalypso timezone (to be used to display any dates in the UI).<br>
   * The timezone is set in the user-preferences. If the preference is not set, the value of the system property
   * 'kalypso.timezone' will be used, or, if not set, the system timezone (see {@link TimeZone#getDefault()}). <br>
   * The user preferences can explicitly be set to:
   * <ul>
   * <li>OS_TIMEZONE: {@link TimeZone#getDefault() is always used}</li>
   * <li>CONFIG_TIMEZONE: timezone definition from config.ini (kalypso.timezone) is used (defaults to system timezone if not set)</li>
   * </ul>
   */
  public static TimeZone getTimeZone( )
  {
    final TimeZone timezone = getInternalTimeZone();

    /** we want to get rid of daylight saving times and only support GMT based time zones! */
    final String identifier = timezone.getID();
    if( !StringUtils.containsIgnoreCase( identifier, "gmt" ) ) //$NON-NLS-1$
    {
      final Calendar calendar = Calendar.getInstance();
      calendar.set( Calendar.MONTH, 0 ); // get offset from winter daylight saving time!
      final int offset = timezone.getOffset( calendar.getTimeInMillis() );

      return TimeZone.getTimeZone( String.format( "GMT%+d", offset ) ); //$NON-NLS-1$
    }

    return timezone;
  }

  private static TimeZone getInternalTimeZone( )
  {
    final KalypsoCorePlugin corePlugin = KalypsoCorePlugin.getDefault();
    final String timeZoneID = corePlugin.getPreferenceStore().getString( IKalypsoCorePreferences.DISPLAY_TIMEZONE );

    final String timezone;
    if( timeZoneID == null || timeZoneID.isEmpty() || IKalypsoCorePreferences.PREFS_CONFIG_TIMEZONE.equals( timeZoneID ) )
      timezone = System.getProperty( IKalypsoCoreConstants.CONFIG_PROPERTY_TIMEZONE, null );
    else
      timezone = timeZoneID;

    if( timezone == null || timezone.isEmpty() )
      return TimeZone.getDefault();

    try
    {
      return TimeZone.getTimeZone( timezone );
    }
    catch( final Exception e )
    {
      final IStatus status = new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), Messages.getString( "org.kalypso.core.KalypsoCorePlugin.warning_timezone", timezone ), e ); //$NON-NLS-1$
      corePlugin.getLog().log( status );

      return TimeZone.getDefault();
    }
  }

  public static boolean isMapKeepPositionOnWheel( )
  {
    final IPreferenceStore store = getStore();
    return store.getBoolean( PREFERENCE_MAP_KEEP_POSITION_ON_MOUSE_WHEEL );
  }

  public static boolean invertMapWheelZoom( )
  {
    final IPreferenceStore store = getStore();
    return store.getBoolean( PREFERENCE_MAP_INVERT_MOUSE_WHEEL_ZOOM );
  }
}