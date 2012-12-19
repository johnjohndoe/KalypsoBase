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
package org.kalypso.ui.wizard.wms;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.ui.IWorkbench;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.addlayer.internal.wms.ImportWmsData;
import org.kalypso.ui.addlayer.internal.wms.ImportWmsWizardPage;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.ui.wizard.AbstractDataImportWizard;

/**
 * Wizard for importing WMS sources.
 *
 * @author Kuepferle (original)
 * @author Holger Albert
 */
public class ImportWmsSourceWizard extends AbstractDataImportWizard
{
  /**
   * This constant stores the id for the dialog settings of this page.
   */
  private static String IMPORT_WMS_WIZARD = "IMPORT_WMS_WIZARD"; //$NON-NLS-1$

  private final ImportWmsData m_data = new ImportWmsData();

  public ImportWmsSourceWizard( )
  {
    // FIXME: progress does not work
    setNeedsProgressMonitor( true );
  }

  @Override
  public void setContainer( final IWizardContainer wizardContainer )
  {
    super.setContainer( wizardContainer );

    m_data.setRunnableContext( wizardContainer );
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    initSettings();

    m_data.init( getDialogSettings() );
  }

  /**
   * Make sure, dialog has settings
   */
  private void initSettings( )
  {
    /* Get the dialog settings. */
    final IDialogSettings dialogSettings = getDialogSettings();
    if( dialogSettings != null )
      return;

    /* If not available, add a section inside the settings of the plugin. */
    final IDialogSettings settings = DialogSettingsUtils.getDialogSettings( KalypsoAddLayerPlugin.getDefault(), IMPORT_WMS_WIZARD );
    setDialogSettings( settings );
  }

  @Override
  public void addPages( )
  {
    final String title = Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.1" ); //$NON-NLS-1$
    final ImportWmsWizardPage page = new ImportWmsWizardPage( "WmsImportPage", title, ImageProvider.IMAGE_UTIL_UPLOAD_WIZ, m_data ); //$NON-NLS-1$
    addPage( page );
  }

  @Override
  public boolean performFinish( )
  {
    m_data.storeSettings( getDialogSettings() );

    final IKalypsoLayerModell mapModel = getMapModel();

    final AddWmsThemeOperation operation = new AddWmsThemeOperation( m_data, getCommandTarget(), mapModel );
    // REMARK: do not fork, else we get problems with binding
    final IStatus status = RunnableContextHelper.execute( getContainer(), false, false, operation );
    if( !status.isOK() )
      StatusDialog.open( getShell(), status, getWindowTitle() );

    return !status.matches( IStatus.ERROR );
  }
}