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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.rules.IZmlTableRule;
import org.kalypso.zml.ui.table.schema.AbstractColumnType;
import org.kalypso.zml.ui.table.schema.CellStyleType;
import org.kalypso.zml.ui.table.schema.IndexColumnType;
import org.kalypso.zml.ui.table.schema.RuleType;
import org.kalypso.zml.ui.table.schema.RulesType;
import org.kalypso.zml.ui.table.schema.StyleSetType;
import org.kalypso.zml.ui.table.utils.TableTypeHelper;

/**
 * @author Dirk Kuch
 */
public class ZmlLabelProvider extends ColumnLabelProvider
{
  private static final ColorRegistry COLOR_REGISTRY = new ColorRegistry();

  private final AbstractColumnType m_type;

  Set<IZmlTableRule> m_rules = new HashSet<IZmlTableRule>();

  private final StyleSetType m_styleSet;

  public ZmlLabelProvider( final StyleSetType styleSet, final AbstractColumnType type )
  {
    m_styleSet = styleSet;
    m_type = type;

    final RulesType ruleTypes = type.getRules();
    if( ruleTypes != null )
    {
      for( final RuleType ruleType : ruleTypes.getRule() )
      {
        final String ruleIdentifier = ruleType.getRule();
        final IZmlTableRule rule = KalypsoZmlUI.getDefault().getTableRule( ruleIdentifier );
        rule.addStyle( type.getId(), ruleType.getStyle() );

        m_rules.add( rule );
      }
    }
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getBackground(java.lang.Object)
   */
  @Override
  public Color getBackground( final Object element )
  {
    final CellStyleType style = findStyle( element );

    final RGB rgb = TableTypeHelper.colorByteToRGB( style.getBackgroundColor() );
    final String name = rgb.toString();

    COLOR_REGISTRY.put( name, rgb );

    return COLOR_REGISTRY.get( name );
  }

  private CellStyleType findStyle( final Object element )
  {
    if( m_type instanceof IndexColumnType )
      return m_styleSet.getDefaultCellStyle();

    if( element instanceof ZmlTableRow )
    {
      final ZmlTableRow row = (ZmlTableRow) element;

      final ZmlValueReference reference = row.get( m_type.getId() );
      if( reference != null )
      {
        for( final IZmlTableRule rule : m_rules )
        {
          if( rule.apply( reference ) )
            return rule.getStyle( m_type.getId() );
        }

      }

    }

    return m_styleSet.getDefaultCellStyle();
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getFont(java.lang.Object)
   */
  @Override
  public Font getFont( final Object element )
  {
    // TODO Auto-generated method stub
    return super.getFont( element );
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getForeground(java.lang.Object)
   */
  @Override
  public Color getForeground( final Object element )
  {
    // TODO Auto-generated method stub
    return super.getForeground( element );
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang.Object)
   */
  @Override
  public Image getImage( final Object element )
  {
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

      if( m_type instanceof IndexColumnType )
      {
        final Object value = set.getIndexValue();

        return format( value );
      }
      else
      {
        try
        {
          final ZmlValueReference reference = set.get( m_type.getId() );
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

  private String format( final Object value )
  {
    final String format = m_type.getFormat();
    if( value instanceof Date )
    {
      final SimpleDateFormat sdf = new SimpleDateFormat( format == null ? "dd.MM.yyyy HH:mm" : format );
      return sdf.format( value );
    }

    return String.format( format == null ? "%s" : format, value );
  }
}
