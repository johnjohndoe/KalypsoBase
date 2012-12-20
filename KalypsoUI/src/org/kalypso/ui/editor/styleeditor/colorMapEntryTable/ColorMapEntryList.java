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
package org.kalypso.ui.editor.styleeditor.colorMapEntryTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.kalypsodeegree.graphics.sld.ColorMapEntry;

public class ColorMapEntryList
{
  private final Set<IColorMapEntryViewer> m_listeners;

  private final List<ColorMapEntry> m_entries;

  public ColorMapEntryList( )
  {
    m_listeners = new HashSet<>();
    m_entries = new ArrayList<>();
  }

  public void addChangeListener( final IColorMapEntryViewer viewer )
  {
    m_listeners.add( viewer );
  }

  public void removeChangeListener( final IColorMapEntryViewer viewer )
  {
    m_listeners.remove( viewer );
  }

  public void colorMapEntryChanged( final ColorMapEntry entry )
  {
    final Iterator<IColorMapEntryViewer> iterator = m_listeners.iterator();
    while( iterator.hasNext() )
      iterator.next().updateColorMapEntry( entry );
  }

  public void addColorMapEntry( final ColorMapEntry entry )
  {
    m_entries.add( m_entries.size(), entry );

    final Iterator<IColorMapEntryViewer> iterator = m_listeners.iterator();
    while( iterator.hasNext() )
      iterator.next().addColorMapEntry( entry );
  }

  public void removeColorMapEntry( final ColorMapEntry entry )
  {
    m_entries.remove( entry );

    final Iterator<IColorMapEntryViewer> iterator = m_listeners.iterator();
    while( iterator.hasNext() )
      iterator.next().removeColorMapEntry( entry );
  }

  public List<ColorMapEntry> getColorMapEntries( )
  {
    return m_entries;
  }
}