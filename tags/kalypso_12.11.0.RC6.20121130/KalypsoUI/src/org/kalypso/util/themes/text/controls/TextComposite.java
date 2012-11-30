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
package org.kalypso.util.themes.text.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.kalypso.contribs.eclipse.ui.forms.MessageUtilitites;
import org.kalypso.ogc.gml.ThemeUtilities;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypso.util.themes.position.PositionUtilities;
import org.kalypso.util.themes.position.controls.PositionComposite;
import org.kalypso.util.themes.position.listener.IPositionChangedListener;
import org.kalypso.util.themes.text.TextUtilities;
import org.kalypso.util.themes.text.listener.ITextChangedListener;

/**
 * This composite edits the position.
 * 
 * @author Holger Albert
 */
public class TextComposite extends Composite
{
  /**
   * This listeners are notified, if a text property has changed.
   */
  private List<ITextChangedListener> m_listener;

  /**
   * The form.
   */
  private Form m_main;

  /**
   * The content, which the form contains.
   */
  private Composite m_content;

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
  protected RGB m_backgroundColor;

  /**
   * The text, which should be shown.
   */
  protected String m_text;

  /**
   * The font size.
   */
  protected int m_fontSize;

  /**
   * True, if the transparency is switched on.
   */
  protected boolean m_transparency;

  /**
   * The constructor.
   * 
   * @param parent
   *          A widget which will be the parent of the new instance (cannot be null).
   * @param style
   *          The style of widget to construct.
   * @param properties
   *          The properties, containing the default values.
   */
  public TextComposite( final Composite parent, final int style, final Properties properties )
  {
    super( parent, style );

    /* Initialize. */
    m_listener = new ArrayList<>();
    m_main = null;
    m_content = null;
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
    m_backgroundColor = new RGB( 255, 255, 255 );
    m_text = null;
    m_fontSize = 10;
    m_transparency = false;

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
    final String textProperty = properties.getProperty( TextUtilities.THEME_PROPERTY_TEXT, null );
    final String fontSizeProperty = properties.getProperty( TextUtilities.THEME_PROPERTY_FONT_SIZE );
    final String transparencyProperty = properties.getProperty( TextUtilities.THEME_PROPERTY_TRANSPARENCY );

    /* Check the horizontal position. */
    final int horizontal = PositionUtilities.checkHorizontalPosition( horizontalProperty );
    if( horizontal != -1 )
      m_horizontal = horizontal;

    /* Check the vertical position. */
    final int vertical = PositionUtilities.checkVerticalPosition( verticalProperty );
    if( vertical != -1 )
      m_vertical = vertical;

    /* Check the background color. */
    final RGB backgroundColor = ThemeUtilities.checkBackgroundColor( backgroundColorProperty );
    if( backgroundColor != null )
      m_backgroundColor = backgroundColor;

    /* Check the text. */
    if( textProperty != null && textProperty.length() > 0 )
      m_text = TextUtilities.checkText( textProperty );

    /* Check the font size. */
    final int fontSize = TextUtilities.checkFontSize( fontSizeProperty );
    if( fontSize >= 1 && fontSize <= 35 )
      m_fontSize = fontSize;

    /* Check the transparency. */
    m_transparency = TextUtilities.checkTransparency( transparencyProperty );
  }

  /**
   * This function creates the controls.
   */
  private void createControls( )
  {
    /* Create the layout. */
    final GridLayout layout = new GridLayout( 1, false );
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    super.setLayout( layout );

    /* The content. */
    final Composite content = new Composite( this, SWT.NONE );
    final GridLayout contentLayout = new GridLayout( 1, false );
    contentLayout.marginHeight = 0;
    contentLayout.marginWidth = 0;
    content.setLayout( contentLayout );
    content.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create the main form. */
    m_main = new Form( content, SWT.NONE );
    m_main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Get the body of the form. */
    final Composite body = m_main.getBody();

    /* Set the properties for the body of the form. */
    final GridLayout bodyLayout = new GridLayout( 1, false );
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
  private Composite createContentComposite( final Composite parent )
  {
    /* Create a composite. */
    final Composite contentComposite = new Composite( parent, SWT.NONE );
    final GridLayout contentLayout = new GridLayout( 1, false );
    contentLayout.marginHeight = 0;
    contentLayout.marginWidth = 0;
    contentComposite.setLayout( contentLayout );

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

    /* Create the text group. */
    final Group textGroup = createTextGroup( contentInternalComposite );
    textGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

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
      @Override
      public void positionChanged( final int horizontal, final int vertical )
      {
        m_horizontal = horizontal;
        m_vertical = vertical;

        fireTextPropertyChanged( getProperties(), m_horizontal, m_vertical, m_backgroundColor, m_text, m_fontSize, m_transparency );
      }
    } );

    /* Return the composite. */
    return positionComposite;
  }

  /**
   * This function creates the text group.
   * 
   * @param parent
   *          The parent composite.
   * @return The text group.
   */
  private Group createTextGroup( final Composite parent )
  {
    /* Create a group. */
    final Group textGroup = new Group( parent, SWT.NONE );
    textGroup.setLayout( new GridLayout( 3, false ) );
    textGroup.setText( Messages.getString( "TextComposite_0" ) ); //$NON-NLS-1$

    /* Create a label. */
    final Label backgroundColorLabel = new Label( textGroup, SWT.NONE );
    backgroundColorLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    backgroundColorLabel.setText( Messages.getString( "TextComposite_1" ) ); //$NON-NLS-1$
    backgroundColorLabel.setAlignment( SWT.LEFT );

    /* Create a label. */
    final Label backgroundLabel = new Label( textGroup, SWT.BORDER );
    backgroundLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    backgroundLabel.setText( Messages.getString( "TextComposite_2" ) ); //$NON-NLS-1$
    backgroundLabel.setEnabled( !m_transparency );
    backgroundLabel.setBackground( new Color( parent.getDisplay(), m_backgroundColor ) );

    backgroundLabel.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        backgroundLabel.getBackground().dispose();
      }
    } );

    /* Create a button. */
    final Button backgroundColorButton = new Button( textGroup, SWT.PUSH );
    backgroundColorButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    backgroundColorButton.setText( "..." ); //$NON-NLS-1$
    backgroundColorButton.setEnabled( !m_transparency );
    backgroundColorButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final Shell shell = TextComposite.this.getShell();

        final ColorDialog dialog = new ColorDialog( shell );
        dialog.setRGB( m_backgroundColor );
        final RGB rgb = dialog.open();
        if( rgb == null )
          return;

        m_backgroundColor = rgb;

        backgroundLabel.getBackground().dispose();
        backgroundLabel.setBackground( new Color( shell.getDisplay(), m_backgroundColor ) );

        fireTextPropertyChanged( getProperties(), m_horizontal, m_vertical, m_backgroundColor, m_text, m_fontSize, m_transparency );
      }
    } );

    /* Create a button. */
    final Button transparencyButton = new Button( textGroup, SWT.CHECK );
    transparencyButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1 ) );
    transparencyButton.setText( Messages.getString( "TextComposite_4" ) ); //$NON-NLS-1$
    transparencyButton.setSelection( m_transparency );
    transparencyButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final Button source = (Button)e.getSource();
        backgroundLabel.setEnabled( !source.getSelection() );
        backgroundColorButton.setEnabled( !source.getSelection() );
        m_transparency = source.getSelection();
        fireTextPropertyChanged( getProperties(), m_horizontal, m_vertical, m_backgroundColor, m_text, m_fontSize, m_transparency );
      }
    } );

    /* Create a label. */
    final Label fontSizeLabel = new Label( textGroup, SWT.NONE );
    fontSizeLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    fontSizeLabel.setText( Messages.getString( "TextComposite_5" ) ); //$NON-NLS-1$
    fontSizeLabel.setAlignment( SWT.LEFT );

    /* Create a spinner. */
    final Spinner fontSizeSpinner = new Spinner( textGroup, SWT.BORDER );
    fontSizeSpinner.setValues( m_fontSize, 1, 25, 0, 1, 5 );
    fontSizeSpinner.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
    fontSizeSpinner.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        m_fontSize = fontSizeSpinner.getSelection();
        fireTextPropertyChanged( getProperties(), m_horizontal, m_vertical, m_backgroundColor, m_text, m_fontSize, m_transparency );
      }
    } );

    /* Create a label. */
    final Label textLabel = new Label( textGroup, SWT.NONE );
    textLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    textLabel.setText( Messages.getString( "TextComposite_6" ) ); //$NON-NLS-1$
    textLabel.setAlignment( SWT.LEFT );

    /* Create a text field. */
    final Text textText = new Text( textGroup, SWT.BORDER );
    final GridData textData = new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 );
    textData.widthHint = 250;
    textText.setLayoutData( textData );
    if( m_text != null )
      textText.setText( m_text );
    textText.setMessage( Messages.getString( "TextComposite_7" ) ); //$NON-NLS-1$

    /* Add a listener. */
    textText.addModifyListener( new ModifyListener()
    {
      /**
       * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
       */
      @Override
      public void modifyText( final ModifyEvent e )
      {
        final Text source = (Text)e.getSource();
        m_text = source.getText();

        fireTextPropertyChanged( getProperties(), m_horizontal, m_vertical, m_backgroundColor, m_text, m_fontSize, m_transparency );
      }
    } );

    return textGroup;
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
   * This function fires a text property changed event.
   * 
   * @param properties
   *          A up to date properties object, containing all serialized text properties.
   * @param horizontal
   *          The horizontal position.
   * @param vertical
   *          The vertical position.
   * @param text
   *          The text, which should be shown.
   * @param fontSize
   *          The font size.
   * @param transparency
   *          True, if the transparency is switched on.
   */
  protected void fireTextPropertyChanged( final Properties properties, final int horizontal, final int vertical, final RGB background, final String text, final int fontSize, final boolean transparency )
  {
    for( final ITextChangedListener listener : m_listener )
      listener.textPropertyChanged( properties, horizontal, vertical, background, text, fontSize, transparency );
  }

  /**
   * This function adds a text changed listener.
   * 
   * @param listener
   *          The text changed listener to add.
   */
  public void addTextChangedListener( final ITextChangedListener listener )
  {
    if( !m_listener.contains( listener ) )
      m_listener.add( listener );
  }

  /**
   * This function removes a text changed listener.
   * 
   * @param listener
   *          The text changed listener to remove.
   */
  public void removeTextChangedListener( final ITextChangedListener listener )
  {
    if( m_listener.contains( listener ) )
      m_listener.remove( listener );
  }

  /**
   * This function returns a up to date properties object, containing all serialized text properties.
   * 
   * @return A up to date properties object, containing all serialized text properties.
   */
  public Properties getProperties( )
  {
    /* Create the properties object. */
    final Properties properties = new Properties();

    /* Serialize the properties. */
    final String horizontalProperty = String.format( Locale.PRC, "%d", m_horizontal ); //$NON-NLS-1$
    final String verticalProperty = String.format( Locale.PRC, "%d", m_vertical ); //$NON-NLS-1$
    final String backgroundColorProperty = String.format( Locale.PRC, "%d;%d;%d", m_backgroundColor.red, m_backgroundColor.green, m_backgroundColor.blue ); //$NON-NLS-1$
    String textProperty = null;
    if( m_text != null )
      textProperty = String.format( Locale.PRC, "%s", m_text ); //$NON-NLS-1$
    final String fontSizeProperty = String.format( Locale.PRC, "%d", m_fontSize ); //$NON-NLS-1$
    final String transparencyProperty = Boolean.toString( m_transparency );

    /* Add the properties. */
    properties.put( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, horizontalProperty );
    properties.put( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, verticalProperty );
    properties.put( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR, backgroundColorProperty );
    properties.put( TextUtilities.THEME_PROPERTY_TEXT, "" ); //$NON-NLS-1$
    if( textProperty != null )
      properties.put( TextUtilities.THEME_PROPERTY_TEXT, textProperty );
    properties.put( TextUtilities.THEME_PROPERTY_FONT_SIZE, fontSizeProperty );
    properties.put( TextUtilities.THEME_PROPERTY_TRANSPARENCY, transparencyProperty );

    return properties;
  }
}