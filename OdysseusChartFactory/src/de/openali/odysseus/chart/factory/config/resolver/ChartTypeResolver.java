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

package de.openali.odysseus.chart.factory.config.resolver;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jregex.Pattern;
import jregex.RETokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.KalypsoCorePlugin;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.openali.odysseus.chart.factory.OdysseusChartFactory;
import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.StyleHelper;
import de.openali.odysseus.chart.factory.util.DerivedLayerTypeHelper;
import de.openali.odysseus.chart.factory.util.IReferenceResolver;
import de.openali.odysseus.chart.factory.util.LayerTypeHelper;
import de.openali.odysseus.chartconfig.x020.AbstractStyleType;
import de.openali.odysseus.chartconfig.x020.AxisRendererType;
import de.openali.odysseus.chartconfig.x020.AxisType;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.ChartType.Mappers;
import de.openali.odysseus.chartconfig.x020.ChartType.Renderers;
import de.openali.odysseus.chartconfig.x020.DerivedLayerType;
import de.openali.odysseus.chartconfig.x020.LayerRefernceType;
import de.openali.odysseus.chartconfig.x020.LayerType;
import de.openali.odysseus.chartconfig.x020.LayerType.MapperRefs;
import de.openali.odysseus.chartconfig.x020.LayersType;
import de.openali.odysseus.chartconfig.x020.ScreenAxisType;
import de.openali.odysseus.chartconfig.x020.StylesDocument.Styles;

/**
 * @author Dirk Kuch
 */
public final class ChartTypeResolver implements IReferenceResolver
{

  private final Cache<String, ChartConfigurationLoader> m_loaderCache;

  private static ChartTypeResolver INSTANCE;

  private ChartTypeResolver( )
  {
    final CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().expireAfterAccess( 10, TimeUnit.MINUTES );
    m_loaderCache = builder.build();
  }

  public static ChartTypeResolver getInstance( )
  {
    if( INSTANCE == null )
      INSTANCE = new ChartTypeResolver();

    return INSTANCE;
  }

  private ChartConfigurationLoader getLoader( final URL context, final String uri ) throws XmlException, IOException
  {
    ChartConfigurationLoader loader = m_loaderCache.asMap().get( uri );
    if( loader != null )
      return loader;

    final URL absoluteUri = new URL( context, uri );
    loader = new ChartConfigurationLoader( absoluteUri );

    m_loaderCache.put( uri, loader );

    return loader;
  }

  public AbstractStyleType findStyleType( final String reference, final URL context ) throws CoreException
  {
    try
    {
      if( reference == null || reference.length() == 0 )
        return null;

      final String plainUrl = getUrl( reference, context );
      final String identifier = getAnchor( reference );

      AbstractStyleType type;
      if( plainUrl.startsWith( "urn:" ) ) //$NON-NLS-1$
        type = findUrnStyleType( context, plainUrl, identifier );
      else
        type = findUrlStyleType( context, plainUrl, identifier );

      return type;
    }
    catch( final Throwable t )
    {
      final String msg = String.format( "Resolving style type \"%s\" failed  ", reference ); //$NON-NLS-1$
      throw new CoreException( new Status( IStatus.ERROR, OdysseusChartFactory.PLUGIN_ID, msg, t ) );
    }
  }

  public AxisType findMapperType( final String reference, final URL context ) throws CoreException
  {
    try
    {
      final String plainUrl = getUrl( reference, context );
      final String identifier = getAnchor( reference );

      AxisType type;
      if( plainUrl.startsWith( "urn:" ) ) //$NON-NLS-1$
        type = findUrnMapperType( context, plainUrl, identifier );
      else
        type = findUrlMapperType( context, plainUrl, identifier );

      return type;
    }
    catch( final Throwable t )
    {
      throw new CoreException( new Status( IStatus.ERROR, OdysseusChartFactory.PLUGIN_ID, "Resolving mapper type failed", t ) ); //$NON-NLS-1$
    }
  }

  public LayerType findLayerType( final LayerRefernceType reference, final URL context ) throws CoreException
  {
    try
    {
      final String url = reference.getUrl();
      if( url != null )
      {
        final String plainUrl = getUrl( url, context );
        final String identifier = getAnchor( url );

        LayerType type;
        if( plainUrl.startsWith( "urn:" ) ) //$NON-NLS-1$
          type = findUrnLayerType( context, plainUrl, identifier );
        else
          type = findUrlLayerType( context, plainUrl, identifier );

        return type;
      }
    }
    catch( final Throwable t )
    {
      throw new CoreException( new Status( IStatus.ERROR, OdysseusChartFactory.PLUGIN_ID, "Resolving layer failed", t ) ); //$NON-NLS-1$
    }

    throw new IllegalStateException();
  }

  private LayerType findUrnLayerType( final URL context, final String urn, final String identifier ) throws XmlException, IOException
  {
    final String uri = KalypsoCorePlugin.getDefault().getCatalogManager().resolve( urn, urn );

    return findUrlLayerType( context, uri, identifier );
  }

  private LayerType findUrlLayerType( final URL context, final String uri, final String identifier ) throws XmlException, IOException
  {
    final ChartConfigurationLoader loader = getLoader( context, uri );
    final ChartType[] charts = loader.getCharts();

    final List<ChartType> chartTypes = new ArrayList<>();
    Collections.addAll( chartTypes, charts );

    for( final ChartType chart : chartTypes )
    {
      try
      {
        final LayersType layers = chart.getLayers();

        final LayerType layer = findLayer( layers, identifier, context );
        if( layer != null )
          return layer;
      }
      catch( final CoreException e )
      {
        OdysseusChartFactory.getDefault().getLog().log( new Status( IStatus.ERROR, OdysseusChartFactory.PLUGIN_ID, e.getLocalizedMessage(), e ) );
      }
    }

    return null;
  }

  private LayerType findLayer( final LayersType layers, final String identifier, final URL context ) throws CoreException
  {
    if( layers == null )
      return null;

    final LayerType[] layerTypes = layers.getLayerArray();
    for( final LayerType layer : layerTypes )
    {
      if( layer.getId().equals( identifier ) )
        return layer;

      final LayersType children = layer.getLayers();
      final LayerType child = findLayer( children, identifier, context );
      if( child != null )
      {
        /** mapper reference can be defined cascading. this could be a problem by handling / initialising derived layers */
        if( Objects.isNull( child.getMapperRefs() ) )
        {
          final MapperRefs reference = LayerTypeHelper.findMapperReference( child );
          child.setMapperRefs( reference );
        }

        return child;
      }

    }

    final LayerRefernceType[] layerReferences = layers.getLayerReferenceArray();
    for( final LayerRefernceType reference : layerReferences )
    {
      final String id = getAnchor( reference.getUrl() );
      if( identifier.equals( id ) )
        return findLayerType( reference, context );
    }

    final DerivedLayerType[] derivedLayerTypes = layers.getDerivedLayerArray();
    for( final DerivedLayerType derivedLayerType : derivedLayerTypes )
    {
      if( identifier.equals( derivedLayerType.getId() ) )
      {
        final LayerType baseLayerType = findLayerType( derivedLayerType.getLayerReference(), context );

        return DerivedLayerTypeHelper.buildDerivedLayerType( derivedLayerType, baseLayerType );
      }
    }

    return null;
  }

  private String getUrl( final String url, final URL context )
  {
    if( StringUtils.isBlank( url ) )
      return context.toString();

    if( url.contains( "#" ) ) //$NON-NLS-1$
    {
      final Pattern pattern = new Pattern( "#.*" ); //$NON-NLS-1$
      if( pattern.matches( url ) )
        return context.toString();

      final RETokenizer tokenizer = new RETokenizer( pattern, url );

      return StringUtils.chomp( tokenizer.nextToken() );
    }

    return context.toString();
  }

  private String getAnchor( final String url )
  {
    if( Strings.isNullOrEmpty( url ) )
      return null;

    final RETokenizer tokenizer = new RETokenizer( new Pattern( ".*#" ), url ); //$NON-NLS-1$

    return StringUtils.chomp( tokenizer.nextToken() );
  }

  private AxisType findUrnMapperType( final URL context, final String urn, final String identifier ) throws XmlException, IOException
  {
    final String uri = KalypsoCorePlugin.getDefault().getCatalogManager().resolve( urn, urn );

    return findUrlMapperType( context, uri, identifier );
  }

  private AxisType findUrlMapperType( final URL context, final String uri, final String identifier ) throws XmlException, IOException
  {
    final ChartConfigurationLoader loader = getLoader( context, uri );
    final ChartType[] charts = loader.getCharts();

    final List<ChartType> chartTypes = new ArrayList<>();
    Collections.addAll( chartTypes, charts );

    for( final ChartType chart : chartTypes )
    {
      final Mappers mappers = chart.getMappers();
      final AxisType[] axes = mappers.getAxisArray();
      for( final AxisType axis : axes )
      {
        if( axis.getId().equals( identifier ) )
          return axis;
      }

      final ScreenAxisType[] screenAxes = mappers.getScreenAxisArray();
      for( final ScreenAxisType screenAxis : screenAxes )
      {
        if( screenAxis.getId().equals( identifier ) )
          return screenAxis;
      }
    }

    return null;
  }

  private AbstractStyleType findUrnStyleType( final URL context, final String urn, final String identifier ) throws XmlException, IOException
  {
    final String uri = KalypsoCorePlugin.getDefault().getCatalogManager().resolve( urn, urn );

    return findUrlStyleType( context, uri, identifier );
  }

  private AbstractStyleType findUrlStyleType( final URL context, final String uri, final String identifier ) throws XmlException, IOException
  {
    final ChartConfigurationLoader loader = getLoader( context, uri );
    final ChartType[] charts = loader.getCharts();

    final List<ChartType> chartTypes = new ArrayList<>();
    Collections.addAll( chartTypes, charts );

    for( final ChartType chart : chartTypes )
    {
      final Styles styles = chart.getStyles();
      final AbstractStyleType style = StyleHelper.findStyle( styles, identifier );
      if( style != null )
        return style;

      final LayersType layers = chart.getLayers();
      final LayerType[] layerArray = layers.getLayerArray();
      for( final LayerType layer : layerArray )
      {
        final Styles layerStyles = layer.getStyles();
        final AbstractStyleType layerStyle = StyleHelper.findStyle( layerStyles, identifier );
        if( layerStyle != null )
          return layerStyle;
      }

      final Renderers renderers = chart.getRenderers();
      final AxisRendererType[] rendererArray = renderers.getAxisRendererArray();
      for( final AxisRendererType renderer : rendererArray )
      {
        final Styles rendererStyles = renderer.getStyles();
        final AbstractStyleType rendererStyle = StyleHelper.findStyle( rendererStyles, identifier );
        if( rendererStyle != null )
          return rendererStyle;
      }
    }

    return null;
  }

  @Override
  public Object resolveReference( final String id )
  {
    final ChartConfigurationLoader[] loaders = m_loaderCache.asMap().values().toArray( new ChartConfigurationLoader[] {} );
    for( final ChartConfigurationLoader loader : loaders )
    {
      final XmlObject reference = loader.resolveReference( id );
      if( reference != null )
        return reference;
    }

    return null;
  }

  public void clear( )
  {
    m_loaderCache.cleanUp();
  }
}
