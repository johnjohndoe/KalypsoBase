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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A content provider which either returns a {@link org.eclipse.core.runtime.IStatus} or delegates to another content
 * provider.<br>
 * Handy for having a status report on error and the normal behaviour else.
 * 
 * @author Gernot Belger
 */
public class StatusAndDelegateContentProvider implements ITreeContentProvider
{
  private final IContentProvider m_delegate;

  private Object m_input;

  private final Object[] NO_CHILDREN;

  /**
   * @param All
   *            request to this content provider are delegated to the delegate as long as the input is not of type
   *            {@link org.eclipse.core.runtime.IStatus}. The delegate will be disposed if this instance gets disposed.
   */
  public StatusAndDelegateContentProvider( final IContentProvider delegate )
  {
    m_delegate = delegate;
    NO_CHILDREN = new Object[] {};
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren( final Object parentElement )
  {
    if( m_input instanceof IStatus )
      return NO_CHILDREN;

    if( m_delegate instanceof ITreeContentProvider )
      return ((ITreeContentProvider) m_delegate).getChildren( parentElement );

    return NO_CHILDREN;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent( final Object element )
  {
    if( m_input instanceof IStatus )
      return null;

    if( m_delegate instanceof ITreeContentProvider )
      return ((ITreeContentProvider) m_delegate).getParent( element );

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren( final Object element )
  {
    if( m_input instanceof IStatus )
      return false;

    if( m_delegate instanceof ITreeContentProvider )
      return ((ITreeContentProvider) m_delegate).hasChildren( element );

    return false;
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements( final Object inputElement )
  {
    if( m_input instanceof IStatus )
      return new Object[] { m_input };

    if( m_delegate instanceof IStructuredContentProvider )
      return ((IStructuredContentProvider) m_delegate).getElements( inputElement );

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose( )
  {
    m_delegate.dispose();
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    m_input = newInput;

    m_delegate.inputChanged( viewer, oldInput, newInput );
  }

}
