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
package org.kalypso.ogc.sensor.tableview.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.java.swing.jtable.PopupMenu;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.lang.CatchRunnable;
import org.kalypso.contribs.java.swing.table.ExcelClipboardAdapter;
import org.kalypso.contribs.java.swing.table.SelectAllCellEditor;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.tableview.TableView;
import org.kalypso.ogc.sensor.tableview.TableViewColumn;
import org.kalypso.ogc.sensor.tableview.swing.editor.DoubleCellEditor;
import org.kalypso.ogc.sensor.tableview.swing.marker.ForecastLabelMarker;
import org.kalypso.ogc.sensor.tableview.swing.renderer.ColumnHeaderListener;
import org.kalypso.ogc.sensor.tableview.swing.renderer.ColumnHeaderRenderer;
import org.kalypso.ogc.sensor.tableview.swing.renderer.DateTableCellRenderer;
import org.kalypso.ogc.sensor.tableview.swing.renderer.MaskedNumberTableCellRenderer;
import org.kalypso.ogc.sensor.tableview.swing.tablemodel.ObservationTableModel;
import org.kalypso.ogc.sensor.template.IObsViewEventListener;
import org.kalypso.ogc.sensor.template.ObsViewEvent;
import org.kalypso.ogc.sensor.template.SwingEclipseUtilities;
import org.kalypso.ogc.sensor.timeseries.TimeserieUtils;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.KalypsoUIExtensions;

/**
 * A JTable that can display observations.
 * 
 * @author schlienger
 */
public class ObservationTable extends Panel implements IObsViewEventListener
{
  private final ObservationTableModel m_model;

  private final MainTable m_table;

  private final TableView m_view;

  protected final DateTableCellRenderer m_dateRenderer;

  private final boolean m_waitForSwing;

  protected String m_currentScenarioName = ""; //$NON-NLS-1$

  /** is created if an observation has a scenario property */
  private final JLabel m_label = new JLabel( "", SwingConstants.CENTER ); //$NON-NLS-1$

  /** default background color for the date renderer when not displaying forecast */
  private static final Color BG_COLOR = new Color( 222, 222, 222 );

  private final JScrollPane m_scrollPane;

  private int m_lastVisibleRow = -1;

  public ObservationTable( final TableView template )
  {
    this( template, false, true );
  }

  /**
   * @param waitForSwing
   *          when true, the events are handled synchronously in onObsviewChanged(), this is useful when you are
   *          creating the table for non-gui purposes such as in the export-document-wizard: there you need to wait for
   *          swing to be finished with updating/painting the table before doing the export, else you get unexpected
   *          results
   */
  public ObservationTable( final TableView template, final boolean waitForSwing, final boolean useContextMenu )
  {
    m_view = template;
    m_waitForSwing = waitForSwing;

    m_model = new ObservationTableModel();
    m_model.setRules( template.getRules() );

    // date renderer with time zone
    m_dateRenderer = new DateTableCellRenderer();

    final TimeZone viewzone = template.getTimezone();
    final TimeZone timezone = viewzone == null ? KalypsoCorePlugin.getDefault().getTimeZone() : viewzone;
    m_dateRenderer.setTimeZone( timezone );

    final NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setGroupingUsed( false );

    final TableColumnModel cm = new MainColumnModel( m_model );

    m_table = new MainTable( useContextMenu, nf, m_model, cm );

    final JTableHeader header = m_table.getTableHeader();
    final ColumnHeaderListener columnHeaderListener = new ColumnHeaderListener();
    header.addMouseListener( columnHeaderListener );
    header.setReorderingAllowed( false );
    header.setEnabled( true );
    addPopupMenu( header );

    final TableCellRenderer nbRenderer = new MaskedNumberTableCellRenderer( m_model );
    m_table.setDefaultRenderer( Date.class, m_dateRenderer );
    m_table.setDefaultRenderer( Number.class, nbRenderer );
    m_table.setDefaultRenderer( Double.class, nbRenderer );
    m_table.setDefaultRenderer( Float.class, nbRenderer );
    m_table.setAutoCreateColumnsFromModel( true );
    m_table.setDefaultEditor( Double.class, new SelectAllCellEditor( new DoubleCellEditor( nf, true, new Double( 0 ) ) ) );
    m_table.setCellSelectionEnabled( true );
    m_table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

    final JTable rowHeader = new JTable( m_model, new RowHeaderColumnModel() );
    rowHeader.setDefaultRenderer( Date.class, m_dateRenderer );
    rowHeader.setDefaultRenderer( Number.class, nbRenderer );
    rowHeader.setDefaultRenderer( Double.class, nbRenderer );
    rowHeader.setDefaultRenderer( Float.class, nbRenderer );
    rowHeader.setColumnSelectionAllowed( false );
    rowHeader.setCellSelectionEnabled( false );
    rowHeader.getTableHeader().setReorderingAllowed( false );
    rowHeader.getTableHeader().setDefaultRenderer( new ColumnHeaderRenderer() );
    rowHeader.setAutoCreateColumnsFromModel( true );

    // make sure that selections between the main table and the header stay in sync
    // by sharing the same model
    m_table.setSelectionModel( rowHeader.getSelectionModel() );

    final JViewport vp = new JViewport();
    vp.setView( rowHeader );
    vp.setPreferredSize( rowHeader.getPreferredSize() );

    m_scrollPane = new JScrollPane( m_table );
    m_scrollPane.setRowHeader( vp );
    m_scrollPane.setCorner( ScrollPaneConstants.UPPER_LEFT_CORNER, rowHeader.getTableHeader() );

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

    m_label.setVisible( false );
    add( m_label );

    add( m_scrollPane );

    // TODO:m initially get observations from obsView

    // removed in this.dispose()
    m_view.addObsViewEventListener( this );
  }

  private void addPopupMenu( final JTableHeader header )
  {
    try
    {
      final java.awt.PopupMenu menu = KalypsoUIExtensions.getObservationTableHeaderPopupMenu();
      if( menu != null )
        header.add( menu );

      header.addMouseListener( new MouseAdapter()
      {
        /**
         * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseReleased( final MouseEvent e )
        {
          if( MouseEvent.BUTTON3 == e.getButton() )
          {
            final Cursor cursor = header.getCursor();
            try
            {
              header.setCursor( java.awt.Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
              if( menu != null )
                menu.show( header, e.getX(), e.getY() );
            }
            finally
            {
              header.setCursor( cursor );
            }
          }

        }
      } );
    }
    catch( final CoreException e )
    {
      KalypsoGisPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

  }

  public void dispose( )
  {
    m_table.dispose();

    m_dateRenderer.clearMarkers();
    m_view.removeObsViewListener( this );

    m_model.clearColumns();
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsViewEventListener#onObsViewChanged(org.kalypso.ogc.sensor.template.ObsViewEvent)
   */
  @Override
  public final void onObsViewChanged( final ObsViewEvent evt )
  {
    // for runnable
    final ObservationTableModel model = m_model;
    final DateTableCellRenderer dateRenderer = m_dateRenderer;
    final MainTable table = m_table;

    final CatchRunnable runnable = new CatchRunnable()
    {
      @Override
      protected void runIntern( ) throws Throwable
      {
        final int evenType = evt.getType();

        // REFRESH ONE COLUMN
        if( evenType == ObsViewEvent.TYPE_ITEM_DATA_CHANGED && evt.getObject() instanceof TableViewColumn )
        {
          final TableViewColumn column = (TableViewColumn) evt.getObject();
          model.refreshColumn( column, evt.getSource() );

          analyseObservation( column.getObservation(), true );

          // also repaint header, status may have changed
          table.getTableHeader().repaint();
        }

        // REFRESH COLUMN ACCORDING TO ITS STATE
        if( evenType == ObsViewEvent.TYPE_ITEM_STATE_CHANGED && evt.getObject() instanceof TableViewColumn )
        {
          final TableViewColumn column = (TableViewColumn) evt.getObject();

          if( column.isShown() )
            model.addColumn( column );
          else
            model.removeColumn( column );

          analyseObservation( column.getObservation(), column.isShown() );
        }

        // ADD COLUMN
        if( evenType == ObsViewEvent.TYPE_ITEM_ADD && evt.getObject() instanceof TableViewColumn )
        {
          final TableViewColumn column = (TableViewColumn) evt.getObject();
          if( column.isShown() )
            model.addColumn( column );

          analyseObservation( column.getObservation(), true );

          /* Try to scroll for every added column, as the next one might be longer */
          scrollToRowForecastOrLastVisible( column.getObservation() );
        }

        // REMOVE COLUMN
        if( evenType == ObsViewEvent.TYPE_ITEM_REMOVE && evt.getObject() instanceof TableViewColumn )
        {
          final TableViewColumn column = (TableViewColumn) evt.getObject();
          model.removeColumn( column );

          analyseObservation( column.getObservation(), false );

          if( model.getColumnCount() == 0 )
          {
            updateLastVisibleRow();
            clearLabel();
            m_currentScenarioName = ""; //$NON-NLS-1$
          }
        }

        // REMOVE ALL
        if( evenType == ObsViewEvent.TYPE_ITEM_REMOVE_ALL )
        {
          updateLastVisibleRow();

          model.clearColumns();
          dateRenderer.clearMarkers();

          clearLabel();
          m_currentScenarioName = ""; //$NON-NLS-1$
        }

        // VIEW CHANGED
        if( evenType == ObsViewEvent.TYPE_VIEW_CHANGED )
        {
          final TableView view = (TableView) evt.getObject();
          model.setAlphaSort( view.isAlphaSort() );

          final TimeZone timezone = view.getTimezone();
          final TimeZone rendererTimezone = timezone == null ? KalypsoCorePlugin.getDefault().getTimeZone() : timezone;
          m_dateRenderer.setTimeZone( rendererTimezone );

          repaint();
        }
      }
    };

    SwingEclipseUtilities.invokeAndHandleError( runnable, m_waitForSwing );
  }

  protected void updateLastVisibleRow( )
  {
    final Rectangle visibleRect = m_table.getVisibleRect();

    final Point leadingPoint = new Point( visibleRect.x, visibleRect.y );
    final int rowAtPoint = m_table.rowAtPoint( leadingPoint );
    m_lastVisibleRow = rowAtPoint;
  }

  protected void scrollToRowForecastOrLastVisible( final IObservation observation )
  {
    if( observation == null )
      return;

    final int rowHeight = m_table.getRowHeight();
    /*
     * Remark: we cannot use m_table.getVisibleRect() as the visibleRect, as this might or might not be 0 at the moment.
     */
    final Rectangle visibleRect = m_scrollPane.getViewportBorderBounds();
    final int visibleRowsCount = visibleRect.height / rowHeight;

    /* We have last visible index: just scroll there */
    if( m_lastVisibleRow != -1 )
    {
      scrollTableToIndex( m_lastVisibleRow + visibleRowsCount );
      return;
    }

    /* Else, scroll to start forecast (center it on middle of table) */
    final DateRange dr = TimeserieUtils.isTargetForecast( observation );
    if( dr == null )
      return;

    final Date from = dr.getFrom();
    final int index = m_model.indexOfKey( from );
    if( index == -1 )
      return;

    /* Center the table on this row */
    final int indexCentered = index + visibleRowsCount / 2;
    scrollTableToIndex( indexCentered );
  }

  private void scrollTableToIndex( final int index )
  {
    final int rowCount = m_model.getRowCount();
    final int indexToShow = Math.min( index, rowCount - 1 );

    final Rectangle cellRect = m_table.getCellRect( indexToShow, 0, false );
    m_table.scrollRectToVisible( cellRect );
  }

  /**
   * Helper method that analyses the observation.
   * <ul>
   * <li>adds a marker to the date renderer for observations that are forecasts or remove the corresponding marker when
   * the associated column is removed from the model
   * <li>adds a label if the observation has a scenario property
   * </ul>
   * 
   * @param adding
   *          is true when the observation (actually its associated table-view-column) is added to the model
   */
  protected void analyseObservation( final IObservation obs, final boolean adding )
  {
    if( obs != null )
    {
      // check if observation is a vorhersage
      final DateRange dr = TimeserieUtils.isTargetForecast( obs );
      if( dr != null )
      {
        if( adding )
          m_dateRenderer.addMarker( new ForecastLabelMarker( dr, BG_COLOR ) );
        else
          m_dateRenderer.removeMarker( new ForecastLabelMarker( dr, BG_COLOR ) );
      }
    }
  }

  public ObservationTableModel getObservationTableModel( )
  {
    return m_model;
  }

  public TableView getTemplate( )
  {
    return m_view;
  }

  public String getCurrentScenarioName( )
  {
    return m_currentScenarioName;
  }

  public void setAlphaSortActivated( final boolean bAlphaSort )
  {
    m_model.setAlphaSort( bAlphaSort );
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsViewEventListener#onPrintObsView(org.kalypso.ogc.sensor.template.ObsViewEvent)
   */
  @Override
  public void onPrintObsView( final ObsViewEvent evt )
  {
  }

  protected void clearLabel( )
  {
    m_label.setText( "" ); //$NON-NLS-1$
    m_label.setVisible( false );
  }

  protected boolean isLabelSet( )
  {
    return m_label.isVisible();
  }

  protected void setLabel( final String txt, final Icon icon, final Color color, final Integer height, final Boolean showTxt )
  {
    final boolean bShowText;
    if( showTxt != null )
      bShowText = showTxt.booleanValue();
    else
      bShowText = true;

    if( bShowText )
      m_label.setText( txt );
    else
      m_label.setText( "" ); //$NON-NLS-1$
    m_label.setIcon( icon );
    setBackground( color );

    if( height != null )
      m_label.setPreferredSize( new Dimension( m_label.getWidth(), height.intValue() ) );

    m_label.setVisible( true );
    doLayout();
  }

  public static class MainColumnModel extends DefaultTableColumnModel
  {
    private final ObservationTableModel m_obsModel;

    public MainColumnModel( final ObservationTableModel model )
    {
      m_obsModel = model;
    }

    // ignore key column
    @Override
    public void addColumn( final TableColumn aColumn )
    {
      final IAxis sharedAxis = m_obsModel.getSharedAxis();
      if( sharedAxis != null && sharedAxis.getName().equals( aColumn.getHeaderValue() ) )
        return;

      final TableCellRenderer headerRenderer = new ColumnHeaderRenderer();
      aColumn.setHeaderRenderer( headerRenderer );
      // Overwrite header value: normally its just 'getColumnName'; but we need more information
      final Object headerValue = m_obsModel.getColumnValue( aColumn.getModelIndex() );
      aColumn.setHeaderValue( headerValue );

      // Auto-resize column
      final Component c = headerRenderer.getTableCellRendererComponent( null, aColumn.getHeaderValue(), false, false, 0, 0 );
      final int colWidth = c.getPreferredSize().width + 5;
      aColumn.setPreferredWidth( colWidth );
      aColumn.setWidth( colWidth );
      aColumn.setMinWidth( 50 );

      super.addColumn( aColumn );
    }

    public ObservationTableModel getObservationModel( )
    {
      return m_obsModel;
    }

  }

  protected static class RowHeaderColumnModel extends DefaultTableColumnModel
  {
    // just fist column, other are ignored
    @Override
    public void addColumn( final TableColumn aColumn )
    {
      if( getColumnCount() >= 1 )
        return;

      aColumn.setMaxWidth( 100 );
      aColumn.setResizable( false );
      aColumn.setMinWidth( 100 );
      aColumn.setWidth( 100 );

      super.addColumn( aColumn );
    }
  }

  private static class MainTable extends JTable
  {
    private PopupMenu m_popup = null;

    private ExcelClipboardAdapter m_excelCp = null;

    public MainTable( final boolean useContextMenu, final NumberFormat nf, final TableModel dm, final TableColumnModel cm )
    {
      super( dm, cm );

      if( useContextMenu )
      {
        m_popup = new PopupMenu( this );
        m_excelCp = new ExcelClipboardAdapter( this, nf );

        m_popup.add( new JPopupMenu.Separator() );
        m_popup.add( m_excelCp.getCopyAction() );
        m_popup.add( m_excelCp.getPasteAction() );
      }
    }

    public void dispose( )
    {
      if( m_excelCp != null )
        m_excelCp.dispose();
    }

    @Override
    protected void processMouseEvent( final MouseEvent e )
    {
      if( e.isPopupTrigger() && m_popup != null )
        m_popup.show( this, e.getX(), e.getY() );
      else
        super.processMouseEvent( e );
    }
  }
}
