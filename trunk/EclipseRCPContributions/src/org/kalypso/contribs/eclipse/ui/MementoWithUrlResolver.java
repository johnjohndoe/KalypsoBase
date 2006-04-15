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
package org.kalypso.contribs.eclipse.ui;

import org.eclipse.ui.IMemento;
import org.kalypso.contribs.java.net.IUrlResolver2;

/**
 * @author FlowsAd
 */
public class MementoWithUrlResolver implements IMemento
{
  private final IMemento m_memento;

  private final IUrlResolver2 m_resolver;

  public MementoWithUrlResolver( IMemento memento, IUrlResolver2 resolver )
  {
    m_memento = memento;
    m_resolver = resolver;
  }

  public MementoWithUrlResolver( IMemento memento, MementoWithUrlResolver mementoWithUrlResolver )
  {
    m_memento = memento;
    m_resolver = mementoWithUrlResolver.getURLResolver();
  }

  public IUrlResolver2 getURLResolver( )
  {
    return m_resolver;
  }

  /**
   * @see org.eclipse.ui.IMemento#createChild(java.lang.String, java.lang.String)
   */
  public IMemento createChild( String type, String id )
  {
    final IMemento child = m_memento.createChild( type, id );
    if(child==null)
      return null;
    return new MementoWithUrlResolver( child, this );
  }

  /**
   * @see org.eclipse.ui.IMemento#createChild(java.lang.String)
   */
  public IMemento createChild( String type )
  {
    final IMemento child = m_memento.createChild( type);
    if(child==null)
      return null;
    return new MementoWithUrlResolver( child, this );
  }

  /**
   * @see org.eclipse.ui.IMemento#getChild(java.lang.String)
   */
  public IMemento getChild( String type )
  {
    final IMemento child = m_memento.getChild( type);
    if(child==null)
      return null;
    return new MementoWithUrlResolver( child, this );
  }

  /**
   * @see org.eclipse.ui.IMemento#getChildren(java.lang.String)
   */
  public IMemento[] getChildren( String type )
  {
    final IMemento[] children = m_memento.getChildren( type);
    if(children==null)
      return null;

    final IMemento[] result = new IMemento[children.length];
    for( int i = 0; i < result.length; i++ )
      result[i] = new MementoWithUrlResolver( children[i], this );
    return result;
  }

  /**
   * @see org.eclipse.ui.IMemento#getFloat(java.lang.String)
   */
  public Float getFloat( String key )
  {
    return m_memento.getFloat( key );
  }

  /**
   * @see org.eclipse.ui.IMemento#getID()
   */
  public String getID( )
  {
    return m_memento.getID();
  }

  /**
   * @see org.eclipse.ui.IMemento#getInteger(java.lang.String)
   */
  public Integer getInteger( String key )
  {
    return m_memento.getInteger( key );
  }

  /**
   * @see org.eclipse.ui.IMemento#getString(java.lang.String)
   */
  public String getString( String key )
  {
    return m_memento.getString( key );
  }

  /**
   * @see org.eclipse.ui.IMemento#getTextData()
   */
  public String getTextData( )
  {
    return m_memento.getTextData();
  }

  /**
   * @see org.eclipse.ui.IMemento#putFloat(java.lang.String, float)
   */
  public void putFloat( String key, float value )
  {
    m_memento.putFloat( key, value );
  }

  /**
   * @see org.eclipse.ui.IMemento#putInteger(java.lang.String, int)
   */
  public void putInteger( String key, int value )
  {
    m_memento.putInteger( key, value );
  }

  /**
   * @see org.eclipse.ui.IMemento#putMemento(org.eclipse.ui.IMemento)
   */
  public void putMemento( IMemento memento )
  {
    m_memento.putMemento( memento );
  }

  /**
   * @see org.eclipse.ui.IMemento#putString(java.lang.String, java.lang.String)
   */
  public void putString( String key, String value )
  {
    m_memento.putString( key, value );
  }

  /**
   * @see org.eclipse.ui.IMemento#putTextData(java.lang.String)
   */
  public void putTextData( String data )
  {
    m_memento.putTextData( data );
  }
}
