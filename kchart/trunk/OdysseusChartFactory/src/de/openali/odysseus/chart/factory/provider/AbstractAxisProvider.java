package de.openali.odysseus.chart.factory.provider;

import java.util.Calendar;

import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.factory.util.ChartFactoryUtilities;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chartconfig.x010.AxisDateRangeType;
import de.openali.odysseus.chartconfig.x010.AxisNumberRangeType;
import de.openali.odysseus.chartconfig.x010.AxisStringRangeType;
import de.openali.odysseus.chartconfig.x010.AxisType;

public abstract class AbstractAxisProvider implements IAxisProvider
{

  private AxisType m_at;

  private IParameterContainer m_pc;

  public void init( AxisType at )
  {

    m_at = at;
    m_pc = ChartFactoryUtilities.createXmlbeansParameterContainer( m_at.getId(), m_at.getProvider() );
  }

  public AxisType getAxisType( )
  {
    return m_at;
  }

  public IParameterContainer getParameterContainer( )
  {
    return m_pc;
  }

  /**
   * default behaviour: return original xml type; implement, if changes shall be saved
   * 
   * @see org.kalypso.chart.factory.provider.IAxisProvider#getXMLType(org.kalypso.chart.framework.model.mapper.IAxis)
   */
  public AxisType getXMLType( IAxis axis )
  {
    AxisType at = (AxisType) getAxisType().copy();

    Class< ? > clazz = axis.getDataClass();
    IDataRange<Number> numericRange = axis.getNumericRange();

    if( Calendar.class.isAssignableFrom( clazz ) )
    {
      AxisDateRangeType range = at.getDateRange();
      IDataOperator<Calendar> dop = axis.getDataOperator( Calendar.class );
      range.setMinValue( dop.numericToLogical( numericRange.getMin() ) );
      range.setMaxValue( dop.numericToLogical( numericRange.getMax() ) );
    }
    else if( Number.class.isAssignableFrom( clazz ) )
    {
      System.out.println( at );
      AxisNumberRangeType range = at.getNumberRange();
      IDataOperator<Number> dop = axis.getDataOperator( Number.class );
      range.setMinValue( dop.numericToLogical( numericRange.getMin() ).doubleValue() );
      range.setMaxValue( dop.numericToLogical( numericRange.getMax() ).doubleValue() );
    }
    else if( String.class.isAssignableFrom( clazz ) )
    {
      AxisStringRangeType range = at.getStringRange();
      IDataOperator<String> dop = axis.getDataOperator( String.class );
      range.setMinValue( dop.numericToLogical( numericRange.getMin() ) );
      range.setMaxValue( dop.numericToLogical( numericRange.getMax() ) );
    }

    return at;
  }
}
