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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.util.PropertiesUtilities;
import org.kalypso.core.catalog.CatalogManager;
import org.kalypso.core.catalog.ICatalogContribution;
import org.kalypso.core.catalog.urn.IURNGenerator;
import org.kalypso.core.gml.provider.IGmlSourceProvider;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoThemeInfo;
import org.kalypso.ogc.sensor.adapter.INativeObservationAdapter;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.IPropertiesFeatureVisitor;

/**
 * Helper class to read extension-points of this plugin.
 * 
 * @author belger
 */
public final class KalypsoCoreExtensions
{
  private static final String VISITOR_EXTENSION_POINT = "org.kalypso.core.featureVisitor"; //$NON-NLS-1$

  private static final String CATALOG_CONTRIBUTIONS_EXTENSION_POINT = "org.kalypso.core.catalogContribution"; //$NON-NLS-1$

  /** id -> config-element */
  private static Map<String, IConfigurationElement> THE_VISITOR_MAP = null;

  /* Theme-Info Extension-Point */
  private static final String THEME_INFO_EXTENSION_POINT = "org.kalypso.core.themeInfo"; //$NON-NLS-1$

  private static Map<String, IConfigurationElement> THE_THEME_INFO_MAP = null;

  /* GmlSourceProvider Extension-Point */
  private static final String GML_SOURCE_PROVIDER_EXTENSION_POINT = "org.kalypso.core.gmlSourceProvider"; //$NON-NLS-1$

  private KalypsoCoreExtensions( )
  {
  }

  public static synchronized FeatureVisitor createFeatureVisitor( final String id, final Properties properties ) throws CoreException
  {
    if( THE_VISITOR_MAP == null )
    {
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IExtensionPoint extensionPoint = registry.getExtensionPoint( VISITOR_EXTENSION_POINT );
      final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
      THE_VISITOR_MAP = new HashMap<>( configurationElements.length );
      for( final IConfigurationElement element : configurationElements )
      {
        final String configid = element.getAttribute( "id" ); //$NON-NLS-1$
        THE_VISITOR_MAP.put( configid, element );
      }
    }

    if( !THE_VISITOR_MAP.containsKey( id ) )
      return null;

    final IConfigurationElement element = THE_VISITOR_MAP.get( id );
    final FeatureVisitor visitor = (FeatureVisitor)element.createExecutableExtension( "class" ); //$NON-NLS-1$
    if( visitor instanceof IPropertiesFeatureVisitor )
      ((IPropertiesFeatureVisitor)visitor).init( properties );

    return visitor;
  }

  public static void loadXMLCatalogs( final CatalogManager catalogManager )
  {
    if( !Platform.isRunning() )
    {
      System.out.println( Messages.getString( "org.kalypso.core.KalypsoCoreExtensions.7" ) ); //$NON-NLS-1$
      return;
    }

    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IExtensionPoint extensionPoint = registry.getExtensionPoint( CATALOG_CONTRIBUTIONS_EXTENSION_POINT );

    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
    for( final IConfigurationElement element : configurationElements )
    {
      try
      {
        final String name = element.getName();
        if( "catalogContribution".equals( name ) ) //$NON-NLS-1$
        {
          final Object createExecutableExtension = element.createExecutableExtension( "class" ); //$NON-NLS-1$
          final ICatalogContribution catalogContribution = (ICatalogContribution)createExecutableExtension;
          catalogContribution.contributeTo( catalogManager );
        }
        else if( "urnGenerator".equals( name ) ) //$NON-NLS-1$
        {
          final Object createExecutableExtension = element.createExecutableExtension( "class" ); //$NON-NLS-1$
          final IURNGenerator urnGenerator = (IURNGenerator)createExecutableExtension;
          catalogManager.register( urnGenerator );
        }
      }
      catch( final Throwable t )
      {
        // In order to prevent bad code from other plugins (see Eclipse-PDE-Rules)
        // catch exception here and just log it
        final IStatus status = StatusUtilities.statusFromThrowable( t );
        KalypsoCorePlugin.getDefault().getLog().log( status );
      }
    }
  }

  /**
   * @param themeInfoId
   *          Id of the requested extension. Can contain properties. Example: <code>org.kalypso.core.someId?prop1=value1&props2=values</code>
   */
  public static IKalypsoThemeInfo createThemeInfo( final String themeInfoId, final IKalypsoTheme theme )
  {
    if( THE_THEME_INFO_MAP == null )
    {
      /* Lookup the existing ids only once */

      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IExtensionPoint extensionPoint = registry.getExtensionPoint( THEME_INFO_EXTENSION_POINT );
      final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
      THE_THEME_INFO_MAP = new HashMap<>( configurationElements.length );
      for( final IConfigurationElement element : configurationElements )
      {
        final String configid = element.getAttribute( "id" ); //$NON-NLS-1$
        THE_THEME_INFO_MAP.put( configid, element );
      }
    }

    final String id;
    final Properties props = new Properties();
    if( themeInfoId.contains( "?" ) ) //$NON-NLS-1$
    {
      final int queryPartIndex = themeInfoId.indexOf( '?' );
      id = themeInfoId.substring( 0, queryPartIndex );

      // replace in order to handle empty query
      final String query = themeInfoId.substring( queryPartIndex ).replaceAll( "\\?", "" ); //$NON-NLS-1$ //$NON-NLS-2$
      PropertiesUtilities.collectProperties( query, "&", "=", props ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else
      id = themeInfoId;

    try
    {
      final IConfigurationElement element = THE_THEME_INFO_MAP.get( id );
      if( element == null )
      {
        final String message = String.format( "Unknown themeInfoId: %s", id ); //$NON-NLS-1$
        final IStatus status = new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), message );
        KalypsoCorePlugin.getDefault().getLog().log( status );
        return null;
      }

      final IKalypsoThemeInfo info = (IKalypsoThemeInfo)element.createExecutableExtension( "class" ); //$NON-NLS-1$
      info.init( theme, props );
      return info;
    }
    catch( final Throwable e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoCorePlugin.getDefault().getLog().log( status );
    }

    return null;
  }

  /**
   * @param category
   *          If non-<code>null</code>, returned providers are filtered by this category. Else all registered providers
   *          are returned.
   */
  public static IGmlSourceProvider[] createGmlSourceProvider( final String category )
  {
    final List<IGmlSourceProvider> result = new ArrayList<>();

    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IExtensionPoint extensionPoint = registry.getExtensionPoint( GML_SOURCE_PROVIDER_EXTENSION_POINT );
    final IConfigurationElement[] providerElements = extensionPoint.getConfigurationElements();
    for( final IConfigurationElement providerElement : providerElements )
    {
      final String providerId = providerElement.getAttribute( "id" ); //$NON-NLS-1$

      final IConfigurationElement[] categoryElements = providerElement.getChildren( "category" ); //$NON-NLS-1$
      for( final IConfigurationElement categoryElement : categoryElements )
      {
        final String categoryId = categoryElement.getAttribute( "id" ); //$NON-NLS-1$
        if( category == null || category.equals( categoryId ) )
        {
          try
          {
            result.add( (IGmlSourceProvider)providerElement.createExecutableExtension( "class" ) ); //$NON-NLS-1$
          }
          catch( final Throwable e )
          {
            final IStatus status = StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.core.KalypsoCoreExtensions.25" ) + providerId ); //$NON-NLS-1$
            KalypsoCorePlugin.getDefault().getLog().log( status );
          }

          /* Add each provider only once */
          break;
        }
      }

    }

    return result.toArray( new IGmlSourceProvider[result.size()] );
  }

  public static ViewerFilter createViewerFilter( final String id ) throws CoreException
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( "org.kalypso.core.featureviewFilter" ); //$NON-NLS-1$
    for( final IConfigurationElement element : elements )
    {
      final String elementId = element.getAttribute( "id" ); //$NON-NLS-1$
      if( id.equals( elementId ) )
        return (ViewerFilter)element.createExecutableExtension( "class" ); //$NON-NLS-1$
    }

    final String msg = String.format( "No registered filter with id '%s'", id ); //$NON-NLS-1$
    final IStatus missingStatus = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), msg );
    throw new CoreException( missingStatus );
  }

  public static ViewerComparator createComparator( final String id ) throws CoreException
  {
    /* Get the sorter of the id. */
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( "org.kalypso.core.featureviewComparator" ); //$NON-NLS-1$
    for( final IConfigurationElement element : elements )
    {
      final String elementId = element.getAttribute( "id" ); //$NON-NLS-1$
      if( id.equals( elementId ) )
        return (ViewerComparator)element.createExecutableExtension( "class" ); //$NON-NLS-1$
    }

    final String msg = String.format( "No registered comparator with id '%s'", id ); //$NON-NLS-1$
    final IStatus missingStatus = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), msg );
    throw new CoreException( missingStatus );
  }

  private static Map<String, IConfigurationElement> readImporters( )
  {
    final Map<String, IConfigurationElement> importers = new TreeMap<>();

    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IExtensionPoint extensionPoint = registry.getExtensionPoint( "org.kalypso.core.observationImporter" ); //$NON-NLS-1$
    if( extensionPoint == null )
      return Collections.unmodifiableMap( importers );

    final IExtension[] extensions = extensionPoint.getExtensions();
    for( final IExtension extension : extensions )
    {
      final IConfigurationElement[] elements = extension.getConfigurationElements();

      for( final IConfigurationElement element : elements )
      {
        final String identifier = element.getAttribute( "id" ); //$NON-NLS-1$
        importers.put( identifier, element );
      }
    }

    return Collections.unmodifiableMap( importers );
  }

  public static synchronized INativeObservationAdapter[] getObservationImporters( )
  {
    final Map<String, IConfigurationElement> importers = readImporters();

    final List<INativeObservationAdapter> adapters = new ArrayList<>();

    final Set<Entry<String, IConfigurationElement>> entries = importers.entrySet();
    for( final Entry<String, IConfigurationElement> entry : entries )
    {
      try
      {
        final INativeObservationAdapter adapter = (INativeObservationAdapter)entry.getValue().createExecutableExtension( "class" ); //$NON-NLS-1$
        adapters.add( adapter );
      }
      catch( final Exception ex )
      {
        ex.printStackTrace();
      }
    }

    return adapters.toArray( new INativeObservationAdapter[] {} );
  }

  public static synchronized INativeObservationAdapter getObservationImporter( final String id ) throws CoreException
  {
    final Map<String, IConfigurationElement> importers = readImporters();

    final IConfigurationElement element = importers.get( id );
    if( Objects.isNull( element ) )
      return null;

    return (INativeObservationAdapter)element.createExecutableExtension( "class" ); //$NON-NLS-1$
  }

  /**
   * @deprecated Use {@link #getObservationImporters()} instead; BUT: solve problems with axis types first.
   */
  @Deprecated
  public static INativeObservationAdapter[] createNativeAdaptersOldStyle( )
  {
    final List<INativeObservationAdapter> adapters = new ArrayList<>();
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IExtensionPoint extensionPoint = registry.getExtensionPoint( "org.kalypso.core.nativeObsAdapter" ); //$NON-NLS-1$

    final IExtension[] extensions = extensionPoint.getExtensions();
    for( final IExtension extension : extensions )
    {
      final IConfigurationElement[] elements = extension.getConfigurationElements();

      for( final IConfigurationElement element : elements )
      {
        try
        {
          final INativeObservationAdapter adapter = (INativeObservationAdapter)element.createExecutableExtension( "class" ); //$NON-NLS-1$
          adapters.add( adapter );
        }
        catch( final CoreException e )
        {
          e.printStackTrace();
        }
      }
    }

    return adapters.toArray( new INativeObservationAdapter[adapters.size()] );
  }
}
