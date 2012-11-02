/*--------------- Kalypso-Header ------------------------------------------

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

 --------------------------------------------------------------------------*/

package org.kalypso.metadoc.ui;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.contribs.java.lang.DisposeHelper;
import org.kalypso.metadoc.IExportTarget;
import org.kalypso.metadoc.IExportableObjectFactory;
import org.kalypso.metadoc.KalypsoMetaDocPlugin;
import org.kalypso.metadoc.configuration.PublishingConfiguration;

/**
 * The export wizard takes care of creating the pages using the given target and exportable-object-factory.
 *
 * @author schlienger
 */
public final class ExportWizard extends Wizard
{
  private final Shell m_shell;

  private final WorkspaceModifyOperation m_operation;

  private final IExportTarget m_target;

  public ExportWizard( final IExportTarget target, final IExportableObjectFactory factory, final Shell shell, final ImageDescriptor defaultImage, final String windowTitle ) throws CoreException
  {
    m_target = target;
    m_shell = shell;

    setNeedsProgressMonitor( true );
    setWindowTitle( windowTitle );

    final PublishingConfiguration configuration = new PublishingConfiguration();

    // one settings-entry per target and factory
    final String settingsName = target.getClass().toString() + "_" + factory.getClass().toString(); //$NON-NLS-1$

    final IDialogSettings workbenchSettings = KalypsoMetaDocPlugin.getDefault().getDialogSettings();
    IDialogSettings section = workbenchSettings.getSection( settingsName );
    if( section == null )
      section = workbenchSettings.addNewSection( settingsName );
    setDialogSettings( section );

    // use the target image as default image for this wizard

    final ICoreRunnableWithProgress initOperation = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws CoreException
      {
        monitor.beginTask( "Initialisiere Dokumente", IProgressMonitor.UNKNOWN );

        // TODO: slow
        final IWizardPage[] factoryPages = factory.createWizardPages( configuration, defaultImage );

        final IWizardPage[] targetPages = target.createWizardPages( configuration );
        for( final IWizardPage factoryPage : factoryPages )
          addPage( factoryPage );
        for( final IWizardPage targetPage : targetPages )
          addPage( targetPage );

        monitor.done();

        return Status.OK_STATUS;
      }
    };
    final IStatus status = ProgressUtilities.busyCursorWhile( initOperation );
    if( !status.isOK() )
      throw new CoreException( status );

    // operation which will be called for finish
    m_operation = new ExportDocumentsOperation( factory, configuration, target );
  }

  @Override
  public void dispose( )
  {
    new DisposeHelper( getPages() ).dispose();

    super.dispose();
  }

  @Override
  public boolean performFinish( )
  {
    final IStatus status = RunnableContextHelper.execute( getContainer(), true, true, m_operation );
    final Throwable exception = status.getException();
    if( exception != null && !status.isOK() )
      exception.printStackTrace();
    ErrorDialog.openError( m_shell, m_target.getName(), "Export-Probleme", status );

    return true;
  }
}