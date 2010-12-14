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
package org.kalypso.util.themes.legend.controls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.kalypso.contribs.eclipse.ui.forms.MessageUtilitites;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.util.themes.legend.LegendUtilities;
import org.kalypso.util.themes.legend.listener.IPropertyChangedListener;
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
   * This listeners are notified, if a property has changed.
   */
  private List<IPropertyChangedListener> m_listener;

  /**
   * The form.
   */
  private Form m_main;

  /**
   * The content, which the form contains.
   */
  private Composite m_content;

  /**
   * All available kalypso themes.
   */
  private List<IKalypsoTheme> m_availableThemes;

  /**
   * The horizontal position.
   */
  protected int m_horizontal;

  /**
   * The vertical position.
   */
  protected int m_vertical;

  protected Color m_backgroundColor;

  protected int m_borderWidth;

  protected IKalypsoTheme[] m_themes;

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
  public LegendComposite( Composite parent, int style, IMapModell mapModell, Properties properties )
  {
    super( parent, style );

    /* Initialize. */
    m_listener = new ArrayList<IPropertyChangedListener>();
    m_main = null;
    m_content = null;
    m_availableThemes = getAvailableThemes( mapModell );
    checkProperties( properties, m_availableThemes );

    /* Create the controls. */
    createControls();
  }

  /**
   * @see org.eclipse.swt.widgets.Composite#setLayout(org.eclipse.swt.widgets.Layout)
   */
  @Override
  public void setLayout( Layout layout )
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
    m_availableThemes = null;
    m_horizontal = PositionUtilities.RIGHT;
    m_vertical = PositionUtilities.BOTTOM;
    m_backgroundColor = null;
    m_borderWidth = 10;
    m_themes = null;

    super.dispose();
  }

  private List<IKalypsoTheme> getAvailableThemes( IMapModell mapModell )
  {
    return Arrays.asList( mapModell.getAllThemes() );
  }

  /**
   * This function checks the provided properties object for properties this composite can edit. Found properties will
   * be checked for correct values. Then they are set to the members. If editable properties are missing or if existing
   * ones have wrong values, they will be set to the members with default values.
   * 
   * @param properties
   *          The properties, containing the default values.
   * @param availableThemes
   *          All available kalypso themes.
   */
  private void checkProperties( Properties properties, List<IKalypsoTheme> availableThemes )
  {
    m_horizontal = PositionUtilities.RIGHT;
    m_vertical = PositionUtilities.BOTTOM;
    m_backgroundColor = new Color( getDisplay(), 255, 255, 255 );
    m_borderWidth = 10;
    m_themes = availableThemes.toArray( new IKalypsoTheme[] {} );

    if( properties == null )
      return;

    String horizontalProperty = properties.getProperty( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION );
    Integer horizontal = NumberUtils.parseQuietInteger( horizontalProperty );
    if( horizontal != null )
      m_horizontal = PositionUtilities.checkHorizontalPosition( horizontal.intValue() );

    String verticalProperty = properties.getProperty( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION );
    Integer vertical = NumberUtils.parseQuietInteger( verticalProperty );
    if( vertical != null )
      m_vertical = PositionUtilities.checkVerticalPosition( vertical.intValue() );

    String backgroundColorProperty = properties.getProperty( LegendUtilities.THEME_PROPERTY_BACKGROUND_COLOR );
    // TODO

    String borderWidthProperty = properties.getProperty( LegendUtilities.THEME_PROPERTY_BORDER_WIDTH );
    Integer borderWitdh = NumberUtils.parseQuietInteger( borderWidthProperty );
    if( borderWitdh != null && borderWitdh.intValue() > 0 )
      m_borderWidth = borderWitdh.intValue();

    String themeIdsProperty = properties.getProperty( LegendUtilities.THEME_PROPERTY_THEME_IDS );
    if( themeIdsProperty != null )
    {
      List<IKalypsoTheme> themes = new ArrayList<IKalypsoTheme>();
      String[] themeIds = StringUtils.split( themeIdsProperty, ";" );
      for( int i = 0; i < themeIds.length; i++ )
      {
        String themeId = themeIds[i];
        for( int j = 0; j < availableThemes.size(); j++ )
        {
          IKalypsoTheme availableTheme = availableThemes.get( j );
          if( themeId.equals( availableTheme.getId() ) )
            themes.add( availableTheme );
        }
      }

      m_themes = themes.toArray( new IKalypsoTheme[] {} );
    }
  }

  /**
   * This function creates the controls.
   */
  private void createControls( )
  {
    /* Create the layout. */
    GridLayout layout = new GridLayout( 1, false );
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    super.setLayout( layout );

    /* The content. */
    Composite content = new Composite( this, SWT.NONE );
    GridLayout contentLayout = new GridLayout( 1, false );
    contentLayout.marginHeight = 0;
    contentLayout.marginWidth = 0;
    content.setLayout( contentLayout );
    content.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create the main form. */
    m_main = new Form( content, SWT.NONE );
    m_main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Get the body of the form. */
    Composite body = m_main.getBody();

    /* Set the properties for the body of the form. */
    GridLayout bodyLayout = new GridLayout( 1, false );
    bodyLayout.marginHeight = 0;
    bodyLayout.marginWidth = 0;
    body.setLayout( bodyLayout );

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
  private Composite createContentComposite( Composite parent )
  {
    /* Create a composite. */
    Composite contentComposite = new Composite( parent, SWT.NONE );
    GridLayout contentLayout = new GridLayout( 1, false );
    contentLayout.marginHeight = 0;
    contentLayout.marginWidth = 0;
    contentComposite.setLayout( contentLayout );

    /* Create the content internal composite. */
    Composite contentInternalComposite = createContentInternalComposite( contentComposite );
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
  private Composite createContentInternalComposite( Composite parent )
  {
    /* Create a composite. */
    Composite contentInternalComposite = new Composite( parent, SWT.NONE );
    contentInternalComposite.setLayout( new GridLayout( 1, false ) );

    /* Create the position composite. */
    Composite positionComposite = createPositionComposite( contentInternalComposite );
    positionComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );

    /* Create the legend group. */
    Group legendGroup = createLegendGroup( contentInternalComposite );
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
  private Composite createPositionComposite( Composite parent )
  {
    /* Create a composite. */
    PositionComposite positionComposite = new PositionComposite( parent, SWT.NONE, m_horizontal, m_vertical );
    positionComposite.addPositionChangedListener( new IPositionChangedListener()
    {
      /**
       * @see org.kalypso.util.themes.position.listener.IPositionChangedListener#positionChanged(int, int)
       */
      @Override
      public void positionChanged( int horizontal, int vertical )
      {
        m_horizontal = horizontal;
        m_vertical = vertical;
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
  private Group createLegendGroup( Composite parent )
  {
    /* Create a group. */
    Group legendGroup = new Group( parent, SWT.NONE );
    legendGroup.setLayout( new GridLayout( 3, false ) );
    legendGroup.setText( "Optionen" );

    /* Create a label. */
    Label backgroundColorLabel = new Label( legendGroup, SWT.NONE );
    backgroundColorLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    backgroundColorLabel.setText( "Hintergrundfarbe" );
    backgroundColorLabel.setAlignment( SWT.LEFT );

    /* Create a text field. */
    final Label backgroundColorText = new Label( legendGroup, SWT.BORDER );
    backgroundColorText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    backgroundColorText.setText( "Hintergrundfarbe" );
    backgroundColorText.setBackground( m_backgroundColor );

    /* Create a button. */
    Button backgroundColorButton = new Button( legendGroup, SWT.PUSH );
    backgroundColorButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    backgroundColorButton.setText( "..." );
    backgroundColorButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        Shell shell = LegendComposite.this.getShell();

        ColorDialog dialog = new ColorDialog( shell );
        dialog.setRGB( m_backgroundColor.getRGB() );
        RGB rgb = dialog.open();
        if( rgb == null )
          return;

        m_backgroundColor.dispose();
        m_backgroundColor = new Color( shell.getDisplay(), rgb );
        backgroundColorText.setBackground( m_backgroundColor );
      }
    } );

    /* Create a label. */
    Label borderWidthLabel = new Label( legendGroup, SWT.NONE );
    borderWidthLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    borderWidthLabel.setText( "Randbreite" );
    borderWidthLabel.setAlignment( SWT.LEFT );

    /* Create a spinner. */
    final Spinner borderWidthSpinner = new Spinner( legendGroup, SWT.BORDER );
    borderWidthSpinner.setValues( m_borderWidth, 1, 25, 0, 1, 5 );
    borderWidthSpinner.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    borderWidthSpinner.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        m_borderWidth = borderWidthSpinner.getSelection();
      }
    } );

    /* Create a label. */
    Label emptyLabel = new Label( legendGroup, SWT.NONE );
    emptyLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    emptyLabel.setText( "" );

    /* Create a label. */
    Label availableThemesLabel = new Label( legendGroup, SWT.NONE );
    availableThemesLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1 ) );
    availableThemesLabel.setText( "Verf¸gbare Themen" );
    availableThemesLabel.setAlignment( SWT.LEFT );

    /* Create a table viewer. */
    TreeViewer availableThemesViewer = new CheckboxTreeViewer( legendGroup, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.VIRTUAL );
    GridData availableThemeData = new GridData( SWT.FILL, SWT.FILL, true, true, 3, 1 );
    availableThemeData.heightHint = 250;
    availableThemesViewer.getTree().setLayoutData( availableThemeData );
    availableThemesViewer.getTree().setLayout( new TableLayout() );
    availableThemesViewer.getTree().setLinesVisible( true );
    availableThemesViewer.getTree().setHeaderVisible( true );

    /* Create a column. */
    TreeViewerColumn nameColumn = new TreeViewerColumn( availableThemesViewer, SWT.LEFT );
    nameColumn.setLabelProvider( new ThemeNameLabelProvider() );
    nameColumn.getColumn().setText( "Name" );
    nameColumn.getColumn().setWidth( 250 );

    /* Create a column. */
    TreeViewerColumn typeColumn = new TreeViewerColumn( availableThemesViewer, SWT.LEFT );
    typeColumn.setLabelProvider( new ThemeTypeLabelProvider() );
    typeColumn.getColumn().setText( "Art" );
    typeColumn.getColumn().setWidth( 200 );

    /* Set a content provider. */
    availableThemesViewer.setContentProvider( new ThemeTableContentProvider() );

    /* Set the input. */
    availableThemesViewer.setInput( m_availableThemes );

    // TODO Checkstate provider...

    // TODO Listener...

    return legendGroup;
  }

  /**
   * This function updates the composite.
   * 
   * @param status
   *          A status, containing a message, which should be displayed in the upper area of the view. May be null.
   */
  protected void update( IStatus status )
  {
    /* Update nothing, when no form or no content is defined. */
    /* In this case the composite was never correct initialized. */
    if( m_main == null || m_content == null )
      return;

    /* Dispose the content of the composite. */
    if( !m_content.isDisposed() )
      m_content.dispose();

    /* Update the message. */
    if( status != null && !status.isOK() )
      m_main.setMessage( status.getMessage(), MessageUtilitites.convertStatusSeverity( status.getSeverity() ) );
    else
      m_main.setMessage( null, IMessageProvider.NONE );

    /* Redraw the content of the composite. */
    m_content = createContentComposite( m_main.getBody() );
    m_content.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Do a reflow. */
    m_main.layout( true, true );
  }

  /**
   * This function fires a property changed event.
   * 
   * @param property
   *          The changed property.
   * @param value
   *          The new value of the changed property.
   */
  protected void firePropertyChanged( String property, String value )
  {
    for( IPropertyChangedListener listener : m_listener )
      listener.propertyChanged( property, value );
  }

  /**
   * This function adds a property changed listener.
   * 
   * @param listener
   *          The property changed listener to add.
   */
  public void addPropertyChangedListener( IPropertyChangedListener listener )
  {
    if( !m_listener.contains( listener ) )
      m_listener.add( listener );
  }

  /**
   * This function removes a property changed listener.
   * 
   * @param listener
   *          The property changed listener to remove.
   */
  public void removePropertyChangedListener( IPropertyChangedListener listener )
  {
    if( m_listener.contains( listener ) )
      m_listener.remove( listener );
  }
}