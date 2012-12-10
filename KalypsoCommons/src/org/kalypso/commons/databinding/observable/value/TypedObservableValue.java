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
package org.kalypso.commons.databinding.observable.value;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.runtime.Assert;

/**
 * @author Gernot Albert
 * @author Holger Albert
 */
public abstract class TypedObservableValue<SOURCE, VALUE> extends AbstractObservableValue implements ITypedObservableValue<SOURCE, VALUE>
{
  private final SOURCE m_source;

  private final Class<VALUE> m_valueType;

  public TypedObservableValue( final SOURCE source, final Class<VALUE> valueType )
  {
    Assert.isNotNull( source );
    Assert.isNotNull( valueType );

    m_source = source;
    m_valueType = valueType;
  }

  protected SOURCE getSource( )
  {
    return m_source;
  }

  /**
   * @see org.eclipse.core.databinding.observable.value.IObservableValue#getValueType()
   */
  @Override
  public Class<VALUE> getValueType( )
  {
    return m_valueType;
  }

  /**
   * @see org.eclipse.core.databinding.observable.value.AbstractObservableValue#doGetValue()
   */
  @Override
  protected final VALUE doGetValue( )
  {
    return doGetValueTyped( m_source );
  }

  /**
   * @see org.eclipse.core.databinding.observable.value.AbstractObservableValue#doSetValue(java.lang.Object)
   */
  @Override
  protected final void doSetValue( final Object value )
  {
    final VALUE typedValue = m_valueType.cast( value );
    final VALUE currentValue = doGetValue();
    if( valueEquals( typedValue, currentValue ) )
      return;

    doSetValueTyped( m_source, typedValue );

    final ValueDiff diff = Diffs.createValueDiff( currentValue, value );
    fireValueChange( diff );
  }

  protected boolean valueEquals( final VALUE value1, final VALUE value2 )
  {
    return ObjectUtils.equals( value1, value2 );
  }
}