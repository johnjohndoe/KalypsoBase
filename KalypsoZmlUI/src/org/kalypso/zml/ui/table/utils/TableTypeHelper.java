/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.ui.table.utils;

import java.util.List;

import jregex.Pattern;
import jregex.RETokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.zml.ui.table.schema.AbstractColumnType;
import org.kalypso.zml.ui.table.schema.AlignmentType;
import org.kalypso.zml.ui.table.schema.DataColumnType;
import org.kalypso.zml.ui.table.schema.IndexColumnType;
import org.kalypso.zml.ui.table.schema.ZmlTableType;

/**
 * @author Dirk Kuch
 */
public final class TableTypeHelper
{
  public static final Pattern PATTERN_CLONED_COLUMN_IDENTIFIER = new Pattern( ".*\\_\\d+$" );

  private TableTypeHelper( )
  {
  }

  public static int toSWT( final AlignmentType alignment )
  {
    if( alignment == null )
      return SWT.LEFT;

    if( AlignmentType.LEFT.toString().equals( alignment.toString() ) )
      return SWT.LEFT;
    else if( AlignmentType.CENTER.toString().equals( alignment.toString() ) )
      return SWT.CENTER;
    else if( AlignmentType.RIGHT.toString().equals( alignment.toString() ) )
      return SWT.RIGHT;

    return SWT.LEFT;
  }

  public static AbstractColumnType finColumn( final ZmlTableType tableType, final String identifier )
  {
    final List<AbstractColumnType> columns = tableType.getColumns().getColumn();
    for( final AbstractColumnType column : columns )
    {
      if( column.getId().equals( identifier ) )
        return column;
    }

    return null;
  }

  public static AbstractColumnType cloneColumn( final AbstractColumnType base )
  {
    if( base instanceof DataColumnType )
    {
      final DataColumnType data = (DataColumnType) base;
      final DataColumnType clone = new DataColumnType();

      copyBasicSettings( base, clone );

      clone.setIndexAxis( data.getIndexAxis() );
      clone.setValueAxis( data.getValueAxis() );

      return clone;
    }
    else if( base instanceof IndexColumnType )
    {
      final IndexColumnType clone = new IndexColumnType();
      copyBasicSettings( base, clone );

      return clone;
    }

    return null;
  }

  private static void copyBasicSettings( final AbstractColumnType source, final AbstractColumnType destination )
  {
    destination.setAlignment( source.getAlignment() );
    destination.setAutopack( source.isAutopack() );
    destination.setEditable( source.isEditable() );
    destination.setFormat( source.getFormat() );
    destination.setId( source.getId() );
    destination.setLabel( source.getLabel() );
    destination.setWidth( source.getWidth() );

  }

  public static AbstractColumnType findColumnType( final ZmlTableType tableType, final String identifier )
  {
    final List<AbstractColumnType> columns = tableType.getColumns().getColumn();

    String id = identifier;

    /** cloned, multiple column entry?!? like W_1 or W_3 */

    if( PATTERN_CLONED_COLUMN_IDENTIFIER.matches( identifier ) )
    {
      final RETokenizer tokenizer = new RETokenizer( new Pattern( "_\\d+$" ), identifier );
      id = tokenizer.nextToken();
    }

    for( final AbstractColumnType column : columns )
    {
      if( column.getId().equals( id ) )
        return column;
    }

    return null;
  }

  public static RGB colorByteToRGB( final byte[] color )
  {
    final int red = byteToInt( color[0] );
    final int green = byteToInt( color[1] );
    final int blue = byteToInt( color[2] );

    return new RGB( red, green, blue );
  }

  /**
   * @param b
   *          a byte value
   */
  private static int byteToInt( final byte b )
  {
    return b & 0xff;
  }

}
