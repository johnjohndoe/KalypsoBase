/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.movie.IMovieImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * This runnable creates and initializes the movie image provider.
 * 
 * @author Holger Albert
 */
public class MovieImageProviderRunnable implements ICoreRunnableWithProgress
{
  /**
   * The gis template map model.
   */
  private final GisTemplateMapModell m_mapModel;

  /**
   * The bounding box.
   */
  private final GM_Envelope m_envelope;

  /**
   * The movie image provider.
   */
  private IMovieImageProvider m_imageProvider;

  /**
   * The constructor.
   * 
   * @param mapModel
   *          The gis template map model.
   * @param boundingBox
   *          The bounding box.
   */
  public MovieImageProviderRunnable( final GisTemplateMapModell mapModel, final GM_Envelope envelope )
  {
    m_mapModel = mapModel;
    m_envelope = envelope;
    m_imageProvider = null;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    try
    {
      /* Create the image provider. */
      m_imageProvider = MovieUtilities.getImageProvider( m_mapModel, m_envelope, monitor );

      return new Status( IStatus.OK, KalypsoGisPlugin.getId(), "OK" ); //$NON-NLS-1$
    }
    catch( final Exception ex )
    {
      return new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), ex.getLocalizedMessage(), ex );
    }
  }

  /**
   * This function returns the initialised movie image provider or null, if an error has occured or this runnable was
   * never started once.
   * 
   * @return The movie image provider or null.
   */
  public IMovieImageProvider getImageProvider( )
  {
    return m_imageProvider;
  }
}