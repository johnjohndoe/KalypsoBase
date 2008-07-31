package org.kalypso.service.wps.server.operations;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import net.opengeospatial.ows.CodeType;
import net.opengeospatial.ows.DCP;
import net.opengeospatial.ows.DomainType;
import net.opengeospatial.ows.HTTP;
import net.opengeospatial.ows.Operation;
import net.opengeospatial.ows.OperationsMetadata;
import net.opengeospatial.ows.RequestMethodType;
import net.opengeospatial.ows.ServiceIdentification;
import net.opengeospatial.ows.ServiceProvider;
import net.opengeospatial.wps.Capabilities;
import net.opengeospatial.wps.ProcessBriefType;
import net.opengeospatial.wps.ProcessOfferings;

import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.ogc.OGCUtilities;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.KalypsoSimulationCoreJaxb;
import org.kalypso.simulation.core.simspec.Modelspec;

/**
 * The implementation of the GetCapabilities function.
 * 
 * @author Holger Albert
 */
@SuppressWarnings("restriction")
public class GetCapabilities implements IOperation
{
  /**
   * All available simulations.
   */
  private List<Modelspec> m_simModelspecs = null;

  /**
   * The Address under which this server is available.
   */
  private String m_serverURL = null;

  /**
   * The constructor.
   */
  public GetCapabilities( )
  {
    m_serverURL = FrameworkProperties.getProperty( "org.kalypso.service.wps.service" );
    if( m_serverURL == null )
      m_serverURL = "not provided";

    /* Append a questionmark at the URL. */
    m_serverURL = m_serverURL + "?";
  }

  /**
   * @see org.kalypso.service.wps.operations.IOperation#executeOperation(org.kalypso.service.ogc.RequestBean)
   */
  public StringBuffer executeOperation( RequestBean request ) throws OWSException
  {
    try
    {
      /* Start the operation. */
      Debug.println( "Operation \"GetCapabilities\" started." );

      String acceptVersions = getAcceptVersions( request );
      if( acceptVersions != null )
      {
        /* Check if our version does match this version. */
        if( !acceptVersions.equals( OGCUtilities.VERSION ) )
        {
          /* Versions does not match. */
          throw new OWSException( OWSException.ExceptionCode.VERSION_NEGPTIOATON_FAILED, "The server does not support the given version.", "" );
        }
      }

      /* Get all simulations. */
      List<ISimulation> simulations = WPSUtilities.getSimulations();

      /* Get all specifications. */
      m_simModelspecs = new LinkedList<Modelspec>();
      for( int i = 0; i < simulations.size(); i++ )
      {
        /* Get the simulation. */
        ISimulation simulation = simulations.get( i );

        /* Get the specification for that simulation. */
        URL spezifikation = simulation.getSpezifikation();
        if( spezifikation == null )
        {
          Debug.println( "Simulation has no specification and will be ignored: " + simulation.getClass().getName() );
          continue;
        }

        Modelspec modelData = (Modelspec) KalypsoSimulationCoreJaxb.JC.createUnmarshaller().unmarshal( spezifikation );

        /* Save the specification. */
        m_simModelspecs.add( modelData );
      }

      /* Build service identification. */
      ServiceIdentification serviceIdentification = buildServiceIdentification();

      /* Build service provider. */
      ServiceProvider serviceProvider = buildServiceProvider();

      /* Build operations metadata. */
      OperationsMetadata operationsMetadata = buildOperationsMetadata();

      /* Build update sequence. */
      String updateSequence = null;

      /* Build process offerings. */
      ProcessOfferings processOfferings = buildProcessOfferings();

      /* Build the GetCapabilities response. */
      Capabilities capabilities = OGCUtilities.buildCapabilities( serviceIdentification, serviceProvider, operationsMetadata, updateSequence, processOfferings );

      /* Marshall it into one XML string. */
      String xml = MarshallUtilities.marshall( capabilities );

      /* Build the response. */
      StringBuffer response = new StringBuffer();
      response.append( xml );

      return response;
    }
    catch( Exception e )
    {
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getLocalizedMessage(), "" );
    }
  }

  /**
   * Checks for the parameter AcceptVersions and returns it. No POST/XML allowed!
   * 
   * @param request
   *          The request.
   * @return The AcceptVersions parameter or null if not present.
   */
  private String getAcceptVersions( RequestBean request )
  {
    if( !request.isPost() )
    {
      /* GET or simple POST. */

      /* Search for the parameter AcceptVersions. */
      return request.getParameterValue( "AcceptVersions" );
    }

    /* POST with XML. */
    return null;
  }

  /**
   * This function builds the service identification for this wps.
   * 
   * @return The service identification.
   */
  private ServiceIdentification buildServiceIdentification( )
  {
    List<String> serviceTypeVersion = new LinkedList<String>();
    serviceTypeVersion.add( OGCUtilities.VERSION );

    return OGCUtilities.buildServiceIdentification( null, null, null, OGCUtilities.buildCodeType( "WPS", "ISimulation" ), serviceTypeVersion, null, null );
  }

  /**
   * This function builds the service provider for this wps.
   * 
   * @return The service provider.
   */
  private ServiceProvider buildServiceProvider( )
  {
    return OGCUtilities.buildServiceProvider( "", null, OGCUtilities.buildResponsiblePartySubsetType( null, null, null, null ) );
  }

  /**
   * This function builds the operations metadata for this wps.
   * 
   * @return The operations metadata.
   */
  private OperationsMetadata buildOperationsMetadata( )
  {
    /* The operations. */
    List<Operation> operations = new LinkedList<Operation>();

    /* GetCapabilities. */
    operations.add( buildGetCapabilities() );

    /* DescribeProcess. */
    operations.add( buildDescribeProcess() );

    /* Execute. */
    operations.add( buildExecute() );

    return OGCUtilities.buildOperationsMetadata( operations, null, null, null );
  }

  /**
   * This function builds the GetCapabilities oepration.
   * 
   * @return GetCapabilities operation.
   */
  private Operation buildGetCapabilities( )
  {
    List<DCP> dcps = new LinkedList<DCP>();
    RequestMethodType requestMethod = OGCUtilities.buildRequestMethodType( m_serverURL, null, null, null, null, null, null );
    JAXBElement<RequestMethodType> get = OGCUtilities.buildHTTPGet( requestMethod );
    List<JAXBElement<RequestMethodType>> requestMethods = new LinkedList<JAXBElement<RequestMethodType>>();
    requestMethods.add( get );
    HTTP http = OGCUtilities.buildHTTP( requestMethods );
    dcps.add( OGCUtilities.buildDCP( http ) );

    List<DomainType> parameter = new LinkedList<DomainType>();

    List<Object> values = new LinkedList<Object>();
    values.add( OGCUtilities.buildValueType( "WPS" ) );
    parameter.add( OGCUtilities.buildDomainType( OGCUtilities.buildAllowedValues( values ), null, null, null, false, null, "Service" ) );

    List<Object> values1 = new LinkedList<Object>();
    values1.add( OGCUtilities.buildValueType( "GetCapabilities" ) );
    parameter.add( OGCUtilities.buildDomainType( OGCUtilities.buildAllowedValues( values1 ), null, null, null, false, null, "Request" ) );

    List<Object> values2 = new LinkedList<Object>();
    values2.add( OGCUtilities.buildValueType( OGCUtilities.VERSION ) );
    parameter.add( OGCUtilities.buildDomainType( OGCUtilities.buildAllowedValues( values2 ), null, null, null, false, null, "AcceptVersions" ) );

    return OGCUtilities.buildOperation( dcps, parameter, null, null, "GetCapabilities" );
  }

  /**
   * This function builds the DescribeProcess oepration.
   * 
   * @return DescribeProcess operation.
   */
  private Operation buildDescribeProcess( )
  {
    List<DCP> dcps = new LinkedList<DCP>();
    RequestMethodType requestMethod = OGCUtilities.buildRequestMethodType( m_serverURL, null, null, null, null, null, null );
    JAXBElement<RequestMethodType> get = OGCUtilities.buildHTTPGet( requestMethod );
    JAXBElement<RequestMethodType> post = OGCUtilities.buildHTTPPost( requestMethod );
    List<JAXBElement<RequestMethodType>> requestMethods = new LinkedList<JAXBElement<RequestMethodType>>();
    requestMethods.add( get );
    requestMethods.add( post );
    HTTP http = OGCUtilities.buildHTTP( requestMethods );
    dcps.add( OGCUtilities.buildDCP( http ) );

    List<DomainType> parameter = new LinkedList<DomainType>();

    List<Object> values = new LinkedList<Object>();
    values.add( OGCUtilities.buildValueType( "WPS" ) );
    parameter.add( OGCUtilities.buildDomainType( OGCUtilities.buildAllowedValues( values ), null, null, null, false, null, "Service" ) );

    List<Object> values1 = new LinkedList<Object>();
    values1.add( OGCUtilities.buildValueType( "DescribeProcess" ) );
    parameter.add( OGCUtilities.buildDomainType( OGCUtilities.buildAllowedValues( values1 ), null, null, null, false, null, "Request" ) );

    List<Object> values2 = new LinkedList<Object>();
    values2.add( OGCUtilities.buildValueType( OGCUtilities.VERSION ) );
    parameter.add( OGCUtilities.buildDomainType( OGCUtilities.buildAllowedValues( values2 ), null, null, null, false, null, "Version" ) );

    List<Object> allowedValues = new LinkedList<Object>();
    for( int i = 0; i < m_simModelspecs.size(); i++ )
    {
      /* Get the modelspec. */
      Modelspec modelData = m_simModelspecs.get( i );
      allowedValues.add( OGCUtilities.buildValueType( modelData.getTypeID() ) );
    }

    parameter.add( OGCUtilities.buildDomainType( OGCUtilities.buildAllowedValues( allowedValues ), null, null, null, false, null, "Identifier" ) );

    return OGCUtilities.buildOperation( dcps, parameter, null, null, "DescribeProcess" );
  }

  /**
   * This function builds the Execute oepration.
   * 
   * @return Execute operation.
   */
  private Operation buildExecute( )
  {
    List<DCP> dcps = new LinkedList<DCP>();
    RequestMethodType requestMethod = OGCUtilities.buildRequestMethodType( m_serverURL, null, null, null, null, null, null );
    JAXBElement<RequestMethodType> post = OGCUtilities.buildHTTPPost( requestMethod );
    List<JAXBElement<RequestMethodType>> requestMethods = new LinkedList<JAXBElement<RequestMethodType>>();
    requestMethods.add( post );
    HTTP http = OGCUtilities.buildHTTP( requestMethods );
    dcps.add( OGCUtilities.buildDCP( http ) );

    List<DomainType> parameter = new LinkedList<DomainType>();
    List<Object> values = new LinkedList<Object>();
    values.add( OGCUtilities.buildValueType( "WPS" ) );
    parameter.add( OGCUtilities.buildDomainType( OGCUtilities.buildAllowedValues( values ), null, null, null, false, null, "Service" ) );

    List<Object> values1 = new LinkedList<Object>();
    values1.add( OGCUtilities.buildValueType( "Execute" ) );
    parameter.add( OGCUtilities.buildDomainType( OGCUtilities.buildAllowedValues( values1 ), null, null, null, false, null, "Request" ) );

    List<Object> values2 = new LinkedList<Object>();
    values2.add( OGCUtilities.buildValueType( OGCUtilities.VERSION ) );
    parameter.add( OGCUtilities.buildDomainType( OGCUtilities.buildAllowedValues( values2 ), null, null, null, false, null, "Version" ) );

    List<Object> allowedValues = new LinkedList<Object>();
    for( int i = 0; i < m_simModelspecs.size(); i++ )
    {
      /* Get the modelspec. */
      Modelspec modelData = m_simModelspecs.get( i );
      allowedValues.add( OGCUtilities.buildValueType( modelData.getTypeID() ) );
    }

    parameter.add( OGCUtilities.buildDomainType( OGCUtilities.buildAllowedValues( allowedValues ), null, null, null, false, null, "Identifier" ) );

    return OGCUtilities.buildOperation( dcps, parameter, null, null, "Execute" );
  }

  /**
   * This function builds the process offerings.
   * 
   * @return The process offerings.
   */
  private ProcessOfferings buildProcessOfferings( )
  {
    List<ProcessBriefType> processBriefs = new LinkedList<ProcessBriefType>();
    for( int i = 0; i < m_simModelspecs.size(); i++ )
    {
      /* Get the modelspec. */
      Modelspec modelData = m_simModelspecs.get( i );

      CodeType identifier = OGCUtilities.buildCodeType( "", modelData.getTypeID() );
      String title = modelData.getTypeID();
      String abstrakt = "No descriptions available.";

      ProcessBriefType processBrief = OGCUtilities.buildProcessBriefType( identifier, title, abstrakt, null, null );
      processBriefs.add( processBrief );
    }

    return OGCUtilities.buildProcessOfferings( processBriefs );
  }
}