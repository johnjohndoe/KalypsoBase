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
package org.kalypso.zml.ui.table.provider;

import java.io.IOException;

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
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;
import org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlTabelCellRenderer
{
  private final IZmlTableCell m_cell;

  private final ZmlLabelProvider m_provider;

  private Color m_background;

  private Color m_foreground;

  private Font m_font;

  public ZmlTabelCellRenderer( final IZmlTableCell cell )
  {
    m_cell = cell;

    m_provider = new ZmlLabelProvider( cell.getRow().getModelRow(), getColumn() );
  }

  public IZmlTableCell getCell( )
  {
    return m_cell;
  }

  public Point getExtend( final Event event )
  {
    try
    {
      final String text = m_provider.getText();
      initGc( event );

      final Point ptr = drawImage( event.gc, new Rectangle( 0, 0, 1, 1 ) );
      add( ptr, event.gc.textExtent( text ) );

      resetGc( event );

      return ptr;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return new Point( 10, 10 );
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
    final ExtendedZmlTableColumn column = getColumn();
    final ZmlRule[] rules = column.findActiveRules( row );
    final IZmlValueReference reference = row.get( column.getModelColumn() );
    if( Objects.isNull( reference ) )
      return ptr;

    for( final ZmlRule rule : rules )
    {
      try
      {
        final CellStyle style = m_provider.resolveRuleStyle( rule, reference );
        if( Objects.isNull( style ) )
          continue;

        final Image image = style.getImage();
        if( Objects.isNull( image ) )
          continue;

        final Rectangle rect = image.getBounds();
        final int offset = Math.max( 0, (bounds.height - rect.height) / 2 );
        gc.drawImage( image, bounds.x + ptr.x, bounds.y + offset );

        ptr.x += rect.width;
        ptr.y = Math.max( ptr.y, rect.height );
      }
      catch( final IOException e )
      {
        e.printStackTrace();
      }
    }

    return ptr;
  }

  public Point drawText( final GC gc, final Rectangle bounds )
  {

    try
    {
      final String label = m_provider.getText();
      if( Objects.isNull( label ) )
        return new Point( 0, 0 );

      final Point extend = gc.textExtent( label );

      /** SWT.RIGHT */
      final int x1 = Math.max( bounds.x, bounds.x + bounds.width - extend.x ) - 1;
      final int offset = Math.max( 0, (bounds.height - extend.y) / 2 );

      gc.drawText( label, x1, bounds.y + offset, true );

      return new Point( Math.abs( x1 + extend.x - bounds.x ), bounds.y );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return new Point( 0, 0 );
  }

  public boolean isVisble( )
  {
    final ExtendedZmlTableColumn column = getColumn();

    return column.isVisible();
  }

  private ExtendedZmlTableColumn getColumn( )
  {
    final IZmlTableColumn column = m_cell.getColumn();
    if( column instanceof ExtendedZmlTableColumn )
    {
      return (ExtendedZmlTableColumn) column;
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
