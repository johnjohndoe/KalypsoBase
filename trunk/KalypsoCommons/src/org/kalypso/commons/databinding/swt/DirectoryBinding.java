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

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.kalypso.commons.databinding.DataBinder;
import org.kalypso.commons.databinding.IDataBinding;
import org.kalypso.commons.databinding.conversion.FileToStringConverter;
import org.kalypso.commons.databinding.conversion.StringToFileConverter;
import org.kalypso.commons.databinding.validation.FileAlreadyExistsValidator;
import org.kalypso.commons.databinding.validation.FileExistsValidator;
import org.kalypso.commons.databinding.validation.FileIsDirectoryValidator;
import org.kalypso.commons.internal.i18n.Messages;

/**
 * Helper class for the ever repeating task to show the user a text-field in order to let him select a directory.
 * 
 * @author Gernot Belger
 */
public class DirectoryBinding
{
  private final IObservableValue m_directoryValue;

  private final int m_style;

  private DataBinder m_historyBinder;

  private DataBinder m_historyInputBinder;

  /**
   * @param style
   *          of the directory chooser. Either {@link SWT#OPEN} or {@link SWT#SAVE}.
   */
  public DirectoryBinding( final IObservableValue directoryValue, final int style )
  {
    m_directoryValue = directoryValue;
    m_style = style;
  }

  /**
   * @param historyValue
   *          The value representing the history of directories. Must be of type SAtring[].
   */
  public Control createDirectoryFieldWithHistory( final Composite parent, final IObservableValue historyValue )
  {
    Assert.isTrue( historyValue.getValueType() == String[].class );

    final ComboViewer viewer = new ComboViewer( parent, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN );
    viewer.getControl().setFont( parent.getFont() );
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setLabelProvider( new LabelProvider() );

    final IObservableValue targetInput = ViewersObservables.observeInput( viewer );
    m_historyInputBinder = new DataBinder( targetInput, historyValue );

    final ISWTObservableValue targetText = SWTObservables.observeText( viewer.getControl() );
    m_historyBinder = new DataBinder( targetText, m_directoryValue );
    m_historyBinder.setModelToTargetConverter( new FileToStringConverter() );
    m_historyBinder.setTargetToModelConverter( new StringToFileConverter() );

    // FIXME: better validation and depending on save or load
    m_historyBinder.addTargetAfterConvertValidator( new FileIsDirectoryValidator( IStatus.ERROR ) );

    if( m_style == SWT.OPEN )
      m_historyBinder.addTargetAfterConvertValidator( new FileExistsValidator( IStatus.ERROR, Messages.getString("DirectoryBinding_0") ) ); //$NON-NLS-1$

    if( m_style == SWT.SAVE )
      m_historyBinder.addTargetAfterConvertValidator( new FileAlreadyExistsValidator( IStatus.WARNING, Messages.getString("DirectoryBinding_1") ) ); //$NON-NLS-1$

    return viewer.getControl();
  }

  public DataBinder getHistoryBinder( )
  {
    return m_historyBinder;
  }

  public Button createDirectorySearchButton( final Composite parent, final Control directoryTextControl, final String dialogTitle, final String dialogMessage )
  {
    // destination browse button
    final Button browseButton = new Button( parent, SWT.PUSH );
    browseButton.setText( Messages.getString("DirectoryBinding_2") ); //$NON-NLS-1$
    browseButton.setFont( parent.getFont() );

    browseButton.addSelectionListener( new DirectoryValueSelectionListener( directoryTextControl, dialogTitle, dialogMessage ) );

    return browseButton;
  }

  /**
   * Must be called after {@link #createDirectoryFieldWithHistory(Composite, IObservableValue)} was called.<br/>
   * Before this is called, the client may add validators to the binding via {@link #getHistoryBinder()}.
   */
  public void applyBinding( final IDataBinding binding )
  {
    binding.bindValue( m_historyInputBinder );
    binding.bindValue( m_historyBinder );
  }
}