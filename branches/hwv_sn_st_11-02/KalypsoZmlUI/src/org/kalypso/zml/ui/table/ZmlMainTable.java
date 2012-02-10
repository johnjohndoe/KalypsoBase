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
package org.kalypso.zml.ui.table;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.jobs.MutexRule;
import org.kalypso.contribs.eclipse.jface.viewers.ArrayTreeContentProvider;
import org.kalypso.contribs.eclipse.swt.layout.LayoutHelper;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.table.commands.toolbar.view.ZmlViewResolutionFilter;
import org.kalypso.zml.ui.table.focus.ZmlTableFocusCellHandler;
import org.kalypso.zml.ui.table.layout.ZmlTableLayoutHandler;
import org.kalypso.zml.ui.table.layout.ZmlTablePager;
import org.kalypso.zml.ui.table.model.IZmlTableModel;
import org.kalypso.zml.ui.table.model.ZmlMainTableModel;
import org.kalypso.zml.ui.table.model.columns.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.columns.ZmlTableColumns;
import org.kalypso.zml.ui.table.provider.rendering.cell.ZmlTableCellCache;
import org.kalypso.zml.ui.table.provider.rendering.cell.ZmlTableCellPaintListener;
import org.kalypso.zml.ui.table.selection.ZmlTableSelectionHandler;

/**
 * @author Dirk Kuch
 */
public class ZmlMainTable extends Composite implements IZmlTable
{
  private final Set<IZmlTableListener> m_listeners = new HashSet<IZmlTableListener>();

  private final ZmlTableCellCache m_cache = new ZmlTableCellCache();

  private TableViewer m_tableViewer;

  private final ZmlTableComposite m_table;

  private ZmlViewResolutionFilter m_filter;

  private ZmlTableFocusCellHandler m_focus;

  private ZmlTableSelectionHandler m_selection;

  final ZmlTablePager m_pager = new ZmlTablePager( this ); // only for main table

  private final ZmlMainTableModel m_model;

  public ZmlMainTable( final ZmlTableComposite table, final IZmlModel model, final FormToolkit toolkit )
  {
    super( table, SWT.NULL );
    m_table = table;
    m_model = new ZmlMainTableModel( this, model );

    final GridLayout layout = LayoutHelper.createGridLayout();
    layout.verticalSpacing = 0;
    setLayout( layout );

    doInit();

    toolkit.adapt( this );
  }

  public void addListener( final IZmlTableListener listener )
  {
    m_listeners.add( listener );
  }

  private void doInit( )
  {
    m_tableViewer = new TableViewer( this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION );
    m_tableViewer.getTable().setLinesVisible( true );
    m_tableViewer.setUseHashlookup( true );

    ColumnViewerToolTipSupport.enableFor( m_tableViewer, ToolTip.NO_RECREATE );

    m_tableViewer.setContentProvider( new ArrayTreeContentProvider()
    {
      @Override
      public Object[] getElements( final Object inputElement )
      {

        if( inputElement instanceof IZmlTableModel )
        {
          final IZmlTableModel model = (IZmlTableModel) inputElement;
          return model.getRows();
        }
        else
          throw new UnsupportedOperationException();

      }
    } );

    addEmptyColumn();

    m_filter = new ZmlViewResolutionFilter( this );
    m_tableViewer.addFilter( m_filter );

    m_tableViewer.setInput( m_model );

    m_selection = new ZmlTableSelectionHandler( this );

    m_focus = new ZmlTableFocusCellHandler( this );
    addListener( m_focus );
    addListener( new ZmlTableLayoutHandler( this ) );

    final Table table = m_tableViewer.getTable();
    final ZmlTableCellPaintListener paintListener = new ZmlTableCellPaintListener( this );
    table.addListener( SWT.EraseItem, paintListener );
    table.addListener( SWT.MeasureItem, paintListener );
    table.addListener( SWT.PaintItem, paintListener );

    /** layout stuff */
    table.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    table.setHeaderVisible( true );
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    m_cache.clear();

    super.dispose();
  }

  /** windows layout bug -> always add a first invisible table column */
  private void addEmptyColumn( )
  {
    final TableViewerColumn column = new TableViewerColumn( m_tableViewer, SWT.NULL );
    column.setLabelProvider( new ColumnLabelProvider() );
    column.getColumn().setWidth( 0 );
    column.getColumn().setResizable( false );
    column.getColumn().setMoveable( false );
  }

  public void refresh( )
  {
    m_tableViewer.refresh( true, true );
  }

  @Override
  public TableViewer getViewer( )
  {
    return m_tableViewer;
  }

  @Override
  public int getResolution( )
  {
    return m_filter.getResolution();
  }

  @Override
  public ZmlTableCellCache getCache( )
  {
    return m_cache;
  }

  @Override
  public ZmlTableFocusCellHandler getFocusHandler( )
  {
    return m_focus;
  }

  @Override
  public void fireTableChanged( final String type, final IZmlModelColumn... columns )
  {
    for( final IZmlTableListener listener : m_listeners )
    {
      listener.eventTableChanged( type, columns );
    }
  }

  private UIJob m_updateJob;

  private static final MutexRule MUTEX_TABLE_UPDATE = new MutexRule( "Aktualisiere Tabelle" ); // $NON-NLS-1$

  @Override
  public synchronized void refresh( final IZmlModelColumn... columns )
  {

    if( Objects.isNotNull( m_updateJob ) )
      m_updateJob.cancel();

    m_updateJob = new UIJob( "Zeitreihen-Tabelle wird aktualisiert" )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        if( ZmlMainTable.this.getViewer().getTable().isDisposed() )
          return Status.OK_STATUS;

        synchronized( this )
        {
          m_pager.update();

// addMissingColumns();

          final IZmlTableColumn[] tableColumns = ZmlTableColumns.toTableColumns( ZmlMainTable.this, true, columns );
          for( final IZmlTableColumn column : tableColumns )
          {
            column.reset();
          }

          ZmlMainTable.this.refresh();
          m_pager.reveal();

          fireTableChanged( IZmlTableCompositeListener.TYPE_REFRESH, columns );
        }

        return Status.OK_STATUS;
      }
    };
    m_updateJob.setUser( false );
    m_updateJob.setSystem( true );

    m_updateJob.setRule( MUTEX_TABLE_UPDATE );
    m_updateJob.schedule( 150 );

  }

  public ZmlViewResolutionFilter getResolutionFilter( )
  {
    return m_filter;
  }

  @Override
  public IZmlTableSelectionHandler getSelectionHandler( )
  {
    return m_selection;
  }

  @Override
  public ZmlViewResolutionFilter getResulutionFilter( )
  {
    return m_filter;
  }

  @Override
  public IZmlTableModel getModel( )
  {
    return m_model;
  }
}
