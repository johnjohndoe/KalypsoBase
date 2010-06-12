package de.openali.odysseus.service.ods.operation;

import javax.servlet.ServletContext;

import org.eclipse.core.runtime.Status;

import de.openali.odysseus.service.ods.environment.IODSEnvironment;
import de.openali.odysseus.service.ods.environment.ODSEnvironment;
import de.openali.odysseus.service.ows.exception.OWSException;
import de.openali.odysseus.service.ows.exception.OWSException.ExceptionCode;
import de.openali.odysseus.service.ows.extension.IOWSOperation;
import de.openali.odysseus.service.ows.request.RequestBean;
import de.openali.odysseus.service.ows.request.ResponseBean;

/**
 * abstract class for preprocessing of all ODS stuff; creating the
 * ODSEnvironment is the initialization step that shall be executed once the
 * service starts; it checks the fulfillment of the preconditions and provides
 * an easy access to paths, etc.; operation computation does only start if the
 * ODSEnvironment contains a non-errenous state, otherwise an exception will be
 * returned to the client
 * 
 * @author burtscher1
 */
public abstract class AbstractODSOperation implements IOWSOperation
{

	private ResponseBean m_responseBean;

	private IODSEnvironment m_env;

	private RequestBean m_requestBean;

	@Override
	public final void checkAndExecute(RequestBean requestBean,
	        ResponseBean responseBean, ServletContext context)
	        throws OWSException
	{
		boolean reset = false;
		if ("TRUE".equals(requestBean.getParameterValue("RESET")))
			reset = true;
		ODSEnvironment env = ODSEnvironment.getInstance(context, reset);
		Status stat = env.getStatus();
		if (stat.getSeverity() == Status.ERROR)
		{
			stat.getException().printStackTrace();
			throw new OWSException(ExceptionCode.NO_APPLICABLE_CODE,
			        "Error initializing service.", env.getStatus().getMessage());
		}

		m_env = env;
		m_responseBean = responseBean;
		m_requestBean = requestBean;

		execute();
	}

	public abstract void execute() throws OWSException;

	public IODSEnvironment getEnv()
	{
		return m_env;
	}

	public ResponseBean getResponse()
	{
		return m_responseBean;
	}

	public RequestBean getRequest()
	{
		return m_requestBean;
	}

}
