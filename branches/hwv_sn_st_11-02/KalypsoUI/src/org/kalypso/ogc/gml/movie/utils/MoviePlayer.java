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

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.kalypso.ogc.gml.AbstractCascadingLayerTheme;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.movie.IMovieControls;
import org.kalypso.ogc.gml.movie.IMovieImageProvider;

/**
 * The movie player.
 * 
 * @author Holger Albert
 */
public class MoviePlayer
{
  /**
   * The gis template map model.
   */
  private GisTemplateMapModell m_mapModel;

  /**
   * The theme, marked as movie theme.
   */
  private AbstractCascadingLayerTheme m_movieTheme;

  /**
   * The movie image provider.
   */
  private IMovieImageProvider m_imageProvider;

  /**
   * May be {@link MovieRuntimeState#PLAYING}, {@link MovieRuntimeState#STOPPED}.
   */
  private MovieRuntimeState m_runtimeState;

  /**
   * The current theme.
   */
  private IKalypsoTheme m_currentTheme;

  /**
   * The next theme.
   */
  private IKalypsoTheme m_nextTheme;

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
    m_mapModel = mapModel;
    m_movieTheme = movieTheme;
    m_imageProvider = MovieUtilities.initImageProvider( movieTheme );
    m_runtimeState = MovieRuntimeState.STOPPED;
    m_currentTheme = null;
    m_nextTheme = null;
  }

  public Composite createScreenControls( Composite parent )
  {
    /* Create a composite. */
    Composite composite = new Composite( parent, SWT.NONE );
    GridLayout layout = new GridLayout( 1, false );
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    composite.setLayout( layout );

    // TODO

    return composite;
  }

  public Composite createButtonControls( Composite parent )
  {
    /* Get the movie controls. */
    IMovieControls movieControls = m_imageProvider.getMovieControls();

    /* Get the actions. */
    Action[] actions = movieControls.getActions( this );

    /* Create a composite. */
    Composite composite = new Composite( parent, SWT.NONE );
    GridLayout layout = new GridLayout( actions.length, false );
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    composite.setLayout( layout );

    /* Create the buttons. */
    for( int i = 0; i < actions.length; i++ )
    {
      /* Get the action. */
      final Action action = actions[i];

      /* Define the layout data properties. */
      int horizontalAlignment = SWT.CENTER;
      boolean grabExcessHorizontalSpace = false;
      if( i == 0 )
      {
        horizontalAlignment = SWT.END;
        grabExcessHorizontalSpace = true;
      }
      if( i == actions.length - 1 )
      {
        horizontalAlignment = SWT.BEGINNING;
        grabExcessHorizontalSpace = true;
      }

      /* Create a button for the action. */
      Button actionButton = new Button( parent, SWT.PUSH );
      actionButton.setImage( action.getImageDescriptor().createImage() );
      actionButton.setLayoutData( new GridData( horizontalAlignment, SWT.CENTER, grabExcessHorizontalSpace, false ) );
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
    }

    return composite;
  }

  /**
   * This function updates the runtime state.
   * 
   * @see {@link MovieRuntimeState#PLAYING} and {@link MovieRuntimeState#STOPPED}
   * @param runtimeState
   *          May be {@link MovieRuntimeState#PLAYING}, {@link MovieRuntimeState#STOPPED} or null, if it should not be
   *          changed.
   * @param nextTheme
   *          The next theme, if playing would be resumed. May be null. In this case, the actual theme will not be
   *          changed.
   */
  public void updateRuntimeState( MovieRuntimeState runtimeState, IKalypsoTheme nextTheme )
  {
    m_runtimeState = runtimeState;
    m_nextTheme = nextTheme;
  }
}