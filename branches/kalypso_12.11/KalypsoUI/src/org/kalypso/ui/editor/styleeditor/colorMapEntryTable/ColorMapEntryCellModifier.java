/*--------------- Kalypso-Header --------------------------------------------------------------------

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

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ui.editor.styleeditor.colorMapEntryTable;

import java.util.Arrays;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypsodeegree.graphics.sld.ColorMapEntry;

/**
 * This class implements an ICellModifier. An ICellModifier is called when the user modifes a cell in the table viewer.
 * 
 * @author Holger Albert
 */
public class ColorMapEntryCellModifier implements ICellModifier
{
  /**
   * The color map entry table.
   */
  private final ColorMapEntryTable m_table;

  /**
   * The constructor.
   * 
   * @param table
   *          The color map entry table.
   */
  public ColorMapEntryCellModifier( final ColorMapEntryTable table )
  {
    m_table = table;
  }

  /**
   * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
   */
  @Override
  public boolean canModify( final Object element, final String property )
  {
    return true;
  }

  /**
   * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
   */
  @Override
  public Object getValue( final Object element, final String property )
  {
    /* Find the index of the column. */
    final int columnIndex = Arrays.asList( ColorMapEntryTable.COLUMN_NAMES ).indexOf( property );

    /* Cast. */
    final ColorMapEntry entry = (ColorMapEntry)element;

    switch( columnIndex )
    {
      case 0: // LABEL
        return entry.getLabel();
      case 1: // QUANTITY
        return String.format( "%.1f", entry.getQuantity() ); //$NON-NLS-1$
      case 2: // COLOR
        final java.awt.Color color = entry.getColor();
        return new RGB( color.getRed(), color.getGreen(), color.getBlue() );
      case 3: // OPACITY
        return String.format( "%.1f", entry.getOpacity() ); //$NON-NLS-1$
      default:
        break;
    }

    return ""; //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
   */
  @Override
  public void modify( final Object element, final String property, final Object value )
  {
    /* Find the index of the column. */
    final int columnIndex = Arrays.asList( ColorMapEntryTable.COLUMN_NAMES ).indexOf( property );

    /* Cast. */
    final TableItem item = (TableItem)element;

    /* Get the data. */
    final ColorMapEntry entry = (ColorMapEntry)item.getData();

    switch( columnIndex )
    {
      case 0: // LABEL
        entry.setLabel( ((String)value).trim() );
        break;
      case 1: // VALUE
        entry.setQuantity( NumberUtils.parseQuietDouble( ((String)value).trim() ) );
        break;
      case 2: // COLOR
        entry.setColor( new java.awt.Color( ((RGB)value).red, ((RGB)value).green, ((RGB)value).blue ) );
        break;
      case 3: // OPACITY
        final double opacity = NumberUtils.parseQuietDouble( ((String)value).trim() );
        if( opacity <= 1 && opacity >= 0 )
          item.setBackground( 3, PlatformUI.getWorkbench().getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
        else
          item.setBackground( 3, PlatformUI.getWorkbench().getDisplay().getSystemColor( SWT.COLOR_RED ) );
        entry.setOpacity( opacity );
        break;
      default:
    }

    m_table.getColorMapEntryList().colorMapEntryChanged( entry );
  }
}