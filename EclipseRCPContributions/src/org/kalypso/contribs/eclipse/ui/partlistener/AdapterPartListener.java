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
package org.kalypso.contribs.eclipse.ui.partlistener;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * The purpose of this part listener is to get a certain adapter of the active workbench part.
 * <p>
 * This listeners is usually used by another workbench part, which needs an adapter from another part.
 * </p>
 * <p>
 * In this case this class is instatiated in the {@link org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)} method.
 * </p>
 * <p>
 * There are severel strategies involved:
 * </p>
 * <p>
 * What is to do on initialization: a) search among the open parts for one adapting to the searched object b) do nothing
 * </p>
 * <p>
 * What to do when the curent part we are adapting to is closed.
 * </p>
 * </p>
 * 
 * @author Gernot Belger
 */
public class AdapterPartListener<C> implements IPartListener2
{
  private final Class<C> m_adapter;

  private final IAdapterEater<C> m_adapterEater;

  private final IAdapterFinder<C> m_closeFinder;

  private IWorkbenchPart m_part = null;

  private final IAdapterFinder<C> m_initFinder;

  private IWorkbenchPage m_page;

  public AdapterPartListener( final Class<C> adapter, final IAdapterEater<C> adapterEater, final IAdapterFinder<C> initFinder, final IAdapterFinder<C> closeFinder )
  {
    m_adapter = adapter;
    m_adapterEater = adapterEater;
    m_initFinder = initFinder;
    m_closeFinder = closeFinder;
  }

  public void init( final IWorkbenchPage page )
  {
    m_page = page;
    page.addPartListener( this );

    m_initFinder.findAdapterPart( page, this );
  }

  public void dispose( )
  {
    if( m_page != null )
    {
      m_page.removePartListener( this );
      m_page = null;
    }
  }

  /**
   * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partActivated( final IWorkbenchPartReference partRef )
  {
    adaptPartReference( partRef );
  }

  /**
   * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partBroughtToTop( final IWorkbenchPartReference partRef )
  {
  }

  /**
   * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partClosed( final IWorkbenchPartReference partRef )
  {
    if( partRef.getPart( false ) == m_part )
    {
      // reset this reference
      m_part = null;

      // try to find a new adapter, if nothing is found this will lead to setAdapter( null, null )
      m_closeFinder.findAdapterPart( m_page, this );
    }
  }

  /**
   * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partDeactivated( final IWorkbenchPartReference partRef )
  {
  }

  /**
   * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partOpened( final IWorkbenchPartReference partRef )
  {
  }

  /**
   * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partHidden( final IWorkbenchPartReference partRef )
  {
  }

  /**
   * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partVisible( final IWorkbenchPartReference partRef )
  {
  }

  /**
   * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partInputChanged( final IWorkbenchPartReference partRef )
  {
  }

  /**
   * Tries to find the adapter for the given part. If one is found, return true.
   * 
   * @return If an adapter for this partRef is found return true, false otherwise.
   */
  public boolean adaptPartReference( final IWorkbenchPartReference partRef )
  {
    if( partRef == null )
      return false;

    return adaptPart( partRef.getPart( false ) );
  }

  @SuppressWarnings("unchecked")
  public boolean adaptPart( final IWorkbenchPart part )
  {
    if( part == null )
      return false;

    final C adapter = (C) part.getAdapter( m_adapter );
    if( adapter == null )
      return false;

    setAdapter( part, adapter );

    return true;
  }

  public void setAdapter( final IWorkbenchPart part, final C adapter )
  {
    m_part = part;

    m_adapterEater.setAdapter( part, adapter );
  }

  public IWorkbenchPart getPart( )
  {
    return m_part;
  }
}
