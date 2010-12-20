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
package org.kalypso.ogc.gml.map.themes;

import java.awt.Graphics;
import java.awt.Image;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.ogc.gml.AbstractKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.IMapModellListener;
import org.kalypso.ogc.gml.mapmodel.MapModellAdapter;
import org.kalypso.util.themes.legend.LegendCoordinate;
import org.kalypso.util.themes.position.PositionUtilities;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * Base implementation of a theme, that can display general images.
 * 
 * @author Holger Albert
 */
public abstract class AbstractImageTheme extends AbstractKalypsoTheme
{
  /**
   * This listener invalidates the displayed images.
   */
  private IMapModellListener m_modellListener = new MapModellAdapter()
  {
    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeAdded(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeAdded( IMapModell source, IKalypsoTheme theme )
    {
      invalidateImage();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeRemoved(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme, boolean)
     */
    @Override
    public void themeRemoved( IMapModell source, IKalypsoTheme theme, boolean lastVisibility )
    {
      invalidateImage();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeOrderChanged(org.kalypso.ogc.gml.mapmodel.IMapModell)
     */
    @Override
    public void themeOrderChanged( IMapModell source )
    {
      invalidateImage();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeVisibilityChanged(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme, boolean)
     */
    @Override
    public void themeVisibilityChanged( IMapModell source, IKalypsoTheme theme, boolean visibility )
    {
      invalidateImage();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeStatusChanged(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeStatusChanged( IMapModell source, IKalypsoTheme theme )
    {
      invalidateImage();
    }
  };

  /**
   * Responsible for updating the legend.
   */
  private Job m_job = new UIJob( "ImageCreator" ) //$NON-NLS-1$
  {
    /**
     * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInUIThread( IProgressMonitor monitor )
    {
      /* If no monitor was given, take a null progress monitor. */
      if( monitor == null )
        monitor = new NullProgressMonitor();

      try
      {
        /* Monitor. */
        monitor.beginTask( "Zeichne Legende...", 1000 );
        monitor.subTask( "Zeichne Legende..." );

        /* Update the image. */
        updateImageInternal( new SubProgressMonitor( monitor, 1000 ) );

        return Status.OK_STATUS;
      }
      catch( CoreException ex )
      {
        return ex.getStatus();
      }
      finally
      {
        /* Monitor. */
        monitor.done();
      }
    }
  };

  /**
   * The horizontal position.
   */
  private int m_horizontal;

  /**
   * The vertical position.
   */
  private int m_vertical;

  /**
   * The image to draw.
   */
  protected Image m_image;

  /**
   * The constructor
   * 
   * @param name
   *          The name of the theme.
   * @param type
   *          The type.
   * @param mapModell
   *          The map modell to use.
   */
  public AbstractImageTheme( I10nString name, String type, IMapModell mapModell )
  {
    super( name, type, mapModell );

    /* Initialize. */
    m_horizontal = -1;
    m_vertical = -1;
    m_image = null;

    /* Add a map modell listener. */
    mapModell.addMapModelListener( m_modellListener );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#paint(java.awt.Graphics,
   *      org.kalypsodeegree.graphics.transformation.GeoTransform, java.lang.Boolean,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus paint( Graphics g, GeoTransform world2screen, Boolean selected, IProgressMonitor monitor )
  {
    if( selected != null && selected )
      return Status.OK_STATUS;

    if( m_image != null )
    {
      /* Determine the position. */
      LegendCoordinate position = PositionUtilities.determinePosition( g, m_image, m_horizontal, m_vertical );

      /* Draw the image. */
      g.setPaintMode();
      g.drawImage( m_image, position.getX(), position.getY(), m_image.getWidth( null ), m_image.getHeight( null ), null );
    }

    return Status.OK_STATUS;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#getFullExtent()
   */
  @Override
  public GM_Envelope getFullExtent( )
  {
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#dispose()
   */
  @Override
  public void dispose( )
  {
    /* Remove the map modell listener. */
    getMapModell().removeMapModelListener( m_modellListener );

    m_horizontal = -1;
    m_vertical = -1;
    m_image = null;

    super.dispose();
  }

  /**
   * This function updates the image.
   * 
   * @param monitor
   *          A progress monitor.
   */
  protected abstract Image updateImage( IProgressMonitor monitor ) throws CoreException;

  /**
   * This function updates the image.
   * 
   * @param monitor
   *          A progress monitor.
   */
  protected void updateImageInternal( IProgressMonitor monitor ) throws CoreException
  {
    /* Update the image. */
    m_image = updateImage( monitor );

    /* Fire a repaint request. */
    fireRepaintRequested( null );
  }

  /**
   * This function invalidates the image.
   */
  protected void invalidateImage( )
  {
    m_job.cancel();
    m_image = null;
    m_job.schedule( 100 );
  }

  /**
   * This function updates the horizontal and vertical position.
   * 
   * @param horizontal
   *          The horizontal position.
   * @param vertical
   *          The vertical position.
   */
  protected void updatePosition( int horizontal, int vertical )
  {
    m_horizontal = horizontal;
    m_vertical = vertical;
  }
}