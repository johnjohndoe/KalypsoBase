package org.kalypso.chart.ext.base.axis.provider;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.kalypso.chart.ext.base.axis.ComparableNumberAxis;
import org.kalypso.chart.factory.configuration.exception.AxisProviderException;
import org.kalypso.chart.factory.configuration.exception.MalformedValueException;
import org.kalypso.chart.factory.configuration.parameters.impl.AxisDirectionParser;
import org.kalypso.chart.factory.configuration.parameters.impl.AxisPositionParser;
import org.kalypso.chart.factory.configuration.parameters.impl.DoubleParser;
import org.kalypso.chart.factory.provider.AbstractAxisProvider;
import org.kalypso.chart.framework.exception.ZeroSizeDataRangeException;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.impl.DataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.PROPERTY;
import org.kalypso.chart.framework.util.math.ComparableNumber;


/**
 * @author alibu
 *
 */
public class ComparableNumberAxisProvider extends AbstractAxisProvider
{

  /**
   * @see org.kalypso.chart.factory.provider.IAxisProvider#getAxis()
   */
  public IAxis<ComparableNumber> getAxis( ) throws AxisProviderException
  {
    AxisPositionParser app=new AxisPositionParser();
    String position = m_at.getPosition().toString();
    POSITION pos=app.createValueFromString( position  );
    AxisDirectionParser adp=new AxisDirectionParser();
    DIRECTION dir=adp.createValueFromString( m_at.getDirection().toString() );
    
    
    
    IAxis<ComparableNumber> axis = new ComparableNumberAxis( m_at.getId(), m_at.getLabel(), PROPERTY.CONTINUOUS, pos, dir, new ComparableComparator());
    if (axis!=null)
    {
      double min=0;
      double max=1;
      DoubleParser dp=new DoubleParser();
      try
      {
        String minValStr=m_at.getMinVal();
        if (!minValStr.equals(""))
        	min=( dp.createValueFromString(minValStr) );
      }
      catch( MalformedValueException e )
      {
    	  Logger.logError(Logger.TOPIC_LOG_CONFIG, "Unparsable value for AxisMin; using default (0) ");
      }
      try
      {
        String maxValStr=m_at.getMaxVal();
        if (!maxValStr.equals(""))
        	min=( dp.createValueFromString(maxValStr) );
      }
      catch( MalformedValueException e )
      {
    	  Logger.logError(Logger.TOPIC_LOG_CONFIG, "Unparsable value for AxisMax; using default (1) ");
      }
      
      DataRange<ComparableNumber> range= null;
      try
		{
			range= new DataRange<ComparableNumber>(new ComparableNumber(min), new ComparableNumber(max));
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
    return ComparableNumber.class;
  }




}
