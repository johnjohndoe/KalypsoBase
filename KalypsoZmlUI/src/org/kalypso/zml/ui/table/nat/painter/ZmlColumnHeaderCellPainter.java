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
package org.kalypso.zml.ui.table.nat.painter;

import java.util.LinkedHashSet;
import java.util.Set;

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.AbstractCellPainter;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.painter.cell.ImagePainter;
import net.sourceforge.nattable.painter.cell.TextPainter;
import net.sourceforge.nattable.style.BorderStyle;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.HorizontalAlignmentEnum;
import net.sourceforge.nattable.style.Style;
import net.sourceforge.nattable.style.VerticalAlignmentEnum;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.rules.AppliedRule;

/**
 * @author Dirk Kuch
 */
public class ZmlColumnHeaderCellPainter extends AbstractCellPainter
{

  public ZmlColumnHeaderCellPainter( )
  {
  }

  @Override
  public void paintCell( final LayerCell cell, final GC gc, final Rectangle bounds, final IConfigRegistry configRegistry )
  {
    final Object object = cell.getDataValue();
    if( object instanceof IZmlModelColumn )
    {
      final IZmlModelColumn column = (IZmlModelColumn) object;

      final Style imageCellStyle = getStyle();
      imageCellStyle.setAttributeValue( CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT );
      configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_STYLE, imageCellStyle, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER.toString() );

      Rectangle ptr = new Rectangle( bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4 );

      final Image[] images = getImages( column );
      for( final Image image : images )
      {
        final ImagePainter imgPainter = new ImagePainter( image );
        imgPainter.paintCell( cell, gc, ptr, configRegistry );

        ptr = move( ptr, image.getBounds() );
      }

      final Style cellStyle = getStyle();
      configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER.toString() );

      final ICellPainter painter = new TextPainter( true, true );
      painter.paintCell( cell, gc, ptr, configRegistry );
    }
    else
      throw new UnsupportedOperationException();
  }

  private Rectangle move( final Rectangle ptr, final Rectangle bounds )
  {
    return new Rectangle( ptr.x + bounds.width, ptr.y, ptr.width - bounds.width, ptr.height );
  }

  private Style getStyle( )
  {

    final Font font = GUIHelper.getFont( new FontData( "Verdana", 10, SWT.NORMAL ) );
    final Color bgColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
    final Color fgColor = GUIHelper.COLOR_WIDGET_FOREGROUND;
    final HorizontalAlignmentEnum hAlign = HorizontalAlignmentEnum.RIGHT;
    final VerticalAlignmentEnum vAlign = VerticalAlignmentEnum.MIDDLE;
    final BorderStyle borderStyle = null;

    final Style cellStyle = new Style();
    cellStyle.setAttributeValue( CellStyleAttributes.BACKGROUND_COLOR, bgColor );
    cellStyle.setAttributeValue( CellStyleAttributes.FOREGROUND_COLOR, fgColor );
    cellStyle.setAttributeValue( CellStyleAttributes.HORIZONTAL_ALIGNMENT, hAlign );
    cellStyle.setAttributeValue( CellStyleAttributes.VERTICAL_ALIGNMENT, vAlign );
    cellStyle.setAttributeValue( CellStyleAttributes.BORDER_STYLE, borderStyle );
    cellStyle.setAttributeValue( CellStyleAttributes.FONT, font );

    return cellStyle;
  }

  private Image[] getImages( final IZmlModelColumn column )
  {
    final Set<Image> images = new LinkedHashSet<Image>();

    final AppliedRule[] rules = column.getActiveRules();
    for( final AppliedRule rule : rules )
    {
      try
      {
        final Image img = rule.getCellStyle().getImage();
        if( Objects.isNotNull( img ) )
          images.add( img );
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
    }

    return images.toArray( new Image[] {} );
  }

  @Override
  public int getPreferredWidth( final LayerCell cell, final GC gc, final IConfigRegistry configRegistry )
  {
    final Object object = cell.getDataValue();
    if( object instanceof IZmlModelColumn )
    {
      final IZmlModelColumn column = (IZmlModelColumn) object;
      final Image[] images = getImages( column );

      int width = 0;
      for( final Image image : images )
      {
        width += image.getBounds().width;
      }

      final TextPainter painter = new TextPainter();
      width += painter.getPreferredWidth( cell, gc, configRegistry );

      return width + 4;
    }

    throw new UnsupportedOperationException();
  }

  @Override
  public int getPreferredHeight( final LayerCell cell, final GC gc, final IConfigRegistry configRegistry )
  {
    final Object object = cell.getDataValue();
    if( object instanceof IZmlModelColumn )
    {
      final IZmlModelColumn column = (IZmlModelColumn) object;
      final Image[] images = getImages( column );

      int height = 0;
      for( final Image image : images )
      {
        final Rectangle bounds = image.getBounds();
        height = Math.max( bounds.height, height );
      }

      final TextPainter painter = new TextPainter();
      height = Math.max( painter.getPreferredHeight( cell, gc, configRegistry ), height );

      return height;
    }

    throw new UnsupportedOperationException();
  }

}
