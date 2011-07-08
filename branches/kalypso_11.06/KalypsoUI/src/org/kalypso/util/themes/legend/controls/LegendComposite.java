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
package org.kalypso.util.themes.legend.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.widgets.Form;
import org.kalypso.contribs.eclipse.swt.layout.Layouts;
import org.kalypso.contribs.eclipse.ui.forms.MessageUtilitites;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.util.themes.ThemeUtilities;
import org.kalypso.util.themes.legend.LegendUtilities;
import org.kalypso.util.themes.legend.listener.ILegendChangedListener;
import org.kalypso.util.themes.legend.provider.ThemeCheckStateProvider;
import org.kalypso.util.themes.legend.provider.ThemeNameLabelProvider;
import org.kalypso.util.themes.legend.provider.ThemeTableContentProvider;
import org.kalypso.util.themes.legend.provider.ThemeTypeLabelProvider;
import org.kalypso.util.themes.position.PositionUtilities;
import org.kalypso.util.themes.position.controls.PositionComposite;
import org.kalypso.util.themes.position.listener.IPositionChangedListener;

/**
 * This composite edits the position.
 * 
 * @author Holger Albert
 */
public class LegendComposite extends Composite
{
  /**
   * This listeners are notified, if a legend property has changed.
   */
  private List<ILegendChangedListener> m_listener;

  /**
   * The form.
   */
  private Form m_main;

  /**
   * The content, which the form contains.
   */
  private Composite m_content;

  /**
   * The map modell.
   */
  private final IMapModell m_mapModell;

  /**
   * The horizontal position.
   */
  protected int m_horizontal;

  /**
   * The vertical position.
   */
  protected int m_vertical;

  /**
   * The background color.
   */
  protected Color m_backgroundColor;

  /**
   * The insets.
   */
  protected int m_insets;

  /**
   * The ids of the selected themes.
   */
  protected List<String> m_themeIds;

  /**
   * The constructor.
   * 
   * @param parent
   *          A widget which will be the parent of the new instance (cannot be null).
   * @param style
   *          The style of widget to construct.
   * @param mapModell
   *          The map modell.
   * @param properties
   *          The properties, containing the default values.
   */
  public LegendComposite( final Composite parent, final int style, final IMapModell mapModell, final Properties properties )
  {
    super( parent, style );

    /* Initialize. */
    m_listener = new ArrayList<ILegendChangedListener>();
    m_main = null;
    m_content = null;
    m_mapModell = mapModell;
    checkProperties( properties );

    /* Create the controls. */
    createControls();
  }

  /**
   * @see org.eclipse.swt.widgets.Composite#setLayout(org.eclipse.swt.widgets.Layout)
   */
  @Override
  public void setLayout( final Layout layout )
  {
    /* Ignore user set layouts, only layout datas are permitted. */
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_listener != null )
      m_listener.clear();

    m_listener = null;
    m_main = null;
    m_content = null;
    m_horizontal = PositionUtilities.RIGHT;
    m_vertical = PositionUtilities.BOTTOM;
    m_backgroundColor = null;
    m_insets = 10;
    m_themeIds = null;

    super.dispose();
  }

  /**
   * This function checks the provided properties object for properties this composite can edit. Found properties will
   * be checked for correct values. Then they are set to the members. If editable properties are missing or if existing
   * ones have wrong values, they will be set to the members with default values.
   * 
   * @param properties
   *          The properties, containing the values.
   */
  private void checkProperties( final Properties properties )
  {
    /* Default values. */
    m_horizontal = PositionUtilities.RIGHT;
    m_vertical = PositionUtilities.BOTTOM;
    m_backgroundColor = new Color( getDisplay(), 255, 255, 255 );
    m_insets = 10;
    m_themeIds = new ArrayList<String>();

    /* Do not change the default values, if no new properties are set. */
    if( properties == null )
      return;

    /* Update the default values, with the one of the given properties. */
    updateProperties( properties );
  }

  /**
   * This function checks the provided properties object for properties this composite can edit. Found properties will
   * be checked for correct values. Then they are set to the members. If editable properties are missing or if existing
   * ones have wrong values, the members will not be changed.
   * 
   * @param properties
   *          The properties, containing the values.
   */
  private void updateProperties( final Properties properties )
  {
    /* Get the properties. */
    final String horizontalProperty = properties.getProperty( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION );
    final String verticalProperty = properties.getProperty( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION );
    final String backgroundColorProperty = properties.getProperty( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR );
    final String insetsProperty = properties.getProperty( LegendUtilities.THEME_PROPERTY_INSETS );
    final String themeIdsProperty = properties.getProperty( LegendUtilities.THEME_PROPERTY_THEME_IDS );

    /* Check the horizontal position. */
    final int horizontal = PositionUtilities.checkHorizontalPosition( horizontalProperty );
    if( horizontal != -1 )
      m_horizontal = horizontal;

    /* Check the vertical position. */
    final int vertical = PositionUtilities.checkVerticalPosition( verticalProperty );
    if( vertical != -1 )
      m_vertical = vertical;

    /* Check the background color. */
    final Color backgroundColor = ThemeUtilities.checkBackgroundColor( getDisplay(), backgroundColorProperty );
    if( backgroundColor != null )
      m_backgroundColor = backgroundColor;

    /* Check the insets. */
    final int insets = LegendUtilities.checkInsets( insetsProperty );
    if( insets >= 1 && insets <= 25 )
      m_insets = insets;

    /* Check the theme ids. */
    final List<String> themeIds = LegendUtilities.verifyThemeIds( m_mapModell, themeIdsProperty );
    if( themeIds != null && themeIds.size() > 0 )
      m_themeIds = themeIds;
  }

  /**
   * This function creates the controls.
   */
  private void createControls( )
  {
    /* Create the layout. */
    super.setLayout( Layouts.createGridLayout() );

    /* The content. */
    final Composite content = new Composite( this, SWT.NONE );
    content.setLayout( Layouts.createGridLayout() );
    content.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create the main form. */
    m_main = new Form( content, SWT.NONE );
    m_main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Get the body of the form. */
    final Composite body = m_main.getBody();

    /* Set the properties for the body of the form. */
    body.setLayout( Layouts.createGridLayout() );

    /* Create the content. */
    m_content = createContentComposite( body );
    m_content.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Do a reflow. */
    m_main.layout( true, true );
  }

  /**
   * This function creates the content composite.
   * 
   * @param parent
   *          The parent composite.
   * @return The content composite.
   */
  private Composite createContentComposite( final Composite parent )
  {
    /* Create a composite. */
    final Composite contentComposite = new Composite( parent, SWT.NONE );
    contentComposite.setLayout( Layouts.createGridLayout() );

    /* Create the content internal composite. */
    final Composite contentInternalComposite = createContentInternalComposite( contentComposite );
    contentInternalComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    return contentComposite;
  }

  /**
   * This function creates the content internal composite.
   * 
   * @param parent
   *          The parent composite.
   * @return The content internal composite.
   */
  private Composite createContentInternalComposite( final Composite parent )
  {
    /* Create a composite. */
    final Composite contentInternalComposite = new Composite( parent, SWT.NONE );
    contentInternalComposite.setLayout( new GridLayout( 1, false ) );

    /* Create the position composite. */
    final Composite positionComposite = createPositionComposite( contentInternalComposite );
    positionComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );

    /* Create the legend group. */
    final Group legendGroup = createLegendGroup( contentInternalComposite );
    legendGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    return contentInternalComposite;
  }

  /**
   * This function creates the position composite.
   * 
   * @param parent
   *          The parent composite.
   * @return The position composite.
   */
  private Composite createPositionComposite( final Composite parent )
  {
    /* Create a composite. */
    final PositionComposite positionComposite = new PositionComposite( parent, SWT.NONE, m_horizontal, m_vertical );
    positionComposite.addPositionChangedListener( new IPositionChangedListener()
    {
      /**
       * @see org.kalypso.util.themes.position.listener.IPositionChangedListener#positionChanged(int, int)
       */
      @Override
      public void positionChanged( final int horizontal, final int vertical )
      {
        m_horizontal = horizontal;
        m_vertical = vertical;

        fireLegendPropertyChanged( getProperties(), m_horizontal, m_vertical, m_backgroundColor, m_insets, m_themeIds.toArray( new String[] {} ) );
      }
    } );

    /* Return the composite. */
    return positionComposite;
  }

  /**
   * This function creates the legend group.
   * 
   * @param parent
   *          The parent composite.
   * @return The legend group.
   */
  private Group createLegendGroup( final Composite parent )
  {
    /* Create a group. */
    final Group legendGroup = new Group( parent, SWT.NONE );
    legendGroup.setLayout( new GridLayout( 3, false ) );
    legendGroup.setText( "Optionen" );

    /* Create a label. */
    final Label backgroundColorLabel = new Label( legendGroup, SWT.NONE );
    backgroundColorLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    backgroundColorLabel.setText( "Hintergrundfarbe" );
    backgroundColorLabel.setAlignment( SWT.LEFT );

    /* Create a label. */
    final Label backgroundLabel = new Label( legendGroup, SWT.BORDER );
    backgroundLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    backgroundLabel.setText( "Hintergrundfarbe" );
    backgroundLabel.setBackground( m_backgroundColor );

    /* Create a button. */
    final Button backgroundColorButton = new Button( legendGroup, SWT.PUSH );
    backgroundColorButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    backgroundColorButton.setText( "..." );
    backgroundColorButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final Shell shell = LegendComposite.this.getShell();

        final ColorDialog dialog = new ColorDialog( shell );
        dialog.setRGB( m_backgroundColor.getRGB() );
        final RGB rgb = dialog.open();
        if( rgb == null )
          return;

        m_backgroundColor.dispose();
        m_backgroundColor = new Color( shell.getDisplay(), rgb );
        backgroundLabel.setBackground( m_backgroundColor );

        fireLegendPropertyChanged( getProperties(), m_horizontal, m_vertical, m_backgroundColor, m_insets, m_themeIds.toArray( new String[] {} ) );
      }
    } );

    /* Create a label. */
    final Label insetsLabel = new Label( legendGroup, SWT.NONE );
    insetsLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    insetsLabel.setText( "Zwischenraum" );
    insetsLabel.setAlignment( SWT.LEFT );

    /* Create a spinner. */
    final Spinner insetsSpinner = new Spinner( legendGroup, SWT.BORDER );
    insetsSpinner.setValues( m_insets, 1, 25, 0, 1, 5 );
    insetsSpinner.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    insetsSpinner.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        m_insets = insetsSpinner.getSelection();
        fireLegendPropertyChanged( getProperties(), m_horizontal, m_vertical, m_backgroundColor, m_insets, m_themeIds.toArray( new String[] {} ) );
      }
    } );

    /* Create a label. */
    final Label emptyLabel = new Label( legendGroup, SWT.NONE );
    emptyLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    emptyLabel.setText( "" );

    /* Create a label. */
    final Label availableThemesLabel = new Label( legendGroup, SWT.NONE );
    availableThemesLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1 ) );
    availableThemesLabel.setText( "Verfügbare Themen" );
    availableThemesLabel.setAlignment( SWT.LEFT );

    /* Create a table viewer. */
    final CheckboxTreeViewer availableThemesViewer = new CheckboxTreeViewer( legendGroup, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.VIRTUAL );
    final GridData availableThemeData = new GridData( SWT.FILL, SWT.FILL, true, true, 3, 1 );
    availableThemeData.heightHint = 250;
    availableThemesViewer.getTree().setLayoutData( availableThemeData );
    availableThemesViewer.getTree().setLayout( new TableLayout() );
    availableThemesViewer.getTree().setLinesVisible( true );
    availableThemesViewer.getTree().setHeaderVisible( true );

    /* Create a column. */
    final TreeViewerColumn nameColumn = new TreeViewerColumn( availableThemesViewer, SWT.LEFT );
    nameColumn.setLabelProvider( new ThemeNameLabelProvider() );
    nameColumn.getColumn().setText( "Name" );
    nameColumn.getColumn().setWidth( 250 );

    /* Create a column. */
    final TreeViewerColumn typeColumn = new TreeViewerColumn( availableThemesViewer, SWT.LEFT );
    typeColumn.setLabelProvider( new ThemeTypeLabelProvider() );
    typeColumn.getColumn().setText( "Art" );
    typeColumn.getColumn().setWidth( 200 );

    /* Set a content provider. */
    availableThemesViewer.setContentProvider( new ThemeTableContentProvider() );

    /* Set the check state provider. */
    availableThemesViewer.setCheckStateProvider( new ThemeCheckStateProvider( m_themeIds ) );

    /* Set the input. */
    availableThemesViewer.setInput( m_mapModell );

    /* Add a listener. */
    availableThemesViewer.addCheckStateListener( new ICheckStateListener()
    {
      /**
       * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
       */
      @Override
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        final IKalypsoTheme element = (IKalypsoTheme) event.getElement();

        final String id = element.getId();
        if( event.getChecked() && !m_themeIds.contains( id ) )
          m_themeIds.add( id );
        else
          m_themeIds.remove( id );

        fireLegendPropertyChanged( getProperties(), m_horizontal, m_vertical, m_backgroundColor, m_insets, m_themeIds.toArray( new String[] {} ) );
      }
    } );

    return legendGroup;
  }

  /**
   * This function updates the composite.
   * 
   * @param status
   *          A status, containing a message, which should be displayed in the upper area of the view. May be null.
   */
  protected void update( final IStatus status )
  {
    /* Update nothing, when no form or no content is defined. */
    /* In this case the composite was never correct initialized. */
    if( m_main == null || m_content == null )
      return;

    /* Update the message. */
    if( status != null && !status.isOK() )
      m_main.setMessage( status.getMessage(), MessageUtilitites.convertStatusSeverity( status.getSeverity() ) );
    else
      m_main.setMessage( null, IMessageProvider.NONE );

    /* Dispose the content of the composite. */
    if( !m_content.isDisposed() )
      m_content.dispose();

    /* Redraw the content of the composite. */
    m_content = createContentComposite( m_main.getBody() );
    m_content.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Do a reflow. */
    m_main.layout( true, true );
  }

  /**
   * This function fires a legend property changed event.
   * 
   * @param properties
   *          A up to date properties object, containing all serialized legend properties.
   * @param horizontal
   *          The horizontal position.
   * @param vertical
   *          The vertical position.
   * @param backgroundColor
   *          The background color.
   * @param insets
   *          The insets.
   * @param themes
   *          The selected themes.
   */
  protected void fireLegendPropertyChanged( final Properties properties, final int horizontal, final int vertical, final Color backgroundColor, final int insets, final String[] themeIds )
  {
    for( final ILegendChangedListener listener : m_listener )
      listener.legendPropertyChanged( properties, horizontal, vertical, backgroundColor, insets, themeIds );
  }

  /**
   * This function adds a legend changed listener.
   * 
   * @param listener
   *          The legend changed listener to add.
   */
  public void addLegendChangedListener( final ILegendChangedListener listener )
  {
    if( !m_listener.contains( listener ) )
      m_listener.add( listener );
  }

  /**
   * This function removes a legend changed listener.
   * 
   * @param listener
   *          The legend changed listener to remove.
   */
  public void removeLegendChangedListener( final ILegendChangedListener listener )
  {
    if( m_listener.contains( listener ) )
      m_listener.remove( listener );
  }

  /**
   * This function returns a up to date properties object, containing all serialized legend properties.
   * 
   * @return A up to date properties object, containing all serialized legend properties.
   */
  public Properties getProperties( )
  {
    /* Create the properties object. */
    final Properties properties = new Properties();

    /* Serialize the properties. */
    final String horizontalProperty = String.format( Locale.PRC, "%d", m_horizontal );
    final String verticalProperty = String.format( Locale.PRC, "%d", m_vertical );
    final String backgroundColorProperty = String.format( Locale.PRC, "%d;%d;%d", m_backgroundColor.getRed(), m_backgroundColor.getGreen(), m_backgroundColor.getBlue() );
    final String insetsProperty = String.format( Locale.PRC, "%d", m_insets );
    final List<String> themeIds = new ArrayList<String>();
    for( int i = 0; i < m_themeIds.size(); i++ )
    {
      final String id = m_themeIds.get( i );
      themeIds.add( id );
    }
    final String themeIdsProperty = StringUtils.join( themeIds, ";" );

    /* Add the properties. */
    properties.put( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, horizontalProperty );
    properties.put( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, verticalProperty );
    properties.put( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR, backgroundColorProperty );
    properties.put( LegendUtilities.THEME_PROPERTY_INSETS, insetsProperty );
    properties.put( LegendUtilities.THEME_PROPERTY_THEME_IDS, themeIdsProperty );

    return properties;
  }
}