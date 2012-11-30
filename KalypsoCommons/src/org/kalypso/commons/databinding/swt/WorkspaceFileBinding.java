/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.commons.databinding.swt;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.commons.databinding.DataBinder;
import org.kalypso.commons.databinding.IDataBinding;
import org.kalypso.commons.databinding.conversion.PathToStringConverter;
import org.kalypso.commons.databinding.conversion.StringToPathConverter;
import org.kalypso.commons.internal.i18n.Messages;
import org.kalypso.contribs.eclipse.core.runtime.PathUtils;
import org.kalypso.contribs.eclipse.ui.dialogs.KalypsoResourceSelectionDialog;
import org.kalypso.contribs.eclipse.ui.dialogs.ResourceSelectionValidator;

/**
 * @author Gernot Belger
 */
public class WorkspaceFileBinding
{
  private final IDataBinding m_binding;

  private final IObservableValue m_fileValue;

  private final String m_dialogMessage;

  private final String[] m_filterExtensions;

  private IContainer m_inputContainer;

  private ViewerFilter m_filter;

  private final WorkbenchFileValidator m_fileValidator = new WorkbenchFileValidator();

  private Binding m_fileBinding;

  public WorkspaceFileBinding( final IDataBinding binding, final IObservableValue fileValue, final String dialogMessage, final String[] filterExtensions )
  {
    this( binding, fileValue, dialogMessage, filterExtensions, false );
  }

  public WorkspaceFileBinding( final IDataBinding binding, final IObservableValue fileValue, final String dialogMessage, final String[] filterExtensions, final boolean isOptional )
  {
    m_binding = binding;
    m_fileValue = fileValue;
    m_dialogMessage = dialogMessage;
    m_filterExtensions = filterExtensions;
    m_fileValidator.setIsOptional( isOptional );
  }

  /**
   * Set to <code>true</code>, if the selected path may be empty (i.e. if choosing the file is optional).
   */
  public void setIsOptional( final boolean isOptional )
  {
    m_fileValidator.setIsOptional( isOptional );
  }

  public void setInputContainer( final IContainer inputContainer )
  {
    m_inputContainer = inputContainer;
  }

  public void setFilter( final ViewerFilter filter )
  {
    m_filter = filter;
  }

  /**
   * @param historyValue
   *          The value representing the history of directories. Must be of type String[].
   */
  public Control createFileFieldWithHistory( final Composite parent, final IObservableValue historyValue )
  {
    Assert.isTrue( historyValue.getValueType() == String[].class );

    final ComboViewer viewer = new ComboViewer( parent, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN );
    viewer.getControl().setFont( parent.getFont() );
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setLabelProvider( new LabelProvider() );

    final IObservableValue targetInput = ViewersObservables.observeInput( viewer );
    m_binding.bindValue( targetInput, historyValue );

    final ISWTObservableValue targetText = SWTObservables.observeText( viewer.getControl() );
    final DataBinder binder = new DataBinder( targetText, m_fileValue );
    binder.setModelToTargetConverter( new PathToStringConverter() );
    binder.setTargetToModelConverter( new StringToPathConverter() );

    binder.addTargetAfterConvertValidator( m_fileValidator );

    m_fileBinding = m_binding.bindValue( binder );

    return viewer.getControl();
  }

  public Button createFileSearchButton( final Composite parent )
  {
    final Button browseButton = new Button( parent, SWT.PUSH );
    browseButton.setText( Messages.getString("WorkspaceFileBinding_0") ); //$NON-NLS-1$
    browseButton.setFont( parent.getFont() );

    browseButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleBrowseButtonSelected( parent.getShell() );
      }
    } );

    return browseButton;
  }

  protected void handleBrowseButtonSelected( final Shell shell )
  {
    final IFile currentFile = PathUtils.toFile( (IPath) m_fileValue.getValue() );

    final IContainer inputContainer = getInputContainer();

    final KalypsoResourceSelectionDialog dialog = new KalypsoResourceSelectionDialog( shell, currentFile, m_dialogMessage, m_filterExtensions, inputContainer, new ResourceSelectionValidator() ); //$NON-NLS-1$
    if( m_filter != null )
      dialog.setViewerFilter( m_filter );
    dialog.open();

    final Object[] result = dialog.getResult();
    if( result == null || result.length != 1 )
      return;

    final IPath newPath = (IPath) result[0];
    m_fileValue.setValue( newPath );
  }

  private IContainer getInputContainer( )
  {
    if( m_inputContainer == null )
      return ResourcesPlugin.getWorkspace().getRoot();

    return m_inputContainer;
  }

  public Binding getFileBinding( )
  {
    return m_fileBinding;
  }
}
