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
package org.kalypso.simulation.ui.calccase;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.commons.java.io.DeleteObsoleteFilesVisitor;
import org.kalypso.commons.java.io.FileCopyVisitor;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.simulation.ui.i18n.Messages;

/**
 * @author belger
 */
public class ModelSynchronizer
{
  private final File m_serverRoot;

  private final IProject m_resourceRoot;

  private final File m_resourceRootFile;

  public ModelSynchronizer( final IProject root, final File serverRoot )
  {
    m_resourceRoot = root;
    // Hack, weil getLocation für IProject's nicht funktioniert!
    // hoffentlich leitet alles von Resource ab
    m_resourceRootFile = ( (Resource)m_resourceRoot ).getLocalManager().locationFor( m_resourceRoot ).toFile();

    m_serverRoot = serverRoot;
  }

  public void updateLocal( final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( Messages.getString("org.kalypso.simulation.ui.calccase.ModelSynchronizer.0"), 3000 ); //$NON-NLS-1$

    try
    {
      if( !m_resourceRoot.exists() )
      {
        m_resourceRoot.create( new SubProgressMonitor( monitor, 500 ) );
        m_resourceRoot.open( new SubProgressMonitor( monitor, 500 ) );
      }
      else
        monitor.worked( 1000 );

      // server -> local
      synchronizeProject( m_serverRoot, m_resourceRootFile, new SubProgressMonitor( monitor, 1000 ) );

      // local refreshen
      m_resourceRoot.refreshLocal( IResource.DEPTH_INFINITE, new SubProgressMonitor( monitor, 1000 ) );
    }
    catch( final IOException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e, Messages.getString("org.kalypso.simulation.ui.calccase.ModelSynchronizer.1") ) ); //$NON-NLS-1$
    }
    finally
    {
      monitor.done();
    }
  }

  private void synchronizeProject( final File from, final File to, final IProgressMonitor monitor ) throws IOException
  {
    monitor.beginTask( Messages.getString("org.kalypso.simulation.ui.calccase.ModelSynchronizer.2"), 1000 ); //$NON-NLS-1$

    final long start = new java.util.Date().getTime();
    System.out.println("Modelsync Starting at: " + start); //$NON-NLS-1$
    
    try
    {
      final FileCopyVisitor copyVisitor = new FileCopyVisitor( from, to, true, ModelNature.CONTROL_NAME );
      org.kalypso.commons.java.io.FileUtilities.accept( from, copyVisitor, true );

      final DeleteObsoleteFilesVisitor deleteVisitor = new DeleteObsoleteFilesVisitor( to, from,
          ModelNature.CONTROL_NAME );
      FileUtilities.accept( to, deleteVisitor, true );
    }
    finally
    {
      final long stop = new java.util.Date().getTime();
      System.out.println("Modelsync Stoping at: " + stop ); //$NON-NLS-1$
      final long diff = stop - start;
      System.out.println("Modelsync Diff: " + diff + " ms --> " + diff/1000 + " s"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
      monitor.done();
    }
  }

  private void copyAll( final File from, final File to, final IProgressMonitor monitor ) throws IOException
  {
    monitor.beginTask( Messages.getString("org.kalypso.simulation.ui.calccase.ModelSynchronizer.3"), 1000 ); //$NON-NLS-1$

    try
    {
      final FileCopyVisitor copyVisitor = new FileCopyVisitor( from, to, true );
      FileUtilities.accept( from, copyVisitor, true );
    }
    finally
    {
      monitor.done();
    }
  }

  /**
   * Schreibt ein einzelnes Verzeichnis innerhalb des lokalen Projekts zurück zum server Das Verzeichnis darf
   * Serverseitig noch nicht existieren
   * 
   * @param folder
   * @param monitor
   * 
   * @throws CoreException
   */
  public void commitFolder( final IFolder folder, final IProgressMonitor monitor ) throws CoreException
  {
    final String projectRelativePath = folder.getProjectRelativePath().toString();

    final File serverDir = new File( m_serverRoot, projectRelativePath );

    if( serverDir.exists() )
      throw new CoreException( StatusUtilities.createErrorStatus( Messages.getString("org.kalypso.simulation.ui.calccase.ModelSynchronizer.4") //$NON-NLS-1$
          + projectRelativePath ) );

    final File localDir = new File( m_resourceRootFile, projectRelativePath );
    try
    {
      copyAll( localDir, serverDir, monitor );
    }
    catch( final IOException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e, Messages.getString("org.kalypso.simulation.ui.calccase.ModelSynchronizer.5") ) ); //$NON-NLS-1$
    }
  }

  public File getServerRoot()
  {
    return m_serverRoot;
  }

  /**
   * Lädt einen Remote Folder vom Server und legt in local ab überschreibt, ist lokal bereits etwas vorhanden, gibts ne
   * Fehlermeldung
   * 
   * @param dir
   * @param localName
   * @param monitor
   * 
   * @throws CoreException
   */
  public void getFolder( final File dir, final String localName, final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( Messages.getString("org.kalypso.simulation.ui.calccase.ModelSynchronizer.6"), 2000 ); //$NON-NLS-1$

    final String relativePath = FileUtilities.getRelativePathTo( m_serverRoot, dir );
    final IFile file = m_resourceRoot.getFile( localName );
    if( file.exists() )
      throw new CoreException( StatusUtilities.createErrorStatus( Messages.getString("org.kalypso.simulation.ui.calccase.ModelSynchronizer.7") //$NON-NLS-1$
          + relativePath ) );

    try
    {
      final File localDir = new File( m_resourceRootFile, localName );
      copyAll( dir, localDir, new SubProgressMonitor( monitor, 1000 ) );
      file.getParent().refreshLocal( IResource.DEPTH_INFINITE, new SubProgressMonitor( monitor, 1000 ) );
    }
    catch( final IOException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e, Messages.getString("org.kalypso.simulation.ui.calccase.ModelSynchronizer.8") ) ); //$NON-NLS-1$
    }

  }

  public void commitProject() throws CoreException
  {
    if( !m_serverRoot.exists() )
      m_serverRoot.mkdir();

    // server -> local
    final File lockFile = new File( m_serverRoot, ".lock" ); //$NON-NLS-1$
    if( lockFile.exists() )
    {
      String user = "<unbekannt>"; //$NON-NLS-1$
      try
      {
        user = FileUtils.readFileToString( lockFile, "UTF-8" ); //$NON-NLS-1$
      }
      catch( final IOException e )
      {
        e.printStackTrace();
      }

      throw new CoreException( StatusUtilities.createErrorStatus( Messages.getString("org.kalypso.simulation.ui.calccase.ModelSynchronizer.9") + user ) ); //$NON-NLS-1$
    }

    try
    {
      final String user = System.getProperties().getProperty( "user.name", "<unbekannt>" ); //$NON-NLS-1$ //$NON-NLS-2$
      FileUtils.writeStringToFile( lockFile, user, "UTF-8" ); //$NON-NLS-1$
      synchronizeProject( m_resourceRootFile, m_serverRoot, new NullProgressMonitor() );
    }
    catch( IOException e )
    {
      e.printStackTrace();

      throw new CoreException( StatusUtilities.statusFromThrowable( e, Messages.getString("org.kalypso.simulation.ui.calccase.ModelSynchronizer.10") ) ); //$NON-NLS-1$
    }
    finally
    {
      lockFile.delete();
    }
  }

  public File[] getRemoteCalcCases() throws CoreException
  {
    try
    {
      final CalcDirCollector collector = new CalcDirCollector();
      FileUtilities.accept( getServerRoot(), collector, true );
      return collector.getCalcDirs();
    }
    catch( IOException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e, Messages.getString("org.kalypso.simulation.ui.calccase.ModelSynchronizer.11") ) ); //$NON-NLS-1$
    }

  }
}