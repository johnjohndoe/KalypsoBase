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
package org.kalypso.gml.ui.commands.importgrid;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;

/**
 * Dieser Wizard dient dazu, (mehrere) Rasterdateien in eine bestehende GML-Datei zu imporiteren.
 * 
 * @author Gernot Belger
 */
public class AddRectifiedGridCoveragesWizard extends Wizard implements IWorkbenchWizard
{
  private PageSelectGeodataFiles m_pageSelect;

  private ICoverageCollection m_coverages;

  private IContainer m_gridFolder;

  private boolean m_allowUserChangeGridFolder;

  private ICoverage[] m_newCoverages;

  public AddRectifiedGridCoveragesWizard( )
  {

    final IDialogSettings settings = DialogSettingsUtils.getDialogSettings( KalypsoGmlUIPlugin.getDefault(), "ImportRectifiedGridCoverageWizardSettings" ); //$NON-NLS-1$
    setDialogSettings( settings );
    setNeedsProgressMonitor( true );

    setWindowTitle( Messages.getString( "org.kalypso.gml.ui.wizard.grid.AddRectifiedGridCoveragesWizard.1" ) ); //$NON-NLS-1$
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    final Object firstElement = selection.getFirstElement();

    final ICoverageCollection cc = findCoverageCollection( firstElement );
    if( cc == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.gml.ui.handler.ImportGridHandler0" ), null ); //$NON-NLS-1$

    // Choose target folder
    final URL gmlContext = cc.getWorkspace().getContext();
    final IFile gmlFile = ResourceUtilities.findFileFromURL( gmlContext );
    final IContainer gmlFolder = gmlFile == null ? null : gmlFile.getParent();

    init( cc, gmlFolder, true );
  }

  private ICoverageCollection findCoverageCollection( final Object firstElement )
  {
    if( firstElement instanceof Feature )
    {
      final Feature fate = (Feature) firstElement;
      return (ICoverageCollection) fate.getAdapter( ICoverageCollection.class );
    }

    return null;
  }

  /**
   * @param gridFolder
   *          The new grid gets imported into this folder. If <code>null</code>, the user will be asked for the folder.
   * @param allowUserChangeGridFolder
   *          If <code>false</code>, the entry field for the grid folder is hidden Resets to <code>true</code>, if
   *          'gridFolder' is null..
   */
  public void init( final ICoverageCollection coverages, final IContainer gridFolder, final boolean allowUserChangeGridFolder )
  {
    m_coverages = coverages;
    m_gridFolder = gridFolder;
    m_allowUserChangeGridFolder = allowUserChangeGridFolder;
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages( )
  {
    m_pageSelect = new PageSelectGeodataFiles( "pageSelect", m_gridFolder, m_allowUserChangeGridFolder ); //$NON-NLS-1$
    m_pageSelect.setTitle( Messages.getString( "org.kalypso.gml.ui.wizard.grid.AddRectifiedGridCoveragesWizard.0" ) ); //$NON-NLS-1$
    m_pageSelect.setDescription( Messages.getString( "org.kalypso.gml.ui.wizard.grid.AddRectifiedGridCoveragesWizard.4" ) ); //$NON-NLS-1$

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
    final IContainer gridFolder = m_pageSelect.getGridFolder();
    try
    {
      final ICoverageCollection coverageCollection = m_coverages;
      final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
      {
        @Override
        public IStatus execute( final IProgressMonitor monitor ) throws CoreException
        {
          final SubMonitor progress = SubMonitor.convert( monitor, Messages.getString( "org.kalypso.gml.ui.wizard.grid.AddRectifiedGridCoveragesWizard.5" ), selectedFiles.length ); //$NON-NLS-1$

          final Collection<ICoverage> newCoverages = new ArrayList<ICoverage>( selectedFiles.length );
          for( final File gridFile : selectedFiles )
            newCoverages.add( ImportGridUtilities.importGrid( coverageCollection, gridFile, gridFile.getName(), crs, gridFolder, null, progress.newChild( 1, SubMonitor.SUPPRESS_NONE ) ) );

          final IFeatureBindingCollection<ICoverage> coverages = coverageCollection.getCoverages();
          setCoverages( coverages.toArray( new ICoverage[coverages.size()] ) );

          return Status.OK_STATUS;
        }
      };

      final IStatus status = RunnableContextHelper.execute( getContainer(), true, true, operation );
      ErrorDialog.openError( getShell(), getWindowTitle(), Messages.getString( "org.kalypso.gml.ui.wizard.grid.AddRectifiedGridCoveragesWizard.6" ), status, IStatus.INFO | IStatus.WARNING | IStatus.ERROR ); //$NON-NLS-1$
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
