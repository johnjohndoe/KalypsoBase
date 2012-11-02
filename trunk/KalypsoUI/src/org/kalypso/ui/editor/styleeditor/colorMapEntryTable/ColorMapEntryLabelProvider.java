/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ui.editor.styleeditor.colorMapEntryTable;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.kalypsodeegree.graphics.sld.ColorMapEntry;

/**
 * A label provider for the color map entry table.
 * 
 * @author Andreas Doemming
 * @author Holger Albert
 */
public class ColorMapEntryLabelProvider extends LabelProvider implements ITableLabelProvider
{
  /**
   * The registry of the images.
   */
  private final Map<java.awt.Color, Image> m_images = new HashMap<>();

  @Override
  public String getColumnText( final Object element, final int columnIndex )
  {
    final ColorMapEntry colorMapEntry = (ColorMapEntry)element;

    switch( columnIndex )
    {
      case 0:
        return colorMapEntry.getLabel();
      case 1:
        return String.format( "%.1f", colorMapEntry.getQuantity() ); //$NON-NLS-1$
      case 2:
        break;
      case 3:
        return String.format( "%.1f", colorMapEntry.getOpacity() ); //$NON-NLS-1$
      default:
        break;
    }

    return ""; //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
   */
  @Override
  public Image getColumnImage( final Object element, final int columnIndex )
  {
    if( !(element instanceof ColorMapEntry) )
      return null;

    if( columnIndex != 2 )
      return null;

    /* Things, that needs to be disposed. */
    Color swtColor = null;
    GC gc = null;

    try
    {
      /* Cast. */
      final ColorMapEntry entry = (ColorMapEntry)element;

      /* Get the display. */
      final Display display = PlatformUI.getWorkbench().getDisplay();

      /* Get the color. */
      final java.awt.Color awtColor = entry.getColor();
      if( m_images.containsKey( awtColor ) )
        return m_images.get( awtColor );

      swtColor = new Color( display, awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue() );

      /* Create the image. */
      final Image image = new Image( display, 25, 15 );
      gc = new GC( image );

      /* Change the background of the image. */
      gc.setBackground( swtColor );
      gc.setAlpha( awtColor.getAlpha() );
      gc.fillRectangle( image.getBounds() );

      /* Store the image. */
      m_images.put( awtColor, image );

      /* Return it. */
      return image;
    }
    finally
    {
      /* Dispose the swt color. */
      if( swtColor != null )
        swtColor.dispose();

      /* Dispose the gc. */
      if( gc != null )
        gc.dispose();
    }
  }

  /**
   * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    for( final Image image : m_images.values() )
      image.dispose();

    super.dispose();
  }
}