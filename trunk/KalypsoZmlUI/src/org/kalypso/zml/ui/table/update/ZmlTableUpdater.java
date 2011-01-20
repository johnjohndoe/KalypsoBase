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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.TableTypeHelper;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.ui.core.element.ZmlLinkDiagramElement;
import org.kalypso.zml.ui.core.zml.MultipleTsLink;
import org.kalypso.zml.ui.core.zml.TSLinkWithName;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.ZmlTableColumnBuilder;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;

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

  /**
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run( )
  {
    for( final MultipleTsLink multipleLink : m_links )
    {

      if( multipleLink.isIgnoreType( m_part.getIgnoreTypes() ) )
        continue;

      final TSLinkWithName[] links = multipleLink.getLinks();
      if( ArrayUtils.isEmpty( links ) )
        continue;
      else if( links.length == 1 )
      {
        final ZmlLinkDiagramElement element = new ZmlLinkDiagramElement( links[0] );
        m_part.getModel().loadColumn( element );
        m_part.getMemento().register( element );
      }
      else
      {
        final String identifier = multipleLink.getIdentifier();

        for( int index = 0; index < links.length; index++ )
        {
          if( index == 0 )
          {
            final ZmlLinkDiagramElement element = new ZmlLinkDiagramElement( links[index] );
            m_part.getModel().loadColumn( element );
            m_part.getMemento().register( element );
          }
          else
          {
            final String multipleIdentifier = String.format( "%s(%d)", identifier, index );
            duplicateColumn( identifier, multipleIdentifier );

            final ZmlLinkDiagramElement element = new ZmlLinkDiagramElement( links[index] )
            {
              @Override
              public String getIdentifier( )
              {
                return multipleIdentifier;
              }
            };

            m_part.getModel().loadColumn( element );
            m_part.getMemento().register( element );
          }
        }
      }
    }
  }

  public void duplicateColumn( final String identifier, final String newIdentifier )
  {
    final IZmlTable table = m_part.getTable();

    // column already exists?
    for( final IZmlTableColumn column : table.getColumns() )
    {
      final BaseColumn columnType = column.getColumnType();
      if( columnType.getIdentifier().equals( newIdentifier ) )
        return;
    }

    final AbstractColumnType base = TableTypeHelper.finColumn( m_part.getModel().getTableType(), identifier );
    final AbstractColumnType clone = TableTypeHelper.cloneColumn( base );
    clone.setId( newIdentifier );

    /** only one rule / style set! */
    final ZmlTableColumnBuilder builder = new ZmlTableColumnBuilder( m_part.getTable(), new BaseColumn( base )
    {
      @Override
      public String getIdentifier( )
      {
        return newIdentifier;
      }

      @Override
      public AbstractColumnType getType( )
      {
        return clone;
      }
    } );

    builder.execute( new NullProgressMonitor() );

  }

}
