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

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.AbstractCellPainter;
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
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlModelIndexCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;

/**
 * @author Dirk Kuch
 */
public class ZmlRowHeaderCellPainter extends AbstractCellPainter
{

  private final ZmlModelViewport m_viewport;

  public ZmlRowHeaderCellPainter( final ZmlModelViewport viewport )
  {
    m_viewport = viewport;
  }

  @Override
  public void paintCell( final LayerCell cell, final GC gc, final Rectangle bounds, final IConfigRegistry configRegistry )
  {
    final Object object = cell.getDataValue();
    if( object instanceof IZmlModelRow )
    {
      configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_STYLE, getStyle(), DisplayMode.NORMAL, GridRegion.ROW_HEADER.toString() );

      final TextPainter painter = new TextPainter();
      painter.paintCell( cell, gc, bounds, configRegistry );
    }
  }

  private Style getStyle( )
  {

    final Font font = GUIHelper.getFont( new FontData( "Verdana", 10, SWT.NORMAL ) );
    final Color bgColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
    final Color fgColor = GUIHelper.COLOR_WIDGET_FOREGROUND;
    final HorizontalAlignmentEnum hAlign = HorizontalAlignmentEnum.LEFT;
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

  @Override
  public int getPreferredWidth( final LayerCell cell, final GC gc, final IConfigRegistry configRegistry )
  {
    final Object object = cell.getDataValue();
    if( object instanceof IZmlModelIndexCell )
    {
      configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_STYLE, getStyle(), DisplayMode.NORMAL, GridRegion.ROW_HEADER.toString() );

      final TextPainter painter = new TextPainter();
      return painter.getPreferredWidth( cell, gc, configRegistry );
    }

    throw new UnsupportedOperationException();
  }

  @Override
  public int getPreferredHeight( final LayerCell cell, final GC gc, final IConfigRegistry configRegistry )
  {
    final Object object = cell.getDataValue();
    if( object instanceof IZmlModelIndexCell )
    {
      configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_STYLE, getStyle(), DisplayMode.NORMAL, GridRegion.ROW_HEADER.toString() );

      final TextPainter painter = new TextPainter();
      return painter.getPreferredHeight( cell, gc, configRegistry );
    }

    throw new UnsupportedOperationException();
  }

}