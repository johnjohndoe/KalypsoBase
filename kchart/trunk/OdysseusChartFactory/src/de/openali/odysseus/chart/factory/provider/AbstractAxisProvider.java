package de.openali.odysseus.chart.factory.provider;

import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;

import de.openali.odysseus.chart.factory.util.ChartFactoryUtilities;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
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
// AxisType at = (AxisType) m_at.copy();
// IDataRange<Number> dra = axis.getNumericRange();
// AbstractAxisRangeType aRange = at.getAbstractAxisRange();
// if( aRange instanceof AxisNumberRangeType )
// {
// IDataOperator<Number> dop = axis.getDataOperator( Number.class );
// AxisNumberRangeType range = (AxisNumberRangeType) aRange;
// XmlDoubleImpl xmlMin = new XmlDoubleImpl();
// xmlMin.set( dop.numericalToLogical( dra.getMin() ).doubleValue() );
// XmlDoubleImpl xmlMax = new XmlDoubleImpl();
// xmlMax.set( dop.numericalToLogical( dra.getMax() ).doubleValue() );
// range.setMinValue( xmlMin );
// range.setMaxValue( xmlMax );
// }
// else if( aRange instanceof AxisDateRangeType )
// {
// IDataOperator<Calendar> dop = axis.getDataOperator( Calendar.class );
// AxisDateRangeType range = (AxisDateRangeType) aRange;
// XmlDateTimeImpl xmlMin = new XmlDateTimeImpl();
// xmlMin.set( dop.numericalToLogical( dra.getMin() ) );
// XmlDateTimeImpl xmlMax = new XmlDateTimeImpl();
// xmlMax.set( dop.numericalToLogical( dra.getMax() ) );
// range.setMinValue( xmlMin );
// range.setMaxValue( xmlMax );
// }
// else if( aRange instanceof AxisDurationRangeType )
// {
// IDataOperator<Calendar> dop = axis.getDataOperator( Calendar.class );
// AxisStringRangeType range = (AxisStringRangeType) aRange;
// XmlDurationImpl xmlMin = new XmlDurationImpl();
// xmlMin.set( dop.numericalToLogical( dra.getMin() ) );
// XmlDateTimeImpl xmlMax = new XmlDateTimeImpl();
// xmlMax.set( dop.numericalToLogical( dra.getMax() ) );
// range.setMinValue( xmlMin );
// range.setMaxValue( xmlMax );
// }
// else if( aRange instanceof AxisStringRangeType )
// {
// IDataOperator<String> dop = axis.getDataOperator( String.class );
// AxisStringRangeType range = (AxisStringRangeType) aRange;
// XmlStringImpl xmlMin = new XmlStringImpl();
// xmlMin.set( dop.numericalToLogical( dra.getMin() ) );
// XmlDateTimeImpl xmlMax = new XmlDateTimeImpl();
// xmlMax.set( dop.numericalToLogical( dra.getMax() ) );
// range.setMinValue( xmlMin );
// range.setMaxValue( xmlMax );
// }
// return at;
    return null;
  }
}
