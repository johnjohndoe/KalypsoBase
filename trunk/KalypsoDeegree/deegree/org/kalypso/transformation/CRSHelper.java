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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.eclipse.core.runtime.Preferences;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.preferences.IKalypsoDeegreePreferences;
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
  }

  /**
   * This function returns true, if the given name is one of the known coordinate systems.
   * 
   * @param name
   *          The name of the coordinate system to check.
   * @return True, if the coordinate system exists.
   */
  public static boolean isKnownCRS( final String name )
  {
    if( name == null )
      return false;

    try
    {
      /* In case it is asked often, it is better to used the cached crs factory. */
      final CachedCRSFactory factory = CachedCRSFactory.getInstance();

      final CoordinateSystem crs = factory.create( name );
      if( crs == null )
        return false;

      return true;
    }
    catch( final UnknownCRSException e )
    {
      return false;
    }
  }

  /**
   * This function returns all known names of coordinate systems.
   * 
   * @return A list of all known names of coordinate systems.
   */
  public static List<String> getAllNames( )
  {
    final Preferences preferences = KalypsoDeegreePlugin.getDefault().getPluginPreferences();

    final String preferenceNames = preferences.getString( IKalypsoDeegreePreferences.AVAILABLE_CRS_SETTING );
    if( preferenceNames == null || preferenceNames.length() == 0 )
      return new ArrayList<String>();

    final String[] availableNames = preferenceNames.split( ";" );
    return Arrays.asList( availableNames );
  }

  /**
   * This function returns the dimension of the coordinate system with the given name.
   * 
   * @return The dimension of the coordinate system.
   */
  public static int getDimension( final String name ) throws UnknownCRSException
  {
    final CoordinateSystem coordinateSystem = CachedCRSFactory.getInstance().create( name );

    return coordinateSystem.getDimension();
  }

  /**
   * This function returns a list of coordinate systems with the given names. <br>
   * <br>
   * ATTENTION: Outside of this plugin, only the names should be used.
   * 
   * @param names
   *          The list of names.
   * @return The list of coordinate systems.
   */
  public static List<CoordinateSystem> getCRSListByNames( final List<String> names ) throws UnknownCRSException
  {
    /* Memory for the coordinate systems. */
    final ArrayList<CoordinateSystem> coordinateSystems = new ArrayList<CoordinateSystem>();

    for( int i = 0; i < names.size(); i++ )
    {
      final CoordinateSystem coordinateSystem = CachedCRSFactory.getInstance().create( names.get( i ) );
      coordinateSystems.add( coordinateSystem );
    }

    return coordinateSystems;
  }

  /**
   * This function returns a string, which is usable for a tooltip of a CRS. It contains the name and all IDs of the
   * given CRS.
   * 
   * @param name
   *          The name of the crs.
   * @return The tooltip string.
   */
  public static String getTooltipText( final String name )
  {
    try
    {
      final CoordinateSystem coordinateSystem = CachedCRSFactory.getInstance().create( name );
      if( coordinateSystem != null )
      {
        /* The tooltip. */
        String tooltip = "Name:\n";
        tooltip = tooltip + coordinateSystem.getCRS().getName() + "\n\n";

        /* Add the identifiers. */
        tooltip = tooltip + "Identifier:\n";

        /* Get all identifiers. */
        final String[] identifiers = coordinateSystem.getCRS().getIdentifiers();
        for( final String identifier : identifiers )
        {
          tooltip = tooltip + identifier + "\n";
        }

        return tooltip;
      }

      /* Leave the tooltip empty. */
      return "No valid CRS: " + name;
    }
    catch( final Exception ex )
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
  public static HashMap<String, CoordinateSystem> getCoordHash( final List<String> names ) throws UnknownCRSException
  {
    /* Get all coordinate systems. */
    final List<CoordinateSystem> coordinateSystems = CRSHelper.getCRSListByNames( names );

    /* Cache the coordinate systems. */
    final HashMap<String, CoordinateSystem> coordHash = new HashMap<String, CoordinateSystem>();
    for( int i = 0; i < coordinateSystems.size(); i++ )
    {
      final CoordinateSystem coordinateSystem = coordinateSystems.get( i );
      coordHash.put( coordinateSystem.getIdentifier(), coordinateSystem );
    }

    return coordHash;
  }

  /**
   * Returns the epsg code of the given coordinate system.
   */
  public static String getEPSG( final String coordinateSystem ) throws UnknownCRSException
  {
    /* first try: parse it direct5ly from the crs name */
    final String epsgPrefix = "EPSG:";//$NON-NLS-1$
    if( coordinateSystem.startsWith( epsgPrefix ) )
      return coordinateSystem.substring( epsgPrefix.length() );

    /* second try: check all identifiers */
    final CoordinateSystem crs = CachedCRSFactory.getInstance().create( coordinateSystem );

    final String[] identifiers = crs.getCRS().getIdentifiers();
    for( final String id : identifiers )
    {
      if( id.startsWith( epsgPrefix ) )
        return id.substring( epsgPrefix.length() );
    }

    return null;
  }

}