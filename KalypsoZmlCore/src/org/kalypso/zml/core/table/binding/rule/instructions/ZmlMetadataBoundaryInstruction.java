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

import org.apache.commons.lang3.ObjectUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.lang.Strings;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataBoundary;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.KalypsoZmlCoreExtensions;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.rules.impl.grenzwert.IZmlGrenzwertValue;
import org.kalypso.zml.core.table.schema.AbstractRuleInstructionType;
import org.kalypso.zml.core.table.schema.MetadataBoundaryInstructionType;

/**
 * @author Dirk Kuch
 */
public class ZmlMetadataBoundaryInstruction extends AbstractZmlRuleInstructionType
{
  public static final String PATTERN_TEXT = "${text}"; //$NON-NLS-1$

  public ZmlMetadataBoundaryInstruction( final AbstractRuleInstructionType type )
  {
    super( type );
  }

  @Override
  public MetadataBoundaryInstructionType getType( )
  {
    return (MetadataBoundaryInstructionType) super.getType();
  }

  @Override
  public boolean matches( final IZmlModelCell reference ) throws SensorException
  {
    if( !(reference instanceof IZmlModelValueCell) )
      return false;

    final IZmlModelValueCell cell = (IZmlModelValueCell) reference;

    final MetadataBoundary metaFrom = getBoundaryFrom( cell );
    final MetadataBoundary metaTo = getBoundaryTo( cell );
    if( Objects.allNull( metaFrom, metaTo ) )
      return false;

    final String boundaryType = findBoundaryType( metaFrom, metaTo );

    final double value = findValue( cell, boundaryType );
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

  private MetadataBoundary getBoundaryFrom( final IZmlModelValueCell reference )
  {
    final IZmlModelColumn column = reference.getColumn();
    final MetadataList metadata = column.getMetadata();
    if( metadata == null )
      return null;

    return getBoundary( metadata, getType().getFrom(), getType().getFromExtensionPoint(), new BigDecimal( -Double.MAX_VALUE ), getType().getFactorFrom() );
  }

  private MetadataBoundary getBoundary( final MetadataList metadata, final String property, final String propertyExtensionPoint, final BigDecimal defaultValue, final double factor )
  {
    if( Strings.isEmpty( propertyExtensionPoint ) )
      return getBoundary( metadata, property, defaultValue, factor );

    final IZmlGrenzwertValue delegate = KalypsoZmlCoreExtensions.getInstance().findGrenzwertDelegate( propertyExtensionPoint );
    if( Objects.isNull( delegate ) )
      return null;

    final Double v = delegate.getValue( metadata, property );
    if( Objects.isNull( v ) )
      return getBoundary( metadata, property, defaultValue, factor );

    final double value = v * factor;

    return new MetadataBoundary( property, BigDecimal.valueOf( value ) );
  }

  private MetadataBoundary getBoundary( final MetadataList metadata, final String property, final BigDecimal defaultValue, final double factor )
  {
    final MetadataBoundary boundary = MetadataBoundary.getBoundary( metadata, property, defaultValue, factor );

    return boundary;
  }

  private MetadataBoundary getBoundaryTo( final IZmlModelValueCell reference )
  {
    final IZmlModelColumn column = reference.getColumn();
    final MetadataList metadata = column.getMetadata();
    if( Objects.isNull( metadata ) )
      return null;

    return getBoundary( metadata, getType().getTo(), getType().getToExtensionPoint(), new BigDecimal( -Double.MAX_VALUE ), getType().getFactorTo() );
  }

  private String findBoundaryType( final MetadataBoundary metaFrom, final MetadataBoundary metaTo )
  {
    if( metaFrom != null )
      return metaFrom.getParameterType();

    if( metaTo != null )
      return metaTo.getParameterType();

    return null;
  }

  private double findValue( final IZmlModelValueCell reference, final String boundaryType ) throws SensorException
  {
    if( Objects.isNull( boundaryType ) )
      return getReferenceValue( reference );

    final IZmlModelColumn column = reference.getColumn();
    final IAxis valueAxis = column.getValueAxis();
    if( Objects.isNull( valueAxis ) )
      return getReferenceValue( reference ); // will return Double.NaN

    if( ObjectUtils.equals( valueAxis.getType(), boundaryType ) )
      return getReferenceValue( reference );

    /* Type of boundary is different from value type -> we need to retrieve the value ourself's */
    final Integer tupleModelIndex = reference.getModelIndex();
    if( Objects.isNull( tupleModelIndex ) )
      return Double.NaN;

    final IAxis[] axes = column.getAxes();
    final IAxis boundaryAxis = AxisUtils.findAxis( axes, boundaryType );
    if( Objects.isNull( boundaryAxis ) )
      return Double.NaN;

    final Object boundaryValue = column.get( tupleModelIndex, boundaryAxis );
    if( boundaryValue instanceof Number )
      return ((Number) boundaryValue).doubleValue();

    return Double.NaN;
  }

  /**
   * Returns the value hold by the reference itself.
   */
  private double getReferenceValue( final IZmlModelValueCell reference ) throws SensorException
  {
    final Object value = reference.getValue();
    if( !(value instanceof Number) )
      return Double.NaN;

    return ((Number) value).doubleValue();
  }

  private boolean compareMeta( final MetadataBoundary meta, final String property, final BigDecimal value, final String op )
  {
    if( meta == null && property != null )
      return false;
    else if( meta == null )
      return true;

    final BigDecimal compareValue = meta.getValue();

    if( "<".equals( op ) )//$NON-NLS-1$ //$NON-NLS-1$
    {
      if( value.compareTo( compareValue ) < 0 )
        return true;
    }
    else if( "<=".equals( op ) )//$NON-NLS-1$ //$NON-NLS-1$
    {
      if( value.compareTo( compareValue ) <= 0 )
        return true;
    }
    else if( ">".equals( op ) )//$NON-NLS-1$ //$NON-NLS-1$
    {
      if( value.compareTo( compareValue ) > 0 )
        return true;
    }
    else if( ">=".equals( op ) ) //$NON-NLS-1$ //$NON-NLS-1$
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
