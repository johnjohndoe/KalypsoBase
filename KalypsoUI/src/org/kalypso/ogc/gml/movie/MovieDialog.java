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
package org.kalypso.ogc.gml.movie;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.ogc.gml.movie.controls.MovieComposite;
import org.kalypso.ogc.gml.movie.utils.MoviePlayer;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * The movie dialog.
 * 
 * @author Holger Albert
 */
public class MovieDialog extends Dialog
{
  /**
   * The movie player.
   */
  private MoviePlayer m_player;

  /**
   * The movie composite.
   */
  protected MovieComposite m_movieComposite;

  /**
   * Creates a dialog instance. Note that the window will have no visual representation (no widgets) until it is told to
   * open. By default, open blocks for dialogs.
   * 
   * @param parentShell
   *          The parent shell, or null to create a top-level shell.
   * @param player
   *          The movie player.
   */
  public MovieDialog( Shell parentShell, MoviePlayer player )
  {
    super( parentShell );

    m_player = player;
    m_movieComposite = null;
  }

  /**
   * Creates a dialog with the given parent.
   * 
   * @param parentShell
   *          Object that returns the current parent shell.
   * @param player
   *          The movie player.
   */
  public MovieDialog( IShellProvider parentShell, MoviePlayer player )
  {
    super( parentShell );

    m_player = player;
    m_movieComposite = null;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( Composite parent )
  {
    /* Set the title. */
    getShell().setText( "Film" );

    /* Create the main composite. */
    Composite main = (Composite) super.createDialogArea( parent );
    main.setLayout( new GridLayout( 1, false ) );
    main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create the movie composite. */
    m_movieComposite = new MovieComposite( main, SWT.NONE, m_player );
    m_movieComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    m_movieComposite.addControlListener( new ControlAdapter()
    {
      /**
       * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
       */
      @Override
      public void controlResized( ControlEvent e )
      {
        m_movieComposite.pack();
        MovieDialog.this.getContents().pack();
        MovieDialog.this.getShell().pack();
      }
    } );

    return main;
  }

  /**
   * @see org.eclipse.jface.window.Window#getContents()
   */
  @Override
  protected Control getContents( )
  {
    return super.getContents();
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createButtonBar( Composite parent )
  {
    Composite buttonComposite = m_movieComposite.createButtonControls( parent );
    buttonComposite.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, false ) );

    Button cancelButton = createButton( buttonComposite, CANCEL, "Ecject", true );
    cancelButton.setImage( KalypsoGisPlugin.getImageProvider().getImageDescriptor( ImageProvider.DESCRIPTORS.MOVIE_PLAYER_EJECT ).createImage() );
    cancelButton.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );

    return buttonComposite;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#isResizable()
   */
  @Override
  protected boolean isResizable( )
  {
    return false;
  }
}