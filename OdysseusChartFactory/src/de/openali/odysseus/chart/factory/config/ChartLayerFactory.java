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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.openali.odysseus.chart.factory.OdysseusChartFactory;
import de.openali.odysseus.chart.factory.config.resolver.ChartTypeResolver;
import de.openali.odysseus.chart.factory.provider.IMapperProvider;
import de.openali.odysseus.chart.factory.util.AxisUtils;
import de.openali.odysseus.chart.factory.util.DerivedLayerTypeHelper;
import de.openali.odysseus.chart.factory.util.IReferenceResolver;
import de.openali.odysseus.chart.factory.util.LayerTypeHelper;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILayerProviderSource;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.impl.CoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.DerivedLayerDocument;
import de.openali.odysseus.chartconfig.x020.DerivedLayerType;
import de.openali.odysseus.chartconfig.x020.LayerDocument;
import de.openali.odysseus.chartconfig.x020.LayerReferenceDocument;
import de.openali.odysseus.chartconfig.x020.LayerRefernceType;
import de.openali.odysseus.chartconfig.x020.LayerType;
import de.openali.odysseus.chartconfig.x020.LayerType.MapperRefs;
import de.openali.odysseus.chartconfig.x020.LayersType;
import de.openali.odysseus.chartconfig.x020.MapperType;
import de.openali.odysseus.chartconfig.x020.ReferencableType;
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
    final LayersType layersType = chartType.getLayers();
    if( layersType == null )
      return;

    final IChartModel model = getModel();
    final ILayerManager layerManager = model.getLayerManager();
    final IChartLayer[] layers = build( layersType, chartType );

    layerManager.addLayer( layers );
  }

  private IChartLayer[] build( final LayersType layersType, final ReferencableType... baseTypes )
  {
    final Set<IChartLayer> layers = new LinkedHashSet<IChartLayer>();

    final Node node = layersType.getDomNode();
    final NodeList childNodes = node.getChildNodes();
    for( int index = 0; index < childNodes.getLength(); index++ )
    {
      final Node child = childNodes.item( index );
      if( StringUtils.isEmpty( child.getLocalName() ) )
        continue;

      try
      {
        final IChartLayer layer = parse( child, baseTypes );
        layers.add( layer );
      }
      catch( final Throwable t )
      {
        OdysseusChartFactory.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }

    return layers.toArray( new IChartLayer[] {} );
  }

  private IChartLayer parse( final Node node, final ReferencableType... baseTypes ) throws CoreException, ConfigurationException, XmlException
  {
    final String name = node.getLocalName();
    if( "Layer".equals( name ) ) //$NON-NLS-1$
    {
      final LayerDocument document = LayerDocument.Factory.parse( node );
      final LayerType layerType = document.getLayer();
      final ILayerProvider layerTypeProvider = LayerTypeHelper.getLayerTypeProvider( getLoader(), layerType );

      return buildLayer( layerType, layerTypeProvider, baseTypes );
    }
    else if( "LayerReference".equals( name ) ) //$NON-NLS-1$
    {
      final LayerReferenceDocument document = LayerReferenceDocument.Factory.parse( node );
      final LayerRefernceType reference = document.getLayerReference();

      return buildLayerReferenceType( reference, baseTypes );
    }
    else if( "DerivedLayer".equals( name ) ) //$NON-NLS-1$
    {
      final DerivedLayerDocument document = DerivedLayerDocument.Factory.parse( node );
      final DerivedLayerType derivedLayerType = document.getDerivedLayer();

      return buildDerivedLayerTypes( derivedLayerType, baseTypes );
    }
    else
      throw new NotImplementedException();
  }

  private IChartLayer buildLayerReferenceType( final LayerRefernceType reference, final ReferencableType... baseTypes ) throws CoreException, ConfigurationException
  {
    final ChartTypeResolver resovler = ChartTypeResolver.getInstance();

    final LayerType type = resovler.findLayerType( reference, getContext() );
    if( type == null )
      throw new IllegalStateException( String.format( "Chart LayerTypeReference not found: %s", reference.getUrl() ) );

    final ILayerProvider provider = LayerTypeHelper.getLayerTypeProvider( getLoader(), type );
    final IChartLayer layer = buildLayer( type, provider, (ReferencableType[]) ArrayUtils.add( baseTypes, type ) );

    return layer;
  }

  /**
   * @return IChartLayer from {@link LayersType}.getDerivedLayerArray()
   */
  private IChartLayer buildDerivedLayerTypes( final DerivedLayerType derivedLayerType, final ReferencableType... baseTypes ) throws CoreException, ConfigurationException
  {
    final ChartTypeResolver resovler = ChartTypeResolver.getInstance();

    final LayerRefernceType reference = derivedLayerType.getLayerReference();
    final LayerType baseLayerType = resovler.findLayerType( reference, getContext() );

    final LayerType derived = DerivedLayerTypeHelper.buildDerivedLayerType( derivedLayerType, baseLayerType );
    final ReferencableType parentBasePlayerType = LayerTypeHelper.getParentNode( derived );

    final Set<ReferencableType> types = new LinkedHashSet<ReferencableType>();
    types.add( derived );
    types.add( baseLayerType );
    types.add( parentBasePlayerType );
    Collections.addAll( types, baseTypes );

    final IChartLayer layer = buildLayer( derived, LayerTypeHelper.getLayerTypeProvider( getLoader(), derived ), types.toArray( new ReferencableType[] {} ) );

    return layer;
  }

  public IChartLayer buildLayer( final LayerType layerType, final ILayerProvider provider, final ReferencableType... baseTypes ) throws ConfigurationException
  {
    final ReferencingType domainAxisRef = getDomainAxisReference( layerType, baseTypes );
    final ReferencingType targetAxisRef = getTargetAxisReference( layerType, baseTypes );

    final IAxis domainAxis = buildMapper( domainAxisRef );
    final IAxis targetAxis = buildMapper( targetAxisRef );

    buildRoleReferences( layerType );

    final IParameterContainer parameters = createParameterContainer( layerType.getId(), provider.getId(), layerType.getProvider() );
    final Styles styles = layerType.getStyles();

    final IStyleSet styleSet = StyleFactory.createStyleSet( styles, baseTypes, getContext() );
    final Map<String, String> map = createMapperMap( layerType );

    provider.init( new ILayerProviderSource()
    {
      @Override
      public String getTargetAxis( )
      {
        return AxisUtils.getIdentifier( targetAxisRef );
      }

      @Override
      public IStyleSet getStyleSet( )
      {
        return styleSet;
      }

      @Override
      public IChartModel getModel( )
      {
        return ChartLayerFactory.this.getModel();
      }

      @Override
      public Map<String, String> getMapperMap( )
      {
        return map;
      }

      @Override
      public String getIdentifier( )
      {
        return layerType.getId();
      }

      @Override
      public String getDomainAxis( )
      {
        return AxisUtils.getIdentifier( domainAxisRef );
      }

      @Override
      public URL getContext( )
      {
        return ChartLayerFactory.this.getContext();
      }

      @Override
      public IParameterContainer getContainer( )
      {
        return parameters;
      }
    } );

    final IChartLayer layer = provider.getLayer( getContext() );
    setBasicParameters( layerType, layer );

    if( Objects.isNotNull( domainAxis, targetAxis ) )
      layer.setCoordinateMapper( new CoordinateMapper( domainAxis, targetAxis ) );

    layer.setData( CONFIGURATION_TYPE_KEY, layerType );
    layer.init();

    final LayersType layers = layerType.getLayers();
    if( layers != null )
    {
      final ReferencableType[] references = (ReferencableType[]) ArrayUtils.add( baseTypes, layerType );
      final ILayerManager layerManager = layer.getLayerManager();

      layerManager.addLayer( build( layers, references ) );
    }

    return layer;
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

  private ReferencingType getTargetAxisReference( final LayerType layerType, final ReferencableType... baseTypes )
  {
    final MapperRefs reference = findMapperReference( layerType, baseTypes );
    if( Objects.isNull( reference ) )
      return null;

    return reference.getTargetAxisRef();
  }

  private ReferencingType getDomainAxisReference( final LayerType layerType, final ReferencableType... baseTypes )
  {
    final MapperRefs reference = findMapperReference( layerType, baseTypes );
    if( reference == null )
      return null;

    return reference.getDomainAxisRef();
  }

  private MapperRefs findMapperReference( final LayerType layerType, final ReferencableType[] baseTypes )
  {
    final MapperRefs reference = layerType.getMapperRefs();
    if( reference != null )
      return reference;

    for( final ReferencableType baseType : baseTypes )
    {
      if( baseType instanceof LayerType )
      {
        final LayerType base = (LayerType) baseType;
        final MapperRefs layerTypeReference = base.getMapperRefs();
        if( layerTypeReference != null )
          return layerTypeReference;
      }
    }

    return null;
  }

  private void setBasicParameters( final LayerType layerType, final IChartLayer layer )
  {
    final String identifier = layerType.getId();
    if( StringUtils.isNotEmpty( identifier ) )
      layer.setId( identifier );

    final String title = layerType.getTitle();
    if( StringUtils.isNotEmpty( title ) )
      layer.setTitle( title );

    if( layerType.isSetDescription() )
      layer.setDescription( layerType.getDescription() );

    layer.setLegend( layerType.getLegend() );
    layer.setVisible( layerType.getVisible() );
  }

  private IAxis buildMapper( final ReferencingType reference )
  {
    if( reference == null )
      return null;

    final MapperType axisType = findMapperType( reference );
    m_mapperFactory.addMapper( axisType );

    return getModel().getMapperRegistry().getAxis( AxisUtils.getIdentifier( reference ) );
  }

  private void addMapper( final MapperType mapperType )
  {
    if( mapperType != null )
    {
      final String mpId = mapperType.getProvider().getEpid();
      if( mpId != null && mpId.length() > 0 )
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

  public static Map<String, String> createMapperMap( final LayerType layerType )
  {
    final Map<String, String> mapperMap = new HashMap<String, String>();
    final MapperRefs mapperReferences = layerType.getMapperRefs();
    if( mapperReferences == null )
      return mapperMap;

    final RoleReferencingType[] mapperRefArray = layerType.getMapperRefs().getMapperRefArray();
    for( final RoleReferencingType roleReferencingType : mapperRefArray )
    {
      mapperMap.put( roleReferencingType.getRole(), AxisUtils.getIdentifier( roleReferencingType ) );
    }

    return mapperMap;
  }
}
