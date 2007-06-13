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
package org.kalypso.gml.ui.wizard.imports;

import java.io.File;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.kalypso.commons.resources.SetContentHelper;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.cv.RectifiedGridDomain;

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

  public RectifiedGridCoverageImportFinishWorker( final Shell shell, final File selectedFile, final RectifiedGridDomain domain, final IPath containerName, final String gmlFileName )
  {
    m_shell = shell;
    m_selectedFile = selectedFile;
    m_domain = domain;
    m_containerName = containerName;
    m_gmlFileName = gmlFileName;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
  {
    monitor.beginTask( "Creating " + m_gmlFileName, 4 );

    try
    {
      /* init kalypso */
      monitor.subTask( " - initializing KALYPSO..." );
      KalypsoGisPlugin.getDefault();

      /* some commonly needed variables */
      final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      final IResource resource = root.findMember( m_containerName );
      final IContainer container = (IContainer) resource;

      final IFile gmlFileResource = container.getFile( new Path( m_gmlFileName ) );
      final IFile importFileResource = container.getFile( new Path( m_selectedFile.getName() ) );

      monitor.worked( 1 );

      /* Workspace && create workspace */
      monitor.subTask( " - creating GMLWorkspace..." );
      final GMLWorkspace workspace = RectifiedGridUtils.createGmlWorkspace( m_domain, importFileResource );

      monitor.worked( 1 );

      /* import external file */
      monitor.subTask( " - creating GMLWorkspace..." );

      final IStatus importStatus = importExternalFiles( monitor, m_selectedFile );
      if( !importStatus.isOK() )
        return importStatus;

      /* create gml file */
      monitor.subTask( "- writing " + m_gmlFileName );
      if( !resource.exists() || !(resource instanceof IContainer) )
        StatusUtilities.createErrorStatus( "Container \"" + m_containerName + "\" does not exist." );

      final SetContentHelper contentHelper = new SetContentHelper()
      {
        @Override
        protected void write( final OutputStreamWriter writer ) throws Throwable
        {
          GmlSerializer.serializeWorkspace( writer, workspace );
        }
      };
      contentHelper.setFileContents( gmlFileResource, false, true, new SubProgressMonitor( monitor, 1 ) );
      monitor.worked( 1 );

      return Status.OK_STATUS;
    }
    catch( final Throwable e )
    {
      throw new InvocationTargetException( e );
    }
    finally
    {
      monitor.done();
    }
  }

  private IStatus importExternalFiles( final IProgressMonitor monitor, final File importFile ) throws InvocationTargetException, InterruptedException
  {
    final List<File> fileList = new ArrayList<File>();
    fileList.add( importFile );

    final IOverwriteQuery overwriteQuery = new IOverwriteQuery()
    {
      public String queryOverwrite( final String pathString )
      {
        if( MessageDialog.openQuestion( m_shell, "Datei import", "Die Datei " + pathString + " exisitert bereits.\nSoll Sie überschrieben werden?" ) )
          return IOverwriteQuery.YES;
        return IOverwriteQuery.NO;
      }
    };
    final ImportOperation operation = new ImportOperation( m_containerName, FileSystemStructureProvider.INSTANCE, overwriteQuery, fileList );
    operation.setOverwriteResources( false );
    operation.setCreateContainerStructure( false );
    operation.setContext( m_shell );
    operation.run( new SubProgressMonitor( monitor, 1 ) );

    return operation.getStatus();
  }

}
