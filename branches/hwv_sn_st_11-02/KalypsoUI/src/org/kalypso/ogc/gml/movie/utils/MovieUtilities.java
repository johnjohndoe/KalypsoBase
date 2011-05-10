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
package org.kalypso.ogc.gml.movie.utils;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.ogc.gml.AbstractCascadingLayerTheme;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemeVisitor;
import org.kalypso.ogc.gml.mapmodel.MapModellHelper;
import org.kalypso.ogc.gml.movie.IMovieImageProvider;
import org.kalypso.ogc.gml.movie.standard.DefaultMovieImageProvider;
import org.kalypso.ui.IKalypsoUIConstants;
import org.kalypso.ui.KalypsoUIExtensions;

/**
 * Helper class for the movie functionality.
 * 
 * @author Holger Albert
 */
public class MovieUtilities
{
  /**
   * The constructor.
   */
  private MovieUtilities( )
  {
  }

  /**
   * This function searches the map model for a {@link AbstractCascadingLayerTheme} with the property "movieTheme" set.
   * 
   * @param mapModel
   *          The map model.
   * @return The {@link AbstractCascadingLayerTheme} or null.
   */
  public static AbstractCascadingLayerTheme findMovieTheme( GisTemplateMapModell mapModel ) throws Exception
  {
    /* Find the movie theme. */
    IKalypsoTheme[] themes = MapModellHelper.findThemeByProperty( mapModel, IKalypsoUIConstants.MOVIE_THEME_PROPERTY, IKalypsoThemeVisitor.DEPTH_ZERO );
    if( themes == null || themes.length == 0 )
      throw new Exception( "Es wurde kein Filmthema in der aktiven Karte gefunden..." );

    IKalypsoTheme theme = themes[0];
    if( !(theme instanceof AbstractCascadingLayerTheme) )
      throw new Exception( "Es wurde kein Filmthema in der aktiven Karte gefunden..." );

    return (AbstractCascadingLayerTheme) theme;
  }

  /**
   * This function returns the configured movie image provider of the theme, marked as movie theme.
   * 
   * @param movieTheme
   *          The theme, marked as movie theme.
   * @return The configured image provider. A default one, if none is configured, the id is wrong or if an error has
   *         occured.
   */
  public static IMovieImageProvider initImageProvider( AbstractCascadingLayerTheme movieTheme )
  {
    try
    {
      String id = movieTheme.getProperty( IKalypsoUIConstants.MOVIE_THEME_PROPERTY, null );

      if( id == null || id.length() == 0 )
        return new DefaultMovieImageProvider();

      IMovieImageProvider imageProvider = KalypsoUIExtensions.createMovieImageProvider( id );
      if( imageProvider != null )
        return imageProvider;

      return new DefaultMovieImageProvider();
    }
    catch( CoreException ex )
    {
      ex.printStackTrace();
      return new DefaultMovieImageProvider();
    }
  }
}