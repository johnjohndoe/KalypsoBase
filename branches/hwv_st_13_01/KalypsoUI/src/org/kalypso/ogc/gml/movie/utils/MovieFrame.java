/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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

import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;

import javax.media.jai.JAI;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemeVisitor;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.MapModellHelper;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.util.themes.legend.LegendUtilities;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * A movie frame contains a theme, a file of an image and a label.
 * 
 * @author Holger Albert
 */
public class MovieFrame implements IMovieFrame
{
  /**
   * The map model.
   */
  private final GisTemplateMapModell m_mapModel;

  /**
   * The label.
   */
  private final String m_label;

  /**
   * The id of the theme.
   */
  private final String m_themeID;

  /**
   * The bounding box.
   */
  private final GM_Envelope m_boundingBox;

  /**
   * The temp directory for that movie.
   */
  private final File m_tmpDirectory;

  public MovieFrame( final GisTemplateMapModell mapModel, final String label, final String themeID, final GM_Envelope boundingBox, final File tmpDirectory )
  {
    m_mapModel = mapModel;
    m_label = label;
    m_themeID = themeID;
    m_boundingBox = boundingBox;
    m_tmpDirectory = tmpDirectory;
  }

  @Override
  public String getLabel( )
  {
    return m_label;
  }

  @Override
  public RenderedImage getImage( final int width, final int height )
  {
    try
    {
      /* Get the directory of the images for this size. */
      /* It will be created, if it does not already exist. */
      final File imageDirectory = getImageDirectory( width, height );

      /* The image file. */
      final String imageFilename = getLabel() + "_" + m_themeID + ".PNG";
      final String correctedImageFilename = imageFilename.replace( " ", "_" );
      final String correctedImageFilename1 = correctedImageFilename.replace( "/", "_" );
      final File imageFile = new File( imageDirectory, correctedImageFilename1 );
      if( imageFile.exists() )
        return JAI.create( "fileload", imageFile.getAbsolutePath() );

      /* Create the image. */
      final IMapModell model = createModel();
      final BufferedImage image = MapModellHelper.createWellFormedImageFromModel( model, width, height, new Insets( 1, 1, 1, 1 ), 0, m_boundingBox );
      model.dispose();

      /* Save the image. */
      JAI.create( "filestore", image, imageFile.getAbsolutePath(), "PNG" );

      return image;
    }
    catch( final Exception ex )
    {
      KalypsoGisPlugin.getDefault().getLog().log( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), ex.getLocalizedMessage(), ex ) );
      return null;
    }
  }

  /**
   * Initialise the model: Show my theme and hide all other themes.
   * 
   * @return The new map model.
   */
  private IMapModell createModel( ) throws Exception
  {
    /* Clone the map model. */
    final GisTemplateMapModell newMapModel = MovieUtilities.cloneMapModel( m_mapModel, m_boundingBox );

    /* Hide all themes except the movie theme. */
    final IKalypsoLayerModell movieTheme = MovieUtilities.findMovieTheme( newMapModel );

    /* Get all themes. */
    final IKalypsoTheme[] allThemes = movieTheme.getAllThemes();
    for( final IKalypsoTheme theme : allThemes )
    {
      if( theme.getId().equals( m_themeID ) )
      {
        /* Find/Add the legend theme. */
        final IKalypsoTheme[] legendThemes = MapModellHelper.findThemeByProperty( newMapModel, LegendUtilities.THEME_PROPERTY_THEME_IDS, IKalypsoThemeVisitor.DEPTH_ZERO );
        if( legendThemes != null && legendThemes.length > 0 )
          legendThemes[0].setProperty( LegendUtilities.THEME_PROPERTY_THEME_IDS, ((IKalypsoTheme) movieTheme).getId() + ";" + theme.getId() );

        theme.setVisible( true );
      }
      else
        theme.setVisible( false );
    }

    return newMapModel;
  }

  /**
   * This function returns the directory, which contains the images of this size. If it does not exist, it will be
   * created.
   * 
   * @param width
   *          The width.
   * @param height
   *          The height.
   * @return The directory, which contains the images of this size.
   */
  private File getImageDirectory( final int width, final int height )
  {
    if( !m_tmpDirectory.exists() )
      m_tmpDirectory.mkdirs();

    final String sizeName = String.format( "%d_x_%d", width, height );
    final File sizeDirectory = new File( m_tmpDirectory, sizeName );
    if( !sizeDirectory.exists() )
      sizeDirectory.mkdirs();

    return sizeDirectory;
  }
}