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
package org.kalypso.metaproject.views;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
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
import org.eclipse.jface.viewers.ITreeContentProvider;
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
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.metaproject.ImageProvider;
import org.kalypso.metaproject.MetaProjectPlugin;

/**
 * This is the dependency-view of the projects available in the current workspace.
 * 
 * @author schlienger
 */
public class ProjectsView extends ViewPart
{
  private Action m_actionRefresh;
  private Action m_actionLayoutReduced;
  private Action m_actionCollapse;
  private Action m_actionShowDescription;
  private Action doubleClickAction;

  private SashForm m_sashForm;
  private TreeViewer m_viewer;
  private Composite m_cmpDesc;
  private Button m_btnSave;
  private Text m_text;

  private ProjectTreeContentProvider m_projectTCP;
  private Action m_actionDump;

  /**
   * This is a callback that will allow us to create the viewer and initialize it.
   */
  public void createPartControl( Composite parent )
  {
    m_sashForm = new SashForm( parent, SWT.HORIZONTAL );

    m_viewer = new TreeViewer( m_sashForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
    m_projectTCP = new ProjectTreeContentProvider();
    m_viewer.setContentProvider( m_projectTCP );
    m_viewer.setLabelProvider( new WorkbenchLabelProvider() );
    m_viewer.getControl().setToolTipText( "Double-click opens the Project-Properties dialog" );

    m_cmpDesc = new Composite( m_sashForm, SWT.NONE );
    m_cmpDesc.setLayout( new GridLayout( 2, false ) );

    final Label lbl = new Label( m_cmpDesc, SWT.NONE );
    lbl.setText( "Project Description:" );

    m_btnSave = new Button( m_cmpDesc, SWT.FLAT );
    m_btnSave.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_END ) );
    m_btnSave.setText( "Save" );
    m_btnSave.setToolTipText( "Saves the project description in the file" );
    m_btnSave.setEnabled( false );

    m_text = new Text( m_cmpDesc, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
    final GridData gdText = new GridData( GridData.FILL_BOTH );
    gdText.horizontalSpan = 2;
    m_text.setLayoutData( gdText );

    m_viewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      public void selectionChanged( SelectionChangedEvent event )
      {
        updateText();
      }
    } );

    m_btnSave.addSelectionListener( new SelectionListener()
    {
      public void widgetSelected( SelectionEvent e )
      {
        final IProject project = (IProject)( (IStructuredSelection)m_viewer.getSelection() ).getFirstElement();
        if( project != null )
        {
          setDescription( project, m_text.getText() );
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
    manager.add( m_actionLayoutReduced );
    manager.add( new Separator() );
    manager.add( m_actionCollapse );
    manager.add( new Separator() );
    manager.add( m_actionShowDescription );
    manager.add( new Separator() );
    manager.add( m_actionDump );
  }

  private void fillContextMenu( IMenuManager manager )
  {
    manager.add( m_actionRefresh );
    manager.add( m_actionLayoutReduced );
    manager.add( new Separator() );
    manager.add( m_actionCollapse );
    manager.add( new Separator() );
    manager.add( m_actionShowDescription );
    manager.add( new Separator() );
    manager.add( m_actionDump );

    manager.add( new Separator() );
    // Other plug-ins can contribute there actions here
    manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
  }

  private void fillLocalToolBar( IToolBarManager manager )
  {
    manager.add( m_actionRefresh );
    manager.add( m_actionLayoutReduced );
    manager.add( new Separator() );
    manager.add( m_actionCollapse );
    manager.add( new Separator() );
    manager.add( m_actionShowDescription );
    manager.add( new Separator() );
    manager.add( m_actionDump );
    manager.add( new Separator() );
  }

  private void makeActions()
  {
    m_actionRefresh = new Action( "Refresh" )
    {
      public void run()
      {
        refreshProjectsList();
      }
    };
    m_actionRefresh.setToolTipText( "Refresh Projects List" );
    m_actionRefresh.setImageDescriptor( ImageProvider.IMAGE_REFRESH );

    m_actionLayoutReduced = new Action( "Reduced-View", IAction.AS_CHECK_BOX )
    {
      public void run()
      {
        m_projectTCP.setLayoutReduced( m_actionLayoutReduced.isChecked() );
        m_viewer.refresh();
      }
    };
    m_actionLayoutReduced
        .setToolTipText( "The reduced-view only displays projects that are not referenced by other projects" );
    m_actionLayoutReduced.setImageDescriptor( ImageProvider.IMAGE_REDUCED );

    m_actionCollapse = new Action( "Collapse All" )
    {
      public void run()
      {
        m_viewer.collapseAll();
      }
    };
    m_actionCollapse.setImageDescriptor( ImageProvider.IMAGE_COLLAPSE );

    m_actionShowDescription = new Action( "Show Description", IAction.AS_CHECK_BOX )
    {
      public void run()
      {
        m_cmpDesc.setVisible( m_actionShowDescription.isChecked() );

        if( m_cmpDesc.isVisible() )
          m_sashForm.setWeights( new int[]
          {
              1,
              1 } );
        else
          m_sashForm.setWeights( new int[]
          {
              1,
              0 } );

        updateText();
      }
    };
    m_actionShowDescription.setToolTipText( "Toggle display of project-description pane" );
    m_actionShowDescription.setImageDescriptor( ImageProvider.IMAGE_DETAILS );
    m_actionShowDescription.setChecked( true );

    m_actionDump = new Action( "Dump to clipboard" )
    {
      public void run()
      {
        final String dump = dumpTree();

        final StringSelection stsel = new StringSelection( dump );
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents( stsel, stsel );
      }
    };
    m_actionDump.setToolTipText( "Copy the whole tree in the clipboard" );
    m_actionDump.setImageDescriptor( ImageProvider.IMAGE_COPY );

    doubleClickAction = new Action()
    {
      public void run()
      {
        final ISelection selection = m_viewer.getSelection();
        final IProject project = (IProject)( (IStructuredSelection)selection ).getFirstElement();

        final ISelectionProvider selpro = new ISelectionProvider()
        {
          public void addSelectionChangedListener( final ISelectionChangedListener listener )
          {}

          public ISelection getSelection()
          {
            return new StructuredSelection( project );
          }

          public void removeSelectionChangedListener( final ISelectionChangedListener listener )
          {}

          public void setSelection( final ISelection sel )
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
    m_viewer.setInput( ResourcesPlugin.getWorkspace().getRoot().getProjects() );
  }

  private void updateText()
  {
    if( m_cmpDesc.isVisible() )
    {
      final IProject project = (IProject)( (IStructuredSelection)m_viewer.getSelection() ).getFirstElement();

      if( project != null )
        m_text.setText( getDescription( project ) );
      else
        m_text.setText( "" );

      m_btnSave.setEnabled( false );
    }
  }

  private String dumpTree()
  {
    final ITreeContentProvider cp = (ITreeContentProvider)m_viewer.getContentProvider();
    final Object[] projects = cp.getElements( m_viewer.getInput() );
    final StringBuffer bf = new StringBuffer();
    for( int i = 0; i < projects.length; i++ )
      dumpTreeInternal( cp, bf, (IProject)projects[i], "" );

    return bf.toString();
  }

  private void dumpTreeInternal( final ITreeContentProvider cp, final StringBuffer bf, final IProject project,
      final String separator )
  {
    bf.append( separator ).append( project.getName() ).append( "\n" );

    final Object[] children = cp.getChildren( project );
    for( int i = 0; i < children.length; i++ )
      dumpTreeInternal( cp, bf, (IProject)children[i], separator + "\t" );
  }

  private String getDescription( final IProject project )
  {
    final IFile readme = project.getFile( "readme.txt" );
    if( readme.exists() )
    {
      InputStream contents = null;
      try
      {
        contents = readme.getContents();

        final StringWriter writer = new StringWriter();
        CopyUtils.copy( contents, writer, readme.getCharset() );

        contents.close();

        return writer.toString();
      }
      catch( final Exception e )
      {
        e.printStackTrace();

        MessageDialog.openError( m_viewer.getControl().getShell(),
            "ProjectDependencies - Error while getting project description", e.getLocalizedMessage() );
      }
      finally
      {
        IOUtils.closeQuietly( contents );
      }
    }

    return "";
  }

  public void setDescription( final IProject project, final String text )
  {
    final IFile readme = project.getFile( "readme.txt" );

    final Job job = new Job( "Saving File " + readme.getName() )
    {
      protected IStatus run( final IProgressMonitor monitor )
      {
        try
        {
          if( readme.exists() )
            readme.setContents( new ByteArrayInputStream( text.getBytes( readme.getCharset() ) ), true, true, monitor );
          else
            readme.create( new ByteArrayInputStream( text.getBytes( readme.getCharset() ) ), true, monitor );
        }
        catch( final Exception e )
        {
          return new Status( IStatus.ERROR, MetaProjectPlugin.getId(), 0,
              "Error while saving file " + readme.getName(), e );
        }

        return Status.OK_STATUS;
      }
    };

    job.schedule();
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  public void setFocus()
  {
    m_viewer.getControl().setFocus();
  }
}