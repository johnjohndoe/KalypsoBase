/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and Coastal Engineering
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
package org.kalypso.ogc.sensor;

/**
 * Utility methods for working with ObsView objects
 * 
 * @author belger
 * @author schlienger
 */
public final class ObservationTokenHelper
{
  public static final String TOKEN_AXISUNIT = "%axisunit%"; //$NON-NLS-1$

  public static final String TOKEN_AXISTYPE = "%axistype%"; //$NON-NLS-1$

  public static final String TOKEN_AXISNAME = "%axisname%"; //$NON-NLS-1$

  public static final String TOKEN_OBSNAME = "%obsname%"; //$NON-NLS-1$

  public static final String DEFAULT_ITEM_NAME = "%axistype% - %obsname%"; //$NON-NLS-1$

  public static final String TOKEN_AXISNAME_OBSNAME = "%axisname% - %obsname%"; //$NON-NLS-1$

  public static final String TOKEN_AXISNAME_AXISUNIT = "%axisname% [%axisunit%]"; //$NON-NLS-1$

  private ObservationTokenHelper( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  /**
   * Replace tokens in Format-String
   * <dl>
   * <dt>%obsname%</dt>
   * <dd>Name der Observation: obs.getName()</dd>
   * <dt>%axisname%</dt>
   * <dd>Name der Wert-Achse: axis.getName()</dd>
   * <dt>%axistype%</dt>
   * <dd>Typ der Wert-Achse: axis.getType()</dd>
   * <dt>%axisunit%</dt>
   * <dd>Einheit der Wert-Achse: axis.getUnit()</dd>
   * </dl>
   */
  public static String replaceTokens( final String formatString, final IObservation obs, final IAxis axis )
  {
    String result = formatString;

    // observation
    if( obs != null )
      result = result.replaceAll( TOKEN_OBSNAME, obs.getName() );

    // axis
    if( axis != null )
    {
      result = result.replaceAll( TOKEN_AXISNAME, axis.getName() );
      result = result.replaceAll( TOKEN_AXISTYPE, axis.getType() );
      result = result.replaceAll( TOKEN_AXISUNIT, axis.getUnit() );
    }

    // Metadata
    if( obs != null )
    {
      int index = 0;
      while( index < result.length() - 1 )
      {
        final int start = result.indexOf( "%metadata-", index ); //$NON-NLS-1$
        if( start == -1 )
          break;

        final int stop = result.indexOf( '%', start + 1 );
        if( stop != -1 )
        {
          final String metaname = result.substring( start + "%metadata-".length(), stop ); //$NON-NLS-1$
          final StringBuffer sb = new StringBuffer( result );

          final String metaval = obs.getMetadataList().getProperty( metaname, "<Metavalue '" + metaname + "' not found>" ); //$NON-NLS-1$ //$NON-NLS-2$
          sb.replace( start, stop + 1, metaval );

          result = sb.toString();
        }

        index = stop + 1;
      }
    }

    return result;
  }
}
