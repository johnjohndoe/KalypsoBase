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
package org.kalypso.ogc.gml.movie;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.ogc.gml.AbstractCascadingLayerTheme;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.movie.standard.DefaultMovieControls;
import org.kalypso.ogc.gml.movie.utils.IMovieFrame;
import org.kalypso.ogc.gml.movie.utils.MovieFrame;
import org.kalypso.ogc.gml.movie.utils.MovieUtilities;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * Base implementation for a movie image provider.
 * 
 * @author Holger Albert
 */
public abstract class AbstractMovieImageProvider implements IMovieImageProvider
{
  /**
   * The temporary directory.
   */
  private File m_tmpDirectory;

  /**
   * The frames.
   */
  private IMovieFrame[] m_frames;

  /**
   * The index of the current frame.
   */
  private int m_currentFrame;

  /**
   * The constructor.
   */
  public AbstractMovieImageProvider( )
  {
    m_tmpDirectory = null;
    m_frames = null;
    m_currentFrame = -1;
  }

  /**
   * @see org.kalypso.ogc.gml.movie.IMovieImageProvider#getMovieControls()
   */
  @Override
  public IMovieControls getMovieControls( )
  {
    return new DefaultMovieControls();
  }

  /**
   * @see org.kalypso.ogc.gml.movie.IMovieImageProvider#initialize(org.kalypso.ogc.gml.GisTemplateMapModell,
   *      org.kalypsodeegree.model.geometry.GM_Envelope, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void initialize( GisTemplateMapModell mapModel, GM_Envelope boundingBox, IProgressMonitor monitor ) throws Exception
  {
    /* Create a temporary directory. */
    m_tmpDirectory = FileUtilities.createNewTempDir( "mov" );

    /* Determine some needed information. */
    m_frames = preProcess( mapModel, boundingBox, monitor );
    if( m_frames.length > 0 )
      m_currentFrame = 0;
  }

  /**
   * @see org.kalypso.ogc.gml.movie.IMovieImageProvider#getCurrentFrame()
   */
  @Override
  public IMovieFrame getCurrentFrame( )
  {
    if( m_currentFrame < 0 || m_currentFrame >= m_frames.length )
      return null;

    return m_frames[m_currentFrame];
  }

  /**
   * @see org.kalypso.ogc.gml.movie.IMovieImageProvider#stepTo(int)
   */
  @Override
  public void stepTo( int step )
  {
    if( step >= m_frames.length )
    {
      m_currentFrame = 0;
      return;
    }

    if( step < 0 )
    {
      m_currentFrame = m_frames.length - 1;
      return;
    }

    m_currentFrame = step;
  }

  /**
   * @see org.kalypso.ogc.gml.movie.IMovieImageProvider#stepAndWait(int, int, int)
   */
  @Override
  public void stepAndWait( int step, int width, int height )
  {
    /* Step to step. */
    stepTo( step );

    /* Get the current frame. */
    IMovieFrame currentFrame = getCurrentFrame();
    if( currentFrame == null )
      return;

    /* Calling this function loads the image. */
    currentFrame.getImage( width, height );
  }

  /**
   * @see org.kalypso.ogc.gml.movie.IMovieImageProvider#getCurrentStep()
   */
  @Override
  public int getCurrentStep( )
  {
    return m_currentFrame;
  }

  /**
   * @see org.kalypso.ogc.gml.movie.IMovieImageProvider#getEndStep()
   */
  @Override
  public int getEndStep( )
  {
    return m_frames.length - 1;
  }

  /**
   * @see org.kalypso.ogc.gml.movie.IMovieImageProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_tmpDirectory != null )
      FileUtilities.deleteQuietly( m_tmpDirectory );

    if( m_frames != null )
    {
      for( IMovieFrame frame : m_frames )
        frame.dispose();
    }

    m_tmpDirectory = null;
    m_frames = null;
    m_currentFrame = -1;
  }

  private IMovieFrame[] preProcess( GisTemplateMapModell mapModel, GM_Envelope boundingBox, IProgressMonitor monitor ) throws Exception
  {
    /* Monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    /* Memory for the results. */
    List<IMovieFrame> results = new ArrayList<IMovieFrame>();

    try
    {
      /* Deactivate all themes. */
      mapModel.activateTheme( null );

      /* Find the movie theme. */
      AbstractCascadingLayerTheme movieTheme = MovieUtilities.findMovieTheme( mapModel );

      /* For each of these themes we need a map model to create a movie frame with it. */
      movieTheme.setVisible( true );
      IKalypsoTheme[] themes = movieTheme.getAllThemes();
      for( IKalypsoTheme theme : themes )
        theme.setVisible( false );

      /* Monitor. */
      monitor.beginTask( "Bereite Kartenthemen vor...", 200 * themes.length );

      /* Clone the map model. */
      GisTemplateMapModell[] newMapModels = MovieUtilities.duplicateMapModel( mapModel, boundingBox, themes.length );

      for( int i = 0; i < themes.length; i++ )
      {
        /* Monitor. */
        monitor.subTask( "Bereite das Filmbild vor..." );
        if( monitor.isCanceled() )
          throw new CoreException( new Status( IStatus.CANCEL, KalypsoGisPlugin.getId(), "Der Film wurde abgebrochen..." ) );

        /* Get the theme. */
        IKalypsoTheme theme = themes[i];

        /* Get the new map model. */
        GisTemplateMapModell newMapModel = newMapModels[i];

        /* Monitor. */
        monitor.worked( 100 );
        monitor.subTask( "Erzeuge das Filmbild..." );

        /* Find the movie theme. */
        AbstractCascadingLayerTheme newMovieTheme = MovieUtilities.findMovieTheme( newMapModel );
        if( !newMovieTheme.getId().equals( movieTheme.getId() ) )
          throw new Exception( "Zuordnung des Filmthemas ist fehlerhaft..." );

        /* Deactivate all themes except the movie theme. */
        IKalypsoTheme[] allThemes = newMovieTheme.getAllThemes();
        for( IKalypsoTheme oneTheme : allThemes )
        {
          /* All themes should already be set invisible. */
          if( oneTheme.getId().equals( theme.getId() ) )
          {
            oneTheme.setVisible( true );
            break;
          }
        }

        /* Create the frame. */
        IMovieFrame frame = new MovieFrame( newMapModel, theme.getLabel(), boundingBox, m_tmpDirectory );

        /* Add the frame. */
        results.add( frame );

        /* Monitor. */
        monitor.worked( 100 );
      }

      return results.toArray( new IMovieFrame[] {} );
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }
  }
}