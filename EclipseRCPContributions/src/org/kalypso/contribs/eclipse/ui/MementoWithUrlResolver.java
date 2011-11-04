/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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

import java.io.IOException;
import java.io.Writer;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.kalypso.contribs.java.net.IUrlResolver2;

/**
 * @author Christoph Kuepferle
 */
public class MementoWithUrlResolver implements IMemento
{
  private final IMemento m_memento;

  private final Properties m_props;

  public final static String PATH_KEY = "PATH";

  public final static String PROJECT_KEY = "PROJ";

  private final IUrlResolver2 m_resolver;

  public MementoWithUrlResolver( final IMemento original, final MementoWithUrlResolver memento )
  {
    m_memento = original;
    m_props = memento.getPropetries();
    m_resolver = memento.getURLResolver();
  }

  public IUrlResolver2 getURLResolver( )
  {
    return m_resolver;
  }

  private Properties getPropetries( )
  {
    return m_props;
  }

  public MementoWithUrlResolver( final IMemento memento, final Properties propsTokenReplaecment, final IUrlResolver2 resolver )
  {
    m_memento = memento;
    if( propsTokenReplaecment == null )
      m_props = new Properties();
    else
      m_props = propsTokenReplaecment;
    m_resolver = resolver;
  }

  /**
   * @see org.eclipse.ui.IMemento#createChild(java.lang.String, java.lang.String)
   */
  @Override
  public IMemento createChild( final String type, final String id )
  {
    final IMemento child = m_memento.createChild( type, id );
    if( child == null )
      return null;
    return new MementoWithUrlResolver( child, this );
  }

  /**
   * @see org.eclipse.ui.IMemento#createChild(java.lang.String)
   */
  @Override
  public IMemento createChild( final String type )
  {
    final IMemento child = m_memento.createChild( type );
    if( child == null )
      return null;
    return new MementoWithUrlResolver( child, this );
  }

  /**
   * @see org.eclipse.ui.IMemento#getChild(java.lang.String)
   */
  @Override
  public IMemento getChild( final String type )
  {
    final IMemento child = m_memento.getChild( type );
    if( child == null )
      return null;
    return new MementoWithUrlResolver( child, this );
  }

  /**
   * @see org.eclipse.ui.IMemento#getChildren(java.lang.String)
   */
  @Override
  public IMemento[] getChildren( final String type )
  {
    final IMemento[] children = m_memento.getChildren( type );
    if( children == null )
      return null;

    final IMemento[] result = new IMemento[children.length];
    for( int i = 0; i < result.length; i++ )
      result[i] = new MementoWithUrlResolver( children[i], this );
    return result;
  }

  /**
   * @see org.eclipse.ui.IMemento#getFloat(java.lang.String)
   */
  @Override
  public Float getFloat( final String key )
  {
    return m_memento.getFloat( key );
  }

  /**
   * @see org.eclipse.ui.IMemento#getID()
   */
  @Override
  public String getID( )
  {
    return m_memento.getID();
  }

  /**
   * @see org.eclipse.ui.IMemento#getInteger(java.lang.String)
   */
  @Override
  public Integer getInteger( final String key )
  {
    return m_memento.getInteger( key );
  }

  /**
   * @see org.eclipse.ui.IMemento#getString(java.lang.String)
   */
  @Override
  public String getString( final String key )
  {
    return replace( m_memento.getString( key ) );
  }

  /**
   * @see org.eclipse.ui.IMemento#getTextData()
   */
  @Override
  public String getTextData( )
  {
    return replace( m_memento.getTextData() );
  }

  /**
   * @see org.eclipse.ui.IMemento#putFloat(java.lang.String, float)
   */
  @Override
  public void putFloat( final String key, final float value )
  {
    m_memento.putFloat( key, value );
  }

  /**
   * @see org.eclipse.ui.IMemento#putInteger(java.lang.String, int)
   */
  @Override
  public void putInteger( final String key, final int value )
  {
    m_memento.putInteger( key, value );
  }

  /**
   * @see org.eclipse.ui.IMemento#putMemento(org.eclipse.ui.IMemento)
   */
  @Override
  public void putMemento( final IMemento memento )
  {
    m_memento.putMemento( memento );
  }

  /**
   * @see org.eclipse.ui.IMemento#putString(java.lang.String, java.lang.String)
   */
  @Override
  public void putString( final String key, final String value )
  {
    m_memento.putString( key, replace( value ) );
  }

  /**
   * @see org.eclipse.ui.IMemento#putTextData(java.lang.String)
   */
  @Override
  public void putTextData( final String data )
  {
    m_memento.putTextData( replace( data ) );
  }

  private String replace( String toBeReplaced )
  {

    if( toBeReplaced == null )
      return null;
    // replace absolute file path with the project protocol
    for( final Entry<Object, Object> entry : m_props.entrySet() )
    {
      final String key = entry.getKey().toString();
      final String value = entry.getValue().toString();

      toBeReplaced = toBeReplaced.replaceAll( key, value );
    }

    return toBeReplaced;

  }

  public void save( final Writer writer ) throws IOException, Exception
  {
    if( m_memento instanceof XMLMemento )
    {
      ((XMLMemento) m_memento).save( writer );
    }
    else
      throw new Exception( "Das Memento muss eine XMLMemento Objekt sein!" );

  }

  /**
   * @return
   * @see org.eclipse.ui.IMemento#getAttributeKeys()
   */
  @Override
  public String[] getAttributeKeys( )
  {
    return m_memento.getAttributeKeys();
  }

  /**
   * @param key
   * @return
   * @see org.eclipse.ui.IMemento#getBoolean(java.lang.String)
   */
  @Override
  public Boolean getBoolean( final String key )
  {
    return m_memento.getBoolean( key );
  }

  /**
   * @return
   * @see org.eclipse.ui.IMemento#getType()
   */
  @Override
  public String getType( )
  {
    return m_memento.getType();
  }

  /**
   * @param key
   * @param value
   * @see org.eclipse.ui.IMemento#putBoolean(java.lang.String, boolean)
   */
  @Override
  public void putBoolean( final String key, final boolean value )
  {
    m_memento.putBoolean( key, value );
  }

}
