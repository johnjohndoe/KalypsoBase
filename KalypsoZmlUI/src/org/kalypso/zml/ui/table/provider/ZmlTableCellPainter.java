/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.zml.ui.table.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.rule.ZmlRule;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.schema.AlignmentType;
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.ZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlTableCellPainter
{
  private final IZmlTableCell m_cell;

  private final ZmlLabelProvider m_provider;

  private Color m_background;

  private Color m_foreground;

  private Font m_font;

  private final ZmlRule[] m_activeRules;

  private Point m_ptr;

  private Image[] m_images;

  public ZmlTableCellPainter( final IZmlTableCell cell )
  {
    m_cell = cell;

    final ZmlTableColumn column = getColumn();

    final IZmlModelRow modelRow = cell.getRow().getModelRow();
    m_activeRules = column.findActiveRules( modelRow );
    m_provider = new ZmlLabelProvider( modelRow, column, m_activeRules );
  }

  public IZmlTableCell getCell( )
  {
    return m_cell;
  }

  public Point getExtend( final Event event )
  {
    if( Objects.isNotNull( m_ptr ) )
      return m_ptr;

    try
    {
      final String text = m_provider.getText();
      initGc( event );

      final Point ptr = drawImage( event.gc, new Rectangle( 0, 0, 1, 1 ) );
      add( ptr, event.gc.textExtent( text ) );

      resetGc( event );

      m_ptr = ptr;
      return m_ptr;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return null;
  }

  private void add( final Point base, final Point add )
  {
    base.x += add.x;
    base.y = Math.max( base.y, add.y );
  }

  public Point drawImage( final GC gc, final Rectangle bounds )
  {
    final Point ptr = new Point( 0, 0 );

    final IZmlModelRow row = m_cell.getRow().getModelRow();
    final IZmlValueReference reference = row.get( getColumn().getModelColumn() );
    if( Objects.isNull( reference ) )
      return ptr;

    final Image[] images = findImages( reference );

    for( final Image image : images )
    {
      final Rectangle rect = image.getBounds();
      final int offset = Math.max( 0, (bounds.height - rect.height) / 2 );
      gc.drawImage( image, bounds.x + ptr.x, bounds.y + offset );

      ptr.x += rect.width;
      ptr.y = Math.max( ptr.y, rect.height );
    }

    return ptr;
  }

  private Image[] findImages( final IZmlValueReference reference )
  {
    if( ArrayUtils.isNotEmpty( m_images ) )
      return m_images;

    final List<Image> images = new ArrayList<Image>();
    for( final ZmlRule rule : m_activeRules )
    {
      try
      {
        final CellStyle style = m_provider.resolveRuleStyle( rule, reference );
        if( Objects.isNull( style ) )
          continue;

        final Image image = style.getImage();
        if( Objects.isNull( image ) )
          continue;

        images.add( image );
      }
      catch( final IOException e )
      {
        e.printStackTrace();
      }
    }

    m_images = images.toArray( new Image[] {} );

    return m_images;
  }

  public Point drawText( final GC gc, final Rectangle bounds )
  {
    try
    {
      final String label = m_provider.getText();
      if( Objects.isNull( label ) )
        return new Point( 0, 0 );

      final Point extend = gc.textExtent( label );

      final AlignmentType alignment = m_cell.getColumn().getColumnType().getAlignment();
      if( AlignmentType.LEFT.equals( alignment ) )
        return drawLeftText( gc, label, bounds, extend );
      else if( AlignmentType.CENTER.equals( alignment ) )
        return drawCenterText( gc, label, bounds, extend );
      else if( AlignmentType.RIGHT.equals( alignment ) )
        return drawRightText( gc, label, bounds, extend );

      return new Point( 0, 0 );

    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return new Point( 0, 0 );
  }

  private Point drawLeftText( final GC gc, final String label, final Rectangle bounds, final Point extend )
  {
    final int offset = getOffsetY( bounds, extend );
    gc.drawText( label, bounds.x, bounds.y + offset, true );

    return extend;
  }

  private Point drawCenterText( final GC gc, final String label, final Rectangle bounds, final Point extend )
  {
    final int x1 = Math.max( bounds.x, bounds.x + (bounds.width - extend.x) / 2 );
    final int offset = getOffsetY( bounds, extend );

    gc.drawText( label, x1, bounds.y + offset, true );

    return new Point( Math.abs( bounds.x - x1 ) + extend.x, bounds.y );
  }

  private Point drawRightText( final GC gc, final String label, final Rectangle bounds, final Point extend )
  {
    int x1 = Math.max( bounds.x, bounds.x + bounds.width - extend.x ) - 1;
    final int offset = getOffsetY( bounds, extend );

    // "buffer" right border
    if( x1 != bounds.x )
    {
      final int x0 = x1 - 7;
      if( x0 > bounds.x )
        x1 = x0;
    }

    gc.drawText( label, x1, bounds.y + offset, true );

    return new Point( Math.abs( bounds.x - x1 ) + extend.x, bounds.y );
  }

  private int getOffsetY( final Rectangle bounds, final Point extend )
  {
    return Math.max( 0, (bounds.height - extend.y) / 2 );
  }

  public boolean isVisble( )
  {
    final IZmlTableColumn column = m_cell.getColumn();

    return column.isVisible();
  }

  private ZmlTableColumn getColumn( )
  {
    final IZmlTableColumn column = m_cell.getColumn();
    if( column instanceof ZmlTableColumn )
    {
      return (ZmlTableColumn) column;
    }

    throw new UnsupportedOperationException();
  }

  public void initGc( final Event event )
  {
    m_background = event.gc.getBackground();
    m_foreground = event.gc.getForeground();
    m_font = event.gc.getFont();

    final Color background = m_provider.getBackground();
    if( Objects.isNotNull( background ) && (event.detail & SWT.SELECTED) == 0 )
      event.gc.setBackground( background );

    final Color foreground = m_provider.getForeground();
    if( Objects.isNotNull( foreground ) )
      event.gc.setForeground( foreground );

    final Font font = m_provider.getFont();
    if( Objects.isNotNull( font ) )
      event.gc.setFont( font );
  }

  public void resetGc( final Event event )
  {
    event.gc.setBackground( m_background );
    event.gc.setForeground( m_foreground );
    event.gc.setFont( m_font );
  }

  public void drawBackground( final Event event )
  {
    event.gc.fillRectangle( new Rectangle( event.x, event.y, event.width, event.height ) );
  }

}