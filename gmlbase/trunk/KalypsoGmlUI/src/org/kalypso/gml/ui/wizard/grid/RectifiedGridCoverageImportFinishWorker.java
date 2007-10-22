/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.gml.ui.wizard.grid;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.kalypso.commons.resources.SetContentHelper;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.grid.ConvertAscii2Binary;
import org.kalypso.grid.GeoGridException;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypsodeegree_impl.gml.binding.commons.CoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;

/**
 * extracted from RectifiedGridCoverageImportWizard
 * 
 * @author kuch
 */
public class RectifiedGridCoverageImportFinishWorker implements ICoreRunnableWithProgress
{
  private final Shell m_shell;

  private final File m_selectedFile;

  private final RectifiedGridDomain m_domain;

  private final IPath m_containerName;

  private final String m_gmlFileName;

  private final ICoverageCollection m_coverages;

  public RectifiedGridCoverageImportFinishWorker( final Shell shell, final File selectedFile, final ICoverageCollection coverages, final RectifiedGridDomain domain, final IPath containerName, final String gmlFileName )
  {
    m_shell = shell;
    m_selectedFile = selectedFile;
    m_coverages = coverages;
    m_domain = domain;
    m_containerName = containerName;
    m_gmlFileName = gmlFileName;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException, CoreException
  {
    final SubMonitor progress = SubMonitor.convert( monitor, "Rasterdaten Import", 100 );

    final List<File> files2delete = new ArrayList<File>();
    try
    {
      monitor.subTask( "- importiere Daten in den Arbeitsbereich" );

      /* Convert .asc files to .bin files if necessairy */
      final List<File> fileList = new ArrayList<File>();
      fileList.add( m_selectedFile );

      try
      {
        convertToBinary( fileList, files2delete, progress.newChild( 70 ) );
      }
      catch( final IOException e )
      {
        e.printStackTrace();
        return StatusUtilities.statusFromThrowable( e, "Fehler beim ASC-BIN Konvertieren: " + e.getLocalizedMessage() );
      }

      final IStatus importStatus = importExternalFiles( progress.newChild( 10 ), fileList );
      if( !importStatus.isOK() )
        return importStatus;

      /* Fill workspace */
      final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      final IResource resource = root.findMember( m_containerName );
      if( !resource.exists() || !(resource instanceof IContainer) )
        throw new CoreException( StatusUtilities.createErrorStatus( "Container \"" + m_containerName + "\" does not exist." ) );
      final IContainer container = (IContainer) resource;

      final ICoverageCollection coverages = m_coverages == null ? new CoverageCollection( ResourceUtilities.createURL( container ), null ) : m_coverages;

      createGmlWorkspace( container, fileList, coverages, progress.newChild( 20 ) );

      return Status.OK_STATUS;
    }
    catch( final CoreException e )
    {
      throw e;
    }
    catch( final Throwable e )
    {
      e.printStackTrace();

      throw new InvocationTargetException( e );
    }
    finally
    {
      /* Delete temporary .bin file */
      for( final File file : files2delete )
        file.delete();

      monitor.done();
    }
  }

  private void createGmlWorkspace( final IContainer container, final List<File> file2import, final ICoverageCollection coverages, final IProgressMonitor monitor ) throws CoreException, MalformedURLException
  {
    final SubMonitor progress = SubMonitor.convert( monitor, file2import.size() + 10 );

    for( final File file : file2import )
    {
      final IFile importFileResource = container.getFile( new Path( file.getName() ) );
      final URL fileUrl = ResourceUtilities.createURL( importFileResource );
      final String mimeType = "image/" + importFileResource.getFileExtension();

      CoverageCollection.addCoverage( coverages, m_domain, fileUrl, mimeType );

      ProgressUtilities.worked( progress, 1 );
    }

    if( m_coverages != null ) // do not save if coverages already exists
      return;

    final IFile gmlFileResource = container.getFile( new Path( m_gmlFileName ) );
    final SetContentHelper contentHelper = new SetContentHelper()
    {
      @Override
      protected void write( final OutputStreamWriter writer ) throws Throwable
      {
        GmlSerializer.serializeWorkspace( writer, coverages.getWrappedFeature().getWorkspace() );
      }
    };
    contentHelper.setFileContents( gmlFileResource, false, true, new SubProgressMonitor( monitor, 1 ) );
    ProgressUtilities.worked( progress, 10 );
  }

  private IStatus importExternalFiles( final IProgressMonitor monitor, final List<File> fileList ) throws InvocationTargetException, InterruptedException
  {
    final Shell shell = m_shell;
    final IOverwriteQuery overwriteQuery = new IOverwriteQuery()
    {
      public String queryOverwrite( final String pathString )
      {
        if( MessageDialog.openQuestion( shell, "Datei import", "Die Datei " + pathString + " exisitert bereits.\nSoll Sie überschrieben werden?" ) )
          return IOverwriteQuery.YES;
        return IOverwriteQuery.NO;
      }
    };
    final ImportOperation operation = new ImportOperation( m_containerName, FileSystemStructureProvider.INSTANCE, overwriteQuery, fileList );
    operation.setOverwriteResources( false );
    operation.setCreateContainerStructure( false );
    operation.setContext( m_shell );
    operation.run( monitor );

    return operation.getStatus();
  }

  /**
   * Special: converts any .asc to an internal binary format. Replaces these occurences in the list of files.
   * 
   * @param files2delete
   *            This list get filled with the newly generated temp files.
   */
  private void convertToBinary( final List<File> fileList, final List<File> files2delete, final IProgressMonitor monitor ) throws IOException, CoreException, GeoGridException
  {
    final SubMonitor progress = SubMonitor.convert( monitor, fileList.size() );

    for( int i = 0; i < fileList.size(); i++ )
    {
      final File file = fileList.get( i );
      if( file.getName().toLowerCase().endsWith( ".asc" ) )
      {
        final File binFile = File.createTempFile( file.getName(), ".bin" );
        binFile.deleteOnExit();

        final ConvertAscii2Binary converter = new ConvertAscii2Binary( file.toURL(), binFile, 2 );
        converter.doConvert( progress.newChild( 1 ) );

        fileList.set( i, binFile ); // just replace it
        files2delete.add( binFile );
      }
    }
  }

}
