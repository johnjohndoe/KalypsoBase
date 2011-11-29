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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;

/**
 * An action that delegates all calls to a given delegate.<br/>
 * Can be used to override a part of an action.
 * 
 * @author Gernot Belger
 */
public abstract class DelegateAction implements IAction
{
  private final IAction m_delegate;

  public DelegateAction( final IAction delegate )
  {
    m_delegate = delegate;
  }

  @Override
  public void addPropertyChangeListener( final IPropertyChangeListener listener )
  {
    m_delegate.addPropertyChangeListener( listener );
  }

  @Override
  public int getAccelerator( )
  {
    return m_delegate.getAccelerator();
  }

  @Override
  public String getActionDefinitionId( )
  {
    return m_delegate.getActionDefinitionId();
  }

  @Override
  public String getDescription( )
  {
    return m_delegate.getDescription();
  }

  @Override
  public ImageDescriptor getDisabledImageDescriptor( )
  {
    return m_delegate.getDisabledImageDescriptor();
  }

  @Override
  public HelpListener getHelpListener( )
  {
    return m_delegate.getHelpListener();
  }

  @Override
  public ImageDescriptor getHoverImageDescriptor( )
  {
    return m_delegate.getHoverImageDescriptor();
  }

  @Override
  public String getId( )
  {
    return m_delegate.getId();
  }

  @Override
  public ImageDescriptor getImageDescriptor( )
  {
    return m_delegate.getImageDescriptor();
  }

  @Override
  public IMenuCreator getMenuCreator( )
  {
    return m_delegate.getMenuCreator();
  }

  @Override
  public int getStyle( )
  {
    return m_delegate.getStyle();
  }

  @Override
  public String getText( )
  {
    return m_delegate.getText();
  }

  @Override
  public String getToolTipText( )
  {
    return m_delegate.getToolTipText();
  }

  @Override
  public boolean isChecked( )
  {
    return m_delegate.isChecked();
  }

  @Override
  public boolean isEnabled( )
  {
    return m_delegate.isEnabled();
  }

  @Override
  public boolean isHandled( )
  {
    return m_delegate.isHandled();
  }

  @Override
  public void removePropertyChangeListener( final IPropertyChangeListener listener )
  {
    m_delegate.removePropertyChangeListener( listener );
  }

  @Override
  public void run( )
  {
    m_delegate.run();
  }

  @Override
  public void runWithEvent( final Event event )
  {
    m_delegate.runWithEvent( event );
  }

  @Override
  public void setActionDefinitionId( final String id )
  {
    m_delegate.setActionDefinitionId( id );
  }

  @Override
  public void setChecked( final boolean checked )
  {
    m_delegate.setChecked( checked );
  }

  @Override
  public void setDescription( final String text )
  {
    m_delegate.setDescription( text );
  }

  @Override
  public void setDisabledImageDescriptor( final ImageDescriptor newImage )
  {
    m_delegate.setDisabledImageDescriptor( newImage );
  }

  @Override
  public void setEnabled( final boolean enabled )
  {
    m_delegate.setEnabled( enabled );
  }

  @Override
  public void setHelpListener( final HelpListener listener )
  {
    m_delegate.setHelpListener( listener );
  }

  @Override
  public void setHoverImageDescriptor( final ImageDescriptor newImage )
  {
    m_delegate.setHoverImageDescriptor( newImage );
  }

  @Override
  public void setId( final String id )
  {
    m_delegate.setId( id );
  }

  @Override
  public void setImageDescriptor( final ImageDescriptor newImage )
  {
    m_delegate.setImageDescriptor( newImage );
  }

  @Override
  public void setMenuCreator( final IMenuCreator creator )
  {
    m_delegate.setMenuCreator( creator );
  }

  @Override
  public void setText( final String text )
  {
    m_delegate.setText( text );
  }

  @Override
  public void setToolTipText( final String text )
  {
    m_delegate.setToolTipText( text );
  }

  @Override
  public void setAccelerator( final int keycode )
  {
    m_delegate.setAccelerator( keycode );
  }

}
