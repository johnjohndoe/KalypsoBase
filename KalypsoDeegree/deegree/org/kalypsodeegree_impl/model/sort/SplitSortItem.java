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
package org.kalypsodeegree_impl.model.sort;

import com.infomatiq.jsi.Rectangle;

/**
 * Elements of the {@link SplitSort}.
 *
 * @author Gernot Belger
 */
class SplitSortItem
{
  private final Object m_data;

  private Rectangle m_envelope;

  public SplitSortItem( final Object data )
  {
    m_data = data;
  }

  public Object getData( )
  {
    return m_data;
  }

  /**
   * @return <code>true</code> iff the previous envelope was different form the new one.
   */
  public boolean setEnvelope( final Rectangle envelope )
  {
    if( m_envelope == null )
    {
      if( envelope == null )
        return false;

      m_envelope = envelope;
      return true;
    }
    else
    {
      if( m_envelope.equals( envelope ) )
        return false;

      m_envelope = envelope;
      return true;
    }
  }

  public Rectangle getEnvelope( )
  {
    return m_envelope;
  }
}