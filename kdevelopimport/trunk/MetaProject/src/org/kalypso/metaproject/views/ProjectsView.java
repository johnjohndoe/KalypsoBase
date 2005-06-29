package org.kalypso.metaproject.views;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.metaproject.ImageProvider;
import org.kalypso.metaproject.MetaProject;
import org.kalypso.metaproject.ProjectsAnalyser;

/**
 * ProjectsView
 */
public class ProjectsView extends ViewPart
{
  private TreeViewer m_viewer;

  private Action m_actionRefresh;

  private Action doubleClickAction;

  private Text m_text;

  private Button m_btnSave;

  /**
   * The constructor.
   */
  public ProjectsView()
  {}

  /**
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {}

  /**
   * This is a callback that will allow us to create the viewer and initialize it.
   */
  public void createPartControl( Composite parent )
  {
    final SashForm sashForm = new SashForm( parent, SWT.HORIZONTAL );

    m_viewer = new TreeViewer( sashForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
    m_viewer.setContentProvider( new MetaProjectTreeContentProvider() );
    m_viewer.setLabelProvider( new MetaProjectLabelProvider() );
    m_viewer.getControl().setToolTipText( "Double-click opens the Project-Properties dialog" );

    Composite cmpDesc = new Composite( sashForm, SWT.NONE );
    cmpDesc.setLayout( new GridLayout( 2, false ) );

    final Label lbl = new Label( cmpDesc, SWT.NONE );
    lbl.setText( "Project Description:" );

    m_btnSave = new Button( cmpDesc, SWT.FLAT );
    m_btnSave.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_END ) );
    m_btnSave.setText( "Save" );
    m_btnSave.setToolTipText( "Saves the project description in the file" );
    m_btnSave.setEnabled( false );

    m_text = new Text( cmpDesc, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
    final GridData gdText = new GridData( GridData.FILL_BOTH );
    gdText.horizontalSpan = 2;
    m_text.setLayoutData( gdText );

    m_viewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      public void selectionChanged( SelectionChangedEvent event )
      {
        final MetaProject mp = (MetaProject)( (IStructuredSelection)event.getSelection() ).getFirstElement();

        if( mp != null )
          m_text.setText( mp.getDescription() );
        else
          m_text.setText( "" );

        m_btnSave.setEnabled( false );
      }
    } );

    m_btnSave.addSelectionListener( new SelectionListener()
    {
      public void widgetSelected( SelectionEvent e )
      {
        final MetaProject mp = (MetaProject)( (IStructuredSelection)m_viewer.getSelection() ).getFirstElement();
        if( mp != null )
        {
          mp.setDescription( m_text.getText() );
          m_btnSave.setEnabled( false );
        }
      }

      public void widgetDefaultSelected( SelectionEvent e )
      {
      // empty
      }
    } );

    m_text.addModifyListener( new ModifyListener()
    {
      public void modifyText( ModifyEvent e )
      {
        m_btnSave.setEnabled( true );
      }
    } );

    refreshProjectsList();

    makeActions();
    hookContextMenu();
    hookDoubleClickAction();
    contributeToActionBars();
  }

  private void hookContextMenu()
  {
    MenuManager menuMgr = new MenuManager();
    menuMgr.setRemoveAllWhenShown( true );
    menuMgr.addMenuListener( new IMenuListener()
    {
      public void menuAboutToShow( IMenuManager manager )
      {
        ProjectsView.this.fillContextMenu( manager );
      }
    } );
    Menu menu = menuMgr.createContextMenu( m_viewer.getControl() );
    m_viewer.getControl().setMenu( menu );
    getSite().registerContextMenu( menuMgr, m_viewer );
  }

  private void contributeToActionBars()
  {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalPullDown( bars.getMenuManager() );
    fillLocalToolBar( bars.getToolBarManager() );
  }

  private void fillLocalPullDown( IMenuManager manager )
  {
    manager.add( m_actionRefresh );
  }

  private void fillContextMenu( IMenuManager manager )
  {
    manager.add( m_actionRefresh );
    manager.add( new Separator() );
    // Other plug-ins can contribute there actions here
    manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
  }

  private void fillLocalToolBar( IToolBarManager manager )
  {
    manager.add( m_actionRefresh );
    manager.add( new Separator() );
  }

  private void makeActions()
  {
    m_actionRefresh = new Action()
    {
      public void run()
      {
        refreshProjectsList();
      }
    };
    m_actionRefresh.setText( "Refresh" );
    m_actionRefresh.setToolTipText( "Refresh Projects List" );
    m_actionRefresh.setImageDescriptor( ImageProvider.IMAGE_REFRESH );

    doubleClickAction = new Action()
    {
      public void run()
      {
        final ISelection selection = m_viewer.getSelection();
        final MetaProject pd = (MetaProject)( (IStructuredSelection)selection ).getFirstElement();

        final ISelectionProvider selpro = new ISelectionProvider()
        {
          public void addSelectionChangedListener( ISelectionChangedListener listener )
          {}

          public ISelection getSelection()
          {
            return new StructuredSelection( pd.getProject() );
          }

          public void removeSelectionChangedListener( ISelectionChangedListener listener )
          {}

          public void setSelection( ISelection sel )
          {}
        };

        final PropertyDialogAction propAction = new PropertyDialogAction( m_viewer.getControl().getShell(), selpro );
        propAction.run();
      }
    };
  }

  private void hookDoubleClickAction()
  {
    m_viewer.addDoubleClickListener( new IDoubleClickListener()
    {
      public void doubleClick( DoubleClickEvent event )
      {
        doubleClickAction.run();
      }
    } );
  }

  private void refreshProjectsList()
  {
    try
    {
      final MetaProject[] dependencies = ProjectsAnalyser.analyse( ResourcesPlugin.getWorkspace().getRoot()
          .getProjects() );

      m_viewer.setInput( dependencies );
    }
    catch( final CoreException e )
    {
      MessageDialog.openError( m_viewer.getControl().getShell(), 
          "ProjectDependencies - Error occured while analysing projects", e
          .getLocalizedMessage() );
    }
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  public void setFocus()
  {
    m_viewer.getControl().setFocus();
  }
}