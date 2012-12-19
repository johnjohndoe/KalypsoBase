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
package org.kalypso.zml.core.table.model.loader;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.table.binding.DataColumn;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.ZmlModel;
import org.kalypso.zml.core.table.model.ZmlModelRow;
import org.kalypso.zml.core.table.model.references.ZmlModelValueCell;

/**
 * @author Dirk Kuch
 */
public class ZmlRowBuilder
{
  private final ZmlModel m_model;

  public ZmlRowBuilder( final ZmlModel model )
  {
    m_model = model;
  }

  public Map<Date, IZmlModelRow> execute( )
  {
    final Map<Date, IZmlModelRow> rows = Collections.synchronizedMap( new TreeMap<Date, IZmlModelRow>() );

    for( final IZmlModelColumn column : m_model.getAvailableColumns() )
    {
      final DataColumn type = column.getDataColumn();
      final IAxis[] axes = column.getAxes();
      final IAxis indexAxis = AxisUtils.findAxis( axes, type.getIndexAxis() );

      try
      {
        for( int modelIndex = 0; modelIndex < column.size(); modelIndex++ )
        {
          final Date indexValue = (Date) column.get( modelIndex, indexAxis );

          IZmlModelRow row = rows.get( indexValue );
          if( Objects.isNull( row ) )
          {
            row = new ZmlModelRow( m_model, indexValue );
            rows.put( indexValue, row );
          }

          ((ZmlModelRow) row).add( new ZmlModelValueCell( row, column, modelIndex ) );
        }
      }
      catch( final SensorException e )
      {
        KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return rows;
  }
}
