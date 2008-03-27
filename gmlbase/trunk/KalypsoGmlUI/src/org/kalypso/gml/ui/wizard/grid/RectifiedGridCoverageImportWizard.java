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
package org.kalypso.gml.ui.wizard.grid;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.grid.IGridMetaReader;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain.OffsetVector;

/**
 * @author Gernot Belger
 */
public class RectifiedGridCoverageImportWizard extends Wizard implements IImportWizard
{
  private IStructuredSelection m_selection;

  private WizardNewFileCreationPage m_fileCreationPage;

  private PageSelectGeodataFile m_pageSelect;

  private ICoverageCollection m_coverages;

  private IFolder m_gridFolder;

  private ICoverage m_newCoverage;

  public RectifiedGridCoverageImportWizard( )
  {
    final IDialogSettings dialogSettings = KalypsoGmlUIPlugin.getDefault().getDialogSettings();
    final IDialogSettings settings = createDialogSettings( dialogSettings, "ImportRectifiedGridCoverageWizardSettings" );
    setDialogSettings( settings );
    setNeedsProgressMonitor( true );

    setWindowTitle( "Rasterdaten Import" );
  }

  /**
   * Sets the coverage, into which to import the new grids.
   * <p>
   * If not set before this wizard is started, the user will be asked for a file-location where a new gml file will be
   * created.
   * </p>
   */
  public void setCoverageCollection( final ICoverageCollection coverages )
  {
    m_coverages = coverages;
  }

  private IDialogSettings createDialogSettings( final IDialogSettings dialogSettings, final String name )
  {
    final IDialogSettings section = dialogSettings.getSection( name );
    if( section != null )
    {
      return section;
    }

    return dialogSettings.addNewSection( name );
  }

  /**
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
   *      org.eclipse.jface.viewers.IStructuredSelection)
   */
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    m_selection = selection;
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages( )
  {
    if( m_gridFolder == null )
    {
      /* Only ask user for gml-file if no coverage collection is set */
      m_fileCreationPage = new WizardNewFileCreationPage( "newFile", m_selection );
    }

    m_pageSelect = new PageSelectGeodataFile( m_fileCreationPage );
    addPage( m_pageSelect );

    if( m_fileCreationPage != null )
    {
      m_fileCreationPage.setTitle( "Auswahl der Datei" );
      m_fileCreationPage.setDescription( "W‰hlen Sie Speicherort und Name der neuen Datei aus." );

      addPage( m_fileCreationPage );
    }

    super.addPages();
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    // read/copy input
    final File selectedFile = m_pageSelect.getSelectedFile();
    final String crs = m_pageSelect.getProjection();
    final OffsetVector offsetX = m_pageSelect.getOffsetX();
    final OffsetVector offsetY = m_pageSelect.getOffsetY();
    final Double[] ulc = m_pageSelect.getUpperLeftCorner();

    final IPath containerName = m_fileCreationPage == null ? null : m_fileCreationPage.getContainerFullPath();
    final String gmlFileName = m_fileCreationPage == null ? null : m_fileCreationPage.getFileName();
    try
    {
      final IGridMetaReader reader = m_pageSelect.getReader();
      final RectifiedGridDomain coverage = reader.getCoverage( offsetX, offsetY, ulc, crs );

      final IFile gmlFile;
      if( containerName != null && gmlFileName != null )
        gmlFile = ResourcesPlugin.getWorkspace().getRoot().getFolder( containerName ).getFile( gmlFileName );
      else
        gmlFile = null;
      final IContainer gridFolder = m_gridFolder == null ? gmlFile.getParent() : m_gridFolder;

      final RectifiedGridCoverageImportFinishWorker op = new RectifiedGridCoverageImportFinishWorker( getShell(), selectedFile, m_coverages, coverage, gmlFile, gridFolder, null );

      final IStatus status = RunnableContextHelper.execute( getContainer(), true, true, op );

      m_newCoverage = op.getNewCoverage();

      ErrorDialog.openError( getShell(), getWindowTitle(), "Fehler beim Import einer Datei", status );
      return status.isOK();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Sets the target forlder for the imported grid file.
   * <p>
   * If no set, the user will be asked for the folder.
   * </p>
   * <p>
   * Must be called before {@link #createPageControls(org.eclipse.swt.widgets.Composite)}.
   */
  public void setGridFolder( final IFolder gridFolder )
  {
    m_gridFolder = gridFolder;
  }

  public ICoverage getNewCoverage( )
  {
    return m_newCoverage;
  }
}
