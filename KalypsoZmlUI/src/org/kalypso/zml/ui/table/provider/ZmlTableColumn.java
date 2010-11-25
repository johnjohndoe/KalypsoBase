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
package org.kalypso.zml.ui.table.provider;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.template.IObsProvider;
import org.kalypso.ogc.sensor.template.IObsProviderListener;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.timeseries.datasource.IDataSourceItem;
import org.kalypso.zml.ui.table.IZmlColumnModel;
import org.kalypso.zml.ui.table.binding.DataColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlTableColumn implements IObsProviderListener
{
  private final IObsProvider m_provider;

  private final IZmlColumnModel m_tabelModel;

  private final DataColumn m_type;

  private ITupleModel m_model;

  private final String m_label;

  public ZmlTableColumn( final String label, final IZmlColumnModel tabelModel, final DataColumn type, final IObsProvider provider )
  {
    m_label = label;
    m_tabelModel = tabelModel;
    m_provider = provider;
    m_type = type;
  }

  public void dispose( )
  {
    m_provider.removeListener( this );
    m_provider.dispose();
  }

  public String getIdentifier( )
  {
    return m_type.getIdentifier();
  }

  public Object get( final int index, final IAxis axis ) throws SensorException
  {
    return getModel().get( index, axis );
  }

  private ITupleModel getModel( ) throws SensorException
  {
    if( m_model == null )
    {
      final IObservation observation = m_provider.getObservation();
      m_model = observation.getValues( null );
    }

    return m_model;
  }

  private boolean isTargetAxis( final IAxis axis )
  {
    return axis.getType().equals( m_type.getValueAxis() );
  }

  public int size( ) throws SensorException
  {
    return getModel().size();
  }

  public void update( final int index, final Object value ) throws SensorException
  {
    final ITupleModel model = getModel();
    final IAxis[] axes = model.getAxisList();

    for( final IAxis axis : axes )
    {
      if( AxisUtils.isDataSrcAxis( axis ) )
      {
        final DataSourceHandler handler = new DataSourceHandler( getMetadata() );
        final int source = handler.addDataSource( IDataSourceItem.SOURCE_MANUAL_CHANGED, IDataSourceItem.SOURCE_MANUAL_CHANGED );

        model.set( index, axis, source );
      }
      else if( AxisUtils.isStatusAxis( axis ) )
      {
        model.set( index, axis, KalypsoStati.BIT_USER_MODIFIED );
      }
      else if( isTargetAxis( axis ) )
      {
        model.set( index, axis, value );
      }
    }

    // FIXME improve update value handling
    final IObservation observation = m_provider.getObservation();
    observation.setValues( model );
    observation.fireChangedEvent( this );
  }

  public void observationChanged( final IObservation obs, final Object source )
  {
    m_tabelModel.fireModelChanged();
  }

  public MetadataList getMetadata( )
  {
    return m_provider.getObservation().getMetadataList();
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsProviderListener#observationReplaced()
   */
  @Override
  public void observationReplaced( )
  {
    m_tabelModel.fireModelChanged();
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsProviderListener#observationChanged(java.lang.Object)
   */
  @Override
  public void observationChanged( final Object source )
  {
    m_tabelModel.fireModelChanged();
  }

  public DataColumn getDataColumn( )
  {
    return m_type;
  }

  public String getLabel( )
  {
    return m_label;
  }

  public boolean isMetadataSource( )
  {
    return m_type.isMetadataSource();
  }
}
