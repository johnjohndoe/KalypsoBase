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
package org.kalypso.zml.ui.table.nat.pager;

import java.util.Date;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.ILayerListener;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.viewport.command.ShowRowInViewportCommand;
import net.sourceforge.nattable.viewport.event.ScrollEvent;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.event.ZmlModelColumnChangeType;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.ui.table.nat.layers.BodyLayerStack;

/**
 * @author Dirk Kuch
 */
public class ZmlTablePager
{
  private boolean m_firstRun = true;

  private final ZmlModelViewport m_viewport;

  final NatTable m_table;

  private final BodyLayerStack m_bodyLayer;

  protected Date m_lastRow;

  public ZmlTablePager( final ZmlModelViewport viewport, final NatTable table, final BodyLayerStack bodyLayer )
  {
    m_viewport = viewport;
    m_table = table;
    m_bodyLayer = bodyLayer;

    m_table.addLayerListener( new ILayerListener()
    {
      @Override
      public void handleLayerEvent( final ILayerEvent event )
      {
        if( event instanceof ScrollEvent )
        {
          final LayerCell cell = m_table.getCellByPosition( 1, 1 );
          final Object dataValue = cell.getDataValue();
          if( dataValue instanceof IZmlModelCell )
          {
            final IZmlModelCell modelCell = (IZmlModelCell) dataValue;
            m_lastRow = modelCell.getIndexValue();
          }
        }

      }
    } );
  }

  public void update( final ZmlModelColumnChangeType event )
  {
    Date date = null;
    if( m_firstRun )
      date = findForecastDate();

    if( Objects.isNull( date ) )
      date = m_lastRow;

    if( Objects.isNull( date ) )
      return;

    final FindClosestDateVisitor visitor = new FindClosestDateVisitor( date );
    m_viewport.accept( visitor );

    final IZmlModelRow row = visitor.getRow();
    if( Objects.isNull( row ) )
      return;

    final int index = ArrayUtils.indexOf( m_viewport.getRows(), row );
    final ShowRowInViewportCommand command = new ShowRowInViewportCommand( m_bodyLayer, index );
    m_table.doCommand( command );
  }

  private Date findForecastDate( )
  {
    final ZmlModelViewport viewport = m_viewport;
    final IZmlModelColumn[] columns = viewport.getColumns();

    final Date date = findForecastDate( columns );
    if( Objects.isNull( date ) )
      return null;

    m_firstRun = false;

    return date;
  }

  private Date findForecastDate( final IZmlModelColumn[] columns )
  {
    for( final IZmlModelColumn column : columns )
    {
      if( !column.isMetadataSource() )
        continue;

      final Date date = MetadataHelper.getForecastStart( column.getMetadata() );
      if( Objects.isNotNull( date ) )
        return date;
    }

    return null;
  }

}
