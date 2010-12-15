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
package org.kalypso.zml.core.table.binding;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.AlignmentType;
import org.kalypso.zml.core.table.schema.ColumnPropertyName;
import org.kalypso.zml.core.table.schema.ColumnPropertyType;
import org.kalypso.zml.core.table.schema.HeaderEntriesType;
import org.kalypso.zml.core.table.schema.HeaderEntry;
import org.kalypso.zml.core.table.schema.RuleRefernceType;
import org.kalypso.zml.core.table.schema.RuleSetType;
import org.kalypso.zml.core.table.schema.StyleReferenceType;

/**
 * @author Dirk Kuch
 */
public class BaseColumn
{
  private final AbstractColumnType m_type;

  private ZmlRule[] m_rules;

  private CellStyle m_cellStyle;

  private CellStyle m_editingCellStyle;

  private ColumnHeader[] m_headers;

  public BaseColumn( final AbstractColumnType type )
  {
    m_type = type;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof BaseColumn )
    {
      final BaseColumn other = (BaseColumn) obj;

      final EqualsBuilder builder = new EqualsBuilder();
      builder.append( getIdentifier(), other.getIdentifier() );

      return builder.isEquals();
    }

    return super.equals( obj );
  }

  public ColumnHeader[] getHeaders( )
  {
    if( ArrayUtils.isNotEmpty( m_headers ) )
      return m_headers;

    final HeaderEntriesType headers = m_type.getHeaders();
    if( headers == null )
    {
      return new ColumnHeader[] {};
    }

    final List<ColumnHeader> columnHeaders = new ArrayList<ColumnHeader>();

    for( final HeaderEntry header : headers.getHeader() )
    {
      columnHeaders.add( new ColumnHeader( header ) );
    }

    m_headers = columnHeaders.toArray( new ColumnHeader[] {} );

    return m_headers;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( getClass().getName() );
    builder.append( getIdentifier() );

    return builder.toHashCode();
  }

  public AbstractColumnType getType( )
  {
    return m_type;
  }

  public String getIdentifier( )
  {
    return m_type.getId();
  }

  public ZmlRule[] getRules( )
  {
    if( ArrayUtils.isNotEmpty( m_rules ) )
      return m_rules;

    final List<ZmlRule> rules = new ArrayList<ZmlRule>();

    final ZmlRule[] resolved1 = resolveFromRuleSetReference();
    final ZmlRule[] resolved2 = resolveRules();

    Collections.addAll( rules, resolved1 );
    Collections.addAll( rules, resolved2 );

    m_rules = rules.toArray( new ZmlRule[] {} );

    return m_rules;
  }

  private ZmlRule[] resolveFromRuleSetReference( )
  {
    final Object ruleSetReference = m_type.getRuleSetReference();
    if( !(ruleSetReference instanceof RuleSetType) )
      return new ZmlRule[] {};

    final ZmlRuleSet ruleSet = new ZmlRuleSet( (RuleSetType) ruleSetReference );

    return ruleSet.getRules();
  }

  private ZmlRule[] resolveRules( )
  {

    final List<RuleRefernceType> ruleReferenceTypes = m_type.getRule();
    if( ruleReferenceTypes == null )
      return new ZmlRule[] {};

    final List<ZmlRule> rules = new ArrayList<ZmlRule>();
    final ZmlRuleResolver resolver = ZmlRuleResolver.getInstance();

    for( final RuleRefernceType referenceType : ruleReferenceTypes )
    {
      try
      {
        // FIXME: use the location of the current xml document (where
        // referenceType was defined) as context
        // in order to support relative url's.
        final URL context = null;
        final ZmlRule rule = resolver.findRule( context, referenceType );
        if( rule != null )
          rules.add( rule );
      }
      catch( final CoreException e )
      {
        KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return rules.toArray( new ZmlRule[] {} );
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

  public CellStyle getDefaultStyle( ) throws CoreException
  {
    if( m_cellStyle != null )
      return m_cellStyle;

    final ZmlStyleResolver resolver = ZmlStyleResolver.getInstance();
    final StyleReferenceType reference = m_type.getDefaultCellStyle();

    m_cellStyle = resolver.findStyle( reference );

    return m_cellStyle;
  }

  public CellStyle getDefaultEditingStyle( ) throws CoreException
  {
    if( m_editingCellStyle != null )
      return m_editingCellStyle;

    final ZmlStyleResolver resolver = ZmlStyleResolver.getInstance();
    final StyleReferenceType reference = m_type.getDefaultCellEditingStyle();
    if( reference == null )
      return null;

    m_editingCellStyle = resolver.findStyle( reference );

    return m_editingCellStyle;
  }

  public String getUriContextMenu( )
  {
    return findProperty( ColumnPropertyName.URI_CONTEXT_MENU );
  }

  public String getUriHeaderContextMenu( )
  {
    return findProperty( ColumnPropertyName.URI_HEADER_CONTEXT_MENU );
  }
}
