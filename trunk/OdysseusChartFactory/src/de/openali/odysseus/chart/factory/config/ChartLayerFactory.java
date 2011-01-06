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
package de.openali.odysseus.chart.factory.config;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;

import de.openali.odysseus.chart.factory.OdysseusChartFactory;
import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.factory.config.resolver.ChartTypeResolver;
import de.openali.odysseus.chart.factory.provider.ILayerProvider;
import de.openali.odysseus.chart.factory.provider.IMapperProvider;
import de.openali.odysseus.chart.factory.util.AxisUtils;
import de.openali.odysseus.chart.factory.util.DummyLayer;
import de.openali.odysseus.chart.factory.util.DummyLayerProvider;
import de.openali.odysseus.chart.factory.util.IReferenceResolver;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.impl.CoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.ChartType.Layers;
import de.openali.odysseus.chartconfig.x020.LayerRefernceType;
import de.openali.odysseus.chartconfig.x020.LayerType;
import de.openali.odysseus.chartconfig.x020.LayerType.MapperRefs;
import de.openali.odysseus.chartconfig.x020.MapperType;
import de.openali.odysseus.chartconfig.x020.ProviderType;
import de.openali.odysseus.chartconfig.x020.ReferencingType;
import de.openali.odysseus.chartconfig.x020.RoleReferencingType;
import de.openali.odysseus.chartconfig.x020.StylesDocument.Styles;

/**
 * @author Dirk Kuch
 */
public class ChartLayerFactory extends AbstractChartFactory
{

  private final ChartMapperFactory m_mapperFactory;

  public ChartLayerFactory( final IChartModel model, final IReferenceResolver resolver, final IExtensionLoader loader, final URL context, final ChartMapperFactory mapperFactory )
  {
    super( model, resolver, loader, context );
    m_mapperFactory = mapperFactory;
  }

  public void build( final ChartType chartType )
  {

    final Layers layers = chartType.getLayers();
    if( layers == null )
      return;

    final Styles globalStyles = chartType.getStyles();

    buildLayers( layers, globalStyles );
    buildLayerReferences( layers, globalStyles );
  }

  private void buildLayerReferences( final Layers layers, final Styles globalStyles )
  {
    final LayerRefernceType[] references = layers.getLayerReferenceArray();
    if( ArrayUtils.isEmpty( references ) )
      return;

    for( final LayerRefernceType reference : references )
    {
      try
      {
        final ChartTypeResolver resovler = ChartTypeResolver.getInstance();
        final LayerType type = resovler.findLayerType( reference, getContext() );

        addLayer( type, globalStyles );
      }
      catch( final CoreException e )
      {
        OdysseusChartFactory.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }
  }

  private void buildLayers( final Layers layers, final Styles globalStyles )
  {
    final LayerType[] layerRefArray = layers.getLayerArray();
    if( ArrayUtils.isEmpty( layerRefArray ) )
      return;

    for( final LayerType layerType : layerRefArray )
    {
      if( layerType != null )
        addLayer( layerType, globalStyles );
      else
        Logger.logWarning( Logger.TOPIC_LOG_CONFIG, "a reference to a layer type could not be resolved " );
    }
  }

  public void addLayer( final LayerType layerType, final Styles globalStyles )
  {
    // Achsen hinzufügen
    final MapperRefs mapper = layerType.getMapperRefs();

    final MapperType domainAxisType = findMapperType( mapper.getDomainAxisRef() );
    m_mapperFactory.addMapper( domainAxisType, globalStyles );

    final MapperType targetAxisType = findMapperType( mapper.getTargetAxisRef() );
    m_mapperFactory.addMapper( targetAxisType, globalStyles );

    // Restliche Mapper hinzufügen
    final RoleReferencingType[] mapperRefArray = mapper.getMapperRefArray();
    for( final RoleReferencingType mapperRef : mapperRefArray )
    {
      final MapperType mapperType = findMapperType( mapperRef );
      if( mapperType != null )
        // nur dann hinzufügen, wenn nicht schon vorhanden
        if( getModel().getMapperRegistry().getMapper( mapperType.getId() ) == null )
          addMapper( mapperType );
    }

    // Layer erzeugen
    final ProviderType provider2 = layerType.getProvider();
    final String providerId = provider2.getEpid();
    ILayerProvider provider = null;
    try
    {
      provider = getLoader().getExtension( ILayerProvider.class, providerId );
      if( provider != null )
        Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "LayerProvider loaded:" + provider.getClass().toString() );
      else
      {
        provider = new DummyLayerProvider();
        Logger.logError( Logger.TOPIC_LOG_GENERAL, "No LayerProvider for " + providerId );
      }

      // create styles
      final IStyleSet styleSet = StyleFactory.createStyleSet( layerType.getStyles(), globalStyles, getContext() );
      // create map if mapper roles to ids
      final Map<String, String> mapperMap = createMapperMap( layerType );
      // create parameter container
      final IParameterContainer parameters = createParameterContainer( layerType.getId(), layerType.getProvider() );

      final ReferencingType domainAxisId = layerType.getMapperRefs().getDomainAxisRef();
      final ReferencingType targetAxisId = layerType.getMapperRefs().getTargetAxisRef();

      provider.init( getModel(), layerType.getId(), parameters, getContext(), AxisUtils.getIdentifier( domainAxisId ), AxisUtils.getIdentifier( targetAxisId ), mapperMap, styleSet );
      final IChartLayer icl = provider.getLayer( getContext() );
      if( icl != null )
      {

        final IAxis domainAxis = getModel().getMapperRegistry().getAxis( AxisUtils.getIdentifier( layerType.getMapperRefs().getDomainAxisRef() ) );
        final IAxis targetAxis = getModel().getMapperRegistry().getAxis( AxisUtils.getIdentifier( layerType.getMapperRefs().getTargetAxisRef() ) );
        final ICoordinateMapper cm = new CoordinateMapper( domainAxis, targetAxis );
        icl.setCoordinateMapper( cm );
        icl.setId( layerType.getId() );
        icl.setTitle( layerType.getTitle() );
        icl.setDescription( layerType.getDescription() );
        icl.setVisible( layerType.getVisible() );
        icl.setLegend( layerType.getLegend() );
        // saving provider so it can be reused for saving to chart file
        icl.setData( LAYER_PROVIDER_KEY, provider );
        // save configuration type so it can be used for saving to chart file
        icl.setData( CONFIGURATION_TYPE_KEY, layerType );
        // initialize layer
        icl.init();

        getModel().getLayerManager().addLayer( icl );
        Logger.logInfo( Logger.TOPIC_LOG_CONFIG, "Adding Layer: " + icl.getTitle() );
      }
    }
    catch( final ConfigurationException e )
    {
      e.printStackTrace();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final IAxis domainAxis = getModel().getMapperRegistry().getAxis( AxisUtils.getIdentifier( layerType.getMapperRefs().getDomainAxisRef() ) );
      final IAxis targetAxis = getModel().getMapperRegistry().getAxis( AxisUtils.getIdentifier( layerType.getMapperRefs().getTargetAxisRef() ) );
      final ICoordinateMapper cm = new CoordinateMapper( domainAxis, targetAxis );
      final IChartLayer icl = new DummyLayer();
      icl.setTitle( layerType.getTitle() );
      icl.setCoordinateMapper( cm );
      // saving the provider into the layer so it can be reused to save changed charts
      icl.setData( LAYER_PROVIDER_KEY, provider );
      // save configuration type so it can be used for saving to chart file
      icl.setData( CONFIGURATION_TYPE_KEY, layerType );
      getModel().getLayerManager().addLayer( icl );
    }
  }

  private void addMapper( final MapperType mapperType )
  {
    if( mapperType != null )
    {
      final String mpId = mapperType.getProvider().getEpid();
      if( (mpId != null) && (mpId.length() > 0) )
        try
        {
          final IMapperRegistry mr = getModel().getMapperRegistry();
          final IMapperProvider mp = getLoader().getExtension( IMapperProvider.class, mpId );
          final String mid = mapperType.getId();
          final IParameterContainer pc = createParameterContainer( mid, mapperType.getProvider() );
          mp.init( getModel(), mid, pc, getContext() );
          final IMapper mapper = mp.getMapper();
          // save provider so it can be used for saving to chartfile
          mapper.setData( ChartFactory.AXIS_PROVIDER_KEY, mp );
          // save configuration type so it can be used for saving to chartfile
          mapper.setData( CONFIGURATION_TYPE_KEY, mapperType );
          mr.addMapper( mapper );
        }
        catch( final ConfigurationException e )
        {
          e.printStackTrace();
        }
      else
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "AxisProvider " + mpId + " not known" );
    }
    else
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "AxisFactory: given axis is NULL." );
  }

  public static Map<String, String> createMapperMap( final LayerType lt )
  {
    final Map<String, String> mapperMap = new HashMap<String, String>();
    final RoleReferencingType[] mapperRefArray = lt.getMapperRefs().getMapperRefArray();
    for( final RoleReferencingType rrt : mapperRefArray )
      mapperMap.put( rrt.getRole(), AxisUtils.getIdentifier( rrt ) );

    return mapperMap;
  }
}
