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

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.edit.EditConfigAttributes;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.grid.data.DefaultCornerDataProvider;
import net.sourceforge.nattable.grid.layer.CornerLayer;
import net.sourceforge.nattable.grid.layer.GridLayer;
import net.sourceforge.nattable.grid.layer.config.DefaultGridLayerConfiguration;
import net.sourceforge.nattable.layer.CompositeLayer;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.event.VisualRefreshEvent;
import net.sourceforge.nattable.painter.cell.decorator.BeveledBorderDecorator;
import net.sourceforge.nattable.style.DisplayMode;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.core.runtime.jobs.MutexRule;
import org.kalypso.zml.core.table.model.IZmlColumnModelListener;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.event.ZmlModelColumnChangeType;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.nat.base.ZmlModelCellDisplayConverter;
import org.kalypso.zml.ui.table.nat.base.ZmlModelRowHeaderDisplayConverter;
import org.kalypso.zml.ui.table.nat.context.menu.NatTableContextMenuSupport;
import org.kalypso.zml.ui.table.nat.editing.ZmlDefaultNumericDataValidator;
import org.kalypso.zml.ui.table.nat.editing.ZmlEditBindings;
import org.kalypso.zml.ui.table.nat.editing.ZmlModelColumnEditingRule;
import org.kalypso.zml.ui.table.nat.editing.ZmlTableCellEditorFacade;
import org.kalypso.zml.ui.table.nat.layers.BodyLayerStack;
import org.kalypso.zml.ui.table.nat.layers.ColumnHeaderLayerStack;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;
import org.kalypso.zml.ui.table.nat.layers.RowHeaderLayerStack;
import org.kalypso.zml.ui.table.nat.pager.DefaultZmlTablePagerCallback;
import org.kalypso.zml.ui.table.nat.pager.IZmlTablePagerCallback;
import org.kalypso.zml.ui.table.nat.pager.UpdateChartSelectionListener;
import org.kalypso.zml.ui.table.nat.painter.ZmlColumnHeaderCellPainter;
import org.kalypso.zml.ui.table.nat.painter.ZmlModelCellPainter;
import org.kalypso.zml.ui.table.nat.painter.ZmlRowHeaderCellPainter;
import org.kalypso.zml.ui.table.nat.tooltip.ZmlTableTooltip;

/**
 * @author Dirk Kuch
 */
public class ZmlTable extends Composite implements IZmlTable
{
  private final UIJob m_updateJob = new UIJob( "Zeitreihen-Tabelle wird aktualisiert" )
  {
    @Override
    public IStatus runInUIThread( final IProgressMonitor monitor )
    {
      return handleTableRefresh( monitor );
    }
  };

  private final ControlListener m_resizeListener = new ControlAdapter()
  {
    @Override
    public void controlResized( final ControlEvent e )
    {
      handleControlResized();
    }
  };

  private IZmlTablePagerCallback m_callback;

  private static final MutexRule MUTEX_TABLE_UPDATE = new MutexRule( "Aktualisiere Tabelle" ); // $NON-NLS-1$

  private NatTable m_table;

  private BodyLayerStack m_bodyLayer;

  private final ZmlModelViewport m_viewport;

  private GridLayer m_gridLayer;

  public ZmlTable( final Composite parent, final int style, final IZmlModel model, final FormToolkit toolkit )
  {
    super( parent, style );

    toolkit.adapt( this );
    GridLayoutFactory.fillDefaults().applyTo( this );

    m_updateJob.setUser( false );
    m_updateJob.setSystem( true );
    m_updateJob.setRule( MUTEX_TABLE_UPDATE );

    m_viewport = new ZmlModelViewport( model );
    m_viewport.addListener( new IZmlColumnModelListener()
    {
      @Override
      public void modelChanged( final ZmlModelColumnChangeType event )
      {
        refresh( event );
      }
    } );

    doInit( toolkit );

    m_table.addControlListener( m_resizeListener );
  }

  private void doInit( final FormToolkit toolkit )
  {
    m_bodyLayer = new BodyLayerStack( m_viewport );

    final ColumnHeaderLayerStack columnHeaderLayer = new ColumnHeaderLayerStack( m_bodyLayer );
    final RowHeaderLayerStack rowHeaderLayer = new RowHeaderLayerStack( m_bodyLayer );

    final DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider( columnHeaderLayer.getProvider(), rowHeaderLayer.getProvider() );
    final CornerLayer cornerLayer = new CornerLayer( new DataLayer( cornerDataProvider ), rowHeaderLayer, columnHeaderLayer );

    m_gridLayer = new GridLayer( m_bodyLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer, false );
    m_gridLayer.addConfiguration( new DefaultGridLayerConfiguration( m_gridLayer )
    {
      @Override
      protected void addAlternateRowColoringConfig( final CompositeLayer layer )
      {
        // disable alternating row coloring
      }

      @Override
      protected void addEditingUIConfig( )
      {
        addConfiguration( new ZmlEditBindings( ZmlTable.this ) );
      }
    } );

    m_table = new NatTable( this, m_gridLayer ); // no default style because of cell backgrounds
    m_table.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    toolkit.adapt( m_table );

    final IConfigRegistry registry = m_table.getConfigRegistry();

    /** value converters */
    final ZmlModelCellDisplayConverter converter = new ZmlModelCellDisplayConverter( m_viewport );
    registry.registerConfigAttribute( CellConfigAttributes.DISPLAY_CONVERTER, converter, DisplayMode.NORMAL, GridRegion.BODY.toString() );
    registry.registerConfigAttribute( CellConfigAttributes.DISPLAY_CONVERTER, new ZmlModelRowHeaderDisplayConverter(), DisplayMode.NORMAL, GridRegion.ROW_HEADER.toString() );
    registry.registerConfigAttribute( CellConfigAttributes.DISPLAY_CONVERTER, converter, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER.toString() );

    /** cell painters */
    registry.registerConfigAttribute( CellConfigAttributes.CELL_PAINTER, new ZmlModelCellPainter( m_viewport ), DisplayMode.NORMAL, GridRegion.BODY.toString() );
    registry.registerConfigAttribute( CellConfigAttributes.CELL_PAINTER, new ZmlRowHeaderCellPainter( m_viewport ), DisplayMode.NORMAL, GridRegion.ROW_HEADER.toString() );
// registry.registerConfigAttribute( CellConfigAttributes.CELL_PAINTER, new ZmlColumnHeaderCellPainter( m_viewport ),
// DisplayMode.NORMAL, GridRegion.COLUMN_HEADER.toString() );
    registry.registerConfigAttribute( CellConfigAttributes.CELL_PAINTER, new BeveledBorderDecorator( new ZmlColumnHeaderCellPainter( m_viewport ) ), DisplayMode.NORMAL, GridRegion.COLUMN_HEADER.toString() );

    /** editing support */
    registry.registerConfigAttribute( EditConfigAttributes.CELL_EDITABLE_RULE, new ZmlModelColumnEditingRule( m_viewport ), DisplayMode.EDIT, GridRegion.BODY.toString() );
    registry.registerConfigAttribute( EditConfigAttributes.CELL_EDITOR, new ZmlTableCellEditorFacade( m_viewport ), DisplayMode.EDIT, GridRegion.BODY.toString() );

    registry.registerConfigAttribute( EditConfigAttributes.DATA_VALIDATOR, new ZmlDefaultNumericDataValidator(), DisplayMode.EDIT, GridRegion.BODY.toString() );

    new ZmlTableTooltip( m_table, getModelViewport() );

    m_table.addMouseListener( new NatTableContextMenuSupport( m_table, m_viewport, getSelection() ) );
    m_table.addLayerListener( new UpdateChartSelectionListener( getSelection() ) );

    m_callback = new DefaultZmlTablePagerCallback( this );
  }

  @Override
  public void dispose( )
  {
    m_table.dispose();
    m_callback.dispose();
    // m_viewport.dispose(); TODO

    super.dispose();
  }

  @Override
  public synchronized void refresh( final ZmlModelColumnChangeType event )
  {
    m_updateJob.cancel();

    m_updateJob.schedule( 50 );
  }

  protected IStatus handleTableRefresh( final IProgressMonitor monitor )
  {
    if( monitor.isCanceled() )
      return Status.CANCEL_STATUS;

    if( ZmlTable.this.isDisposed() )
      return Status.OK_STATUS;

    m_callback.beforeRefresh();

    m_table.fireLayerEvent( new VisualRefreshEvent( m_bodyLayer ) );
    m_table.refresh();

    doResizeColumns();

    m_callback.afterRefresh();

    return Status.OK_STATUS;
  }

  @Override
  public BodyLayerStack getBodyLayer( )
  {
    return m_bodyLayer;
  }

  @Override
  public NatTable getTable( )
  {
    return m_table;
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

  @Override
  public IZmlTablePagerCallback getCallback( )
  {
    return m_callback;
  }

  @Override
  public void setCallback( final IZmlTablePagerCallback callback )
  {
    Assert.isNotNull( callback );

    m_callback.dispose();
    m_callback = callback;
  }

  protected void handleControlResized( )
  {
    doResizeColumns();
  }

  protected void doResizeColumns( )
  {
    final ColumnResizeWorker worker = new ColumnResizeWorker( m_table, m_gridLayer );
    worker.execute();
  }
}