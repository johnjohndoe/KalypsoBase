package org.kalypso.chart.factory.provider;

import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.util.ChartFactoryUtilities;
import org.ksp.chart.factory.AxisType;

public abstract class AbstractAxisProvider implements IAxisProvider {


	private AxisType m_at;
	private IParameterContainer m_pc;

	public void init(AxisType at) {
		
		m_at=at;
		m_pc=ChartFactoryUtilities.createXmlbeansParameterContainer(m_at.getId(), m_at.getProvider());
	}
	
	public AxisType getAxisType()
	{
		return m_at;
	}
	
	public IParameterContainer getParameterContainer()
	{
		return m_pc;
	}

}
