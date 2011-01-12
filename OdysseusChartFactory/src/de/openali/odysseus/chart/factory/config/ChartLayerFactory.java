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
package de.openali.odysseus.chart.factory.config;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;

import de.openali.odysseus.chart.factory.OdysseusChartFactory;
import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.factory.config.resolver.ChartTypeResolver;
import de.openali.odysseus.chart.factory.layer.PlainLayer;
import de.openali.odysseus.chart.factory.provider.ILayerProvider;
import de.openali.odysseus.chart.factory.provider.IMapperProvider;
import de.openali.odysseus.chart.factory.util.AxisUtils;
import de.openali.odysseus.chart.factory.util.IReferenceResolver;
import de.openali.odysseus.chart.factory.util.LayerTypeHelper;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.impl.CoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.LayerRefernceType;
import de.openali.odysseus.chartconfig.x020.LayerType;
import de.openali.odysseus.chartconfig.x020.LayerType.MapperRefs;
import de.openali.odysseus.chartconfig.x020.LayersType;
import de.openali.odysseus.chartconfig.x020.MapperType;
import de.openali.odysseus.chartconfig.x020.ProviderType;
import de.openali.odysseus.chartconfig.x020.ReferencableType;
import de.openali.odysseus.chartconfig.x020.ReferencingType;
import de.openali.odysseus.chartconfig.x020.RoleReferencingType;

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
    final LayersType layersType = chartType.getLayers();
    if( layersType == null )
      return;

    buildLayerReferences( layersType, chartType );

    final IChartModel model = getModel();
    final ILayerManager layerManager = model.getLayerManager();

    final IChartLayer[] layers = buildLayers( layersType, chartType );
    for( final IChartLayer layer : layers )
    {
      layerManager.addLayer( layer );
    }
  }

  private void buildLayerReferences( final LayersType layers, final ReferencableType baseType )
  {
    final LayerRefernceType[] references = layers.getLayerReferenceArray();
    if( ArrayUtils.isEmpty( references ) )
      return;

    final ILayerManager layerManager = getModel().getLayerManager();

    for( final LayerRefernceType reference : references )
    {
      try
      {
        final ChartTypeResolver resovler = ChartTypeResolver.getInstance();
        final LayerType type = resovler.findLayerType( reference, getContext() );

        LayerTypeHelper.appendParameters( type, reference.getParameters() );

        final IChartLayer layer = buildLayer( type, baseType );
        layerManager.addLayer( layer );
      }
      catch( final Throwable t )
      {
        OdysseusChartFactory.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }
  }

  private IChartLayer[] buildLayers( final LayersType layers, final ReferencableType baseType )
  {
    final LayerType[] layerRefArray = layers.getLayerArray();
    if( ArrayUtils.isEmpty( layerRefArray ) )
      return new IChartLayer[] {};

    final Set<IChartLayer> stack = new LinkedHashSet<IChartLayer>();

    for( final LayerType layerType : layerRefArray )
    {
      try
      {
        if( layerType != null )
          stack.add( buildLayer( layerType, baseType ) );
        else
          Logger.logWarning( Logger.TOPIC_LOG_CONFIG, "a reference to a layer type could not be resolved " );
      }
      catch( final ConfigurationException e )
      {
        e.printStackTrace();

        Logger.logWarning( Logger.TOPIC_LOG_CONFIG, "a reference to a layer type could not be resolved " );
      }
    }

    return stack.toArray( new IChartLayer[] {} );
  }

  public IChartLayer buildLayer( final LayerType layerType, final ReferencableType baseType ) throws ConfigurationException
  {
    final ProviderType providerType = layerType.getProvider();
    if( providerType == null )
    {
      return buildPlainLayer( layerType, baseType );
    }
    else
    {

      final ReferencingType domainAxisRef = getDomainAxisReference( layerType, baseType );
      final ReferencingType targetAxisRef = getTargetAxisReference( layerType, baseType );

      final IAxis domainAxis = buildMapper( domainAxisRef, baseType );
      final IAxis targetAxis = buildMapper( targetAxisRef, baseType );

      final CoordinateMapper mapper = new CoordinateMapper( domainAxis, targetAxis );
      buildRoleReferences( layerType );

      final IParameterContainer parameters = createParameterContainer( layerType.getId(), layerType.getProvider() );

      final IStyleSet styleSet = StyleFactory.createStyleSet( layerType.getStyles(), baseType, getContext() );

      final ILayerProvider provider = getLoader().getExtension( ILayerProvider.class, providerType.getEpid() );
      provider.init( getModel(), layerType.getId(), parameters, getContext(), AxisUtils.getIdentifier( domainAxisRef ), AxisUtils.getIdentifier( targetAxisRef ), createMapperMap( layerType ), styleSet );

      final IChartLayer layer = provider.getLayer( getContext() );
      setBasicParameters( layerType, layer );
      layer.setCoordinateMapper( mapper );

      layer.setData( CONFIGURATION_TYPE_KEY, layerType );
      layer.init();

      return layer;
    }

// // create styles
// // TODO global styleset
// final IStyleSet styleSet = StyleFactory.createStyleSet( layerType.getStyles(), null, getContext() );
// // create map if mapper roles to ids
// final Map<String, String> mapperMap = createMapperMap( layerType );
// // create parameter container
// final IParameterContainer parameters = createParameterContainer( layerType.getId(), layerType.getProvider() );
//
// final ReferencingType domainAxisId = layerType.getMapperRefs().getDomainAxisRef();
// final ReferencingType targetAxisId = layerType.getMapperRefs().getTargetAxisRef();
//
// provider.init( getModel(), layerType.getId(), parameters, getContext(), AxisUtils.getIdentifier( domainAxisId ),
// AxisUtils.getIdentifier( targetAxisId ), mapperMap, styleSet );
// final IChartLayer icl = provider.getLayer( getContext() );
// if( icl != null )
// {
//

// getModel().getLayerManager().addLayer( icl );
// Logger.logInfo( Logger.TOPIC_LOG_CONFIG, "Adding Layer: " + icl.getTitle() );
// }
// }
// catch( final ConfigurationException e )
// {
// e.printStackTrace();
// }
// catch( final Exception e )
// {
// e.printStackTrace();
  }

  private void buildRoleReferences( final LayerType layerType )
  {
    final MapperRefs references = layerType.getMapperRefs();
    if( references == null )
      return;

    final RoleReferencingType[] roleReferences = references.getMapperRefArray();
    if( ArrayUtils.isEmpty( roleReferences ) )
      return;

    for( final RoleReferencingType reference : roleReferences )
    {
      final MapperType mapperType = findMapperType( reference );
      if( mapperType != null )
        // nur dann hinzuf¸gen, wenn nicht schon vorhanden
        if( getModel().getMapperRegistry().getMapper( mapperType.getId() ) == null )
          addMapper( mapperType );
    }

  }

  private ReferencingType getTargetAxisReference( final LayerType layerType, final ReferencableType baseType )
  {
    final MapperRefs reference = layerType.getMapperRefs();
    if( reference == null )
    {
      if( baseType instanceof LayerType )
      {
        final LayerType base = (LayerType) baseType;
        return base.getMapperRefs().getTargetAxisRef();
      }
      else
        throw new NotImplementedException();
    }

    return reference.getTargetAxisRef();
  }

  private ReferencingType getDomainAxisReference( final LayerType layerType, final ReferencableType baseType )
  {
    final MapperRefs reference = layerType.getMapperRefs();
    if( reference == null )
    {
      if( baseType instanceof LayerType )
      {
        final LayerType base = (LayerType) baseType;
        return base.getMapperRefs().getDomainAxisRef();
      }
      else
        throw new NotImplementedException();
    }

    return reference.getDomainAxisRef();
  }

  private void setBasicParameters( final LayerType layerType, final IChartLayer layer )
  {
    final String identifier = layerType.getId();
    if( StringUtils.isNotEmpty( identifier ) )
      layer.setId( identifier );

    final String title = layerType.getTitle();
    if( StringUtils.isNotEmpty( title ) )
      layer.setTitle( title );

    final String description = layerType.getDescription();
    if( StringUtils.isNotEmpty( description ) )
      layer.setDescription( description );

    layer.setLegend( layerType.isSetLegend() );
    layer.setVisible( layerType.isSetVisible() );
  }

  private IChartLayer buildPlainLayer( final LayerType layerType, final ReferencableType baseType )
  {
    final PlainLayer plainLayer = new PlainLayer();
    setBasicParameters( layerType, plainLayer );

    final MapperRefs mapperReferences = layerType.getMapperRefs();
    if( mapperReferences != null )
    {
      final IAxis domainAxis = buildMapper( mapperReferences.getDomainAxisRef(), baseType );
      final IAxis targetAxis = buildMapper( mapperReferences.getTargetAxisRef(), baseType );

      final CoordinateMapper mapper = new CoordinateMapper( domainAxis, targetAxis );
      plainLayer.setCoordinateMapper( mapper );

      final RoleReferencingType[] mapperRefArray = mapperReferences.getMapperRefArray();
      if( ArrayUtils.isNotEmpty( mapperRefArray ) )
        throw new NotImplementedException();

      buildRoleReferences( layerType );
    }

    final ILayerManager layerManager = plainLayer.getLayerManager();

    final LayersType layers = layerType.getLayers();
    final IChartLayer[] children = buildLayers( layers, layerType );
    for( final IChartLayer child : children )
    {
      layerManager.addLayer( child );
    }

    // handle style set
    plainLayer.init();

    return plainLayer;
  }

  private IAxis buildMapper( final ReferencingType reference, final ReferencableType baseType )
  {
    if( reference == null )
      return null;

    final MapperType axisType = findMapperType( reference );
    m_mapperFactory.addMapper( axisType, baseType );

    return getModel().getMapperRegistry().getAxis( AxisUtils.getIdentifier( reference ) );
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
    final MapperRefs mapperRefs = lt.getMapperRefs();
    if( mapperRefs == null )
      return mapperMap;

    final RoleReferencingType[] mapperRefArray = lt.getMapperRefs().getMapperRefArray();
    for( final RoleReferencingType rrt : mapperRefArray )
    {
      mapperMap.put( rrt.getRole(), AxisUtils.getIdentifier( rrt ) );
    }

    return mapperMap;
  }
}
