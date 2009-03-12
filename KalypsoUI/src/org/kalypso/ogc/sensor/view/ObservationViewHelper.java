/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
  
---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.view;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.kalypso.repository.IRepositoryItem;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.preferences.IKalypsoPreferences;
import org.kalypso.util.runtime.args.DateRangeArgument;

/**
 * ObservationViewHelper
 * 
 * @author schlienger
 */
public final class ObservationViewHelper
{
  private ObservationViewHelper()
  {
  // not to be instanciated
  }

  /**
   * Makes a DateRangeArgument using the properties of the repository into which
   * the item belongs. If the property is not defined, it uses the Kalypso
   * Preferences.
   * 
   * @param item
   * @return new instance of DateRangeArgument
   */
  public static DateRangeArgument makeDateRange( final IRepositoryItem item )
  {
    final DateFormat df = DateFormat.getDateTimeInstance();

    if( item.getRepository().getProperty( IKalypsoPreferences.USE_RANGE ) != null )
    {
      final boolean useRange = Boolean.valueOf(
          item.getRepository().getProperty( IKalypsoPreferences.USE_RANGE ) ).booleanValue();

      if( useRange )
      {
        try
        {
          final Date dateFrom = df.parse( item.getRepository().getProperty(
              IKalypsoPreferences.DATE_FROM ) );

          final Date dateTo = df.parse( item.getRepository().getProperty(
              IKalypsoPreferences.DATE_TO ) );

          return new DateRangeArgument( dateFrom, dateTo );
        }
        catch( ParseException e )
        {
          return new DateRangeArgument();
        }
      }

      final String strDays = item.getRepository().getProperty( IKalypsoPreferences.NUMBER_OF_DAYS );

      return DateRangeArgument.createFromPastDays( Integer.valueOf( strDays ).intValue() );
    }

    final boolean useRange = KalypsoGisPlugin.getDefault().getPluginPreferences().getBoolean(
        IKalypsoPreferences.USE_RANGE );

    if( useRange )
    {
      try
      {
        final Date dateFrom = df.parse( KalypsoGisPlugin.getDefault().getPluginPreferences()
            .getString( IKalypsoPreferences.DATE_FROM ) );

        final Date dateTo = df.parse( KalypsoGisPlugin.getDefault().getPluginPreferences()
            .getString( IKalypsoPreferences.DATE_TO ) );

        return new DateRangeArgument( dateFrom, dateTo );
      }
      catch( ParseException e )
      {
        return new DateRangeArgument();
      }
    }

    final int days = KalypsoGisPlugin.getDefault().getPluginPreferences().getInt(
        IKalypsoPreferences.NUMBER_OF_DAYS );

    return DateRangeArgument.createFromPastDays( days );
  }
}