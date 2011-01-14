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
package de.openali.odysseus.chart.ext.base.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimwerner
 */
public class ArrayContentProvider implements IAxisContentProvider
{

  private final List<Object> m_content = new ArrayList<Object>();

  private final List<String> m_labels = new ArrayList<String>();

  public ArrayContentProvider( )
  {
// default
  }

  public ArrayContentProvider( final Object... contents )
  {
    for( final Object content : contents )
      m_content.add( content );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.data.IAxisContentProvider#add(java.lang.Object)
   */
  @Override
  public void addContent( final Object content, final String label )
  {
    m_content.add( content );
    m_labels.add( label );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.data.IAxisContentProvider#clear()
   */
  @Override
  public void clear( )
  {
    m_content.clear();
    m_labels.clear();
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.data.IAxisContentProvider#get(int)
   */
  @Override
  public Object getContent( final int index )
  {
    if( index < 0 || index > m_content.size() - 1 )
      return null;
    return m_content.get( index );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.data.IAxisContentProvider#remove(java.lang.Object)
   */
  @Override
  public void removeContent( final Object content )
  {
    final int index = m_content.indexOf( content );
    if( index < 0 )
      return;
    m_content.remove( content );
    m_labels.remove( m_labels.get( index ) );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.data.IAxisContentProvider#size()
   */
  @Override
  public int size( )
  {
    return m_content.size();
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.data.IAxisContentProvider#getLabel(int)
   */
  @Override
  public String getLabel( final int index )
  {
    return m_labels.get( index );
  }

}
