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
import org.kalypso.contribs.eclipse.swt.layout.LayoutHelper;
import org.kalypso.zml.ui.table.commands.toolbar.view.ZmlViewResolutionFilter;
import org.kalypso.zml.ui.table.model.rows.IZmlTableRow;
import org.kalypso.zml.ui.table.provider.ZmlTableContentProvider;
import org.kalypso.zml.ui.table.provider.rendering.cell.ZmlTableCellPaintListener;

/**
 * @author Dirk Kuch
 */
public class ZmlMainTable extends Composite
{
  private TableViewer m_tableViewer;

  private final ZmlTableComposite m_table;

  private ZmlTableContentProvider m_contentProvider;

  private ZmlViewResolutionFilter m_filter;

  public ZmlMainTable( final ZmlTableComposite table, final FormToolkit toolkit )
  {
    super( table, SWT.NULL );
    m_table = table;

    final GridLayout layout = LayoutHelper.createGridLayout();
    layout.verticalSpacing = 0;
    setLayout( layout );

    doInit();

    toolkit.adapt( this );
  }

  private void doInit( )
  {
    m_tableViewer = new TableViewer( this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION );
    m_tableViewer.getTable().setLinesVisible( true );
    m_tableViewer.setUseHashlookup( true );

    ColumnViewerToolTipSupport.enableFor( m_tableViewer, ToolTip.NO_RECREATE );

    m_contentProvider = new ZmlTableContentProvider( m_table );
    m_tableViewer.setContentProvider( m_contentProvider );

    addEmptyColumn();

    m_filter = new ZmlViewResolutionFilter( m_table );
    m_tableViewer.addFilter( m_filter );

    m_tableViewer.setInput( m_table.getModel() );

    final Table table = m_tableViewer.getTable();
    final ZmlTableCellPaintListener paintListener = new ZmlTableCellPaintListener( m_table );
    table.addListener( SWT.EraseItem, paintListener );
    table.addListener( SWT.MeasureItem, paintListener );
    table.addListener( SWT.PaintItem, paintListener );

    /** layout stuff */
    table.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    table.setHeaderVisible( true );
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

  public TableViewer getTableViewer( )
  {
    return m_tableViewer;
  }

  public int getResolution( )
  {
    return m_filter.getResolution();
  }

  public IZmlTableRow[] getRows( )
  {
    return m_contentProvider.getRows();
  }

}
