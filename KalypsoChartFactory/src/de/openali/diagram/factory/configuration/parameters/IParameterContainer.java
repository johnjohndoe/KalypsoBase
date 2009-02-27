package de.openali.diagram.factory.configuration.parameters;

import de.openali.diagram.factory.configuration.xsd.ParametersType;

/**
 * The interface provides access to a collection of parameters, grouped by namespaces
 * @author alibu
 *
 */
public interface IParameterContainer
{
		
		

    /**
     * returns the value of a parameter with the given name or the defaultValue if the parameter is not found.
     */
    public<T> T getParsedParameterValue(String paramName, String defaultValue, String namespace, IStringParser<T> parser);


    /**
     * returns the String representation of a parameter with the given name or the defaultValue if the parameter is not found.
     */
    public String getParameterValue(String paramName, String defaultValue, String namespace);



    /**
     * returns a value without checking for the namespace; should only be used if there's only one namespace or if all parameters have unique names
     */
    public String getParameterValue(String paramName, String defaultValue);


    /**
     * returns a value without checking for the namespace; should only be used if there's only one namespace or if all parameters have unique names
     */
    public<T> T getParsedParameterValue(String paramName, String defaultValue, IStringParser<T> parser);


    /**
     * returns the first namespace which posseses a parameter by the name paramName
     */
    public String getParameterNamespace(String paramName);
}

