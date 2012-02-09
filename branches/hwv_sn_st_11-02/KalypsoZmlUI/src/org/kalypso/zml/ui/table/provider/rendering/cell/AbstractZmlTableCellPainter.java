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
package org.kalypso.zml.ui.table.provider.rendering.cell;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.ui.table.model.cells.IZmlTableCell;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractZmlTableCellPainter implements IZmlTableCellPainter
{
  private Color m_background;

  private Color m_foreground;

  private Font m_font;

  private final IZmlTableCell m_cell;

  private Image[] m_images;

  public AbstractZmlTableCellPainter( final IZmlTableCell cell )
  {
    m_cell = cell;
  }

  public IZmlTableCell getCell( )
  {
    return m_cell;
  }

  protected void setImages( final Image[] images )
  {
    m_images = images;
  }

  protected Image[] getImages( )
  {
    return m_images;
  }

  public void initGc( final Event event )
  {
    m_background = event.gc.getBackground();
    m_foreground = event.gc.getForeground();
    m_font = event.gc.getFont();

    final Color background = getBackground();
    if( Objects.isNotNull( background ) && (event.detail & SWT.SELECTED) == 0 )
      event.gc.setBackground( background );

    final Color foreground = getForeground();
    if( Objects.isNotNull( foreground ) )
      event.gc.setForeground( foreground );

    final Font font = getFont();
    if( Objects.isNotNull( font ) )
      event.gc.setFont( font );
  }

  protected abstract Font getFont( );

  protected abstract Color getBackground( );

  protected abstract Color getForeground( );

  public void resetGc( final Event event )
  {
    event.gc.setBackground( m_background );
    event.gc.setForeground( m_foreground );
    event.gc.setFont( m_font );
  }
}
