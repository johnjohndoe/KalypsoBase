package org.kalypso.ui.action;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.kalypso.ui.nature.ModelNature;

/**
 * @author belger
 */
public class StartCalculationActionDelegate implements IWorkbenchWindowActionDelegate
{
  private IWorkbenchWindow m_window;

  /**
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose()
  {
  // nix zu tun?
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

    final IFolder[] calcCasesToCalc = CalcCaseHelper.chooseCalcCases( m_window.getShell(), selection, "Berechnung starten", "Folgende Rechenvarianten werden berechnet:" );
    
    if( calcCasesToCalc == null )
      return;
    
    for( int i = 0; i < calcCasesToCalc.length; i++ )
    {
      final IFolder folder = calcCasesToCalc[i];

      final Job job = new Job( "Berechne: " + folder.getName() )
      {
        /**
         * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        protected IStatus run( final IProgressMonitor monitor )
        {
          try
          {
            final ModelNature nature = (ModelNature)folder.getProject().getNature( ModelNature.ID );
            nature.runCalculation( folder, monitor );
          }
          catch( final CoreException e )
          {
            e.printStackTrace();

            return e.getStatus();
          }

          return Status.OK_STATUS;
        }
      };
      job.setUser( true );
      job.schedule();
    }
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   *      org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged( final IAction action, final ISelection selection )
  {
    // mir doch egal!
  }

}