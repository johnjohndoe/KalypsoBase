package de.openali.odysseus.service.ows.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class RequestBean
{
	Map<String, String[]> m_parameters = null;

	String m_url = null;

	public RequestBean(HttpServletRequest request)
	{
		m_url = request.getRequestURL().toString() + "?"
		        + request.getQueryString();
		m_parameters = preprocessParameters(request.getParameterMap());
	}

	public String getUrl()
	{
		return m_url;
	}

	public String getParameterValue(String key)
	{
		String[] values = m_parameters.get(key);
		if ((values != null) && (values.length > 0))
			return values[0];
		else
			return null;
	}

	public String[] getParameterValues(String key)
	{
		return m_parameters.get(key);
	}

	/**
	 * preprocessing of parameter values; not implemented right now, but needed:
	 * parameter values can be case-insensitive
	 */
	@SuppressWarnings("unchecked")
	private Map<String, String[]> preprocessParameters(Map parameters)
	{
		Map<String, String[]> newParameters = new HashMap<String, String[]>();

		Set set = parameters.keySet();
		for (Object object : set)
		{
			String key = (String) object;
			Object objValue = parameters.get(key);
			String[] value = (String[]) objValue;
			newParameters.put(key, value);
		}
		return newParameters;
	}
}