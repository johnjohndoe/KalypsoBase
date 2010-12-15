package org.kalypso.zml.core.table.binding.rule.instructions;

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

import java.math.BigDecimal;

import org.apache.commons.lang.ObjectUtils;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataBoundary;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.schema.MetadataBoundaryInstructionType;

/**
 * @author Dirk Kuch
 */
public class ZmlMetadataBoundaryInstruction extends AbstractZmlRuleInstructionType
{
  public static final String PATTERN_TEXT = "${text}";

  private MetadataBoundary m_boundaryFrom;

  private MetadataBoundary m_boundaryTo;

  private IZmlModelColumn m_lastToColumn;

  private IZmlModelColumn m_lastFromColumn;

  public ZmlMetadataBoundaryInstruction( final MetadataBoundaryInstructionType type )
  {
    super( type );
  }

  @Override
  public MetadataBoundaryInstructionType getType( )
  {
    return (MetadataBoundaryInstructionType) super.getType();
  }

  public boolean matches( final IZmlValueReference reference ) throws SensorException
  {
    final MetadataBoundary metaFrom = getBoundaryFrom( reference );
    final MetadataBoundary metaTo = getBoundaryTo( reference );

    final String boundaryType = findBoundaryType( metaFrom, metaTo );

    final double value = findValue( reference, boundaryType );
    if( Double.isNaN( value ) )
      return false;

    // FIXME: get the scale from the axis and/or table column definition
    final int scale = 3;
    final BigDecimal valueDecimal = new BigDecimal( value ).setScale( scale, BigDecimal.ROUND_HALF_UP );

    if( !compareMeta( metaFrom, getType().getFrom(), valueDecimal, getType().getOpFrom() ) )
      return false;
    if( !compareMeta( metaTo, getType().getTo(), valueDecimal, getType().getOpTo() ) )
      return false;

    return true;
  }

  private MetadataBoundary getBoundaryFrom( final IZmlValueReference reference )
  {
    if( m_boundaryFrom == null || reference.getColumn() != m_lastFromColumn )
    {
      m_lastFromColumn = reference.getColumn();
      m_boundaryFrom = MetadataBoundary.getBoundary( m_lastFromColumn.getMetadata(), getType().getFrom(), new BigDecimal( -Double.MAX_VALUE ) );
    }

    return m_boundaryFrom;
  }

  private MetadataBoundary getBoundaryTo( final IZmlValueReference reference )
  {
    if( m_boundaryTo == null || reference.getColumn() != m_lastToColumn )
    {
      m_lastToColumn = reference.getColumn();
      m_boundaryTo = MetadataBoundary.getBoundary( m_lastToColumn.getMetadata(), getType().getTo(), new BigDecimal( -Double.MAX_VALUE ) );
    }

    return m_boundaryTo;
  }

  private String findBoundaryType( final MetadataBoundary metaFrom, final MetadataBoundary metaTo )
  {
    if( metaFrom != null )
      return metaFrom.getParameterType();

    if( metaTo != null )
      return metaTo.getParameterType();

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

    final Object boundaryValue = column.get( tupleModelIndex, boundaryAxis );
    if( boundaryValue instanceof Number )
      return ((Number) boundaryValue).doubleValue();

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

  private boolean compareMeta( final MetadataBoundary meta, final String property, final BigDecimal value, final String op )
  {
    if( meta == null && property != null )
      return false;
    else if( meta == null )
      return true;

    final BigDecimal compareValue = meta.getValue();

    if( "<".equals( op ) )
    {
      if( value.compareTo( compareValue ) < 0 )
        return true;
    }
    else if( "<=".equals( op ) )
    {
      if( value.compareTo( compareValue ) <= 0 )
        return true;
    }
    else if( ">".equals( op ) )
    {
      if( value.compareTo( compareValue ) > 0 )
        return true;
    }
    else if( ">=".equals( op ) )
    {
      if( value.compareTo( compareValue ) >= 0 )
        return true;
    }

    return false;
  }

  public String update( final String text )
  {
    final String label = getType().getLabel();
    if( label == null )
      return text;

    return label.replace( PATTERN_TEXT, text );
  }

}
