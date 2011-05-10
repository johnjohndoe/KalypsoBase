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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.Form;
import org.kalypso.contribs.eclipse.ui.forms.MessageUtilitites;
import org.kalypso.ogc.gml.movie.utils.MoviePlayer;

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
   * The constructor.
   * 
   * @param parent
   *          A widget which will be the parent of the new instance (cannot be null).
   * @param style
   *          The style of widget to construct.
   * @param moviePlayer
   *          The movie player
   */
  public MovieComposite( Composite parent, int style, MoviePlayer player )
  {
    super( parent, style );

    /* Initialize with parameters. */
    m_player = player;

    /* Initialize. */
    m_form = null;
    m_content = null;

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
    Composite screenComposite = m_player.createScreenControls( contentInternalComposite );
    screenComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    return contentInternalComposite;
  }

  /**
   * This function updates the status.
   * 
   * @param status
   *          A status, containing a message, which should be displayed in the upper area of the view. May be null.
   */
  protected void updateStatus( IStatus status )
  {
    if( m_form == null || m_form.isDisposed() || m_content == null || m_content.isDisposed() )
      return;

    if( status != null && !status.isOK() )
      m_form.setMessage( status.getMessage(), MessageUtilitites.convertStatusSeverity( status.getSeverity() ) );
    else
      m_form.setMessage( null, IMessageProvider.NONE );

    m_form.layout( true, true );
  }
}