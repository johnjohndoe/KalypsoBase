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
package org.kalypso.ogc.sensor.template;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * PseudoTemplateEditorInput
 * 
 * @author schlienger
 */
public class PseudoTemplateEditorInput implements IFileEditorInput
{
  private final TemplateStorage m_storage;

  private final String m_fileExtension;

  /**
   * Constructor
   * 
   * @param storage
   *          template storage on which this pseudo template is based
   * @param fileExtension
   *          name of the file extension that this template should have once saved
   */
  public PseudoTemplateEditorInput( final TemplateStorage storage, final String fileExtension )
  {
    m_storage = storage;
    m_fileExtension = fileExtension;
  }

  /**
   * @see org.eclipse.ui.IStorageEditorInput#getStorage()
   */
  @Override
  public IStorage getStorage( )
  {
    return m_storage;
  }

  /**
   * @see org.eclipse.ui.IEditorInput#exists()
   */
  @Override
  public boolean exists( )
  {
    return false;
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
    return FilenameUtils.getBaseName( m_storage.getName() ) + m_fileExtension;
  }

  /**
   * @see org.eclipse.ui.IEditorInput#getPersistable()
   */
  @Override
  public IPersistableElement getPersistable( )
  {
    return null;
  }

  /**
   * @see org.eclipse.ui.IEditorInput#getToolTipText()
   */
  @Override
  public String getToolTipText( )
  {
    return m_storage.getHref();
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    return null;
  }

  /**
   * Call is delegated to Object class since we always want new pseudo templates even if the underlying file is the same
   * one.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    return super.equals( obj );
  }

  /**
   * @see org.eclipse.ui.IFileEditorInput#getFile()
   */
  @Override
  public IFile getFile( )
  {
    final IPath fullPath = m_storage.getFullPath();
    if( fullPath == null )
      return null;

    final String pathNoExt = FilenameUtils.removeExtension( fullPath.toString() );
    final IPath filePath = new Path( pathNoExt + m_fileExtension );

    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final IWorkspaceRoot root = workspace.getRoot();
    return root.getFile( filePath );
  }
}