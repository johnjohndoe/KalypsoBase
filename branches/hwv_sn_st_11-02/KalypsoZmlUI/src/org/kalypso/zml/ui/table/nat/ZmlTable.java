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
package org.kalypso.zml.ui.table.nat;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.config.IEditableRule;
import net.sourceforge.nattable.data.validate.DefaultNumericDataValidator;
import net.sourceforge.nattable.edit.EditConfigAttributes;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.grid.data.DefaultCornerDataProvider;
import net.sourceforge.nattable.grid.layer.CornerLayer;
import net.sourceforge.nattable.grid.layer.GridLayer;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.painter.cell.decorator.BeveledBorderDecorator;
import net.sourceforge.nattable.style.DisplayMode;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.jobs.MutexRule;
import org.kalypso.contribs.eclipse.swt.layout.LayoutHelper;
import org.kalypso.zml.core.table.model.IZmlColumnModelListener;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.event.ZmlModelColumnChangeType;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableListener;
import org.kalypso.zml.ui.table.ZmlTableComposite;
import org.kalypso.zml.ui.table.nat.base.ZmlModelCellDisplayConverter;
import org.kalypso.zml.ui.table.nat.base.ZmlModelRowHeaderDisplayConverter;
import org.kalypso.zml.ui.table.nat.context.menu.NatTableContextMenuSupport;
import org.kalypso.zml.ui.table.nat.layers.BodyLayerStack;
import org.kalypso.zml.ui.table.nat.layers.ColumnHeaderLayerStack;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;
import org.kalypso.zml.ui.table.nat.layers.RowHeaderLayerStack;
import org.kalypso.zml.ui.table.nat.painter.ZmlColumnHeaderCellPainter;
import org.kalypso.zml.ui.table.nat.painter.ZmlModelCellPainter;
import org.kalypso.zml.ui.table.nat.painter.ZmlRowHeaderCellPainter;
import org.kalypso.zml.ui.table.nat.tooltip.ZmlTableTooltip;

/**
 * @author Dirk Kuch
 */
public class ZmlTable extends Composite implements IZmlTable
{
  private final Set<IZmlTableListener> m_listeners = new HashSet<IZmlTableListener>();

  private UIJob m_updateJob;

  private static final MutexRule MUTEX_TABLE_UPDATE = new MutexRule( "Aktualisiere Tabelle" ); // $NON-NLS-1$

  protected NatTable m_table;

  protected BodyLayerStack m_bodyLayer;

// final ZmlTablePager m_pager = new ZmlTablePager( this ); // only for main table

  protected final ZmlModelViewport m_viewport;

  public ZmlTable( final ZmlTableComposite table, final IZmlModel model, final FormToolkit toolkit )
  {
    super( table, SWT.NULL );
    m_viewport = new ZmlModelViewport( model );

    m_viewport.addListener( new IZmlColumnModelListener()
    {
      @Override
      public void modelChanged( final ZmlModelColumnChangeType event )
      {
        refresh( event );
      }
    } );

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
    m_bodyLayer = new BodyLayerStack( m_viewport );

    final ColumnHeaderLayerStack columnHeaderLayer = new ColumnHeaderLayerStack( m_bodyLayer );
    final RowHeaderLayerStack rowHeaderLayer = new RowHeaderLayerStack( m_bodyLayer );

    final DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider( columnHeaderLayer.getProvider(), rowHeaderLayer.getProvider() );
    final CornerLayer cornerLayer = new CornerLayer( new DataLayer( cornerDataProvider ), rowHeaderLayer, columnHeaderLayer );

    final GridLayer gridLayer = new GridLayer( m_bodyLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer );
    m_table = new NatTable( this, gridLayer );
    m_table.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    final IConfigRegistry registry = m_table.getConfigRegistry();

    /** value converters */
    final ZmlModelCellDisplayConverter converter = new ZmlModelCellDisplayConverter( m_viewport );
    registry.registerConfigAttribute( CellConfigAttributes.DISPLAY_CONVERTER, converter, DisplayMode.NORMAL, GridRegion.BODY.toString() );
    registry.registerConfigAttribute( CellConfigAttributes.DISPLAY_CONVERTER, new ZmlModelRowHeaderDisplayConverter(), DisplayMode.NORMAL, GridRegion.ROW_HEADER.toString() );
    registry.registerConfigAttribute( CellConfigAttributes.DISPLAY_CONVERTER, converter, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER.toString() );

    /** cell painters */
    registry.registerConfigAttribute( CellConfigAttributes.CELL_PAINTER, new ZmlModelCellPainter( m_viewport ), DisplayMode.NORMAL, GridRegion.BODY.toString() );
    registry.registerConfigAttribute( CellConfigAttributes.CELL_PAINTER, new ZmlRowHeaderCellPainter( m_viewport ), DisplayMode.NORMAL, GridRegion.ROW_HEADER.toString() );
    registry.registerConfigAttribute( CellConfigAttributes.CELL_PAINTER, new BeveledBorderDecorator( new ZmlColumnHeaderCellPainter( m_viewport ) ), DisplayMode.NORMAL, GridRegion.COLUMN_HEADER.toString() );

    /** editing support */
    // FIXME implement IEdiableRule
    registry.registerConfigAttribute( EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, GridRegion.BODY.toString() );
    registry.registerConfigAttribute( EditConfigAttributes.DATA_VALIDATOR, new DefaultNumericDataValidator(), DisplayMode.EDIT, "myCellLabel" );

    addTooltipSupport();

    m_table.addMouseListener( new NatTableContextMenuSupport( m_table, m_viewport ) );
  }

  private void addTooltipSupport( )
  {
    final DefaultToolTip toolTip = new ZmlTableTooltip( m_table, getModelViewport() );
    toolTip.setBackgroundColor( m_table.getDisplay().getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
    toolTip.setPopupDelay( 500 );
    toolTip.activate();
    toolTip.setShift( new Point( 10, 10 ) );
  }

  @Override
  public void dispose( )
  {
    m_table.dispose();

    super.dispose();
  }

  @Override
  public synchronized void refresh( final ZmlModelColumnChangeType event )
  {
    if( Objects.isNotNull( m_updateJob ) )
      m_updateJob.cancel();

    m_updateJob = new UIJob( "Zeitreihen-Tabelle wird aktualisiert" )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        if( monitor.isCanceled() )
          return Status.CANCEL_STATUS;

        if( ZmlTable.this.isDisposed() )
          return Status.OK_STATUS;

        // TOOD event based table refresh

// if( event.doForceChange() )
        m_table.redraw();
// m_pager.update();

// m_pager.reveal();
// m_table.fireLayerEvent( new RowUpdateEvent( m_bodyLayer, 0 ) );
// m_table.redraw();

// final int count = m_table.getColumnCount();
// for( int index = 1; index < count; index++ )
// {
// final InitializeAutoResizeColumnsCommand command = new InitializeAutoResizeColumnsCommand( m_table, count,
// m_table.getConfigRegistry(), new GCFactory( m_table ) );
// m_table.doCommand( command );
// }

        return Status.OK_STATUS;
      }
    };

    m_updateJob.setUser( false );
    m_updateJob.setSystem( true );

    m_updateJob.setRule( MUTEX_TABLE_UPDATE );
    m_updateJob.schedule( 150 );

  }

  @Override
  public ZmlModelViewport getModelViewport( )
  {
    return m_viewport;
  }

  @Override
  public IZmlTableSelection getSelection( )
  {
    return m_bodyLayer.getSelection();
  }

}
