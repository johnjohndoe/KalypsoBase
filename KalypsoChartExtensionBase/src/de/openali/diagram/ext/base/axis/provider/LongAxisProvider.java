package de.openali.diagram.ext.base.axis.provider;

import de.openali.diagram.ext.base.axis.LongAxis;
import de.openali.diagram.factory.configuration.exception.AxisProviderException;
import de.openali.diagram.factory.configuration.exception.MalformedValueException;
import de.openali.diagram.factory.configuration.parameters.impl.AxisDirectionParser;
import de.openali.diagram.factory.configuration.parameters.impl.AxisPositionParser;
import de.openali.diagram.factory.configuration.parameters.impl.LongParser;
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
public class LongAxisProvider extends AbstractAxisProvider
{

  /**
   * @see de.openali.diagram.factory.provider.IAxisProvider#getAxis()
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
   * @see de.openali.diagram.framework.IAxisProvider#getDataClass()
   */
  public Class< ? > getDataClass( )
  {
    return Number.class;
  }




}
