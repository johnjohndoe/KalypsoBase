package org.kalypso.chart.ext.base.axis.provider;

import java.util.Calendar;

import org.kalypso.chart.ext.base.axis.CalendarAxis;
import org.kalypso.chart.factory.configuration.exception.AxisProviderException;
import org.kalypso.chart.factory.configuration.parameters.impl.AxisDirectionParser;
import org.kalypso.chart.factory.configuration.parameters.impl.AxisPositionParser;
import org.kalypso.chart.factory.configuration.parameters.impl.CalendarParser;
import org.kalypso.chart.factory.provider.AbstractAxisProvider;
import org.kalypso.chart.framework.exception.MalformedValueException;
import org.kalypso.chart.framework.exception.ZeroSizeDataRangeException;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.impl.DataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.PROPERTY;

/**
 * @author alibu
 */
public class CalendarAxisProvider extends AbstractAxisProvider
{

  /**
   * @see org.kalypso.chart.factory.provider.IAxisProvider#getAxis()
   */
  public IAxis getAxis( ) throws AxisProviderException
  {
    final AxisPositionParser app = new AxisPositionParser();
    final POSITION pos = app.stringToLogical( getAxisType().getPosition().toString() );
    final AxisDirectionParser adp = new AxisDirectionParser();
    final DIRECTION dir = adp.stringToLogical( getAxisType().getDirection().toString() );
    final IAxis<Calendar> axis = new CalendarAxis( getAxisType().getId(), getAxisType().getLabel(), PROPERTY.CONTINUOUS, pos, dir );
    if( axis != null )
    {
      final CalendarParser gcp = new CalendarParser();
      final String minValStr = getAxisType().getMinVal();
      final String maxValStr = getAxisType().getMaxVal();
      Calendar minVal;
      Calendar maxVal;

      try
      {
        minVal = gcp.stringToLogical( minValStr );
      }
      catch( final MalformedValueException e )
      {
        // config-date not valid - using Calendar start date
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "Unparsable value for AxisRange (" + minValStr + "); using default date " );
        minVal = Calendar.getInstance();
        minVal.setTimeInMillis( 0 );
      }
      try
      {
        maxVal = gcp.stringToLogical( maxValStr );
      }
      catch( final MalformedValueException e )
      {
        // config-date not valid - using "now"
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "Unparsable value for AxisMin; using default date " );
        maxVal = Calendar.getInstance();
      }

      DataRange<Calendar> range = null;

      try
      {
        range = new DataRange<Calendar>( minVal, maxVal );
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
    return Calendar.class;
  }

}
