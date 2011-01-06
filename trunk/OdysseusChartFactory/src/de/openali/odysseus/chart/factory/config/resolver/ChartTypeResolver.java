package de.openali.odysseus.chart.factory.config.resolver;

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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jregex.Pattern;
import jregex.RETokenizer;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.java.util.StringUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.catalog.ICatalog;

import com.google.common.collect.MapMaker;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.ChartType.Layers;
import de.openali.odysseus.chartconfig.x020.LayerRefernceType;
import de.openali.odysseus.chartconfig.x020.LayerType;

/**
 * @author Dirk Kuch
 */
public final class ChartTypeResolver
{
  private final Map<String, List<ChartType>> m_chartTypeCache;

  private final Map<String, LayerType> m_layerCache;

  private static ChartTypeResolver INSTANCE;

  private ChartTypeResolver( )
  {
    final MapMaker marker = new MapMaker().expiration( 30, TimeUnit.MINUTES );
    m_chartTypeCache = marker.makeMap();
    m_layerCache = marker.makeMap();
  }

  public static ChartTypeResolver getInstance( )
  {
    if( INSTANCE == null )
      INSTANCE = new ChartTypeResolver();

    return INSTANCE;
  }

  public LayerType findLayerType( final URL context, final LayerRefernceType reference ) throws CoreException
  {
    try
    {
      final String url = reference.getUrl();
      if( url != null )
      {
        final LayerType cached = getCachedLayerType( url );
        if( cached != null )
          return cached;

        final String plainUrl = getUrl( url );
        final String identifier = getAnchor( url );

        LayerType type;
        if( plainUrl.startsWith( "urn:" ) )
          type = findUrnLayerType( context, plainUrl, identifier );
        else
          type = findUrlLayerType( context, plainUrl, identifier );

        // FIXME: what to do if rule null?
        if( type != null )
          m_layerCache.put( url, type );

        return type;
      }
    }
    catch( final Throwable t )
    {
      throw new CoreException( StatusUtilities.createExceptionalErrorStatus( "Resolving style failed", t ) );
    }
    throw new IllegalStateException();
  }

  private LayerType getCachedLayerType( final String url )
  {
    // FIXME: we should consider a timeout based on the modification timestamp of the underlying resource here
    // Else, the referenced resource will never be loaded again, even if it has changed meanwhile
    return m_layerCache.get( url );
  }

  private LayerType findUrlLayerType( final URL context, final String uri, final String identifier ) throws XmlException, IOException
  {
    final URL absoluteUri = new URL( context, uri );

    List<ChartType> chartTypes = m_chartTypeCache.get( uri );
    if( chartTypes == null )
    {
      final ChartConfigurationLoader loader = new ChartConfigurationLoader( absoluteUri );
      final ChartType[] charts = loader.getCharts();

      chartTypes = new ArrayList<ChartType>();
      Collections.addAll( chartTypes, charts );

      m_chartTypeCache.put( uri, chartTypes );
    }

    for( final ChartType chart : chartTypes )
    {
      final Layers layers = chart.getLayers();
      final LayerType layer = findLayer( layers, identifier );
      if( layer != null )
        return layer;
    }

    return null;
  }

  private LayerType findLayer( final Layers layers, final String identifier )
  {
    // TODO resolve references
    final LayerType[] layerTypes = layers.getLayerArray();
    for( final LayerType layer : layerTypes )
    {
      if( layer.getId().equals( identifier ) )
        return layer;
    }

    return null;
  }

  private LayerType findUrnLayerType( final URL context, final String urn, final String identifier ) throws XmlException, IOException
  {
    final ICatalog baseCatalog = KalypsoCorePlugin.getDefault().getCatalogManager().getBaseCatalog();
    final String uri = baseCatalog.resolve( urn, urn );

    return findUrlLayerType( context, uri, identifier );
  }

  private String getUrl( final String url )
  {
    final RETokenizer tokenizer = new RETokenizer( new Pattern( "#.*" ), url ); //$NON-NLS-1$

    return StringUtilities.chop( tokenizer.nextToken() );
  }

  private String getAnchor( final String url )
  {
    final RETokenizer tokenizer = new RETokenizer( new Pattern( ".*#" ), url ); //$NON-NLS-1$

    return StringUtilities.chop( tokenizer.nextToken() );
  }

}
