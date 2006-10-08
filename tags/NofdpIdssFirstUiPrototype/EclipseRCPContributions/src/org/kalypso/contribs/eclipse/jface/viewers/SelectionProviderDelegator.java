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
package org.kalypso.contribs.eclipse.jface.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * This class can be used to delegate selection providement to a delegate.
 * <p>
 * All selection event from the delegate are propagated to the listeners of this class.
 * </p>
 * <p>
 * All gets/sets of selections are delegated to the delegate.
 * </p>
 * 
 * @author Gernot Belger
 */
public class SelectionProviderDelegator implements ISelectionProvider, ISelectionChangedListener
{
  private ISelectionProvider m_delegate = null;

  private List<ISelectionChangedListener> m_listener = new ArrayList<ISelectionChangedListener>( 5 );

  public SelectionProviderDelegator( )
  {
  }

  public SelectionProviderDelegator( final ISelectionProvider delegate )
  {
    setDelegate( delegate );
  }

  public void dispose( )
  {
    if( m_delegate != null )
    {
      m_delegate.removeSelectionChangedListener( this );
      m_delegate = null;
    }
    
    m_listener.clear();
  }

  public void setDelegate( final ISelectionProvider delegate )
  {
    if( m_delegate != null )
    {
      m_delegate.removeSelectionChangedListener( this );
      m_delegate = null;
    }

    m_delegate = delegate;

    if( m_delegate != null )
      m_delegate.addSelectionChangedListener( this );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void addSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_listener.add( listener );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  public ISelection getSelection( )
  {
    if( m_delegate != null )
      return m_delegate.getSelection();

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void removeSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_listener.remove( listener );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  public void setSelection( final ISelection selection )
  {
    if( m_delegate != null )
      m_delegate.setSelection( selection );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  public void selectionChanged( final SelectionChangedEvent event )
  {
    // changes from my delegate,
    // delegate it to my listeners
    fireSelectionChanged( event );
  }

  private void fireSelectionChanged( final SelectionChangedEvent event )
  {
    for( final ISelectionChangedListener l : m_listener )
      l.selectionChanged( event );
  }

}
