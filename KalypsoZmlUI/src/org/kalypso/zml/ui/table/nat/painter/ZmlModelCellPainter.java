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

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.AbstractCellPainter;
import net.sourceforge.nattable.painter.cell.CheckBoxPainter;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.painter.cell.ImagePainter;
import net.sourceforge.nattable.painter.cell.TextPainter;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.HorizontalAlignmentEnum;
import net.sourceforge.nattable.style.Style;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.commons.java.lang.Objects;
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

  public ZmlModelCellPainter( final ZmlModelViewport viewport )
  {
    m_viewport = viewport;
  }

  @Override
  public void paintCell( final LayerCell cell, final GC gc, final Rectangle bounds, final IConfigRegistry configRegistry )
  {
    final Object object = cell.getDataValue();
    if( object instanceof IZmlModelValueCell )
    {
      final IZmlModelValueCell modelCell = (IZmlModelValueCell) object;
      final IZmlModelCellLabelProvider provider = modelCell.getStyleProvider();

      final Style imageCellStyle = provider.getStyle( m_viewport, modelCell );

      imageCellStyle.setAttributeValue( CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT );
      configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_STYLE, imageCellStyle, DisplayMode.NORMAL, GridRegion.BODY.toString() );

      final Style cellStyle = provider.getStyle( m_viewport, modelCell );

      Rectangle ptr = new Rectangle( bounds.x, bounds.y, bounds.width, bounds.height );

      try
      {
        final Image[] images = provider.getImages( m_viewport, modelCell );
        for( final Image image : images )
        {
          final ImagePainter imgPainter = new ImagePainter( image );
          imgPainter.paintCell( cell, gc, ptr, configRegistry );

          ptr = move( ptr, image.getBounds(), cellStyle );
        }

      }
      catch( final SensorException e )
      {
        e.printStackTrace();
      }

      configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, GridRegion.BODY.toString() );

      final ICellPainter painter = getPainter( modelCell );
      painter.paintCell( cell, gc, ptr, configRegistry );
    }

  }

  private ICellPainter getPainter( final IZmlModelValueCell cell )
  {
    final IZmlModelColumn column = cell.getColumn();
    if( Objects.isNull( column ) )
      return new TextPainter();

    final String axis = column.getDataColumn().getValueAxis();
    if( StringUtils.equalsIgnoreCase( ITimeseriesConstants.TYPE_POLDER_CONTROL, axis ) )
      return new CheckBoxPainter();

    return new TextPainter();
  }

  private Rectangle move( final Rectangle ptr, final Rectangle bounds, final Style cellStyle )
  {
    /*
     * differ between assigned alignments. Example: alignment is CENTER - the centered text / check box should always be
     * drawn in the center of the complete control
     */
    final HorizontalAlignmentEnum baseAlignment = cellStyle.getAttributeValue( CellStyleAttributes.HORIZONTAL_ALIGNMENT );

    if( HorizontalAlignmentEnum.CENTER.equals( baseAlignment ) )
      return new Rectangle( ptr.x + bounds.width, ptr.y, ptr.width - bounds.width * 2, ptr.height );

    return new Rectangle( ptr.x + bounds.width, ptr.y, ptr.width - bounds.width, ptr.height );

  }

  @Override
  public int getPreferredWidth( final LayerCell cell, final GC gc, final IConfigRegistry configRegistry )
  {
    final Object object = cell.getDataValue();
    if( object instanceof IZmlModelValueCell )
    {
      final IZmlModelValueCell value = (IZmlModelValueCell) object;
      final IZmlModelCellLabelProvider provider = value.getStyleProvider();

      int width = 0;
      try
      {
        final Image[] images = provider.getImages( m_viewport, value );
        for( final Image image : images )
        {
          width += image.getBounds().width;
        }
      }
      catch( final SensorException e )
      {
        e.printStackTrace();
      }

      final TextPainter painter = new TextPainter();
      width += painter.getPreferredWidth( cell, gc, configRegistry );

      return width;
    }

    return -1;
  }

  @Override
  public int getPreferredHeight( final LayerCell cell, final GC gc, final IConfigRegistry configRegistry )
  {
    final Object object = cell.getDataValue();
    if( object instanceof IZmlModelValueCell )
    {
      final IZmlModelValueCell value = (IZmlModelValueCell) object;
      final IZmlModelCellLabelProvider provider = value.getStyleProvider();

      int height = 0;

      try
      {
        final Image[] images = provider.getImages( m_viewport, value );
        for( final Image image : images )
        {
          final Rectangle bounds = image.getBounds();
          height = Math.max( bounds.height, height );
        }
      }
      catch( final SensorException e )
      {
        e.printStackTrace();
      }

      final TextPainter painter = new TextPainter();
      height = Math.max( painter.getPreferredHeight( cell, gc, configRegistry ), height );

      return height;
    }

    return -1;
  }

}
