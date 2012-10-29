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

import de.openali.odysseus.chart.framework.model.layer.EditInfo;

/**
 * @author Gernot Belger
 */
public class BarRectangle implements IRectangleProvider
{
  private final Rectangle m_rectangle;

  private final Set<String> m_styleNames = new LinkedHashSet<>();

  private EditInfo m_info;

  public BarRectangle( final Rectangle rectangle, final String[] styleNames, final EditInfo info )
  {
    m_rectangle = rectangle;
    m_info = info;
    m_styleNames.addAll( Arrays.asList( styleNames ) );
  }

  public void addStyle( final String... styles )
  {
    m_styleNames.addAll( Arrays.asList( styles ) );
  }

  @Override
  public Rectangle getRectangle( )
  {
    return m_rectangle;
  }

  public String[] getStyles( )
  {
    return m_styleNames.toArray( new String[m_styleNames.size()] );
  }

  public EditInfo getEditInfo( )
  {
    return m_info;
  }

  public void setEditInfo( final EditInfo info )
  {
    m_info = info;
  }
}