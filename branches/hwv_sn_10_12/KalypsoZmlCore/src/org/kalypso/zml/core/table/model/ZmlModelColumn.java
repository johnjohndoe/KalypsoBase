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
package org.kalypso.zml.core.table.model;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.IObsProviderListener;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.timeseries.datasource.IDataSourceItem;
import org.kalypso.zml.core.table.binding.DataColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlModelColumn implements IZmlModelColumn
{
  private final IObsProvider m_provider;

  private final IZmlModel m_zmlModel;

  private final DataColumn m_type;

  private ITupleModel m_tupleModel;

  private final String m_label;

  private final IObsProviderListener m_observationProviderListener = new IObsProviderListener()
  {
    @Override
    public void observationReplaced( )
    {
      onObservationLoaded();
    }

    /**
     * @see org.kalypso.ogc.sensor.template.IObsProviderListener#observationChangedX(java.lang.Object)
     */
    @Override
    public void observationChanged( final Object source )
    {
      onObservationChanged();
    }
  };

  public ZmlModelColumn( final String label, final IObsProvider provider, final IZmlModel tabelModel, final DataColumn type )
  {
    m_label = label;
    m_zmlModel = tabelModel;
    m_provider = provider;
    m_type = type;

    provider.addListener( m_observationProviderListener );
  }

  protected void onObservationChanged( )
  {
    m_zmlModel.fireModelChanged();
  }

  protected void onObservationLoaded( )
  {
    m_zmlModel.fireModelChanged();
  }

  public void dispose( )
  {
    m_provider.removeListener( m_observationProviderListener );
    m_provider.dispose();
  }

  @Override
  public String getIdentifier( )
  {
    return m_type.getIdentifier();
  }

  @Override
  public Object get( final int index, final IAxis axis ) throws SensorException
  {
    return getModel().get( index, axis );
  }

  private ITupleModel getModel( ) throws SensorException
  {
    if( m_tupleModel == null )
    {
      final IObservation observation = m_provider.getObservation();
      m_tupleModel = observation.getValues( null );
    }

    return m_tupleModel;
  }

  private boolean isTargetAxis( final IAxis axis )
  {
    return axis.getType().equals( m_type.getValueAxis() );
  }

  @Override
  public int modelSize( ) throws SensorException
  {
    return getModel().size();
  }

  @Override
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

  @SuppressWarnings("unused")
  public void observationChanged( final IObservation obs, final Object source )
  {
    m_zmlModel.fireModelChanged();
  }

  @Override
  public MetadataList getMetadata( )
  {
    return m_provider.getObservation().getMetadataList();
  }

  @Override
  public DataColumn getDataColumn( )
  {
    return m_type;
  }

  @Override
  public String getLabel( )
  {
    return m_label;
  }

  @Override
  public boolean isMetadataSource( )
  {
    return m_type.isMetadataSource();
  }

  @Override
  public IAxis[] getAxes( )
  {
    return m_provider.getObservation().getAxisList();
  }

  /**
   * @see org.kalypso.zml.ui.table.model.IZmlModelColumn#getObservation()
   */
  @Override
  public IObservation getObservation( )
  {
    return m_provider.getObservation();
  }

  @Override
  public IAxis getValueAxis( )
  {
    return AxisUtils.findAxis( getAxes(), m_type.getValueAxis() );
  }

  /**
   * @see org.kalypso.zml.ui.table.model.IZmlModelColumn#getStatusAxis()
   */
  @Override
  public IAxis getStatusAxis( )
  {
    final IAxis valueAxis = getValueAxis();
    if( valueAxis == null )
      return null;

    return KalypsoStatusUtils.findStatusAxisFor( getAxes(), valueAxis );
  }

  /**
   * @see org.kalypso.zml.ui.table.model.IZmlModelColumn#getZmlModel()
   */
  @Override
  public IZmlModel getZmlModel( )
  {
    return m_zmlModel;
  }
}
