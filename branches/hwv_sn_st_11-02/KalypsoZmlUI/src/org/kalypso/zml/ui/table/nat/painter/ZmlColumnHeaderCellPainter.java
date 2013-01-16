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
import org.kalypso.zml.core.table.model.utils.ZmlModelColumns;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.core.table.rules.AppliedRule;

/**
 * @author Dirk Kuch
 */
public class ZmlColumnHeaderCellPainter extends AbstractCellPainter
{
  private final ZmlModelViewport m_viewport;

  private final Style m_style;

  private final Style m_selectionStyle;

  private final Style m_imageCellStyle;

  private final Style m_selectionImageCellStyle;

  private final int m_borderWidth = 5;

  private final int m_imageGap = 5;

  public ZmlColumnHeaderCellPainter( final ZmlModelViewport viewport )
  {
    m_viewport = viewport;

    m_style = getStyle();
    m_selectionStyle = getSelectionStyle( m_style );

    m_imageCellStyle = getStyle();
    m_imageCellStyle.setAttributeValue( CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT );

    m_selectionImageCellStyle = getSelectionStyle( m_imageCellStyle );
    m_selectionImageCellStyle.setAttributeValue( CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT );
  }

  @Override
  public void paintCell( final LayerCell cell, final GC gc, final Rectangle bounds, final IConfigRegistry configRegistry )
  {
    final Object object = cell.getDataValue();

    /* erase background, needed because of insets */
    final Color backgroundColor;
    if( cell.getDisplayMode() == DisplayMode.SELECT )
      backgroundColor = GUIHelper.COLOR_GRAY;
    else
      backgroundColor = GUIHelper.COLOR_WIDGET_BACKGROUND;

    final Color originalBackground = gc.getBackground();
    gc.setBackground( backgroundColor );
    gc.fillRectangle( bounds );
    gc.setBackground( originalBackground );

    // FIXME: was 2; consider in getPrefferedWidth!
    Rectangle ptr = new Rectangle( bounds.x, bounds.y, bounds.width - m_borderWidth, bounds.height );

    final ImagePainter[] imagePainters = createImagePainters( object, configRegistry );
    for( final ICellPainter imagePainter : imagePainters )
    {
      imagePainter.paintCell( cell, gc, ptr, configRegistry );

      if( imagePainter instanceof ImagePainter )
      {
        // REMARTK: check is only for performance reasons, non need to calculate width for last painter
        final int width = imagePainter.getPreferredWidth( cell, gc, configRegistry );
        ptr = new Rectangle( ptr.x + width, ptr.y, ptr.width - width, ptr.height );
      }
    }

    ptr = new Rectangle( ptr.x + m_imageGap, ptr.y, ptr.width - m_imageGap, ptr.height );

    final ICellPainter painter = createTextPainter( configRegistry );
    painter.paintCell( cell, gc, ptr, configRegistry );
  }

  private ImagePainter[] createImagePainters( final Object object, final IConfigRegistry configRegistry )
  {
    if( object instanceof IZmlModelColumn )
    {
      final IZmlModelColumn column = (IZmlModelColumn) object;

      configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_STYLE, m_imageCellStyle, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER.toString() );
      configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_STYLE, m_selectionImageCellStyle, DisplayMode.SELECT, GridRegion.COLUMN_HEADER.toString() );

      final Image[] images = getImages( column );

      final ImagePainter[] imgPainters = new ImagePainter[images.length];

      for( int i = 0; i < images.length; i++ )
        imgPainters[i] = new ImagePainter( images[i] );

      return imgPainters;
    }

    throw new UnsupportedOperationException();
  }

  private ICellPainter createTextPainter( final IConfigRegistry configRegistry )
  {
    configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_STYLE, m_style, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER.toString() );
    configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_STYLE, m_selectionStyle, DisplayMode.SELECT, GridRegion.COLUMN_HEADER.toString() );

    return new TextPainter();
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

  private Style getSelectionStyle( final Style style )
  {
    final Font font = style.getAttributeValue( CellStyleAttributes.FONT );
    final Color bgColor = GUIHelper.COLOR_GRAY;
    final Color fgColor = GUIHelper.COLOR_WHITE;
    final HorizontalAlignmentEnum hAlign = style.getAttributeValue( CellStyleAttributes.HORIZONTAL_ALIGNMENT );
    final VerticalAlignmentEnum vAlign = style.getAttributeValue( CellStyleAttributes.VERTICAL_ALIGNMENT );
    final BorderStyle borderStyle = style.getAttributeValue( CellStyleAttributes.BORDER_STYLE );

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

    final AppliedRule[] rules = ZmlModelColumns.findRules( m_viewport, column );
    for( final AppliedRule rule : rules )
    {
      if( !rule.hasHeaderIcon() )
        continue;

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
    final Object dataValue = cell.getDataValue();

    final ImagePainter[] imagePainters = createImagePainters( dataValue, configRegistry );

    int width = 0;
    for( final ImagePainter imagePainter : imagePainters )
    {
      width += imagePainter.getPreferredWidth( cell, gc, configRegistry );
    }

    width += m_imageGap;

    final ICellPainter textPainter = createTextPainter( configRegistry );
    width += textPainter.getPreferredWidth( cell, gc, configRegistry );

    return width + m_borderWidth;
  }

  @Override
  public int getPreferredHeight( final LayerCell cell, final GC gc, final IConfigRegistry configRegistry )
  {
    final Object dataValue = cell.getDataValue();

    final ImagePainter[] imagePainters = createImagePainters( dataValue, configRegistry );

    int height = 0;
    for( final ImagePainter imagePainter : imagePainters )
    {
      final int imageHeight = imagePainter.getPreferredHeight( cell, gc, configRegistry );
      height = Math.max( imageHeight, height );
    }

    final ICellPainter textPainter = createTextPainter( configRegistry );
    height = Math.max( textPainter.getPreferredHeight( cell, gc, configRegistry ), height );

    return height;
  }
}