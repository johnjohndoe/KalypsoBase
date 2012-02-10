/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.ui.table.provider;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.ZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.model.cells.IZmlTableIndexCell;
import org.kalypso.zml.ui.table.model.cells.IZmlTableValueCell;
import org.kalypso.zml.ui.table.model.columns.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.columns.IZmlTableIndexColumn;
import org.kalypso.zml.ui.table.model.columns.IZmlTableValueColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlTooltipProvider extends ColumnLabelProvider
{
  private final ZmlTooltipSupport m_tooltip;

  private final IZmlTableColumn m_column;

  private final IZmlTable m_table;

  @Override
  public String getText( final Object element )
  {
    return null;
  }

  public ZmlTooltipProvider( final IZmlTable table, final IZmlTableColumn column )
  {
    m_table = table;
    m_column = column;
    m_tooltip = new ZmlTooltipSupport( table, column );
  }

  @Override
  public int getToolTipStyle( final Object object )
  {
    return SWT.TOP | SWT.BEGINNING | SWT.LEFT | SWT.SHADOW_NONE;
  }

  @Override
  public Image getToolTipImage( final Object object )
  {
    if( !m_column.getColumnType().isTooltip() )
      return null;

    if( !ZmlTooltipSupport.isShowTooltips() )
      return null;

    if( !m_column.isVisible() )
      return null;
    else if( object instanceof IZmlModelRow )
    {
      if( m_column instanceof IZmlTableIndexColumn )
      {
        final IZmlTableIndexColumn indexColumn = (IZmlTableIndexColumn) m_column;
        final IZmlTableIndexCell cell = indexColumn.findCell( (IZmlModelRow) object );
        if( Objects.isNotNull( cell ) )
        {
          final IZmlModelCell reference = cell.getValueReference();
          if( Objects.isNotNull( reference ) )
            return m_tooltip.getToolTipImage();
        }
      }
      else if( m_column instanceof IZmlTableValueColumn )
      {
        final IZmlTableValueColumn valueCell = (IZmlTableValueColumn) m_column;
        final IZmlTableValueCell cell = valueCell.findCell( (IZmlModelRow) object );
        if( Objects.isNotNull( cell ) )
        {
          final IZmlModelCell reference = cell.getValueReference();
          if( Objects.isNotNull( reference ) )
            return m_tooltip.getToolTipImage();
        }
      }
    }

    return super.getToolTipImage( object );
  }

  @Override
  public String getToolTipText( final Object element )
  {
    if( !ZmlTooltipSupport.isShowTooltips() )
      return null;

    if( !m_column.getColumnType().isTooltip() )
      return null;

    if( !m_column.isVisible() )
      return null;

    if( element instanceof ZmlModelRow )
    {
      return m_tooltip.getToolTipText( (ZmlModelRow) element );
    }

    return super.getToolTipText( element );
  }

  @Override
  public Point getToolTipShift( final Object object )
  {
    return new Point( 10, 20 );
  }
}
