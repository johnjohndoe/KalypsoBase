/*--------------- Kalypso-Header --------------------------------------------------------------------

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
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.contribs.eclipse.core.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.kalypso.contribs.eclipse.EclipsePlatformContributionsPlugin;
import org.kalypso.contribs.eclipse.ui.editorinput.StringStorageInputFactory;

/**
 * REMARK: implementing {@link org.eclipse.ui.IPersistableElement} is only used in combinisation with
 * {@link org.kalypso.contribs.eclipse.ui.editorinput.StorageEditorInput}.
 * 
 * @author Gernot Belger
 */
public class StringStorage implements IEncodedStorage, IPersistableElement
{
  private final String m_content;

  private final IPath m_path;

  public StringStorage( final String content, final IPath path )
  {
    m_content = content;
    m_path = path;
  }

  /**
   * @throws CoreException
   * @see org.eclipse.core.resources.IStorage#getContents()
   */
  @Override
  public InputStream getContents( ) throws CoreException
  {
    try
    {
      final byte[] bytes = m_content.getBytes( "UTF-8" ); //$NON-NLS-1$
      return new ByteArrayInputStream( bytes );
    }
    catch( final UnsupportedEncodingException e )
    {
      throw new CoreException( new Status( IStatus.ERROR, EclipsePlatformContributionsPlugin.getID(), 0, "", e ) ); //$NON-NLS-1$
    }
  }

  /**
   * @see org.eclipse.core.resources.IStorage#getFullPath()
   */
  @Override
  public IPath getFullPath( )
  {
    return m_path;
  }

  /**
   * @see org.eclipse.core.resources.IStorage#getName()
   */
  @Override
  public String getName( )
  {
    if( m_path == null )
      return "<Unknown>";
    
    return m_path.lastSegment();
  }

  /**
   * @see org.eclipse.core.resources.IStorage#isReadOnly()
   */
  @Override
  public boolean isReadOnly( )
  {
    return true;
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    final IAdapterManager adapterManager = Platform.getAdapterManager();
    return adapterManager.loadAdapter( this, adapter.getName() );
  }

  /**
   * @see org.eclipse.core.resources.IEncodedStorage#getCharset()
   */
  @Override
  public String getCharset( )
  {
    // allways Unicode
    return "UTF-8"; //$NON-NLS-1$
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( this == obj )
      return true;

    if( !(obj instanceof StringStorage) )
      return false;

    final StringStorage other = (StringStorage) obj;

    if( m_path == null )
      return m_content.equals( other.m_content ) && other.m_path == null;

    return m_content.equals( other.m_content ) && m_path.equals( other.m_path );
  }

  /**
   * @see org.eclipse.ui.IPersistableElement#getFactoryId()
   */
  @Override
  public String getFactoryId( )
  {
    return StringStorageInputFactory.ID;
  }

  /**
   * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
   */
  @Override
  public void saveState( final IMemento memento )
  {
    StringStorageInputFactory.saveState( this, memento );
  }

  public String getData( )
  {
    return m_content;
  }
}
