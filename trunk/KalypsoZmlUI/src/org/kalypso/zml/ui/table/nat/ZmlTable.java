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
import net.sourceforge.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.util.GCFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.jobs.MutexRule;
import org.kalypso.zml.core.table.model.IZmlColumnModelListener;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.event.ZmlModelColumnChangeType;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.ui.i18n.Messages;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableListener;
import org.kalypso.zml.ui.table.ZmlTableComposite;
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
import org.kalypso.zml.ui.table.nat.pager.UpdateChartSelectionListener;
import org.kalypso.zml.ui.table.nat.pager.ZmlTablePager;
import org.kalypso.zml.ui.table.nat.painter.ZmlColumnHeaderCellPainter;
import org.kalypso.zml.ui.table.nat.painter.ZmlModelCellPainter;
import org.kalypso.zml.ui.table.nat.painter.ZmlRowHeaderCellPainter;
import org.kalypso.zml.ui.table.nat.tooltip.ZmlTableTooltip;

/**
 * @author Dirk Kuch
 */
public class ZmlTable extends Composite implements IZmlTable
{
  private final Set<IZmlTableListener> m_listeners = new HashSet<>();

  private UIJob m_updateJob;

  private static final MutexRule MUTEX_TABLE_UPDATE = new MutexRule( "Aktualisiere Tabelle" ); //$NON-NLS-1$

  protected NatTable m_table;

  protected BodyLayerStack m_bodyLayer;

  protected final ZmlModelViewport m_viewport;

  private ColumnHeaderLayerStack m_columnHeaderLayer;

  private GridLayer m_gridLayer;

  protected ZmlTablePager m_pager;

  public ZmlTable( final ZmlTableComposite table, final IZmlModel model, final FormToolkit toolkit )
  {
    super( table, SWT.BORDER );

    m_viewport = new ZmlModelViewport( model );
    m_viewport.addListener( new IZmlColumnModelListener()
    {
      @Override
      public void modelChanged( final ZmlModelColumnChangeType event )
      {
        refresh( event );
      }
    } );

    final GridLayout layout = GridLayoutFactory.fillDefaults().create();
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

    m_columnHeaderLayer = new ColumnHeaderLayerStack( m_bodyLayer );
    final RowHeaderLayerStack rowHeaderLayer = new RowHeaderLayerStack( m_bodyLayer );

    final DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider( m_columnHeaderLayer.getProvider(), rowHeaderLayer.getProvider() );
    final CornerLayer cornerLayer = new CornerLayer( new DataLayer( cornerDataProvider ), rowHeaderLayer, m_columnHeaderLayer );

    m_gridLayer = new GridLayer( m_bodyLayer, m_columnHeaderLayer, rowHeaderLayer, cornerLayer, false );
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
        addConfiguration( new ZmlEditBindings( m_viewport ) );
      }
    } );

    m_table = new NatTable( this, m_gridLayer ); // no default style because of cell backgrounds
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
    registry.registerConfigAttribute( EditConfigAttributes.CELL_EDITABLE_RULE, new ZmlModelColumnEditingRule( m_viewport ), DisplayMode.EDIT, GridRegion.BODY.toString() );
    registry.registerConfigAttribute( EditConfigAttributes.CELL_EDITOR, new ZmlTableCellEditorFacade( m_viewport ), DisplayMode.EDIT, GridRegion.BODY.toString() );

    registry.registerConfigAttribute( EditConfigAttributes.DATA_VALIDATOR, new ZmlDefaultNumericDataValidator(), DisplayMode.EDIT, GridRegion.BODY.toString() );

    new ZmlTableTooltip( m_table, getModelViewport() );

    m_table.addMouseListener( new NatTableContextMenuSupport( m_table, m_viewport, getSelection() ) );
    m_table.addLayerListener( new UpdateChartSelectionListener( getSelection() ) );

    m_pager = new ZmlTablePager( m_viewport, m_table, m_bodyLayer );
  }

  @Override
  public void dispose( )
  {
    m_table.dispose();
    // m_viewport.dispose(); TODO

    super.dispose();
  }

  protected int m_event = 0;

  @Override
  public synchronized void refresh( final ZmlModelColumnChangeType event )
  {
    if( Objects.isNotNull( m_updateJob ) )
      m_updateJob.cancel();

    m_event |= event.getEvent();

    m_updateJob = new UIJob( Messages.ZmlTable_0 )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        if( monitor.isCanceled() )
          return Status.CANCEL_STATUS;

        if( ZmlTable.this.isDisposed() )
          return Status.OK_STATUS;

        final ZmlModelColumnChangeType change = new ZmlModelColumnChangeType( m_event );
        m_event = 0;

        m_table.fireLayerEvent( new VisualRefreshEvent( m_bodyLayer ) );
        m_table.refresh();

        doResizeColumns();

        m_pager.update( change );

        return Status.OK_STATUS;
      }
    };

    m_updateJob.setUser( false );
    m_updateJob.setSystem( true );

    m_updateJob.setRule( MUTEX_TABLE_UPDATE );
    m_updateJob.schedule( 333 );
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

  protected void doResizeColumns( )
  {
    final int count = m_table.getColumnCount();
    for( int index = 1; index <= count; index++ )
    {
      final InitializeAutoResizeColumnsCommand command = new InitializeAutoResizeColumnsCommand( m_gridLayer, index, m_table.getConfigRegistry(), new GCFactory( m_table ) );
      m_gridLayer.doCommand( command );
    }
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