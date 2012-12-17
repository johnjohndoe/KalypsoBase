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
package org.kalypso.ogc.gml.om.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.jface.viewers.ColumnViewerUtil;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerColumnItem;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerUtilities;
import org.kalypso.contribs.eclipse.jface.viewers.table.ColumnWidthInfo;
import org.kalypso.contribs.eclipse.jface.viewers.table.ColumnsResizeControlListener;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.ITupleResultChangedListener;
import org.kalypso.observation.result.ITupleResultChangedListener.TYPE;
import org.kalypso.observation.result.ITupleResultChangedListener.ValueChange;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.table.handlers.ComponentUiFirstColumnHandler;
import org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler;
import org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandlerProvider;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * @author Marc Schlienger
 */
public class TupleResultContentProvider2 implements IStructuredContentProvider
{
  private static final String DUMMY = "dummy"; //$NON-NLS-1$

  private static final String DATA_HANDLER = "columnHandler"; //$NON-NLS-1$

  private final UIJob m_updateColumnsJob = new UIJob( Messages.getString( "org.kalypso.ogc.gml.om.table.TupleResultContentProvider.1" ) ) //$NON-NLS-1$
  {
    @Override
    public IStatus runInUIThread( final IProgressMonitor monitor )
    {
      final TableViewer tableViewer = getTableViewer();
      final Table table = tableViewer.getTable();
      if( table.isDisposed() )
        return Status.OK_STATUS;

      refreshColumns();
      tableViewer.refresh();

      return Status.OK_STATUS;
    }
  };

  private final UIJob m_refreshTableJob = new UIJob( "Refresh tuple result table" ) //$NON-NLS-1$
  {
    @Override
    public IStatus runInUIThread( final IProgressMonitor monitor )
    {
      final TableViewer tableViewer = getTableViewer();
      final Table table = tableViewer.getTable();
      if( table.isDisposed() )
        return Status.OK_STATUS;

      tableViewer.refresh();

      // REMARK: at this place it is OK to force the selection to be shown
      // as it is quite probable that the user changed the value of the current selection
      tableViewer.getTable().showSelection();

      return Status.OK_STATUS;
    }
  };

  private final ITupleResultChangedListener m_changeListener = new ITupleResultChangedListener()
  {
    @Override
    public void valuesChanged( final ValueChange[] changes )
    {
      handleValuesChanged( changes );
    }

    @Override
    public void recordsChanged( final IRecord[] records, final TYPE type )
    {
      handleRecordsChanged( records, type );
    }

    @Override
    public void componentsChanged( final IComponent[] components, final TYPE type )
    {
      handleComponentsChanged();
    }
  };

  private final ControlListener m_columnSizeListener = new ColumnsResizeControlListener();

  private TableViewer m_viewer;

  private TupleResult m_result;

  private final IComponentUiHandlerProvider m_factory;

  public TupleResultContentProvider2( final IComponentUiHandlerProvider factory )
  {
    m_factory = factory;
    m_updateColumnsJob.setSystem( true );
  }

  /* default */
  static IComponentUiHandler[] addFakeHandler( final IComponentUiHandler[] componentHandlers )
  {
    final IComponentUiHandler[] handlerWithFake = new IComponentUiHandler[componentHandlers.length + 1];

    handlerWithFake[0] = new ComponentUiFirstColumnHandler();
    System.arraycopy( componentHandlers, 0, handlerWithFake, 1, componentHandlers.length );

    return handlerWithFake;
  }

  @Override
  public void dispose( )
  {
    // empty
  }

  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    if( m_viewer != null )
      m_viewer.getTable().removeControlListener( m_columnSizeListener );

    final TableViewer tableViewer = (TableViewer)viewer;
    m_viewer = tableViewer;

    if( m_viewer != null )
      m_viewer.getTable().addControlListener( m_columnSizeListener );

    if( oldInput instanceof TupleResult )
      ((TupleResult)oldInput).removeChangeListener( m_changeListener );

    m_result = (TupleResult)newInput;
    if( m_result != null )
    {
      // Only remove columns if input non null, because input==null may happen while disposing
      refreshColumns();
      m_result.addChangeListener( m_changeListener );
    }
  }

  protected void refreshColumns( )
  {
    removeAllColumns();

    final Map<Integer, IComponentUiHandler> componentHandlers = m_factory.createComponentHandler( m_result );

    final List<CellEditor> cellEditors = new ArrayList<>( componentHandlers.size() + 1 );
    final Collection<String> properties = new ArrayList<>( componentHandlers.size() + 1 );

    // HACK: add a 'dummy' column (size 0) ,in order to avoid the MS-Windows feature, that the first column is always
    // left-aligned
    final ComponentUiFirstColumnHandler dummyHandler = new ComponentUiFirstColumnHandler();

    addColumn( dummyHandler );
    properties.add( DUMMY );

    cellEditors.add( dummyHandler.createCellEditor( m_viewer.getTable() ) );

    for( final Entry<Integer, IComponentUiHandler> entry : componentHandlers.entrySet() )
    {
      final Integer componentIndex = entry.getKey();
      final IComponentUiHandler handler = entry.getValue();

      final String property = "" + componentIndex; //$NON-NLS-1$

      addColumn( handler );
      properties.add( property );

      cellEditors.add( handler.createCellEditor( m_viewer.getTable() ) );
    }

    m_viewer.setColumnProperties( properties.toArray( new String[properties.size()] ) );
    m_viewer.setCellEditors( cellEditors.toArray( new CellEditor[cellEditors.size()] ) );
  }

  private void removeAllColumns( )
  {
    final Table table = m_viewer.getTable();
    if( /* m_disposing || */table.isDisposed() )
      return;

    final TableColumn[] columns = table.getColumns();
    for( final TableColumn element : columns )
      element.dispose();

    m_viewer.setColumnProperties( new String[] {} );
  }

  private void addColumn( final IComponentUiHandler handler )
  {
    final int columnWidth = handler.getColumnWidth();

    final int columnStyle = handler.getColumnStyle();

    // TODO: check if cell editors work
    // final boolean editable = handler.isEditable();

    final boolean resizeable = handler.isResizeable();
    final boolean moveable = handler.isMoveable();

    final String label = handler.getColumnLabel();
    final String tooltip = handler.getColumnTooltip();

    /* create column */
    final ViewerColumn column = ColumnViewerUtil.createViewerColumn( m_viewer, columnStyle );
    final ViewerColumnItem item = new ViewerColumnItem( column );
    item.setText( label );
    item.setToolTipText( tooltip );
    item.setResizable( resizeable );
    item.setMoveable( moveable );

    item.setData( DATA_HANDLER, handler );

    // Set width, according to ColumnWidthInfo constants
    final boolean autoResize = columnWidth == ColumnWidthInfo.PACK;
    ColumnsResizeControlListener.setWidthInfo( item.getColumn(), columnWidth, autoResize );

    column.setLabelProvider( new ComponentUiHandlerLabelProvider( handler ) );

    // TODO: sortable? get from handler!
    // ColumnViewerSorter.registerSorter( column, new PdbNameComparator() );
  }

  @Override
  public Object[] getElements( final Object inputElement )
  {
    if( inputElement != null && inputElement instanceof TupleResult )
    {
      final TupleResult result = (TupleResult)inputElement;

      return result.toArray();
    }

    return null;
  }

  public TupleResult getResult( )
  {
    return m_result;
  }

  protected void handleValuesChanged( final ValueChange[] changes )
  {
    if( m_result == null )
      return;

    final IRecord[] records = new IRecord[changes.length];
    final Set<String> properties = new HashSet<>();
    for( int i = 0; i < changes.length; i++ )
    {
      final ValueChange change = changes[i];

      final IRecord record = change.getRecord();

      records[i] = record;

      final int compIndex = change.getComponent();
      properties.add( "" + compIndex ); //$NON-NLS-1$
    }

    final String[] props = properties.toArray( new String[properties.size()] );

    ViewerUtilities.update( m_viewer, records, props, true );
  }

  protected void handleRecordsChanged( final IRecord[] records, final TYPE type )
  {
    // TODO: Performance optimization needed for lots of single changes...
    final TableViewer tableViewer = m_viewer;
    final Control control = tableViewer.getControl();
    if( control.isDisposed() )
      return;

    control.getDisplay().asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        if( !control.isDisposed() )
          switch( type )
          {
            case ADDED:
              // TODO: optimize, depending on event (events must deliver more information)
              // we need the insert positions here... or the viewer should have an sorter?
              // tableViewer.add( records );
              // tableViewer.reveal( records[records.length - 1] );
              scheduleRefresh();
              break;

            case REMOVED:
              tableViewer.remove( records );
              break;

            case CHANGED:
            {
              if( records == null )
                scheduleRefresh();
              else
                tableViewer.update( records, null );
            }
              break;
          }
      }
    } );
  }

  protected void handleComponentsChanged( )
  {
    m_updateColumnsJob.cancel();
    m_updateColumnsJob.schedule( 100 );
  }

  protected void scheduleRefresh( )
  {
    /* protected against too many refresh's at once */
    m_refreshTableJob.cancel();
    m_refreshTableJob.schedule( 50 );
  }

//  IComponentUiHandler getHandler( final String property )
//  {
//    final Object[] columnProperties = m_viewer.getColumnProperties();
//    final int index = ArrayUtils.indexOf( columnProperties, property );
//    if( index == -1 )
//      return null;
//
//    return getHandler( index );
//  }

  IComponentUiHandlerProvider getFactory( )
  {
    return m_factory;
  }

  protected TableViewer getTableViewer( )
  {
    return m_viewer;
  }
}