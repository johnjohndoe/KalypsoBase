/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/

package org.kalypso.core;

import java.io.File;
import java.util.TimeZone;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.commons.eclipse.core.runtime.PluginImageProvider;
import org.kalypso.core.catalog.CatalogManager;
import org.kalypso.core.catalog.CatalogSLD;
import org.kalypso.core.internal.DictionaryCache;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.deegree.binding.gml.Dictionary;
import org.kalypso.loader.DefaultLoaderFactory;
import org.kalypso.loader.ILoaderFactory;
import org.kalypso.ogc.gml.selection.FeatureSelectionManager2;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.osgi.framework.BundleContext;

/**
 * @author Gernot Belger
 */
public class KalypsoCorePlugin extends AbstractUIPlugin
{
  private static KalypsoCorePlugin INSTANCE;

  private IFeatureSelectionManager m_selectionManager = null;

  private CatalogManager m_catalogManager = null;

  private CatalogSLD m_sldCatalog = null;

  private ResourcePool m_pool;

  private ILoaderFactory m_loaderFactory;

  private DictionaryCache m_dictionaryCache;

  private PluginImageProvider m_imgProvider = null;

  public static String getID( )
  {
    return getDefault().getBundle().getSymbolicName();
  }

  public static KalypsoCorePlugin getDefault( )
  {
    return INSTANCE;
  }

  public KalypsoCorePlugin( )
  {
    INSTANCE = this;
  }

  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );

    m_imgProvider = new PluginImageProvider( this );
    m_imgProvider.resetTmpFiles();
  }

  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    m_catalogManager = null;
    m_sldCatalog = null;
    m_selectionManager = null;

    savePluginPreferences();

    m_imgProvider.resetTmpFiles();
    m_imgProvider = null;

    super.stop( context );
  }

  public synchronized CatalogManager getCatalogManager( )
  {
    if( m_catalogManager == null )
    {
      final File stateLocation = getStateLocation().toFile();
      final File managerDir = new File( stateLocation, "catalogManager" ); //$NON-NLS-1$
      managerDir.mkdirs();
      m_catalogManager = new CatalogManager( managerDir );
      KalypsoCoreExtensions.loadXMLCatalogs( m_catalogManager );
    }

    return m_catalogManager;
  }

  public synchronized CatalogSLD getSLDCatalog( )
  {
    if( m_sldCatalog == null )
    {
      final File stateLocation = getStateLocation().toFile();
      final File styleCatalogDir = new File( stateLocation, "style-catalog" ); //$NON-NLS-1$
      styleCatalogDir.mkdirs();
      m_sldCatalog = new CatalogSLD( getCatalogManager(), styleCatalogDir );
    }

    return m_sldCatalog;
  }

  public synchronized IFeatureSelectionManager getSelectionManager( )
  {
    if( m_selectionManager == null )
      m_selectionManager = new FeatureSelectionManager2();

    return m_selectionManager;
  }

  /**
   * @see KalypsoCorePreferences#getTimeZone()
   */
  public TimeZone getTimeZone( )
  {
    return KalypsoCorePreferences.getTimeZone();
  }

  /**
   * This function returns the pool.
   * 
   * @return The pool.
   */
  public synchronized ResourcePool getPool( )
  {
    if( m_pool == null )
      m_pool = new ResourcePool( getLoaderFactory() );

    return m_pool;
  }

  /**
   * This function returns the loader factory.
   * 
   * @return The loader factory.
   */
  private synchronized ILoaderFactory getLoaderFactory( )
  {
    if( m_loaderFactory == null )
      m_loaderFactory = new DefaultLoaderFactory();

    return m_loaderFactory;
  }

  public Dictionary getDictionary( final String urn )
  {
    final DictionaryCache dictionaryCache = getDictionaryCache();
    final GMLWorkspace gmlWorkspace = dictionaryCache.get( urn );
    if( gmlWorkspace == null )
      return null;

    return (Dictionary)gmlWorkspace.getRootFeature();
  }

  private synchronized DictionaryCache getDictionaryCache( )
  {
    if( m_dictionaryCache == null )
      m_dictionaryCache = new DictionaryCache();

    return m_dictionaryCache;
  }

  public static PluginImageProvider getImageProvider( )
  {
    return getDefault().m_imgProvider;
  }
}