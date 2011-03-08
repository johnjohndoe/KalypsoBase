/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.ui.table;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.action.ContributionUtils;
import org.kalypso.contribs.eclipse.jface.viewers.ArrayTreeContentProvider;
import org.kalypso.contribs.eclipse.swt.layout.LayoutHelper;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.model.IZmlColumnModelListener;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.ZmlModel;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.ZmlTableType;
import org.kalypso.zml.ui.table.commands.toolbar.view.ZmlViewResolutionFilter;
import org.kalypso.zml.ui.table.layout.ZmlTableLayoutHandler;
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.IZmlTableRow;
import org.kalypso.zml.ui.table.model.ZmlTableRow;
import org.kalypso.zml.ui.table.provider.IZmlTableSelectionHandler;
import org.kalypso.zml.ui.table.provider.ZmlTableSelectionHandler;
import org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn;
import org.kalypso.zml.ui.table.provider.strategy.IExtendedZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlTableComposite extends Composite implements IZmlColumnModelListener, IZmlTable
{
  private TableViewer m_tableViewer;

  private final Set<ExtendedZmlTableColumn> m_columns = new LinkedHashSet<ExtendedZmlTableColumn>();

  private final IZmlModel m_model;

  protected ZmlTableSelectionHandler m_selection;

  private ZmlTableUiUpdateJob m_updateJob;

  private final Set<IZmlTableListener> m_listeners = new LinkedHashSet<IZmlTableListener>();

  private ZmlViewResolutionFilter m_filter;

  private final ZmlTableLayoutHandler m_handler;

  public ZmlTableComposite( final IZmlModel model, final Composite parent, final FormToolkit toolkit )
  {
    super( parent, SWT.NULL );
    m_model = model;

    m_handler = new ZmlTableLayoutHandler( this );

    setLayout( LayoutHelper.createGridLayout() );
    setup( toolkit );

    model.addListener( this );
    toolkit.adapt( this );
  }

  private void setup( final FormToolkit toolkit )
  {
    final ZmlTableType tableType = m_model.getTableType();

    Composite toolbar = null;
    if( hasToolbar( tableType ) )
      toolbar = toolkit.createComposite( this );

    m_tableViewer = new TableViewer( this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION );
    m_tableViewer.getTable().setLinesVisible( true );

    m_selection = new ZmlTableSelectionHandler( this );

    ColumnViewerToolTipSupport.enableFor( m_tableViewer, ToolTip.NO_RECREATE );

    m_tableViewer.setContentProvider( new ArrayTreeContentProvider()
    {
      /**
       * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
       */
      @Override
      public Object[] getElements( final Object inputElement )
      {
        if( inputElement instanceof ZmlModel )
        {
          final ZmlModel model = (ZmlModel) inputElement;
          return model.getRows();
        }

        return new Object[] {};
      }
    } );

    addEmptyColumn();

    final List<JAXBElement< ? extends AbstractColumnType>> columnTypes = tableType.getColumns().getAbstractColumn();
    for( final JAXBElement< ? extends AbstractColumnType> columnType : columnTypes )
    {
      final AbstractColumnType column = columnType.getValue();

      final ZmlTableColumnBuilder builder = new ZmlTableColumnBuilder( this, new BaseColumn( column ) );
      builder.execute( new NullProgressMonitor() );
    }

    m_tableViewer.setInput( m_model );

    addBasicFilters();

    /** layout stuff */
    final Table table = m_tableViewer.getTable();
    table.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    table.setHeaderVisible( true );

    if( hasToolbar( tableType ) )
      initToolbar( tableType, toolbar, toolkit );

    refresh();
  }

  private boolean hasToolbar( final ZmlTableType tableType )
  {
    final List<String> toolbar = tableType.getToolbar();
    if( toolbar == null )
      return false;

    return !toolbar.isEmpty();
  }

  private void addBasicFilters( )
  {
    m_filter = new ZmlViewResolutionFilter( this );
    m_tableViewer.addFilter( m_filter );
  }

  private void initToolbar( final ZmlTableType tableType, final Composite composite, final FormToolkit toolkit )
  {
    /** process as job in order to handle toolbar IElementUpdate job actions */
    new UIJob( "" )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        final List<String> toolbarReferences = tableType.getToolbar();
        if( toolbarReferences == null || toolbarReferences.isEmpty() )
          return Status.OK_STATUS;

        composite.setLayout( LayoutHelper.createGridLayout() );
        composite.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

        final ToolBarManager toolBarManager = new ToolBarManager();

        final ToolBar control = toolBarManager.createControl( composite );
        control.setLayoutData( new GridData( SWT.RIGHT, GridData.FILL, true, false ) );

        for( final String reference : toolbarReferences )
        {
          ContributionUtils.populateContributionManager( PlatformUI.getWorkbench(), toolBarManager, reference );
        }

        toolBarManager.update( true );

        toolkit.adapt( control );
        composite.layout();
        layout();

        return Status.OK_STATUS;
      }
    }.schedule();
  }

  private void addEmptyColumn( )
  {
    final TableViewerColumn column = new TableViewerColumn( m_tableViewer, SWT.NULL );
    column.setLabelProvider( new ColumnLabelProvider() );
    column.getColumn().setWidth( 0 );
    column.getColumn().setResizable( false );
    column.getColumn().setMoveable( false );
  }

  @Override
  public void refresh( )
  {
    if( m_tableViewer.getTable().isDisposed() )
      return;

    for( final ExtendedZmlTableColumn column : m_columns )
    {
      column.reset();
    }

    final IStructuredSelection selection = (IStructuredSelection) m_tableViewer.getSelection();

    final IZmlTableCell cell = m_selection.getActiveCell();

    m_tableViewer.refresh();

    fireTableChanged();

    if( !selection.isEmpty() )
    {
      m_tableViewer.setSelection( selection );
      m_tableViewer.getTable().setFocus();
    }

    if( Objects.isNotNull( cell ) )
      new UIJob( "" )
      {
        @Override
        public IStatus runInUIThread( final IProgressMonitor monitor )
        {
          m_selection.setFocusCell( (Date) cell.getRow().getModelRow().getIndexValue(), cell.getColumn() );

          return Status.OK_STATUS;
        }
      }.schedule( 250 );

  }

  public void fireTableChanged( )
  {
    final IZmlTableListener[] listeners = m_listeners.toArray( new IZmlTableListener[] {} );
    for( final IZmlTableListener listener : listeners )
    {
      listener.eventTableChanged();
    }

    m_handler.tableChanged();
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.IZmlColumnModelListener#modelChanged()
   */
  @Override
  public void modelChanged( )
  {
    if( m_updateJob != null )
      m_updateJob.cancel();

    m_updateJob = new ZmlTableUiUpdateJob( this );
    m_updateJob.schedule( 250 );

  }

  @Override
  public ExtendedZmlTableColumn[] getColumns( )
  {
    return m_columns.toArray( new ExtendedZmlTableColumn[] {} );
  }

  public void accept( final IZmlTableColumnVisitor visitor )
  {
    for( final ExtendedZmlTableColumn column : getColumns() )
    {
      visitor.accept( column );
    }
  }

  @Override
  public TableViewer getTableViewer( )
  {
    return m_tableViewer;
  }

  /**
   * @see org.kalypso.zml.ui.table.IZmlTableComposite#getDataModel()
   */
  @Override
  public IZmlModel getDataModel( )
  {
    return m_model;
  }

  /**
   * @see org.kalypso.zml.ui.table.IZmlTableComposite#addListener(org.kalypso.zml.ui.table.IZmlTableListener)
   */
  @Override
  public void addListener( final IZmlTableListener listener )
  {
    m_listeners.add( listener );
  }

  /**
   * @see org.kalypso.zml.ui.table.IZmlTableComposite#removeListener(org.kalypso.zml.ui.table.IZmlTableListener)
   */
  @Override
  public void removeListener( final IZmlTableListener listener )
  {
    m_listeners.remove( listener );
  }

  /**
   * @see org.kalypso.zml.ui.table.IZmlTableComposite#getResoltion()
   */
  @Override
  public int getResolution( )
  {
    return m_filter.getResolution();
  }

  /**
   * @see org.kalypso.zml.ui.table.IZmlTable#findColumn(org.kalypso.zml.ui.table.binding.BaseColumn)
   */
  @Override
  public IZmlTableColumn findColumn( final BaseColumn column )
  {
    final IZmlTableColumn[] tableColumns = getColumns();
    for( final IZmlTableColumn tableColumn : tableColumns )
    {
      if( tableColumn.getColumnType().equals( column ) )
        return tableColumn;
    }

    return null;
  }

  /**
   * @see org.kalypso.zml.ui.table.IZmlTable#getRows()
   */
  @Override
  public IZmlTableRow[] getRows( )
  {
    final List<IZmlTableRow> rows = new ArrayList<IZmlTableRow>();

    final Table table = m_tableViewer.getTable();
    final TableItem[] items = table.getItems();
    for( final TableItem item : items )
    {
      final IZmlModelRow row = (IZmlModelRow) item.getData();
      rows.add( new ZmlTableRow( this, row ) );
    }

    return rows.toArray( new IZmlTableRow[] {} );
  }

  /**
   * @see org.kalypso.zml.ui.table.IZmlTable#getRow(int)
   */
  @Override
  public IZmlTableRow getRow( final int index )
  {
    final IZmlTableRow[] rows = getRows();
    if( index < rows.length )
      return rows[index];

    return null;
  }

  public IZmlTableColumn findColumn( final int columnIndex )
  {
    for( final ExtendedZmlTableColumn column : m_columns )
    {
      if( column.getTableColumnIndex() == columnIndex )
        return column;
    }

    return null;
  }

  /**
   * @see org.kalypso.zml.ui.table.IZmlTable#add(org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn)
   */
  @Override
  public void add( final ExtendedZmlTableColumn column )
  {
    m_columns.add( column );
  }

  public void remove( final IExtendedZmlTableColumn column )
  {
    m_columns.remove( column );
  }

  /**
   * @see org.kalypso.zml.ui.table.IZmlTable#getSelectionHandler()
   */
  @Override
  public IZmlTableSelectionHandler getSelectionHandler( )
  {
    return m_selection;
  }
}
