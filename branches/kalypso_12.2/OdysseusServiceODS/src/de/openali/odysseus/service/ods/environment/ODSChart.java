package de.openali.odysseus.service.ods.environment;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.service.ods.x020.ChartOfferingType;

/**
 * @author Alexander Burtscher, Holger Albert
 */
public class ODSChart implements IODSChart
{
  /**
   * The chart configuration loader.
   */
  private final ChartConfigurationLoader m_chartConfigLoader;

  /**
   * The chart offering type.
   */
  private final ChartOfferingType m_chartOfferingType;

  /**
   * The constructor.
   * 
   * @param ccl
   *          The chart configuration loader.
   * @param cot
   *          The chart offering type.
   */
  public ODSChart( final ChartConfigurationLoader ccl, final ChartOfferingType cot )
  {
    m_chartConfigLoader = ccl;
    m_chartOfferingType = cot;
  }

  /**
   * @see de.openali.odysseus.service.ods.environment.IODSChart#getChartOfferingType()
   */
  @Override
  public ChartOfferingType getChartOfferingType( )
  {
    return m_chartOfferingType;
  }

  /**
   * @see de.openali.odysseus.service.ods.environment.IODSChart#getDefinitionType()
   */
  @Override
  public ChartConfigurationLoader getDefinitionType( )
  {
    return m_chartConfigLoader;
  }
}