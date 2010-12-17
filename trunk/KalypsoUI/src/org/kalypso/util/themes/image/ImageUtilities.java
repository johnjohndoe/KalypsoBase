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
package org.kalypso.util.themes.image;

import java.util.Locale;
import java.util.Properties;

import org.kalypso.util.themes.position.PositionUtilities;

/**
 * This class provides functions for {@link org.kalypso.ogc.gml.IKalypsoTheme}s.
 * 
 * @author Holger Albert
 */
public class ImageUtilities
{
  /**
   * This constant defines the theme property, used to configure the URL of the image, which should be shown.
   */
  public static final String THEME_PROPERTY_IMAGE_URL = "image_url";

  /**
   * The constructor.
   */
  private ImageUtilities( )
  {
  }

  public static String checkImageUrl( String imageUrlProperty )
  {
    // TODO Perhaps we want to validate the URL of the image?
    return imageUrlProperty;
  }

  /**
   * This function returns a properties object, containing all serialized default image properties.
   * 
   * @return A properties object, containing all serialized default image properties.
   */
  public static Properties getDefaultProperties( )
  { /* Create the properties object. */
    Properties properties = new Properties();

    /* Serialize the properties. */
    String horizontalProperty = String.format( Locale.PRC, "%d", PositionUtilities.RIGHT );
    String verticalProperty = String.format( Locale.PRC, "%d", PositionUtilities.BOTTOM );
    String imageUrlProperty = "";

    /* Add the properties. */
    properties.put( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, horizontalProperty );
    properties.put( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, verticalProperty );
    properties.put( ImageUtilities.THEME_PROPERTY_IMAGE_URL, imageUrlProperty );

    return properties;
  }
}