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
package org.kalypso.util.themes.position;

/**
 * This class provides functions for positioning.
 * 
 * @author Holger Albert
 */
public class PositionUtilities
{
  /**
   * This constant defines the theme property, used to configure the horizontal position.
   */
  public static final String THEME_PROPERTY_HORIZONTAL_POSITION = "horizontal_position";

  /**
   * This constant defines the theme property, used to configure the vertical position.
   */
  public static final String THEME_PROPERTY_VERTICAL_POSITION = "vertical_position";

  /**
   * This constant defines the left/west position.
   */
  public static final int LEFT = 0x01;

  /**
   * This constant defines the center (horizontal) position.
   */
  public static final int H_CENTER = 0x02;

  /**
   * This constant defines the right/east position.
   */
  public static final int RIGHT = 0x04;

  /**
   * This constant defines the top/north position.
   */
  public static final int TOP = 0x01;

  /**
   * This constant defines the center (vertical) position.
   */
  public static final int V_CENTER = 0x02;

  /**
   * This constant defines the bottom/south position.
   */
  public static final int BOTTOM = 0x04;

  /**
   * The constructor.
   */
  private PositionUtilities( )
  {
  }

  /**
   * This function checks if the horizontal position provided has a correct value. If not
   * {@link PositionUtilities#RIGHT} will be returned.
   * 
   * @param horizontal
   *          The horizontal position to be checked.
   * @return The horizontal position, if it is correct. Otherwise {@link PositionUtilities#RIGHT}.
   */
  public static int checkHorizontalPosition( int horizontal )
  {
    if( (horizontal & PositionUtilities.LEFT) != 0 )
      return horizontal;

    if( (horizontal & PositionUtilities.H_CENTER) != 0 )
      return horizontal;

    if( (horizontal & PositionUtilities.RIGHT) != 0 )
      return horizontal;

    return PositionUtilities.RIGHT;
  }

  /**
   * This function checks if the vertical position provided has a correct value. If not {@link PositionUtilities#BOTTOM}
   * will be returned.
   * 
   * @param vertical
   *          The vertical position to be checked.
   * @return The vertical position, if it is correct. Otherwise {@link PositionUtilities#BOTTOM}.
   */
  public static int checkVerticalPosition( int vertical )
  {
    if( (vertical & PositionUtilities.TOP) != 0 )
      return vertical;

    if( (vertical & PositionUtilities.V_CENTER) != 0 )
      return vertical;

    if( (vertical & PositionUtilities.BOTTOM) != 0 )
      return vertical;

    return PositionUtilities.BOTTOM;
  }
}