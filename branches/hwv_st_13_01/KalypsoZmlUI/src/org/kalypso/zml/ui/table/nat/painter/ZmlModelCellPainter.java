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

import java.awt.Insets;

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.AbstractCellPainter;
import net.sourceforge.nattable.painter.cell.CheckBoxPainter;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.painter.cell.ImagePainter;
import net.sourceforge.nattable.painter.cell.TextPainter;
import net.sourceforge.nattable.style.BorderStyle;
import net.sourceforge.nattable.style.BorderStyle.LineStyleEnum;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.HorizontalAlignmentEnum;
import net.sourceforge.nattable.style.IStyle;
import net.sourceforge.nattable.style.Style;
import net.sourceforge.nattable.util.GUIHelper;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.labeling.IZmlModelCellLabelProvider;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;

/**
 * @author Dirk Kuch
 */
public class ZmlModelCellPainter extends AbstractCellPainter
{
  private final ZmlModelViewport m_viewport;

  private final Insets m_insets = new Insets( 0, 0, 0, 0 );

  public ZmlModelCellPainter( final ZmlModelViewport viewport )
  {
    m_viewport = viewport;
  }

  @Override
  public void paintCell( final LayerCell cell, final GC gc, final Rectangle bounds, final IConfigRegistry configRegistry )
  {
    final Object dataValue = cell.getDataValue();
    if( dataValue == null )
      return;

    if( !(dataValue instanceof IZmlModelValueCell) )
      throw new UnsupportedOperationException();

    final IZmlModelValueCell modelCell = (IZmlModelValueCell) dataValue;
    final IStyle cellStyle = getCellStyle( modelCell );

    final HorizontalAlignmentEnum baseAlignment = cellStyle.getAttributeValue( CellStyleAttributes.HORIZONTAL_ALIGNMENT );
    final boolean isCentered = HorizontalAlignmentEnum.CENTER.equals( baseAlignment );

    Rectangle ptr = new Rectangle( bounds.x - m_insets.left, bounds.y - m_insets.top, bounds.width - m_insets.left - m_insets.right, bounds.height - m_insets.top - m_insets.bottom );
    ptr = move( ptr, m_insets.left );

    try
    {
      final ImagePainter[] imagePainters = createImagePainters( modelCell, configRegistry );

      for( final ImagePainter imagePainter : imagePainters )
      {
        imagePainter.paintCell( cell, gc, ptr, configRegistry );
        final int imageWidth = imagePainter.getPreferredWidth( cell, gc, configRegistry );
        ptr = move( ptr, imageWidth );
        if( isCentered )
        {
          /*
           * differ between assigned alignments. Example: alignment is CENTER - the centered text / check box should
           * always be drawn in the center of the complete control
           */
          ptr = new Rectangle( ptr.x, ptr.y, ptr.width - imageWidth, ptr.height );
        }
      }
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

    final ICellPainter painter = getPainter( modelCell, cellStyle, configRegistry );
    painter.paintCell( cell, gc, ptr, configRegistry );
  }

  private IStyle getCellStyle( final IZmlModelValueCell modelCell )
  {
    final IZmlModelCellLabelProvider provider = modelCell.getStyleProvider();

    return provider.getStyle( m_viewport, modelCell );
  }

  private ImagePainter[] createImagePainters( final IZmlModelValueCell modelCell, final IConfigRegistry configRegistry ) throws SensorException
  {
    final IZmlModelCellLabelProvider provider = modelCell.getStyleProvider();

    final Style imageCellStyle = provider.getStyle( m_viewport, modelCell );

    imageCellStyle.setAttributeValue( CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT );
    configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_STYLE, imageCellStyle, DisplayMode.NORMAL, GridRegion.BODY.toString() );

    final Image[] images = provider.getImages( m_viewport, modelCell );
    final ImagePainter[] painters = new ImagePainter[images.length];
    for( int i = 0; i < painters.length; i++ )
      painters[i] = new ImagePainter( images[i] );

    return painters;
  }

  private ICellPainter getPainter( final IZmlModelValueCell modelCell, final IStyle cellStyle, final IConfigRegistry configRegistry )
  {
    final BorderStyle borderStyle = new BorderStyle( 10, GUIHelper.COLOR_WHITE, LineStyleEnum.SOLID );
    cellStyle.setAttributeValue( CellStyleAttributes.BORDER_STYLE, borderStyle );

    configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, GridRegion.BODY.toString() );

    final IZmlModelColumn column = modelCell.getColumn();
    if( column == null )
      return new TextPainter();

    final String axis = column.getDataColumn().getValueAxis();
    if( StringUtils.equalsIgnoreCase( ITimeseriesConstants.TYPE_POLDER_CONTROL, axis ) )
      return new CheckBoxPainter();

    return new TextPainter();
  }

  private Rectangle move( final Rectangle ptr, final int width )
  {
    return new Rectangle( ptr.x + width, ptr.y, ptr.width - width, ptr.height );
  }

  @Override
  public int getPreferredWidth( final LayerCell cell, final GC gc, final IConfigRegistry configRegistry )
  {
    final Object dataValue = cell.getDataValue();
    if( dataValue == null )
      return 0;

    if( !(dataValue instanceof IZmlModelValueCell) )
      throw new UnsupportedOperationException();

    final IZmlModelValueCell modelCell = (IZmlModelValueCell) dataValue;
    final IStyle cellStyle = getCellStyle( modelCell );

    int width = 0;

    try
    {
      final ImagePainter[] imagePainters = createImagePainters( modelCell, configRegistry );

      for( final ImagePainter imagePainter : imagePainters )
        width += imagePainter.getPreferredWidth( cell, gc, configRegistry );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

    final ICellPainter painter = getPainter( modelCell, cellStyle, configRegistry );
    final int imageWidth = painter.getPreferredWidth( cell, gc, configRegistry );
    width += imageWidth;

    return width + m_insets.left + m_insets.right;
  }

  @Override
  public int getPreferredHeight( final LayerCell cell, final GC gc, final IConfigRegistry configRegistry )
  {
    final Object dataValue = cell.getDataValue();
    if( dataValue == null )
      return 0;

    if( !(dataValue instanceof IZmlModelValueCell) )
      throw new UnsupportedOperationException();

    final IZmlModelValueCell modelCell = (IZmlModelValueCell) dataValue;
    final IStyle cellStyle = getCellStyle( modelCell );

    int height = 0;

    try
    {
      final ImagePainter[] imagePainters = createImagePainters( modelCell, configRegistry );

      for( final ImagePainter imagePainter : imagePainters )
      {
        final int imageHeight = imagePainter.getPreferredHeight( cell, gc, configRegistry );
        height = Math.max( imageHeight, height );
      }
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

    final ICellPainter painter = getPainter( modelCell, cellStyle, configRegistry );
    final int textHeight = painter.getPreferredHeight( cell, gc, configRegistry );
    height = Math.max( textHeight, height );

    return height + m_insets.top + m_insets.bottom;
  }
}