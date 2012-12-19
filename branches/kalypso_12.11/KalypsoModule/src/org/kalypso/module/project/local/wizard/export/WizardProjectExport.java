/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.module.project.local.wizard.export;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.module.internal.i18n.Messages;

/**
 * Exports a given project as zip file into the file system.
 * 
 * @author Dirk Kuch
 */
public class WizardProjectExport extends Wizard implements IWorkbenchWizard
{
  private PageSelectExportDestination m_pageSelectZipFile;

  private final IProject m_project;

  private boolean m_useTargetNameAsProjectName = false;

  public WizardProjectExport( final IProject project )
  {
    setWindowTitle( Messages.getString( "org.kalypso.core.projecthandle.local.exportwizard.WizardProjectExport.0" ) ); //$NON-NLS-1$
    setHelpAvailable( false );

    m_project = project;
  }

  /**
   * If set to <code>true</code>, the name of the target file will be used as the exported project name.<br>
   * I.e. if the project is re-imported, eclipse will find that name instead of the old project name.
   */
  public void setUseTargetNameAsProjectName( final boolean useTargetNameAsProjectName )
  {
    m_useTargetNameAsProjectName = useTargetNameAsProjectName;
  }

  @Override
  public void addPages( )
  {
    m_pageSelectZipFile = new PageSelectExportDestination();
    addPage( m_pageSelectZipFile );
  }

  /**
   * Both parameters are ignored.
   * 
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
   *      org.eclipse.jface.viewers.IStructuredSelection)
   */
  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
  }

  @Override
  public boolean performFinish( )
  {
    final File selectedFile = m_pageSelectZipFile.getSelectedFile();
    if( selectedFile.exists() )
    {
      final String message = String.format( Messages.getString("WizardProjectExport.0"), selectedFile.getName() ); //$NON-NLS-1$
      if( !MessageDialog.openConfirm( getShell(), getWindowTitle(), message ) )
        return false;
    }

    final ProjectExportWorker worker = new ProjectExportWorker( m_project, selectedFile, m_useTargetNameAsProjectName );
    // FIXME: show warning dialog if export file already exists. But allow it, if the user wishes so.

    final IStatus result = ProgressUtilities.busyCursorWhile( worker );
    ErrorDialog.openError( getShell(), getWindowTitle(), Messages.getString("WizardProjectExport.1"), result ); //$NON-NLS-1$

    return result.isOK();
  }
}