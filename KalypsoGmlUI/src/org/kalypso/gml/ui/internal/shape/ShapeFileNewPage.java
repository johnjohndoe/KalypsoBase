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
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.kalypso.commons.databinding.validation.MultiValidator;
import org.kalypso.commons.databinding.viewers.ComboViewerEditingSupport;
import org.kalypso.commons.databinding.viewers.TextEditingSupport;
import org.kalypso.contribs.eclipse.jface.action.ActionHyperlink;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.dbf.FieldType;

/**
 * @author Gernot Belger
 */
public class ShapeFileNewPage extends WizardPage
{
  private final DataBindingContext m_context = new DataBindingContext();

  private final ShapeFileNewData m_input;

  protected ShapeFileNewPage( final String pageName, final ShapeFileNewData input )
  {
    super( pageName );

    m_input = input;

    setTitle( Messages.getString("ShapeFileNewPage_0") ); //$NON-NLS-1$
    setDescription( Messages.getString("ShapeFileNewPage_1") ); //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
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
    createTypeChooser( parent );
    createFieldEditor( parent );
  }

  private void createFileChooser( final Composite parent )
  {
    final Label label = new Label( parent, SWT.NONE );
    label.setText( Messages.getString("ShapeFileNewPage_2") ); //$NON-NLS-1$

    final Text fileField = new Text( parent, SWT.BORDER );
    fileField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    fileField.setMessage( Messages.getString("ShapeFileNewPage_3") ); //$NON-NLS-1$

    final Button fileButton = new Button( parent, SWT.PUSH );
    fileButton.setText( Messages.getString("ShapeFileNewPage_4") ); //$NON-NLS-1$

    createFileBinding( fileField, fileButton );
  }

  private void createTypeChooser( final Composite parent )
  {
    final Label label = new Label( parent, SWT.NONE );
    label.setText( Messages.getString("ShapeFileNewPage_5") ); //$NON-NLS-1$

    final ComboViewer viewer = new ComboViewer( parent, SWT.READ_ONLY | SWT.DROP_DOWN );
    viewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    viewer.setLabelProvider( new LabelProvider() );
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setInput( ShapeType.values() );

    new Label( parent, SWT.NONE );

    createTypeBinding( viewer );
  }

  private void createFieldEditor( final Composite parent )
  {
    final Label label = new Label( parent, SWT.NONE );
    label.setText( Messages.getString("ShapeFileNewPage_6") ); //$NON-NLS-1$
    label.setLayoutData( new GridData( SWT.BEGINNING, SWT.BEGINNING, false, false ) );

    final TableViewer viewer = new TableViewer( parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER );

    final Table table = viewer.getTable();
    table.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
    table.setHeaderVisible( true );
    table.setLinesVisible( true );

    final IObservableList fieldList = createFieldBinding( viewer );

    createTableActions( parent, viewer, fieldList );
  }

  private void createTableActions( final Composite parent, final TableViewer viewer, final IObservableList fieldList )
  {
    new Label( parent, SWT.NONE );

    final Composite actionPanel = new Composite( parent, SWT.NONE );
    actionPanel.setLayout( new RowLayout() );
    actionPanel.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, true, 2, 1 ) );

    final IAction addAction = new AddFieldAction( fieldList );
    final IAction removeAction = new RemoveFieldAction( viewer, fieldList );

    ActionHyperlink.createHyperlink( null, actionPanel, SWT.PUSH, addAction );
    ActionHyperlink.createHyperlink( null, actionPanel, SWT.PUSH, removeAction );
  }

  private void createFileBinding( final Text fileField, final Button fileButton )
  {
    final ISWTObservableValue fileText = SWTObservables.observeText( fileField, SWT.Modify );
    final IObservableValue pathValue = new ShapePathValue( m_input );

    fileButton.addSelectionListener( new FileSaveAsSelectionListener( pathValue, Messages.getString("ShapeFileNewPage_7") ) ); //$NON-NLS-1$

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

  private void createTypeBinding( final ComboViewer viewer )
  {
    final IViewerObservableValue comboValue = ViewerProperties.singleSelection().observe( viewer );
    final IObservableValue typeValue = new ShapeTypeValue( m_input );

    m_context.bindValue( comboValue, typeValue );
  }

  private IObservableList createFieldBinding( final TableViewer viewer )
  {
    final IValueProperty nameProperty = BeanProperties.value( DBFFieldBean.class, DBFFieldBean.PROPERTY_NAME );
    final IValueProperty typeProperty = BeanProperties.value( DBFFieldBean.class, DBFFieldBean.PROPERTY_TYPE );
    final IValueProperty typeLabelProperty = BeanProperties.value( DBFFieldBean.class, DBFFieldBean.PROPERTY_TYPE_LABEL );
    final IValueProperty lengthProperty = BeanProperties.value( DBFFieldBean.class, DBFFieldBean.PROPERTY_LENGTH );
    final IValueProperty decimalsProperty = BeanProperties.value( DBFFieldBean.class, DBFFieldBean.PROPERTY_DECIMAL_COUNT );

    final IValueProperty[] labelProperties = new IValueProperty[] { nameProperty, typeLabelProperty, lengthProperty, decimalsProperty };

    createNameColumn( viewer, nameProperty );
    createTypeColumn( viewer, typeProperty );
    createLengthColumn( viewer, lengthProperty );
    createDecimalColumn( viewer, decimalsProperty );

    final WritableList tableInput = new WritableList( m_input.getFieldList(), DBFFieldBean.class );

    ViewerSupport.bind( viewer, tableInput, labelProperties );

    return tableInput;
  }

  public void createNameColumn( final TableViewer viewer, final IValueProperty nameProperty )
  {
    final TableViewerColumn nameColumn = new TableViewerColumn( viewer, SWT.LEFT );
    nameColumn.getColumn().setText( Messages.getString("ShapeFileNewPage_8") ); //$NON-NLS-1$
    nameColumn.getColumn().setWidth( 100 );
    final TextEditingSupport editingSupport = new TextEditingSupport( viewer, m_context, nameProperty )
    {
      /**
       * @see org.kalypso.commons.databinding.viewers.TextEditingSupport#createValidator(java.lang.Object)
       */
      @Override
      protected IValidator createValidator( final Object element )
      {
        return new DBFFieldNameValidator( (DBFFieldBean) element );
      }
    };
    nameColumn.setEditingSupport( editingSupport );
  }

  public void createTypeColumn( final TableViewer viewer, final IValueProperty typeProperty )
  {
    final TableViewerColumn typeColumn = new TableViewerColumn( viewer, SWT.LEFT );
    typeColumn.getColumn().setWidth( 100 );
    typeColumn.getColumn().setText( Messages.getString("ShapeFileNewPage_9") ); //$NON-NLS-1$
    final ComboViewerEditingSupport typeEditingSupport = new ComboViewerEditingSupport( viewer, m_context, typeProperty, SWT.DROP_DOWN | SWT.READ_ONLY )
    {
      @Override
      protected IValidator createValidator( final Object element )
      {
        return new DBFFieldTypeValidator( (DBFFieldBean) element );
      }
    };
    final ComboViewer typeEditor = typeEditingSupport.getComboViewer();
    typeEditor.setLabelProvider( new LabelProvider() );
    typeEditor.setContentProvider( new ArrayContentProvider() );
    typeEditor.setInput( FieldType.values() );
    typeColumn.setEditingSupport( typeEditingSupport );
  }

  public void createLengthColumn( final TableViewer viewer, final IValueProperty lengthProperty )
  {
    final TableViewerColumn lengthColumn = new TableViewerColumn( viewer, SWT.LEFT );
    lengthColumn.getColumn().setWidth( 100 );
    lengthColumn.getColumn().setText( Messages.getString("ShapeFileNewPage_10") ); //$NON-NLS-1$
    final TextEditingSupport editingSupport = new TextEditingSupport( viewer, m_context, lengthProperty )
    {
      @Override
      protected IValidator createValidator( final Object element )
      {
        return new DBFFieldLengthValidator( (DBFFieldBean) element );
      }
    };

    lengthColumn.setEditingSupport( editingSupport );
  }

  public void createDecimalColumn( final TableViewer viewer, final IValueProperty decimalsProperty )
  {
    final TableViewerColumn decimalColumn = new TableViewerColumn( viewer, SWT.LEFT );
    decimalColumn.getColumn().setWidth( 100 );
    decimalColumn.getColumn().setText( Messages.getString("ShapeFileNewPage_11") ); //$NON-NLS-1$
    final TextEditingSupport editingSupport = new TextEditingSupport( viewer, m_context, decimalsProperty )
    {
      /**
       * @see org.kalypso.commons.databinding.viewers.TextEditingSupport#createValidator(java.lang.Object)
       */
      @Override
      protected IValidator createValidator( final Object element )
      {
        return new DBFFieldDecimalCountValidator( (DBFFieldBean) element );
      }
    };

    decimalColumn.setEditingSupport( editingSupport );
  }
}