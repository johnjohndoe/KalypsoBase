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
package org.kalypso.chart.ext.base.data;

import java.text.Format;
import java.util.Comparator;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.IOrdinalDataOperator;

/**
 * @author alibu
 * 
 */
public class StringDataOperator implements IOrdinalDataOperator<String>
{

  private final String[] m_values;

  private final Comparator<String> m_comparator = new Comparator<String>()
  {

    public int compare( String o1, String o2 )
    {
      int pos1 = getPosition( o1 );
      int pos2 = getPosition( o2 );

      if( pos1 == -1 )
      {
        throw new IllegalArgumentException( "Cannot compare '" + o1 + "' to '" + o2 + "': '" + o1 + "' is not part of the set" );
      }
      if( pos2 == -1 )
      {
        throw new IllegalArgumentException( "Cannot compare '" + o1 + "' to '" + o2 + "': '" + o2 + "' is not part of the set" );
      }

      if( pos1 < pos2 )
      {
        return -1;
      }
      else if( pos1 > pos2 )
      {
        return 1;
      }
      else
      {
        return 0;
      }
    }

  };

  private final Format m_format = new StringFormat();

  public StringDataOperator( String[] values )
  {
    m_values = values;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IOrdinalDataOperator#getValues()
   */
  public String[] getValues( )
  {
    return m_values;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IDataOperator#getComparator()
   */
  public Comparator<String> getComparator( )
  {
    return m_comparator;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IDataOperator#getContainingInterval(java.lang.Object,
   *      java.lang.Number, java.lang.Object)
   */
  public IDataRange<String> getContainingInterval( String logVal, Number numIntervalWidth, String logFixedPoint )
  {
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IDataOperator#getFormat(de.openali.odysseus.chart.framework.model.data.IDataRange)
   */
  public Format getFormat( IDataRange<Number> range )
  {
    return m_format;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IDataOperator#logicalToNumeric(java.lang.Object)
   */
  public Number logicalToNumeric( String logVal )
  {
    return getPosition( logVal );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IDataOperator#numericToLogical(java.lang.Number)
   */
  public String numericToLogical( Number numVal )
  {
    return m_values[numVal.intValue()];
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IStringDataConverter#logicalToString(java.lang.Object)
   */
  public String logicalToString( String value )
  {
    return value;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    return "see available values";
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  public String stringToLogical( String value )
  {
    return value;
  }

  int getPosition( String s )
  {
    for( int i = 0; i < m_values.length; i++ )
    {
      if( s.equals( m_values[i] ) )
      {
        return i;
      }
    }
    return -1;
  }

}
