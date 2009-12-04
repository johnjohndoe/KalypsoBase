/*--------------- Kalypso-Header --------------------------------------------------------------------

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
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.simulation.ui.wizards.createCalcCase;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.simulation.ui.actions.CalcCaseHelper;
import org.kalypso.simulation.ui.calccase.ModelNature;
import org.kalypso.simulation.ui.i18n.Messages;
import org.kalypso.ui.ImageProvider;

/**
 * @author belger
 */
public class NewCalculationCaseWizard extends BasicNewResourceWizard
{
  private NewCalculationCaseCreateFolderPage m_createFolderPage;

  private SteuerparameterWizardPage m_createControlPage;

  protected IFolder m_newFolderHandle;

  /**
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
   *      org.eclipse.jface.viewers.IStructuredSelection)
   */
  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection currentSelection )
  {
    super.init( workbench, currentSelection );
    setWindowTitle(Messages.getString("org.kalypso.simulation.ui.wizards.createCalcCase.NewCalculationCaseWizard.0") ); //$NON-NLS-1$
    setNeedsProgressMonitor( true );
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#addPages()
   */
  @Override
  public void addPages( )
  {
    super.addPages();
    m_createFolderPage = new NewCalculationCaseCreateFolderPage(Messages.getString("org.kalypso.simulation.ui.wizards.createCalcCase.NewCalculationCaseWizard.1"), getSelection() ); //$NON-NLS-1$
    m_createControlPage = new SteuerparameterWizardPage( m_createFolderPage, ImageProvider.IMAGE_KALYPSO_ICON_BIG, false )
    {
      /**
       * @see org.kalypso.simulation.ui.wizards.createCalcCase.SteuerparameterWizardPage#createControl(org.eclipse.swt.widgets.Composite)
       */
      @Override
      public void createControl( final Composite parent )
      {
        m_newFolderHandle = createCalculationCase();

        setFolder( m_newFolderHandle );

        super.createControl( parent );
      }
    };

    m_createControlPage.setUpdate( true );

    addPage( m_createFolderPage );
    addPage( m_createControlPage );
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    // zuerst die Grunddaten erzeugen erzeugen
    final IFolder newFolderHandle = m_createFolderPage.getFolder();
    final SteuerparameterWizardPage controlPage = m_createControlPage;

    final WorkspaceModifyOperation op = new WorkspaceModifyOperation( null )
    {
      @Override
      public void execute( final IProgressMonitor monitor ) throws CoreException
      {
        monitor.beginTask( "Neue Rechenvariante erzeugen - ", 1000 );
        monitor.subTask( "" ); // Hack, else the begin task will not be set here

        controlPage.saveChanges( newFolderHandle, new SubProgressMonitor( monitor, 100 ) );

        if( controlPage.isUpdate() )
        {
          final ModelNature nature = (ModelNature) newFolderHandle.getProject().getNature( ModelNature.ID );
          final IStatus updateStatus = nature.updateCalcCase( newFolderHandle, new SubProgressMonitor( monitor, 900 ) );
          if( !updateStatus.isOK() )
            throw new CoreException( updateStatus );
        }
        else
        {
          monitor.worked( 1000 );
        }
      }
    };

    executeOp( op, newFolderHandle );

    // im Navigator zeigen
    selectAndReveal( newFolderHandle );

    return true;
  }

  protected IFolder createCalculationCase( )
  {
    final IFolder newFolderHandle = m_createFolderPage.getFolder();

    final Map<String, Object> antProperties = CalcCaseHelper.configureAntProperties( null );

    final WorkspaceModifyOperation op = new WorkspaceModifyOperation( null )
    {
      @Override
      public void execute( final IProgressMonitor monitor ) throws CoreException
      {
        try
        {
          monitor.beginTask( IDEWorkbenchMessages.WizardNewFolderCreationPage_progress, 4000 );
          final ContainerGenerator generator = new ContainerGenerator( newFolderHandle.getParent().getFullPath() );
          generator.generateContainer( new SubProgressMonitor( monitor, 1000 ) );
          createFolder( newFolderHandle, new SubProgressMonitor( monitor, 1000 ) );
          final ModelNature nature = (ModelNature) newFolderHandle.getProject().getNature( ModelNature.ID );
          final IStatus createStatus = nature.createCalculationCaseInFolder( newFolderHandle, antProperties, new SubProgressMonitor( monitor, 1000 ) );
          if( createStatus.matches( IStatus.ERROR ) )
            throw new CoreException( createStatus );
        }
        finally
        {
          monitor.done();
        }
      }
    };

    if( executeOp( op, newFolderHandle ) )
      return newFolderHandle;

    return null;
  }

  private boolean executeOp( final WorkspaceModifyOperation op, final IFolder newFolderHandle )
  {
    try
    {
      getContainer().run( true, true, op );
    }
    catch( final InterruptedException e )
    {
      cleanup( newFolderHandle );

      return false;
    }
    catch( final InvocationTargetException e )
    {
      if( e.getTargetException() instanceof CoreException )
      {
        final IStatus status = ((CoreException) e.getTargetException()).getStatus();
        StatusUtilities.openSpecialErrorDialog( getContainer().getShell(), IDEWorkbenchMessages.WizardNewFolderCreationPage_errorTitle, null, status, IStatus.ERROR, true );

        // TODO: even ignore error, else we get a problem if no raster data is available
// if( status.matches( IStatus.ERROR ) )
// {
// e.printStackTrace();
// cleanup( newFolderHandle );
// return false;
// }
      }
      else
      {
        // CoreExceptions are handled above, but unexpected runtime exceptions
        // and errors may still occur.
        e.printStackTrace();

        IDEWorkbenchPlugin.log( MessageFormat.format( "Exception in {0}.getNewFolder(): {1}", new Object[] { getClass().getName(), e.getTargetException() } ) );//$NON-NLS-1$
        MessageDialog.openError( getContainer().getShell(), IDEWorkbenchMessages.WizardNewFolderCreationPage_internalErrorTitle, NLS.bind( IDEWorkbenchMessages.WizardNewFolder_internalError, e.getTargetException().getMessage() ) );

        cleanup( newFolderHandle );
        return false; // ie.- one of the steps resulted in a core exception
      }
    }

    return true;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#performCancel()
   */
  @Override
  public boolean performCancel( )
  {
    if( m_newFolderHandle != null )
      cleanup( m_newFolderHandle );

    return true;
  }

  private void cleanup( final IFolder folder )
  {
    try
    {
      folder.delete( true, new NullProgressMonitor() );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }
  }

  /**
   * Creates a folder resource given the folder handle.
   * 
   * @param folderHandle
   *          the folder handle to create a folder resource for
   * @param monitor
   *          the progress monitor to show visual progress with
   * @exception CoreException
   *              if the operation fails
   * @exception OperationCanceledException
   *              if the operation is canceled TODO: move this code to FolderUtilities
   */
  protected void createFolder( final IFolder folderHandle, final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      // Create the folder resource in the workspace
      // Update: Recursive to create any folders which do not exist already
      if( !folderHandle.exists() )
      {
        final IContainer parent = folderHandle.getParent();
        if( parent instanceof IFolder && (!((IFolder) parent).exists()) )
          createFolder( (IFolder) parent, monitor );

        folderHandle.create( false, true, monitor );
      }
    }
    catch( final CoreException e )
    {
      // If the folder already existed locally, just refresh to get contents
      if( e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED )
        folderHandle.refreshLocal( IResource.DEPTH_INFINITE, new SubProgressMonitor( monitor, 500 ) );
      else
        throw e;
    }

    if( monitor.isCanceled() )
      throw new OperationCanceledException();
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#createPageControls(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPageControls( final Composite pageContainer )
  {
    // nichts tun, die Seiten sollen lazy initialisiert werden
    // weil die Steuerparameter Seite erst aufgebaut werden kann,
    // wenn das Projekt festliegt
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#canFinish()
   */
  @Override
  public boolean canFinish( )
  {
    return getContainer().getCurrentPage() == m_createControlPage;
  }
}