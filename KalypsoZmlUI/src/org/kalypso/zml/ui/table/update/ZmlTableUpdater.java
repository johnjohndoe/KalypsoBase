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
package org.kalypso.zml.ui.table.update;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.base.IMultipleZmlSourceElement;
import org.kalypso.zml.core.base.IZmlSourceElement;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.TableTypes;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.ZmlModel;
import org.kalypso.zml.core.table.model.utils.IClonedColumn;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.base.helper.ZmlTables;

/**
 * @author Dirk Kuch
 */
public class ZmlTableUpdater implements Runnable
{
  private final IMultipleZmlSourceElement[] m_sources;

  private final IZmlTable m_table;

  public ZmlTableUpdater( final IZmlTable table, final IMultipleZmlSourceElement[] sources )
  {
    m_table = table;
    m_sources = sources;
  }

  @Override
  public void run( )
  {
    /** tricky: map is used for restoring the order of columns from the underlying calcWizard.xml */
    final Map<Integer, Object[]> map = new TreeMap<Integer, Object[]>();

    for( final IMultipleZmlSourceElement multipleLink : m_sources )
    {
      final IZmlSourceElement[] links = multipleLink.getSources();
      if( ArrayUtils.isEmpty( links ) )
        continue;

      final String baseTypeIdentifier = multipleLink.getIdentifier();

      for( int index = 0; index < links.length; index++ )
      {
        final IZmlSourceElement source = links[index];
        final BaseColumn column = toBaseColumn( baseTypeIdentifier, index );
        if( Objects.isNull( column ) )
          continue;

        source.setIdentifier( column.getIdentifier() ); // update multiple selection index!

        final int tableIndex = source.getIndex();
        map.put( tableIndex, new Object[] { source, column } );
      }
    }

    final Set<Entry<Integer, Object[]>> entries = map.entrySet();
    for( final Entry<Integer, Object[]> entry : entries )
    {
      final Object[] values = entry.getValue();

      final IZmlSourceElement source = (IZmlSourceElement) values[0];
      final BaseColumn column = (BaseColumn) values[1];

      doLoadModelColumn( source );
      ZmlTables.addTableColumn( m_table, column );
    }
  }

  private void doLoadModelColumn( final IZmlSourceElement source )
  {
    final ZmlModel model = (ZmlModel) m_table.getModel();
    model.getLoader().load( source );
    model.getMemento().register( source );
  }

  private BaseColumn toBaseColumn( final String baseTypeIdentifier, final int index )
  {
    final IZmlModel model = m_table.getModel();
    final AbstractColumnType baseColumnType = model.getColumnType( baseTypeIdentifier );
    if( Objects.isNull( baseColumnType ) )
      return null;

    if( index == 0 )
    {
      return new BaseColumn( baseColumnType );
    }

    final String multipleIdentifier = String.format( IClonedColumn.CLONED_COLUMN_POSTFIX_FORMAT, baseTypeIdentifier, index );

    final AbstractColumnType clonedColumnType = TableTypes.cloneColumn( baseColumnType );
    clonedColumnType.setId( multipleIdentifier );

    return new BaseColumn( clonedColumnType )
    {
      @Override
      public String getIdentifier( )
      {
        return multipleIdentifier;
      }

      @Override
      public AbstractColumnType getType( )
      {
        return clonedColumnType;
      }
    };
  }

}
