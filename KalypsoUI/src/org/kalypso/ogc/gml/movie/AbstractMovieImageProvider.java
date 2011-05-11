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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
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
   *      org.kalypso.ogc.gml.AbstractCascadingLayerTheme, org.kalypsodeegree.model.geometry.GM_Envelope,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void initialize( GisTemplateMapModell mapModel, AbstractCascadingLayerTheme movieTheme, GM_Envelope boundingBox, IProgressMonitor monitor )
  {
    /* Determine some needed information. */
    m_frames = preProcess( mapModel, movieTheme, boundingBox, monitor );
    if( m_frames.length > 0 )
      m_currentFrame = 0;
  }

  /**
   * @see org.kalypso.ogc.gml.movie.IMovieImageProvider#getCurrentFrame()
   */
  @Override
  public IMovieFrame getCurrentFrame( )
  {
    if( m_currentFrame < 0 || m_currentFrame <= m_frames.length )
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

  private IMovieFrame[] preProcess( GisTemplateMapModell mapModel, AbstractCascadingLayerTheme movieTheme, GM_Envelope boundingBox, IProgressMonitor monitor )
  {
    /* Monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    /* Memory for the results. */
    List<IMovieFrame> results = new ArrayList<IMovieFrame>();

    try
    {
      /* For each of these themes we need a map model to create a movie frame with it. */
      IKalypsoTheme[] themes = movieTheme.getAllThemes();

      /* Monitor. */
      monitor.beginTask( "Bereite Kartenthemen vor...", 200 * themes.length );

      for( int i = 0; i < themes.length; i++ )
      {
        /* Monitor. */
        monitor.subTask( "Erzeuge detailgetreue Kopie der Karte..." );

        /* Get the theme. */
        IKalypsoTheme theme = themes[i];

        /* Clone the map model. */
        GisTemplateMapModell newMapModel = MovieUtilities.cloneMapModel( mapModel, boundingBox );

        /* Monitor. */
        monitor.worked( 100 );
        monitor.subTask( "Erzeuge das Filmbild..." );

        /* Find the movie theme. */
        AbstractCascadingLayerTheme newMovieTheme = MovieUtilities.findMovieTheme( newMapModel );
        if( !newMovieTheme.getId().equals( movieTheme.getId() ) )
          throw new Exception( "Zuordnung des Filmthemes ist fehlerhaft..." );

        /* Deactivate all themes except the movie theme. */
        IKalypsoTheme[] allThemes = newMovieTheme.getAllThemes();
        for( IKalypsoTheme oneTheme : allThemes )
        {
          if( oneTheme.getId().equals( theme.getId() ) )
            oneTheme.setVisible( true );
          else
            oneTheme.setVisible( false );
        }

        /* Create the frame. */
        IMovieFrame frame = new MovieFrame( newMapModel, theme.getLabel() );

        /* Add the frame. */
        results.add( frame );

        /* Monitor. */
        monitor.worked( 100 );
      }

      return results.toArray( new IMovieFrame[] {} );
    }
    catch( Exception ex )
    {
      KalypsoGisPlugin.getDefault().getLog().log( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), ex.getLocalizedMessage(), ex ) );
      return new IMovieFrame[] {};
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }
  }
}