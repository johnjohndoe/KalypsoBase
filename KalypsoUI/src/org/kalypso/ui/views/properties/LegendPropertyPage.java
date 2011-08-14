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
package org.kalypso.ui.views.properties;

import java.awt.Insets;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.kalypso.contribs.eclipse.swt.widgets.ImageCanvas;
import org.kalypso.core.status.StatusComposite;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.themes.KalypsoLegendTheme;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ogc.gml.outline.nodes.LegendExporter;
import org.kalypso.ogc.gml.outline.nodes.NodeFactory;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.util.themes.ThemeUtilities;
import org.kalypso.util.themes.legend.LegendUtilities;
import org.kalypso.util.themes.legend.controls.LegendComposite;
import org.kalypso.util.themes.legend.listener.ILegendChangedListener;

/**
 * This page will show a legend for a theme, if one is available.
 * 
 * @author Holger Albert
 */
public class LegendPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{
  /**
   * The node.
   */
  private IThemeNode m_node;

  /**
   * The theme.
   */
  private IKalypsoTheme m_theme;

  /**
   * A up to date properties object, containing all serialized legend properties.
   */
  protected Properties m_properties;

  /**
   * The properties tab.
   */
  private CTabItem m_propertiesTabItem;

  /**
   * The preview tab.
   */
  private CTabItem m_previewTabItem;

  /**
   * The constructor.
   */
  public LegendPropertyPage( )
  {
    /* Initialize the members. */
    m_node = null;
    m_theme = null;
    m_properties = null;
    m_propertiesTabItem = null;
    m_previewTabItem = null;
  }

  /**
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents( final Composite parent )
  {
    /* Initialize. */
    init();

    /* Create the main composite. */
    final Composite main = new Composite( parent, SWT.NONE );
    main.setLayout( new GridLayout( 1, false ) );
    main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Get the node. */
    if( m_node == null || m_theme == null )
    {
      /* Create a label. */
      final Label messageLabel = new Label( main, SWT.NONE );
      messageLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
      messageLabel.setText( Messages.getString( "org.kalypso.ui.views.properties.LegendPropertyPage.0" ) );//$NON-NLS-1$

      return main;
    }

    /* Get the theme. */
    if( !(m_theme instanceof KalypsoLegendTheme) )
    {
      /* Create the properties page for a other theme. */
      createOtherProperties( main );

      return main;
    }

    /* Create the properties page for a legend theme. */
    createLegendProperties( main );

    return main;
  }

  /**
   * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
   */
  @Override
  protected void performDefaults( )
  {
    if( m_node == null || m_theme == null )
    {
      super.performDefaults();
      return;
    }

    if( !(m_theme instanceof KalypsoLegendTheme) )
    {
      super.performDefaults();
      return;
    }

    /* Get the default properties. */
    m_properties = LegendUtilities.getDefaultProperties();

    /* Update the property tab. */
    updatePropertyTab();

    /* Update the preview tab. */
    updatePreviewTab();

    super.performDefaults();
  }

  /**
   * @see org.eclipse.jface.preference.PreferencePage#performOk()
   */
  @Override
  public boolean performOk( )
  {
    if( m_theme == null || m_properties == null )
      return super.performOk();

    if( !(m_theme instanceof KalypsoLegendTheme) )
      return super.performOk();

    ((KalypsoLegendTheme) m_theme).updatePosition( m_properties );

    return super.performOk();
  }

  /**
   * This function initializes the property page.
   */
  private void init( )
  {
    /* Get the element. */
    final IAdaptable element = getElement();

    /* Get the node. */
    final IThemeNode node = (IThemeNode) (element instanceof IThemeNode ? element : element.getAdapter( IThemeNode.class ));

    /* Get the theme. */
    final Object nodeElement = node.getElement();
    final IKalypsoTheme theme = (nodeElement instanceof IKalypsoTheme) ? (IKalypsoTheme) nodeElement : null;

    /* Store the members. */
    m_node = node;
    m_theme = theme;
    m_properties = null;

    if( m_theme instanceof KalypsoLegendTheme )
    {
      /* Cast. */
      final KalypsoLegendTheme legendTheme = (KalypsoLegendTheme) m_theme;

      /* Get the properties of the legend theme. */
      final Properties legendProperties = new Properties();
      final String[] propertyNames = legendTheme.getPropertyNames();
      for( final String propertyName : propertyNames )
        legendProperties.put( propertyName, legendTheme.getProperty( propertyName, null ) );

      /* Store the member. */
      m_properties = legendProperties;
    }
  }

  /**
   * This function creates the properties page for a other theme.
   * 
   * @param parent
   *          The parent composite.
   */
  private void createOtherProperties( final Composite parent )
  {
    try
    {
      /* Get the display. */
      final Display display = parent.getDisplay();

      /* Get the legend graphic. */
      final LegendExporter legendExporter = new LegendExporter();
      final Image legendGraphic = legendExporter.exportLegends( null, new IThemeNode[] { m_node }, display, null, new RGB( 255, 255, 255 ), -1, -1, false, null );
      if( legendGraphic == null )
        throw new Exception( Messages.getString( "org.kalypso.ui.views.properties.LegendPropertyPage.2" ) );//$NON-NLS-1$

      /* Create a group. */
      final Composite group = new Composite( parent, SWT.BORDER );
      group.setLayout( new GridLayout( 1, false ) );
      group.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
      group.setBackground( display.getSystemColor( SWT.COLOR_WHITE ) );

      /* And finally display it. */
      /* REMARK: We are using an real ImageCanvas instead of just setting the background, */
      /* as this will not work for transparent images. */
      final ImageCanvas canvas = new ImageCanvas( group, SWT.NONE );
      final Rectangle bounds = legendGraphic.getBounds();
      final GridData canvasData = new GridData( SWT.BEGINNING, SWT.TOP, true, true );
      canvasData.heightHint = bounds.height;
      canvasData.widthHint = bounds.width;
      canvas.setLayoutData( canvasData );
      canvas.setSize( bounds.width, bounds.height );
      canvas.setBackground( display.getSystemColor( SWT.COLOR_WHITE ) );
      canvas.setImage( legendGraphic );
    }
    catch( final Exception ex )
    {
      /* Create a status composite. */
      final StatusComposite statusComposite = new StatusComposite( parent, SWT.NONE );
      statusComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
      statusComposite.setStatus( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), "Konnte die Legende nicht anzeigen...", ex ) );
    }
  }

  /**
   * This function creates the properties page for a legend theme.
   * 
   * @param parent
   *          The parent composite.
   */
  private void createLegendProperties( final Composite parent )
  {
    /* Create a tab folder. */
    final CTabFolder legendTabFolder = new CTabFolder( parent, SWT.BORDER );
    legendTabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    legendTabFolder.setSimple( true );
    legendTabFolder.setTabPosition( SWT.TOP );

    /* Create the properties tab. */
    createPropertiesTab( legendTabFolder );

    /* Create the preview tab. */
    createPreviewTab( legendTabFolder );

    /* Select the first tab. */
    legendTabFolder.setSelection( 0 );

    /* Add a listener. */
    legendTabFolder.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        /* Get the source. */
        final CTabFolder source = (CTabFolder) e.getSource();

        /* Update the preview tab, if it is switched to it. */
        if( source.getSelectionIndex() == 1 )
        {
          /* Update the preview tab. */
          updatePreviewTab();
        }
      }
    } );
  }

  /**
   * This function creates the properties tab.
   * 
   * @param parent
   *          The parent tab folder.
   */
  private void createPropertiesTab( final CTabFolder parent )
  {
    /* Create a tab item. */
    m_propertiesTabItem = new CTabItem( parent, SWT.NONE );
    m_propertiesTabItem.setText( "Eigenschaften" );

    /* Create the properties composite. */
    final Composite propertiesComposite = createPropertiesComposite( parent );

    /* Set the control. */
    m_propertiesTabItem.setControl( propertiesComposite );
  }

  /**
   * This function creates the properties composite.
   * 
   * @param parent
   *          The parent tab folder.
   * @return The properties composite.
   */
  private Composite createPropertiesComposite( final CTabFolder parent )
  {
    /* Create a composite. */
    final Composite propertiesComposite = new Composite( parent, SWT.NONE );
    propertiesComposite.setLayout( new GridLayout( 1, false ) );

    /* Create the legend composite. */
    final LegendComposite legendComposite = new LegendComposite( propertiesComposite, SWT.NONE, m_theme.getMapModell(), m_properties );
    legendComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    legendComposite.addLegendChangedListener( new ILegendChangedListener()
    {
      /**
       * @see org.kalypso.util.themes.legend.listener.ILegendChangedListener#legendPropertyChanged(java.util.Properties,
       *      int, int, org.eclipse.swt.graphics.Color, int, java.lang.String[])
       */
      @Override
      public void legendPropertyChanged( final Properties properties, final int horizontal, final int vertical, final Color backgroundColor, final int insets, final String[] themeIds )
      {
        /* Update the properties object. */
        m_properties = properties;

        /* HINT: Do not update the preview tab here. */
        /* HINT: It could be very slow. */
      }
    } );

    return propertiesComposite;
  }

  /**
   * This function creates the preview tab.
   * 
   * @param parent
   *          The parent tab folder.
   */
  private void createPreviewTab( final CTabFolder parent )
  {
    /* Create a tab item. */
    m_previewTabItem = new CTabItem( parent, SWT.NONE );
    m_previewTabItem.setText( "Vorschau" );

    /* Create the preview composite. */
    final Composite previewComposite = createPreviewComposite( parent );

    /* Set the control. */
    m_previewTabItem.setControl( previewComposite );
  }

  /**
   * This function creates the preview composite.
   * 
   * @param parent
   *          The parent tab folder.
   * @return The preview composite.
   */
  private Composite createPreviewComposite( final CTabFolder parent )
  {
    /* Create a composite. */
    final Composite previewComposite = new Composite( parent, SWT.NONE );
    previewComposite.setLayout( new GridLayout( 1, false ) );

    /* Create the legend. */
    createPreviewLegend( previewComposite );

    return previewComposite;
  }

  /**
   * This function creates the preview legend.
   * 
   * @param parent
   *          The parent composite.
   */
  private void createPreviewLegend( final Composite parent )
  {
    /* Get the display. */
    final Display display = parent.getDisplay();

    try
    {
      /* Get the properties. */
      final String backgroundColorProperty = m_properties.getProperty( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR );
      final String insetsProperty = m_properties.getProperty( LegendUtilities.THEME_PROPERTY_INSETS );
      final String themeIdsProperty = m_properties.getProperty( LegendUtilities.THEME_PROPERTY_THEME_IDS );

      /* Check the properties. */
      Color backgroundColor = ThemeUtilities.checkBackgroundColor( display, backgroundColorProperty );
      if( backgroundColor == null )
        backgroundColor = display.getSystemColor( SWT.COLOR_WHITE );
      int insets = LegendUtilities.checkInsets( insetsProperty );
      if( insets == -1 )
        insets = 5;
      final List<String> themeIds = LegendUtilities.verifyThemeIds( m_theme.getMapModell(), themeIdsProperty );
      if( themeIds == null || themeIds.size() == 0 )
        throw new Exception( "Es wurden keine Themen ausgewählt..." );

      /* Create the nodes. */
      final IThemeNode rootNode = NodeFactory.createRootNode( m_theme.getMapModell(), null );
      final IThemeNode[] nodes = rootNode.getChildren();

      /* Get the legend graphic. */
      final LegendExporter legendExporter = new LegendExporter();
      final Image legendGraphic = legendExporter.exportLegends( themeIds.toArray( new String[] {} ), nodes, display, new Insets( insets, insets, insets, insets ), backgroundColor.getRGB(), -1, -1, false, null );
      if( legendGraphic == null )
        throw new Exception( Messages.getString( "org.kalypso.ui.views.properties.LegendPropertyPage.2" ) );//$NON-NLS-1$

      /* Create a group. */
      final Composite group = new Composite( parent, SWT.BORDER );
      group.setLayout( new GridLayout( 1, false ) );
      group.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
      group.setBackground( display.getSystemColor( SWT.COLOR_WHITE ) );

      /* And finally display it. */
      /* REMARK: We are using an real ImageCanvas instead of just setting the background, */
      /* as this will not work for transparent images. */
      final ImageCanvas canvas = new ImageCanvas( group, SWT.NONE );
      final Rectangle bounds = legendGraphic.getBounds();
      final GridData canvasData = new GridData( SWT.BEGINNING, SWT.TOP, true, true );
      canvasData.heightHint = bounds.height;
      canvasData.widthHint = bounds.width;
      canvas.setLayoutData( canvasData );
      canvas.setSize( bounds.width, bounds.height );
      canvas.setBackground( display.getSystemColor( SWT.COLOR_WHITE ) );
      canvas.setImage( legendGraphic );
    }
    catch( final Exception ex )
    {
      /* Create a status composite. */
      final StatusComposite statusComposite = new StatusComposite( parent, SWT.NONE );
      statusComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
      statusComposite.setStatus( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), "Konnte die Legende nicht anzeigen...", ex ) );
    }
  }

  /**
   * This function updates the property tab.
   */
  protected void updatePropertyTab( )
  {
    if( m_propertiesTabItem == null || m_propertiesTabItem.isDisposed() )
      return;

    /* Dispose the control. */
    final Control control = m_propertiesTabItem.getControl();
    control.dispose();

    /* Create the properties composite. */
    final Composite propertiesComposite = createPropertiesComposite( m_propertiesTabItem.getParent() );

    /* Set the control. */
    m_propertiesTabItem.setControl( propertiesComposite );
  }

  /**
   * This function updates the preview tab.
   */
  protected void updatePreviewTab( )
  {
    if( m_previewTabItem == null || m_previewTabItem.isDisposed() )
      return;

    /* Dispose the control. */
    final Control control = m_previewTabItem.getControl();
    control.dispose();

    /* Create the preview composite. */
    final Composite previewComposite = createPreviewComposite( m_previewTabItem.getParent() );

    /* Set the control. */
    m_previewTabItem.setControl( previewComposite );
  }
}