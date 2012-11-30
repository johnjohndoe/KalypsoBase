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

import java.io.File;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.kalypso.commons.databinding.DataBinder;
import org.kalypso.commons.databinding.IDataBinding;
import org.kalypso.commons.databinding.conversion.FileToStringConverter;
import org.kalypso.commons.databinding.conversion.StringToFileConverter;
import org.kalypso.commons.internal.i18n.Messages;
import org.kalypso.contribs.eclipse.jface.wizard.IFileChooserDelegate;

/**
 * @author Gernot Belger
 */
public class FileBinding
{
  private final IDataBinding m_binding;

  private final IObservableValue m_fileValue;

  private final IFileChooserDelegate m_delegate;

  public FileBinding( final IDataBinding binding, final IObservableValue fileValue, final IFileChooserDelegate delegate )
  {
    m_binding = binding;
    m_fileValue = fileValue;
    m_delegate = delegate;
  }

  public Text createFileField( final Composite parent )
  {
    final Text field = new Text( parent, SWT.SINGLE | SWT.BORDER );
    field.setFont( parent.getFont() );

    final IObservableValue targetFile = SWTObservables.observeText( field, SWT.Modify );

    final DataBinder binder = new DataBinder( targetFile, m_fileValue );
    binder.setModelToTargetConverter( new FileToStringConverter() );
    binder.setTargetToModelConverter( new StringToFileConverter() );

    binder.addTargetAfterConvertValidator( new FileChooserValidator( m_delegate ) );

    m_binding.bindValue( binder );

    return field;
  }

  /**
   * Create a text editor for a file path. The editor is a combo box, that also shows the recent history of edited file
   * pathes.
   * 
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
    binder.setModelToTargetConverter( new FileToStringConverter() );
    binder.setTargetToModelConverter( new StringToFileConverter() );

    binder.addTargetAfterConvertValidator( new FileChooserValidator( m_delegate ) );

    m_binding.bindValue( binder );

    return viewer.getControl();
  }

  public Button createFileSearchButton( final Composite parent, final Control fileTextControl )
  {
    // destination browse button
    final Button browseButton = new Button( parent, SWT.PUSH );
    browseButton.setText( Messages.getString( "FileBinding_0" ) ); //$NON-NLS-1$
    browseButton.setFont( parent.getFont() );

    browseButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleBrowseButtonSelected( parent.getShell(), fileTextControl );
      }
    } );

    return browseButton;
  }

  protected void handleBrowseButtonSelected( final Shell shell, final Control fileTextControl )
  {
    final File currentfile = (File)m_fileValue.getValue();
    final File newFile = m_delegate.chooseFile( shell, currentfile );
    if( newFile == null )
      return;

    final String newPath = newFile.getAbsolutePath();
    DirectoryValueSelectionListener.setValue( fileTextControl, newPath );
  }
}
