/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.DrillDownComposite;

@SuppressWarnings("restriction")
public class ResourceSelectionGroup extends Composite
{
  private final Listener m_listener;

  private final boolean m_allowNewResourceName;

  private final boolean m_showClosedProjects;

  private static final String DEFAULT_MSG_NEW_ALLOWED = IDEWorkbenchMessages.ContainerGroup_message;

  private static final String DEFAULT_MSG_SELECT_ONLY = IDEWorkbenchMessages.ContainerGroup_selectFolder;

  private static final int SIZING_SELECTION_PANE_WIDTH = 320;

  private static final int SIZING_SELECTION_PANE_HEIGHT = 300;

  private Text m_resourceNameField;

  TreeViewer m_treeViewer;

  private IResource m_selectedResource;

  private final String[] m_allowedResourceExtensions;

  private final IContainer m_inputContainer;

  /*
   * übernommen von ContainerSelectionGroup und leicht verändert @author peiler
   */
  public ResourceSelectionGroup( final Composite parent, final Listener listener, final boolean allowNewContainerName, final String message, final boolean showClosedProjects, final String[] allowedResourceExtensions, final IContainer inputContainer )
  {
    this( parent, listener, allowNewContainerName, message, showClosedProjects, SIZING_SELECTION_PANE_HEIGHT, allowedResourceExtensions, inputContainer );
  }

  ResourceSelectionGroup( final Composite parent, final Listener listener, final boolean allowNewResourceName, final String message, final boolean showClosedProjects, final int heightHint, final String[] allowedResourceExtensions, final IContainer inputContainer )
  {
    super( parent, SWT.NONE );
    m_listener = listener;
    m_allowNewResourceName = allowNewResourceName;
    m_showClosedProjects = showClosedProjects;
    m_allowedResourceExtensions = allowedResourceExtensions;
    m_inputContainer = inputContainer;
    if( message != null )
      createContents( message, heightHint );
    else if( m_allowNewResourceName )
      createContents( DEFAULT_MSG_NEW_ALLOWED, heightHint );
    else
      createContents( DEFAULT_MSG_SELECT_ONLY, heightHint );
  }

  public void addViewerFilter( final ViewerFilter filter )
  {
    m_treeViewer.addFilter( filter );
  }

  public void resourceSelectionChanged( final IResource resource )
  {
    m_selectedResource = resource;

    if( m_allowNewResourceName )
    {
      if( resource == null )
        m_resourceNameField.setText( "" );//$NON-NLS-1$
      else
        m_resourceNameField.setText( resource.getFullPath().makeRelative().toString() );
    }

    // fire an event so the parent can update its controls
    fireEvent( SWT.Selection );
  }

  void fireEvent( final int type )
  {
    if( m_listener != null )
    {
      final Event changeEvent = new Event();
      changeEvent.type = type;
      changeEvent.widget = this;
      m_listener.handleEvent( changeEvent );
    }
  }

  /**
   * Creates the contents of the composite.
   */
  public void createContents( final String message )
  {
    createContents( message, SIZING_SELECTION_PANE_HEIGHT );
  }

  /**
   * Creates the contents of the composite.
   *
   * @param heightHint
   *          height hint for the drill down composite
   */
  public void createContents( final String message, final int heightHint )
  {
    setLayout( GridLayoutFactory.fillDefaults().create() );
    setLayoutData( new GridData( GridData.FILL_BOTH ) );

    final Label label = new Label( this, SWT.WRAP );
    label.setText( message );
    label.setFont( getFont() );

    if( m_allowNewResourceName )
    {
      m_resourceNameField = new Text( this, SWT.SINGLE | SWT.BORDER );
      m_resourceNameField.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
      // TODO: add listener to control user input
      // resourceNameField.addListener( SWT.DefaultSelection, m_listener );
      m_resourceNameField.setFont( getFont() );
    }
    else
    {
      // filler...
      new Label( this, SWT.NONE );
    }

    createTreeViewer( heightHint );
    Dialog.applyDialogFont( this );
  }

  protected void createTreeViewer( final int heightHint )
  {
    // Create drill down.
    final DrillDownComposite drillDown = new DrillDownComposite( this, SWT.BORDER );
    final GridData spec = new GridData( GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL );
    spec.widthHint = SIZING_SELECTION_PANE_WIDTH;
    spec.heightHint = heightHint;
    drillDown.setLayoutData( spec );

    // Create tree viewer inside drill down.
    m_treeViewer = new TreeViewer( drillDown, SWT.NONE );
    drillDown.setChildTree( m_treeViewer );
    final ResourceContentProvider cp = new ResourceContentProvider( m_allowedResourceExtensions );
    cp.showClosedProjects( m_showClosedProjects );
    m_treeViewer.setContentProvider( cp );
    m_treeViewer.setLabelProvider( WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider() );
    m_treeViewer.setSorter( new ViewerSorter() );
    m_treeViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        resourceSelectionChanged( (IResource) selection.getFirstElement() ); // allow
        // null
      }
    } );
    m_treeViewer.addDoubleClickListener( new IDoubleClickListener()
    {
      @Override
      public void doubleClick( final DoubleClickEvent event )
      {
        final ISelection selection = event.getSelection();
        if( selection instanceof IStructuredSelection )
        {
          final Object item = ((IStructuredSelection) selection).getFirstElement();
          if( m_treeViewer.getExpandedState( item ) )
            m_treeViewer.collapseToLevel( item, 1 );
          else
            m_treeViewer.expandToLevel( item, 1 );
        }
        fireEvent( SWT.MouseDoubleClick );
      }
    } );

    // This has to be done after the viewer has been laid out
    // treeViewer.setInput( ResourcesPlugin.getWorkspace() );
    m_treeViewer.setInput( m_inputContainer );
  }

  public IPath getResourceFullPath( )
  {
    IPath resourcePath = null;
    if( m_allowNewResourceName )
    {
      final String pathName = m_resourceNameField.getText();
      if( pathName == null || pathName.length() < 1 )
      {
        // nothing
      }
      else
      {
        // The user may not have made this absolute so do it for them
        resourcePath = new Path( pathName ).makeAbsolute();
      }
    }
    else
    {
      if( m_selectedResource == null )
      {
        // nothing
      }
      else
        resourcePath = m_selectedResource.getFullPath();
    }
    return resourcePath;
  }

  /**
   * Gives focus to one of the widgets in the group, as determined by the group.
   */
  public void setInitialFocus( )
  {
    if( m_allowNewResourceName )
      m_resourceNameField.setFocus();
    else
      m_treeViewer.getTree().setFocus();
  }

  /**
   * Sets the selected existing container.
   */
  public void setSelectedResource( final IResource resource )
  {
    m_selectedResource = resource;

    // expand to and select the specified container
    final List<IContainer> itemsToExpand = new ArrayList<>();
    IContainer parent = resource.getParent();
    while( parent != null )
    {
      itemsToExpand.add( 0, parent );
      parent = parent.getParent();
    }
    m_treeViewer.setExpandedElements( itemsToExpand.toArray() );
    m_treeViewer.setSelection( new StructuredSelection( resource ), true );
  }

  public IResource getSelectedResource( )
  {
    return m_selectedResource;
  }

}