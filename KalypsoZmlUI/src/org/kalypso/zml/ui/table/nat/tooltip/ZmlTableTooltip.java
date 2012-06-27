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
package org.kalypso.zml.ui.table.nat.tooltip;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.cell.LayerCell;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.util.StringUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.zml.core.table.binding.DataColumn;
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.core.table.model.view.ZmlModelViewportResolutionFilter;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.DataColumnType;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.i18n.Messages;

import com.google.common.base.Strings;

/**
 * @author Dirk Kuch
 */
public class ZmlTableTooltip extends DefaultToolTip
{
  private static final Image IMG = new Image( null, ZmlTableTooltip.class.getResourceAsStream( "icons/help_hint_48.png" ) ); //$NON-NLS-1$

  private final NatTable m_table;

  private final ZmlModelViewport m_viewport;

  private static boolean SHOW_TOOLTIP = true;

  public ZmlTableTooltip( final NatTable table, final ZmlModelViewport viewport )
  {
    super( table );
    m_table = table;
    m_viewport = viewport;

    setBackgroundColor( m_table.getDisplay().getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
    setPopupDelay( 500 );
    activate();
    setShift( new Point( 10, 10 ) );
  }

  @Override
  protected boolean shouldCreateToolTip( final Event event )
  {
    if( !SHOW_TOOLTIP )
      return false;

    return getText( event ) != null;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.window.ToolTip#getToolTipArea(org.eclipse.swt.widgets.Event) Implementation here means the
   * tooltip is not redrawn unless mouse hover moves outside of the current cell (the combination of ToolTip.NO_RECREATE
   * style and override of this method).
   */
  @Override
  protected Object getToolTipArea( final Event event )
  {
    final int col = m_table.getColumnPositionByX( event.x );
    final int row = m_table.getRowPositionByY( event.y );

    return new Point( col, row );
  }

  @Override
  protected String getText( final Event event )
  {
    try
    {
      final IZmlModelValueCell cell = findCell( event );
      if( Objects.isNull( cell ) )
        return null;

      final IZmlModelColumn column = cell.getColumn();

      final AbstractColumnType type = column.getDataColumn().getType();
      if( !type.isTooltip() )
        return null;

      if( type instanceof DataColumnType )
      {
        final String tip1 = getSourceTooltip( cell );
        final String tip2 = getRuleTooltip( cell );
        final String tip3 = getModelTooltip( cell );

        return StringUtilities.concat( tip1, Strings.repeat( "\n", 2 ), tip2, Strings.repeat( "\n", 2 ), tip3 ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

    return null;
  }

  private IZmlModelValueCell findCell( final Event event )
  {
    final int col = m_table.getColumnPositionByX( event.x );
    final int row = m_table.getRowPositionByY( event.y );

    final LayerCell layerCell = m_table.getCellByPosition( col, row );
    if( Objects.isNull( layerCell ) )
      return null;

    final Object objCell = layerCell.getDataValue();
    if( !(objCell instanceof IZmlModelValueCell) )
      return null;

    return (IZmlModelValueCell) objCell;
  }

  @Override
  protected Composite createToolTipContentArea( final Event event, final Composite parent )
  {
    if( !SHOW_TOOLTIP )
      return parent;

    // This is where you could get really creative with your tooltips...
    return super.createToolTipContentArea( event, parent );
  }

  private boolean isAggregated( )
  {
    final ZmlModelViewportResolutionFilter filter = m_viewport.getFilter();
    if( Objects.isNull( filter ) )
      return false;

    return filter.getResolution() > 1;
  }

  private String getRuleTooltip( final IZmlModelValueCell cell ) throws SensorException
  {
    final ZmlCellRule[] rules = cell.findActiveRules( m_viewport );
    if( ArrayUtils.isEmpty( rules ) )
      return null;

    final StringBuffer buffer = new StringBuffer();
    buffer.append( Messages.ZmlTableTooltip_0 );

    for( final ZmlCellRule rule : rules )
    {
      buffer.append( String.format( "    - %s\n", rule.getLabel( cell ) ) );//$NON-NLS-1$
    }

    return StringUtils.chomp( buffer.toString() );
  }

  private String getModelTooltip( final IZmlModelValueCell cell )
  {
    final IZmlModelColumn column = cell.getColumn();
    final DataColumn type = column.getDataColumn();

    final StringBuffer buffer = new StringBuffer();

    final String indexAxis = type.getIndexAxis();
    final String valueAxis = type.getValueAxis();

    if( Objects.isNotNull( indexAxis ) )
      buffer.append( buildInfoText( Messages.ZmlTableTooltip_1, indexAxis ) );

    if( Objects.isNotNull( valueAxis ) )
      buffer.append( buildInfoText( Messages.ZmlTableTooltip_2, valueAxis ) );

    return StringUtils.chop( buffer.toString() );
  }

  private String getSourceTooltip( final IZmlModelValueCell cell )
  {
    final StringBuffer buffer = new StringBuffer();

    try
    {
      final Object value = cell.getValue();

      final boolean aggregated = isAggregated();
      if( !aggregated )
        buffer.append( buildInfoText( Messages.ZmlTableTooltip_3, value.toString() ) );

      final Integer status = cell.getStatus();
      if( Objects.isNotNull( status ) && !aggregated )
        buffer.append( buildInfoText( Messages.ZmlTableTooltip_4, getStatus( status ) ) );

      final String source = cell.getDataSource();
      if( Objects.isNotNull( source ) )
        buffer.append( buildInfoText( Messages.ZmlTableTooltip_5, source ) );

      final String href = cell.getHref();
      if( Objects.isNotNull( href ) )
        buffer.append( buildInfoText( Messages.ZmlTableTooltip_6, href ) );

    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return StringUtils.chop( buffer.toString() );
  }

  private String getStatus( final Integer status )
  {
    return KalypsoStatusUtils.getTooltipFor( status );
  }

  private Object buildInfoText( final String label, final String value )
  {
    String tabs;
    if( label.length() > 8 )
      tabs = "\t"; //$NON-NLS-1$
    else
      tabs = "\t\t"; //$NON-NLS-1$

    String v;
    if( value.length() > 60 )
    {
      v = value.subSequence( 0, 60 ) + "\n\t\t" + value.substring( 60 ); //$NON-NLS-1$
    }
    else
      v = value;

    return String.format( "%s:%s%s\n", label, tabs, v ); //$NON-NLS-1$
  }

  @Override
  protected Image getImage( final Event event )
  {
    final IZmlModelValueCell cell = findCell( event );
    if( Objects.isNull( cell ) )
      return null;

    return IMG;
  }

  public static void setShowTooltips( final boolean showTooltips )
  {
    ZmlTableTooltip.SHOW_TOOLTIP = showTooltips;

  }

  public static boolean isShowTooltips( )
  {
    return ZmlTableTooltip.SHOW_TOOLTIP;
  }
}
