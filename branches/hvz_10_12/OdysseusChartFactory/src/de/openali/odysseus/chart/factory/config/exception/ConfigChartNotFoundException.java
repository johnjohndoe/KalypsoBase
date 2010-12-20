package de.openali.odysseus.chart.factory.config.exception;

/**
 * @author burtscher Exception specialization: if a desired chart name can not be found in the configuration file, this
 *         exception should be thrown
 */
public class ConfigChartNotFoundException extends ConfigurationException
{

  public ConfigChartNotFoundException( String chartname )
  {
    super( "Chart \"" + chartname + "\" was not found in configuration" );
  }
}
