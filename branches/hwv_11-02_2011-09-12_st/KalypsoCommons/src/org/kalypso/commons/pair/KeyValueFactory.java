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
package org.kalypso.commons.pair;

import org.kalypso.commons.internal.pair.DefaultKeyValue;
import org.kalypso.commons.internal.pair.KeyValueEqualsKey;
import org.kalypso.commons.internal.pair.KeyValueEqualsValue;

/**
 * Creates different instances of {@link IKeyValue} implementations.
 * 
 * @author Gernot Belger
 */
public final class KeyValueFactory
{
  private KeyValueFactory( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a {@link IKeyValue} pair, that equals another pair if both key AND value are equal.<br/>
   * Works only, if pair created by this same method are compared.
   */
  public static <K, V> IKeyValue<K, V> createPairEqualsBoth( final K key, final V value )
  {
    return new DefaultKeyValue<K, V>( key, value );
  }

  /**
   * Creates a {@link IKeyValue} pair, that equals another pair if their keys are equal.<br/>
   * Works only, if pair created by this same method are compared.
   */
  public static <K, V> IKeyValue<K, V> createPairEqualsKey( final K key, final V value )
  {
    return new KeyValueEqualsKey<K, V>( key, value );
  }

  /**
   * Creates a {@link IKeyValue} pair, that equals another pair if their values are equal.<br/>
   * Works only, if pair created by this same method are compared.
   */
  public static <K, V> IKeyValue<K, V> createPairEqualsValue( final K key, final V value )
  {
    return new KeyValueEqualsValue<K, V>( key, value );
  }
}