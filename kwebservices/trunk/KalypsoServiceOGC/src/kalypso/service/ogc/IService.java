package kalypso.service.ogc;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import kalypso.service.ogc.exception.OWSException;

public interface IService {

	
	public void executeOperation(Map parameters, HttpServletResponse response) throws OWSException;
	
}
