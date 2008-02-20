/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.chart.ext.base.axis.provider;

import java.util.Calendar;
import java.util.Comparator;

import org.kalypso.chart.ext.base.axis.GenericLinearAxis;
import org.kalypso.chart.factory.configuration.exception.AxisProviderException;
import org.kalypso.chart.factory.configuration.parameters.impl.AxisDirectionParser;
import org.kalypso.chart.factory.configuration.parameters.impl.AxisPositionParser;
import org.kalypso.chart.factory.configuration.parameters.impl.DoubleParser;
import org.kalypso.contribs.java.util.DoubleComparator;

import de.openali.odysseus.chart.factory.provider.AbstractAxisProvider;
import de.openali.odysseus.chart.framework.impl.model.data.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DATATYPE;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.PROPERTY;
import de.openali.odysseus.chartconfig.x010.AxisDateRangeType;
import de.openali.odysseus.chartconfig.x010.AxisNumberRangeType;
import de.openali.odysseus.chartconfig.x010.AxisStringRangeType;
import de.openali.odysseus.chartconfig.x010.AxisType;

/**
 * @author alibu
 * 
 */
public class GenericLinearAxisProvider extends AbstractAxisProvider
{

  /**
   * @see org.kalypso.chart.factory.provider.IAxisProvider#getAxis()
   */
  public IAxis getAxis( ) throws AxisProviderException
  {
    final AxisPositionParser app = new AxisPositionParser();
    final AxisType at = getAxisType();
    final String position = at.getPosition().toString();
    final POSITION pos = app.stringToLogical( position );
    final AxisDirectionParser adp = new AxisDirectionParser();
    final DIRECTION dir = adp.stringToLogical( getAxisType().getDirection().toString() );

    final Comparator<Number> nc = new DoubleComparator( 0.0001 );

    String[] valueArray = null;

    DATATYPE type = DATATYPE.NUMBER;
    if( at.isSetDateRange() )
    {
      type = DATATYPE.DATE;
    }
    else if( at.isSetDurationRange() )
    {
      type = DATATYPE.DATE;

    }
    else if( at.isSetStringRange() )
    {
      AxisStringRangeType range = at.getStringRange();
      valueArray = range.getValueSet().getValueArray();
      type = DATATYPE.STRING;
    }

    final GenericLinearAxis axis = new GenericLinearAxis( getAxisType().getId(), getAxisType().getLabel(), PROPERTY.CONTINUOUS, pos, dir, valueArray, type );
    if( axis != null )
    {

      Number min = 0;
      Number max = 1;
      final DoubleParser dp = new DoubleParser();

      if( at.isSetDateRange() )
      {
        AxisDateRangeType range = at.getDateRange();
        IDataOperator<Calendar> dataOperator = axis.getDataOperator( Calendar.class );
        Calendar minValue = range.getMinValue();
        min = dataOperator.logicalToNumerical( minValue );
        Calendar maxValue = range.getMaxValue();
        max = dataOperator.logicalToNumerical( maxValue );
      }
      if( at.isSetNumberRange() )
      {
        AxisNumberRangeType range = at.getNumberRange();
        IDataOperator<Number> dataOperator = axis.getDataOperator( Number.class );
        min = range.getMinValue();
        max = range.getMaxValue();

      }
      // TODO: String und Duration

      IDataRange<Number> range = new ComparableDataRange<Number>( new Number[] { min, max } );
      axis.setNumericRange( range );
    }
    else
    {
      throw new AxisProviderException();
    }

    return axis;
  }

  /**
   * @see org.kalypso.chart.factory.provider.IAxisProvider#getDataClass()
   */
  public Class< ? > getDataClass( )
  {
    return Number.class;
  }

  /**
   * default behaviour: return original xml type; implement, if changes shall be saved TODO: implement me
   * 
   * @see org.kalypso.chart.factory.provider.IAxisProvider#getXMLType(org.kalypso.chart.framework.model.mapper.IAxis)
   */
  @Override
  public AxisType getXMLType( IAxis axis )
  {
    AxisType at = (AxisType) getAxisType().copy();

    String dataType = (String) axis.getData( "type" );
    IDataOperator dop = null;
    if( dataType != null )
    {
      if( dataType.equals( "DATE" ) )
      {
        dop = axis.getDataOperator( Calendar.class );
      }
      else if( dataType.equals( "NUMBER" ) )
      {
        dop = axis.getDataOperator( Number.class );
      }
      else if( dataType.equals( "STRING" ) )
      {
        dop = axis.getDataOperator( String.class );
      }
    }

    IDataRange<Number> dra = axis.getNumericRange();

    // at.setMinVal( dop.logicalToString( dop.numericalToLogical( dra.getMin() ) ) );
    // at.setMaxVal( dop.logicalToString( dop.numericalToLogical( dra.getMax() ) ) );
    return at;
  }

}
