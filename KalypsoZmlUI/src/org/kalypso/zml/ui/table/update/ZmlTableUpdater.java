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
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.zml.core.diagram.base.provider.observation.AsynchronousObservationProvider;
import org.kalypso.zml.core.diagram.base.zml.MultipleTsLink;
import org.kalypso.zml.core.diagram.base.zml.TSLinkWithName;
import org.kalypso.zml.core.diagram.base.zml.TsLinkWrapper;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.IClonedColumn;
import org.kalypso.zml.core.table.binding.TableTypes;
import org.kalypso.zml.core.table.model.ZmlModel;
import org.kalypso.zml.core.table.model.memento.ILabeledObsProvider;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.ui.core.element.ZmlLinkDiagramElement;
import org.kalypso.zml.ui.table.base.helper.ZmlTables;

/**
 * @author Dirk Kuch
 */
public class ZmlTableUpdater implements Runnable
{
  private final MultipleTsLink[] m_links;

  private final IZmlTableLayoutPart m_part;

  public ZmlTableUpdater( final IZmlTableLayoutPart part, final MultipleTsLink[] links )
  {
    m_part = part;
    m_links = links;
  }

  @Override
  public void run( )
  {
    /** tricky: map is used for restoring the order of columns from the underlying calcWizard.xml */
    final Map<Integer, Object[]> map = new TreeMap<Integer, Object[]>();

    for( final MultipleTsLink multipleLink : m_links )
    {
// if( link.isIgnoreType( m_part.getIgnoreTypes() ) )
// continue;

      final TsLinkWrapper[] links = multipleLink.getLinks();
      if( ArrayUtils.isEmpty( links ) )
        continue;

      final String baseTypeIdentifier = multipleLink.getIdentifier();

      for( int index = 0; index < links.length; index++ )
      {
        final TsLinkWrapper link = links[index];

        final BaseColumn column = toBaseColumn( baseTypeIdentifier, index );
        final ZmlLinkDiagramElement element = toZmlDiagrammElement( link, column, index );

        final int tableIndex = link.getIndex();
        map.put( tableIndex, new Object[] { link, column, element } );
      }
    }

    final Set<Entry<Integer, Object[]>> entries = map.entrySet();
    for( final Entry<Integer, Object[]> entry : entries )
    {
      final Object[] values = entry.getValue();

      final TsLinkWrapper link = (TsLinkWrapper) values[0];
      final BaseColumn column = (BaseColumn) values[1];
      final ZmlLinkDiagramElement element = (ZmlLinkDiagramElement) values[2];

      doLoadModelColumn( link, element );
      ZmlTables.addTableColumn( m_part.getTable(), column );
    }
  }

  private void doLoadModelColumn( final TSLinkWithName link, final ZmlLinkDiagramElement element )
  {
    final AsynchronousObservationProvider provider = element.getObsProvider();

    m_part.getModel().getLoader().load( element );

    final ILabeledObsProvider obsWithLabel = new TsLinkObsProvider( link, provider.copy() );
    final IPoolableObjectType poolKey = element.getPoolKey();
    m_part.getModel().getMemento().register( poolKey, obsWithLabel );

  }

  private BaseColumn toBaseColumn( final String baseTypeIdentifier, final int index )
  {
    final ZmlModel model = m_part.getModel();
    final AbstractColumnType baseColumnType = model.getColumnType( baseTypeIdentifier );
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

  private ZmlLinkDiagramElement toZmlDiagrammElement( final TSLinkWithName link, final BaseColumn column, final int index )
  {
    if( index == 0 )
      return new ZmlLinkDiagramElement( link );

    return new ZmlLinkDiagramElement( link )
    {
      @Override
      public String getIdentifier( )
      {
        return column.getIdentifier();
      }
    };
  }

}
