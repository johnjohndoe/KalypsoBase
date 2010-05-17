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
package org.kalypso.gml.ui.commands.exportshape;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.kalypso.shape.ShapeType;

/**
 * @author Gernot Belger
 */
public class ExportShapeSignaturePage extends WizardPage
{
  private final ShapeSignature m_signature;

  public ExportShapeSignaturePage( final String pageName, final ShapeSignature signature )
  {
    super( pageName );
    m_signature = signature;

    setTitle( "Shape Definition" );
    setDescription( "Please define the shape signature on this page." );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    final Composite panel = new Composite( parent, SWT.NONE );
    panel.setLayout( new GridLayout() );
    setControl( panel );

    final Control geometryControl = createGeometryControl( panel );
    geometryControl.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );

    final Control fieldsControl = createFieldsControl( panel );
    fieldsControl.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
  }

  private Control createGeometryControl( final Composite parent )
  {
    final Group group = new Group( parent, SWT.NONE );
    group.setText( "Geometry" );
    group.setLayout( new GridLayout( 2, false ) );

    // Let user choose shape type
    final Label typeLabel = new Label( group, SWT.NONE );
    typeLabel.setText( "Shape Type" );

    final ComboViewer typeViewer = new ComboViewer( group, SWT.READ_ONLY | SWT.DROP_DOWN );
    typeViewer.setContentProvider( new ArrayContentProvider() );
    typeViewer.setLabelProvider( new LabelProvider()
    {
      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
       */
      @Override
      public String getText( final Object element )
      {
        return ((ShapeType) element).getLabel();
      }
    } );

    typeViewer.setInput( ShapeType.values() );

    typeViewer.setSelection( new StructuredSelection( m_signature.getShapeType() ) );

    typeViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final ShapeSignature signature = getSignature();
        final ShapeType selectedType = (ShapeType) ((IStructuredSelection) event.getSelection()).getFirstElement();
        signature.setShapeType( selectedType );
      }
    } );

    // Let user choose which geometry to export

    return group;
  }

  protected ShapeSignature getSignature( )
  {
    return m_signature;
  }

  private Control createFieldsControl( final Composite parent )
  {
    final Group group = new Group( parent, SWT.NONE );
    group.setLayout( new FillLayout() );
    group.setText( "Fields" );

    final TableViewer tableViewer = new TableViewer( group, SWT.BORDER );

    tableViewer.setContentProvider( new ArrayContentProvider() );
    tableViewer.setLabelProvider( new ViewerColumLabelProvider( tableViewer ) );

    final TableViewerColumn nameColumn = new TableViewerColumn( tableViewer, SWT.LEFT );

    nameColumn.setLabelProvider( new FieldNameLabelProvider() );
    nameColumn.getColumn().setText( "Field Name" );
    nameColumn.getColumn().setWidth( 100 );
    nameColumn.setEditingSupport( new FieldNameEditingSupport( tableViewer ) );

    final TableViewerColumn typeColumn = new TableViewerColumn( tableViewer, SWT.LEFT );
    typeColumn.setLabelProvider( new FieldTypeLabelProvider() );
    typeColumn.getColumn().setText( "Field Type" );

    final TableViewerColumn lengthColumn = new TableViewerColumn( tableViewer, SWT.LEFT );
    lengthColumn.setLabelProvider( new FieldLengthLabelProvider() );
    lengthColumn.getColumn().setText( "Field Length" );

    final TableViewerColumn decimalColumn = new TableViewerColumn( tableViewer, SWT.LEFT );
    decimalColumn.setLabelProvider( new FieldDecimalsLabelProvider() );
    decimalColumn.getColumn().setText( "Field Decimals" );

    final TableViewerColumn sourceColumn = new TableViewerColumn( tableViewer, SWT.LEFT );
    sourceColumn.setLabelProvider( new FieldSourceLabelProvider() );
    sourceColumn.getColumn().setText( "Source" );

    tableViewer.setInput( m_signature.getFields() );

    final Table table = tableViewer.getTable();
    table.setHeaderVisible( true );
    final TableColumn[] columns = table.getColumns();
    for( final TableColumn tableColumn : columns )
      tableColumn.pack();

    return group;
  }

}
