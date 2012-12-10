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
package org.kalypso.contribs.eclipse.jface.action;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A button based on a {@link IAction}.<br/>
 * The button will be configured and updated automatically depending on the state of an {@link IAction}.<br/>
 * If the button is selected, the action is run.
 * 
 * @author Gernot Belger
 */
public class ActionButton
{
  private final Collection<Image> m_images = new ArrayList<Image>();

  private final IAction m_action;

  private final Button m_button;

  public static Button createButton( final FormToolkit toolkit, final Composite parent, final IAction action )
  {
    final int style = convertStyle( action.getStyle() );

    final Button button = createButton( toolkit, parent, style );
    new ActionButton( button, action );
    return button;
  }

  private static Button createButton( final FormToolkit toolkit, final Composite parent, final int style )
  {
    if( toolkit == null )
      return new Button( parent, style );
    else
      return toolkit.createButton( parent, "", style );
  }

  private static int convertStyle( final int style )
  {
    switch( style )
    {
      case IAction.AS_PUSH_BUTTON:
        return SWT.PUSH;
      case IAction.AS_CHECK_BOX:
        return SWT.CHECK;
      case IAction.AS_RADIO_BUTTON:
        return SWT.RADIO;
      case IAction.AS_DROP_DOWN_MENU:
        return SWT.PUSH;

      default:
        throw new UnsupportedOperationException( "Unsupported button style: " + style ); //$NON-NLS-1$
    }
  }

  public ActionButton( final Button button, final IAction action )
  {
    m_button = button;
    m_action = action;

    hookListeners( button, action );

    initializeButton();

    updateButton();
  }

  private void hookListeners( final Button button, final IAction action )
  {
    final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener()
    {
      @Override
      public void propertyChange( final PropertyChangeEvent event )
      {
        updateButton();
      }
    };

    action.addPropertyChangeListener( propertyChangeListener );

    button.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        action.removePropertyChangeListener( propertyChangeListener );

        disposeImages();
      }
    } );

    button.addListener( SWT.Selection, new Listener()
    {
      @Override
      public void handleEvent( final Event event )
      {
        final boolean selection = button.getSelection();

        handleButtonSelected( event, selection );
      }
    } );
  }

  protected void disposeImages( )
  {
    for( final Image image : m_images )
      image.dispose();
  }

  protected void handleButtonSelected( final Event event, final boolean selection )
  {
    final int style = m_action.getStyle();
    if( style == IAction.AS_DROP_DOWN_MENU )
    {
      final IMenuCreator menuCreator = m_action.getMenuCreator();
      final Menu menu = menuCreator.getMenu( m_button );
      menu.setVisible( true );
    }
    else if( style == IAction.AS_CHECK_BOX || style == IAction.AS_RADIO_BUTTON )
      m_action.setChecked( selection );

    m_action.runWithEvent( event );
  }

  /**
   * Set properties that do NOT vary over time, only once.
   */
  private void initializeButton( )
  {
    // m_action.getAccelerator();

    // m_action.getHelpListener();

    // TODO: move to update and change if image changes
    // TODO: implement hover image
    final ImageDescriptor imageDescriptor = m_action.getImageDescriptor();
    if( imageDescriptor != null )
    {
      final Image image = imageDescriptor.createImage( m_button.getDisplay() );
      m_images.add( image );
      m_button.setImage( image );
    }
  }

  /**
   * Change properties that may vary over time.
   */
  protected void updateButton( )
  {
    m_button.setEnabled( m_action.isEnabled() );

    // m_action.getImageDescriptor();
    // m_button.setImage( null );
    // m_action.getDisabledImageDescriptor();
    // m_action.getHoverImageDescriptor();

    final String text = m_action.getText();
    m_button.setText( text == null ? "" : text );//$NON-NLS-1$
    m_button.setToolTipText( m_action.getDescription() );

    m_button.setSelection( m_action.isChecked() );
  }

}
