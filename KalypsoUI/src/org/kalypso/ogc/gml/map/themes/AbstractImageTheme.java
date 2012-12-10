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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.ogc.gml.AbstractKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ui.KalypsoGisPlugin;
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
   * The horizontal position.
   */
  private int m_horizontal;

  /**
   * The vertical position.
   */
  private int m_vertical;

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

    try
    {
      /* Update the image. */
      Image image = updateImage( new SubProgressMonitor( monitor, 1000 ) );
      if( image == null )
        return Status.CANCEL_STATUS;

      /* Determine the position. */
      LegendCoordinate position = PositionUtilities.determinePosition( g, image, m_horizontal, m_vertical );

      /* Draw the image. */
      g.setPaintMode();
      g.drawImage( image, position.getX(), position.getY(), image.getWidth( null ), image.getHeight( null ), null );

      return Status.OK_STATUS;
    }
    catch( Exception ex )
    {
      return new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), ex.getLocalizedMessage(), ex );
    }
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
    m_horizontal = -1;
    m_vertical = -1;

    super.dispose();
  }

  /**
   * This function updates the image.
   * 
   * @param monitor
   *          A progress monitor.
   * @return The image.
   */
  protected abstract Image updateImage( IProgressMonitor monitor ) throws CoreException;

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