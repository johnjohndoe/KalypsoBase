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
package org.kalypso.zml.ui.table.provider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.binding.AbstractColumn;
import org.kalypso.zml.ui.table.binding.CellStyle;
import org.kalypso.zml.ui.table.binding.IndexColumn;
import org.kalypso.zml.ui.table.rules.IZmlTableRule;

/**
 * @author Dirk Kuch
 */
public class ZmlLabelProvider extends ColumnLabelProvider
{
  private final AbstractColumn m_column;

  public ZmlLabelProvider( final AbstractColumn column )
  {
    m_column = column;
  }

  private CellStyle findStyle( final Object element )
  {
    if( element instanceof ZmlTableRow )
    {
      final ZmlTableRow row = (ZmlTableRow) element;

      final IZmlValueReference reference = row.get( m_column );
      if( reference != null )
      {
        for( final IZmlTableRule rule : m_column.getRules() )
        {
          if( rule.apply( reference ) )
            return rule.getStyle( m_column.getIdentifier() );
        }
      }
    }

    return m_column.getCellStyle();
  }

  private String format( final Object value )
  {
    final String format = m_column.getFormat();
    if( value instanceof Date )
    {
      final SimpleDateFormat sdf = new SimpleDateFormat( format == null ? "dd.MM.yyyy HH:mm" : format );
      return sdf.format( value );
    }

    return String.format( format == null ? "%s" : format, value );
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getBackground(java.lang.Object)
   */
  @Override
  public Color getBackground( final Object element )
  {
    final CellStyle style = findStyle( element );

    return style.getBackgroundColor();
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getFont(java.lang.Object)
   */
  @Override
  public Font getFont( final Object element )
  {
    final CellStyle style = findStyle( element );

    return style.getFont();
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getForeground(java.lang.Object)
   */
  @Override
  public Color getForeground( final Object element )
  {
    final CellStyle style = findStyle( element );

    return style.getForegroundColor();
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang.Object)
   */
  @Override
  public Image getImage( final Object element )
  {
    try
    {
      final CellStyle style = findStyle( element );

      return style.getImage();
    }
    catch( final IOException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return super.getImage( element );
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
   */
  @Override
  public String getText( final Object element )
  {
    if( element instanceof ZmlTableRow )
    {
      final ZmlTableRow set = (ZmlTableRow) element;

      if( m_column instanceof IndexColumn )
      {
        final Object value = set.getIndexValue();

        return format( value );
      }
      else
      {
        try
        {
          final IZmlValueReference reference = set.get( m_column );
          if( reference != null )
            return format( reference.getValue() );

          return "";
        }
        catch( final SensorException e )
        {
          KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
        }
      }
    }

    return super.getText( element );
  }
}
