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
package org.kalypso.util.themes.position.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.Form;
import org.kalypso.contribs.eclipse.ui.forms.MessageUtilitites;
import org.kalypso.util.themes.position.PositionUtilities;
import org.kalypso.util.themes.position.listener.IPositionChangedListener;

/**
 * This composite edits the position.
 * 
 * @author Holger Albert
 */
public class PositionComposite extends Composite
{
  /**
   * The horizontal position.
   */
  protected int m_horizontal;

  /**
   * The vertical position.
   */
  protected int m_vertical;

  /**
   * This listeners are notified, if the position has changed.
   */
  private List<IPositionChangedListener> m_listener;

  /**
   * The form.
   */
  private Form m_main;

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
   * @param horizontal
   *          The default horizontal position.
   * @param vertical
   *          The default vertical position.
   */
  public PositionComposite( Composite parent, int style, int horizontal, int vertical )
  {
    super( parent, style );

    /* Initialize. */
    m_horizontal = PositionUtilities.checkHorizontalPosition( horizontal );
    m_vertical = PositionUtilities.checkVerticalPosition( vertical );
    m_listener = new ArrayList<IPositionChangedListener>();
    m_main = null;
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
    if( m_listener != null )
      m_listener.clear();

    m_horizontal = PositionUtilities.RIGHT;
    m_vertical = PositionUtilities.BOTTOM;
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
    GridLayout contentInternalData = new GridLayout( 2, false );
    contentInternalData.marginHeight = 0;
    contentInternalData.marginWidth = 0;
    contentInternalComposite.setLayout( contentInternalData );

    /* Create the horizontal group. */
    Group horizontalGroup = createHorizontalGroup( contentInternalComposite );
    horizontalGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create the vertical group. */
    Group verticalGroup = createVerticalGroup( contentInternalComposite );
    verticalGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    return contentInternalComposite;
  }

  /**
   * This function creates the horizontal group.
   * 
   * @param parent
   *          The parent composite.
   * @return The horizontal group.
   */
  private Group createHorizontalGroup( Composite parent )
  {
    /* Create a group. */
    Group horizontalGroup = new Group( parent, SWT.NONE );
    horizontalGroup.setLayout( new GridLayout( 1, false ) );
    horizontalGroup.setText( "Position Horizontal" );

    /* Create a button. */
    Button leftButton = new Button( horizontalGroup, SWT.RADIO );
    leftButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    leftButton.setText( "Links" );
    if( (m_horizontal & PositionUtilities.LEFT) != 0 )
      leftButton.setSelection( true );
    leftButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        m_horizontal = PositionUtilities.LEFT;
        firePositionChanged( m_horizontal, m_vertical );
      }
    } );

    /* Create a button. */
    Button hCenterButton = new Button( horizontalGroup, SWT.RADIO );
    hCenterButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    hCenterButton.setText( "Zentriert" );
    if( (m_horizontal & PositionUtilities.H_CENTER) != 0 )
      hCenterButton.setSelection( true );
    hCenterButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        m_horizontal = PositionUtilities.H_CENTER;
        firePositionChanged( m_horizontal, m_vertical );
      }
    } );

    /* Create a button. */
    Button rightButton = new Button( horizontalGroup, SWT.RADIO );
    rightButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    rightButton.setText( "Rechts" );
    if( (m_horizontal & PositionUtilities.RIGHT) != 0 )
      rightButton.setSelection( true );
    rightButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        m_horizontal = PositionUtilities.RIGHT;
        firePositionChanged( m_horizontal, m_vertical );
      }
    } );

    return horizontalGroup;
  }

  /**
   * This function creates the vertical group.
   * 
   * @param parent
   *          The parent composite.
   * @return The vertical group.
   */
  private Group createVerticalGroup( Composite parent )
  {
    /* Create a group. */
    Group verticalGroup = new Group( parent, SWT.NONE );
    verticalGroup.setLayout( new GridLayout( 1, false ) );
    verticalGroup.setText( "Position Vertikal" );

    /* Create a button. */
    Button topButton = new Button( verticalGroup, SWT.RADIO );
    topButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    topButton.setText( "Oben" );
    if( (m_vertical & PositionUtilities.TOP) != 0 )
      topButton.setSelection( true );
    topButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        m_vertical = PositionUtilities.TOP;
        firePositionChanged( m_horizontal, m_vertical );
      }
    } );

    /* Create a button. */
    Button vCenterButton = new Button( verticalGroup, SWT.RADIO );
    vCenterButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    vCenterButton.setText( "Zentriert" );
    if( (m_vertical & PositionUtilities.V_CENTER) != 0 )
      vCenterButton.setSelection( true );
    vCenterButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        m_vertical = PositionUtilities.V_CENTER;
        firePositionChanged( m_horizontal, m_vertical );
      }
    } );

    /* Create a button. */
    Button bottomButton = new Button( verticalGroup, SWT.RADIO );
    bottomButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    bottomButton.setText( "Unten" );
    if( (m_vertical & PositionUtilities.BOTTOM) != 0 )
      bottomButton.setSelection( true );
    bottomButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        m_vertical = PositionUtilities.BOTTOM;
        firePositionChanged( m_horizontal, m_vertical );
      }
    } );

    return verticalGroup;
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
   * This function fires a position changed event.
   * 
   * @param horizontal
   *          The horzizontal position.
   * @param vertical
   *          The vertical position.
   */
  protected void firePositionChanged( int horizontal, int vertical )
  {
    for( IPositionChangedListener listener : m_listener )
      listener.positionChanged( horizontal, vertical );
  }

  /**
   * This function adds a position changed listener.
   * 
   * @param listener
   *          The position changed listener to add.
   */
  public void addPositionChangedListener( IPositionChangedListener listener )
  {
    if( !m_listener.contains( listener ) )
      m_listener.add( listener );
  }

  /**
   * This function removes a position changed listener.
   * 
   * @param listener
   *          The position changed listener to remove.
   */
  public void removePositionChangedListener( IPositionChangedListener listener )
  {
    if( m_listener.contains( listener ) )
      m_listener.remove( listener );
  }

  /**
   * This function returns the horizontal position.
   * 
   * @return The horizontal position.
   */
  public int getHorizontal( )
  {
    return m_horizontal;
  }

  /**
   * This function returns the vertical position.
   * 
   * @return The vertical position.
   */
  public int getVertical( )
  {
    return m_vertical;
  }
}