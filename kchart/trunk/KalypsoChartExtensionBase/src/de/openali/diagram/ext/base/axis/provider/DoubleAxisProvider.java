package de.openali.diagram.ext.base.axis.provider;

import org.kalypso.contribs.java.util.DoubleComparator;

import de.openali.diagram.ext.base.axis.DoubleAxis;
import de.openali.diagram.factory.configuration.exception.AxisProviderException;
import de.openali.diagram.factory.configuration.exception.MalformedValueException;
import de.openali.diagram.factory.configuration.parameters.impl.AxisDirectionParser;
import de.openali.diagram.factory.configuration.parameters.impl.AxisPositionParser;
import de.openali.diagram.factory.configuration.parameters.impl.DoubleParser;
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
public class DoubleAxisProvider extends AbstractAxisProvider
{

  /**
   * @see de.openali.diagram.factory.provider.IAxisProvider#getAxis()
   */
  public IAxis<Double> getAxis( ) throws AxisProviderException
  {
    AxisPositionParser app=new AxisPositionParser();
    String position = m_at.getPosition().toString();
    POSITION pos=app.createValueFromString( position  );
    AxisDirectionParser adp=new AxisDirectionParser();
    DIRECTION dir=adp.createValueFromString( m_at.getDirection().toString() );
    IAxis<Double> axis = new DoubleAxis( m_at.getId(), m_at.getLabel(), PROPERTY.CONTINUOUS, pos, dir, new DoubleComparator( 0.001 ) );
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
   * @see de.openali.diagram.framework.IAxisProvider#getDataClass()
   */
  public Class< ? > getDataClass( )
  {
    return Number.class;
  }




}
