package org.kalypso.chart.ext.base.axis.provider;

import java.util.Comparator;

import org.kalypso.chart.ext.base.axis.DoubleAxis;
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
import org.kalypso.contribs.java.util.DoubleComparator;


/**
 * @author alibu
 *
 */
public class DoubleAxisProvider extends AbstractAxisProvider
{

  /**
   * @see org.kalypso.chart.factory.provider.IAxisProvider#getAxis()
   */
  public IAxis<Double> getAxis( ) throws AxisProviderException
  {
    AxisPositionParser app=new AxisPositionParser();
    String position = m_at.getPosition().toString();
    POSITION pos=app.createValueFromString( position  );
    AxisDirectionParser adp=new AxisDirectionParser();
    DIRECTION dir=adp.createValueFromString( m_at.getDirection().toString() );
    
    /**
     * Problem: 
     * org.kalypso.contribs.java.util.DoubleComparator implements Comparable<Number>,
     * but Comparable<Double> is needed here (as the generic type needs to
     * implement Comparable => wrap 
     */
    Comparator<Double> dc= new Comparator<Double>()
    {
    	Comparator<Number> m_comp=new DoubleComparator(0.001);
		public int compare(Double o1, Double o2) {
			return m_comp.compare(o1, o2);
		}
    };

    
    
    IAxis<Double> axis = new DoubleAxis( m_at.getId(), m_at.getLabel(), PROPERTY.CONTINUOUS, pos, dir, dc );
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
      
      DataRange<Double> range= null;
      try
		{
			range= new DataRange<Double>(min, max);
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
