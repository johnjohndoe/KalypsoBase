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
package org.kalypso.model.wspm.core.profil;

import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;

/**
 * @author Kim Werner
 * @author Dirk Kuch
 */
public abstract class AbstractProfileObject implements IProfileObject
{
  private final IObservation<TupleResult> m_observation;

  protected AbstractProfileObject( final IObservation<TupleResult> observation )
  {
    m_observation = observation;
  }

  @Override
  public IObservation<TupleResult> getObservation( )
  {
    return m_observation;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfileObject#getObjectProperty(java.lang.String)
   */
  @Override
  public IComponent getObjectProperty( final String componentId )
  {
    final IComponent[] components = getObjectProperties();
    if( components.length < 1 )
      return null;
    for( final IComponent component : components )
    {
      if( component.getId().equals( componentId ) )
        return component;
    }
    return null;
  }

  protected abstract String[] getProfileProperties( );

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfileObject#getObjectProperties()
   */
  @Override
  public IComponent[] getObjectProperties( )
  {
    return getObservation().getResult().getComponents();
  }

  protected static IComponent getObjectComponent( final String id )
  {
    return ProfilUtil.getFeatureComponent( id );
  }

  /**
   * Retrieves the value of a given property as a double, if possible.
   * 
   * @return {@link Double#NaN} if the property is not a number or does not exist.
   */
  public double getDoubleValueFor( final String componentId )
  {
    final Object value = getValueFor( componentId );
    if( value instanceof Number )
      return ((Number) value).doubleValue();

    return Double.NaN;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfileObject#getValue(org.kalypso.observation.result.IComponent)
   */
  @Override
  public Object getValue( final IComponent component )
  {
    final TupleResult result = getObservation().getResult();
    final int index = result.indexOfComponent( component );

    if( index < 0 )
      throw new IllegalArgumentException( component == null ? getObservation().getDescription() : component.getDescription() );

    // quite dubious! we shouldn't enforce the size of the observation here, but in the constructor of the object!

    if( result.size() > 1 )
      throw new IllegalStateException( "Profile object always consists of one IRecord-Set row" ); //$NON-NLS-1$
    else if( result.size() == 0 )
      result.add( result.createRecord() );

    return result.get( 0 ).getValue( index );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfileObject#getValueFor(String)
   */
  @Override
  public Object getValueFor( final String componentID )
  {
    return getValue( getObjectProperty( componentID ) );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfileObject#setValue(org.kalypso.observation.result.IComponent,
   *      java.lang.Object)
   */
  @Override
  public void setValue( final IComponent component, final Object value )
  {
    final TupleResult result = getObservation().getResult();
    if( result.size() > 1 )
      throw new IllegalStateException( "Building always consists of one IRecord-Set row" ); //$NON-NLS-1$
    final int index = result.indexOfComponent( component );
    if( index < 0 )
      throw new IllegalArgumentException( component.getName() );

    final IRecord record;
    if( result.size() == 0 )
    {
      record = result.createRecord();
      result.add( record );
    }
    else
      record = result.get( 0 );
    record.setValue( index, value );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfileObject#setValueFor(String, java.lang.Object)
   */
  @Override
  public void setValueFor( final String componentID, final Object value )
  {
    setValue( getObjectProperty( componentID ), value );
  }

  public void cloneValuesFrom( final IProfileObject other )
  {
    for( final IComponent cmp : this.getObjectProperties() )
    {
      try
      {
        setValue( cmp, other.getValue( cmp ) );
      }
      catch( final IllegalArgumentException e )
      {
        continue;
      }
    }
  }

}
