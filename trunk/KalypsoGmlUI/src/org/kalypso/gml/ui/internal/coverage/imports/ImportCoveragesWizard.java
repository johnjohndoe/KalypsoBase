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
package org.kalypso.gml.ui.internal.coverage.imports;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.coverage.CoverageManagementAction;
import org.kalypso.gml.ui.coverage.ImportCoverageData;
import org.kalypso.gml.ui.coverage.ImportCoveragesOperation;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;

/**
 * Dieser Wizard dient dazu, (mehrere) Rasterdateien in eine bestehende GML-Datei zu imporiteren.
 * 
 * @author Gernot Belger
 */
public class ImportCoveragesWizard extends Wizard implements IWorkbenchWizard
{
  private final ImportCoverageData m_data;

  private PageSelectGeodataFiles m_pageSelect;

  public ImportCoveragesWizard( )
  {
    m_data = new ImportCoverageData();
    m_pageSelect = null;

    final IDialogSettings settings = DialogSettingsUtils.getDialogSettings( KalypsoGmlUIPlugin.getDefault(), "ImportRectifiedGridCoverageWizardSettings" ); //$NON-NLS-1$
    setDialogSettings( settings );
    setNeedsProgressMonitor( true );
    setWindowTitle( Messages.getString( "org.kalypso.gml.ui.wizard.grid.AddRectifiedGridCoveragesWizard.1" ) ); //$NON-NLS-1$
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    m_data.initFromSelection( selection );
  }

  /**
   * @param dataFolder
   *          The new data files get imported into this folder. If <code>null</code>, the user will be asked for the
   *          folder.
   * @param allowUserChangeDataFolder
   *          If <code>false</code>, the entry field for the grid folder is hidden Resets to <code>true</code>, if
   *          'gridFolder' is null..
   */
  public void init( final ICoverageCollection coverages, final IContainer dataFolder, final boolean allowUserChangeDataFolder )
  {
    m_data.init( coverages, dataFolder, allowUserChangeDataFolder );
    m_data.loadSettings( getDialogSettings() );
  }

  @Override
  public void addPages( )
  {
    m_pageSelect = new PageSelectGeodataFiles( m_data );
    m_pageSelect.setTitle( Messages.getString( "org.kalypso.gml.ui.wizard.grid.AddRectifiedGridCoveragesWizard.0" ) ); //$NON-NLS-1$
    m_pageSelect.setDescription( Messages.getString( "org.kalypso.gml.ui.wizard.grid.AddRectifiedGridCoveragesWizard.4" ) ); //$NON-NLS-1$

    addPage( m_pageSelect );

    super.addPages();
  }

  @Override
  public boolean performFinish( )
  {
    /* Save the dialog settings. */
    m_data.storeSettings( getDialogSettings() );

    /* Execute the import coverages operation. */
    final ImportCoveragesOperation operation = new ImportCoveragesOperation( m_data );
    final IStatus operationStatus = RunnableContextHelper.execute( getContainer(), true, true, operation );
    if( !operationStatus.isOK() )
    {
      StatusDialog.open( getShell(), operationStatus, getWindowTitle() );
      return true;
    }

    /* Execute additional actions. */
    final CoverageManagementAction[] checkedActions = m_pageSelect.getCheckedActions();
    for( final CoverageManagementAction checkedAction : checkedActions )
    {
      /* Init the checked action. */
      checkedAction.init( getShell(), m_data );

      /* Get the action. */
      final IAction action = checkedAction.getAction();

      /* Execute the action. */
      action.run();
    }

    // On error, we might have imported some coverages and some not, so we always leave the wizard.
    return true;
  }

  public ICoverage[] getNewCoverages( )
  {
    return m_data.getNewCoverages();
  }
}