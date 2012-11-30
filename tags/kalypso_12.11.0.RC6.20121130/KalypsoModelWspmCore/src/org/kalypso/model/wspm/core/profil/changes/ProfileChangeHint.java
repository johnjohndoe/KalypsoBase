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
package org.kalypso.model.wspm.core.profil.changes;

/**
 * @author belger
 */
public class ProfileChangeHint
{
  public static final int OBJECT_CHANGED = 1;

  public static final int OBJECT_DATA_CHANGED = 2;

  public static final int POINT_VALUES_CHANGED = 4;

  /* Profile properties (i.e. component of tuple model) has changed */
  public static final int POINT_PROPERTIES_CHANGED = 8;

  public static final int POINTS_CHANGED = 16;

  public static final int MARKER_DATA_CHANGED = 32;

  public static final int MARKER_MOVED = 64;

  // FIXME: this is not clear!
  public static final int PROFILE_PROPERTY_CHANGED = 128;

  public static final int ACTIVE_PROPERTY_CHANGED = 256;

  public static final int SELECTION_CHANGED = 512;

  public static final int SELECTION_CURSOR_CHANGED = 1024;

  public static final int ACTIVE_POINTS_CHANGED = 2048;

  // FIXME: neads cleanup, makes no sense, chaos!
  public static final int DATA_CHANGED = OBJECT_CHANGED | OBJECT_DATA_CHANGED | POINT_VALUES_CHANGED | POINT_PROPERTIES_CHANGED | POINTS_CHANGED | MARKER_DATA_CHANGED | MARKER_MOVED
      | PROFILE_PROPERTY_CHANGED | ACTIVE_POINTS_CHANGED;

  private int m_event = 0;

  public ProfileChangeHint( )
  {
  }

  public ProfileChangeHint( final int mask )
  {
    m_event = mask;
  }

  public int getEvent( )
  {
    return m_event;
  }

  public void setObjectChanged( )
  {
    m_event |= OBJECT_CHANGED;
  }

  /** true, if building was added or removed or replaced */
  public boolean isObjectChanged( )
  {
    return (m_event & OBJECT_CHANGED) != 0;
  }

  public void setObjectDataChanged( )
  {
    m_event |= OBJECT_DATA_CHANGED;
  }

  /**
   * true, if data of the building was changed
   */
  public boolean isObjectDataChanged( )
  {
    return (m_event & OBJECT_DATA_CHANGED) != 0;
  }

  public void setPointValuesChanged( )
  {
    m_event |= POINT_VALUES_CHANGED;
  }

  /**
   * true, if values of one ore more point were changed
   */
  public boolean isPointValuesChanged( )
  {
    return (m_event & POINT_VALUES_CHANGED) != 0;
  }

  public void setPointPropertiesChanged( )
  {
    m_event |= POINT_PROPERTIES_CHANGED;
  }

  /**
   * true, if pointProperty was remove or added
   */
  public boolean isPointPropertiesChanged( )
  {
    return (m_event & POINT_PROPERTIES_CHANGED) != 0;
  }

  public void setPointsChanged( )
  {
    m_event |= POINTS_CHANGED;
  }

  /**
   * true if points were added or removed
   */
  public boolean isPointsChanged( )
  {
    return (m_event & POINTS_CHANGED) != 0;
  }

  public void setMarkerMoved( )
  {
    m_event |= MARKER_MOVED;
  }

  /**
   * true if one or more devider moved
   */
  public boolean isMarkerMoved( )
  {
    return (m_event & MARKER_MOVED) != 0;
  }

  public void setMarkerDataChanged( )
  {
    m_event |= MARKER_DATA_CHANGED;
  }

  /**
   * true if one or more devider changed properties
   */
  public boolean isMarkerDataChanged( )
  {
    return (m_event & MARKER_DATA_CHANGED) != 0;
  }

  public void setProfilPropertyChanged( )
  {
    m_event |= PROFILE_PROPERTY_CHANGED;
  }

  /**
   * true if profilPropertyChanged.
   */
  public boolean isProfilPropertyChanged( )
  {
    return (m_event & PROFILE_PROPERTY_CHANGED) != 0;
  }

  public void setActivePropertyChanged( )
  {
    m_event |= ACTIVE_PROPERTY_CHANGED;
  }

  public boolean isActivePropertyChanged( )
  {
    return (m_event & ACTIVE_PROPERTY_CHANGED) != 0;
  }

  public void setSelectionChanged( )
  {
    m_event |= SELECTION_CHANGED;
  }

  public boolean isSelectionChanged( )
  {
    return (m_event & SELECTION_CHANGED) != 0;
  }

  public void setSelectionCursorChanged( )
  {
    m_event |= SELECTION_CURSOR_CHANGED;
  }

  public boolean isSelectionCursorChanged( )
  {
    return (m_event & SELECTION_CURSOR_CHANGED) != 0;
  }
}
