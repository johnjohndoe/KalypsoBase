package org.kalypso.chart.ext.base.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.kalypso.chart.factory.configuration.parameters.impl.CalendarParser;
import org.kalypso.chart.framework.exception.MalformedValueException;
import org.kalypso.chart.framework.exception.ZeroSizeDataRangeException;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.IDataOperator;
import org.kalypso.chart.framework.model.data.impl.DataRange;

public class CalendarDataOperator implements IDataOperator<Calendar>
{

	private CalendarParser m_stringParser;

	public CalendarDataOperator()
	{
		m_stringParser=new CalendarParser();
	}
	
	public DataRange<Calendar> getContainingInterval(Number numVal,
			Number numIntervalWidth, Number numFixedPoint)
	{
		Long min=numFixedPoint.longValue();
		Long max=numVal.longValue();
		
		int dirFactor=1;
		
		Long longFixedPoint=numFixedPoint.longValue();
		Long longVal=numVal.longValue();
		Long longIntervalWidth=numIntervalWidth.longValue();
		
		if (longFixedPoint.compareTo(longVal)>0)
		{
			dirFactor=-1;
		}
		for (long d=longFixedPoint; d>longVal; d+=dirFactor*longIntervalWidth)
		{
			min=longFixedPoint+d;
		}
		max=min+dirFactor*longIntervalWidth;
				
		DataRange<Calendar> dataRange=null;
		try
		{
			dataRange = new DataRange<Calendar>(numericalToLogical(Math.min(min, max)), numericalToLogical(Math.max(min, max)));
		} 
		catch (ZeroSizeDataRangeException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataRange;
	}

	public Long logicalToNumerical(Calendar logVal)
	{
		return logVal.getTimeInMillis();
	}



	public Calendar numericalToLogical(Number numVal)
	{
		Calendar cal= Calendar.getInstance();
		cal.set( Calendar.DST_OFFSET, 0);
	    cal.set( Calendar.ZONE_OFFSET, 0);
		cal.setTimeInMillis(numVal.longValue());
		return cal;
	}


	public String logicalToString(Calendar value) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		System.out.println(sdf.format(value.getTime()));
		return sdf.format(value.getTime());
	}

	public Calendar stringToLogical(String value) throws MalformedValueException {
		Calendar cal=m_stringParser.stringToLogical(value);;
	    return cal;
	}

	public String getFormatHint() {
		return "yyyy-MM-dd (HH:mm)";
	}



}
