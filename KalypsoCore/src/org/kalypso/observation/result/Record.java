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
package org.kalypso.observation.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.i18n.Messages;
import org.kalypso.observation.result.ITupleResultChangedListener.TYPE;
import org.kalypso.observation.result.ITupleResultChangedListener.ValueChange;

/**
 * Default visibility: do NOT use outside of TupleResult.
 * 
 * @author schlienger Default visibility, use IRecord and TupleResult.createRecord.
 */
/* default */class Record implements IRecord
{
  private final List<Object> m_values = new ArrayList<Object>();

  private List<IComponent> m_components = new ArrayList<IComponent>();

  private TupleResult m_owner;

  Record( final TupleResult result, final IComponent[] components )
  {
    m_owner = result;
    m_components = Arrays.asList( components );

    for( final IComponent component : components )
      m_values.add( component.getDefaultValue() );
  }

  @Override
  public String toString( )
  {
    return ArrayUtils.toString( m_values );
  }

  @Override
  @Deprecated
  public Object getValue( final IComponent comp )
  {
    final int index = checkComponent( comp );
    return getValue( index );
  }

  @Override
  public Object getValue( final int index ) throws IndexOutOfBoundsException
  {
    return m_values.get( index );
  }

  private int checkComponent( final IComponent comp )
  {
    final int index = m_components.indexOf( comp );
    if( index == -1 )
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.observation.result.Record.0" ) + comp ); //$NON-NLS-1$

    return index;
  }

  @Override
  @Deprecated
  public void setValue( final IComponent comp, final Object value )
  {
    final int index = checkComponent( comp );
    setValue( index, value );
  }

  @Override
  public void setValue( final int index, final Object value ) throws IndexOutOfBoundsException
  {
    setValue( index, value, false );
  }

  @Override
  public void setValue( final int index, final Object value, final boolean fireNoEvent ) throws IndexOutOfBoundsException
  {
    final Object oldValue = m_values.get( index );
    if( ObjectUtils.equals( value, oldValue ) )
      return;

    m_values.set( index, value );

    if( !fireNoEvent && m_owner != null )
    {
      if( m_owner.invalidateSort( index ) )
        m_owner.fireRecordsChanged( null, TYPE.CHANGED );
      else
      {
        final ValueChange[] changes = new ValueChange[] { new ValueChange( this, index, oldValue, value ) };
        if( !fireNoEvent )
          m_owner.fireValuesChanged( changes );
      }
    }

  }

  /* default */void remove( final int index )
  {
    m_values.remove( index );
  }

  @Override
  public TupleResult getOwner( )
  {
    return m_owner;
  }

  @Override
  public IRecord cloneRecord( )
  {
    final TupleResult result = getOwner();
    final IComponent[] components = m_components.toArray( new IComponent[m_components.size()] );

    final Record record = new Record( result, components );
    for( int i = 0; i < components.length; i++ )
      record.setValue( i, getValue( i ), true );

    return record;
  }

  /**
   * sets a value of an given index - index doesn't exists (new value end of list) -> index will be created
   */
  /* default */void set( final int index, final Object value )
  {
    if( m_values.size() == index )
    {
      m_values.add( value );
    }
    else
    {
      // Might throw IndexOutOfBoundsException..
      m_values.set( index, value );
    }
  }

  void setOwner( final TupleResult owner, final List<IComponent> components )
  {
    m_owner = owner;
    m_components = components;
  }

  @Override
  public int indexOfComponent( final String componentID )
  {
    for( int i = 0; i < m_components.size(); i++ )
    {
      final IComponent comp = m_components.get( i );
      if( comp.getId().equals( componentID ) )
        return i;
    }

    return -1;
  }

  @Override
  public int getIndex( )
  {
    final TupleResult owner = getOwner();
    if( Objects.isNull( owner ) )
      return -1;

    return owner.indexOf( this );
  }
}
