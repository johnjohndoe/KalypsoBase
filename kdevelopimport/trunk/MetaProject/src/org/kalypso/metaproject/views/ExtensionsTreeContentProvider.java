/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/

package org.kalypso.metaproject.views;

import java.util.ArrayList;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author schlienger
 */
public class ExtensionsTreeContentProvider implements ITreeContentProvider
{
  private final IExtensionRegistry m_reg = Platform.getExtensionRegistry();
  
  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren( Object parentElement )
  {
    if( parentElement instanceof IInfo )
      return ( (IInfo)parentElement ).getChildren();
    
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent( Object element )
  {
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren( Object element )
  {
    if( element instanceof IInfo )
      return ( (IInfo)element ).hasChildren();
    
    return false;
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements( Object inputElement )
  {
    if( inputElement instanceof String[] )
    {
      final String[] namespaces = (String[])inputElement;
      final NamespaceInfo[] infos = new NamespaceInfo[namespaces.length];

      for( int i = 0; i < infos.length; i++ )
        infos[i] = new NamespaceInfo( namespaces[i] );

      return infos;
    }

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose()
  {
  // empty
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
  {
  // empty
  }

  private static interface IInfo
  {
    public Object[] getChildren();

    public boolean hasChildren();
  }

  public class NamespaceInfo implements IInfo
  {
    private final String m_namespace;

    public NamespaceInfo( final String namespace )
    {
      m_namespace = namespace;
    }

    public String getNamespace()
    {
      return m_namespace;
    }

    @Override
    public String toString()
    {
      return m_namespace;
    }

    public Object[] getChildren()
    {
      final ExtensionInfo ei = new ExtensionInfo( m_namespace );
      final ExtensionPointInfo epi = new ExtensionPointInfo( m_namespace );

      final ArrayList<IInfo> l = new ArrayList<IInfo>( 2 );

      if( ei.hasChildren() )
        l.add( ei );

      if( epi.hasChildren() )
        l.add( epi );

      return l.toArray();
    }

    public boolean hasChildren()
    {
      final ExtensionInfo ei = new ExtensionInfo( m_namespace );
      if( ei.hasChildren() )
        return true;

      final ExtensionPointInfo epi = new ExtensionPointInfo( m_namespace );
      return epi.hasChildren();
    }
  }

  public class ExtensionInfo implements IInfo
  {
    private final String m_namespace;

    public ExtensionInfo( final String namespace )
    {
      m_namespace = namespace;
    }

    public Object[] getChildren()
    {
      return m_reg.getExtensions( m_namespace );
    }

    @Override
    public String toString()
    {
      return "Extensions";
    }

    public boolean hasChildren()
    {
      return m_reg.getExtensions( m_namespace ).length > 0;
    }
  }

  public class ExtensionPointInfo implements IInfo
  {
    private final String m_namespace;

    public ExtensionPointInfo( final String namespace )
    {
      m_namespace = namespace;
    }

    public Object[] getChildren()
    {
      return m_reg.getExtensionPoints( m_namespace );
    }

    @Override
    public String toString()
    {
      return "ExtensionPoints";
    }

    public boolean hasChildren()
    {
      return m_reg.getExtensionPoints( m_namespace ).length > 0;
    }
  }
}
