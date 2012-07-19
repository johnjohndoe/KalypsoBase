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
package de.openali.odysseus.chart.ext.base.layer;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.swt.graphics.Rectangle;

/**
 * @author Gernot Belger
 */
public class BarRectangle
{
  private final Rectangle m_rectangle;

  private Object m_data;

  private final Set<String> m_styleNames = new LinkedHashSet<>();

  public BarRectangle( final Object data, final Rectangle rectangle, final String[] styleNames )
  {
    m_data = data;
    m_rectangle = rectangle;
    m_styleNames.addAll( Arrays.asList( styleNames ) );
  }

  public void addStyle( final String... styles )
  {
    m_styleNames.addAll( Arrays.asList( styles ) );
  }

  public Rectangle getRectangle( )
  {
    return m_rectangle;
  }

  public String[] getStyles( )
  {
    return m_styleNames.toArray( new String[m_styleNames.size()] );
  }

  public Object getData( )
  {
    return m_data;
  }

  public void setData( final Object data )
  {
    m_data = data;
  }
}