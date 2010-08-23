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
package org.kalypso.ui;

import java.awt.PopupMenu;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.types.ITypeHandlerFactory;
import org.kalypso.ogc.gml.featureview.IFeatureModifier;
import org.kalypso.ogc.gml.featureview.IFeatureModifierExtension;
import org.kalypso.ogc.gml.featureview.control.IExtensionsFeatureControlFactory;
import org.kalypso.ogc.gml.featureview.control.IExtensionsFeatureControlFactory2;
import org.kalypso.ogc.gml.gui.IGuiTypeHandler;
import org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandlerProvider;

/**
 * Within this class all extension-point from the KalypsoUI plug-in are handled.
 * 
 * @author Gernot Belger
 */
public class KalypsoUIExtensions
{
  private KalypsoUIExtensions( )
  {
    // do not instantiate
  }

  /* extension-point typeHandlers */
  private static final String TYPE_HANDLERS_EXTENSION_POINT = "org.kalypso.ui.typeHandlers"; //$NON-NLS-1$

  private static final String TYPE_HANDLER_ELEMENT_NAME = "typeHandler"; //$NON-NLS-1$

  private static final String FEATURE_MODIFIER_ELEMENT_NAME = "featureModifier"; //$NON-NLS-1$

  private static final String TYPE_HANDLER_FACTORY_CLASS = "factory"; //$NON-NLS-1$

  private static Map<String, IConfigurationElement> THE_FEATURE_MODIFIERS_MAP = null;

  /* extension-point 'featureViewExtensionControl' */
  private static final String FEATUREVIEW_CONTROL_EXTENSION_POINT = "org.kalypso.ui.featureViewExtensionControl"; //$NON-NLS-1$

  private static final String OBSERVATION_TABLE_HEADER_POPUP_MENU_EXTENSION_POINT = "org.kalypso.ui.observationTableHeaderPopupMenu"; //$NON-NLS-1$

  private static Map<String, IConfigurationElement> THE_FEATUREVIEW_CONTROL_MAP = null;

  private static Map<String, IConfigurationElement> OBSERVATION_TABLE_HEADER_POPUP_MENUS = null;

  public static IExtensionsFeatureControlFactory2 getFeatureviewControlFactory( final String id ) throws CoreException
  {
    final Map<String, IConfigurationElement> map = getFeatureviewControlMap();
    if( map == null )
      return null;

    final IConfigurationElement factoryElement = map.get( id );
    if( factoryElement == null )
      throw new CoreException( StatusUtilities.createErrorStatus( "No feature-control-factory found with id: " + id ) ); //$NON-NLS-1$

    final Object factory = factoryElement.createExecutableExtension( "class" ); //$NON-NLS-1$;
    if( factory instanceof IExtensionsFeatureControlFactory)
      return new ExtensionFeatureControl2Wrapper( (IExtensionsFeatureControlFactory)factory );

    return (IExtensionsFeatureControlFactory2) factory;
  }

  private static synchronized Map<String, IConfigurationElement> getFeatureviewControlMap( )
  {
    if( THE_FEATUREVIEW_CONTROL_MAP == null )
    {
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IExtensionPoint extensionPoint = registry.getExtensionPoint( FEATUREVIEW_CONTROL_EXTENSION_POINT );
      final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
      THE_FEATUREVIEW_CONTROL_MAP = new HashMap<String, IConfigurationElement>( configurationElements.length );

      for( final IConfigurationElement element : configurationElements )
      {
        final String id = element.getAttribute( "id" ); //$NON-NLS-1$
        THE_FEATUREVIEW_CONTROL_MAP.put( id, element );
      }
    }

    return THE_FEATUREVIEW_CONTROL_MAP;
  }

  public static PopupMenu getObservationTableHeaderPopupMenu( ) throws CoreException
  {
    final Map<String, IConfigurationElement> map = getObservationTableHeaderPopupMenus();
    if( map == null )
      return null;

    final Collection<IConfigurationElement> collection = map.values();
    if( collection.size() == 0 )
      return null;
    else if( collection.size() > 1 )
      throw new NotImplementedException(); // at the moment we only support one popup menu

    final IConfigurationElement element = collection.iterator().next();

    return (PopupMenu) element.createExecutableExtension( "menu" ); //$NON-NLS-1$
  }

  private static synchronized Map<String, IConfigurationElement> getObservationTableHeaderPopupMenus( )
  {
    if( OBSERVATION_TABLE_HEADER_POPUP_MENUS == null )
    {
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IExtensionPoint extensionPoint = registry.getExtensionPoint( OBSERVATION_TABLE_HEADER_POPUP_MENU_EXTENSION_POINT );
      final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
      OBSERVATION_TABLE_HEADER_POPUP_MENUS = new HashMap<String, IConfigurationElement>( configurationElements.length );

      for( final IConfigurationElement element : configurationElements )
      {
        final String id = element.getAttribute( "id" ); //$NON-NLS-1$
        OBSERVATION_TABLE_HEADER_POPUP_MENUS.put( id, element );
      }
    }

    return OBSERVATION_TABLE_HEADER_POPUP_MENUS;
  }

  public static IComponentUiHandlerProvider createComponentUiHandlerProvider( final String componentUiHandlerProviderId )
  {
    final String idToFind = componentUiHandlerProviderId == null ? "org.kalypso.ogc.gml.om.table.handlers.DefaultComponentUiHandlerProvider" : componentUiHandlerProviderId.trim(); //$NON-NLS-1$

    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IExtensionPoint extensionPoint = registry.getExtensionPoint( "org.kalypso.ui.componentUiHandlerProvider" ); //$NON-NLS-1$
    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();

    for( final IConfigurationElement element : configurationElements )
    {
      final String id = element.getAttribute( "id" ); //$NON-NLS-1$
      if( id.equals( idToFind ) )
      {
        try
        {
          return (IComponentUiHandlerProvider) element.createExecutableExtension( "class" ); //$NON-NLS-1$
        }
        catch( final CoreException e )
        {
          e.printStackTrace();

          KalypsoGisPlugin.getDefault().getLog().log( e.getStatus() );

          return null;
        }
      }
    }

    final IStatus status = StatusUtilities.createErrorStatus( "No componenUiHandlerProvider found with id: " + componentUiHandlerProviderId ); //$NON-NLS-1$
    KalypsoGisPlugin.getDefault().getLog().log( status );

    return null;
  }

  @SuppressWarnings("unchecked")
  public static ITypeHandlerFactory<IGuiTypeHandler>[] createGuiTypeHandlerFactories( )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IExtensionPoint extensionPoint = registry.getExtensionPoint( TYPE_HANDLERS_EXTENSION_POINT );
    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();

    final List<ITypeHandlerFactory<IGuiTypeHandler>> factories = new ArrayList<ITypeHandlerFactory<IGuiTypeHandler>>( configurationElements.length );

    for( final IConfigurationElement element : configurationElements )
    {
      try
      {
        if( TYPE_HANDLER_ELEMENT_NAME.equals( element.getName() ) )
        {
          final ITypeHandlerFactory<IGuiTypeHandler> factory = (ITypeHandlerFactory<IGuiTypeHandler>) element.createExecutableExtension( TYPE_HANDLER_FACTORY_CLASS );
          factories.add( factory );
        }
      }
      catch( final CoreException e )
      {
        KalypsoGisPlugin.getDefault().getLog().log( e.getStatus() );
      }
    }
    return factories.toArray( new ITypeHandlerFactory[factories.size()] );
  }

  public static IFeatureModifier createFeatureModifier( final IPropertyType ftp, final String id ) throws CoreException
  {
    final IConfigurationElement ce = getFeatureModifierElement( id );
    if( ce == null )
      return null;

    final IFeatureModifier modifier = (IFeatureModifier) ce.createExecutableExtension( "class" );

    if( modifier instanceof IFeatureModifierExtension )
      ((IFeatureModifierExtension) modifier).init( ftp );

    return modifier;
  }

  private static synchronized IConfigurationElement getFeatureModifierElement( final String modifierId )
  {
    if( THE_FEATURE_MODIFIERS_MAP == null )
    {
      THE_FEATURE_MODIFIERS_MAP = new HashMap<String, IConfigurationElement>();

      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IExtensionPoint extensionPoint = registry.getExtensionPoint( TYPE_HANDLERS_EXTENSION_POINT );
      final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
      for( final IConfigurationElement element : configurationElements )
      {
        if( FEATURE_MODIFIER_ELEMENT_NAME.equals( element.getName() ) )
        {
          final String id = element.getAttribute( "id" );
          THE_FEATURE_MODIFIERS_MAP.put( id, element );
        }
      }
    }

    return THE_FEATURE_MODIFIERS_MAP.get( modifierId );
  }
}
