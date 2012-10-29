/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Bj�rnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universit�t Hamburg-Harburg, Institut f�r Wasserbau, Hamburg, Germany
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
package org.kalypso.contribs.eclipse.ui.editorinput;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.FileEditorInput;
import org.kalypso.contribs.eclipse.i18n.Messages;

/**
 * An {@link org.eclipse.ui.IEditorInput} which is based on a {@link IStorage}.
 * <p>
 * Is persitable if the storage implements {@link org.eclipse.ui.IPersistableElement}. <br/>
 * 
 * @author Gernot Belger
 */
public class StorageEditorInput implements IFileEditorInput
{
  private final IStorage m_storage;

  /**
   * @param storage
   *          The stroage is used to represent the contents
   */
  public StorageEditorInput( final IStorage storage )
  {
    m_storage = storage;
  }

  /**
   * @see org.eclipse.ui.IStorageEditorInput#getStorage()
   */
  @Override
  public IStorage getStorage( )
  {
    return m_storage;
  }

  @Override
  public IFile getFile( )
  {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final IWorkspaceRoot root = workspace.getRoot();
    final IPath fullPath = m_storage.getFullPath();
    if( fullPath == null )
      return null;

    final IFile file = root.getFile( fullPath );

    if( FileEditorInput.isLocalFile( file ) )
      return file;

    return null;
  }

  /**
   * @see org.eclipse.ui.IEditorInput#exists()
   */
  @Override
  public boolean exists( )
  {
    final IFile file = getFile();
    if( file == null )
      return false;

    return file.exists();
  }

  /**
   * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
   */
  @Override
  public ImageDescriptor getImageDescriptor( )
  {
    return null;
  }

  /**
   * @see org.eclipse.ui.IEditorInput#getName()
   */
  @Override
  public String getName( )
  {
    return m_storage.getName();
  }

  /**
   * @see org.eclipse.ui.IEditorInput#getPersistable()
   */
  @Override
  public IPersistableElement getPersistable( )
  {
    if( m_storage instanceof IPersistableElement )
      return (IPersistableElement)m_storage;

    return null;
  }

  /**
   * @see org.eclipse.ui.IEditorInput#getToolTipText()
   */
  @Override
  public String getToolTipText( )
  {
    final IPath fullPath = m_storage.getFullPath();
    if( fullPath == null )
      return Messages.getString( "org.kalypso.contribs.eclipse.ui.editorinput.StorageEditorInput.0" ); //$NON-NLS-1$

    return fullPath.toOSString();
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( final Class adapter )
  {
    final IAdapterManager adapterManager = Platform.getAdapterManager();
    return adapterManager.loadAdapter( this, adapter.getName() );
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( this == obj )
      return true;

    if( !(obj instanceof StorageEditorInput) )
      return false;

    return m_storage.equals( ((StorageEditorInput)obj).m_storage );
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    return m_storage.hashCode();
  }
}