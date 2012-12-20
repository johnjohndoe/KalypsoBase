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
package org.kalypso.zml.core.table.model.references;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.core.table.rules.IZmlCellRuleImplementation;
import org.kalypso.zml.core.table.schema.CellStyleType;

/**
 * @author Dirk Kuch
 */
public class ZmlModelIndexCell extends AbstractZmlCell implements IZmlModelIndexCell
{
  private CellStyle m_style;

  private final BaseColumn m_base;

  public ZmlModelIndexCell( final IZmlModelRow row, final BaseColumn column )
  {
    super( row, null, -1 );
    m_base = column;
  }

  @Override
  public Date getIndexValue( )
  {
    return getRow().getIndex();
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof ZmlModelIndexCell )
    {
      final ZmlModelIndexCell other = (ZmlModelIndexCell) obj;

      final EqualsBuilder builder = new EqualsBuilder();
      builder.append( getIndexValue(), other.getIndexValue() );

      return builder.isEquals();
    }

    return super.equals( obj );
  }

  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( getClass().getName() );
    builder.append( getIndexValue() );

    return builder.toHashCode();
  }

  @Override
  public ZmlCellRule[] findActiveRules( final ZmlModelViewport viewport )
  {
    final List<ZmlCellRule> rules = new ArrayList<>();
    final ZmlCellRule[] columnRules = m_base.getCellRules();
    for( final ZmlCellRule rule : columnRules )
    {
      final IZmlCellRuleImplementation impl = rule.getImplementation();
      if( impl.apply( rule, this ) )
      {
        rules.add( rule );
      }
    }

    return rules.toArray( new ZmlCellRule[] {} );
  }

  @Override
  public CellStyle getStyle( final ZmlModelViewport viewport ) throws CoreException
  {
    if( Objects.isNotNull( m_style ) )
      return m_style;

    final ZmlCellRule[] rules = findActiveRules( viewport );

    if( ArrayUtils.isNotEmpty( rules ) )
    {

      CellStyleType baseType = m_base.getDefaultStyle().getType();
      for( final ZmlCellRule rule : rules )
      {
        final CellStyle style = rule.getStyle( this );
        baseType = CellStyle.merge( baseType, style.getType() );
      }

      m_style = new CellStyle( baseType );
    }
    else
    {
      m_style = m_base.getDefaultStyle();
    }

    return m_style;
  }

  @Override
  public BaseColumn getBaseColumn( )
  {
    return m_base;
  }

  @Override
  public void reset( )
  {
    m_style = null;
  }
}
