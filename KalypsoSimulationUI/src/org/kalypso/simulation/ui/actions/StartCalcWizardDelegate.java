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
package org.kalypso.simulation.ui.actions;

import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.kalypso.contribs.eclipse.core.resources.ProjectUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.jface.wizard.view.IWizardContainerListener;
import org.kalypso.contribs.eclipse.jface.wizard.view.WizardContainerAdapter;
import org.kalypso.contribs.eclipse.jface.wizard.view.WizardView;
import org.kalypso.contribs.eclipse.ui.PartAdapter2;
import org.kalypso.simulation.ui.startscreen.PrognosePerspective;
import org.kalypso.simulation.ui.wizards.calculation.CalcWizard;

/**
 * Action Delegate zum Starten des Berechnungs-Wizards
 * 
 * @author belger
 */
public class StartCalcWizardDelegate implements IWorkbenchWindowActionDelegate
{
  protected final Logger m_logger = Logger.getLogger( this.getClass().getName() );

  protected IWorkbenchWindow m_window;

  /**
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose()
  {
  // nix passiert
  }

  /**
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  public void init( final IWorkbenchWindow window )
  {
    m_window = window;
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run( final IAction action )
  {
    final ISelection selection = m_window.getSelectionService().getSelection( IPageLayout.ID_RES_NAV );
    final IProject[] projects = ProjectUtilities.findProjectsFromSelection( selection );

    if( projects == null || projects.length != 1 )
    {
      MessageDialog.openInformation( m_window.getShell(), "Hochwasser Vorhersage durchführen",
          "Bitte wählen Sie genau ein Projekt im Navigator aus" );
      return;
    }

    openCalculactionWizardOnProject( m_window, projects[0] );
  }

  public static void openCalculactionWizardOnProject( final IWorkbenchWindow workbenchWindow, final IProject project )
  {
    final CalcWizard wizard = new CalcWizard( project );

    final ICoreRunnableWithProgress runnable = new ICoreRunnableWithProgress()
    {
      public IStatus execute( final IProgressMonitor monitor ) throws CoreException
      {
        wizard.initWizard( monitor );
        return Status.OK_STATUS;
      }
    };
    final IStatus status = RunnableContextHelper.execute( workbenchWindow, false, true, runnable );
    if( !status.isOK() )
    {
      ErrorDialog.openError( workbenchWindow.getShell(), "Vorhersage Assistent",
          "Der Vorhersage Assistent konnte nicht initialisiert werden.", status );
      return;
    }

    // remeber old perspective
    final IWorkbenchPage activePage = workbenchWindow.getActivePage();

    // change to wizard perspective
    final IPerspectiveRegistry perspectiveRegistry = workbenchWindow.getWorkbench().getPerspectiveRegistry();
    final IPerspectiveDescriptor wizardPerspective = perspectiveRegistry
        .findPerspectiveWithId( PrognosePerspective.class.getName() );

    final IPerspectiveDescriptor lastPerspective = activePage.getPerspective();

    activePage.setPerspective( wizardPerspective );

    try
    {
      final String wizardViewId = WizardView.class.getName();

      // alle vorhandenen views verstecken (ausser es ist gerade die wizard-view)
      final IViewReference[] viewReferences = activePage.getViewReferences();
      for( int i = 0; i < viewReferences.length; i++ )
      {
        final IViewReference reference = viewReferences[i];
        if( !reference.getId().equals( wizardViewId ) && activePage.isPartVisible( reference.getPart( false ) ) )
          activePage.hideView( reference );
        else
          viewReferences[i] = null;
      }

      final IViewPart showView = activePage.showView( wizardViewId );
      if( showView instanceof WizardView )
      {
        final WizardView wizardView = (WizardView)showView;
        wizardView.setBackJumpsToLastVisited( false );
        wizardView.setErrorBackgroundBehaviour( true );
        wizardView.setButtonLabel( IDialogConstants.FINISH_ID, "Be&enden" );
        wizardView.setWizard( wizard );
        
        final ImageDescriptor imageDescriptor = wizardPerspective.getImageDescriptor();
        wizardView.setTitleImage( imageDescriptor );

        // if wizard finishes, close view
        final WizardContainerAdapter wizardContainerAdapter = new WizardContainerAdapter()
        {
          /**
           * @see org.kalypso.contribs.eclipse.jface.wizard.view.WizardContainerAdapter#onWizardChanged(org.eclipse.jface.wizard.IWizard,
           *      int)
           */
          public void onWizardChanged( final IWizard newwizard, final int reason )
          {
            if( reason == IWizardContainerListener.REASON_FINISHED
                || reason == IWizardContainerListener.REASON_CANCELED )
            {
              activePage.hideView( wizardView );
            }
          }
        };

        // reset perspective, if wizard-view closes
        activePage.addPartListener( new PartAdapter2()
        {
          /**
           * @see org.kalypso.contribs.eclipse.ui.PartAdapter2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
           */
          public void partClosed( final IWorkbenchPartReference partRef )
          {
            final IWorkbenchPart part = partRef.getPart( false );
            if( part == wizardView )
            {
              activePage.removePartListener( this );
              wizardView.removeWizardContainerListener( wizardContainerAdapter );

              // TODO: gives null-point exception, if workbench is closing
              // in 3.1 there will be a method to check this 'isClosing'
              try
              {
                activePage.resetPerspective();
                activePage.setPerspective( lastPerspective );
              }
              catch( final NullPointerException ignored )
              {
                // TODO: remove this exception once the problem with 'is closing' is solved
              }
            }
          }

        } );

        wizardView.addWizardContainerListener( wizardContainerAdapter );
      }
    }
    catch( final PartInitException e )
    {
      // error message?
      e.printStackTrace();
    }
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   *      org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged( final IAction action, final ISelection selection )
  {
  // mir wurscht
  }

}