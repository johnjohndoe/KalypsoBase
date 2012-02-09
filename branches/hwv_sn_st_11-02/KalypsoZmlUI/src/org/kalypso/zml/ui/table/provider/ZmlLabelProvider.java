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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.rules.IZmlCellRuleImplementation;
import org.kalypso.zml.ui.table.model.columns.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.columns.IZmlTableIndexColumn;
import org.kalypso.zml.ui.table.model.columns.IZmlTableValueColumn;
import org.kalypso.zml.ui.table.provider.strategy.labeling.IZmlLabelStrategy;

/**
 * @author Dirk Kuch
 */
public class ZmlLabelProvider
{
  private final IZmlTableColumn m_column;

  private final IZmlModelRow m_row;

  private final ZmlCellRule[] m_activeRules;

  public ZmlLabelProvider( final IZmlModelRow row, final IZmlTableColumn column, final ZmlCellRule[] activeRules )
  {
    m_row = row;
    m_column = column;
    m_activeRules = activeRules;
  }

  public Color getBackground( )
  {
    try
    {
      final Color ruleBackgroundColor = getRuleBackground();
      if( Objects.isNotNull( ruleBackgroundColor ) )
        return ruleBackgroundColor;

      final CellStyle style = m_column.findStyle( m_row );

      return style.getBackgroundColor();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
    return null;
  }

  private Color getRuleBackground( )
  {
    for( final ZmlCellRule rule : m_activeRules )
    {
      final CellStyle style = resolveRuleStyle( rule, m_row.get( m_column.getModelColumn() ) );
      if( Objects.isNotNull( style ) )
      {
        final Color color = style.getBackgroundColor();
        if( Objects.isNotNull( color ) )
          return color;
      }
    }

    return null;
  }

  public Font getFont( )
  {
    try
    {
      final CellStyle style = m_column.findStyle( m_row );

      return style.getFont();
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }

    return null;
  }

  public Color getForeground( )
  {
    try
    {
      final CellStyle style = m_column.findStyle( m_row );

      return style.getForegroundColor();
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }

    return null;
  }

  public CellStyle resolveRuleStyle( final ZmlCellRule rule, final IZmlModelValueCell reference )
  {
    if( Objects.isNull( reference ) )
      return null;

    try
    {
      final IZmlCellRuleImplementation implementation = rule.getImplementation();
      final CellStyle style = implementation.getCellStyle( rule, reference );
      if( Objects.isNotNull( style ) )
        return style;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return null;
  }

  public String getText( ) throws SensorException, CoreException
  {
    if( m_column instanceof IZmlTableValueColumn )
    {
      final IZmlTableValueColumn column = (IZmlTableValueColumn) m_column;
      final IZmlLabelStrategy strategy = column.getLabelingStrategy();
      if( Objects.isNull( strategy ) )
        return "";

      return strategy.getText( m_row );
    }
    else if( m_column instanceof IZmlTableIndexColumn )
    {
      final IZmlTableIndexColumn column = (IZmlTableIndexColumn) m_column;
      final IZmlLabelStrategy strategy = column.getLabelingStrategy();
      if( Objects.isNull( strategy ) )
        return "";

      return strategy.getText( m_row );
    }

    throw new UnsupportedOperationException();
  }

  public Object getPlainValue( ) throws SensorException
  {
    final IZmlModelValueCell reference = m_row.get( m_column.getModelColumn() );

    return reference.getValue();
  }
}
