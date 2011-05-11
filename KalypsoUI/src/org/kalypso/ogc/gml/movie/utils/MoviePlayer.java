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

import org.kalypso.ogc.gml.AbstractCascadingLayerTheme;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.movie.IMovieImageProvider;
import org.kalypso.ogc.gml.movie.controls.MovieComposite;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * The movie player.
 * 
 * @author Holger Albert
 */
public class MoviePlayer
{
  /**
   * The parent movie composite.
   */
  private MovieComposite m_parent;

  /**
   * The movie image provider.
   */
  private IMovieImageProvider m_imageProvider;

  /**
   * The constructor.
   * 
   * @param parent
   *          The parent movie composite.
   * @param mapModel
   *          The gis template map model.
   * @param movieTheme
   *          The theme, marked as movie theme.
   * @param boundingBox
   *          The bounding box.
   */
  public MoviePlayer( MovieComposite parent, GisTemplateMapModell mapModel, AbstractCascadingLayerTheme movieTheme, GM_Envelope boundingBox )
  {
    m_parent = parent;
    m_imageProvider = MovieUtilities.getImageProvider( mapModel, movieTheme, boundingBox );
  }

  public void updateControls( )
  {
    if( m_parent == null || m_parent.isDisposed() )
      return;

    m_parent.updateControls();
  }

  public IMovieImageProvider getImageProvider( )
  {
    return m_imageProvider;
  }

  public IMovieFrame getCurrentFrame( )
  {
    return m_imageProvider.getCurrentFrame();
  }

  public void stepTo( int step )
  {
    m_imageProvider.stepTo( step );
  }

  public int getCurrentStep( )
  {
    return m_imageProvider.getCurrentStep();
  }

  public int getEndStep( )
  {
    return m_imageProvider.getEndStep();
  }
}