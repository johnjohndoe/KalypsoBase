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
package org.kalypso.zml.ui.chart.update;

import java.util.LinkedHashSet;
import java.util.Set;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.data.ZmlObsProviderDataHandler;
import org.kalypso.zml.core.diagram.layer.IZmlLayer;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.core.provider.observation.AsynchronousObservationProvider;
import org.kalypso.zml.ui.core.zml.MultipleTsLink;
import org.kalypso.zml.ui.core.zml.TSLinkWithName;

import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.impl.CoordinateMapper;

/**
 * @author Dirk Kuch
 */
public class ZmlDiagramUpdater implements Runnable
{
  private final Set<IObsProvider> m_providers = new LinkedHashSet<IObsProvider>();

  private final ILayerManager m_manager;

  private final MultipleTsLink[] m_links;

  private final String[] m_ignoreTypes;

  public ZmlDiagramUpdater( final ILayerManager manager, final MultipleTsLink[] links, final String[] ignoreTypes )
  {
    m_manager = manager;
    m_links = links;
    m_ignoreTypes = ignoreTypes;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run( )
  {
    m_manager.accept( new RemoveClonedLayerVisitor() );

    for( final MultipleTsLink multiple : m_links )
    {
      if( multiple.isIgnoreType( m_ignoreTypes ) )
        continue;

      final ParameterTypeLayerVisitor visitor = new ParameterTypeLayerVisitor( multiple.getIdentifier() );
      m_manager.accept( visitor );

      final IZmlLayer[] layers = visitor.getLayers();

      final TSLinkWithName[] links = multiple.getLinks();
      for( int index = 0; index < links.length; index++ )
      {
        final TSLinkWithName link = links[index];
        final AsynchronousObservationProvider provider = new AsynchronousObservationProvider( link, multiple.getType() );
        m_providers.add( provider );

        update( layers, provider, index, link.getName() );
      }
    }
  }

  private void update( final IZmlLayer[] layers, final AsynchronousObservationProvider provider, final int index, final String labelDescriptor )
  {
    for( final IZmlLayer baseLayer : layers )
    {
      try
      {
        if( !supportsMultiSelect( baseLayer ) && index > 0 )
          continue;

        final IZmlLayer layer = buildLayer( baseLayer, index );

        final IZmlLayerDataHandler handler = layer.getDataHandler();
        if( handler instanceof ZmlObsProviderDataHandler )
          ((ZmlObsProviderDataHandler) handler).setObsProvider( provider );

        layer.setLabelDescriptor( labelDescriptor );
      }
      catch( final ConfigurationException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }
  }

  private boolean supportsMultiSelect( final IZmlLayer layer )
  {
    final ILayerProvider provider = layer.getProvider();
    if( provider == null )
      return false;

    final IParameterContainer container = provider.getParameterContainer();
    if( container == null )
      return false;

    final String property = container.getParameterValue( "supportsMultiSelect", "false" );

    return Boolean.valueOf( property );
  }

  private IZmlLayer buildLayer( final IZmlLayer baseLayer, final int index ) throws ConfigurationException
  {
    if( index == 0 )
      return baseLayer;

    final ILayerProvider provider = baseLayer.getProvider();
    final IZmlLayer clone = (IZmlLayer) provider.getLayer( provider.getContext() );
    clone.setId( String.format( IClonedLayer.CLONED_LAYER_POSTFIX_FORMAT, baseLayer.getId(), index ) );
    clone.setDataHandler( new ZmlObsProviderDataHandler( clone, baseLayer.getDataHandler().getTargetAxisId() ) );

    final ICoordinateMapper baseMapper = baseLayer.getCoordinateMapper();
    clone.setCoordinateMapper( new CoordinateMapper( baseMapper.getDomainAxis(), baseMapper.getTargetAxis() ) );

    final ILayerContainer parent = baseLayer.getParent();
    parent.getLayerManager().addLayer( clone );

    return clone;
  }

  public IObsProvider[] getProviders( )
  {
    return m_providers.toArray( new IObsProvider[] {} );
  }
}
