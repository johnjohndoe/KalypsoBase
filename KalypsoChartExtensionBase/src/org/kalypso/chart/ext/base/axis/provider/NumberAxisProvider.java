package org.kalypso.chart.ext.base.axis.provider;

import java.util.Comparator;

import org.kalypso.chart.ext.base.axis.NumberAxis;
import org.kalypso.chart.factory.configuration.exception.AxisProviderException;
import org.kalypso.chart.factory.configuration.parameters.impl.AxisDirectionParser;
import org.kalypso.chart.factory.configuration.parameters.impl.AxisPositionParser;
import org.kalypso.chart.factory.configuration.parameters.impl.DoubleParser;
import org.kalypso.chart.factory.provider.AbstractAxisProvider;
import org.kalypso.chart.framework.exception.MalformedValueException;
import org.kalypso.chart.framework.exception.ZeroSizeDataRangeException;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.impl.DataRange;
import org.kalypso.chart.framework.model.mapper.AxisAdjustment;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.PROPERTY;
import org.kalypso.contribs.java.util.DoubleComparator;
import org.ksp.chart.factory.AxisType.PreferredAdjustment;

/**
 * @author alibu
 */
public class NumberAxisProvider extends AbstractAxisProvider
{

  /**
   * @see org.kalypso.chart.factory.provider.IAxisProvider#getAxis()
   */
  public IAxis<Number> getAxis( ) throws AxisProviderException
  {
    final AxisPositionParser app = new AxisPositionParser();
    final String position = getAxisType().getPosition().toString();
    final POSITION pos = app.stringToLogical( position );
    final AxisDirectionParser adp = new AxisDirectionParser();
    final DIRECTION dir = adp.stringToLogical( getAxisType().getDirection().toString() );

    final Comparator<Number> nc = new DoubleComparator( 0.0001 );

    final IAxis<Number> axis = new NumberAxis( getAxisType().getId(), getAxisType().getLabel(), PROPERTY.CONTINUOUS, pos, dir, nc );
    if( axis != null )
    {
      // set preferred adjustment
      PreferredAdjustment adj = getAxisType().getPreferredAdjustment();
      if( adj != null )
      {
        AxisAdjustment axisAdjustment = new AxisAdjustment( adj.getBefore(), adj.getRange(), adj.getAfter() );
        axis.setPreferredAdjustment( axisAdjustment );
      }

      // set range
      double min = 0;
      double max = 1;
      final DoubleParser dp = new DoubleParser();
      try
      {
        final String minValStr = getAxisType().getMinVal();
        if( !minValStr.equals( "" ) )
          min = (dp.stringToLogical( minValStr ));
      }
      catch( final MalformedValueException e )
      {
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "Unparsable value for AxisMin; using default (0) " );
      }
      try
      {
        final String maxValStr = getAxisType().getMaxVal();
        if( !maxValStr.equals( "" ) )
          max = (dp.stringToLogical( maxValStr ));
      }
      catch( final MalformedValueException e )
      {
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "Unparsable value for AxisMax; using default (1) " );
      }

      DataRange<Number> range = null;
      try
      {
        range = new DataRange<Number>( min, max );
      }
      catch( final ZeroSizeDataRangeException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      axis.setLogicalRange( range );
    }
    else
      throw new AxisProviderException();

    return axis;
  }

  /**
   * @see org.kalypso.chart.framework.IAxisProvider#getDataClass()
   */
  public Class< ? > getDataClass( )
  {
    return Number.class;
  }

}
