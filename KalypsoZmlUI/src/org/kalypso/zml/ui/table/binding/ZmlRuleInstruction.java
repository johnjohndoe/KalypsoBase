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

import java.math.BigDecimal;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataBoundary;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.ui.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.table.schema.RuleInstruction;
import org.kalypso.zml.ui.table.schema.StyleReferenceType;

/**
 * @author Dirk Kuch
 */
public class ZmlRuleInstruction
{
  public static final String PATTERN_TEXT = "${text}";

  private final RuleInstruction m_type;

  private CellStyle m_style;

  public ZmlRuleInstruction( final RuleInstruction type )
  {
    m_type = type;
  }

  public boolean matches( final IZmlValueReference reference ) throws SensorException
  {
    final IZmlModelColumn column = reference.getColumn();

    final MetadataList metadata = column.getMetadata();
    final IAxis valueAxis = column.getValueAxis();
    final Object refValue = reference.getValue();
    if( metadata == null || valueAxis == null || !(refValue instanceof Number) )
      return false;

    final String keyFrom = m_type.getFrom();
    final String keyTo = m_type.getTo();
    final String opFrom = m_type.getOpFrom();
    final String opTo = m_type.getOpTo();

    final MetadataBoundary metaFrom = MetadataBoundary.getBoundary( metadata, keyFrom, new BigDecimal( -Double.MAX_VALUE ) );
    final MetadataBoundary metaTo = MetadataBoundary.getBoundary( metadata, keyTo, new BigDecimal( -Double.MIN_VALUE ) );

    /* If defined but not satisfied, do not apply this rule. */
    if( keyFrom != null && metaFrom == null )
      return false;
    if( keyTo != null && metaTo == null )
      return false;

    final String boundaryType = findBoundaryType( metaFrom, metaTo );

    final double value = findValue( reference, boundaryType );
    if( Double.isNaN( value ) )
      return false;

    // FIXME: get the scale from the axis and/or table column definition
    final int scale = 3;
    final BigDecimal valueDecimal = new BigDecimal( value ).setScale( scale, BigDecimal.ROUND_HALF_UP );

    if( !compareMeta( metaFrom, valueDecimal, opFrom ) )
      return false;
    if( !compareMeta( metaTo, valueDecimal, opTo ) )
      return false;

    return true;
  }

  private String findBoundaryType( final MetadataBoundary metaFrom, final MetadataBoundary metaTo )
  {
    if( metaFrom != null )
      return metaFrom.getType();

    if( metaTo != null )
      return metaTo.getType();

    return null;
  }

  private double findValue( final IZmlValueReference reference, final String boundaryType ) throws SensorException
  {
    if( boundaryType == null )
      return getReferenceValue( reference );

    final IZmlModelColumn column = reference.getColumn();
    final IAxis valueAxis = column.getValueAxis();
    if( ObjectUtils.equals( valueAxis.getType(), boundaryType ) )
      return getReferenceValue( reference );

    /* Type of boundary is different from value type -> we need to retrieve the value ourselfs */
    final Integer tupleModelIndex = reference.getTupleModelIndex();
    if( tupleModelIndex == null )
      return Double.NaN;

    final IAxis[] axes = column.getAxes();
    final IAxis boundaryAxis = AxisUtils.findAxis( axes, boundaryType );
    if( boundaryAxis == null )
      return Double.NaN;

    final Object boundaryValue = column.get( 0, valueAxis );
    if( boundaryValue instanceof Number )
      return ((Number) boundaryAxis).doubleValue();

    return Double.NaN;
  }

  /**
   * Returns the value hold by the reference itself.
   */
  private double getReferenceValue( final IZmlValueReference reference ) throws SensorException
  {
    final Object valueObject = reference.getValue();
    if( valueObject instanceof Number )
      return ((Number) valueObject).doubleValue();

    return Double.NaN;
  }

  private boolean compareMeta( final MetadataBoundary meta, final BigDecimal value, final String op )
  {
    if( meta == null )
      return true;

    final BigDecimal compareValue = meta.getValue();

    if( "<".equals( op ) )
    {
      if( value.compareTo( compareValue ) < 0 )
        return true;
    }

    if( "<=".equals( op ) )
    {
      if( value.compareTo( compareValue ) <= 0 )
        return true;
    }

    if( ">".equals( op ) )
    {
      if( value.compareTo( compareValue ) > 0 )
        return true;
    }

    if( ">=".equals( op ) )
    {
      if( value.compareTo( compareValue ) >= 0 )
        return true;
    }

    return false;
  }

  public String update( final String text )
  {
    final String label = m_type.getLabel();
    return label.replace( PATTERN_TEXT, text );
  }

  public CellStyle getStyle( ) throws CoreException
  {
    if( m_style != null )
      return m_style;

    final ZmlStyleResolver resolver = ZmlStyleResolver.getInstance();
    final StyleReferenceType styleReference = m_type.getStyleReference();

    m_style = resolver.findStyle( styleReference );
    return m_style;
  }

}
