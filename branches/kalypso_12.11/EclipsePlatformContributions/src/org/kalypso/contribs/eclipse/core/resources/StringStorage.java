/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse.core.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.builder.HashCodeBuilder;
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
 * REMARK: implementing {@link org.eclipse.ui.IPersistableElement} is only used in combinisation with {@link org.kalypso.contribs.eclipse.ui.editorinput.StorageEditorInput}.
 *
 * @author Gernot Belger
 */
public class StringStorage implements IEncodedStorage, IPersistableElement
{
  private final String m_content;

  private final IPath m_path;

  private String m_name = null;

  public StringStorage( final String content, final IPath path )
  {
    m_content = content;
    m_path = path;
  }

  /**
   * Overwrites the name of this storage.<br/>
   * If set to non-<code>null</code>, {@link #getName()} will return the given value.
   */
  public void setName( final String name )
  {
    m_name = name;
  }

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

  @Override
  public IPath getFullPath( )
  {
    return m_path;
  }

  @Override
  public String getName( )
  {
    if( m_name != null )
      return m_name;

    if( m_path == null )
      return "<Unknown>";

    return m_path.lastSegment();
  }

  @Override
  public boolean isReadOnly( )
  {
    return true;
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    final IAdapterManager adapterManager = Platform.getAdapterManager();
    return adapterManager.loadAdapter( this, adapter.getName() );
  }

  @Override
  public String getCharset( )
  {
    // allways Unicode
    return "UTF-8"; //$NON-NLS-1$
  }

  @Override
  public int hashCode( )
  {
    return new HashCodeBuilder().append( m_content ).append( m_path ).toHashCode();
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( this == obj )
      return true;

    if( !(obj instanceof StringStorage) )
      return false;

    final StringStorage other = (StringStorage)obj;

    if( m_path == null )
      return m_content.equals( other.m_content ) && other.m_path == null;

    return m_content.equals( other.m_content ) && m_path.equals( other.m_path );
  }

  @Override
  public String getFactoryId( )
  {
    return StringStorageInputFactory.ID;
  }

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
