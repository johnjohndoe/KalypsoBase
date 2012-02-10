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
package org.kalypso.contribs.eclipse.compare;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Image;
import org.kalypso.contribs.eclipse.EclipsePlatformContributionsPlugin;

/**
 * {@link IStructureComparator} implementation based on {@link File}s.
 * 
 * @author Gernot Belger
 */
public class FileStructureComparator implements IStructureComparator, IStreamContentAccessor, ITypedElement
{
  private final File m_file;

  public FileStructureComparator( final File file )
  {
    m_file = file;
  }

  /**
   * @see org.eclipse.compare.structuremergeviewer.IStructureComparator#getChildren()
   */
  @Override
  public Object[] getChildren( )
  {
    final File[] childFiles = m_file.listFiles();
    if( childFiles == null )
      return null;

    final FileStructureComparator[] children = new FileStructureComparator[childFiles.length];
    for( int i = 0; i < children.length; i++ )
      children[i] = new FileStructureComparator( childFiles[i] );

    return children;
  }

  /**
   * @see org.eclipse.compare.IStreamContentAccessor#getContents()
   */
  @Override
  public InputStream getContents( ) throws CoreException
  {
    if( !m_file.isFile() )
      return null;

    try
    {
      return new BufferedInputStream( new FileInputStream( m_file ) );
    }
    catch( final FileNotFoundException e )
    {
      final Status status = new Status( IStatus.ERROR, EclipsePlatformContributionsPlugin.getID(), "Failed to open stream", e );
      throw new CoreException( status );
    }
  }

  /**
   * @see org.eclipse.compare.ITypedElement#getName()
   */
  @Override
  public String getName( )
  {
    return m_file.getName();
  }

  /**
   * @see org.eclipse.compare.ITypedElement#getImage()
   */
  @Override
  public Image getImage( )
  {
    return null;
  }

  /**
   * @see org.eclipse.compare.ITypedElement#getType()
   */
  @Override
  public String getType( )
  {
    if( m_file.isDirectory() )
      return ITypedElement.FOLDER_TYPE;

    if( m_file.isFile() )
      return ITypedElement.TEXT_TYPE;

    return ITypedElement.UNKNOWN_TYPE;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return m_file.getName();
  }

  /**
   * Tricky: we hash a(and equal) according to file name, as this is unique within one container.<br/>
   * The file or it's path wouldn't work, as they are never equal for different ancestors.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    return m_file.getName().hashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( !(obj instanceof FileStructureComparator) )
      return false;
    if( obj == this )
      return true;

    final String otherName = ((FileStructureComparator) obj).m_file.getName();
    return m_file.getName().equals( otherName );
  }

  public File getFile( )
  {
    return m_file;
  }

}
