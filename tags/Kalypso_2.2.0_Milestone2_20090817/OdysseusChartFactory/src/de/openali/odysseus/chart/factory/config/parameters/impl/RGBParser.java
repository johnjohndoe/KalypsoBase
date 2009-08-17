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
package de.openali.odysseus.chart.factory.config.parameters.impl;

import java.math.BigInteger;

import junit.framework.Assert;

import org.eclipse.swt.graphics.RGB;
import org.junit.Test;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.data.IStringParser;

/**
 * @author alibu
 */
public class RGBParser implements IStringParser<RGB>
{
  /**
   * @see de.openali.odysseus.chart.framework.model.data.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    return "String containing 6 hex values";
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  public RGB stringToLogical( final String value ) throws MalformedValueException
  {
    if( value.length() != 6 )
    {
      throw new MalformedValueException( "value must have length 6" );
    }
    final String lValue = value.toLowerCase();

    if( !lValue.matches( "[0-9a-f]{6}" ) )
    {
      throw new MalformedValueException( "each digit must be 0 - F" );
    }

    final int red = new BigInteger( lValue.substring( 0, 2 ), 16 ).intValue();
    final int green = new BigInteger( lValue.substring( 2, 4 ), 16 ).intValue();
    final int blue = new BigInteger( lValue.substring( 4 ), 16 ).intValue();

    return new RGB( red, green, blue );
  }

  @Test
  public void test( )
  {
    final RGBParser p = new RGBParser();
    try
    {
      p.stringToLogical( "aabbc" );
      Assert.fail();
    }
    catch( final MalformedValueException e )
    {
    }
    try
    {
      p.stringToLogical( "AaBbCG" );
      Assert.fail();
    }
    catch( final MalformedValueException e )
    {
    }
    try
    {
      final RGB rgb = p.stringToLogical( "00ccff" );
      System.out.println( rgb.toString() );

    }
    catch( final MalformedValueException e )
    {
      Assert.fail();
    }
    catch( final NumberFormatException e )
    {
      e.printStackTrace();
      Assert.fail();
    }
  }
}
