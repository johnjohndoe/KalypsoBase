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
package org.kalypso.observation.result;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.XmlTypes;

/**
 * @author Dirk Kuch
 */
public final class Records
{
  private Records( )
  {
  }

  public static IComponent findComponent( final IRecord record, final String property )
  {
    return TupleResultUtilities.findComponentById( record.getOwner().getComponents(), property );
  }

  public static void set( final IRecord record, final IComponent component, final Number value )
  {
    final QName qname = component.getValueTypeName();
    if( XmlTypes.XS_DECIMAL.equals( qname ) )
      record.setValue( component, BigDecimal.valueOf( value.doubleValue() ) );
    else if( XmlTypes.XS_DOUBLE.equals( qname ) )
      record.setValue( component, Double.valueOf( value.doubleValue() ) );
    else if( XmlTypes.XS_FLOAT.equals( qname ) )
      record.setValue( component, Float.valueOf( value.floatValue() ) );
    else if( XmlTypes.XS_INT.equals( qname ) )
      record.setValue( component, Integer.valueOf( value.intValue() ) );
    else if( XmlTypes.XS_INTEGER.equals( qname ) )
      record.setValue( component, BigInteger.valueOf( value.longValue() ) );
    else if( XmlTypes.XS_LONG.equals( qname ) )
      record.setValue( component, Long.valueOf( value.longValue() ) );
    else if( XmlTypes.XS_SHORT.equals( qname ) )
      record.setValue( component, Short.valueOf( value.shortValue() ) );
    else
      throw new UnsupportedOperationException();
  }

}