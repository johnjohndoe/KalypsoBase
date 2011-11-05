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
package org.kalypso.model.wspm.ui.view.chart.layer.wsp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;
import org.kalypso.commons.pair.IKeyValue;
import org.kalypso.commons.pair.KeyValueFactory;

/**
 * FIXME: use for all profile tooltip, move to common place.
 * 
 * @author Gernot Belger
 */
public class TooltipFormatter
{
  private final Collection<IKeyValue<String, String>> m_lines = new LinkedList<IKeyValue<String, String>>();

  private final String m_header;

  public TooltipFormatter( final String header )
  {
    m_header = header;
  }

  public void addLine( final String key, final String value )
  {
    final IKeyValue<String, String> pair = KeyValueFactory.createPairEqualsBoth( key, value );
    m_lines.add( pair );
  }

  public String format( )
  {
    final StringWriter out = new StringWriter();
    final PrintWriter pw = new PrintWriter( out );

    if( m_header != null )
    {
      pw.println( m_header );
      pw.println();
    }

    final int maxKeyLength = findMaxKeyLength();
    final int maxValueLength = findMaxValueLength();

    for( final IKeyValue<String, String> pair : m_lines )
    {
      final String key = pair.getKey();
      final String value = pair.getValue();

      pw.append( StringUtils.rightPad( key, maxKeyLength + 1 ) );
      pw.append( StringUtils.leftPad( value, maxValueLength ) );

      pw.println();
    }

    pw.flush();
    return StringUtils.chomp( out.toString() );
  }

  private int findMaxKeyLength( )
  {
    int maxLength = 0;

    for( final IKeyValue<String, String> pair : m_lines )
    {
      final int length = pair.getKey().length();
      maxLength = Math.max( maxLength, length );
    }

    return maxLength;
  }

  private int findMaxValueLength( )
  {
    int maxLength = 0;

    for( final IKeyValue<String, String> pair : m_lines )
    {
      final int length = pair.getValue().length();
      maxLength = Math.max( maxLength, length );
    }

    return maxLength;
  }
}
