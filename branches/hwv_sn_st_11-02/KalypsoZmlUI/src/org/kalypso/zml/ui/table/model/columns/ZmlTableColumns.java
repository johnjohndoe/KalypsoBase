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
package org.kalypso.zml.ui.table.model.columns;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ViewerCell;
import org.kalypso.commons.java.lang.Arrays;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.utils.ZmlModelColumns;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.DataColumnType;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableComposite;
import org.kalypso.zml.ui.table.model.rows.IZmlTableValueRow;

/**
 * @author Dirk Kuch
 */
public final class ZmlTableColumns
{
  private ZmlTableColumns( )
  {
  }

  public static IZmlTableColumn[] toTableColumns( final IZmlTable table, final boolean index, final IZmlModelColumn[] modelColumns )
  {
    final Set<IZmlTableColumn> columns = new HashSet<IZmlTableColumn>();

    final String[] modelIds = toIdentifiers( modelColumns );

    final IZmlTableColumn[] tableColumns = table.getColumns();
    for( final IZmlTableColumn tableColumn : tableColumns )
    {
      if( tableColumn instanceof IZmlTableIndexColumn && index )
        columns.add( tableColumn );
      // columns empty? means refresh all columns
      else if( ArrayUtils.isEmpty( modelColumns ) )
        columns.add( tableColumn );
      else
      {
        final BaseColumn type = tableColumn.getColumnType();
        if( Objects.isNotNull( type ) && ArrayUtils.contains( modelIds, type.getIdentifier() ) )
          columns.add( tableColumn );

      }
    }

    return columns.toArray( new IZmlTableColumn[] {} );
  }

  private static String[] toIdentifiers( final IZmlModelColumn[] columns )
  {
    if( Arrays.isEmpty( columns ) )
      return new String[] {};

    final Set<String> identifiers = new HashSet<String>();
    for( final IZmlModelColumn column : columns )
    {
      identifiers.add( column.getIdentifier() );
    }

    return identifiers.toArray( new String[] {} );
  }

  public static boolean isCloned( final IZmlTableColumn column )
  {
    final IZmlModelColumn modelColumn = column.getModelColumn();
    if( Objects.isNull( modelColumn ) )
      return false;

    return ZmlModelColumns.isCloned( modelColumn );
  }

  public static IZmlTableValueRow toTableRow( final ViewerCell cell )
  {
    final Object element = cell.getElement();
    if( element instanceof IZmlTableValueRow )
    {
      return (IZmlTableValueRow) element;
    }

    return null;
  }

  /**
   * @param identifier
   *          column identifier
   */
  public static boolean hasColumn( final IZmlTableComposite table, final String identifier )
  {
    final IZmlTableColumn[] columns = table.getColumns();
    for( final IZmlTableColumn column : columns )
    {
      if( StringUtils.equals( column.getColumnType().getIdentifier(), identifier ) )
        return true;
    }

    return false;
  }

  public static IZmlModelColumn[] findMissingColumns( final IZmlTableComposite table, final IZmlModelColumn[] modelColumns )
  {
    final Set<IZmlModelColumn> missing = new LinkedHashSet<IZmlModelColumn>();
    for( final IZmlModelColumn modelColumn : modelColumns )
    {

      final IZmlTableColumn tableColumn = findTableColumn( table, modelColumn );
      if( Objects.isNull( tableColumn ) )
        missing.add( modelColumn );

    }

    return missing.toArray( new IZmlModelColumn[] {} );
  }

  private static IZmlTableColumn findTableColumn( final IZmlTableComposite table, final IZmlModelColumn modelColumn )
  {
    final IZmlTableColumn[] columns = table.getColumns();
    for( final IZmlTableColumn column : columns )
    {
      final IZmlModelColumn m = column.getModelColumn();
      if( Objects.isNull( modelColumn, m ) )
        continue;
      else if( StringUtils.equals( modelColumn.getIdentifier(), m.getIdentifier() ) )
        return column;
    }

    return null;
  }

  public static void addTableColumn( final IZmlTableComposite table, final BaseColumn column )
  {
    final IZmlModel model = table.getModel();

    final AbstractColumnType columnType = column.getType();
    if( Objects.isNull( columnType ) )
      return;

    if( columnType instanceof DataColumnType )
    {
      final DataColumnType dataColumnType = (DataColumnType) columnType;

      /** index axis exists? */
      final String indexAxis = dataColumnType.getIndexAxis();
      if( !hasColumn( table, indexAxis ) )
      {
        final AbstractColumnType indexColumnType = model.getColumnType( indexAxis );
        final ZmlTableIndexColumnBuilder builder = new ZmlTableIndexColumnBuilder( table, new BaseColumn( indexColumnType ) );
        builder.execute( new NullProgressMonitor() );
      }
    }

    if( !hasColumn( table, column.getIdentifier() ) )
    {
      final ZmlTableValueColumnBuilder builder = new ZmlTableValueColumnBuilder( table, column );
      builder.execute( new NullProgressMonitor() );
    }
  }

}
