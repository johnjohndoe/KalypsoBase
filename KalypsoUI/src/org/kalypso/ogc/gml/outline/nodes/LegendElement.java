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
package org.kalypso.ogc.gml.outline.nodes;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.SWTUtilities;

/**
 * This class wraps an object for the legend graphic.
 * 
 * @author Holger Albert
 */
public class LegendElement
{
  private static final int LEVEL_INSET = 20;

  /**
   * This is the size of the icon. This size is used for summarizing the width of the legend item.
   */
  public static int ICON_SIZE = 16;

  /**
   * This is the gap after the icon. This size is used for summarizing the width of the legend item.
   */
  public static int GAP = 4;

  /**
   * The level of this legend element.
   */
  private final int m_level;

  private final IThemeNode m_node;

  private Image m_image;

  /**
   * @param font
   *          The font, to use for this legend element.
   * @param level
   *          The level of this legend element.
   * @param object
   *          The object of this legend element.
   */
  public LegendElement( final int level, final IThemeNode node )
  {
    m_level = level;
    m_node = node;
  }

  public void dispose( )
  {
    if( m_image != null )
      m_image.dispose();
  }

  /**
   * This function returns the dimension of this legend element.
   * 
   * @return The dimension of this element.
   */
  public Point getSize( final Font font )
  {
    final Point imageSize = getImageSize();

    if( m_node.isLabelInImage() )
      return new Point( imageSize.x, imageSize.y );

    /* Add text size to image size */
    final Point textSize = SWTUtilities.calcTextSize( getText(), font );

    final int width = m_level * LEVEL_INSET + imageSize.x + GAP + textSize.x;
    final int height = Math.max( imageSize.y, textSize.y );

    return new Point( width, height );
  }

  private Point getImageSize( )
  {
    final Image image = getImage();

    if( image == null )
      return new Point( ICON_SIZE, ICON_SIZE );

    final Rectangle imageSize = image.getBounds();
    return new Point( imageSize.width, imageSize.height );
  }

  /**
   * This function returns the text for this legend item.
   * 
   * @return The text of this legend item.
   */
  public String getText( )
  {
    return m_node.getLabel();
  }

  /**
   * This function returns the image for this legend item.
   * 
   * @return The image of this legend item.
   */
  public Image getImage( )
  {
    if( m_image == null )
    {
      final ImageDescriptor legendImage = m_node.getLegendImage();

      if( legendImage != null )
        m_image = legendImage.createImage( true );
    }
    return m_image;

  }

  /**
   * This function returns the level of this legend element.
   * 
   * @return The level of this legend element.
   */
  public int getLevel( )
  {
    return m_level;
  }

  public void paintLegend( final GC gc )
  {
    final int xPosition = getLevel() * LEVEL_INSET;

    /* Get the icon. */
    final Image icon = getImage();
    if( icon != null )
      gc.drawImage( icon, xPosition, 0 );

    if( m_node.isLabelInImage() )
      return;

    final Point imageSize = getImageSize();

    /* Draw the text. */
    final String legendText = getText();
    if( legendText != null )
      gc.drawText( legendText, xPosition + imageSize.x + GAP, 0, true );
  }
}