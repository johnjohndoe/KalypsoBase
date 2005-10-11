/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/

package org.kalypso.simulation.ui.wizards.exporter.sumuptable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kalypso.commons.arguments.Arguments;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.template.NameUtils;

public final class ColumnSpec implements Comparable
{
  private final String m_label;
  private final String m_content;
  private final int m_position;

  public ColumnSpec( final Arguments args )
  {
    this( Integer.valueOf( args.getProperty( "position" ) ).intValue(), args.getProperty( "label" ), args
        .getProperty( "content" ) );
  }

  public ColumnSpec( final int position, final String label, final String content )
  {
    m_position = position;
    m_label = label;
    m_content = content;
  }

  public String getLabel()
  {
    return m_label;
  }

  public String getContent()
  {
    return m_content;
  }

  /**
   * Helper method that handles the resolution of the token-oriented content of the column specification.
   * <p>
   * If the content begins with:
   * <ul>
   * <li>'arg:', this method will try to translate what follows as an argument-name (within the arguments of the exporter) and will return its value found in the given arguments
   * <li>'metadata:', this method will try to translate what follows as a metadata-name and will return the value found in the metadata of the given observation
   * </ul>
   * in all other cases the content is simply returned.
   * 
   * @param args used in the case the content of the column specification begins with 'arg:'
   * @param obs used in the case the content of the column specification begins with 'metadata:' or when token-replacement should take place (ex. %obsname% is given)
   */
  public String resolveContent( final Arguments args, final IObservation obs )
  {
    String content = m_content;
    
    if( content.startsWith( "arg:" ) && args != null )
    {
      final String key = content.replaceAll( "arg:", "" );
      content = args.getProperty( key, "" );
    }

    if( content.startsWith( "metadata:" ) && obs != null )
    {
      final String key = content.replaceAll( "metadata:", "" );
      content = obs.getMetadataList().getProperty( key, "" );
    }

    return NameUtils.replaceTokens( content, obs, null );
  }

  public String toString()
  {
    return getLabel();
  }

  public static ColumnSpec[] getColumns( final Arguments args )
  {
    final List cols = new ArrayList();

    for( final Iterator it = args.entrySet().iterator(); it.hasNext(); )
    {
      final Map.Entry entry = (Map.Entry)it.next();
      final String argName = (String)entry.getKey();

      if( argName.startsWith( "column" ) )
        cols.add( new ColumnSpec( (Arguments)entry.getValue() ) );
    }

    final ColumnSpec[] columns = (ColumnSpec[])cols.toArray( new ColumnSpec[cols.size()] );
    Arrays.sort( columns );

    return columns;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo( final Object o )
  {
    final ColumnSpec other = (ColumnSpec)o;

    return m_position - other.m_position;
  }
}