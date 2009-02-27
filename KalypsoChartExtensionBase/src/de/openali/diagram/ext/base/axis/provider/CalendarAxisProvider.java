package de.openali.diagram.ext.base.axis.provider;

import java.util.Calendar;

import de.openali.diagram.ext.base.axis.CalendarAxis;
import de.openali.diagram.factory.configuration.exception.AxisProviderException;
import de.openali.diagram.factory.configuration.exception.MalformedValueException;
import de.openali.diagram.factory.configuration.parameters.impl.AxisDirectionParser;
import de.openali.diagram.factory.configuration.parameters.impl.AxisPositionParser;
import de.openali.diagram.factory.configuration.parameters.impl.CalendarParser;
import de.openali.diagram.framework.exception.ZeroSizeDataRangeException;
import de.openali.diagram.framework.logging.Logger;
import de.openali.diagram.framework.model.data.impl.DataRange;
import de.openali.diagram.framework.model.mapper.IAxis;
import de.openali.diagram.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.diagram.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.diagram.framework.model.mapper.IAxisConstants.PROPERTY;


/**
 * @author alibu
 *
 */
public class CalendarAxisProvider  extends AbstractAxisProvider
{

  /**
   * @see de.openali.diagram.factory.provider.IAxisProvider#getAxis()
   */
  public IAxis getAxis( ) throws AxisProviderException
  {
    AxisPositionParser app=new AxisPositionParser();
    POSITION pos=app.createValueFromString( m_at.getPosition().toString() );
    AxisDirectionParser adp=new AxisDirectionParser();
    DIRECTION dir=adp.createValueFromString( m_at.getDirection().toString() );
    IAxis<Calendar> axis = new CalendarAxis( m_at.getId(), m_at.getLabel(), PROPERTY.CONTINUOUS, pos, dir );
    if (axis!=null)
    {
        CalendarParser gcp=new CalendarParser();
        String minValStr= m_at.getMinVal();
        String maxValStr= m_at.getMaxVal();
        Calendar minVal;
        Calendar maxVal;
        
        try
        {
	       minVal=gcp.createValueFromString(minValStr);
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
        	 maxVal=gcp.createValueFromString(maxValStr);
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
   * @see de.openali.diagram.framework.IAxisProvider#getDataClass()
   */
  public Class< ? > getDataClass( )
  {
    return Calendar.class;
  }




}
