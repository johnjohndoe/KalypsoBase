package de.openali.odysseus.chart.factory.provider;

import java.net.URL;

import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;

public abstract class AbstractAxisProvider extends AbstractChartComponentProvider implements IAxisProvider
{

  private POSITION m_pos;

  private String[] m_valueArray;

  private Class< ? > m_dataClass;

  /**
   * @see de.openali.odysseus.chart.factory.provider.IAxisProvider#init(de.openali.odysseus.chart.framework.model.IChartModel,
   *      java.lang.String, de.openali.odysseus.chart.factory.config.parameters.IParameterContainer, java.net.URL,
   *      java.lang.Class, de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION, java.lang.String[])
   */
  @Override
  public void init( final IChartModel model, final String id, final IParameterContainer parameters, final URL context, final Class< ? > dataClass, final POSITION pos, final String[] valueArray )
  {
    super.init( model, id, parameters, context );
    m_pos = pos;
    m_valueArray = valueArray;
    m_dataClass = dataClass;
  }

  protected POSITION getPosition( )
  {
    return m_pos;
  }

  protected Class< ? > getDataClass( )
  {
    return m_dataClass;
  }

  protected String[] getValueArray( )
  {
    return m_valueArray;
  }

// /**
// * default behaviour: return original xml type; implement, if changes shall be saved
// *
// * @see org.kalypso.chart.factory.provider.IAxisProvider#getXMLType(org.kalypso.chart.framework.model.mapper.IAxis)
// */
// public AxisType getXMLType( IAxis axis )
// {
// AxisType at = (AxisType) getAxisType().copy();
//
// Class< ? > clazz = axis.getDataClass();
// IDataRange<Number> numericRange = axis.getNumericRange();
//
// if( Calendar.class.isAssignableFrom( clazz ) )
// {
// AxisDateRangeType range = at.getDateRange();
// IDataOperator<Calendar> dop = axis.getDataOperator( Calendar.class );
// range.setMinValue( dop.numericToLogical( numericRange.getMin() ) );
// range.setMaxValue( dop.numericToLogical( numericRange.getMax() ) );
// }
// else if( Number.class.isAssignableFrom( clazz ) )
// {
// System.out.println( at );
// AxisNumberRangeType range = at.getNumberRange();
// IDataOperator<Number> dop = axis.getDataOperator( Number.class );
// range.setMinValue( dop.numericToLogical( numericRange.getMin() ).doubleValue() );
// range.setMaxValue( dop.numericToLogical( numericRange.getMax() ).doubleValue() );
// }
// else if( String.class.isAssignableFrom( clazz ) )
// {
// AxisStringRangeType range = at.getStringRange();
// IDataOperator<String> dop = axis.getDataOperator( String.class );
// range.setMinValue( dop.numericToLogical( numericRange.getMin() ) );
// range.setMaxValue( dop.numericToLogical( numericRange.getMax() ) );
// }
// return at;
// }

}
