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
package org.kalypsodeegree;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.kalypso.contribs.eclipse.osgi.FrameworkUtilities;
import org.kalypso.grid.GeoGridUtilities;
import org.kalypso.grid.GeoGridUtilities.Interpolation;

/**
 * Helper for preferences of the KalypsoDeegree plugin.
 *
 * @author Gernot Belger
 */
public class KalypsoDeegreePreferences
{
  private static final String SYSTEM_PROPERTY_KALYPSO_DEFAULT_SRS = "kalypso.defaultSRS"; //$NON-NLS-1$

  /**
   * Defines the key for available coordinate systems.
   */
  private static final String SETTING_AVAILABLE_CRS = "org.kalypso.deegree.coordinate.systems"; //$NON-NLS-1$

  /**
   * Defines the default values for available coordinate systems.
   */
  public static final String AVAILABLE_CRS_DEFAULT_VALUE = "EPSG:3034;EPSG:3035;EPSG:3396;EPSG:3397;EPSG:3398;EPSG:3399;EPSG:4258;EPSG:4289;EPSG:4314;EPSG:4326;EPSG:31466;EPSG:31467;EPSG:31468;EPSG:31469;EPSG:25831;EPSG:25832;EPSG:25833;EPSG:32631;EPSG:32632;EPSG:32633;EPSG:22523"; //$NON-NLS-1$

  /**
   * Defines the key for the default coordinate system.
   */
  private static final String SETTING_CRS = "org.kalypso.deegree.coordinate.systems.default"; //$NON-NLS-1$

  /**
   * Defines the default value for the default coordinate system.<br/>
   * Should not used outside of this plug-in! Instead, always get the current system from the plug-in.
   */
  public static final String CRS_DEFAULT_VALUE = "EPSG:31467"; //$NON-NLS-1$

  /**
   * Defines the key for the RasterSymbolizer pixelResolution.
   */
  public static final String SETTING_RASTER_PAINTING_PIXEL_RESOLUTION = "org.kalypso.deegree.preferences.rasterpainting.pixelresolution"; //$NON-NLS-1$

  /**
   * Defines the default value for the pixel resolution.<br/>
   */
  private static final int RASTER_PAINTING_PIXEL_RESOLUTION_DEFAULT_VALUE = 2;

  /**
   * Defines the key for the RasterSymbolizer interpolation method.
   */
  public static final String SETTING_RASTER_PAINTING_INTERPOLATION_METHOD = "org.kalypso.deegree.preferences.rasterpainting.interpolation.method"; //$NON-NLS-1$

  /**
   * Defines the default value for the RasterSymbolizer interpolation method..<br/>
   */
  private static final GeoGridUtilities.Interpolation RASTER_PAINTING_INTERPOLATION_METHOD_DEFAULT_VALUE = Interpolation.nearest;

  public static IPreferenceStore getStore( )
  {
    return KalypsoDeegreePlugin.getDefault().getPreferenceStore();
  }

  static void initDefaults( final IPreferenceStore store )
  {
    final String defaultSrs = getDefaultCoordinateSystem();
    store.setDefault( SETTING_CRS, defaultSrs );

    store.setDefault( SETTING_AVAILABLE_CRS, AVAILABLE_CRS_DEFAULT_VALUE );

    store.setDefault( SETTING_RASTER_PAINTING_PIXEL_RESOLUTION, RASTER_PAINTING_PIXEL_RESOLUTION_DEFAULT_VALUE );
    store.setDefault( SETTING_RASTER_PAINTING_INTERPOLATION_METHOD, RASTER_PAINTING_INTERPOLATION_METHOD_DEFAULT_VALUE.name() );
  }

  public static String getCoordinateSystem( )
  {
    return getStore().getString( SETTING_CRS );
  }

  private static String getDefaultCoordinateSystem( )
  {
    return FrameworkUtilities.getProperty( SYSTEM_PROPERTY_KALYPSO_DEFAULT_SRS, CRS_DEFAULT_VALUE );
  }

  public static void setCoordinateSystem( final String srsName )
  {
    getStore().setValue( SETTING_CRS, srsName );
  }

  public static String getAvailableSrsNames( )
  {
    return getStore().getString( SETTING_AVAILABLE_CRS );
  }

  public static String[] getAvailableSrsNamesArray( )
  {
    final String preferenceCodes = getAvailableSrsNames();
    if( StringUtils.isBlank( preferenceCodes ) )
      return new String[] {};

    return preferenceCodes.split( ";" ); //$NON-NLS-1$
  }

  public static void setAvailableSrsNames( final String availableSrsNames )
  {
    getStore().setValue( SETTING_AVAILABLE_CRS, availableSrsNames );
  }

  public static int getRasterPaintingPixelResolution( )
  {
    return getStore().getInt( SETTING_RASTER_PAINTING_PIXEL_RESOLUTION );
  }

  public static Interpolation getRasterPaintingInterpolationMethod( )
  {
    final String methodName = getStore().getString( SETTING_RASTER_PAINTING_INTERPOLATION_METHOD );

    try
    {
      if( StringUtils.isBlank( methodName ) )
        return RASTER_PAINTING_INTERPOLATION_METHOD_DEFAULT_VALUE;

      return Interpolation.valueOf( methodName );
    }
    catch( final IllegalArgumentException e )
    {
      e.printStackTrace();
      return RASTER_PAINTING_INTERPOLATION_METHOD_DEFAULT_VALUE;
    }
  }
}