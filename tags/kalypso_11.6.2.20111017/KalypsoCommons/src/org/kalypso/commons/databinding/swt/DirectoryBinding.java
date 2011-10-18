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
import org.kalypso.commons.databinding.validation.FileIsDirectoryValidator;

/**
 * Helper class for the ever repeating task to show the user a text-field in order to let him select a directory.
 * 
 * @author Gernot Belger
 */
public class DirectoryBinding
{
  private final IDataBinding m_binding;

  private final IObservableValue m_directoryValue;

  private final int m_style;

  /**
   * @param style
   *          of the directory chooser. Either {@link SWT#OPEN} or {@link SWT#SAVE}.
   */
  public DirectoryBinding( final IDataBinding binding, final IObservableValue directoryValue, final int style )
  {
    m_binding = binding;
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
    m_binding.bindValue( targetInput, historyValue );

    final ISWTObservableValue targetText = SWTObservables.observeText( viewer.getControl() );
    final DataBinder binder = new DataBinder( targetText, m_directoryValue );
    binder.setModelToTargetConverter( new FileToStringConverter() );
    binder.setTargetToModelConverter( new StringToFileConverter() );

    // FIXME: better validation and depending on save or load
    binder.addTargetAfterConvertValidator( new FileIsDirectoryValidator( IStatus.ERROR ) );

    if( m_style == SWT.SAVE )
      binder.addTargetAfterConvertValidator( new FileAlreadyExistsValidator( IStatus.WARNING, "Directory already exists" ) );
// else
// binder.addTargetAfterConvertValidator( new File

    m_binding.bindValue( binder );

    return viewer.getControl();
  }

  public Button createDirectorySearchButton( final Composite parent, final Control directoryTextControl, final String dialogTitle, final String dialogMessage )
  {
    // destination browse button
    final Button browseButton = new Button( parent, SWT.PUSH );
    browseButton.setText( "Search..." );
    browseButton.setFont( parent.getFont() );

    browseButton.addSelectionListener( new DirectoryValueSelectionListener( directoryTextControl, dialogTitle, dialogMessage ) );

    return browseButton;
  }
}