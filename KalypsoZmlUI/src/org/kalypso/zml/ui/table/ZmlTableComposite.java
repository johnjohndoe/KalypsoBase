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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.jobs.MutexRule;
import org.kalypso.contribs.eclipse.jface.action.ContributionUtils;
import org.kalypso.contribs.eclipse.jface.viewers.ArrayTreeContentProvider;
import org.kalypso.contribs.eclipse.swt.layout.LayoutHelper;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.model.IZmlColumnModelListener;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.ZmlModel;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.ZmlTableType;
import org.kalypso.zml.ui.table.commands.toolbar.view.ZmlViewResolutionFilter;
import org.kalypso.zml.ui.table.focus.IZmlTableFocusHandler;
import org.kalypso.zml.ui.table.focus.ZmlTableFocusCellHandler;
import org.kalypso.zml.ui.table.layout.ZmlTableLayoutHandler;
import org.kalypso.zml.ui.table.layout.ZmlTablePager;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.IZmlTableRow;
import org.kalypso.zml.ui.table.model.ZmlTableColumn;
import org.kalypso.zml.ui.table.model.ZmlTableColumns;
import org.kalypso.zml.ui.table.model.ZmlTableRow;
import org.kalypso.zml.ui.table.provider.ZmlTableCellCache;
import org.kalypso.zml.ui.table.provider.ZmlTableCellPaintListener;
import org.kalypso.zml.ui.table.selection.ZmlTableSelectionHandler;

/**
 * @author Dirk Kuch
 */
public class ZmlTableComposite extends Composite implements IZmlColumnModelListener, IZmlTable
{
  private static final MutexRule MUTEX_TABLE_UPDATE = new MutexRule( "updating of time series table" ); // $NON-NLS-1$

  protected TableViewer m_tableViewer;

  private final Set<ZmlTableColumn> m_columns = new LinkedHashSet<ZmlTableColumn>();

  private final IZmlModel m_model;

  private UIJob m_updateJob;

  private final Set<IZmlTableListener> m_listeners = new LinkedHashSet<IZmlTableListener>();

  private final ZmlViewResolutionFilter m_filter = new ZmlViewResolutionFilter( this );

  private ZmlTableFocusCellHandler m_focus;

  protected ZmlTableSelectionHandler m_selection;

  final ZmlTablePager m_pager = new ZmlTablePager( this );

  private final ZmlTableCellCache m_cache = new ZmlTableCellCache();

  public ZmlTableComposite( final IZmlModel model, final Composite parent, final FormToolkit toolkit )
  {
    super( parent, SWT.NULL );
    m_model = model;

    final GridLayout layout = LayoutHelper.createGridLayout();
    layout.verticalSpacing = 0;
    setLayout( layout );
    setup( toolkit );

    model.addListener( this );
    toolkit.adapt( this );
  }

  private void setup( final FormToolkit toolkit )
  {
    synchronized( this )
    {
      final ZmlTableType tableType = m_model.getTableType();

      Composite toolbar = null;
      if( hasToolbar( tableType ) )
      {
        toolbar = toolkit.createComposite( this );
        toolbar.setLayout( LayoutHelper.createGridLayout() );
        toolbar.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
      }

      m_tableViewer = new TableViewer( this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION );
      m_tableViewer.getTable().setLinesVisible( true );
      m_tableViewer.setUseHashlookup( true );

      m_selection = new ZmlTableSelectionHandler( this );

      ColumnViewerToolTipSupport.enableFor( m_tableViewer, ToolTip.NO_RECREATE );

      m_tableViewer.setContentProvider( new ArrayTreeContentProvider()
      {
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

      m_focus = new ZmlTableFocusCellHandler( this );
      addListener( m_focus );
      addListener( new ZmlTableLayoutHandler( this ) );

      final List<JAXBElement< ? extends AbstractColumnType>> columnTypes = tableType.getColumns().getAbstractColumn();
      for( final JAXBElement< ? extends AbstractColumnType> columnType : columnTypes )
      {
        final AbstractColumnType column = columnType.getValue();

        final ZmlTableColumnBuilder builder = new ZmlTableColumnBuilder( this, new BaseColumn( column ) );
        builder.execute( new NullProgressMonitor() );
      }

      m_tableViewer.setInput( m_model );

      addBasicFilters();

      final Table table = m_tableViewer.getTable();
      final ZmlTableCellPaintListener paintListener = new ZmlTableCellPaintListener( this );
      table.addListener( SWT.EraseItem, paintListener );
      table.addListener( SWT.MeasureItem, paintListener );
      table.addListener( SWT.PaintItem, paintListener );

      /** layout stuff */
      table.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
      table.setHeaderVisible( true );
      if( hasToolbar( tableType ) )
        initToolbar( tableType, toolbar, toolkit );
    }
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
    m_tableViewer.addFilter( m_filter );
  }

  private void initToolbar( final ZmlTableType tableType, final Composite composite, final FormToolkit toolkit )
  {
    /** process as job in order to handle toolbar IElementUpdate job actions */
    final List<String> toolbarReferences = tableType.getToolbar();
    if( toolbarReferences == null || toolbarReferences.isEmpty() )
      return;

    final ToolBarManager toolBarManager = new ToolBarManager( SWT.HORIZONTAL | SWT.FLAT | SWT.RIGHT );

    final ToolBar control = toolBarManager.createControl( composite );
    control.setLayoutData( new GridData( SWT.RIGHT, SWT.FILL, true, false ) );

    for( final String reference : toolbarReferences )
    {
      ContributionUtils.populateContributionManager( PlatformUI.getWorkbench(), toolBarManager, reference );
    }

    toolBarManager.update( true );
    toolkit.adapt( control );

    composite.getParent().layout( true, true );
  }

  private void addEmptyColumn( )
  {
    final TableViewerColumn column = new TableViewerColumn( m_tableViewer, SWT.NULL );
    column.setLabelProvider( new ColumnLabelProvider() );
    column.getColumn().setWidth( 0 );
    column.getColumn().setResizable( false );
    column.getColumn().setMoveable( false );
  }

  final Set<IZmlModelColumn> m_stackColumns = Collections.synchronizedSet( new LinkedHashSet<IZmlModelColumn>() );

  @Override
  public void refresh( final IZmlModelColumn... cols )
  {
    synchronized( this )
    {
      if( Objects.isNotNull( m_updateJob ) )
        m_updateJob.cancel();

      Collections.addAll( m_stackColumns, cols );

      m_updateJob = new UIJob( "Aktualisiere Zeitreihen-Tabelle" )
      {
        @Override
        public IStatus runInUIThread( final IProgressMonitor monitor )
        {
          if( m_tableViewer.getTable().isDisposed() )
            return Status.OK_STATUS;

          synchronized( this )
          {
            m_pager.update();

            final IZmlModelColumn[] stack = m_stackColumns.toArray( new IZmlModelColumn[] {} );
            m_stackColumns.clear();

            final IZmlTableColumn[] tableColumns = ZmlTableColumns.toTableColumns( ZmlTableComposite.this, true, stack );
            for( final IZmlTableColumn column : tableColumns )
            {
              column.reset();
            }

            m_tableViewer.refresh( true, true );
            m_pager.reveal();

            fireTableChanged( IZmlTableListener.TYPE_REFRESH, stack );
          }

          return Status.OK_STATUS;
        }
      };

      m_updateJob.setRule( MUTEX_TABLE_UPDATE );
      m_updateJob.schedule( 100 );
    }
  }

  @Override
  public void fireTableChanged( final String type, final IZmlModelColumn... columns )
  {
    final IZmlTableListener[] listeners = m_listeners.toArray( new IZmlTableListener[] {} );
    for( final IZmlTableListener listener : listeners )
    {
      listener.eventTableChanged( type, columns );
    }
  }

  @Override
  public void modelChanged( final IZmlModelColumn... columns )
  {
    refresh( columns );
  }

  @Override
  public ZmlTableColumn[] getColumns( )
  {
    return m_columns.toArray( new ZmlTableColumn[] {} );
  }

  @Override
  public void accept( final IZmlTableColumnVisitor visitor )
  {
    for( final ZmlTableColumn column : getColumns() )
    {
      visitor.visit( column );
    }
  }

  @Override
  public void accept( final IZmlTableRowVisitor visitor )
  {
    for( final IZmlTableRow row : getRows() )
    {
      visitor.visit( row );
    }
  }

  @Override
  public TableViewer getViewer( )
  {
    return m_tableViewer;
  }

  @Override
  public IZmlModel getDataModel( )
  {
    return m_model;
  }

  @Override
  public void addListener( final IZmlTableListener listener )
  {
    m_listeners.add( listener );
  }

  @Override
  public void removeListener( final IZmlTableListener listener )
  {
    m_listeners.remove( listener );
  }

  @Override
  public int getResolution( )
  {
    return m_filter.getResolution();
  }

  @Override
  public IZmlTableRow[] getRows( )
  {
    final List<IZmlTableRow> rows = new ArrayList<IZmlTableRow>();

    synchronized( this )
    {
      final Table table = m_tableViewer.getTable();
      final TableItem[] items = table.getItems();
      for( final TableItem item : items )
      {
        final IZmlModelRow row = (IZmlModelRow) item.getData();
        rows.add( new ZmlTableRow( this, row ) );
      }
    }

    return rows.toArray( new IZmlTableRow[] {} );
  }

  @Override
  public IZmlTableRow getRow( final int index )
  {
    if( index < 0 )
      return null;

    final IZmlTableRow[] rows = getRows();
    if( index < rows.length )
      return rows[index];

    return null;
  }

  @Override
  public IZmlTableColumn findColumn( final int columnIndex )
  {
    for( final ZmlTableColumn column : m_columns )
    {
      if( column.getTableColumnIndex() == columnIndex )
        return column;
    }

    return null;
  }

  @Override
  public void add( final ZmlTableColumn column )
  {
    m_columns.add( column );
  }

  @Override
  public IZmlTableSelectionHandler getSelectionHandler( )
  {
    return m_selection;
  }

  public boolean isEmpty( )
  {
    return ArrayUtils.isEmpty( getRows() );
  }

  @Override
  public IZmlTableFocusHandler getFocusHandler( )
  {
    return m_focus;
  }

  @Override
  public ZmlTableCellCache getCache( )
  {
    return m_cache;
  }
}
