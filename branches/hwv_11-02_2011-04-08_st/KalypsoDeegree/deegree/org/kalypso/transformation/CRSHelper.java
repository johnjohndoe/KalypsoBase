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
package org.kalypso.transformation;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.Platform;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.preferences.IKalypsoDeegreePreferences;
import org.kalypso.transformation.crs.CoordinateSystemFactory;
import org.kalypso.transformation.crs.ICoordinateSystem;
import org.kalypsodeegree.KalypsoDeegreePlugin;

/**
 * This class is a helper for dealing with coordinate systems.
 * 
 * @author Holger Albert
 */
public class CRSHelper
{
  /**
   * The constructor.
   */
  private CRSHelper( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" ); //$NON-NLS-1$
  }

  /**
   * This function returns true, if the given code is one of the known coordinate systems.
   * 
   * @param code
   *          The code of the coordinate system to check.
   * @return True, if the coordinate system exists.
   */
  public static boolean isKnownCRS( String code )
  {
    if( code == null )
      return false;

    try
    {
      ICoordinateSystem coordinateSystem = CoordinateSystemFactory.getCoordinateSystem( code );
      if( coordinateSystem == null )
        return false;

      if( !coordinateSystem.isValid() )
        return false;

      return true;
    }
    catch( Exception e )
    {
      return false;
    }
  }

  /**
   * This function returns all known codes of coordinate systems.
   * 
   * @return An array of all known codes of coordinate systems.
   */
  public static String[] getAllNames( )
  {
    String preferenceCodes = Platform.getPreferencesService().getString( KalypsoDeegreePlugin.getID(), IKalypsoDeegreePreferences.AVAILABLE_CRS_SETTING, IKalypsoDeegreePreferences.AVAILABLE_CRS_VALUE, null );
    if( preferenceCodes == null || preferenceCodes.length() == 0 )
      return new String[] {};

    String[] availableNames = preferenceCodes.split( ";" );
    return availableNames;
  }

  /**
   * This function returns the dimension of the coordinate system with the given code.
   * 
   * @return The dimension of the coordinate system.
   */
  public static int getDimension( String code )
  {
    ICoordinateSystem coordinateSystem = CoordinateSystemFactory.getCoordinateSystem( code );
    if( coordinateSystem == null )
      return -1;

    return coordinateSystem.getDimension();
  }

  /**
   * This function returns an array of coordinate systems with the given codes.<br>
   * <br>
   * ATTENTION: Outside of this plugin, only the codes should be used.
   * 
   * @param codes
   *          The array of codes.
   * @return The array of coordinate systems.
   */
  public static ICoordinateSystem[] getCRSListByNames( String[] codes )
  {
    /* Memory for the coordinate systems. */
    ArrayList<ICoordinateSystem> coordinateSystems = new ArrayList<ICoordinateSystem>();

    for( int i = 0; i < codes.length; i++ )
    {
      ICoordinateSystem coordinateSystem = CoordinateSystemFactory.getCoordinateSystem( codes[i] );
      coordinateSystems.add( coordinateSystem );
    }

    return coordinateSystems.toArray( new ICoordinateSystem[] {} );
  }

  /**
   * This function returns a string, which is usable for a tooltip of a CRS. It contains the code the given coordinate
   * system.
   * 
   * @param code
   *          The code of the coordinate system.
   * @return The tooltip string.
   */
  public static String getTooltipText( String code )
  {
    try
    {
      ICoordinateSystem coordinateSystem = CoordinateSystemFactory.getCoordinateSystem( code );
      if( coordinateSystem != null )
      {
        /* The tooltip. */
        String tooltip = "Code:\n";
        tooltip = tooltip + coordinateSystem.getCode() + "\n\n";
        tooltip = tooltip + "Name:\n";
        tooltip = tooltip + coordinateSystem.getName() + "\n";

        return tooltip;
      }

      /* Leave the tooltip empty. */
      return "No valid CRS: " + code;
    }
    catch( Exception ex )
    {
      /* Log the error. */
      KalypsoDeegreePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( ex ) );

      /* Leave the tooltip empty. */
      return "";//$NON-NLS-1$
    }
  }

  /**
   * This function hashes the given coordinate systems with its EPSG code as key.
   * 
   * @return The hash of the given coordinate systems.
   */
  public static HashMap<String, ICoordinateSystem> getCoordHash( String[] codes )
  {
    /* Get all coordinate systems. */
    ICoordinateSystem[] coordinateSystems = CRSHelper.getCRSListByNames( codes );

    /* Cache the coordinate systems. */
    HashMap<String, ICoordinateSystem> coordHash = new HashMap<String, ICoordinateSystem>();
    for( int i = 0; i < coordinateSystems.length; i++ )
    {
      ICoordinateSystem coordinateSystem = coordinateSystems[i];
      coordHash.put( coordinateSystem.getCode(), coordinateSystem );
    }

    return coordHash;
  }

  /**
   * This function returns the EPSG code of the given coordinate system.
   * 
   * @param code
   *          The code of the coordinate system.
   * @return The EPSG code of the given coordinate system.
   */
  public static String getEPSG( String code )
  {
    /* First try: Parse it directly from the code. */
    String epsgPrefix = "EPSG:"; //$NON-NLS-1$
    if( code.startsWith( epsgPrefix ) )
      return code.substring( epsgPrefix.length() );

    /* Second try: Check the name. */
    ICoordinateSystem coordinateSystem = CoordinateSystemFactory.getCoordinateSystem( code );
    String name = coordinateSystem.getName();
    if( name.startsWith( epsgPrefix ) )
      return name.substring( epsgPrefix.length() );

    return null;
  }
}