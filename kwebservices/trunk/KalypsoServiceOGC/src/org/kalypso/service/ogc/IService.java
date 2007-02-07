package org.kalypso.service.ogc;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.kalypso.service.ogc.exception.OWSException;


public interface IService {

	
	public void executeOperation(Map parameters, HttpServletResponse response) throws OWSException;
	
}
