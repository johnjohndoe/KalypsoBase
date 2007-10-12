package org.kalypso.chart.factory.provider;

import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.util.ChartFactoryUtilities;
import org.kalypso.chart.framework.model.data.IDataOperator;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.ksp.chart.factory.AxisType;

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
    AxisType at = (AxisType) m_at.copy();
    IDataRange dra = axis.getLogicalRange();
    IDataOperator dop = axis.getDataOperator();
    at.setMinVal( dop.logicalToString( dra.getMin() ) );
    at.setMaxVal( dop.logicalToString( dra.getMax() ) );
    return at;
  }
}
