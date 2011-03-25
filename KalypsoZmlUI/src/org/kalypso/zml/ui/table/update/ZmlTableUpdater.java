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
package org.kalypso.zml.ui.table.update;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.IObsProviderListener;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.IClonedColumn;
import org.kalypso.zml.core.table.binding.TableTypeHelper;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.ui.core.element.ZmlLinkDiagramElement;
import org.kalypso.zml.ui.core.zml.MultipleTsLink;
import org.kalypso.zml.ui.core.zml.TSLinkWithName;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.ZmlTableColumnBuilder;
import org.kalypso.zml.ui.table.memento.ILabeledObsProvider;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlTableUpdater implements Runnable
{
  private final IObsProviderListener m_obsListenber = new IObsProviderListener()
  {
    @Override
    public void observationReplaced( )
    {
    }

    @Override
    public void observationChanged( final Object source )
    {
      handleObservationChanged();
    }
  };

  private final MultipleTsLink[] m_links;

  private final IZmlTableLayoutPart m_part;

  private final UIJob m_resetUpdateJob = new UIJob( "Update Reset Button" )
  {
    @Override
    public IStatus runInUIThread( final IProgressMonitor monitor )
    {
      updateResetButtons();
      return Status.OK_STATUS;
    }
  };

  public ZmlTableUpdater( final IZmlTableLayoutPart part, final MultipleTsLink[] links )
  {
    m_part = part;
    m_links = links;
  }


  protected void handleObservationChanged( )
  {
    m_resetUpdateJob.cancel();
    m_resetUpdateJob.schedule( 100 );
  }

  protected void updateResetButtons( )
  {
    m_part.onObservationChanged();
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

      final String identifier = multipleLink.getIdentifier();

      for( int index = 0; index < links.length; index++ )
      {
        final TSLinkWithName link = links[index];
        update( link, identifier, index );
      }
    }
  }

  private void update( final TSLinkWithName link, final String identifier, final int index )
  {
    final ZmlLinkDiagramElement element = createZmlDiagrammElement( link, identifier, index );
    final IObsProvider clonedProvider = element.getObsProvider().copy();

    m_part.getModel().loadColumn( element );

    // REMARK: no need to unregister that listener: the memento will dispose the 'clonedProvider'
    clonedProvider.addListener( m_obsListenber );


    final ILabeledObsProvider obsWithLabel = new TsLinkObsProvider( link, clonedProvider );
    final IPoolableObjectType poolKey = element.getPoolKey();
    m_part.getMemento().register( poolKey, obsWithLabel );
  }

  private ZmlLinkDiagramElement createZmlDiagrammElement( final TSLinkWithName link, final String identifier, final int index )
  {
    if( index == 0 )
      return new ZmlLinkDiagramElement( link );

    final String multipleIdentifier = duplicateColumn( identifier, index );
    return new ZmlLinkDiagramElement( link )
    {
      @Override
      public String getIdentifier( )
      {
        return multipleIdentifier;
      }
    };
  }

  public String duplicateColumn( final String identifier, final int index )
  {
    final String multipleIdentifier = String.format( IClonedColumn.CLONED_COLUMN_POSTFIX_FORMAT, identifier, index );
    final IZmlTable table = m_part.getTable();

    // column already exists?
    final IZmlTableColumn[] columns = table.getColumns();
    for( final IZmlTableColumn column : columns )
    {
      final BaseColumn columnType = column.getColumnType();
      if( columnType.getIdentifier().equals( multipleIdentifier ) )
        return multipleIdentifier;
    }

    final AbstractColumnType base = TableTypeHelper.finColumn( m_part.getModel().getTableType(), identifier );
    final AbstractColumnType clone = TableTypeHelper.cloneColumn( base );
    clone.setId( multipleIdentifier );

    /** only one rule / style set! */
    final ZmlTableColumnBuilder builder = new ZmlTableColumnBuilder( m_part.getTable(), new BaseColumn( base )
    {
      @Override
      public String getIdentifier( )
      {
        return multipleIdentifier;
      }

      @Override
      public AbstractColumnType getType( )
      {
        return clone;
      }
    } );

    builder.execute( new NullProgressMonitor() );

    return multipleIdentifier;
  }
}
