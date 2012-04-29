/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso. If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.ui.addlayer.internal.wms;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableContext;
import org.kalypso.commons.java.util.AbstractModelObject;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;

/**
 * @author Gernot Belger
 */
public class ImportWmsData extends AbstractModelObject implements ICapabilitiesData
{
  /**
   * This constant stores the id for the last used service.
   */
  public static final String PROPERTY_CURRENT_SERVICE = "currentService"; //$NON-NLS-1$

  public static final String PROPERTY_SERVICE_HISTORY = "serviceHistory"; //$NON-NLS-1$

  public static final String PROPERTY_MULTI_LAYER_ENABLEMENT = "multiLayerEnabled"; //$NON-NLS-1$

  public static final String PROPERTY_MULTI_LAYER = "multiLayer"; //$NON-NLS-1$

  /**
   * This constant stores the id for the sub dialog settings of this page.
   */
  private static final String SECTION_PROVIDERS = "providers"; //$NON-NLS-1$

  /**
   * The last successfully used service URL.
   */
  private CapabilitiesInfo m_currentService = new CapabilitiesInfo( StringUtils.EMPTY );

  /**
   * History of used services.<br/>
   * Contains all services that have been loaded successfully.<br/>
   */
  private final IObservableSet/* <CapabilitiesInfo> */m_serviceHistory = new WritableSet();

  /**
   * If <code>true</code>, add layers into one WMSTheme.
   */
  private boolean m_multiLayer = true;

  private IRunnableContext m_runnableContext = null;

  private final IObservableSet m_selectedLayers = new WritableSet();

  private final IObservableSet m_chosenLayers = new WritableSet();

  /**
   * This function initializes the variables from the dialog settings, if available.
   */
  public void init( final IDialogSettings settings )
  {
    if( settings == null )
      return;

    /* Get the last used service. */
    final String currentService = DialogSettingsUtils.getString( settings, PROPERTY_ADDRESS, StringUtils.EMPTY );
    m_currentService = new CapabilitiesInfo( currentService );

    /* Get the favorite services. */
    final String[] favoriteServices = settings.getArray( PROPERTY_SERVICE_HISTORY );
    if( favoriteServices != null )
    {
      for( final String address : favoriteServices )
        m_serviceHistory.add( new CapabilitiesInfo( address ) );
    }

    /* Get the service <--> provider mappings. */
    final IDialogSettings subDialogSettings = DialogSettingsUtils.getSection( settings, SECTION_PROVIDERS );

    for( final Object historyObject : m_serviceHistory )
    {
      final CapabilitiesInfo info = (CapabilitiesInfo) historyObject;
      final String address = info.getAddress();
      final String providerID = subDialogSettings.get( address );
      if( providerID != null )
        info.setProviderID( providerID );
    }
  }

  public void storeSettings( final IDialogSettings settings )
  {
    if( settings == null )
      return;

    /* Update the dialog settings for the last used service. */
    settings.put( PROPERTY_ADDRESS, m_currentService.getAddress() );

    /* Update the dialog settings for the service <--> provider mapping. */
    final IDialogSettings subSettings = settings.addNewSection( SECTION_PROVIDERS );

    /* Update the dialog settings for the last used services. */
    final Collection<String> history = new ArrayList<>( m_serviceHistory.size() );
    for( final Object historyObject : m_serviceHistory )
    {
      final CapabilitiesInfo info = (CapabilitiesInfo) historyObject;
      history.add( info.getAddress() );
    }
    settings.put( PROPERTY_SERVICE_HISTORY, history.toArray( new String[history.size()] ) );

    /* Iterate over all remaining favorites, and get the mapping out of the old sub dialog settings. */
    for( final Object historyObject : m_serviceHistory )
    {
      final CapabilitiesInfo info = (CapabilitiesInfo) historyObject;
      subSettings.put( info.getAddress(), info.getImageProvider() );
    }
  }

  public CapabilitiesInfo getCurrentService( )
  {
    return m_currentService;
  }

  public void setAddress( final String address )
  {
    final CapabilitiesInfo existingInfo = findServiceInHistory( address );
    if( existingInfo == null )
      setCurrentService( new CapabilitiesInfo( address ) );
    else
      setCurrentService( existingInfo );
  }

  @Override
  public String getAddress( )
  {
    return m_currentService.getAddress();
  }

  @Override
  public boolean getValidAddress( )
  {
    return m_currentService.getValidAddress();
  }

  public void setCurrentService( final CapabilitiesInfo service )
  {
    Assert.isNotNull( service );

    if( m_currentService.equals( service ) )
      return;

    final boolean oldMultiLayerEnablement = getMultiLayerEnabled();

    final CapabilitiesInfo oldService = m_currentService;
    final boolean oldValidAddress = getValidAddress();

    m_currentService = service;

    clearLayers();

    firePropertyChange( PROPERTY_CURRENT_SERVICE, oldService, m_currentService );

    firePropertyChange( PROPERTY_ADDRESS, oldService.getAddress(), m_currentService.getAddress() );
    firePropertyChange( PROPERTY_VALID_ADDRESS, oldValidAddress, getValidAddress() );
    firePropertyChange( PROPERTY_IMAGE_PROVIDER, oldService.getImageProvider(), m_currentService.getImageProvider() );

    firePropertyChange( PROPERTY_LOAD_STATUS, oldService.getStatus(), m_currentService.getStatus() );
    firePropertyChange( PROPERTY_TITLE, oldService.getTitle(), m_currentService.getTitle() );
    firePropertyChange( PROPERTY_ABSTRACT, oldService.getAbstract(), m_currentService.getAbstract() );
    firePropertyChange( PROPERTY_CAPABILITIES, oldService.getCapabilities(), service.getCapabilities() );
    firePropertyChange( PROPERTY_MULTI_LAYER_ENABLEMENT, oldMultiLayerEnablement, getMultiLayerEnabled() );
  }

  public CapabilitiesInfo[] getServiceHistory( )
  {
    return (CapabilitiesInfo[]) m_serviceHistory.toArray( new CapabilitiesInfo[m_serviceHistory.size()] );
  }

  public void setServiceHistory( final CapabilitiesInfo[] serviceHistory )
  {
    m_serviceHistory.clear();

    m_serviceHistory.addAll( Arrays.asList( serviceHistory ) );
  }

  @Override
  public String getImageProvider( )
  {
    return m_currentService.getImageProvider();
  }

  @Override
  public void setImageProvider( final String providerID )
  {
    final String oldProviderID = m_currentService.getImageProvider();

    m_currentService.setProviderID( providerID );

    firePropertyChange( PROPERTY_IMAGE_PROVIDER, oldProviderID, m_currentService.getImageProvider() );

    loadCapabilities();
  }

  public boolean getMultiLayerEnabled( )
  {
    return m_currentService.isLoaded();
  }

  public boolean getMultiLayer( )
  {
    return m_multiLayer;
  }

  public void setMultiLayer( final boolean multiLayer )
  {
    final boolean oldValue = m_multiLayer;

    m_multiLayer = multiLayer;

    firePropertyChange( PROPERTY_MULTI_LAYER, oldValue, multiLayer );
  }

  @Override
  public IStatus getLoadStatus( )
  {
    return m_currentService.getStatus();
  }

  @Override
  public WMSCapabilities getCapabilities( )
  {
    return m_currentService.getCapabilities();
  }

  @Override
  public String getTitle( )
  {
    return m_currentService.getTitle();
  }

  @Override
  public String getAbstract( )
  {
    return m_currentService.getAbstract();
  }

  /**
   * @return The layer list that should be added to the map.
   */
  public Layer[] getChosenLayers( )
  {
    return (Layer[]) m_chosenLayers.toArray( new Layer[m_chosenLayers.size()] );
  }

  public IObservableSet getSelectedLayerSet( )
  {
    return m_selectedLayers;
  }

  private void clearLayers( )
  {
    m_selectedLayers.clear();
    m_chosenLayers.clear();
  }

  /**
   * This function loads the capabilities of the given service. It caches the capabilities, if they are loaded once.
   *
   * @param service
   *          The URL to the service.
   */
  @Override
  public void loadCapabilities( )
  {
    if( m_runnableContext == null )
      throw new IllegalStateException();

    final CapabilitiesInfo service = m_currentService;

    final URL url = service.getURL();
    final String provider = service.getImageProvider();
    // FIXME: we should not be able to start loading if url is bad
    if( url == null )
      return;

    /* Create the runnable, which loads the capabilities. */
    final CapabilitiesGetter operation = new CapabilitiesGetter( url, provider );

    /* Execute it. */
    final IStatus status = RunnableContextHelper.execute( m_runnableContext, true, true, operation );

    /* Update the capabilities. */
    final IStatus oldStatus = service.getStatus();
    final String oldTitle = service.getTitle();
    final String oldAbstract = service.getAbstract();
    final WMSCapabilities oldCapabilities = service.getCapabilities();
    final boolean oldMultiLayerEnablement = service.isLoaded();

    final WMSCapabilities capabilities = operation.getCapabilities();
    service.setCapabilities( capabilities, status );

    /* fire associated property changes */
    firePropertyChange( PROPERTY_LOAD_STATUS, oldStatus, service.getStatus() );
    firePropertyChange( PROPERTY_TITLE, oldTitle, service.getTitle() );
    firePropertyChange( PROPERTY_ABSTRACT, oldAbstract, service.getAbstract() );
    firePropertyChange( PROPERTY_CAPABILITIES, oldCapabilities, service.getCapabilities() );
    firePropertyChange( PROPERTY_MULTI_LAYER_ENABLEMENT, oldMultiLayerEnablement, service.isLoaded() );

    /** Add capabilities into history */
    final CapabilitiesInfo storedInfo = findServiceInHistory( service.getAddress() );
    if( storedInfo == null )
      m_serviceHistory.add( service );

    clearLayers();
  }

  private CapabilitiesInfo findServiceInHistory( final String address )
  {
    for( final Object historyObject : m_serviceHistory )
    {
      final CapabilitiesInfo info = (CapabilitiesInfo) historyObject;
      if( info.getAddress().equals( address ) )
        return info;
    }

    return null;
  }

  public void setRunnableContext( final IRunnableContext context )
  {
    m_runnableContext = context;
  }

  public IObservableSet getChosenLayerSet( )
  {
    return m_chosenLayers;
  }

  public IObservableSet getServiceHistorySet( )
  {
    return m_serviceHistory;
  }
}