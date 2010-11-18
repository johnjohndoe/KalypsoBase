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
package org.kalypso.core.status.tree.provider;

import java.util.WeakHashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A content provider for status objects.
 * 
 * @author Holger Albert
 */
public class StatusContentProvider implements ITreeContentProvider
{
  /**
   * The input of the viewer.
   */
  private IStatus m_input;

  /**
   * The parents of the status objects.
   */
  private WeakHashMap<IStatus, IStatus> m_parents;

  /**
   * The constructor.
   */
  public StatusContentProvider( )
  {
    m_input = null;
    m_parents = null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  @Override
  public Object[] getChildren( Object parentElement )
  {
    if( parentElement instanceof IStatus )
    {
      IStatus status = (IStatus) parentElement;
      if( !status.isMultiStatus() )
        return null;

      return status.getChildren();
    }

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  @Override
  public Object getParent( Object element )
  {
    if( m_input == null || m_parents == null )
      return null;

    if( element instanceof IStatus )
    {
      IStatus status = (IStatus) element;

      return m_parents.get( status );
    }

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  @Override
  public boolean hasChildren( Object element )
  {
    if( element instanceof IStatus )
      return ((IStatus) element).isMultiStatus();

    return false;
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  @Override
  public Object[] getElements( Object inputElement )
  {
    if( inputElement instanceof IStatus )
    {
      IStatus status = (IStatus) inputElement;
      if( !status.isMultiStatus() )
        return null;

      return status.getChildren();
    }

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    m_input = null;
    m_parents = null;
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  @Override
  public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
  {
    /* Reset the old input. */
    m_input = null;
    m_parents = null;

    /* If the new input is no working set manager, ignore it. */
    if( newInput == null || !(newInput instanceof IStatus) )
      return;

    /* Store the new input. */
    m_input = (IStatus) newInput;
    m_parents = retrieveParents();
  }

  /**
   * This function builds up an index with the parents of each project.
   * 
   * @return The index of the parents.
   */
  private WeakHashMap<IStatus, IStatus> retrieveParents( )
  {
    /* If the input is null, this function can and should not be called. */
    if( m_input == null )
      return null;

    /* Create the parents hash map. */
    WeakHashMap<IStatus, IStatus> parents = new WeakHashMap<IStatus, IStatus>();

    /* If it is only a single status, no parents exist. */
    if( !m_input.isMultiStatus() )
      return parents;

    addChildren( parents, m_input );

    return parents;
  }

  /**
   * This function hashes all status objects along with their parent in the hash map.
   * 
   * @param parents
   *          The hash map.
   * @param parentNode
   *          The parent node.
   */
  private void addChildren( WeakHashMap<IStatus, IStatus> parents, IStatus parentNode )
  {
    IStatus[] children = parentNode.getChildren();
    for( int i = 0; i < children.length; i++ )
    {
      /* Get the status. */
      IStatus childNode = children[i];

      /* Add this status and its parent. */
      parents.put( childNode, parentNode );

      /* If it is a multi status, add its children and their parents, too. */
      if( childNode.isMultiStatus() )
        addChildren( parents, childNode );
    }
  }
}