package org.kalypso.chart.factory.configuration.exception;

/**
 * @author burtscher Exception specialization: if a desired chart name can not be found in the configuration file, this
 *         exception should be thrown
 */
public class ConfigChartNotFoundException extends ConfigurationException
{

  /**
   * 
   */
  private static final long serialVersionUID = 9158779860328918490L;

  public ConfigChartNotFoundException( String chartname )
  {
    super( "Chart \"" + chartname + "\" was not found in configuration" );
  }
}
