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
package org.kalypso.zml.ui.table.binding;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.rules.IZmlTableRule;
import org.kalypso.zml.ui.table.schema.AbstractColumnType;
import org.kalypso.zml.ui.table.schema.AlignmentType;
import org.kalypso.zml.ui.table.schema.CellStyleType;
import org.kalypso.zml.ui.table.schema.ColumnPropertyName;
import org.kalypso.zml.ui.table.schema.ColumnPropertyType;
import org.kalypso.zml.ui.table.schema.RuleType;
import org.kalypso.zml.ui.table.schema.RulesType;
import org.kalypso.zml.ui.table.schema.ZmlTableType;
import org.kalypso.zml.ui.table.utils.TableTypeHelper;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractColumn
{
  private final AbstractColumnType m_type;

  private final ZmlTableType m_root;

  private final Set<IZmlTableRule> m_rules = new LinkedHashSet<IZmlTableRule>();

  public AbstractColumn( final ZmlTableType root, final AbstractColumnType type )
  {
    m_root = root;
    m_type = type;
  }

  public AbstractColumnType getType( )
  {
    return m_type;
  }

  public String getIdentifier( )
  {
    return m_type.getId();
  }

  public IZmlTableRule[] getRules( )
  {
    final RulesType ruleTypes = m_type.getRules();
    if( ruleTypes != null )
    {
      for( final RuleType ruleType : ruleTypes.getRule() )
      {
        final String ruleIdentifier = ruleType.getRuleReference();
        final IZmlTableRule rule = KalypsoZmlUI.getDefault().getTableRule( ruleIdentifier );
        final CellStyleType styleReference = (CellStyleType) ruleType.getStyleReference();

        rule.addStyle( getIdentifier(), new CellStyle( m_root.getStyleSet(), styleReference ) );

        m_rules.add( rule );
      }
    }

    return m_rules.toArray( new IZmlTableRule[] {} );
  }

  public String getFormat( )
  {
    return findProperty( ColumnPropertyName.FORMAT );
  }

  public AlignmentType getAlignment( )
  {
    final String property = findProperty( ColumnPropertyName.ALIGNMENT );
    if( property == null )
      return AlignmentType.fromValue( AlignmentType.LEFT.value() );

    return AlignmentType.fromValue( property );
  }

  public String getLabel( )
  {
    return findProperty( ColumnPropertyName.LABEL );
  }

  public Integer getWidth( )
  {
    final String property = findProperty( ColumnPropertyName.WIDTH );
    if( property != null )
      return Integer.valueOf( property );

    return null;
  }

  public boolean isAutopack( )
  {
    return m_type.isAutopack();
  }

  public boolean isEditable( )
  {
    return m_type.isEditable();
  }

  protected String findProperty( final ColumnPropertyName property )
  {
    final List<ColumnPropertyType> properties = m_type.getProperty();
    for( final ColumnPropertyType prop : properties )
    {
      final String propertyName = TableTypeHelper.getPropertyName( prop );
      if( property.value().equals( propertyName ) )
        return prop.getValue();
    }

    return null;
  }
}
