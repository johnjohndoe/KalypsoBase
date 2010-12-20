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
package org.kalypso.ogc.gml.map.themes;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.util.themes.image.ImageUtilities;
import org.kalypso.util.themes.legend.LegendUtilities;
import org.kalypso.util.themes.position.PositionUtilities;

/**
 * This theme displays an image on the map.
 * 
 * @author Holger Albert
 */
public class KalypsoImageTheme extends AbstractImageTheme
{
  /**
   * The URL of the image, which should be shown.
   */
  protected String m_imageUrl;

  /**
   * The constructor
   * 
   * @param name
   *          The name of the theme.
   * @param mapModell
   *          The map modell to use.
   */
  public KalypsoImageTheme( I10nString name, IMapModell mapModell )
  {
    super( name, "image", mapModell );

    /* Initialize. */
    m_imageUrl = null;
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getDefaultIcon()
   */
  @Override
  public ImageDescriptor getDefaultIcon( )
  {
    return KalypsoGisPlugin.getImageProvider().getImageDescriptor( ImageProvider.DESCRIPTORS.IMAGE_THEME_IMAGE );
  }

  /**
   * @see org.kalypso.ogc.gml.map.themes.AbstractImageTheme#updateImage(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  protected Image updateImage( IProgressMonitor monitor )
  {
    /* If no monitor was given, take a null progress monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    /* The input stream. */
    InputStream inputStream = null;

    try
    {
      /* Monitor. */
      monitor.beginTask( "Zeichne Bild...", 1000 );
      monitor.subTask( "Initialisiere Thema..." );

      /* Initialize properties. */
      initFromProperties();

      /* Monitor. */
      monitor.worked( 500 );
      monitor.subTask( "Erzeuge Bild..." );

      /* Create the input stream. */
      if( m_imageUrl == null )
        return null;

      /* Create the input stream. */
      URL imageUrl = new URL( getContext(), m_imageUrl );
      inputStream = imageUrl.openStream();

      /* Create the image. */
      BufferedImage awtImage = ImageIO.read( inputStream );

      /* Monitor. */
      monitor.worked( 500 );

      return awtImage;
    }
    catch( IOException ex )
    {
      ex.printStackTrace();
      setStatus( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), ex.getLocalizedMessage(), ex ) );
      return null;
    }
    finally
    {
      /* Close the input stream. */
      IOUtils.closeQuietly( inputStream );

      /* Monitor. */
      monitor.done();
    }
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractImageTheme#dispose()
   */
  @Override
  public void dispose( )
  {
    m_imageUrl = null;

    super.dispose();
  }

  /**
   * This function initializes the legend theme from its own properties. For these not found, defaults will be set.
   */
  private void initFromProperties( )
  {
    /* Default values. */
    updatePosition( PositionUtilities.RIGHT, PositionUtilities.BOTTOM );
    m_imageUrl = null;

    /* Get the properties. */
    String horizontalProperty = getProperty( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, null );
    String verticalProperty = getProperty( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, null );
    String imageUrlProperty = getProperty( ImageUtilities.THEME_PROPERTY_IMAGE_URL, null );

    /* Check the horizontal and vertical position. */
    int horizontal = LegendUtilities.checkHorizontalPosition( horizontalProperty );
    int vertical = LegendUtilities.checkVerticalPosition( verticalProperty );
    if( horizontal != -1 && vertical != -1 )
      updatePosition( horizontal, vertical );

    /* Check the URL of the image. */
    if( imageUrlProperty != null && imageUrlProperty.length() > 0 )
      m_imageUrl = ImageUtilities.checkImageUrl( imageUrlProperty );
  }
}