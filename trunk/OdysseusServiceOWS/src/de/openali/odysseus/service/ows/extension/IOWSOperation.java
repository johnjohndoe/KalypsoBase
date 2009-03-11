package de.openali.odysseus.service.ows.extension;

import de.openali.odysseus.service.ows.exception.OWSException;
import de.openali.odysseus.service.ows.request.RequestBean;
import de.openali.odysseus.service.ows.request.ResponseBean;

/**
 * @author burtscher simple interface to define service operations
 * 
 */
public interface IOWSOperation
{
	/**
	 * starts ods operation - used for ods operation extension point
	 */
	public void checkAndExecute(RequestBean requestBean,
	        ResponseBean responseBean) throws OWSException;

}
