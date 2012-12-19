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
package org.kalypso.zml.core.table.model.event;

/**
 * @author Dirk Kuch
 */
public class ZmlModelColumnChangeType implements IZmlModelColumnEvent
{
  private final int m_type;

  public ZmlModelColumnChangeType( final int type )
  {
    m_type = type;
  }

  public boolean structureChanged( )
  {
    return (m_type & IZmlModelColumnEvent.STRUCTURE_CHANGE) != 0;
  }

  public boolean valuesChanged( )
  {
    return (m_type & IZmlModelColumnEvent.VALUE_CHANGED) != 0;
  }

  public boolean disposed( )
  {
    return (m_type & IZmlModelColumnEvent.COLUMN_DISPOSED) != 0;
  }

  public boolean rulesChanged( )
  {
    return (m_type & IZmlModelColumnEvent.COLUMN_RULES_CHANGED) != 0;
  }

  public boolean ignoreTypeChanged( )
  {
    return (m_type & IZmlModelColumnEvent.IGNORE_TYPES_CHANGED) != 0;
  }

  public boolean resultionChanged( )
  {
    return (m_type & IZmlModelColumnEvent.RESULUTION_CHANGED) != 0;
  }

  public boolean doForceChange( )
  {
    if( disposed() )
      return true;
    else if( structureChanged() )
      return true;

    return false;
  }

  public int getEvent( )
  {
    return m_type;
  }
}