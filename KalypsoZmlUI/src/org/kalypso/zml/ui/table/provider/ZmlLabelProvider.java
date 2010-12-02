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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.binding.BaseColumn;
import org.kalypso.zml.ui.table.binding.CellStyle;
import org.kalypso.zml.ui.table.binding.ZmlRule;
import org.kalypso.zml.ui.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.table.rules.IZmlRuleImplementation;
import org.kalypso.zml.ui.table.schema.CellStyleType;
import org.kalypso.zml.ui.table.schema.IndexColumnType;

/**
 * @author Dirk Kuch
 */
public class ZmlLabelProvider extends ColumnLabelProvider
{
  private final BaseColumn m_column;

  private final RuleMapper m_mapper = new RuleMapper();

  private Object m_lastRow = null;

  private CellStyle m_lastCellStyle = null;

  public ZmlLabelProvider( final BaseColumn column )
  {
    m_column = column;
  }

  private CellStyle findStyle( final IZmlModelRow row ) throws CoreException
  {
    if( m_lastRow == row )
      return m_lastCellStyle;

    final ZmlRule[] rules = m_mapper.findActiveRules( row, m_column );
    if( ArrayUtils.isNotEmpty( rules ) )
    {
      CellStyleType baseType = new CellStyleType();
      for( final ZmlRule rule : rules )
      {
        baseType = CellStyle.merge( baseType, rule.getPlainStyle( row, m_column ).getType() );
      }

      m_lastCellStyle = new CellStyle( baseType );
    }
    else
    {
      m_lastCellStyle = m_column.getDefaultStyle();
    }

    m_lastRow = row;

    return m_lastCellStyle;
  }

  private String format( final Object value ) throws CoreException
  {
    final CellStyle style = m_column.getDefaultStyle();
    final String format = style.getTextFormat();
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
    if( element instanceof IZmlModelRow )
    {
      try
      {
        final CellStyle style = findStyle( (IZmlModelRow) element );

        return style.getBackgroundColor();
      }
      catch( final CoreException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return super.getBackground( element );
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getFont(java.lang.Object)
   */
  @Override
  public Font getFont( final Object element )
  {

    if( element instanceof IZmlModelRow )
    {
      try
      {
        final CellStyle style = findStyle( (IZmlModelRow) element );

        return style.getFont();
      }
      catch( final CoreException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return super.getFont( element );
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getForeground(java.lang.Object)
   */
  @Override
  public Color getForeground( final Object element )
  {
    if( element instanceof IZmlModelRow )
    {
      try
      {
        final CellStyle style = findStyle( (IZmlModelRow) element );

        return style.getForegroundColor();
      }
      catch( final CoreException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return super.getForeground( element );
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang.Object)
   */
  @Override
  public Image getImage( final Object element )
  {
    if( element instanceof IZmlModelRow )
    {
      try
      {
        final CellStyle style = findStyle( (IZmlModelRow) element );

        return style.getImage();
      }
      catch( final Exception e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return super.getImage( element );
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
   */
  @Override
  public String getText( final Object element )
  {
    if( element instanceof IZmlModelRow )
    {
      try
      {
        String text = "";

        final IZmlModelRow row = (IZmlModelRow) element;

        final Object value = getValue( row );
        if( value != null )
          text = format( value );

        final IZmlValueReference reference = row.get( m_column.getType() );
        final ZmlRule[] rules = m_mapper.findActiveRules( row, m_column );
        for( final ZmlRule rule : rules )
        {
          final IZmlRuleImplementation impl = rule.getImplementation();
          text = impl.update( rule, reference, text );
        }

        return text;
      }
      catch( final Throwable t )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }

    return super.getText( element );
  }

  private Object getValue( final IZmlModelRow row ) throws SensorException
  {
    if( m_column.getType() instanceof IndexColumnType )
    {
      return row.getIndexValue();
    }

    final IZmlValueReference reference = row.get( m_column.getType() );
    if( reference != null )
      return reference.getValue();

    return null;
  }
}
