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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.util.pool.PoolableObjectType;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.IObsProviderListener;
import org.kalypso.ogc.sensor.provider.PooledObsProvider;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.zml.core.base.obsprovider.SynchronousObservationProvider;
import org.kalypso.zml.core.base.request.IRequestStrategy;
import org.kalypso.zml.core.diagram.base.IZmlLayer;
import org.kalypso.zml.core.diagram.base.IZmlLayerProvider;
import org.kalypso.zml.core.diagram.base.ZmlLayerProviders;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener.ContentChangeType;
import de.openali.odysseus.chart.framework.model.impl.settings.CHART_DATA_LOADER_STRATEGY;
import de.openali.odysseus.chart.framework.model.impl.settings.IBasicChartSettings;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;

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

    @Override
    public void observationChanged( final Object source )
    {
      onObservationChanged();
    }
  };

  private final IZmlLayer m_layer;

  private IObsProvider m_provider;

  private final String m_targetAxisId;

  private IAxis m_valueAxis;

  private IRequestStrategy m_request;

  public ZmlObsProviderDataHandler( final IZmlLayer layer, final String targetAxisId )
  {
    m_layer = layer;
    m_targetAxisId = targetAxisId;
  }

  @Override
  public void setRequestStrategy( final IRequestStrategy strategy )
  {
    m_request = strategy;
  }

  @Override
  public IRequestStrategy getRequestStrategy( )
  {
    if( m_request == null )
    {
      final IZmlLayer layer = getLayer();
      final IZmlLayerProvider provider = layer.getProvider();

      m_request = provider.getRequestHandler( layer );
    }

    return m_request;
  }

  public void setObsProvider( final IObsProvider provider )
  {
    synchronized( this )
    {
      m_valueAxis = null;

      if( provider == m_provider )
        return;

      if( Objects.isNotNull( m_provider ) )
      {
        m_provider.removeListener( m_observationProviderListener );
        m_provider.dispose();
      }

      if( Objects.isNull( provider ) )
      {
        m_provider = null;
      }
      else
      {
        m_provider = provider.copy();
        m_provider.addListener( m_observationProviderListener );
      }
    }

    m_layer.onObservationChanged( ContentChangeType.all );
  }

  @Override
  public synchronized IAxis getValueAxis( )
  {
    if( m_valueAxis == null )
      m_valueAxis = ZmlLayerProviders.getValueAxis( m_provider, m_targetAxisId );

    return m_valueAxis;
  }

  protected void onObservationLoaded( )
  {
    m_valueAxis = null;

    m_layer.onObservationChanged( ContentChangeType.all );
  }

  protected void onObservationChanged( )
  {
    m_layer.onObservationChanged( ContentChangeType.value );
  }

  @Override
  public synchronized void dispose( )
  {
    if( m_provider != null )
    {
      m_provider.removeListener( m_observationProviderListener );
      m_provider.dispose();
      m_provider = null;
    }
  }

  @Override
  public IRequest getRequest( )
  {
    final IRequestStrategy strategy = getRequestStrategy();

    return strategy.getRequest();
  }

  @Override
  public String getTargetAxisId( )
  {
    return m_targetAxisId;
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    // for thread-safety, we first fetch the provider once
    final IObsProvider provider = getProvider();

    if( adapter.isAssignableFrom( IObsProvider.class ) )
      return provider;

    if( adapter.isAssignableFrom( IObservation.class ) )
    {
      if( provider != null )
        return provider.getObservation();
    }

    return null;
  }

  private synchronized IObsProvider getProvider( )
  {
    return m_provider;
  }

  public void load( final IZmlLayerProvider provider, final URL context ) throws MalformedURLException, SensorException, URISyntaxException
  {
    final IParameterContainer parameters = provider.getParameterContainer();
    if( Objects.isNull( parameters ) )
      return;

    final String href = parameters.getParameterValue( "href", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    if( StringUtils.isNotEmpty( href ) )
    {
      final IChartModel model = provider.getModel();
      final IBasicChartSettings settings = model.getSettings();

      final CHART_DATA_LOADER_STRATEGY strategy = settings.getDataLoaderStrategy();
      if( CHART_DATA_LOADER_STRATEGY.eSynchrone.equals( strategy ) )
      {
        final URL localContext = ZmlContext.resolveContext( model, href, context );
        final String plainHref = ZmlContext.resolvePlainHref( href );

        setObsProvider( new SynchronousObservationProvider( localContext, plainHref, provider.getRequestHandler( getLayer() ) ) );
      }
      else
      {
        final URL localContext = ZmlContext.resolveContext( model, href, context );
        final String plainHref = ZmlContext.resolvePlainHref( href );

        final PooledObsProvider obsProvider = new PooledObsProvider( new PoolableObjectType( "zml", plainHref, localContext, true ) ); //$NON-NLS-1$
        setObsProvider( obsProvider ); //$NON-NLS-1$
      }
    }
  }

  public IZmlLayer getLayer( )
  {
    return m_layer;
  }
}
