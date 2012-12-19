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
package org.kalypso.zml.ui.table.nat.base;

import java.text.SimpleDateFormat;

import net.sourceforge.nattable.data.convert.DisplayConverter;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlModelIndexCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.labeling.IZmlModelCellLabelProvider;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;

/**
 * @author Dirk Kuch
 */
public class ZmlModelCellDisplayConverter extends DisplayConverter
{
  private final ZmlModelViewport m_model;

  public ZmlModelCellDisplayConverter( final ZmlModelViewport model )
  {
    m_model = model;
  }

  @Override
  public Object canonicalToDisplayValue( final Object canonicalValue )
  {
    if( Objects.isNull( canonicalValue ) )
      return StringUtils.EMPTY;
    else if( canonicalValue instanceof IZmlModelIndexCell )
    {
      final SimpleDateFormat sdf = new SimpleDateFormat();
      sdf.setTimeZone( KalypsoCorePlugin.getDefault().getTimeZone() );

      return sdf.format( ((IZmlModelIndexCell) canonicalValue).getIndexValue() );
    }
    else if( canonicalValue instanceof IZmlModelValueCell )
    {
      final IZmlModelValueCell cell = (IZmlModelValueCell) canonicalValue;
       if( isPolderControl( cell ) )
      {
        try
        {
          return cell.getValue();
        }
        catch( final SensorException e )
        {
          e.printStackTrace();
        }

        return false;
      }
      
      if( isPolderControl( cell ) )
      {
        try
        {
          return cell.getValue();
        }
        catch( final SensorException e )
        {
          e.printStackTrace();
        }

        return false;
      }

      final IZmlModelCellLabelProvider provider = cell.getStyleProvider();

      return provider.getText( m_model, cell );
    }
    else if( canonicalValue instanceof IZmlModelColumn )
    {
      final IZmlModelColumn column = (IZmlModelColumn) canonicalValue;

      return column.getLabel();
    }

    return canonicalValue.toString();
  }

  private boolean isPolderControl( final IZmlModelValueCell cell )
  {
    final IZmlModelColumn column = cell.getColumn();
    if( Objects.isNull( column ) )
      return false;

    final String type = column.getDataColumn().getValueAxis();

    return StringUtils.equalsIgnoreCase( ITimeseriesConstants.TYPE_POLDER_CONTROL, type );
  }

  @Override
  public Object displayToCanonicalValue( final Object displayValue )
  {
    return displayValue;
  }

}
