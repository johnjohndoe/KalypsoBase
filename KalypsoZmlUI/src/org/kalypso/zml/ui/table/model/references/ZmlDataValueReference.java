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
package org.kalypso.zml.ui.table.model.references;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.ui.table.binding.DataColumn;
import org.kalypso.zml.ui.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.table.model.IZmlModelRow;

/**
 * @author Dirk Kuch
 */
public class ZmlDataValueReference implements IZmlValueReference
{
  private final IZmlModelColumn m_column;

  private final int m_tupleModelIndex;

  private final IZmlModelRow m_row;

  protected ZmlDataValueReference( final IZmlModelRow row, final IZmlModelColumn column, final int tupleModelIndex )
  {
    m_row = row;
    m_column = column;
    m_tupleModelIndex = tupleModelIndex;
  }

  public Object getIndexValue( ) throws SensorException
  {
    final DataColumn type = m_column.getDataColumn();
    final IAxis[] axes = m_column.getAxes();
    final IAxis axis = AxisUtils.findAxis( axes, type.getIndexAxis() );

    return m_column.get( m_tupleModelIndex, axis );
  }

  @Override
  public Object getValue( ) throws SensorException
  {
    return m_column.get( m_tupleModelIndex, getValueAxis() );
  }

  @Override
  public void update( final Object value ) throws SensorException
  {
    m_column.update( m_tupleModelIndex, value );
  }

  @Override
  public IAxis getValueAxis( )
  {
    final DataColumn type = m_column.getDataColumn();
    final IAxis[] axes = m_column.getAxes();

    return AxisUtils.findAxis( axes, type.getValueAxis() );
  }

  public String getIdentifier( )
  {
    return m_column.getIdentifier();
  }

  @Override
  public Integer getStatus( ) throws SensorException
  {
    final IAxis axis = KalypsoStatusUtils.findStatusAxisFor( m_column.getAxes(), getValueAxis() );

    final Object value = m_column.get( m_tupleModelIndex, axis );
    if( value instanceof Number )
      return ((Number) value).intValue();

    return null;
  }

  @Override
  public MetadataList[] getMetadata( )
  {
    return new MetadataList[] { m_column.getMetadata() };
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.IZmlValueReference#isMetadataSource()
   */
  @Override
  public boolean isMetadataSource( )
  {
    return m_column.isMetadataSource();
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlValueReference#getBaseColumn()
   */
  @Override
  public DataColumn getBaseColumn( )
  {
    return m_column.getDataColumn();
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlValueReference#getColumn()
   */
  @Override
  public IZmlModelColumn getColumn( )
  {
    return m_column;
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlValueReference#getRow()
   */
  @Override
  public IZmlModelRow getRow( )
  {
    return m_row;
  }

}
