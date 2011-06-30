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

package org.kalypso.ui.wizard.raster;

import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.ui.wizard.IKalypsoDataImportWizard;

public class ImportRasterSourceWizard extends Wizard implements IKalypsoDataImportWizard
{
  private ICommandTarget m_commandTarget;

  private ImportRasterSourceWizardPage m_page;

  private IProject m_project;

  private IKalypsoLayerModell m_mapModel;

  private WizardNewFileCreationPage m_gmlFilePage;

  private IFile m_mapFile;

  /**
   * @see org.kalypso.ui.wizard.IKalypsoDataImportWizard#setMapModel(org.kalypso.ogc.gml.IKalypsoLayerModell)
   */
  @Override
  public void setMapModel( final IKalypsoLayerModell modell )
  {
    Assert.isTrue( modell != null );

    m_mapModel = modell;
    m_project = m_mapModel.getProject();

    final URL context = m_mapModel.getContext();
    m_mapFile = ResourceUtilities.findFileFromURL( context );
  }

  @Override
  public void addPages( )
  {
    final IContainer mapContainer = m_mapFile == null ? null : m_mapFile.getParent();
    final IStructuredSelection selection = mapContainer == null ? StructuredSelection.EMPTY : new StructuredSelection( mapContainer );

    m_gmlFilePage = new WizardNewFileCreationPage( "gmlFile", selection );
    m_gmlFilePage.setFileExtension( "gml" );
    m_gmlFilePage.setAllowExistingResources( true );
    m_gmlFilePage.setTitle( "Coverage Data File" );
    m_gmlFilePage.setDescription( "Please choose where to store the data file containing the grid coverages. You may also enter an existing filename." );
    addPage( m_gmlFilePage );

    m_page = new ImportRasterSourceWizardPage( "Add RasterDataModel" ); //$NON-NLS-1$ 
    if( m_project != null )
      m_page.setProject( m_project );
    addPage( m_page );
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    final IFile coverageFile = getGmlFile();

    final IFile styleFile = getSldFile( coverageFile );
    final boolean generateDefaultStyle = m_page.checkDefaultStyle();
    final String styleName = generateDefaultStyle ? null : m_page.getStyleName();

    final ICommandTarget outlineviewer = m_commandTarget;
    final ICoreRunnableWithProgress operation = new ImportRasterOperation( coverageFile, styleFile, styleName, outlineviewer, m_mapModel );

    final IStatus status = RunnableContextHelper.execute( getContainer(), true, false, operation );
    KalypsoAddLayerPlugin.getDefault().getLog().log( status );
    ErrorDialog.openError( getShell(), getWindowTitle(), Messages.getString( "org.kalypso.ui.wizard.raster.ImportRasterSourceWizard.2" ), status ); //$NON-NLS-1$

    return status.isOK();
  }

  private IFile getSldFile( final IFile coverageFile )
  {
    final boolean useDefaultStyle = m_page.checkDefaultStyle();
    if( useDefaultStyle )
    {
      final String gmlName = coverageFile.getName().toString();
      final String basicName = FilenameUtils.removeExtension( gmlName );
      final String sldName = basicName + ".sld";
      return coverageFile.getParent().getFile( new Path( sldName ) );
    }

    final IPath stylePath = m_page.getStylePath();
    return coverageFile.getWorkspace().getRoot().getFile( stylePath );
  }

  private IFile getGmlFile( )
  {
    final IPath containerFullPath = m_gmlFilePage.getContainerFullPath();
    String fileName = m_gmlFilePage.getFileName();
    if( !FilenameUtils.isExtension( fileName.toLowerCase(), "gml" ) )
      fileName += ".gml";

    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final IResource folder = root.findMember( containerFullPath );
    if( folder instanceof IContainer )
    {
      final IContainer container = (IContainer) folder;
      return container.getFile( new Path( fileName ) );
    }

    // TODO: error handling
    return null;
  }

  /**
   * @see org.kalypso.ui.wizard.data.IKalypsoDataImportWizard#setOutlineViewer(org.kalypso.ogc.gml.outline.GisMapOutlineViewer)
   */
  @Override
  public void setCommandTarget( final ICommandTarget commandTarget )
  {
    m_commandTarget = commandTarget;
  }

  /**
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
   *      org.eclipse.jface.viewers.IStructuredSelection)
   */
  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
  }
}