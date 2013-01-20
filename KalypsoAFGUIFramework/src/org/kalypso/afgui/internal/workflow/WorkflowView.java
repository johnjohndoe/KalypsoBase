package org.kalypso.afgui.internal.workflow;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.internal.ui.workflow.WorkflowBreadcrumbViewer;
import org.kalypso.afgui.internal.ui.workflow.WorkflowControl;
import org.kalypso.afgui.scenarios.ScenarioHelper;
import org.kalypso.contribs.eclipse.jface.viewers.SelectionProviderAdapter;

import de.renew.workflow.base.IWorkflow;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;
import de.renew.workflow.connector.context.ActiveWorkContext;
import de.renew.workflow.connector.context.IActiveScenarioChangeListener;

/**
 * @author Patrice Congo, Stefan Kurzbach
 */
public class WorkflowView extends ViewPart
{
  final static public String ID = "org.kalypso.kalypso1d2d.pjt.views.WorklistView"; //$NON-NLS-1$

  private WorkflowControl m_workflowControl;

  private ActiveWorkContext m_activeWorkContext;

  private WorkflowBreadcrumbViewer m_breadcrumbViewer;

  private final IActiveScenarioChangeListener m_contextListener = new IActiveScenarioChangeListener()
  {
    @Override
    public void activeScenarioChanged( final ScenarioHandlingProjectNature newProject, final IScenario scenario )
    {
      handleScenarioChanged( newProject, scenario );
    }
  };

  @Override
  public void createPartControl( final Composite parent )
  {
    final Composite panel = new Composite( parent, SWT.NONE );
    GridLayoutFactory.fillDefaults().spacing( 0, 0 ).applyTo( panel );

    m_breadcrumbViewer = new WorkflowBreadcrumbViewer( panel, this );
    m_breadcrumbViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    m_workflowControl.createControl( panel );
    m_workflowControl.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    handleScenarioChanged( m_activeWorkContext.getCurrentProject(), m_activeWorkContext.getCurrentCase() );

    final SelectionProviderAdapter selectionProvider = new SelectionProviderAdapter();
    getSite().setSelectionProvider( selectionProvider );
  }

  protected void handleScenarioChanged( final ScenarioHandlingProjectNature newProject, final IScenario scenario )
  {
    final UIJob job = new UIJob( Messages.getString( "org.kalypso.afgui.views.WorkflowView.2" ) ) //$NON-NLS-1$
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        doUpdateControls( newProject, scenario );

        return Status.OK_STATUS;
      }
    };

    job.setUser( false );

    // REMARK: tricky: if we immediately execute, the progress dialog for activating the scenario may still be open
    // I this case, we get errors when activating the views (during task activation), because the workflow window
    // is not active right now.
    job.schedule( 0 );
  }

  void doUpdateControls( final ScenarioHandlingProjectNature newProject, final IScenario scenario )
  {
    m_breadcrumbViewer.setScenario( scenario );

    final IWorkflow workflow = ScenarioHelper.findWorkflow( scenario, newProject );
    m_workflowControl.setWorkflow( workflow );
  }

  @Override
  public void dispose( )
  {
    m_activeWorkContext.removeActiveContextChangeListener( m_contextListener );
    super.dispose();
  }

  @Override
  public void init( final IViewSite site, final IMemento memento ) throws PartInitException
  {
    super.init( site, memento );

    m_activeWorkContext = KalypsoAFGUIFrameworkPlugin.getActiveWorkContext();
    m_activeWorkContext.addActiveContextChangeListener( m_contextListener );
    m_workflowControl = new WorkflowControl( KalypsoAFGUIFrameworkPlugin.getTaskExecutor() );
  }

  @Override
  public void setFocus( )
  {
    if( m_workflowControl != null )
    {
      m_workflowControl.setFocus();
    }
  }
}
