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
import org.kalypso.service.wps.i18n.Messages;
import org.kalypso.service.wps.internal.KalypsoServiceWPSDebug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.WPSUtilities.WPS_VERSION;
import org.kalypso.service.wps.utils.ogc.WPS040ObjectFactoryUtilities;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.KalypsoSimulationCoreJaxb;
import org.kalypso.simulation.core.simspec.Modelspec;

/**
 * The implementation of the GetCapabilities function.
 * 
 * @author Holger Albert
 */
@SuppressWarnings("restriction")
public class GetCapabilitiesOperation implements IOperation
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
  public GetCapabilitiesOperation( )
  {
    m_serverURL = FrameworkProperties.getProperty( "org.kalypso.service.wps.service" ); //$NON-NLS-1$
    if( m_serverURL == null )
      m_serverURL = "not provided"; //$NON-NLS-1$

    /* Append a questionmark at the URL. */
    m_serverURL = m_serverURL + "?"; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.service.wps.operations.IOperation#executeOperation(org.kalypso.service.ogc.RequestBean)
   */
  public StringBuffer executeOperation( RequestBean request ) throws OWSException
  {
    try
    {
      /* Start the operation. */
      KalypsoServiceWPSDebug.DEBUG.printf( "Operation \"GetCapabilities\" started.\n" ); //$NON-NLS-1$

      String acceptVersions = getAcceptVersions( request );
      if( acceptVersions != null )
      {
        /* Check if our version does match this version. */
        // TODO: version 1.0
        if( !acceptVersions.equals( WPSUtilities.WPS_VERSION.V040.toString() ) )
        {
          /* Versions does not match. */
          throw new OWSException( OWSException.ExceptionCode.VERSION_NEGPTIOATON_FAILED, Messages.getString( "org.kalypso.service.wps.server.operations.GetCapabilitiesOperation.0" ), "" ); //$NON-NLS-1$ //$NON-NLS-2$
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
          KalypsoServiceWPSDebug.DEBUG.printf( "Simulation has no specification and will be ignored: " + simulation.getClass().getName() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
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
      Capabilities capabilities = WPS040ObjectFactoryUtilities.buildCapabilities( serviceIdentification, serviceProvider, operationsMetadata, updateSequence, processOfferings );

      /* Marshall it into one XML string. */
      String xml = MarshallUtilities.marshall( capabilities, WPS_VERSION.V040 );

      /* Build the response. */
      StringBuffer response = new StringBuffer();
      response.append( xml );

      return response;
    }
    catch( Exception e )
    {
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getLocalizedMessage(), "" ); //$NON-NLS-1$
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
      return request.getParameterValue( "AcceptVersions" ); //$NON-NLS-1$
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
    // TODO: add version 1.0
    serviceTypeVersion.add( WPSUtilities.WPS_VERSION.V040.toString() );

    return WPS040ObjectFactoryUtilities.buildServiceIdentification( null, null, null, WPS040ObjectFactoryUtilities.buildCodeType( "kalypso:wps", "ISimulation" ), serviceTypeVersion, null, null ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * This function builds the service provider for this wps.
   * 
   * @return The service provider.
   */
  private ServiceProvider buildServiceProvider( )
  {
    return WPS040ObjectFactoryUtilities.buildServiceProvider( "", null, WPS040ObjectFactoryUtilities.buildResponsiblePartySubsetType( null, null, null, null ) ); //$NON-NLS-1$
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

    return WPS040ObjectFactoryUtilities.buildOperationsMetadata( operations, null, null, null );
  }

  /**
   * This function builds the GetCapabilities oepration.
   * 
   * @return GetCapabilities operation.
   */
  private Operation buildGetCapabilities( )
  {
    List<DCP> dcps = new LinkedList<DCP>();
    RequestMethodType requestMethod = WPS040ObjectFactoryUtilities.buildRequestMethodType( m_serverURL, null, null, null, null, null, null );
    JAXBElement<RequestMethodType> get = WPS040ObjectFactoryUtilities.buildHTTPGet( requestMethod );
    List<JAXBElement<RequestMethodType>> requestMethods = new LinkedList<JAXBElement<RequestMethodType>>();
    requestMethods.add( get );
    HTTP http = WPS040ObjectFactoryUtilities.buildHTTP( requestMethods );
    dcps.add( WPS040ObjectFactoryUtilities.buildDCP( http ) );

    List<DomainType> parameter = new LinkedList<DomainType>();

    List<Object> values = new LinkedList<Object>();
    values.add( WPS040ObjectFactoryUtilities.buildValueType( "WPS" ) ); //$NON-NLS-1$
    parameter.add( WPS040ObjectFactoryUtilities.buildDomainType( WPS040ObjectFactoryUtilities.buildAllowedValues( values ), null, null, null, false, null, "Service" ) ); //$NON-NLS-1$

    List<Object> values1 = new LinkedList<Object>();
    values1.add( WPS040ObjectFactoryUtilities.buildValueType( "GetCapabilities" ) ); //$NON-NLS-1$
    parameter.add( WPS040ObjectFactoryUtilities.buildDomainType( WPS040ObjectFactoryUtilities.buildAllowedValues( values1 ), null, null, null, false, null, "Request" ) ); //$NON-NLS-1$

    List<Object> values2 = new LinkedList<Object>();
    values2.add( WPS040ObjectFactoryUtilities.buildValueType( WPSUtilities.WPS_VERSION.V040.toString() ) );
    parameter.add( WPS040ObjectFactoryUtilities.buildDomainType( WPS040ObjectFactoryUtilities.buildAllowedValues( values2 ), null, null, null, false, null, "AcceptVersions" ) ); //$NON-NLS-1$

    return WPS040ObjectFactoryUtilities.buildOperation( dcps, parameter, null, null, "GetCapabilities" ); //$NON-NLS-1$
  }

  /**
   * This function builds the DescribeProcess oepration.
   * 
   * @return DescribeProcess operation.
   */
  private Operation buildDescribeProcess( )
  {
    List<DCP> dcps = new LinkedList<DCP>();
    RequestMethodType requestMethod = WPS040ObjectFactoryUtilities.buildRequestMethodType( m_serverURL, null, null, null, null, null, null );
    JAXBElement<RequestMethodType> get = WPS040ObjectFactoryUtilities.buildHTTPGet( requestMethod );
    JAXBElement<RequestMethodType> post = WPS040ObjectFactoryUtilities.buildHTTPPost( requestMethod );
    List<JAXBElement<RequestMethodType>> requestMethods = new LinkedList<JAXBElement<RequestMethodType>>();
    requestMethods.add( get );
    requestMethods.add( post );
    HTTP http = WPS040ObjectFactoryUtilities.buildHTTP( requestMethods );
    dcps.add( WPS040ObjectFactoryUtilities.buildDCP( http ) );

    List<DomainType> parameter = new LinkedList<DomainType>();

    List<Object> values = new LinkedList<Object>();
    values.add( WPS040ObjectFactoryUtilities.buildValueType( "WPS" ) ); //$NON-NLS-1$
    parameter.add( WPS040ObjectFactoryUtilities.buildDomainType( WPS040ObjectFactoryUtilities.buildAllowedValues( values ), null, null, null, false, null, "Service" ) ); //$NON-NLS-1$

    List<Object> values1 = new LinkedList<Object>();
    values1.add( WPS040ObjectFactoryUtilities.buildValueType( "DescribeProcess" ) ); //$NON-NLS-1$
    parameter.add( WPS040ObjectFactoryUtilities.buildDomainType( WPS040ObjectFactoryUtilities.buildAllowedValues( values1 ), null, null, null, false, null, "Request" ) ); //$NON-NLS-1$

    List<Object> values2 = new LinkedList<Object>();
    values2.add( WPS040ObjectFactoryUtilities.buildValueType( WPSUtilities.WPS_VERSION.V040.toString() ) );
    parameter.add( WPS040ObjectFactoryUtilities.buildDomainType( WPS040ObjectFactoryUtilities.buildAllowedValues( values2 ), null, null, null, false, null, "Version" ) ); //$NON-NLS-1$

    List<Object> allowedValues = new LinkedList<Object>();
    for( int i = 0; i < m_simModelspecs.size(); i++ )
    {
      /* Get the modelspec. */
      Modelspec modelData = m_simModelspecs.get( i );
      allowedValues.add( WPS040ObjectFactoryUtilities.buildValueType( modelData.getTypeID() ) );
    }

    parameter.add( WPS040ObjectFactoryUtilities.buildDomainType( WPS040ObjectFactoryUtilities.buildAllowedValues( allowedValues ), null, null, null, false, null, "Identifier" ) ); //$NON-NLS-1$

    return WPS040ObjectFactoryUtilities.buildOperation( dcps, parameter, null, null, "DescribeProcess" ); //$NON-NLS-1$
  }

  /**
   * This function builds the Execute oepration.
   * 
   * @return Execute operation.
   */
  private Operation buildExecute( )
  {
    List<DCP> dcps = new LinkedList<DCP>();
    RequestMethodType requestMethod = WPS040ObjectFactoryUtilities.buildRequestMethodType( m_serverURL, null, null, null, null, null, null );
    JAXBElement<RequestMethodType> post = WPS040ObjectFactoryUtilities.buildHTTPPost( requestMethod );
    List<JAXBElement<RequestMethodType>> requestMethods = new LinkedList<JAXBElement<RequestMethodType>>();
    requestMethods.add( post );
    HTTP http = WPS040ObjectFactoryUtilities.buildHTTP( requestMethods );
    dcps.add( WPS040ObjectFactoryUtilities.buildDCP( http ) );

    List<DomainType> parameter = new LinkedList<DomainType>();
    List<Object> values = new LinkedList<Object>();
    values.add( WPS040ObjectFactoryUtilities.buildValueType( "WPS" ) ); //$NON-NLS-1$
    parameter.add( WPS040ObjectFactoryUtilities.buildDomainType( WPS040ObjectFactoryUtilities.buildAllowedValues( values ), null, null, null, false, null, "Service" ) ); //$NON-NLS-1$

    List<Object> values1 = new LinkedList<Object>();
    values1.add( WPS040ObjectFactoryUtilities.buildValueType( "Execute" ) ); //$NON-NLS-1$
    parameter.add( WPS040ObjectFactoryUtilities.buildDomainType( WPS040ObjectFactoryUtilities.buildAllowedValues( values1 ), null, null, null, false, null, "Request" ) ); //$NON-NLS-1$

    List<Object> values2 = new LinkedList<Object>();
    values2.add( WPS040ObjectFactoryUtilities.buildValueType( WPSUtilities.WPS_VERSION.V040.toString() ) );
    parameter.add( WPS040ObjectFactoryUtilities.buildDomainType( WPS040ObjectFactoryUtilities.buildAllowedValues( values2 ), null, null, null, false, null, "Version" ) ); //$NON-NLS-1$

    List<Object> allowedValues = new LinkedList<Object>();
    for( int i = 0; i < m_simModelspecs.size(); i++ )
    {
      /* Get the modelspec. */
      Modelspec modelData = m_simModelspecs.get( i );
      allowedValues.add( WPS040ObjectFactoryUtilities.buildValueType( modelData.getTypeID() ) );
    }

    parameter.add( WPS040ObjectFactoryUtilities.buildDomainType( WPS040ObjectFactoryUtilities.buildAllowedValues( allowedValues ), null, null, null, false, null, "Identifier" ) ); //$NON-NLS-1$

    return WPS040ObjectFactoryUtilities.buildOperation( dcps, parameter, null, null, "Execute" ); //$NON-NLS-1$
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

      CodeType identifier = WPS040ObjectFactoryUtilities.buildCodeType( "", modelData.getTypeID() ); //$NON-NLS-1$
      String title = modelData.getTypeID();
      String abstrakt = "No descriptions available."; //$NON-NLS-1$

      ProcessBriefType processBrief = WPS040ObjectFactoryUtilities.buildProcessBriefType( identifier, title, abstrakt, null, null );
      processBriefs.add( processBrief );
    }

    return WPS040ObjectFactoryUtilities.buildProcessOfferings( processBriefs );
  }
}