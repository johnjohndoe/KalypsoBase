package org.kalypso.chart.ext.base.axis.provider;

import org.kalypso.chart.ext.base.axis.LongAxis;
import org.kalypso.chart.factory.configuration.exception.AxisProviderException;
import org.kalypso.chart.factory.configuration.exception.MalformedValueException;
import org.kalypso.chart.factory.configuration.parameters.impl.AxisDirectionParser;
import org.kalypso.chart.factory.configuration.parameters.impl.AxisPositionParser;
import org.kalypso.chart.factory.configuration.parameters.impl.LongParser;
import org.kalypso.chart.factory.provider.AbstractAxisProvider;
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
public class LongAxisProvider extends AbstractAxisProvider
{

  /**
   * @see org.kalypso.chart.factory.provider.IAxisProvider#getAxis()
   */
  public IAxis<Long> getAxis( ) throws AxisProviderException
  {
    AxisPositionParser app=new AxisPositionParser();
    String position = m_at.getPosition().toString();
    POSITION pos=app.createValueFromString( position  );
    AxisDirectionParser adp=new AxisDirectionParser();
    DIRECTION dir=adp.createValueFromString( m_at.getDirection().toString() );
    IAxis<Long> axis = new LongAxis( m_at.getId(), m_at.getLabel(), PROPERTY.CONTINUOUS, pos, dir);
    if (axis!=null)
    {
      long min=0;
      long max=1;
      LongParser lp=new LongParser();
      try
      {
        String minValStr=m_at.getMinVal();
        if (!minValStr.equals(""))
        	min=( lp.createValueFromString(minValStr) );
      }
      catch( MalformedValueException e )
      {
    	  Logger.logError(Logger.TOPIC_LOG_CONFIG, "Unparsable value for AxisMin; using default (0) ");
      }
      try
      {
        String maxValStr=m_at.getMinVal();
        if (!maxValStr.equals(""))
        	min=( lp.createValueFromString(maxValStr) );
      }
      catch( MalformedValueException e )
      {
    	  Logger.logError(Logger.TOPIC_LOG_CONFIG, "Unparsable value for AxisMax; using default (1) ");
      }
      
      DataRange<Long> range = null;
      try
	{
		range=new DataRange<Long>(min, max);
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
    return Number.class;
  }




}
