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
package org.kalypso.zml.ui.table.nat.editing;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.editing.IZmlEditingStrategy;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Dirk Kuch
 */
class ZmlPasteDataFromString implements IZmlPasteData
{
  private final List<String[]> m_rows;

  private final ZmlModelViewport m_viewport;

  public ZmlPasteDataFromString( final ZmlModelViewport viewport, final String clipboardContents ) throws IOException
  {
    m_viewport = viewport;
    m_rows = readCSV( clipboardContents );
  }

  private List<String[]> readCSV( final String clipboardContents ) throws IOException
  {
    final CSVReader reader = new CSVReader( new StringReader( clipboardContents ), '\t' );
    try
    {
      return reader.readAll();
    }
    finally
    {
      IOUtils.closeQuietly( reader );
    }
  }

  @Override
  public int findDataIndex( final IZmlModelColumn column, final int startInputColumn )
  {
    final String type = column.getDataColumn().getValueAxis();

    final int index = getIndex( m_rows.get( 0 ), type );
    if( index == -1 )
    {
      m_rows.remove( 0 );// header row
      if( !m_rows.isEmpty() )
        return getIndex( m_rows.get( 0 ), type );
    }

    return index;
  }

  private int getIndex( final String[] row, final String type )
  {
    for( int index = 0; index < ArrayUtils.getLength( row ); index++ )
    {
      try
      {
        final String cell = row[index];

        // FIXME: arg! both should be delegated to the strategy!
        if( StringUtils.equalsIgnoreCase( ITimeseriesConstants.TYPE_POLDER_CONTROL, type ) )
        {
          if( StringUtils.equalsIgnoreCase( "true", cell ) ) //$NON-NLS-1$
            return index;
          else if( StringUtils.equalsIgnoreCase( "false", cell ) ) //$NON-NLS-1$
            return index;
        }
        else
        {
          final double value = NumberUtils.parseDouble( cell );
          if( Double.isNaN( value ) )
            continue;

          return index;
        }
      }
      catch( final Throwable t )
      {
        // nothing to do
      }
    }

    return -1;
  }

  @Override
  public int getRowCount( )
  {
    return m_rows.size();
  }

  @Override
  public Object getData( final IZmlModelValueCell cell, final int columnIndex, final int rowIndex )
  {
    final IZmlModelColumn column = cell.getColumn();

    final IZmlEditingStrategy strategy = m_viewport.getEditingStrategy( column );

    final String[] row = m_rows.get( rowIndex );
    final String text = row[columnIndex];

    return strategy.parseValue( cell, text );
  }
}