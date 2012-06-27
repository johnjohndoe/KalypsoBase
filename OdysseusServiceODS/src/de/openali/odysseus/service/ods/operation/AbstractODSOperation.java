package de.openali.odysseus.service.ods.operation;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.ogc.core.exceptions.ExceptionCode;
import org.kalypso.ogc.core.exceptions.OWSException;
import org.kalypso.ogc.core.operations.IOGCOperation;
import org.kalypso.ogc.core.service.OGCRequest;
import org.kalypso.ogc.core.service.OGCResponse;
import org.kalypso.ogc.core.utils.OWSUtilities;

import de.openali.odysseus.service.ods.environment.IODSEnvironment;
import de.openali.odysseus.service.ods.environment.ODSEnvironment;

/**
 * abstract class for preprocessing of all ODS stuff; creating the ODSEnvironment is the initialization step that shall
 * be executed once the service starts; it checks the fulfillment of the preconditions and provides an easy access to
 * paths, etc.; operation computation does only start if the ODSEnvironment contains a non-errenous state, otherwise an
 * exception will be returned to the client
 * 
 * @author burtscher1
 */
public abstract class AbstractODSOperation implements IOGCOperation
{
  private OGCResponse m_responseBean;

  private IODSEnvironment m_env;

  private OGCRequest m_requestBean;

  /**
   * @see org.kalypso.ogc.core.operations.IOGCOperation#execute(org.kalypso.ogc.core.service.OGCRequest,
   *      org.kalypso.ogc.core.service.OGCResponse)
   */
  @Override
  public final void execute( final OGCRequest requestBean, final OGCResponse responseBean ) throws OWSException
  {
    boolean reset = false;
    if( "TRUE".equals( requestBean.getParameterValue( "RESET" ) ) )
      reset = true;

    final ODSEnvironment env = ODSEnvironment.getInstance( reset );
    final Status stat = env.getStatus();
    if( stat.getSeverity() == IStatus.ERROR )
    {
      stat.getException().printStackTrace();
      throw new OWSException( "Error initializing service.", OWSUtilities.OWS_VERSION, "en", ExceptionCode.NO_APPLICABLE_CODE, env.getStatus().getMessage() );
    }

    m_env = env;
    m_responseBean = responseBean;
    m_requestBean = requestBean;

    execute();
  }

  public abstract void execute( ) throws OWSException;

  public IODSEnvironment getEnv( )
  {
    return m_env;
  }

  public OGCResponse getResponse( )
  {
    return m_responseBean;
  }

  public OGCRequest getRequest( )
  {
    return m_requestBean;
  }
}