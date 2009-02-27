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
package org.kalypso.chart.ext.test.axis;

import java.text.Format;
import java.util.Comparator;

import org.kalypso.chart.framework.exception.MalformedValueException;
import org.kalypso.chart.framework.model.data.IDataOperator;
import org.kalypso.chart.framework.model.data.IDataRange;

/**
 * @author alibu
 * 
 */
public class StringOperator implements IDataOperator<String>, Comparator<String>
{

  public StringOperator( )
  {
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IDataOperator#getComparator()
   */
  public Comparator<String> getComparator( )
  {
    return this;
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IDataOperator#getContainingInterval(java.lang.Object, java.lang.Number,
   *      java.lang.Object)
   */
  public IDataRange<String> getContainingInterval( String logVal, Number numIntervalWidth, String logFixedPoint )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IDataOperator#getDefaultRange()
   */
  public IDataRange<String> getDefaultRange( )
  {
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IDataOperator#getFormat(org.kalypso.chart.framework.model.data.IDataRange)
   */
  public Format getFormat( IDataRange<String> range )
  {
    return new StringFormat();
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IDataOperator#logicalToNumerical(java.lang.Object)
   */
  public Number logicalToNumerical( String logVal )
  {
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IDataOperator#numericalToLogical(java.lang.Number)
   */
  public String numericalToLogical( Number numVal )
  {
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IStringDataConverter#logicalToString(java.lang.Object)
   */
  public String logicalToString( String value )
  {
    return value;
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    return "a simple string";
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  public String stringToLogical( String value ) throws MalformedValueException
  {
    return value;
  }

  /**
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare( String o1, String o2 )
  {
    return o1.compareTo( o2 );
  }
}
