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
package org.kalypso.util.themes.image.controls;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.kalypso.contribs.eclipse.ui.forms.MessageUtilitites;
import org.kalypso.ogc.gml.ThemeUtilities;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypso.util.themes.image.ImageUtilities;
import org.kalypso.util.themes.image.listener.IImageChangedListener;
import org.kalypso.util.themes.position.PositionUtilities;
import org.kalypso.util.themes.position.controls.PositionComposite;
import org.kalypso.util.themes.position.listener.IPositionChangedListener;

/**
 * This composite edits the position.
 * 
 * @author Holger Albert
 */
public class ImageComposite extends Composite
{
  /**
   * This listeners are notified, if a image property has changed.
   */
  private List<IImageChangedListener> m_listener;

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
  protected RGB m_background;

  /**
   * The URL of the image, which should be shown.
   */
  protected String m_imageUrl;

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
  public ImageComposite( final Composite parent, final int style, final Properties properties )
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
   * This function creates the controls.
   */
  private void createControls( )
  {
    /* Create the layout. */
    super.setLayout( GridLayoutFactory.fillDefaults().create() );

    /* The content. */
    final Composite content = new Composite( this, SWT.NONE );
    GridLayoutFactory.fillDefaults().applyTo( content );
    content.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create the main form. */
    m_main = new Form( content, SWT.NONE );
    m_main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Get the body of the form. */
    final Composite body = m_main.getBody();

    /* Set the properties for the body of the form. */
    GridLayoutFactory.fillDefaults().applyTo( body );

    /* Create the content. */
    m_content = createContentComposite( body );
    m_content.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Do a reflow. */
    m_main.layout( true, true );
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
    m_background = new RGB( 255, 255, 255 );
    m_imageUrl = null;

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
    final String imageUrlProperty = properties.getProperty( ImageUtilities.THEME_PROPERTY_IMAGE_URL, null );

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
      m_background = backgroundColor;

    /* Check the URL of the image. */
    if( imageUrlProperty != null && imageUrlProperty.length() > 0 )
      m_imageUrl = ImageUtilities.checkImageUrl( imageUrlProperty );
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
    GridLayoutFactory.fillDefaults().applyTo( contentComposite );

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

    /* Create the image group. */
    final Group imageGroup = createImageGroup( contentInternalComposite );
    imageGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

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

        fireImagePropertyChanged( getProperties(), m_horizontal, m_vertical, m_background, m_imageUrl );
      }
    } );

    /* Return the composite. */
    return positionComposite;
  }

  /**
   * This function creates the image group.
   * 
   * @param parent
   *          The parent composite.
   * @return The image group.
   */
  private Group createImageGroup( final Composite parent )
  {
    /* Create a group. */
    final Group imageGroup = new Group( parent, SWT.NONE );
    imageGroup.setLayout( new GridLayout( 3, false ) );
    imageGroup.setText( Messages.getString( "ImageComposite_0" ) ); //$NON-NLS-1$

    /* Create a label. */
    final Label backgroundColorLabel = new Label( imageGroup, SWT.NONE );
    backgroundColorLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    backgroundColorLabel.setText( Messages.getString( "ImageComposite_1" ) ); //$NON-NLS-1$
    backgroundColorLabel.setAlignment( SWT.LEFT );

    /* Create a label. */
    final Label backgroundLabel = new Label( imageGroup, SWT.BORDER );
    backgroundLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    backgroundLabel.setText( Messages.getString( "ImageComposite_2" ) ); //$NON-NLS-1$
    backgroundLabel.setBackground( new Color( parent.getDisplay(), m_background ) );

    backgroundLabel.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        backgroundLabel.getBackground().dispose();
      }
    } );

    /* Create a button. */
    final Button backgroundColorButton = new Button( imageGroup, SWT.PUSH );
    backgroundColorButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    backgroundColorButton.setText( "..." ); //$NON-NLS-1$
    backgroundColorButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final Shell shell = ImageComposite.this.getShell();

        final ColorDialog dialog = new ColorDialog( shell );
        dialog.setRGB( m_background );
        final RGB rgb = dialog.open();
        if( rgb == null )
          return;

        m_background = rgb;

        backgroundLabel.getBackground().dispose();
        backgroundLabel.setBackground( new Color( parent.getDisplay(), m_background ) );

        fireImagePropertyChanged( getProperties(), m_horizontal, m_vertical, m_background, m_imageUrl );
      }
    } );

    /* Create a label. */
    final Label imageUrlLabel = new Label( imageGroup, SWT.NONE );
    imageUrlLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    imageUrlLabel.setText( Messages.getString( "ImageComposite_4" ) ); //$NON-NLS-1$
    imageUrlLabel.setAlignment( SWT.LEFT );

    /* Create a text field. */
    final Text imageUrlText = new Text( imageGroup, SWT.BORDER );
    final GridData imageUrlData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    imageUrlData.widthHint = 250;
    imageUrlText.setLayoutData( imageUrlData );
    if( m_imageUrl != null )
      imageUrlText.setText( m_imageUrl );

    /* Add a listener. */
    imageUrlText.addModifyListener( new ModifyListener()
    {
      /**
       * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
       */
      @Override
      public void modifyText( final ModifyEvent e )
      {
        /* Reset the URL of the image. */
        m_imageUrl = null;

        /* Get the source. */
        final Text source = (Text)e.getSource();

        /* Get the text. */
        final String text = source.getText();

        /* Create the URL of the image. */
        if( text != null && text.length() > 0 )
          m_imageUrl = text;

        /* Fire the image property changed event. */
        fireImagePropertyChanged( getProperties(), m_horizontal, m_vertical, m_background, m_imageUrl );
      }
    } );

    /* Create a button. */
    final Button imageUrlButton = new Button( imageGroup, SWT.PUSH );
    imageUrlButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    imageUrlButton.setText( Messages.getString( "ImageComposite_5" ) ); //$NON-NLS-1$

    /* Add a listener. */
    imageUrlButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        try
        {
          /* Create a file dialog. */
          final FileDialog dialog = new FileDialog( ImageComposite.this.getShell(), SWT.OPEN );
          dialog.setText( Messages.getString( "ImageComposite_6" ) ); //$NON-NLS-1$
          dialog.setFilterExtensions( new String[] { "*.bmp", "*.png", "*.gif", "*.jpg", "*.tif" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
          dialog.setFilterNames( new String[] {
              Messages.getString( "ImageComposite_12" ), Messages.getString( "ImageComposite_13" ), Messages.getString( "ImageComposite_14" ), Messages.getString( "ImageComposite_15" ), Messages.getString( "ImageComposite_16" ) } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

          /* Open the dialog. */
          final String file = dialog.open();
          if( file == null )
            return;

          /* Set the URL of the image. */
          final URL[] urls = FileUtils.toURLs( new File[] { new File( file ) } );
          imageUrlText.setText( urls[0].toExternalForm() );
        }
        catch( final IOException ex )
        {
          /* Print the stack trace. */
          ex.printStackTrace();
        }
      }
    } );

    return imageGroup;
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
   * This function fires a image property changed event.
   * 
   * @param properties
   *          A up to date properties object, containing all serialized image properties.
   * @param horizontal
   *          The horizontal position.
   * @param vertical
   *          The vertical position.
   * @param imageUrl
   *          The URL of the image, which should be shown.
   */
  protected void fireImagePropertyChanged( final Properties properties, final int horizontal, final int vertical, final RGB backgroundColor, final String imageUrl )
  {
    for( final IImageChangedListener listener : m_listener )
      listener.imagePropertyChanged( properties, horizontal, vertical, backgroundColor, imageUrl );
  }

  /**
   * This function adds a image changed listener.
   * 
   * @param listener
   *          The image changed listener to add.
   */
  public void addImageChangedListener( final IImageChangedListener listener )
  {
    if( !m_listener.contains( listener ) )
      m_listener.add( listener );
  }

  /**
   * This function removes a image changed listener.
   * 
   * @param listener
   *          The image changed listener to remove.
   */
  public void removeImageChangedListener( final IImageChangedListener listener )
  {
    if( m_listener.contains( listener ) )
      m_listener.remove( listener );
  }

  /**
   * This function returns a up to date properties object, containing all serialized image properties.
   * 
   * @return A up to date properties object, containing all serialized image properties.
   */
  public Properties getProperties( )
  {
    /* Create the properties object. */
    final Properties properties = new Properties();

    /* Serialize the properties. */
    final String horizontalProperty = String.format( Locale.PRC, "%d", m_horizontal ); //$NON-NLS-1$
    final String verticalProperty = String.format( Locale.PRC, "%d", m_vertical ); //$NON-NLS-1$
    // FIXME: put into ThemeUtilities; its always the same in every theme ;-(
    final String backgroundColorProperty = String.format( Locale.PRC, "%d;%d;%d", m_background.red, m_background.green, m_background.blue ); //$NON-NLS-1$
    String imageUrlProperty = null;
    if( m_imageUrl != null )
      imageUrlProperty = String.format( Locale.PRC, "%s", m_imageUrl ); //$NON-NLS-1$

    /* Add the properties. */
    properties.put( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, horizontalProperty );
    properties.put( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, verticalProperty );
    properties.put( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR, backgroundColorProperty );
    properties.put( ImageUtilities.THEME_PROPERTY_IMAGE_URL, "" ); //$NON-NLS-1$
    if( imageUrlProperty != null )
      properties.put( ImageUtilities.THEME_PROPERTY_IMAGE_URL, imageUrlProperty );

    return properties;
  }
}