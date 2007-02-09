package org.kalypso.service.wps.server;

import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.kalypso.service.ogc.IOGCService;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.server.operations.CancelSimulation;
import org.kalypso.service.wps.server.operations.DescribeProcess;
import org.kalypso.service.wps.server.operations.DisposeSimulation;
import org.kalypso.service.wps.server.operations.Execute;
import org.kalypso.service.wps.server.operations.GetCapabilities;
import org.kalypso.service.wps.server.operations.IOperation;
import org.kalypso.service.wps.utils.Debug;

/**
 * This class implements a WPS Service.
 * 
 * @author Holger Albert
 */
public class WPSService implements IOGCService
{
  /**
   * Stores the operations.
   */
  private HashMap<String, IOperation> m_operations = null;

  /**
   * @see org.kalypso.service.ogc.IOGCService#executeOperation(org.kalypso.service.ogc.RequestBean,
   *      javax.servlet.http.HttpServletResponse)
   */
  public void executeOperation( RequestBean request, HttpServletResponse response ) throws OWSException
  {
    /* Get the REQUEST parameter. With it the operation which should be executed is determined. */
    String parameterValue = request.getParameterValue( "REQUEST" );
    if( parameterValue == null )
    {
      Debug.println( "Missing parameter REQUEST!" );
      throw new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Parameter REQUEST is missing ...", request.getUrl() );
    }

    /* Initialize everything. */
    init();

    /* Check, if the operation is available. */
    Debug.println( "Searching for operation \"" + parameterValue + "\" ..." );

    IOperation operation = m_operations.get( parameterValue );
    if( operation == null )
    {
      Debug.println( "Unsupported operation \"" + parameterValue + "\"!" );
      throw new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Invalid operation " + parameterValue + " ...", request.getUrl() );
    }

    Debug.println( "Found operation \"" + parameterValue + "\"." );

    /* Execute the operation. */
    operation.executeOperation( request );

    /* Handle the response. */
    // TODO
  }

  /**
   * @see org.kalypso.service.ogc.IOGCService#destroy()
   */
  public void destroy( )
  {
  }

  /**
   * Initializes the wps service.
   */
  private void init( )
  {
    m_operations = new HashMap<String, IOperation>();

    /* Mandatory operations. */
    m_operations.put( "DescribeProcess", new DescribeProcess() );
    m_operations.put( "Execute", new Execute() );
    m_operations.put( "GetCapabilities", new GetCapabilities() );

    /* Not OGC conform operations. */
    m_operations.put( "CancelSimulation", new CancelSimulation() );
    m_operations.put( "DisposeSimulation", new DisposeSimulation() );
  }
}