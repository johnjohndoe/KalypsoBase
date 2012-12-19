package org.kalypso.afgui.internal.ui.workflow;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.internal.i18n.Messages;

import de.renew.workflow.base.ITask;
import de.renew.workflow.base.IWorkflow;
import de.renew.workflow.connector.IWorklistChangeListener;
import de.renew.workflow.connector.worklist.ITaskExecutionListener;
import de.renew.workflow.connector.worklist.ITaskExecutor;

/**
 * @author Stefan Kurzbach
 */
public class WorkflowControl implements IWorklistChangeListener, ITaskExecutionListener
{
  private TreeViewer m_treeViewer;

  private TreePath m_lastTreePath;

  private Object m_lastSelectedElement;

  private final ITaskExecutor m_taskExecutor;

  public WorkflowControl( final ITaskExecutor taskExecutor )
  {
    m_taskExecutor = taskExecutor;

    taskExecutor.addTaskExecutionListener( this );
  }

  public ITaskExecutor getTaskExecutor( )
  {
    return m_taskExecutor;
  }

  public void createControl( final Composite parent )
  {
    m_treeViewer = new TreeViewer( parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION );

    // enable tooltips per cell
    ColumnViewerToolTipSupport.enableFor( m_treeViewer );

    // Content provider
    m_treeViewer.setContentProvider( new WorkflowContentProvider() );
    // Label provider
    m_treeViewer.setLabelProvider( new WorkflowLabelProvider( this ) );

    final ITaskExecutor taskExecutor = m_taskExecutor;
    m_treeViewer.getControl().addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        taskExecutor.removeTaskExecutionListener( WorkflowControl.this );
      }
    } );

    // Listen to open events
    m_treeViewer.addOpenListener( new IOpenListener()
    {
      @Override
      public void open( final OpenEvent event )
      {
        final ITreeSelection selection = (ITreeSelection) event.getSelection();
        final Object first = selection.getFirstElement();
        if( first != null )
        {
          if( first instanceof ITask )
          {
            final ITask task = (ITask) first;
            doTask( task );
          }
        }
      }
    } );

    // listen to select events
    m_treeViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        handleSelectionChanged( event );
      }
    } );
  }

  public Control getControl( )
  {
    return m_treeViewer.getControl();
  }

  final void doTask( final ITask task )
  {
    if( m_treeViewer.getTree().isDisposed() )
      return;

    final IStatus result = m_taskExecutor.execute( task );
    if( result.matches( IStatus.CANCEL ) )
    {
      /* Task is probably a task group with no own functionality -> just toggle expansion state */
      m_treeViewer.setExpandedState( task, !m_treeViewer.getExpandedState( task ) );
    }

    // TODO: error handling should be done by the task executor!; why isn't there a job?
    final Shell shell = m_treeViewer.getControl().getShell();
    final String title = Messages.getString( "org.kalypso.afgui.views.WorkflowControl.2" );//$NON-NLS-1$
    final String message = Messages.getString( "org.kalypso.afgui.views.WorkflowControl.3" ); //$NON-NLS-1$
    if( !result.isOK() && !result.matches( IStatus.CANCEL ) )
    {
      KalypsoAFGUIFrameworkPlugin.getDefault().getLog().log( result );
    }
    ErrorDialog.openError( shell, title, message + task.getName(), result, IStatus.WARNING | IStatus.ERROR );
  }

  public void setWorkflow( final IWorkflow workflow )
  {
    if( m_treeViewer != null && !m_treeViewer.getControl().isDisposed() )
    {
      m_treeViewer.setInput( workflow );
      m_treeViewer.collapseAll();

      final ITask activeTask = m_taskExecutor.getActiveTask();
      if( workflow != null && activeTask != null )
      {
        selectTask( workflow, activeTask );
      }
    }
  }

  @Override
  public void worklistChanged( )
  {
    if( m_treeViewer.getTree().isDisposed() )
    {
      return;
    }

    m_treeViewer.refresh();
  }

  public void setFocus( )
  {
    if( m_treeViewer != null && !m_treeViewer.getControl().isDisposed() )
    {
      m_treeViewer.getControl().setFocus();
    }
  }

  @Override
  public void handleActiveTaskChanged( final IStatus result, final ITask previouslyActive, final ITask activeTask )
  {
    final TreeViewer treeViewer = m_treeViewer;
    if( treeViewer == null )
      return;

    final Control control = treeViewer.getControl();
    if( control == null || control.isDisposed() )
      return;

    new UIJob( "" ) //$NON-NLS-1$
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        if( control.isDisposed() )
          return Status.OK_STATUS;

        final IWorkflow workflow = (IWorkflow) treeViewer.getInput();
        if( workflow != null && activeTask != null )
          selectTask( workflow, activeTask );

        if( previouslyActive != null )
          treeViewer.update( previouslyActive, null );

        if( activeTask != null )
          treeViewer.update( activeTask, null );

        return Status.OK_STATUS;
      }
    }.schedule();
  }

  protected void handleSelectionChanged( final SelectionChangedEvent event )
  {
    if( m_treeViewer.getTree().isDisposed() )
    {
      return;
    }

    final ITreeSelection selection = (ITreeSelection) event.getSelection();
    final Object first = selection.getFirstElement();
    if( first != null && m_lastSelectedElement != first )
    {
      final TreePath newTreePath = selection.getPathsFor( first )[0];
      m_lastSelectedElement = null;
      if( m_lastTreePath != null )
      {
        final int segmentCount = m_lastTreePath.getSegmentCount();
        final int newSegmentCount = newTreePath.getSegmentCount();
        final Object segment = m_lastTreePath.getSegment( Math.min( segmentCount, newSegmentCount ) - 1 );
        final TreePath longerPath;
        final TreePath shorterPath;
        if( segmentCount > newSegmentCount )
        {
          longerPath = m_lastTreePath;
          shorterPath = newTreePath;
        }
        else
        {
          longerPath = newTreePath;
          shorterPath = m_lastTreePath;
        }
        if( !longerPath.startsWith( shorterPath, null ) )
        {
          m_treeViewer.collapseToLevel( segment, AbstractTreeViewer.ALL_LEVELS );
        }
        m_lastSelectedElement = first;
      }
      m_treeViewer.expandToLevel( first, 1 );
      m_lastTreePath = newTreePath;
      m_treeViewer.setSelection( new StructuredSelection( first ) );
    }
  }

  protected void selectTask( final IWorkflow workflow, final ITask task )
  {
    final TreePath findPart = TaskHelper.findPart( task.getURI(), workflow );
    if( findPart != null )
    {
      final TreeSelection newSelection = new TreeSelection( findPart );
      m_treeViewer.setSelection( newSelection, true );
    }
  }

  public TreeViewer getTreeViewer( )
  {
    return m_treeViewer;
  }

  public IWorkflow getWorkflow( )
  {
    final Object input = m_treeViewer.getInput();
    if( input instanceof IWorkflow )
      return (IWorkflow) input;

    return null;
  }
}
