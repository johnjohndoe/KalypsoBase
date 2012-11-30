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
package org.kalypso.ogc.gml.outline.handler;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.kalypso.commons.databinding.DataBinder;
import org.kalypso.commons.databinding.IDataBinding;
import org.kalypso.commons.databinding.conversion.FileToStringConverter;
import org.kalypso.commons.databinding.conversion.StringToFileConverter;
import org.kalypso.commons.databinding.swt.FileValueSelectionListener;
import org.kalypso.commons.databinding.validation.FileAlreadyExistsValidator;
import org.kalypso.commons.databinding.validation.FileShouldNotBeDirectoryValidator;
import org.kalypso.commons.databinding.validation.StringBlankValidator;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * Helper class for creating the typical export file field and button.
 * 
 * @author Gernot Belger
 */
public class ExportFileControls
{
  private final IDataBinding m_binding;

  private final IExportFileData m_data;

  private final FileValueSelectionListener m_fileListener;

  public ExportFileControls( final IExportFileData data, final IDataBinding binding, final String titel )
  {
    m_data = data;
    m_binding = binding;

    m_fileListener = new FileValueSelectionListener( null, titel, SWT.SAVE );
  }

  public void createControls( final Composite parent, final int numFieldColumns )
  {
    final Text fileField = new Text( parent, SWT.BORDER | SWT.SINGLE );
    fileField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final Button fileButton = new Button( parent, SWT.PUSH );
    fileButton.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false, numFieldColumns, 1 ) );
    fileButton.setText( Messages.getString( "ExportFileControls_0" ) ); //$NON-NLS-1$

    /* field binding */
    final ISWTObservableValue target = SWTObservables.observeText( fileField, new int[] { SWT.Modify, SWT.DefaultSelection } );
    final IObservableValue model = BeansObservables.observeValue( m_data, AbstractExportFileData.PROPERTY_EXPORT_FILE );
    final DataBinder binder = new DataBinder( target, model );

    binder.setTargetToModelConverter( new StringToFileConverter() );
    binder.setModelToTargetConverter( new FileToStringConverter() );

    binder.addTargetAfterGetValidator( new StringBlankValidator( IStatus.ERROR, Messages.getString( "ExportFileControls_1" ) ) ); //$NON-NLS-1$
    binder.addTargetAfterConvertValidator( new FileShouldNotBeDirectoryValidator() );
    binder.addTargetAfterConvertValidator( new FileAlreadyExistsValidator() );

    m_binding.bindValue( binder );

    /* Button binding */
    m_fileListener.addAllFilter();
    m_fileListener.setFileValue( model );
    fileButton.addSelectionListener( m_fileListener );
  }

  public void addFilter( final String name, final String extension )
  {
    m_fileListener.addFilter( name, extension );
  }
}
