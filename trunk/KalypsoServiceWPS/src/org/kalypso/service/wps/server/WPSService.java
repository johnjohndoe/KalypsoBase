package org.kalypso.service.wps.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import net.opengeospatial.ows.GetCapabilitiesType;
import net.opengeospatial.wps.RequestBaseType;

import org.kalypso.service.ogc.IOGCService;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.i18n.Messages;
import org.kalypso.service.wps.server.operations.CancelSimulation;
import org.kalypso.service.wps.server.operations.DescribeProcessOperation;
import org.kalypso.service.wps.server.operations.DisposeSimulation;
import org.kalypso.service.wps.server.operations.ExecuteOperation;
import org.kalypso.service.wps.server.operations.GetCapabilitiesOperation;
import org.kalypso.service.wps.server.operations.IOperation;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.MarshallUtilities;

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
   *      org.kalypso.service.ogc.ResponseBean)
   */
  public void executeOperation( final RequestBean request, final ResponseBean responseBean ) throws OWSException
  {
    String parameterValue = null;
    if( request.isPost() && request.getBody() != null )
    {
      try
      {
        /* POST with XML. */
        final String xml = request.getBody();

        /* Check if ALL parameter are available. */
        final Object object = MarshallUtilities.unmarshall( xml );

        if( object instanceof RequestBaseType )
        {
          final RequestBaseType baseRequest = (RequestBaseType) object;

          /* Need the XML attribute service. */
          final String service = baseRequest.getService();
          if( service == null || service.length() == 0 )
          {
            Debug.println( "Missing attribute Service!" ); //$NON-NLS-1$
            throw new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, Messages.getString("org.kalypso.service.wps.server.WPSService.0"), "Service" ); //$NON-NLS-1$ //$NON-NLS-2$
          }

          /* Need the XML attribute version. */
          final String version = baseRequest.getVersion();
          if( version == null || version.length() == 0 )
          {
            Debug.println( "Missing attribute Version!" ); //$NON-NLS-1$
            throw new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, Messages.getString("org.kalypso.service.wps.server.WPSService.1"), "Version" ); //$NON-NLS-1$ //$NON-NLS-2$
          }

          /* The type of the unmarshalled object has to be the right one. */
          if( baseRequest instanceof net.opengeospatial.wps.DescribeProcess )
            parameterValue = "DescribeProcess"; //$NON-NLS-1$
          else if( baseRequest instanceof net.opengeospatial.wps.Execute )
            parameterValue = "Execute"; //$NON-NLS-1$
          else
          {
            Debug.println( "Wrong request type!" ); //$NON-NLS-1$
            throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, Messages.getString("org.kalypso.service.wps.server.WPSService.2"), "" ); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }
        else if( object instanceof JAXBElement )
        {
          // check type of contained value
          final JAXBElement< ? > element = (JAXBElement< ? >) object;
          final Object value = element.getValue();
          if( value instanceof GetCapabilitiesType )
            parameterValue = "GetCapabilities"; //$NON-NLS-1$

          // TODO What, if it is another type? Throw an exception?
        }

        // TODO What, if none of this matches? Throw an exception?
      }
      catch( final JAXBException e )
      {
        throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getLocalizedMessage(), "" ); //$NON-NLS-1$
      }
    }
    else
    {
      /* GET or simple POST. */

      /* Get the REQUEST parameter. With it the operation which should be executed is determined. */
      parameterValue = request.getParameterValue( "Request" ); //$NON-NLS-1$
      if( parameterValue == null )
      {
        Debug.println( "Missing parameter Request!" ); //$NON-NLS-1$
        throw new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, Messages.getString("org.kalypso.service.wps.server.WPSService.3"), "Request" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }

    /* Initialize everything. */
    init();

    /* Check, if the operation is available. */
    Debug.println( "Searching for operation \"" + parameterValue + "\" ..." ); //$NON-NLS-1$ //$NON-NLS-2$
    final IOperation operation = m_operations.get( parameterValue );
    if( operation == null )
    {
      Debug.println( "Unsupported operation \"" + parameterValue + "\"!" ); //$NON-NLS-1$ //$NON-NLS-2$
      throw new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "Invalid operation '" + parameterValue + "' ...", "Request" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /* Execute the operation. */
    Debug.println( "Found operation \"" + parameterValue + "\"." ); //$NON-NLS-1$ //$NON-NLS-2$
    final StringBuffer buffer = operation.executeOperation( request );

    /* Handle the response. */
    if( buffer != null )
    {
      final OutputStream outputStream = responseBean.getOutputStream();
      final OutputStreamWriter writer = new OutputStreamWriter( outputStream );
      try
      {
        writer.write( buffer.toString() );
        writer.close();
      }
      catch( final IOException e )
      {
        Debug.println( "Sending the response failed: " + e.getLocalizedMessage() ); //$NON-NLS-1$
        throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, Messages.getString("org.kalypso.service.wps.server.WPSService.4") + e.getLocalizedMessage(), "" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  }

  /**
   * @see org.kalypso.service.ogc.IOGCService#responsibleFor(org.kalypso.service.ogc.RequestBean)
   */
  public boolean responsibleFor( final RequestBean request )
  {
    if( request.isPost() )
    {
      /* Post request. */
      final String body = request.getBody();

      /* If no body is present, the wps service is not responsible. */
      if( body == null )
        return false;

      try
      {
        /* Check if the wps service could be responsible for this input. */
        Debug.println( "Checking if the WPS service is responsible for this input ..." ); //$NON-NLS-1$
        final Object object = MarshallUtilities.unmarshall( body );
        // requestBase can be of any known type, needs checking
        Debug.println( "Marshalling was successfull." ); //$NON-NLS-1$

        if( object instanceof RequestBaseType )
        {
          final RequestBaseType requestBase = (RequestBaseType) object;
          if( !requestBase.getService().equals( "WPS" ) ) //$NON-NLS-1$
          {
            Debug.println( "No, the request was send to another service using the same binding classes." ); //$NON-NLS-1$
            return false;
          }

          Debug.println( "Yes, it seems it is responsible for the request." ); //$NON-NLS-1$
          return true;
        }
        else if( object instanceof JAXBElement )
        {
          // check type of contained value
          final JAXBElement< ? > element = (JAXBElement< ? >) object;
          final Object value = element.getValue();
          if( value instanceof GetCapabilitiesType )
          {
            Debug.println( "This is a GetCababilities request. This WPS feels responsible for it." ); //$NON-NLS-1$
            return true;
          }

          Debug.println( "This is a known binding class, but it is not a valid request for this service." ); //$NON-NLS-1$
          return false;
        }

        Debug.println( "No, the request was send to another service using the same binding classes." ); //$NON-NLS-1$
        return false;
      }
      catch( final JAXBException e )
      {
        Debug.println( "No, marshalling has failed: " + e.getLocalizedMessage() ); //$NON-NLS-1$
        return false;
      }
    }
    else
    {
      /* Get request. */
      final String parameterValue = request.getParameterValue( "Service" ); //$NON-NLS-1$

      if( parameterValue != null && parameterValue.equals( "WPS" ) ) //$NON-NLS-1$
        return true;

      return false;
    }
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
    m_operations.put( "DescribeProcess", new DescribeProcessOperation() ); //$NON-NLS-1$
    m_operations.put( "Execute", new ExecuteOperation() ); //$NON-NLS-1$
    m_operations.put( "GetCapabilities", new GetCapabilitiesOperation() ); //$NON-NLS-1$

    /* Not OGC conform operations. */
    m_operations.put( "CancelSimulation", new CancelSimulation() ); //$NON-NLS-1$
    m_operations.put( "DisposeSimulation", new DisposeSimulation() ); //$NON-NLS-1$
  }
}