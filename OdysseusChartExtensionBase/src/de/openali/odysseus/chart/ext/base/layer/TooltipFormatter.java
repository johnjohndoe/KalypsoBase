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
package de.openali.odysseus.chart.ext.base.layer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;

/**
 * Helper class for formatting chart tooltips.<br/>
 * Shows an (optional) header line and two columns, one for the label, one for the value.
 *
 * @author Gernot Belger
 */
public class TooltipFormatter
{
  private int[] m_columnWidths = null;

  private final List<String[]> m_lines = new LinkedList<>();

  private final String m_header;

  private String[] m_formats;

  private int[] m_alignments;

  private Collection<String> m_footer = new ArrayList<>();
  
  public TooltipFormatter( final String header )
  {
    this( header, null, null );
  }

  public TooltipFormatter( final String header, final String[] formats, final int[] alignments )
  {
    m_header = header;
    m_formats = formats;
    m_alignments = alignments;
  }

  public void addFooter(String line)
  {
    m_footer.add( line );
  }
  
  public void addLine( final Object... values )
  {
    /* check length of columns and create default formats if necessary */
    if( m_formats != null )
    {
      if( m_formats.length != values.length )
        throw new IllegalArgumentException( "all lines of the tooltip must have the same number of columns" ); //$NON-NLS-1$
    }
    else
    {
      m_formats = new String[values.length];
      for( int i = 0; i < m_formats.length; i++ )
      {
        m_formats[i] = "%s"; //$NON-NLS-1$
      }
    }

    /* create default alignments */
    if( m_alignments == null || m_alignments.length != m_formats.length )
    {
      m_alignments = new int[m_formats.length];
      if( m_alignments.length > 0 )
        m_alignments[0] = SWT.LEFT;
      for( int i = 1; i < m_alignments.length; i++ )
        m_alignments[i] = SWT.RIGHT;
    }

    if( m_columnWidths == null )
      m_columnWidths = new int[m_formats.length];

    /* directly format as string and update widths */
    final String[] texts = new String[values.length];
    for( int i = 0; i < texts.length; i++ )
    {
      final String formattedValue = String.format( m_formats[i], values[i] );
      texts[i] = formattedValue;
    }

    m_lines.add( texts );

    /* update max widths */
    for( int i = 0; i < texts.length; i++ )
      m_columnWidths[i] = Math.max( m_columnWidths[i], texts[i].length() );
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

    for( final String[] texts : m_lines )
    {
      for( int i = 0; i < texts.length; i++ )
      {
        final String alignedText = alignText( texts, i );
        pw.append( alignedText );

        /* one space between columns */
        if( i != texts.length - 1 )
          pw.append( ' ' );
      }

      pw.println();
    }

    for( String footer : m_footer )
      pw.println(footer);
    
    pw.flush();
    return StringUtils.chomp( out.toString() );
  }

  private String alignText( final String[] texts, final int i )
  {
    final String value = texts[i];
    final int alignment = m_alignments[i];

    if( alignment == SWT.LEFT )
      return StringUtils.rightPad( value, m_columnWidths[i] );
    else
      return StringUtils.leftPad( value, m_columnWidths[i] );
  }
}
