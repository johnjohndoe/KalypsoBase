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
package org.kalypso.model.wspm.core.profil.wrappers;

import org.eclipse.core.runtime.Assert;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;

/**
 * @author Dirk Kuch
 */
public class AbstractRecordWrapper implements IRecord
{
  private final IRecord m_record;

  public AbstractRecordWrapper( final IRecord record )
  {
    Assert.isNotNull( record );

    m_record = record;
  }

  public IRecord getRecord( )
  {
    return m_record;
  }

  protected int findComponent( final String id )
  {
    final TupleResult owner = getRecord().getOwner();
    final IComponent[] components = owner.getComponents();

    for( int i = 0; i < components.length; i++ )
    {
      final IComponent comp = components[i];

      if( id.equals( comp.getId() ) )
        return i;
    }

    return -1;
  }

  /**
   * @see org.kalypso.observation.result.IRecord#getOwner()
   */
  @Override
  public TupleResult getOwner( )
  {
    return m_record.getOwner();
  }

  /**
   * @see org.kalypso.observation.result.IRecord#getValue(org.kalypso.observation.result.IComponent)
   */
  @Override
  public Object getValue( final IComponent comp ) throws IllegalArgumentException
  {
    return m_record.getValue( comp );
  }

  /**
   * @see org.kalypso.observation.result.IRecord#getValue(int)
   */
  @Override
  public Object getValue( final int index ) throws IndexOutOfBoundsException
  {
    return m_record.getValue( index );
  }

  /**
   * @see org.kalypso.observation.result.IRecord#setValue(org.kalypso.observation.result.IComponent, java.lang.Object)
   */
  @Override
  public void setValue( final IComponent comp, final Object value )
  {
    m_record.setValue( comp, value );
  }

  /**
   * @see org.kalypso.observation.result.IRecord#setValue(int, java.lang.Object)
   */
  @Override
  public void setValue( final int index, final Object value ) throws IndexOutOfBoundsException
  {
    m_record.setValue( index, value );
  }

  /**
   * @see org.kalypso.observation.result.IRecord#setValue(int, java.lang.Object, boolean)
   */
  @Override
  public void setValue( final int index, final Object value, final boolean fireNoEvent ) throws IndexOutOfBoundsException
  {
    m_record.setValue( index, value, fireNoEvent );
  }

  /**
   * @see org.kalypso.observation.result.IRecord#cloneRecord()
   */
  @Override
  public IRecord cloneRecord( )
  {
    return m_record.cloneRecord();
  }
}
