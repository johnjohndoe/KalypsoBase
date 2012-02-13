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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractZmlTableCellPainter implements IZmlTableCellPainter
{
  private Color m_background;

  private Color m_foreground;

  private Font m_font;

  private final IZmlModelCell m_cell;

  private Image[] m_images;

  public AbstractZmlTableCellPainter( final IZmlModelCell cell )
  {
    m_cell = cell;
  }

  @Override
  public IZmlModelCell getCell( )
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

  @Override
  public void initGc( final GC gc )
  {
    m_background = gc.getBackground();
    m_foreground = gc.getForeground();
    m_font = gc.getFont();

    final Color background = getBackground();
    if( Objects.isNotNull( background ) ) // TODO && (event.detail & SWT.SELECTED) == 0 )
      gc.setBackground( background );

    final Color foreground = getForeground();
    if( Objects.isNotNull( foreground ) )
      gc.setForeground( foreground );

    final Font font = getFont();
    if( Objects.isNotNull( font ) )
      gc.setFont( font );
  }

  protected abstract Font getFont( );

  protected abstract Color getBackground( );

  protected abstract Color getForeground( );

  @Override
  public void resetGc( final GC gc )
  {
    gc.setBackground( m_background );
    gc.setForeground( m_foreground );
    gc.setFont( m_font );
  }
}
