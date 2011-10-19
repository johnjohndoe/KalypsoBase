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
package org.kalypso.zml.ui.table.layout;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableColumn;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.ColumnHeader;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.provider.AppliedRule;
import org.kalypso.zml.ui.table.provider.ZmlTableImage;
import org.kalypso.zml.ui.table.provider.ZmlTableImageMerger;
import org.kalypso.zml.ui.table.provider.strategy.IExtendedZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class PackTableColumnVisitor extends AbstractTableColumnPackVisitor
{
  Set<IExtendedZmlTableColumn> m_indexColumns = new HashSet<IExtendedZmlTableColumn>();

  /**
   * @see org.kalypso.zml.ui.table.IZmlTableColumnVisitor#visit(org.kalypso.zml.ui.table.provider.strategy.IExtendedZmlTableColumn)
   */
  @Override
  public void visit( final IExtendedZmlTableColumn column )
  {
    if( column.isIndexColumn() )
    {
      m_indexColumns.add( column );
      return;
    }

    final BaseColumn columnType = column.getColumnType();
    final TableViewerColumn tableViewerColumn = column.getTableViewerColumn();
    final TableColumn tableColumn = tableViewerColumn.getColumn();

    final IZmlModelColumn modelColumn = column.getModelColumn();
    if( Objects.isNull( modelColumn ) )
    {
      hide( tableColumn );
    }
    else
    {
      if( !isVisible( modelColumn ) )
        hide( tableColumn );
      else
      {
        /** only update headers of data column types */
        updateHeader( column );

        final String label = modelColumn.getLabel();
        tableColumn.setText( label );

        pack( tableColumn, columnType, label, isVisible( modelColumn ) );
      }
    }

  }

  private void updateHeader( final IExtendedZmlTableColumn column )
  {
    final TableColumn tableColumn = column.getTableViewerColumn().getColumn();

    final ZmlTableImageMerger provider = new ZmlTableImageMerger( 1 );

    final BaseColumn columnType = column.getColumnType();
    for( final ColumnHeader header : columnType.getHeaders() )
    {
      try
      {
        final Image icon = header.getIcon();
        if( Objects.isNotNull( icon ) )
          provider.addImage( new ZmlTableImage( header.getIdentifier(), icon ) );
      }
      catch( final Throwable t )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }

    final AppliedRule[] rules = column.getAppliedRules();
    for( final AppliedRule rule : rules )
    {
      try
      {
        if( rule.hasHeaderIcon() )
        {
          final CellStyle style = rule.getCellStyle();
          final Image image = style.getImage();
          if( Objects.isNotNull( image ) )
            provider.addImage( new ZmlTableImage( style.getIdentifier(), image ) );
        }
      }
      catch( final Throwable t )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }

    tableColumn.setImage( provider.createImage( tableColumn.getDisplay() ) );
  }

}
