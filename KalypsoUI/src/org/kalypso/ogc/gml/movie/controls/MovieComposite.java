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
package org.kalypso.ogc.gml.movie.controls;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.widgets.Form;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.ogc.gml.movie.IMovieControls;
import org.kalypso.ogc.gml.movie.IMovieImageProvider;
import org.kalypso.ogc.gml.movie.utils.IMovieFrame;
import org.kalypso.ogc.gml.movie.utils.MoviePlayer;
import org.kalypso.ogc.gml.movie.utils.MovieResolution;
import org.kalypso.ogc.gml.movie.utils.MovieUtilities;
import org.kalypso.ogc.gml.movie.utils.ResolutionLabelProvider;

import com.sun.media.jai.widget.DisplayJAI;

/**
 * The movie composite.
 * 
 * @author Holger Albert
 */
public class MovieComposite extends Composite
{
  /**
   * The movie player.
   */
  protected MoviePlayer m_player;

  /**
   * The form.
   */
  protected Form m_form;

  /**
   * The content, which the form contains.
   */
  private Composite m_content;

  /**
   * An AWT control for displaying the image.
   */
  protected DisplayJAI m_displayJAI;

  /**
   * The progress bar.
   */
  private ProgressBar m_progressBar;

  /**
   * The progress label.
   */
  private Label m_progressLabel;

  /**
   * The selected resolution.
   */
  protected MovieResolution m_resolution;

  /**
   * The constructor.
   * 
   * @param parent
   *          A widget which will be the parent of the new instance (cannot be null).
   * @param style
   *          The style of widget to construct.
   * @param player
   *          The movie player.
   */
  public MovieComposite( final Composite parent, final int style, final MoviePlayer player )
  {
    super( parent, style );

    /* Initialize with parameters. */
    m_player = player;
    m_player.initialize( this );

    /* Initialize. */
    m_form = null;
    m_content = null;
    m_displayJAI = null;
    m_progressBar = null;
    m_progressLabel = null;
    m_resolution = new MovieResolution( "default", 800, 600 ); //$NON-NLS-1$

    /* Create the controls. */
    createControls();
    ControlUtils.addDisposeListener( this );
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
    /* Stop the player. */
    m_player.dispose();

    /* Discard the references. */
    m_player = null;
    m_form = null;
    m_content = null;
    m_displayJAI = null;
    m_progressBar = null;
    m_progressLabel = null;

    super.dispose();
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

    /* A form. */
    m_form = new Form( content, SWT.NONE );
    m_form.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Get the body of the form. */
    final Composite body = m_form.getBody();

    /* Set the properties for the body of the form. */
    final GridLayout bodyLayout = new GridLayout( 1, false );
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
    contentInternalComposite.setLayout( new GridLayout( 2, false ) );

    /* Create the default screen. */
    final Composite screenComposite = createScreenControls( contentInternalComposite );
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
  private Composite createScreenControls( final Composite parent )
  {
    /* Create a composite. */
    final Composite composite = new Composite( parent, SWT.NONE );
    final GridLayout layout = new GridLayout( 3, false );
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    composite.setLayout( layout );

    /* Create the image composite. */
    final Composite imageComposite = new Composite( composite, SWT.EMBEDDED | SWT.NO_BACKGROUND );
    final GridData imageData = new GridData( SWT.FILL, SWT.FILL, true, true, 3, 1 );
    imageData.widthHint = m_resolution.getWidth();
    imageData.heightHint = m_resolution.getHeight();
    imageComposite.setLayoutData( imageData );

    /* Create the image canvas. */
    final Frame virtualFrame = SWT_AWT.new_Frame( imageComposite );
    virtualFrame.setLayout( new GridBagLayout() );
    m_displayJAI = new DisplayJAI( createEmptyImage( m_resolution.getWidth(), m_resolution.getHeight() ) );
    virtualFrame.add( m_displayJAI, new GridBagConstraints( 0, 0, 1, 1, 100, 100, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

    /* Create a progress bar. */
    m_progressBar = new ProgressBar( composite, SWT.NONE );
    m_progressBar.setMinimum( 0 );
    m_progressBar.setMaximum( m_player.getEndStep() );
    m_progressBar.setSelection( m_player.getCurrentStep() );
    m_progressBar.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false, 3, 1 ) );

    /* Create the progress label. */
    m_progressLabel = new Label( composite, SWT.NONE );
    m_progressLabel.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
    m_progressLabel.setText( "" ); //$NON-NLS-1$

    /* Create a combo box. */
    final ComboViewer resolutionViewer = new ComboViewer( composite, SWT.READ_ONLY );
    resolutionViewer.getCombo().setLayoutData( new GridData( SWT.FILL, SWT.TOP, false, false ) );
    resolutionViewer.setContentProvider( new ArrayContentProvider() );
    resolutionViewer.setLabelProvider( new ResolutionLabelProvider() );
    final MovieResolution[] input = MovieUtilities.getResolutions();
    resolutionViewer.setInput( input );
    resolutionViewer.setSelection( new StructuredSelection( input[1] ) );
    resolutionViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      /**
       * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
       */
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final ISelection selection = event.getSelection();
        final Object firstElement = ((StructuredSelection)selection).getFirstElement();
        m_resolution = (MovieResolution)firstElement;

        final GridData layoutData = new GridData( SWT.FILL, SWT.FILL, true, true, 3, 1 );
        layoutData.widthHint = m_resolution.getWidth();
        layoutData.heightHint = m_resolution.getHeight();
        imageComposite.setLayoutData( layoutData );
        imageComposite.setSize( m_resolution.getWidth(), m_resolution.getHeight() );

        m_displayJAI.set( createEmptyImage( m_resolution.getWidth(), m_resolution.getHeight() ) );

        MovieComposite.this.pack();
      }
    } );

    /* Create a spinner. */
    final Spinner spinner = new Spinner( composite, SWT.BORDER );
    spinner.setLayoutData( new GridData( SWT.FILL, SWT.TOP, false, false ) );
    spinner.setMinimum( 100 );
    spinner.setIncrement( 50 );
    spinner.setMaximum( 1000 );
    spinner.setSelection( 250 );
    spinner.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        /* Spinner. */
        final Spinner source = (Spinner)e.getSource();

        /* Get the selection. */
        final int selection = source.getSelection();

        /* Update the frame relay. */
        m_player.updateFrameDelay( selection );
      }
    } );

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
  protected BufferedImage createEmptyImage( final int width, final int height )
  {
    /* Create an empty image. */
    final byte[] byteArray = new byte[] { -1, 0 };
    final ColorModel colorModel = new IndexColorModel( 1, 2, byteArray, byteArray, byteArray );
    final WritableRaster writeableRaster = Raster.createPackedRaster( DataBuffer.TYPE_BYTE, width, height, 1, 1, null );
    final BufferedImage bufferedImage = new BufferedImage( colorModel, writeableRaster, false, null );

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
  protected void updateImageCanvas( final int width, final int height )
  {
    /* Is the control disposed? */
    if( m_displayJAI == null )
      return;

    /* Get the current frame. */
    final IMovieFrame currentFrame = m_player.getCurrentFrame();
    if( currentFrame != null )
    {
      /* Get the current image. */
      final RenderedImage currentImage = currentFrame.getImage( width, height );
      if( currentImage != null )
      {
        m_displayJAI.set( currentImage );
        return;
      }
    }

    /* Create the start image. */
    final BufferedImage startImage = createEmptyImage( width, height );

    /* Set the start image. */
    m_displayJAI.set( startImage );
  }

  /**
   * This function updates the progress bar.
   */
  protected void updateProgressBar( )
  {
    /* Is the control disposed? */
    if( m_progressBar == null || m_progressBar.isDisposed() )
      return;

    /* Get the current step. */
    final int currentStep = m_player.getCurrentStep();

    /* Get the current frame. */
    final IMovieFrame currentFrame = m_player.getCurrentFrame();
    if( currentFrame == null )
      return;

    /* Set the current step. */
    m_progressBar.setSelection( currentStep );
    m_progressLabel.setText( currentFrame.getLabel() );
  }

  /**
   * This function creates the button controls.
   * 
   * @param parent
   *          The parent composite.
   * @return The button controls.
   */
  public Composite createButtonControls( final Composite parent )
  {
    /* Get the image provider. */
    final IMovieImageProvider imageProvider = m_player.getImageProvider();

    /* Get the movie controls. */
    final IMovieControls movieControls = imageProvider.getMovieControls();

    /* Get the actions. */
    final Action[] actions = movieControls.getActions( m_player );

    /* Create a composite. */
    final Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayout( new GridLayout( actions.length, false ) );

    /* Create the buttons. */
    for( final Action action : actions )
    {
      /* Create the image. */
      final Image image = action.getImageDescriptor().createImage();

      /* Create a button for the action. */
      final Button actionButton = new Button( composite, SWT.PUSH );
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
        public void widgetSelected( final SelectionEvent e )
        {
          /* Get the source. */
          final Button source = (Button)e.getSource();

          /* Build the event. */
          final Event event = new Event();
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
        public void widgetDisposed( final DisposeEvent e )
        {
          image.dispose();
        }
      } );
    }

    return composite;
  }

  /**
   * This function returns the selected resolution.
   * 
   * @return The selected resolution.
   */
  public MovieResolution getResolution( )
  {
    return m_resolution;
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
    if( isDisposed() )
      return;

    updateImageCanvas( m_resolution.getWidth(), m_resolution.getHeight() );

    if( isDisposed() )
      return;

    final Display display = getDisplay();
    display.asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        updateProgressBar();
      }
    } );
  }
}