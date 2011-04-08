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
package org.kalypso.contribs.eclipse.jobs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.contribs.eclipse.jobs.BufferPaintJob.IPaintable;

/**
 * @author Gernot Belger
 */
public class TextPaintable implements IPaintable
{
  private final Point m_size;
  private final String m_message;
  private final Color m_bgColor;

  public TextPaintable( final Point size, final String message, final Color bgColor )
  {
    m_size = size;
    m_message = message;
    m_bgColor = bgColor;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jobs.BufferPaintJob.IPaintable#getSize()
   */
  @Override
  public Point getSize( )
  {
    return m_size;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jobs.BufferPaintJob.IPaintable#paint(java.awt.Graphics2D,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void paint( final Graphics2D g, final IProgressMonitor monitor )
  {
    if( m_bgColor != null )
    {
      g.setBackground( m_bgColor );
      g.fillRect( 0, 0, m_size.x, m_size.y );
    }

    g.setColor( new Color( 0, 0, 0 ) );

    final Rectangle2D bounds = g.getFontMetrics().getStringBounds( m_message, g );

    /* Centre text onto visible rectangle */
    final float x = (float) (m_size.x - bounds.getWidth()) / 2;
    final float y = (float) (m_size.y - bounds.getHeight()) / 2;
    g.drawString( m_message, x, y );
  }

}
