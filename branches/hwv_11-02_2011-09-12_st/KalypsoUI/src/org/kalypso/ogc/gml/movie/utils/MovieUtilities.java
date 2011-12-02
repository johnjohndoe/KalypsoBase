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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.ogc.gml.AbstractCascadingLayerTheme;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoCascadingTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemeVisitor;
import org.kalypso.ogc.gml.mapmodel.MapModellHelper;
import org.kalypso.ogc.gml.movie.IMovieImageProvider;
import org.kalypso.ogc.gml.movie.standard.DefaultMovieImageProvider;
import org.kalypso.ogc.gml.selection.FeatureSelectionManager2;
import org.kalypso.template.gismapview.Gismapview;
import org.kalypso.ui.IKalypsoUIConstants;
import org.kalypso.ui.KalypsoUIExtensions;
import org.kalypsodeegree.model.geometry.GM_Envelope;

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
   * @return The {@link AbstractCascadingLayerTheme} or null. FIXME: return IKalypsoCascadingTheme instead!
   */
  public static AbstractCascadingLayerTheme findMovieTheme( final GisTemplateMapModell mapModel ) throws Exception
  {
    final IKalypsoTheme[] themes = MapModellHelper.findThemeByProperty( mapModel, IKalypsoUIConstants.MOVIE_THEME_PROPERTY, IKalypsoThemeVisitor.DEPTH_ZERO );
    if( themes == null || themes.length == 0 )
      throw new Exception( "Es wurde kein Filmthema in der aktiven Karte gefunden..." );

    final IKalypsoTheme theme = themes[0];
    if( !(theme instanceof AbstractCascadingLayerTheme) )
      throw new Exception( "Es wurde kein Filmthema in der aktiven Karte gefunden..." );

    return (AbstractCascadingLayerTheme) theme;
  }

  /**
   * This function returns the configured movie image provider of the theme, marked as movie theme.
   * 
   * @param mapModel
   *          The gis template map model.
   * @param boundingBox
   *          The bounding box.
   * @param monitor
   *          A progress monitor.
   * @return The configured image provider. A default one, if none is configured, the id is wrong or if an error has
   *         occured.
   */
  public static IMovieImageProvider getImageProvider( final GisTemplateMapModell mapModel, final GM_Envelope boundingBox, final IProgressMonitor monitor ) throws Exception
  {
    final IKalypsoCascadingTheme movieTheme = MovieUtilities.findMovieTheme( mapModel );
    final String id = movieTheme.getProperty( IKalypsoUIConstants.MOVIE_THEME_PROPERTY, null );
    if( id == null || id.length() == 0 )
      return getDefaultImageProvider( mapModel, boundingBox, monitor );

    final IMovieImageProvider imageProvider = KalypsoUIExtensions.createMovieImageProvider( id );
    if( imageProvider != null )
    {
      imageProvider.initialize( mapModel, boundingBox, monitor );
      return imageProvider;
    }

    return getDefaultImageProvider( mapModel, boundingBox, monitor );
  }

  /**
   * This function returns the default image provider.
   * 
   * @param mapModel
   *          The gis template map model.
   * @param boundingBox
   *          The bounding box.
   * @param monitor
   *          A progress monitor.
   * @return The default image provider.
   */
  public static DefaultMovieImageProvider getDefaultImageProvider( final GisTemplateMapModell mapModel, final GM_Envelope boundingBox, final IProgressMonitor monitor ) throws Exception
  {
    final DefaultMovieImageProvider imageProvider = new DefaultMovieImageProvider();
    imageProvider.initialize( mapModel, boundingBox, monitor );
    return imageProvider;
  }

  /**
   * This function clones the map model.
   * 
   * @param mapModel
   *          The map model.
   * @param boundingBox
   *          The bounding box.
   * @return The cloned map model.
   */
  public static GisTemplateMapModell cloneMapModel( final GisTemplateMapModell mapModel, final GM_Envelope boundingBox ) throws IOException
  {
    /* The output stream. */
    BufferedOutputStream outputStream = null;

    /* The temporary file. */
    File tmpFile = null;

    try
    {
      /* Create a gis map view. */
      final Gismapview gisview = mapModel.createGismapTemplate( boundingBox, mapModel.getCoordinatesSystem(), new NullProgressMonitor() );

      /* Create the temporary file. */
      tmpFile = FileUtilities.createNewUniqueFile( "mov", FileUtilities.TMP_DIR );

      /* Create the output stream. */
      outputStream = new BufferedOutputStream( new FileOutputStream( tmpFile ) );

      /* Save the gis map view. */
      GisTemplateHelper.saveGisMapView( gisview, outputStream, "UTF-8" );

      /* Close the output stream. */
      IOUtils.closeQuietly( outputStream );

      /* And load it again, to pratically clone it. */
      final Gismapview newGisview = GisTemplateHelper.loadGisMapView( tmpFile );

      /* Create the new gis template map model. */
      final GisTemplateMapModell newGisModel = new GisTemplateMapModell( mapModel.getContext(), mapModel.getCoordinatesSystem(), mapModel.getProject(), new FeatureSelectionManager2() );
      newGisModel.createFromTemplate( newGisview );

      return newGisModel;
    }
    catch( final Exception ex )
    {
      throw new IOException( "Konnte die Karte nicht kopieren...", ex );
    }
    finally
    {
      /* Close the output stream. */
      IOUtils.closeQuietly( outputStream );

      /* Delete the temporary file. */
      if( tmpFile != null )
        FileUtilities.deleteQuietly( tmpFile );
    }
  }

  /**
   * This function returns possible resolutions for the current screen size.
   * 
   * @return The possible resolutions.
   */
  public static MovieResolution[] getResolutions( )
  {
    /* All available resolutions. */
    final List<MovieResolution> resolutions = new ArrayList<MovieResolution>();
    resolutions.add( new MovieResolution( null, 640, 480 ) );
    resolutions.add( new MovieResolution( null, 800, 600 ) );
    resolutions.add( new MovieResolution( null, 1024, 768 ) );
    resolutions.add( new MovieResolution( null, 1280, 1024 ) );
    resolutions.add( new MovieResolution( "720p", 1280, 720 ) );
    resolutions.add( new MovieResolution( "1080p", 1920, 1080 ) );

    /* The screen resolution. */
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final MovieResolution screenResolution = new MovieResolution( "Bildschirm", screenSize.width, screenSize.height );

    /* Memory for the results. */
    final List<MovieResolution> results = new ArrayList<MovieResolution>();

    /* Collect all resolutions until the screen resolution. */
    for( final MovieResolution resolution : resolutions )
    {
      if( resolution.getWidth() > screenResolution.getWidth() )
        break;

      if( resolution.getHeight() > screenResolution.getHeight() )
        break;

      results.add( resolution );
    }

    /* The screen resolution is always the maximum. */
    resolutions.add( screenResolution );

    return resolutions.toArray( new MovieResolution[] {} );
  }
}