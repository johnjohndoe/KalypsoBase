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
package org.kalypso.gml.ui.internal.shape;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.kalypso.commons.databinding.conversion.StringToPathConverter;
import org.kalypso.commons.databinding.validation.MultiValidator;
import org.kalypso.gml.ui.i18n.Messages;

/**
 * Allow user to enter the file path to the new shape file.
 *
 * @author Gernot Belger
 */
public class ShapeFileNewFilePage extends WizardPage
{
  private final DataBindingContext m_context = new DataBindingContext();

  private final ShapeFileNewData m_input;

  ShapeFileNewFilePage( final String pageName, final ShapeFileNewData input )
  {
    super( pageName );

    m_input = input;

    setTitle( Messages.getString("ShapeFileNewFilePage.0") ); //$NON-NLS-1$
    setDescription( Messages.getString("ShapeFileNewFilePage.1") ); //$NON-NLS-1$
  }

  @Override
  public void createControl( final Composite parent )
  {
    final Form form = new Form( parent, SWT.NONE );
    form.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    final Composite body = form.getBody();
    body.setLayout( new GridLayout( 3, false ) );

    createContents( body );

    setControl( form );

    WizardPageSupport.create( this, m_context );
  }

  private void createContents( final Composite parent )
  {
    createFileChooser( parent );
  }

  private void createFileChooser( final Composite parent )
  {
    final Label label = new Label( parent, SWT.NONE );
    label.setText( Messages.getString( "ShapeFileNewPage_2" ) ); //$NON-NLS-1$

    final Text fileField = new Text( parent, SWT.BORDER );
    fileField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    fileField.setMessage( Messages.getString( "ShapeFileNewPage_3" ) ); //$NON-NLS-1$

    final Button fileButton = new Button( parent, SWT.PUSH );
    fileButton.setText( Messages.getString( "ShapeFileNewPage_4" ) ); //$NON-NLS-1$

    createFileBinding( fileField, fileButton );
  }

  private void createFileBinding( final Text fileField, final Button fileButton )
  {
    final ISWTObservableValue fileText = SWTObservables.observeText( fileField, SWT.Modify );
    final IObservableValue pathValue = new ShapePathValue( m_input );

    fileButton.addSelectionListener( new FileSaveAsSelectionListener( pathValue, Messages.getString( "ShapeFileNewPage_7" ) ) ); //$NON-NLS-1$

    final UpdateValueStrategy targetToModel = new UpdateValueStrategy();
    targetToModel.setConverter( new StringToPathConverter() );

    final MultiValidator multiValidator = new MultiValidator();
    multiValidator.add( new PathIsProjectValidator( IStatus.ERROR ) );
    multiValidator.add( new PathIsDirectoryValidator( IStatus.ERROR ) );
    multiValidator.add( new PathShapeExistsValidator( IStatus.WARNING ) );
    targetToModel.setBeforeSetValidator( multiValidator );

    final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
    // AfterGet also works when new dialog sets the new path into the model
    modelToTarget.setAfterGetValidator( multiValidator );

    m_context.bindValue( fileText, pathValue, targetToModel, modelToTarget );
  }
}