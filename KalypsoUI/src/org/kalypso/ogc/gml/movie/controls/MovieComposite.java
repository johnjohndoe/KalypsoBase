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
package org.kalypso.ogc.gml.movie.controls;

import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;

import javax.media.jai.widget.ImageCanvas;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.forms.widgets.Form;
import org.kalypso.contribs.eclipse.ui.forms.MessageUtilitites;
import org.kalypso.ogc.gml.AbstractCascadingLayerTheme;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.movie.IMovieControls;
import org.kalypso.ogc.gml.movie.IMovieImageProvider;
import org.kalypso.ogc.gml.movie.utils.IMovieFrame;
import org.kalypso.ogc.gml.movie.utils.MoviePlayer;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * The movie composite.
 * 
 * @author Holger Albert
 */
@SuppressWarnings("deprecation")
public class MovieComposite extends Composite
{
  /**
   * The movie player.
   */
  private MoviePlayer m_player;

  /**
   * The form.
   */
  private Form m_form;

  /**
   * The content, which the form contains.
   */
  private Composite m_content;

  /**
   * The image canvas.
   */
  private ImageCanvas m_imageCanvas;

  /**
   * The progress bar.
   */
  private ProgressBar m_progressBar;

  /**
   * The width.
   */
  private int m_width;

  /**
   * The height.
   */
  private int m_height;

  /**
   * The constructor.
   * 
   * @param parent
   *          A widget which will be the parent of the new instance (cannot be null).
   * @param style
   *          The style of widget to construct.
   * @param mapModel
   *          The gis template map model.
   * @param movieTheme
   *          The theme, marked as movie theme.
   * @param boundingBox
   *          The bounding box.
   */
  public MovieComposite( Composite parent, int style, GisTemplateMapModell mapModel, AbstractCascadingLayerTheme movieTheme, GM_Envelope boundingBox )
  {
    super( parent, style );

    /* Initialize with parameters. */
    m_player = new MoviePlayer( this, mapModel, movieTheme, boundingBox );

    /* Initialize. */
    m_form = null;
    m_content = null;
    m_imageCanvas = null;
    m_progressBar = null;
    m_width = 640;
    m_height = 480;

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
    /* Discard the references. */
    m_form = null;
    m_content = null;
    m_imageCanvas = null;
    m_progressBar = null;

    super.dispose();
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

    /* A form. */
    m_form = new Form( content, SWT.NONE );
    m_form.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    m_form.setMessage( "Daten werden abgerufen...", IMessageProvider.INFORMATION );

    /* Get the body of the form. */
    Composite body = m_form.getBody();

    /* Set the properties for the body of the form. */
    GridLayout bodyLayout = new GridLayout( 1, false );
    bodyLayout.marginHeight = 0;
    bodyLayout.marginWidth = 0;
    body.setLayout( bodyLayout );

    /* Create the content. */
    m_content = createContentComposite( body );
    m_content.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Do a reflow. */
    m_form.layout( true, true );
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
    contentInternalComposite.setLayout( new GridLayout( 2, false ) );

    /* Create the default screen. */
    Composite screenComposite = createScreenControls( contentInternalComposite );
    screenComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    return contentInternalComposite;
  }

  /**
   * This function creates the screen controls.
   * 
   * @param parent
   *          The parent composite.
   * @return The screen controls.
   */
  private Composite createScreenControls( Composite parent )
  {
    /* Create a composite. */
    Composite composite = new Composite( parent, SWT.NONE );
    GridLayout layout = new GridLayout( 1, false );
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    composite.setLayout( layout );

    /* Create the image composite. */
    Composite imageComposite = new Composite( composite, SWT.EMBEDDED | SWT.NO_BACKGROUND );
    imageComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create the start image. */
    BufferedImage startImage = createEmptyImage( m_width, m_height );

    /* Create the image canvas. */
    m_imageCanvas = new ImageCanvas( startImage );
    Frame virtualFrame = SWT_AWT.new_Frame( imageComposite );
    virtualFrame.add( m_imageCanvas );

    /* Create a progress bar. */
    m_progressBar = new ProgressBar( composite, SWT.NONE );
    m_progressBar.setMinimum( 0 );
    m_progressBar.setMaximum( m_player.getEndStep() );
    m_progressBar.setSelection( m_player.getCurrentStep() );
    m_progressBar.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );

    return composite;
  }

  /**
   * This function creates an empty image.
   * 
   * @param width
   *          The width of the image.
   * @param height
   *          The height of the image.
   * @return The empty image.
   */
  private BufferedImage createEmptyImage( int width, int height )
  {
    /* Create an empty image. */
    byte[] byteArray = new byte[] { -1, 0 };
    ColorModel colorModel = new IndexColorModel( 1, 2, byteArray, byteArray, byteArray );
    WritableRaster writeableRaster = Raster.createPackedRaster( DataBuffer.TYPE_BYTE, width, height, 1, 1, null );
    BufferedImage bufferedImage = new BufferedImage( colorModel, writeableRaster, false, null );

    return bufferedImage;
  }

  /**
   * This function updates the image canvas.
   * 
   * @param width
   *          The width.
   * @param height
   *          The height.
   */
  private void updateImageCanvas( int width, int height )
  {
    /* Is the control disposed? */
    if( m_imageCanvas == null )
      return;

    /* Get the current frame. */
    IMovieFrame currentFrame = m_player.getCurrentFrame();
    if( currentFrame == null )
    {
      /* Create the start image. */
      BufferedImage startImage = createEmptyImage( width, height );

      /* Set the start image. */
      m_imageCanvas.set( startImage );

      return;
    }

    /* Get the current image. */
    RenderedImage currentImage = currentFrame.getImage( width, height );

    /* Set the current image. */
    m_imageCanvas.set( currentImage );
  }

  /**
   * This function updates the progress bar.
   */
  private void updateProgressBar( )
  {
    /* Is the control disposed? */
    if( m_progressBar == null || m_progressBar.isDisposed() )
      return;

    /* Get the current step. */
    int currentStep = m_player.getCurrentStep();

    /* Set the current step. */
    m_progressBar.setSelection( currentStep );
  }

  /**
   * This function creates the button controls.
   * 
   * @param parent
   *          The parent composite.
   * @return The button controls.
   */
  public Composite createButtonControls( Composite parent )
  {
    /* Get the image provider. */
    IMovieImageProvider imageProvider = m_player.getImageProvider();

    /* Get the movie controls. */
    IMovieControls movieControls = imageProvider.getMovieControls();

    /* Get the actions. */
    Action[] actions = movieControls.getActions( m_player );

    /* Create a composite. */
    Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayout( new GridLayout( actions.length, false ) );

    /* Create the buttons. */
    for( int i = 0; i < actions.length; i++ )
    {
      /* Get the action. */
      final Action action = actions[i];

      /* Create the image. */
      final Image image = action.getImageDescriptor().createImage();

      /* Create a button for the action. */
      Button actionButton = new Button( composite, SWT.PUSH );
      actionButton.setImage( image );
      actionButton.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );
      actionButton.setText( action.getText() );
      actionButton.setToolTipText( action.getText() );
      actionButton.addSelectionListener( new SelectionAdapter()
      {
        /**
         * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
        @Override
        public void widgetSelected( SelectionEvent e )
        {
          /* Get the source. */
          Button source = (Button) e.getSource();

          /* Build the event. */
          Event event = new Event();
          event.display = source.getDisplay();
          event.item = source;
          event.doit = true;

          /* Execute the action. */
          action.runWithEvent( event );
        }
      } );

      actionButton.addDisposeListener( new DisposeListener()
      {
        /**
         * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
         */
        @Override
        public void widgetDisposed( DisposeEvent e )
        {
          image.dispose();
        }
      } );
    }

    return composite;
  }

  /**
   * This function updates the controls.
   * 
   * @param width
   *          The width.
   * @param height
   *          The height.
   */
  public void updateControls( )
  {
    updateImageCanvas( m_width, m_height );
    updateProgressBar();
    updateStatus( null );
  }

  /**
   * This function updates the status.
   * 
   * @param status
   *          A status, containing a message, which should be displayed in the upper area of the view. May be null.
   */
  public void updateStatus( IStatus status )
  {
    if( m_form == null || m_form.isDisposed() || m_content == null || m_content.isDisposed() )
      return;

    if( status != null && !status.isOK() )
      m_form.setMessage( status.getMessage(), MessageUtilitites.convertStatusSeverity( status.getSeverity() ) );
    else
      m_form.setMessage( null, IMessageProvider.NONE );

    m_form.layout( true, true );
  }

  /**
   * This function returns the movie player.
   * 
   * @return The movie player.
   */
  public MoviePlayer getPlayer( )
  {
    return m_player;
  }
}