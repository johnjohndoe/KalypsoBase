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
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;

/**
 * Dieser Wizard dient dazu, (mehrere) Rasterdateien in eine bestehende GML-Datei zu imporiteren.
 *
 * @author Gernot Belger
 */
public class AddRectifiedGridCoveragesWizard extends Wizard
{
  private PageSelectGeodataFiles m_pageSelect;

  private final ICoverageCollection m_coverages;

  private final IContainer m_gridFolder;

  private final boolean m_allowUserChangeGridFolder;

  private ICoverage[] m_newCoverages;

  /**
   * @param gridFolder
   *          The new grid gets imported into this folder. If <code>null</code>, the user will be asked for the folder.
   * @param allowUserChangeGridFolder
   *          If <code>false</code>, the entry field for the grid folder is hidden Resets to <code>true</code>, if
   *          'gridFolder' is null..
   */
  public AddRectifiedGridCoveragesWizard( final ICoverageCollection coverages, final IContainer gridFolder, final boolean allowUserChangeGridFolder )
  {
    m_coverages = coverages;
    m_gridFolder = gridFolder;
    m_allowUserChangeGridFolder = allowUserChangeGridFolder;

    final IDialogSettings settings = PluginUtilities.getDialogSettings( KalypsoGmlUIPlugin.getDefault(), "ImportRectifiedGridCoverageWizardSettings" );
    setDialogSettings( settings );
    setNeedsProgressMonitor( true );

    setWindowTitle( "Import" );
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages( )
  {
    m_pageSelect = new PageSelectGeodataFiles( "pageSelect", m_gridFolder, m_allowUserChangeGridFolder );
    m_pageSelect.setTitle( "Rasterdatei" );
    m_pageSelect.setDescription( "W‰hlen Sie die Rasterdatei aus. Die Rasterdatei wird in den Arbeitsbereich kopiert." );

    addPage( m_pageSelect );

    super.addPages();
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    // read/copy input
    final File[] selectedFiles = m_pageSelect.getSelectedFiles();
    final String crs = m_pageSelect.getProjection();
    final IFolder gridFolder = m_pageSelect.getGridFolder();
    try
    {
      final ICoverageCollection coverageCollection = m_coverages;
      final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
      {
        public IStatus execute( final IProgressMonitor monitor ) throws CoreException, InvocationTargetException, InterruptedException
        {
          final SubMonitor progress = SubMonitor.convert( monitor, "Importiere Rasterdaten", 100 );
          final RectifiedGridDomain[] domains = ImportGridUtilities.readDomains( selectedFiles, crs, progress.newChild( 5, SubMonitor.SUPPRESS_NONE ) );
          final File[] convertedGrids = ImportGridUtilities.convertGrids( selectedFiles, crs, progress.newChild( 80, SubMonitor.SUPPRESS_NONE ) );
          final IFile[] importedFiles = ImportGridUtilities.importExternalFiles( getShell(), convertedGrids, gridFolder, progress.newChild( 10, SubMonitor.SUPPRESS_NONE ) );
          final String[] names = new String[selectedFiles.length];
          for( int i = 0; i < selectedFiles.length; i++ )
            names[i] = selectedFiles[i].getName();
          final ICoverage[] coverages = addCoverages( names, domains, importedFiles, coverageCollection, progress.newChild( 5, SubMonitor.SUPPRESS_NONE ) );
          setCoverages( coverages );

          return Status.OK_STATUS;
        }

        public ICoverage[] addCoverages( final String[] names, final RectifiedGridDomain[] domains, final IFile[] gridFiles, final ICoverageCollection coverages, final IProgressMonitor monitor ) throws CoreException
        {
          Assert.isTrue( domains.length == gridFiles.length );

          final ICoverage[] result = new ICoverage[domains.length];
          for( int i = 0; i < result.length; i++ )
            result[i] = ImportGridUtilities.addCoverage( names[i], domains[i], gridFiles[i], coverages, monitor );

          return result;
        }

      };

      final IStatus status = RunnableContextHelper.execute( getContainer(), true, true, operation );
      ErrorDialog.openError( getShell(), getWindowTitle(), "Fehler beim Rasterdaten-Import", status );
      return status.isOK();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return false;
  }

  protected void setCoverages( final ICoverage[] coverages )
  {
    m_newCoverages = coverages;
  }

  public ICoverage[] getNewCoverages( )
  {
    return m_newCoverages;
  }
}
