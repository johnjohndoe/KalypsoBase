/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ui.editor.styleeditor.colorMapEntryTable;

import java.awt.Color;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.graphics.sld.ColorMapEntry;
import org.kalypsodeegree.graphics.sld.RasterSymbolizer;
import org.kalypsodeegree_impl.graphics.sld.ColorMapEntry_Impl;

/**
 * A table for editing color maps.
 * 
 * @author Andreas Doemming
 * @author Holger Albert
 */
public class ColorMapEntryTable
{
  private static final String NON_NEGATIVE_INTEGER_FIELD = "(\\d){1,9}"; //$NON-NLS-1$

  private static final String INTEGER_FIELD = "(-)?" + NON_NEGATIVE_INTEGER_FIELD; //$NON-NLS-1$

  private static final String NON_NEGATIVE_FLOATING_POINT_FIELD = "(\\d){1,10}\\.(\\d){1,10}"; //$NON-NLS-1$

  private static final String FLOATING_POINT_FIELD = "(-)?" + NON_NEGATIVE_FLOATING_POINT_FIELD; //$NON-NLS-1$

  private static final String LABEL_COLUMN = "label"; //$NON-NLS-1$

  private static final String QUANTITY_COLUMN = "quantity"; //$NON-NLS-1$

  private static final String COLOR_COLUMN = "color"; //$NON-NLS-1$

  private static final String OPACITY_COLUMN = "opacity"; //$NON-NLS-1$

  public static final String[] COLUMN_NAMES = new String[] { LABEL_COLUMN, QUANTITY_COLUMN, COLOR_COLUMN, OPACITY_COLUMN };

  private static final String[] COLUMN_LABLES = new String[] { "Bezeichnung", "Obere Grenze", "Farbe", "Transparenz" };

  private IKalypsoStyle m_style;

  private RasterSymbolizer m_rasterSymbolizer;

  protected TableViewer m_tableViewer;

  protected ColorMapEntryList m_entryList;

  /**
   * The constructor (testing).
   * 
   * @param parent
   *          The parent composite.
   */
  public ColorMapEntryTable( Composite parent )
  {
    m_style = null;
    m_rasterSymbolizer = null;
    m_tableViewer = null;
    m_entryList = new ColorMapEntryList();

    for( int i = 0; i < 10; i++ )
      m_entryList.addColorMapEntry( new ColorMapEntry_Impl( Color.CYAN, i * 0.1, i, "Label " + i ) );

    createControls( parent );
  }

  /**
   * The constructor.
   * 
   * @param parent
   *          The parent composite.
   * @param style
   *          The kalypso style.
   * @param rasterSymbolizer
   *          The raster symbolizer.
   */
  public ColorMapEntryTable( Composite parent, IKalypsoStyle style, RasterSymbolizer rasterSymbolizer )
  {
    m_style = style;
    m_rasterSymbolizer = rasterSymbolizer;
    m_tableViewer = null;
    m_entryList = new ColorMapEntryList();

    for( ColorMapEntry entry : rasterSymbolizer.getColorMap().values() )
      m_entryList.addColorMapEntry( entry.clone() );

    createControls( parent );
  }

  /**
   * The constructor.
   * 
   * @param parent
   *          The parent composite.
   * @param entries
   *          The color map entries.
   */
  public ColorMapEntryTable( Composite parent, ColorMapEntry[] entries )
  {
    m_style = null;
    m_rasterSymbolizer = null;
    m_tableViewer = null;
    m_entryList = new ColorMapEntryList();

    for( ColorMapEntry entry : entries )
      m_entryList.addColorMapEntry( entry.clone() );

    createControls( parent );
  }

  /**
   * This function creates the controls.
   * 
   * @param parent
   *          The parent composite.
   */
  private void createControls( Composite parent )
  {
    /* Create a composite to hold the children. */
    Composite main = new Composite( parent, SWT.NONE );
    main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    main.setLayout( new GridLayout( 4, false ) );

    /* Create the table viewer. */
    m_tableViewer = createTableViewer( main, m_entryList );
    TableColumn[] columns = m_tableViewer.getTable().getColumns();
    for( TableColumn column : columns )
      column.pack();

    /* Add the buttons. */
    createButtons( main );
  }

  /**
   * This function creates the table viewer.
   * 
   * @param parent
   *          The parent composite.
   * @param input
   *          The input.
   * @return The table viewer.
   */
  private TableViewer createTableViewer( Composite parent, ColorMapEntryList input )
  {
    /* Create the table viewer. */
    TableViewer viewer = new TableViewer( parent, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION );
    viewer.getTable().setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, true, 4, 1 ) );
    viewer.getTable().setLinesVisible( true );
    viewer.getTable().setHeaderVisible( true );
    viewer.setUseHashlookup( true );
    viewer.setColumnProperties( COLUMN_NAMES );

    /* Create the table columns. */
    createColumns( viewer );

    /* Create the cell editors. */
    createEditors( viewer );

    /* Configure the table viewer. */
    viewer.setCellModifier( new ColorMapEntryCellModifier( this ) );
    viewer.setSorter( new QuantitySorter() );
    viewer.setContentProvider( new ColorMapEntryContentProvider() );
    viewer.setLabelProvider( new ColorMapEntryLabelProvider() );
    viewer.setInput( input );

    return viewer;
  }

  /**
   * This function creates the columns for the table viewer.
   * 
   * @param parent
   *          The parent table viewer.
   */
  private void createColumns( TableViewer parent )
  {
    /* Get the table. */
    Table table = parent.getTable();

    /* Create the label column. */
    TableColumn tableColumn = new TableColumn( table, SWT.LEFT );
    tableColumn.setText( COLUMN_LABLES[0] );
    tableColumn.setWidth( 100 );

    /* Create the quantity column. */
    TableColumn quantityColumn = new TableColumn( table, SWT.RIGHT );
    quantityColumn.setText( COLUMN_LABLES[1] );
    quantityColumn.setWidth( 75 );
    quantityColumn.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        m_tableViewer.setSorter( new QuantitySorter() );
      }
    } );

    /* Create the color column. */
    TableColumn colorColumn = new TableColumn( table, SWT.CENTER );
    colorColumn.setText( COLUMN_LABLES[2] );
    colorColumn.setWidth( 50 );

    /* Create the opacity column. */
    TableColumn opacityColumn = new TableColumn( table, SWT.RIGHT );
    opacityColumn.setText( COLUMN_LABLES[3] );
    opacityColumn.setWidth( 75 );
  }

  /**
   * This function creates the cell editors for the table viewer.
   * 
   * @param parent
   *          The parent table viewer.
   */
  private void createEditors( TableViewer parent )
  {
    /* Memory for the cell editors. */
    CellEditor[] editors = new CellEditor[COLUMN_NAMES.length];

    /* Get the table. */
    Table table = parent.getTable();

    /* Create the label cell editor. */
    TextCellEditor labelEditor = new TextCellEditor( table );
    editors[0] = labelEditor;

    /* Create the quantity cell editor. */
    TextCellEditor quantityEditor = new TextCellEditor( table );
    editors[1] = quantityEditor;

    /* Create the color cell editor. */
    editors[2] = new ColorCellEditor( table );

    /* Create the opacity cell editor. */
    TextCellEditor opacityEditor = new TextCellEditor( table );
    editors[3] = opacityEditor;

    /* Configure the label cell editor. */
    Text labelText = (Text) labelEditor.getControl();
    labelText.setTextLimit( 100 );

    /* Configure the quantity cell editor. */
    Text quantityText = (Text) quantityEditor.getControl();
    quantityText.addVerifyListener( new VerifyListener()
    {
      /**
       * @see org.eclipse.swt.events.VerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
       */
      @Override
      public void verifyText( VerifyEvent e )
      {
        if( e.text.matches( INTEGER_FIELD ) || e.text.matches( FLOATING_POINT_FIELD ) )
          e.doit = true;
      }
    } );

    /* Configure the opacity cell editor. */
    Text opacityText = (Text) opacityEditor.getControl();
    opacityText.addVerifyListener( new VerifyListener()
    {
      /**
       * @see org.eclipse.swt.events.VerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
       */
      @Override
      public void verifyText( VerifyEvent e )
      {
        if( e.text.matches( NON_NEGATIVE_INTEGER_FIELD ) || e.text.matches( NON_NEGATIVE_FLOATING_POINT_FIELD ) )
          e.doit = true;
      }
    } );

    /* Set the cell editors. */
    parent.setCellEditors( editors );
  }

  /**
   * This functions creates the "Generate Range", "Add", "Delete" and "Refresh" buttons.
   * 
   * @param parent
   *          The parent composite.
   */
  private void createButtons( Composite parent )
  {
    /* Create the generate range button. */
    Button generateRangeButton = new Button( parent, SWT.PUSH | SWT.CENTER );
    generateRangeButton.setImage( ImageProvider.IMAGE_STYLEEDITOR_EDIT_COLOR_RANGE.createImage() );
    generateRangeButton.setLayoutData( new GridData( SWT.BEGINNING, SWT.TOP, false, false ) );
    generateRangeButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        generateColorRange();
      }
    } );

    /* Create the add button. */
    Button add = new Button( parent, SWT.PUSH | SWT.CENTER );
    add.setImage( ImageProvider.IMAGE_STYLEEDITOR_ADD_RULE.createImage() );
    add.setLayoutData( new GridData( SWT.BEGINNING, SWT.TOP, false, false ) );
    add.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        m_entryList.addColorMapEntry( new ColorMapEntry_Impl( Color.WHITE, 1, 0, "" ) );
      }
    } );

    /* Create the delete button. */
    Button delete = new Button( parent, SWT.PUSH | SWT.CENTER );
    delete.setImage( ImageProvider.IMAGE_STYLEEDITOR_REMOVE.createImage() );
    delete.setLayoutData( new GridData( SWT.BEGINNING, SWT.TOP, false, false ) );
    delete.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        ColorMapEntry entry = (ColorMapEntry) ((IStructuredSelection) m_tableViewer.getSelection()).getFirstElement();
        if( entry != null )
          m_entryList.removeColorMapEntry( entry );
      }
    } );

    /* Without a raster symbolizer, a refresh is not possible. */
    if( m_rasterSymbolizer != null && m_style != null )
    {
      /* Create the refresh button. */
      Button refresh = new Button( parent, SWT.PUSH | SWT.CENTER );
      refresh.setText( "&Aktualisieren" );
      refresh.setLayoutData( new GridData( SWT.END, SWT.TOP, true, false ) );
      refresh.addSelectionListener( new SelectionAdapter()
      {
        /**
         * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
        @Override
        public void widgetSelected( SelectionEvent e )
        {
          updateRasterSymbolizer();
        }
      } );
    }
    else
    {
      /* Create a empty label. */
      Label emptyLabel = new Label( parent, SWT.NONE );
      emptyLabel.setText( "" );
      emptyLabel.setLayoutData( new GridData( SWT.END, SWT.TOP, true, false ) );
    }
  }

  /**
   * This function opens a dialog for generating a color range.
   */
  protected void generateColorRange( )
  {
    if( m_tableViewer == null || m_tableViewer.getTable().isDisposed() )
      return;

    /* Create the dialog. */
    GenerateColorRangeDialog dialog = new GenerateColorRangeDialog( m_tableViewer.getTable().getDisplay().getActiveShell(), m_entryList.getColorMapEntries().toArray( new ColorMapEntry[] {} ) );

    /* Open the dialog. */
    int open = dialog.open();
    if( open == Window.CANCEL )
      return;

    /* Clear old entries. */
    m_entryList.getColorMapEntries().clear();

    /* Add all new ones. */
    ColorMapEntry[] entries = dialog.getEntries();
    for( ColorMapEntry entry : entries )
      m_entryList.addColorMapEntry( entry );
  }

  /**
   * This function updates the raster symbolizer.
   */
  protected void updateRasterSymbolizer( )
  {
    if( m_rasterSymbolizer == null || m_style == null )
      return;

    try
    {
      TreeMap<Double, ColorMapEntry> colorMap = new TreeMap<Double, ColorMapEntry>();

      List<ColorMapEntry> entries = m_entryList.getColorMapEntries();
      for( int i = 0; i < entries.size(); i++ )
      {
        ColorMapEntry entry = entries.get( i );

        if( colorMap.containsKey( new Double( entry.getQuantity() ) ) )
          throw new Exception( Messages.getString( "org.kalypso.ui.editor.styleeditor.colorMapEntryTable.ColorMapEntryTable.12" ) );

        colorMap.put( new Double( entry.getQuantity() ), entry.clone() );
      }

      m_rasterSymbolizer.setColorMap( colorMap );
      m_style.fireStyleChanged();
    }
    catch( Exception ex )
    {
      /* Open a error dialog. */
      ErrorDialog.openError( m_tableViewer.getTable().getShell(), "Error", "Es ist ein Fehler beim Aktualisieren aufgetreten...", new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), ex.getLocalizedMessage(), ex ) ); //$NON-NLS-1$ 
    }
  }

  /**
   * This function returns the color map entry list.
   * 
   * @return The color map entry list.
   */
  public ColorMapEntryList getColorMapEntryList( )
  {
    return m_entryList;
  }
}