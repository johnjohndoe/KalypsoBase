package org.kalypso.service.ogc;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.kalypso.service.ogc.exception.OWSException;


public interface IOGCService {

	/**
	 * 
	 * @param parameters
	 * @param response
	 * @throws OWSException
	 */
	public void executeOperation(Map<String, String> parameters, HttpServletResponse response) throws OWSException;
	
	/**
	 * method will be called when the main servlet is unloaded; 
	 * can be used to dispose swt-objects, etc.
	 */
	public void destroy();
	
	
}
