package de.openali.odysseus.chart.factory.config.exception;

import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;

/**
 * @author burtscher Exception specialization: if a desired chart name can not be found in the configuration file, this
 *         exception should be thrown
 */
public class ConfigChartNotFoundException extends ConfigurationException
{

  public ConfigChartNotFoundException( final String chartname )
  {
    super( "Chart \"" + chartname + "\" was not found in configuration" );
  }
}
