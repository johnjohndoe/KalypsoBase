package org.kalypso.chart.factory.configuration.parameters;

import org.kalypso.chart.framework.model.data.IStringParser;

/**
 * The interface provides access to a collection of parameters, grouped by namespaces
 * 
 * @author alibu
 */
public interface IParameterContainer
{
  /**
   * returns a value a parameter as String or null of no parameter by the given name is present
   */
  public String getParameterValue( String paramName, String defaultValue );

  /**
   * returns a value without checking for the namespace; should only be used if there's only one namespace or if all
   * parameters have unique names
   * <p>
   * TODO: should it be 'T defaultValue' ?
   */
  public <T> T getParsedParameterValue( String paramName, String defaultValue, IStringParser<T> parser );

  public String getOwnerId( );

}
