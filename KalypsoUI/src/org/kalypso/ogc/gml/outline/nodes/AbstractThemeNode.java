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
package org.kalypso.ogc.gml.outline.nodes;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Font;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerUtilities;

/**
 * @author Gernot Belger
 */
abstract class AbstractThemeNode<T> extends PlatformObject implements IThemeNode
{
  protected static Object[] EMPTY_CHILDREN = new Object[] {};

  private final IThemeNode m_parent;

  private final T m_element;

  private IThemeNode[] m_childNodes;

  // private boolean m_disposed;

  AbstractThemeNode( final IThemeNode parent, final T element )
  {
    m_parent = parent;

    Assert.isNotNull( element );

    m_element = element;
  }

  @Override
  public void dispose( )
  {
    disposeChildren();

    // m_disposed = true;
  }

  protected void checkDisposed( )
  {
    // FIXME: we need to do something....
// if( m_disposed )
//throw new IllegalStateException( "this node has already been dispsoed" ); //$NON-NLS-1$
//      System.out.println( "this node has already been dispsoed" ); //$NON-NLS-1$
  }

  @Override
  public void clear( )
  {
    checkDisposed();

    disposeChildren();
  }

  private void disposeChildren( )
  {
    if( m_childNodes == null )
      return;

    for( final IThemeNode child : m_childNodes )
      child.dispose();
    m_childNodes = null;
  }

  @Override
  public T getElement( )
  {
    checkDisposed();

    return m_element;
  }

  /**
   * Overwritten, else mapping items to object does not work for the tree, as these item get recreated for every call to
   * getChildren.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    checkDisposed();

    if( obj instanceof AbstractThemeNode< ? > )
      return getElement() == ((AbstractThemeNode< ? >)obj).getElement();

    return false;
  }

  /**
   * @see #equals(Object)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    checkDisposed();

    return getElement().hashCode();
  }

  @Override
  public IThemeNode getParent( )
  {
    checkDisposed();

    return m_parent;
  }

  @Override
  public String getDescription( )
  {
    checkDisposed();

    return null;
  }

  @Override
  public ImageDescriptor getImageDescriptor( )
  {
    checkDisposed();

    final IThemeNode[] children = getChildren();
    if( children.length > 0 )
      return children[children.length - 1].getImageDescriptor();

    return null;
  }

  @Override
  public Font getFont( final Object element )
  {
    checkDisposed();

    return null;
  }

  @Override
  public boolean hasChildren( )
  {
    checkDisposed();

    return getElementChildren().length > 0;
  }

  @Override
  public boolean hasChildrenCompact( )
  {
    checkDisposed();

    final Object[] children = getElementChildren();
    if( children.length == 0 )
      return false;

    if( children.length > 1 )
      return true;

    final IThemeNode childNode = NodeFactory.createNode( this, children[0] );
    final boolean hasChildren = childNode.hasChildrenCompact();
    childNode.dispose();
    return hasChildren;
  }

  @Override
  public IThemeNode[] getChildren( )
  {
    checkDisposed();

    if( m_childNodes != null )
      return m_childNodes;

    final Object[] children = getElementChildren();
    m_childNodes = NodeFactory.createNodes( this, children );
    if( doReverseChildren() )
      ArrayUtils.reverse( m_childNodes );
    return m_childNodes;
  }

  /**
   * Returns <code>true</code> by default, overwrite to change.
   */
  protected boolean doReverseChildren( )
  {
    checkDisposed();

    return true;
  }

  @Override
  public IThemeNode[] getChildrenCompact( )
  {
    checkDisposed();

    /* Get all children. */
    final IThemeNode[] children = getChildren();
    if( children.length == 0 )
      return new IThemeNode[] {};

    /* If there are more than one children or if this node belongs to a cascading theme, return all children. */
    if( children.length > 1 || this instanceof CascadingThemeNode )
      return children;

    /* Else ask the single children for its own children. */
    return children[0].getChildrenCompact();
  }

  /**
   * Return the children of the real element here, they will be wrapped into {@link IThemeNode}'s.
   */
  protected abstract Object[] getElementChildren( );

  @Override
  public boolean isChecked( final Object element )
  {
    checkDisposed();

    Assert.isTrue( element == this );

    return true;
  }

  @Override
  public boolean isGrayed( final Object element )
  {
    checkDisposed();

    Assert.isTrue( element == this );

    return true;
  }

  @Override
  public String resolveI18nString( final String text )
  {
    checkDisposed();

    return m_parent.resolveI18nString( text );
  }

  @Override
  public ICommand setVisible( final boolean checked )
  {
    checkDisposed();

    return null;
  }

  protected TreeViewer getViewer( )
  {
    checkDisposed();

    if( m_parent == null )
      return null;

    return ((AbstractThemeNode< ? >)m_parent).getViewer();
  }

  protected final void refreshViewer( final IThemeNode elementToRefresh )
  {
    checkDisposed();

    elementToRefresh.clear();

    ViewerUtilities.refresh( getViewer(), elementToRefresh, true );
  }

  protected final void updateViewer( final IThemeNode[] elementsToUpdate )
  {
    checkDisposed();

    ViewerUtilities.update( getViewer(), elementsToUpdate, null, true );
  }

  /**
   * Returns <code>false</code> by default.
   */
  @Override
  public boolean isLabelInImage( )
  {
    return false;
  }

  /**
   * Returns the outline image by default. Overwrite to implement a different behavior.
   * 
   * @see #getImageDescriptor()
   */
  @Override
  public ImageDescriptor getLegendImage( )
  {
    return getImageDescriptor();
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    checkDisposed();

    final T element = getElement();

    if( adapter.isInstance( element ) )
      return element;

    if( element instanceof IAdaptable )
    {
      final IAdaptable adaptable = (IAdaptable)element;
      final Object adapted = adaptable.getAdapter( adapter );
      if( adapted != null )
        return adapted;
    }

    return null;
  }

  /**
   * Return <code>true</code> by default, overwrite to change.
   * 
   * @see org.kalypso.ogc.gml.outline.nodes.IThemeNode#isCompactable()
   */
  @Override
  public boolean isCompactable( )
  {
    return true;
  }
}