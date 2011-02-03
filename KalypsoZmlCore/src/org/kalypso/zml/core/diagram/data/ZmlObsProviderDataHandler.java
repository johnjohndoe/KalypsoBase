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
package org.kalypso.zml.core.diagram.data;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.IObsProviderListener;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.zml.core.diagram.base.LayerProviderUtils;
import org.kalypso.zml.core.diagram.layer.IZmlLayer;

import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;

/**
 * @author Dirk Kuch
 */
public class ZmlObsProviderDataHandler implements IZmlLayerDataHandler
{
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

  private final IZmlLayer m_layer;

  private IObsProvider m_provider;

  private ITupleModel m_model;

  private final String m_targetAxisId;

  private IAxis m_valueAxis;

  public ZmlObsProviderDataHandler( final IZmlLayer layer, final String targetAxisId )
  {
    m_layer = layer;
    m_targetAxisId = targetAxisId;

    m_layer.setDataHandler( this );
  }

  public void setObsProvider( final IObsProvider provider )
  {
    if( m_provider != null )
    {
      m_provider.removeListener( m_observationProviderListener );
      m_provider.dispose(); // TODO check - really dispose old provider?
    }

    m_provider = provider;
    m_model = null;

    if( provider != null )
    {
      provider.addListener( m_observationProviderListener );

      if( !provider.isLoaded() )
        m_layer.setVisible( false );
    }

    m_layer.getEventHandler().fireLayerContentChanged( m_layer );
  }

  @Override
  public IAxis getValueAxis( )
  {
    if( m_valueAxis == null )
      m_valueAxis = LayerProviderUtils.getValueAxis( m_provider, m_targetAxisId );

    return m_valueAxis;
  }

  protected void onObservationLoaded( )
  {
    m_model = null;

    final IObservation observation = m_provider.getObservation();
    m_layer.setVisible( observation != null );

    m_layer.getEventHandler().fireLayerVisibilityChanged( m_layer );
    m_layer.getEventHandler().fireLayerContentChanged( m_layer );
  }

  protected void onObservationChanged( )
  {
    m_model = null;
    m_layer.getEventHandler().fireLayerContentChanged( m_layer );
  }

  /**
   * @see org.kalypso.zml.ui.chart.data.IZmlLayerDataHandler#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_provider != null )
    {
      m_provider.removeListener( m_observationProviderListener );
      m_provider.dispose();
    }
  }

  @Override
  public ITupleModel getModel( ) throws SensorException
  {
    if( m_provider == null )
      return null;

    if( m_model == null )
    {

      final IRequest request = getRequest();
      final IObservation observation = m_provider.getObservation();
      if( observation != null )
        m_model = observation.getValues( request );
    }

    return m_model;
  }

  private IRequest getRequest( )
  {
    final ILayerProvider layerProvider = m_layer.getProvider();
    if( layerProvider == null )
      return m_provider.getArguments();

    final ZmlLayerRequestHandler handler = new ZmlLayerRequestHandler( layerProvider.getParameterContainer() );
    final IObservation observation = getObservation();
    if( observation == null )
      return m_provider.getArguments();

    return handler.getArguments( observation.getMetadataList() );
  }

  /**
   * @see org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler#getTargetAxisId()
   */
  @Override
  public String getTargetAxisId( )
  {
    return m_targetAxisId;
  }

  /**
   * @see org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler#getObservation()
   */
  @Override
  public IObservation getObservation( )
  {
    if( m_provider == null )
      return null;

    return m_provider.getObservation();
  }
}
