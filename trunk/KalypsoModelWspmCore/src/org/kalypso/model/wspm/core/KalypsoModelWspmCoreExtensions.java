package org.kalypso.model.wspm.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.kalypso.model.wspm.core.gml.IProfileFeatureProvider;
import org.kalypso.model.wspm.core.profil.IProfileObjectProvider;
import org.kalypso.model.wspm.core.profil.IProfilePointMarkerProvider;
import org.kalypso.model.wspm.core.profil.IProfilePointPropertyProvider;
import org.kalypso.model.wspm.core.profil.ProfileType;
import org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter;
import org.kalypso.model.wspm.core.profil.reparator.IProfileMarkerResolution;
import org.kalypso.model.wspm.core.profil.serializer.IProfileSource;

/** Helper class to read extension points of this plugin. */
public final class KalypsoModelWspmCoreExtensions
{
  private KalypsoModelWspmCoreExtensions( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" ); //$NON-NLS-1$
  }

  private static IProfileFeatureProvider[] PROFILE_FEATURE_PROVIDER = null;

  private static IProfilePointFilter[] PROFILE_POINT_FILTERS = null;

  private static Map<String, List<IProfilePointMarkerProvider>> THE_MARKER_PROVIDER_MAP = null;

  private static Map<String, ProfileType> THE_PROFILE_TYPE_MAP = null;

  private static Map<String, IProfileObjectProvider> PROFILE_OBJECT_PROVIDER = null;

  public static IProfileMarkerResolution[] createReparatorRules( )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( "org.kalypso.model.wspm.core.reparatorrule" ); //$NON-NLS-1$

    final Collection<IProfileMarkerResolution> reparators = new ArrayList<>( elements.length );
    final Collection<IStatus> stati = new ArrayList<>( elements.length );

    for( final IConfigurationElement element : elements )
    {
      try
      {
        final IProfileMarkerResolution rule = (IProfileMarkerResolution)element.createExecutableExtension( "class" ); //$NON-NLS-1$
        reparators.add( rule );
      }
      catch( final CoreException e )
      {
        e.printStackTrace();
        final IStatus status = e.getStatus();
        KalypsoModelWspmCorePlugin.getDefault().getLog().log( status );

        stati.add( status );
      }
    }

    return reparators.toArray( new IProfileMarkerResolution[reparators.size()] );
  }

  public static IProfileMarkerResolution getReparatorRule( final String parameterStream )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( "org.kalypso.model.wspm.core.reparatorrule" ); //$NON-NLS-1$

    for( final IConfigurationElement element : elements )
    {
      try
      {
        final IProfileMarkerResolution rule = (IProfileMarkerResolution)element.createExecutableExtension( "class" ); //$NON-NLS-1$
        if( parameterStream.startsWith( rule.getClass().getName() ) )
        {
          rule.setData( parameterStream );
          return rule;
        }
      }
      catch( final CoreException e )
      {
        e.printStackTrace();
      }
    }

    return null;
  }

  /**
   * @param fileExtension
   *          File extension without '.'
   */
  public static IProfileSource createProfilSource( final String fileExtension ) throws CoreException
  {
    final Map<String, IConfigurationElement> sinkMap = getSinksOrSources( "source" ); //$NON-NLS-1$

    final IConfigurationElement element = sinkMap.get( fileExtension );
    if( element == null )
      return null;

    return (IProfileSource)element.createExecutableExtension( "class" ); //$NON-NLS-1$
  }

  private static Map<String, IConfigurationElement> getSinksOrSources( final String name )
  {
    final IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
    final IConfigurationElement[] elements = extensionRegistry.getConfigurationElementsFor( "org.kalypso.model.wspm.core", "profilserializer" ); //$NON-NLS-1$ //$NON-NLS-2$
    final Map<String, IConfigurationElement> map = new HashMap<>( elements.length );
    for( final IConfigurationElement element : elements )
    {
      final String eltName = element.getName();
      if( eltName.equals( name ) )
      {
        final String ext = element.getAttribute( "extension" ); //$NON-NLS-1$
        map.put( ext, element );
      }
    }

    return map;
  }

  public static synchronized IProfileFeatureProvider[] getProfileFeatureProvider( )
  {
    if( PROFILE_FEATURE_PROVIDER != null )
      return PROFILE_FEATURE_PROVIDER;

    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( "org.kalypso.model.wspm.core.profileFeatureProvider" ); //$NON-NLS-1$

    final Collection<IProfileFeatureProvider> provider = new ArrayList<>( elements.length );
    for( final IConfigurationElement element : elements )
    {
      try
      {
        provider.add( (IProfileFeatureProvider)element.createExecutableExtension( "class" ) ); //$NON-NLS-1$
      }
      catch( final CoreException e )
      {
        KalypsoModelWspmCorePlugin.getDefault().getLog().log( e.getStatus() );
      }
    }

    PROFILE_FEATURE_PROVIDER = provider.toArray( new IProfileFeatureProvider[provider.size()] );

    return PROFILE_FEATURE_PROVIDER;
  }

  public static synchronized IProfilePointFilter[] getProfilePointFilters( final String usageHint )
  {
    if( PROFILE_POINT_FILTERS == null )
    {
      PROFILE_POINT_FILTERS = readProfileFilters();
    }

    return restrictFilterByUsage( PROFILE_POINT_FILTERS, usageHint );
  }

  private static IProfilePointFilter[] restrictFilterByUsage( final IProfilePointFilter[] filters, final String usageHint )
  {
    final Collection<IProfilePointFilter> restrictedFilters = new ArrayList<>( filters.length );

    for( final IProfilePointFilter filter : filters )
    {
      final String filterUsage = filter.getUsageHint();

      /* Blank usage: filter should be used everywhere */
      if( StringUtils.isBlank( filterUsage ) )
      {
        restrictedFilters.add( filter );
      }
      else
      {
        /*
         * Else, filters hint must contain the given hint to be added. I.e. if usageHint is empty, all filters with non
         * -empty hint are removed.
         */
        final String[] usages = filterUsage.split( "," ); //$NON-NLS-1$
        if( ArrayUtils.contains( usages, usageHint ) )
        {
          restrictedFilters.add( filter );
        }
      }
    }

    return restrictedFilters.toArray( new IProfilePointFilter[restrictedFilters.size()] );
  }

  private static IProfilePointFilter[] readProfileFilters( )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( "org.kalypso.model.wspm.core.profilePointFilter" ); //$NON-NLS-1$

    final Collection<IProfilePointFilter> filter = new ArrayList<>( elements.length );
    for( final IConfigurationElement element : elements )
    {
      try
      {
        filter.add( (IProfilePointFilter)element.createExecutableExtension( "class" ) ); //$NON-NLS-1$
      }
      catch( final CoreException e )
      {
        KalypsoModelWspmCorePlugin.getDefault().getLog().log( e.getStatus() );
      }
    }

    return filter.toArray( new IProfilePointFilter[filter.size()] );
  }

  public static IProfilePointMarkerProvider getMarkerProviders( final String profilType )
  {
    final Map<String, List<IProfilePointMarkerProvider>> map = getMarkerProviders();
    final List<IProfilePointMarkerProvider> list = map.get( profilType );
    if( list == null )
      return null;

    if( list.size() > 1 )
      throw new IllegalStateException();

    return list.get( 0 );
  }

// public static IProfilPointMarkerProvider[] getAllMarkerProviders( )
// {
// final Map<String, List<IProfilPointMarkerProvider>> map = getMarkerProviders();
// final List<IProfilPointMarkerProvider> list = new ArrayList<IProfilPointMarkerProvider>();
// for( final List<IProfilPointMarkerProvider> ppmp : map.values() )
// {
// list.addAll( ppmp );
// }
//
// return list.toArray( new IProfilPointMarkerProvider[list.size()] );
// }

  private static synchronized Map<String, List<IProfilePointMarkerProvider>> getMarkerProviders( )
  {
    if( THE_MARKER_PROVIDER_MAP != null )
      return THE_MARKER_PROVIDER_MAP;

    THE_MARKER_PROVIDER_MAP = new HashMap<>();

    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] markerProvider = registry.getConfigurationElementsFor( "org.kalypso.model.wspm.core.profilPointMarkerProvider" ); //$NON-NLS-1$
    for( final IConfigurationElement configurationElement : markerProvider )
    {
      try
      {
        final String profilType = configurationElement.getAttribute( "profiletype" ); //$NON-NLS-1$
        final Object protoProvider = configurationElement.createExecutableExtension( "provider" ); //$NON-NLS-1$
        final IProfilePointMarkerProvider provider = (IProfilePointMarkerProvider)protoProvider;

        if( !THE_MARKER_PROVIDER_MAP.containsKey( profilType ) )
        {
          THE_MARKER_PROVIDER_MAP.put( profilType, new ArrayList<IProfilePointMarkerProvider>() );
        }

        THE_MARKER_PROVIDER_MAP.get( profilType ).add( provider );
      }
      catch( final CoreException e )
      {
        KalypsoModelWspmCorePlugin.getDefault().getLog().log( e.getStatus() );
      }
    }

    return THE_MARKER_PROVIDER_MAP;
  }

// public static IProfileObjectProvider[] getObjectProviders( final String profilType )
// {
// final Map<String, List<IProfileObjectProvider>> map = getObjectProviders();
// final List<IProfileObjectProvider> list = map.get( profilType );
// if( list == null )
// return new IProfileObjectProvider[0];
//
// return list.toArray( new IProfileObjectProvider[list.size()] );
// }
//
// private static synchronized Map<String, List<IProfileObjectProvider>> getObjectProviders( )
// {
// if( THE_OBJECT_PROVIDER_MAP != null )
// return THE_OBJECT_PROVIDER_MAP;
//
// THE_OBJECT_PROVIDER_MAP = new HashMap<String, List<IProfileObjectProvider>>();
//
// final IExtensionRegistry registry = Platform.getExtensionRegistry();
//    final IConfigurationElement[] objectProvider = registry.getConfigurationElementsFor( "org.kalypso.model.wspm.core.profileObjectProvider" ); //$NON-NLS-1$
// for( final IConfigurationElement configurationElement : objectProvider )
// {
// try
// {
//        final String profilType = configurationElement.getAttribute( "profiletype" ); //$NON-NLS-1$
//        final Object protoProvider = configurationElement.createExecutableExtension( "provider" ); //$NON-NLS-1$
// final IProfileObjectProvider provider = (IProfileObjectProvider) protoProvider;
//
// if( !THE_OBJECT_PROVIDER_MAP.containsKey( profilType ) )
// THE_OBJECT_PROVIDER_MAP.put( profilType, new ArrayList<IProfileObjectProvider>() );
//
// THE_OBJECT_PROVIDER_MAP.get( profilType ).add( provider );
// }
// catch( final CoreException e )
// {
// KalypsoModelWspmCorePlugin.getDefault().getLog().log( e.getStatus() );
// }
// }
//
// return THE_OBJECT_PROVIDER_MAP;
// }

  public static IProfilePointPropertyProvider getPointPropertyProviders( final String profilType )
  {
    final Map<String, ProfileType> map = getProfileTypes();
    final ProfileType profileType = map.get( profilType );
    if( profileType == null )
      return null;

    return profileType.m_pointProvider;
  }

  private static synchronized Map<String, ProfileType> getProfileTypes( )
  {
    if( THE_PROFILE_TYPE_MAP != null )
      return THE_PROFILE_TYPE_MAP;

    // TODO: hashing this map is not robust against registry changes; listen to extension registry and clear the map if
    // necessary

    THE_PROFILE_TYPE_MAP = new HashMap<>();

    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] propertyProvider = registry.getConfigurationElementsFor( "org.kalypso.model.wspm.core.profiletype" ); //$NON-NLS-1$
    for( final IConfigurationElement configurationElement : propertyProvider )
    {
      try
      {
        final String id = configurationElement.getAttribute( "id" ); //$NON-NLS-1$
        final String label = configurationElement.getAttribute( "name" ); //$NON-NLS-1$
        final String desc = configurationElement.getAttribute( "description" ); //$NON-NLS-1$
        final IProfilePointPropertyProvider provider = (IProfilePointPropertyProvider)configurationElement.createExecutableExtension( "class" ); //$NON-NLS-1$

        final ProfileType profileType = new ProfileType( id, label, desc, provider );

        THE_PROFILE_TYPE_MAP.put( id, profileType );
      }
      catch( final CoreException e )
      {
        KalypsoModelWspmCorePlugin.getDefault().getLog().log( e.getStatus() );
      }
    }

    return THE_PROFILE_TYPE_MAP;
  }

  public static IProfileObjectProvider getProfileObjectProvider( final String providerId )
  {
    final Map<String, IProfileObjectProvider> map = getProfileObjectProviders();
    return map.get( providerId );
  }

  private static synchronized Map<String, IProfileObjectProvider> getProfileObjectProviders( )
  {
    if( PROFILE_OBJECT_PROVIDER != null )
      return PROFILE_OBJECT_PROVIDER;

    // TODO: hashing this map is not robust against registry changes; listen to extension registry and clear the map if
    // necessary
    PROFILE_OBJECT_PROVIDER = new HashMap<>();

    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] propertyProvider = registry.getConfigurationElementsFor( "org.kalypso.model.wspm.core.profileObjectProvider" ); //$NON-NLS-1$
    for( final IConfigurationElement configurationElement : propertyProvider )
    {
      try
      {
        final String id = configurationElement.getAttribute( "id" ); //$NON-NLS-1$
        final IProfileObjectProvider provider = (IProfileObjectProvider)configurationElement.createExecutableExtension( "provider" ); //$NON-NLS-1$

        PROFILE_OBJECT_PROVIDER.put( id, provider );
      }
      catch( final CoreException e )
      {
        KalypsoModelWspmCorePlugin.getDefault().getLog().log( e.getStatus() );
      }
    }

    return PROFILE_OBJECT_PROVIDER;
  }

}