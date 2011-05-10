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
package org.kalypso.ogc.gml.movie.utils;

import java.awt.Frame;
import java.awt.image.RenderedImage;

import javax.media.jai.widget.ImageCanvas;

import org.eclipse.jface.action.Action;
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
import org.eclipse.swt.widgets.ProgressBar;
import org.kalypso.ogc.gml.AbstractCascadingLayerTheme;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.movie.IMovieControls;
import org.kalypso.ogc.gml.movie.IMovieImageProvider;

/**
 * The movie player.
 * 
 * @author Holger Albert
 */
@SuppressWarnings("deprecation")
public class MoviePlayer
{
  /**
   * The movie image provider.
   */
  private IMovieImageProvider m_imageProvider;

  /**
   * The image canvas.
   */
  private ImageCanvas m_imageCanvas;

  /**
   * The progress bar.
   */
  private ProgressBar m_progressBar;

  /**
   * The constructor.
   * 
   * @param mapModel
   *          The gis template map model.
   * @param movieTheme
   *          The theme, marked as movie theme.
   */
  public MoviePlayer( GisTemplateMapModell mapModel, AbstractCascadingLayerTheme movieTheme )
  {
    m_imageProvider = MovieUtilities.getImageProvider( mapModel, movieTheme );
    m_imageCanvas = null;
    m_progressBar = null;
  }

  /**
   * This function creates the screen controls.
   * 
   * @param parent
   *          The parent composite.
   * @return The screen controls.
   */
  public Composite createScreenControls( Composite parent )
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
    m_imageCanvas = new ImageCanvas( null );
    Frame virtualFrame = SWT_AWT.new_Frame( imageComposite );
    virtualFrame.add( m_imageCanvas );

    /* Create a progress bar. */
    m_progressBar = new ProgressBar( composite, SWT.NONE );
    m_progressBar.setMinimum( 0 );
    m_progressBar.setMaximum( m_imageProvider.getEndStep() );
    m_progressBar.setSelection( m_imageProvider.getCurrentStep() );
    m_progressBar.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );

    return composite;
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
    /* Get the movie controls. */
    IMovieControls movieControls = m_imageProvider.getMovieControls();

    /* Get the actions. */
    Action[] actions = movieControls.getActions( this );

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
  public void updateControls( int width, int height )
  {
    updateImageCanvas( width, height );
    updateProgressBar();
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

    /* Get the current image. */
    IMovieFrame currentFrame = m_imageProvider.getCurrentFrame();
    RenderedImage image = currentFrame.getImage( width, height );

    /* Set the current image. */
    m_imageCanvas.set( image );
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
    int currentStep = m_imageProvider.getCurrentStep();

    /* Set the current step. */
    m_progressBar.setSelection( currentStep );
  }

  public void stepTo( int step )
  {
    m_imageProvider.stepTo( step );
  }

  public int getCurrentStep( )
  {
    return m_imageProvider.getCurrentStep();
  }

  public int getEndStep( )
  {
    return m_imageProvider.getEndStep();
  }
}