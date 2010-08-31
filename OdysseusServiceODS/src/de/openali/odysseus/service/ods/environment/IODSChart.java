package de.openali.odysseus.service.ods.environment;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.service.ods.x020.ChartOfferingType;

public interface IODSChart
{

	public ChartOfferingType getChartOfferingType();

	public ChartConfigurationLoader getDefinitionType();
}
