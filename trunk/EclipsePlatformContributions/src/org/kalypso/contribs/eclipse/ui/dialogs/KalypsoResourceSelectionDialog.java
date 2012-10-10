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
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

@SuppressWarnings("restriction")
public class KalypsoResourceSelectionDialog extends SelectionDialog
{
  private final IResource m_initialSelection;

  private final String[] m_allowedResourceExtensions;

  private final boolean m_allowNewResourceName = false;

  protected Label m_statusMessage;

  protected ISelectionValidator m_validator;

  private boolean m_showClosedProjects = true;

  ResourceSelectionGroup m_group;

  private final IContainer m_inputContainer;

  private ViewerFilter m_filter;

  /*
   * abgeleitet von ContainerSelectionDialog @author peiler
   */
  public KalypsoResourceSelectionDialog( final Shell parentShell, final IResource initialSelection, final String message, final String[] allowedResourceExtensions, final IContainer inputContainer, final ISelectionValidator validator )
  {
    super( parentShell );

    // TODO: check if messages are still ok
    setTitle( IDEWorkbenchMessages.ResourceSelectionDialog_title );
    m_allowedResourceExtensions = allowedResourceExtensions;
    m_inputContainer = inputContainer;
    m_initialSelection = initialSelection;
    m_validator = validator;
    if( message != null )
      setMessage( message );
    else
      setMessage( IDEWorkbenchMessages.ResourceSelectionDialog_message );
    setShellStyle( getShellStyle() | SWT.RESIZE );
  }

  @Override
  protected Control createDialogArea( final Composite parent )
  {
    // create composite
    final Composite area = (Composite) super.createDialogArea( parent );

    final Listener listener = new Listener()
    {
      @Override
      public void handleEvent( final Event event )
      {
        final IResource selectedResource = m_group.getSelectedResource();
        getOkButton().setEnabled( false );

        if( m_validator != null && selectedResource != null )
        {
          final String errorMsg = m_validator.isValid( selectedResource );
          if( errorMsg == null || "".equals( errorMsg ) ) { //$NON-NLS-1$
            m_statusMessage.setText( "" ); //$NON-NLS-1$
            getOkButton().setEnabled( true );
          }
          else
          {
            m_statusMessage.setForeground( JFaceColors.getErrorText( m_statusMessage.getDisplay() ) );
            m_statusMessage.setText( errorMsg );
            getOkButton().setEnabled( false );
          }
          if( event.type == SWT.MouseDoubleClick )
          {
            if( errorMsg == null )
              okPressed();
            else
            {
              m_statusMessage.setText( errorMsg );
              getOkButton().setEnabled( false );
            }
          }
        }
      }
    };

    m_statusMessage = new Label( parent, SWT.NONE );
    m_statusMessage.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
    m_statusMessage.setFont( parent.getFont() );

    // container selection group
    m_group = new ResourceSelectionGroup( area, listener, m_allowNewResourceName, getMessage(), m_showClosedProjects, m_allowedResourceExtensions, m_inputContainer );
    if( m_filter != null )
      m_group.addViewerFilter( m_filter );

    return dialogArea;
  }

  public void setViewerFilter( final ViewerFilter filter )
  {
    m_filter = filter;
  }

  @Override
  protected Control createContents( final Composite parent )
  {
    // create the top level composite for the dialog
    final Composite composite = new Composite( parent, 0 );
    GridLayoutFactory.fillDefaults().spacing( 5, 0 ).applyTo( composite );

    composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
    applyDialogFont( composite );
    // initialize the dialog units
    initializeDialogUnits( composite );
    // create the dialog area and button bar
    dialogArea = createDialogArea( composite );
    buttonBar = createButtonBar( composite );

    // Change: set selected resource
    if( m_initialSelection != null )
      m_group.setSelectedResource( m_initialSelection );

    return composite;
  }

  /**
   * The <code>ContainerSelectionDialog</code> implementation of this <code>Dialog</code> method builds a list of the
   * selected resource containers for later retrieval by the client and closes this dialog.
   */
  @Override
  protected void okPressed( )
  {
    final List<IPath> chosenResourcePathList = new ArrayList<>();
    final IPath returnValue = m_group.getResourceFullPath();
    if( returnValue != null )
      chosenResourcePathList.add( returnValue );
    setResult( chosenResourcePathList );
    super.okPressed();
  }

  /**
   * Sets the validator to use.
   *
   * @param val
   *          A selection validator
   */
  public void setValidator( final ISelectionValidator val )
  {
    m_validator = val;
  }

  /**
   * Set whether or not closed projects should be shown in the selection dialog.
   *
   * @param show
   *          Whether or not to show closed projects.
   */
  public void showClosedProjects( final boolean show )
  {
    m_showClosedProjects = show;
  }

}