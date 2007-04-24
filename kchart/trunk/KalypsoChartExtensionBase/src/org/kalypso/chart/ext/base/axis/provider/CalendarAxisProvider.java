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
 *
 */
public class CalendarAxisProvider extends AbstractAxisProvider
{

  /**
   * @see org.kalypso.chart.factory.provider.IAxisProvider#getAxis()
   */
  public IAxis getAxis( ) throws AxisProviderException
  {
    AxisPositionParser app=new AxisPositionParser();
    POSITION pos=app.dataFromString( getAxisType().getPosition().toString() );
    AxisDirectionParser adp=new AxisDirectionParser();
    DIRECTION dir=adp.dataFromString( getAxisType().getDirection().toString() );
    IAxis<Calendar> axis = new CalendarAxis( getAxisType().getId(), getAxisType().getLabel(), PROPERTY.CONTINUOUS, pos, dir );
    if (axis!=null)
    {
        CalendarParser gcp=new CalendarParser();
        String minValStr= getAxisType().getMinVal();
        String maxValStr= getAxisType().getMaxVal();
        Calendar minVal;
        Calendar maxVal;
        
        try
        {
	       minVal=gcp.dataFromString(minValStr);
        }
        catch( MalformedValueException e )
        {
          //config-date not valid - using Calendar start date
          Logger.logError(Logger.TOPIC_LOG_CONFIG, "Unparsable value for AxisRange ("+minValStr+"); using default date ");
          minVal=Calendar.getInstance();
          minVal.setTimeInMillis( 0 );
        }
        try
        {
        	 maxVal=gcp.dataFromString(maxValStr);
        }
        catch( MalformedValueException e )
        {
          //config-date not valid - using "now"
          Logger.logError(Logger.TOPIC_LOG_CONFIG, "Unparsable value for AxisMin; using default date ");
          maxVal=Calendar.getInstance();
        }
        
        DataRange<Calendar> range = null;
        
        try
		{
			range=new DataRange<Calendar>(minVal, maxVal);
		} catch (ZeroSizeDataRangeException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        axis.setDataRange(range);
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
