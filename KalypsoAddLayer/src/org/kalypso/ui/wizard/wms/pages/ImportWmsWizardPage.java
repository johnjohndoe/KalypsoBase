/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.ui.wizard.wms.pages;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.ui.wizard.wms.utils.CapabilitiesGetter;
import org.kalypso.ui.wizard.wms.utils.WMSCapabilitiesContentProvider;
import org.kalypso.ui.wizard.wms.utils.WMSCapabilitiesLabelProvider;

/**
 * This is a page for importing a WMS Layer.
 * 
 * @author Kuepferle, Doemming (original)
 * @author Holger Albert
 */
public class ImportWmsWizardPage extends WizardPage
{
  /**
   * This constant stores the id for the dialog settings of this page.
   */
  private static final String IMPORT_WMS_WIZARD_PAGE = "IMPORT_WMS_WIZARD_PAGE"; //$NON-NLS-1$

  /**
   * This constant stores the id for the sub dialog settings of this page.
   */
  private static final String IMPORT_WMS_WIZARD_PAGE_SUB = "IMPORT_WMS_WIZARD_PAGE_SUB"; //$NON-NLS-1$

  /**
   * This constant stores the id for the last used service.
   */
  private static final String LAST_USED_SERVICE = "LAST_USED_SERVICE"; //$NON-NLS-1$

  /**
   * This constant stores the id for the dialog settings of this page.
   */
  private static final String LAST_USED_SERVICES = "LAST_USED_SERVICES"; //$NON-NLS-1$

  /**
   * This constant stores the minimum list width.
   */
  private static final int MIN_LIST_WITH = 200;

  /**
   * This constant stores an small error text, concerning the URL.
   */
  private static final String MSG_BASEURL_ERROR = Messages.getString("org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.0"); //$NON-NLS-1$

  /**
   * This variable stores the dialog settings for this page.
   */
  private IDialogSettings m_dialogSettings;

  /**
   * The last successfull used service URL.
   */
  protected String m_lastService;

  /**
   * The last successfull used services. The favorites, to call them another way.
   */
  private List<String> m_lastServices;

  /**
   * The mappings from service (URL) to provider (provider id).
   */
  protected Map<String, String> m_serviceProviderMappings;

  /**
   * Capabilites cache in this wizard.
   */
  private final Map<URL, WMSCapabilities> m_capabilites;

  /**
   * This variable stores the last successfully used base URL.
   */
  private URL m_baseURL;

  /**
   * The url as text, which was last tried to load the service with.
   */
  private String m_text;

  /**
   * The text for entering the URL.
   */
  protected Text m_urlText;

  /**
   * The combo viewer for selecting an image provider.
   */
  private ComboViewer m_urlCombo;

  /**
   * This variable stores the tree, which displays the capabilities.
   */
  protected TreeViewer m_capabilitiesTree;

  /**
   * This variable stores the viewer, which displays the selected layers.
   */
  protected ListViewer m_layerViewer;

  /**
   * This variable stores the button for the multi layer option.
   */
  protected Button m_multiLayerButton;

  /**
   * This variable stores all available providers, which are registered at the extension point.
   */
  private final Map<String, String> m_availableProviders;

  /**
   * The constructor.
   * 
   * @param pageName
   *          The name of the page.
   */
  public ImportWmsWizardPage( final String pageName )
  {
    this( pageName, Messages.getString("org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.1"), null ); //$NON-NLS-1$
  }

  /**
   * The constructor.
   * 
   * @param pageName
   *          The name of the page.
   * @param title
   *          The title for this wizard page, or null if none.
   * @param titleImage
   *          The image descriptor for the title of this wizard page, or null if none.
   */
  public ImportWmsWizardPage( final String pageName, final String title, final ImageDescriptor titleImage )
  {
    super( pageName, title, titleImage );

    /* Initialize. */
    m_dialogSettings = null;

    /* No default settings (only used for manageing the dialog settings). */
    m_lastService = null;
    m_lastServices = new LinkedList<String>();
    m_serviceProviderMappings = new HashMap<String, String>();

    /* Initialize the capabilities cache. */
    m_capabilites = new HashMap<URL, WMSCapabilities>();

    /* Initialize the selection variables. */
    m_baseURL = null;

    /* Initialize the last tried text for loading a service. */
    m_text = null;

    /* Initialize some UI elements. */
    m_urlText = null;
    m_capabilitiesTree = null;
    m_layerViewer = null;
    m_multiLayerButton = null;

    /* Initialize the list for all available providers. */
    m_availableProviders = new LinkedHashMap<String, String>();

    /* The page is not complete in the beginning. */
    setPageComplete( false );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    /* Init the dialog settings. */
    initDialogSettings();

    /* Init the variables from the dialog settings. */
    initFromDialogSettings();

    /* Init the provider list. */
    initExtensions();

    /* Create the main composite. */
    final Composite panel = new Composite( parent, SWT.NONE );
    panel.setLayout( new GridLayout( 1, false ) );
    panel.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create the section for the URL selection. */
    final Composite urlComposite = new Composite( panel, SWT.NONE );
    urlComposite.setLayout( new GridLayout( 4, false ) );
    urlComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    /* The url label. */
    final Label urlLabel = new Label( urlComposite, SWT.NONE );
    urlLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
    urlLabel.setText( Messages.getString("org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.2") ); //$NON-NLS-1$
    urlLabel.setToolTipText( Messages.getString("org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.3") ); //$NON-NLS-1$

    /* The url text. */
    m_urlText = new Text( urlComposite, SWT.BORDER );
    final GridData urlTextData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    urlTextData.widthHint = 300;
    urlTextData.minimumWidth = 300;
    m_urlText.setLayoutData( urlTextData );
    m_urlText.setToolTipText( Messages.getString("org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.4") ); //$NON-NLS-1$

    /* The url combo. */
    m_urlCombo = new ComboViewer( urlComposite, SWT.READ_ONLY );
    final GridData urlComboData = new GridData( SWT.BEGINNING, SWT.CENTER, false, false );
    urlComboData.widthHint = 100;
    urlComboData.minimumWidth = 100;
    m_urlCombo.getCombo().setLayoutData( urlComboData );
    m_urlCombo.getCombo().setToolTipText( Messages.getString("org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.5") ); //$NON-NLS-1$

    /* Set the content provider. */
    m_urlCombo.setContentProvider( new ArrayContentProvider() );

    /* Set the label provider. */
    m_urlCombo.setLabelProvider( new LabelProvider()
    {
      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
       */
      @Override
      public String getText( final Object element )
      {
        if( element instanceof Entry )
        {
          final Entry< ? , ? > entry = (Entry< ? , ? >) element;

          return entry.getValue().toString();
        }

        return null;
      }
    } );

    /* Set the input. */
    m_urlCombo.setInput( m_availableProviders.entrySet() );

    /* The favorites button. */
    final Button favoritesButton = new Button( urlComposite, SWT.NONE );
    favoritesButton.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
    favoritesButton.setText( "Favoriten" ); //$NON-NLS-1$
    favoritesButton.setToolTipText( Messages.getString("org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.6") ); //$NON-NLS-1$

    /* Create the section for the layer selection. */
    final Group layerGroup = new Group( panel, SWT.NONE );
    layerGroup.setLayout( new GridLayout( 3, false ) );
    layerGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    layerGroup.setText( Messages.getString("org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.7") ); //$NON-NLS-1$

    /* The capabilities tree. */
    m_capabilitiesTree = new TreeViewer( layerGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
    final GridData capabilitiesData = new GridData( SWT.FILL, SWT.FILL, true, true );
    capabilitiesData.widthHint = MIN_LIST_WITH;
    capabilitiesData.minimumWidth = MIN_LIST_WITH;
    m_capabilitiesTree.getControl().setLayoutData( capabilitiesData );

    /* Set the wms content provider. */
    m_capabilitiesTree.setContentProvider( new WMSCapabilitiesContentProvider() );

    /* Set the wms label provider. */
    m_capabilitiesTree.setLabelProvider( new WMSCapabilitiesLabelProvider() );

    /* The layer button composite. */
    final Composite layerButtonComposite = new Composite( layerGroup, SWT.NONE );
    layerButtonComposite.setLayout( new GridLayout( 1, false ) );
    final GridData layerButtonData = new GridData( SWT.CENTER, SWT.FILL, false, true );
    layerButtonData.widthHint = 37;
    layerButtonData.minimumWidth = 37;
    layerButtonComposite.setLayoutData( layerButtonData );

    /* The layer button for adding a layer. */
    final Button layerButtonAdd = new Button( layerButtonComposite, SWT.PUSH );
    layerButtonAdd.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );
    layerButtonAdd.setImage( ImageProvider.IMAGE_STYLEEDITOR_FORWARD.createImage() );
    layerButtonAdd.setToolTipText( Messages.getString("org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.8") ); //$NON-NLS-1$

    /* The layer button for removing a layer. */
    final Button layerButtonRemove = new Button( layerButtonComposite, SWT.PUSH );
    layerButtonRemove.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );
    layerButtonRemove.setImage( ImageProvider.IMAGE_STYLEEDITOR_REMOVE.createImage() );
    layerButtonRemove.setToolTipText( Messages.getString("org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.9") ); //$NON-NLS-1$

    /* The layer viewer. */
    m_layerViewer = new ListViewer( layerGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
    final GridData layerViewerData = new GridData( SWT.FILL, SWT.FILL, true, true );
    layerViewerData.widthHint = MIN_LIST_WITH;
    layerViewerData.minimumWidth = MIN_LIST_WITH;
    m_layerViewer.getList().setLayoutData( layerViewerData );

    /* Set the content provider. */
    m_layerViewer.setContentProvider( new IStructuredContentProvider()
    {
      /**
       * The current input.
       */
      private Object m_input = null;

      /**
       * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
       */
      @Override
      public Object[] getElements( final Object inputElement )
      {
        if( m_input instanceof List )
          return ((List< ? >) m_input).toArray();

        return new Object[] { m_input };
      }

      /**
       * @see org.eclipse.jface.viewers.IContentProvider#dispose()
       */
      @Override
      public void dispose( )
      {
        m_input = null;
      }

      /**
       * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
       *      java.lang.Object, java.lang.Object)
       */
      @Override
      public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
      {
        m_input = newInput;
      }
    } );

    /* Set the wms label provider. */
    m_layerViewer.setLabelProvider( new WMSCapabilitiesLabelProvider() );

    /* The multi layer button. */
    m_multiLayerButton = new Button( layerGroup, SWT.CHECK );
    final GridData multiData = new GridData( SWT.BEGINNING, SWT.CENTER, false, false );
    multiData.horizontalSpan = 3;
    m_multiLayerButton.setLayoutData( multiData );
    m_multiLayerButton.setText( Messages.getString("org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.10") ); //$NON-NLS-1$
    m_multiLayerButton.setToolTipText( Messages.getString("org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.11") ); //$NON-NLS-1$
    m_multiLayerButton.setEnabled( false );

    /* Add all required listener here. */

    /* The selection listener for the url text. */
    m_urlText.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetDefaultSelected( final SelectionEvent e )
      {
        /* Get the source. */
        final Text source = (Text) e.getSource();

        /* Update the UI. */
        updateAllFromURLText( source.getText() );
      }
    } );

    /* The focus listener for the url text. */
    m_urlText.addFocusListener( new FocusListener()
    {
      /**
       * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
       */
      @Override
      public void focusGained( final FocusEvent e )
      {
      }

      /**
       * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
       */
      @Override
      public void focusLost( final FocusEvent e )
      {
        /* Get the source. */
        final Text source = (Text) e.getSource();

        /* Update the UI. */
        updateAllFromURLText( source.getText() );
      }
    } );

    /* The selection changed listener for the url combo. */
    m_urlCombo.addSelectionChangedListener( new ISelectionChangedListener()
    {
      /**
       * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
       */
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        if( !m_urlText.getText().equals( m_lastService ) )
          return;

        final String providerID = getProviderID();
        if( providerID == null )
          return;

        m_serviceProviderMappings.put( m_lastService, providerID );
      }
    } );

    favoritesButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        /* Open the favorites dialog. */
        handleFavoritesClicked();
      }
    } );

    /* The double click listener for the capabilities tree. */
    m_capabilitiesTree.addDoubleClickListener( new IDoubleClickListener()
    {
      /**
       * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
       */
      @Override
      public void doubleClick( final DoubleClickEvent event )
      {
        /* Get the current selection. */
        final IStructuredSelection selection = (IStructuredSelection) m_capabilitiesTree.getSelection();

        /* Add the layer. */
        handleAddLayer( selection );
      }
    } );

    /* The selection listener for the layer button add. */
    layerButtonAdd.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        /* Get the current selection. */
        final IStructuredSelection selection = (IStructuredSelection) m_capabilitiesTree.getSelection();

        /* Add the layer. */
        handleAddLayer( selection );
      }
    } );

    /* The selection listener for the layer button remove. */
    layerButtonRemove.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final IStructuredSelection selection = (IStructuredSelection) m_layerViewer.getSelection();
        if( selection == null || selection.isEmpty() )
          return;

        /* Get all layers. */
        final List<Layer> input = getLayers();

        /* Remove the selected. */
        input.removeAll( selection.toList() );

        /* Set it again. */
        m_layerViewer.setInput( input );

        /* Update the multi layer Button. */
        m_multiLayerButton.setEnabled( input.size() > 1 );

        /* If there is no input anymore, the page cannot be completed. */
        setPageComplete( !input.isEmpty() );
      }
    } );

    /* Set the default values. */

    /* The default for the url text. */
    if( m_lastService != null )
      m_urlText.setText( m_lastService );

    /* The default for the url combo. */
    final String providerID = m_serviceProviderMappings.get( m_lastService );
    if( providerID != null )
    {
      final Set<Entry<String, String>> entrySet = m_availableProviders.entrySet();
      for( final Entry<String, String> entry : entrySet )
      {
        if( entry.getKey().equals( providerID ) )
        {
          m_urlCombo.setSelection( new StructuredSelection( entry ) );
          break;
        }
      }
    }

    /* Set the control. */
    setControl( panel );
  }

  /**
   * This function inits the provider list from the extensions.
   */
  private void initExtensions( )
  {
    /* Get the extension registry. */
    final IExtensionRegistry er = Platform.getExtensionRegistry();

    /* The registry must exist. */
    if( er != null )
    {
      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( "org.kalypso.ui.addlayer.WMSImageProvider" ); //$NON-NLS-1$
      for( final IConfigurationElement element : configurationElementsFor )
      {
        /* Get some attributes. */
        final String id = element.getAttribute( "id" ); //$NON-NLS-1$
        final String name = element.getAttribute( "name" ); //$NON-NLS-1$

        /* Index with the id. */
        m_availableProviders.put( id, name );
      }
    }
  }

  /**
   * This function initializes the dialog settings for this page. Could not be called in the constructor, because then,
   * the wizard is not set.
   */
  private void initDialogSettings( )
  {
    /* Get the wizard. */
    final IWizard wizard = getWizard();
    if( wizard != null )
    {
      /* Get its dialog settings. */
      final IDialogSettings dialogSettings = wizard.getDialogSettings();

      /* If they are not null, ... */
      if( dialogSettings != null )
      {
        /* ... then get the section for this page. */
        m_dialogSettings = dialogSettings.getSection( IMPORT_WMS_WIZARD_PAGE );

        /* If the section is missing, create it. */
        if( m_dialogSettings == null )
          m_dialogSettings = dialogSettings.addNewSection( IMPORT_WMS_WIZARD_PAGE );

        /* Get the service <--> provider mappings section. */
        final IDialogSettings subDialogSettings = m_dialogSettings.getSection( IMPORT_WMS_WIZARD_PAGE_SUB );

        /* If the section is missing, create it. */
        if( subDialogSettings == null )
          m_dialogSettings.addNewSection( IMPORT_WMS_WIZARD_PAGE_SUB );
      }
    }
  }

  /**
   * This function inits the variables from the dialog settings, if available.
   */
  private void initFromDialogSettings( )
  {
    if( m_dialogSettings == null )
      return;

    /* Get the last used service. */
    m_lastService = m_dialogSettings.get( LAST_USED_SERVICE );

    /* Get the favorite services. */
    final String[] favoriteServices = m_dialogSettings.getArray( LAST_USED_SERVICES );
    if( favoriteServices != null )
    {
      for( final String favoriteService : favoriteServices )
        m_lastServices.add( favoriteService );
    }

    /* Get the service <--> provider mappings. */
    final IDialogSettings subDialogSettings = m_dialogSettings.getSection( IMPORT_WMS_WIZARD_PAGE_SUB );

    for( int i = 0; i < m_lastServices.size(); i++ )
    {
      final String service = m_lastServices.get( i );
      final String providerID = subDialogSettings.get( service );

      if( providerID != null )
        m_serviceProviderMappings.put( service, providerID );
    }
  }

  /**
   * This function updates the capabilities tree and the rest of the UI.
   * 
   * @param urlText
   *          The typed URL.
   */
  protected void updateAllFromURLText( final String urlText )
  {
    /* If the the last tried text to load the capabilities, was the same, do nothing. */
    if( m_text != null && m_text.equals( urlText ) )
      return;

    /* Store this try for the next call of this function. */
    m_text = urlText;

    /* Reset the errors. */
    setErrorMessage( null );
    setMessage( "" ); //$NON-NLS-1$
    setPageComplete( false );

    /* Reset the UI. */
    m_capabilitiesTree.setInput( null );
    m_layerViewer.setInput( null );
    m_multiLayerButton.setSelection( false );

    try
    {
      /* Validate the typed URL. */
      final URL baseURL = validateURLField( urlText );
      if( baseURL != null )
      {
        /* Set the base URL. */
        m_baseURL = baseURL;
        m_capabilitiesTree.setInput( getCapabilites( baseURL ) );

        /* Update the variables for the dialog settings. */

        /* Need the base URL. */
        final String lastService = baseURL.toExternalForm();

        /* Udpate the variable for the last used service. */
        m_lastService = lastService;

        /* Udpate the variable for the last used services. */
        if( !m_lastServices.contains( lastService ) )
          m_lastServices.add( lastService );

        /* Update the variable for the service <--> provider mapping. */

        /* Get the selected provider id. */
        final String providerID = getProviderID();

        /* Set it, if available. */
        if( providerID != null )
          m_serviceProviderMappings.put( lastService, providerID );
      }
    }
    catch( final Exception e )
    {
      m_baseURL = null;

      setErrorMessage( MSG_BASEURL_ERROR + ": " + e.getLocalizedMessage() + "\n(" + e.getClass().getName() + ")" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
  }

  /**
   * This function updates the error text, if the typed URL is not valid.
   * 
   * @param url
   *          The typed URL.
   */
  private URL validateURLField( final String url )
  {
    URL validURL = null;
    String errorMsg = null;

    try
    {
      /* Checks catchment field entry and file suffix. */
      if( url.length() == 0 )
        errorMsg = Messages.getString("org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.12"); //$NON-NLS-1$
      else
      {
        validURL = new URL( url );

        validURL.openConnection();
      }
    }
    catch( final Exception e )
    {
      errorMsg = e.getLocalizedMessage();
      validURL = null;
    }

    if( validURL != null )
      setMessage( Messages.getString("org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.13") ); //$NON-NLS-1$
    else
      setErrorMessage( MSG_BASEURL_ERROR + errorMsg );

    return validURL;
  }

  /**
   * This function loads the capabilities of the given service. It caches the capabilities, if they are loaded once.
   * 
   * @param service
   *          The URL to the service.
   */
  private synchronized WMSCapabilities getCapabilites( final URL service ) throws Exception
  {
    /* If there is one cached, take this. */
    if( m_capabilites.containsKey( service ) )
    {
      final WMSCapabilities capabilities = m_capabilites.get( service );
      if( capabilities != null )
        return capabilities;
    }

    /* Create the runnable, which loads the capabilities. */
    final CapabilitiesGetter runnable = new CapabilitiesGetter( service, getProviderID() );

    /* Execute it. */
    final IStatus execute = RunnableContextHelper.execute( getContainer(), false, false, runnable );
    if( !execute.isOK() )
      setErrorMessage( execute.getMessage() );

    /* Get the capabilities. */
    final WMSCapabilities capabilities = runnable.getCapabilities();

    /* Cache them. */
    if( capabilities != null )
      m_capabilites.put( service, capabilities );

    return runnable.getCapabilities();
  }

  /**
   * This function returns the selected layers.
   * 
   * @return The selected layers.
   */
  public Layer[] getLayersList( )
  {
    final List<Layer> list = getLayers();

    return list.toArray( new Layer[list.size()] );
  }

  /**
   * For casting the input to List<Layer>.
   * 
   * @return The layer list.
   */
  @SuppressWarnings("unchecked")
  protected List<Layer> getLayers( )
  {
    Object input = m_layerViewer.getInput();
    if( input == null )
      input = new ArrayList<Layer>();

    return (List<Layer>) input;
  }

  /**
   * This function opens the favorites dialog.
   */
  protected void handleFavoritesClicked( )
  {
    if( m_dialogSettings == null )
      return;

    /* Create the favorites dialog. */
    final WMSFavoritesDialog dialog = new WMSFavoritesDialog( getShell(), m_lastService, m_lastServices );

    /* Open the favorites dialog. */
    if( dialog.open() != Window.OK )
      return;

    /* If here, the user has clicked ok. */
    final String selectedService = dialog.getSelectedService();

    /* This URL is already set. */
    if( m_baseURL != null && m_baseURL.toExternalForm().equals( selectedService ) )
      return;

    if( selectedService != null )
    {
      m_urlText.setText( selectedService );

      /* Reset the UI. */
      m_urlCombo.setSelection( new StructuredSelection() );

      /* Get the provider for this service. */
      final String selectedProvider = m_serviceProviderMappings.get( selectedService );
      if( selectedProvider != null )
      {
        final Set<Entry<String, String>> entrySet = m_availableProviders.entrySet();
        for( final Entry<String, String> entry : entrySet )
        {
          if( entry.getKey().equals( selectedProvider ) )
          {
            m_urlCombo.setSelection( new StructuredSelection( entry ) );
            break;
          }
        }
      }

      /* Update the UI. */
      updateAllFromURLText( selectedService );
    }
  }

  /**
   * This function adds the selection to the selected layers.
   * 
   * @param selection
   *          The current selection.
   */
  protected void handleAddLayer( final IStructuredSelection selection )
  {
    if( selection == null || selection.isEmpty() )
      return;

    final List<Layer> input = getLayers();
    List<Layer> selectableLayer = new ArrayList<Layer>();

    for( final Iterator< ? > iter = selection.iterator(); iter.hasNext(); )
    {
      final Layer layer = (Layer) iter.next();
      selectableLayer = getSelectableLayer( selectableLayer, layer );
    }

    /* Only add layers that are not already in the selected layer viewer. */
    for( final Layer layer : selectableLayer )
    {
      if( !input.contains( layer ) )
        input.add( layer );
    }

    m_layerViewer.setInput( input );

    /* Update the multi layer Button. */
    m_multiLayerButton.setEnabled( input.size() > 1 );

    /* Set the page complete, if any layer is available. */
    setPageComplete( !input.isEmpty() );
  }

  /**
   * This function returns all selectable layers.
   * 
   * @return All selectable layers.
   */
  private List<Layer> getSelectableLayer( final List<Layer> resultCollector, final Layer layer )
  {
    List<Layer> resultList;
    if( resultCollector == null )
      resultList = new ArrayList<Layer>();
    else
      resultList = resultCollector;

    final Layer[] subLayers = layer.getLayer();
    if( subLayers.length > 0 )
    {
      for( final Layer subLayer : subLayers )
        resultList = getSelectableLayer( resultList, subLayer );
    }
    else
      resultList.add( layer );

    return resultList;
  }

  /**
   * This function returns the selected base URL.
   * 
   * @return The selected base URL.
   */
  public URL getBaseURL( )
  {
    return m_baseURL;
  }

  /**
   * This function returns true, if the checkbox for multi layer is selected.
   * 
   * @return True, if multilayer is wanted.
   */
  public boolean isMultiLayer( )
  {
    return m_multiLayerButton.getSelection();
  }

  /**
   * This function returns the selected image provider id, or null, if not available.
   * 
   * @return The selected image provider id or null, if not available.
   */
  public String getProviderID( )
  {
    if( m_availableProviders.size() == 0 )
      return null;

    final ISelection selection = m_urlCombo.getSelection();
    if( selection.isEmpty() )
      return null;

    if( !(selection instanceof IStructuredSelection) )
      return null;

    final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
    final Object firstElement = structuredSelection.getFirstElement();
    if( firstElement == null )
      return null;

    if( !(firstElement instanceof Entry) )
      return null;

    final Entry< ? , ? > entry = (Entry< ? , ? >) firstElement;
    final Object key = entry.getKey();
    if( !(key instanceof String) )
      return null;

    return (String) key;
  }

  /**
   * Finishes the work of this page. Also updates the dialog settings.
   */
  public void finish( )
  {
    if( m_dialogSettings != null )
    {
      /* Update the dialog settings for the last used service. */
      m_dialogSettings.put( LAST_USED_SERVICE, m_lastService );

      /* Update the dialog settings for the last used services. */
      m_dialogSettings.put( LAST_USED_SERVICES, m_lastServices.toArray( new String[] {} ) );

      /* Update the dialog settings for the service <--> provider mapping. */
      final IDialogSettings subSettings = m_dialogSettings.addNewSection( IMPORT_WMS_WIZARD_PAGE_SUB );

      /* Iterate over all remaining favorites, and get the mapping out of the old sub dialog settings. */
      for( final String service : m_lastServices )
      {
        /* Update the new sub settings with the mapping out of the old sub settings. */
        subSettings.put( service, m_serviceProviderMappings.get( service ) );
      }
    }
  }

  public void setServices( final List<String> services )
  {
    m_lastServices = services;
  }
}