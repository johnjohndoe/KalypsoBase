package de.openali.odysseus.service.ods.environment;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.service.ods.x020.ChartOfferingType;

public class ODSChart implements IODSChart
{

	private final ChartConfigurationLoader m_chartConfigLoader;
	private final ChartOfferingType m_chartOfferingType;

	public ODSChart(ChartConfigurationLoader ccl, ChartOfferingType cot)
	{
		m_chartConfigLoader = ccl;
		m_chartOfferingType = cot;
	}

	@Override
	public ChartOfferingType getChartOfferingType()
	{
		return m_chartOfferingType;
	}

	@Override
	public ChartConfigurationLoader getDefinitionType()
	{
		return m_chartConfigLoader;
	}

}
