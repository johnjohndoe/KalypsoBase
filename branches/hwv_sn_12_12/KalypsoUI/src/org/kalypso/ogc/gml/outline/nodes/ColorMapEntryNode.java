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

import java.awt.Color;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypsodeegree.graphics.sld.ColorMapEntry;

/**
 * @author Gernot Belger
 */
class ColorMapEntryNode extends AbstractThemeNode<ColorMapEntry>
{
  ColorMapEntryNode( final IThemeNode parent, final ColorMapEntry entry )
  {
    super( parent, entry );
  }

  /**
   * @see org.kalypso.ogc.gml.outline.AbstractThemeNode#getElementChildren()
   */
  @Override
  protected Object[] getElementChildren( )
  {
    return EMPTY_CHILDREN;
  }

  @Override
  public ImageDescriptor getImageDescriptor( )
  {
    final ColorMapEntry element = getElement();

    final TreeObjectImage treeImage = new TreeObjectImage( 16, 16 );
    try
    {
      final GC gc = treeImage.getGC();
      final Rectangle clipping = gc.getClipping();
      final int alpha = (int) Math.round( element.getOpacity() * 255 );
      final Color awtColor = element.getColor();
      final RGB rgb = new RGB( awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue() );
      final org.eclipse.swt.graphics.Color color = new org.eclipse.swt.graphics.Color( gc.getDevice(), rgb );
      gc.setAlpha( alpha );
      gc.setBackground( color );
      gc.fillRectangle( clipping.x + 1, clipping.y + 1, clipping.width - 2, clipping.height - 2 );
      color.dispose();
      return treeImage.getImageDescriptor();
    }
    finally
    {
      treeImage.dispose();
    }
  }

  @Override
  public String getLabel( )
  {
    final ColorMapEntry element = getElement();

    final String label = element.getLabel().trim();
    if( label.isEmpty() )
      return "" + element.getQuantity(); //$NON-NLS-1$

    return resolveI18nString( label );
  }


}
