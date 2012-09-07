package org.kalypso.ogc.wfs;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.deegree.datatypes.QualifiedName;
import org.deegree.model.filterencoding.capabilities.Operator;
import org.deegree.model.filterencoding.capabilities.SpatialOperator;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.Protocol;
import org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.net.ProxyUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.net.UrlUtilities;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gml.GMLException;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaFactory;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An WebFeatureServiceClient. Implements the basic operations to access an OGC-WebFeatureService.
 *
 * @author Gernot Belger
 */
public class WFSClient
{
  public static final String OPERATION_DESCRIBE_FEATURE_TYPE = "DescribeFeatureType"; //$NON-NLS-1$

  public static final String OPERATION_GET_CAPABILITIES = "GetCapabilities"; //$NON-NLS-1$

  private static final String OPERATION_GET_FEATURE = "GetFeature"; //$NON-NLS-1$

  // TODO: move these in constant interface
  public static final String URL_PARAM_VERSION = "VERSION"; //$NON-NLS-1$

  public static final String URL_PARAM_VERSION_DEFAULT = "1.1.0"; //$NON-NLS-1$

  public static final String URL_PARAM_SERVICE = "SERVICE"; //$NON-NLS-1$

  public static final String URL_PARAM_SERVICE_DEFAULT = "WFS"; //$NON-NLS-1$

  public static final String URL_PARAM_REQUEST = "REQUEST"; //$NON-NLS-1$

  private static final String PARAM_DESCRIBE_FEATURE_TYPE_NAMESPACE = "NAMESPACE"; //$NON-NLS-1$

  private static final String PARAM_DESCRIBE_FEATURE_TYPE_TYPENAME = "TYPENAME"; //$NON-NLS-1$

  public static final String PARAM_DESCRIBE_FEATURE_TYPE_FORMAT = "OUTPUTFORMAT"; //$NON-NLS-1$

  private static final String OUTPUT_FORMAT = "text/xml; subtype=gml/3.1.1"; //$NON-NLS-1$

  private static String PARAM_DESCRIBE_FEATURE_TYPE_FORMAT_DEFAULT;
  {
    try
    {
      PARAM_DESCRIBE_FEATURE_TYPE_FORMAT_DEFAULT = URLEncoder.encode( OUTPUT_FORMAT, "UTF-8" ); //$NON-NLS-1$
    }
    catch( final UnsupportedEncodingException e )
    {
      // this will never happen
      e.printStackTrace();
    }
  }

  private final URL m_wfsURL;

  private WFSCapabilities m_wfsCapabilities;

  private final HttpClient m_httpClient;

  /** All Feature types supported by this WFS. */
  private final Map<QName, WFSFeatureType> m_featureTypes = new HashMap<>();

  private final Map<WFSFeatureType, IFeatureType> m_schemaHash = new HashMap<>();

  public WFSClient( final URL wfsURL )
  {
    m_wfsURL = wfsURL;
    m_httpClient = createHttpClient( wfsURL );
  }

  public URL getUrl( )
  {
    return m_wfsURL;
  }

  /**
   * Initialises the client. Must be called before any other method is called.
   */
  public IStatus load( )
  {
    try
    {
      final WFSCapabilitiesDocument wfsCapsDoc = new WFSCapabilitiesDocument();
      final URL capasUrl = createCapabilitiesUrl( m_wfsURL );
      wfsCapsDoc.load( capasUrl );
      m_wfsCapabilities = (WFSCapabilities) wfsCapsDoc.parseCapabilities();

      /* Hash the feature types */
      final FeatureTypeList featureTypeList = m_wfsCapabilities.getFeatureTypeList();
      final WFSFeatureType[] featureTypes = featureTypeList.getFeatureTypes();
      for( final WFSFeatureType featureType : featureTypes )
      {
        final QualifiedName name = featureType.getName();
        final QName qname = new QName( name.getNamespace().toString(), name.getLocalName(), name.getPrefix() );
        m_featureTypes.put( qname, featureType );
      }

      return Status.OK_STATUS;
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
      return StatusUtilities.createStatus( IStatus.ERROR, Messages.getString( "org.kalypso.ogc.wfs.WFSClient.1", m_wfsURL.toExternalForm() ), e );//$NON-NLS-1$
    }
    catch( final IOException e )
    {
      e.printStackTrace();
      return StatusUtilities.createStatus( IStatus.ERROR, Messages.getString( "org.kalypso.ogc.wfs.WFSClient.2", m_wfsURL.toExternalForm() ), e );//$NON-NLS-1$
    }
    catch( final SAXException e )
    {
      e.printStackTrace();
      return StatusUtilities.createStatus( IStatus.ERROR, Messages.getString( "org.kalypso.ogc.wfs.WFSClient.3", m_wfsURL.toExternalForm() ), e );//$NON-NLS-1$
    }
    catch( final InvalidCapabilitiesException e )
    {
      e.printStackTrace();
      return StatusUtilities.createStatus( IStatus.ERROR, Messages.getString( "org.kalypso.ogc.wfs.WFSClient.3", m_wfsURL.toExternalForm() ), e );//$NON-NLS-1$
    }
  }

  /**
   * Creates an url to the capabilities document for a given WFs-base-URL. I.e. adds the appropriate query part to the
   * given url.
   */
  private static URL createCapabilitiesUrl( final URL wfsURL ) throws MalformedURLException
  {
    final Map<String, String> params = UrlUtilities.parseQuery( wfsURL );

    /* Check if necessary parameters already exists; if yes they must fit else we have a problem */

    /* SERVICE */
    if( params.containsKey( URL_PARAM_SERVICE ) )
    {
      final String serviceName = params.get( URL_PARAM_SERVICE );
      if( !URL_PARAM_SERVICE_DEFAULT.equals( serviceName ) )
        throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.wfs.WFSClient.0", serviceName ) ); //$NON-NLS-1$
    }
    else
    {
      params.put( URL_PARAM_SERVICE, URL_PARAM_SERVICE_DEFAULT );
    }

    /* VERSION */
    if( params.containsKey( URL_PARAM_VERSION ) )
    {
      final String version = params.get( URL_PARAM_VERSION );
      if( !URL_PARAM_VERSION_DEFAULT.equals( version ) )
        throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.wfs.WFSClient.4", version ) ); //$NON-NLS-1$
    }
    else
    {
      params.put( URL_PARAM_VERSION, URL_PARAM_VERSION_DEFAULT );
    }

    /* REQUEST */
    if( params.containsKey( URL_PARAM_REQUEST ) )
    {
      final String request = params.get( URL_PARAM_REQUEST );
      if( !OPERATION_GET_CAPABILITIES.equals( request ) )
        throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.wfs.WFSClient.5", request ) ); //$NON-NLS-1$
    }
    else
    {
      params.put( URL_PARAM_REQUEST, OPERATION_GET_CAPABILITIES );
    }

    return UrlUtilities.addQuery( wfsURL, params );
  }

  public WFSFeatureType[] getFeatureTypes( )
  {
    return m_featureTypes.values().toArray( new WFSFeatureType[m_featureTypes.size()] );
  }

  public WFSFeatureType getFeatureType( final QName name )
  {
    return m_featureTypes.get( name );
  }

  private static HttpClient createHttpClient( final URL url )
  {
    final HttpClient client = ProxyUtilities.getConfiguredHttpClient( 60000, url, 3 );

    return client;
  }

  /**
   * This function returns all filter capabilities operations for the wfs.
   *
   * @return All filter capabilities operations.
   */
  public String[] getAllFilterCapabilitesOperations( )
  {
    final List<String> operators = new ArrayList<>();

    final SpatialOperator[] spatialOperators = m_wfsCapabilities.getFilterCapabilities().getSpatialCapabilities().getSpatialOperators();
    for( final SpatialOperator spatialOperator : spatialOperators )
    {
      operators.add( spatialOperator.getName() );
    }

    final QualifiedName[] geometryOperands = m_wfsCapabilities.getFilterCapabilities().getSpatialCapabilities().getGeometryOperands();
    for( final QualifiedName qualifiedName : geometryOperands )
    {
      operators.add( qualifiedName.getLocalName() );
    }

    final Operator[] arithmeticOperators = m_wfsCapabilities.getFilterCapabilities().getScalarCapabilities().getArithmeticOperators();
    for( final Operator operator : arithmeticOperators )
    {
      operators.add( operator.getName() );
    }

    final Operator[] comparisonOperators = m_wfsCapabilities.getFilterCapabilities().getScalarCapabilities().getComparisonOperators();
    for( final Operator operator : comparisonOperators )
    {
      operators.add( operator.getName() );
    }

    return operators.toArray( new String[] {} );
  }

  public Operation getOperation( final String operationName )
  {
    final Operation[] operations = m_wfsCapabilities.getOperationsMetadata().getOperations();
    for( final Operation operation : operations )
    {
      if( operation.getName().equals( operationName ) )
        return operation;
    }

    return null;
  }

  /**
   * Finds the first get operation {@link URL} for the given operation.
   */
  public URL findGetOperationURL( final String operationName )
  {
    final Operation operation = getOperation( operationName );
    if( operation == null )
      return null;

    final DCPType[] dcps = operation.getDCPs();
    for( final DCPType dcp : dcps )
    {
      final Protocol protocol = dcp.getProtocol();
      if( protocol instanceof HTTP )
      {
        final URL[] getOnlineResources = ((HTTP) protocol).getGetOnlineResources();
        if( getOnlineResources.length > 0 )
          return getOnlineResources[0];
      }
    }

    return null;
  }

  /**
   * Finds the first post operation {@link URL} for the given operation.
   */
  public URL findPostOperationURL( final String operationName )
  {
    final Operation operation = getOperation( operationName );
    if( operation == null )
      return null;

    final DCPType[] dcps = operation.getDCPs();
    for( final DCPType dcp : dcps )
    {
      final Protocol protocol = dcp.getProtocol();
      if( protocol instanceof HTTP )
      {
        final URL[] postOnlineResources = ((HTTP) protocol).getPostOnlineResources();
        if( postOnlineResources.length > 0 )
          return postOnlineResources[0];
      }
    }

    return null;
  }

  /**
   * Implementation of the 'DescribeFeatureType'-Operation. Returns the URL to the schema document.
   */
  public URL createDescribeFeatureTypeURL( final QName name ) throws MalformedURLException
  {
    final WFSFeatureType featureType = getFeatureType( name );
    final URL describeUrl = findGetOperationURL( WFSClient.OPERATION_DESCRIBE_FEATURE_TYPE );
    if( describeUrl == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.wfs.WFSClient.6", WFSClient.OPERATION_DESCRIBE_FEATURE_TYPE ) ); //$NON-NLS-1$

    final QualifiedName qname = featureType.getName();

    final Map<String, String> params = UrlUtilities.parseQuery( describeUrl );
    if( !params.containsKey( URL_PARAM_SERVICE ) )
    {
      params.put( URL_PARAM_SERVICE, URL_PARAM_SERVICE_DEFAULT );
    }

    params.put( URL_PARAM_VERSION, m_wfsCapabilities.getVersion() );
    params.put( URL_PARAM_REQUEST, OPERATION_DESCRIBE_FEATURE_TYPE );
    params.put( PARAM_DESCRIBE_FEATURE_TYPE_FORMAT, PARAM_DESCRIBE_FEATURE_TYPE_FORMAT_DEFAULT );

    final String namespaceUri = qname.getNamespace().toString();
    final String prefix = qname.getPrefix();
    final String namespaceValue = String.format( "xmlns(%s=%s)", prefix, namespaceUri ); //$NON-NLS-1$
    params.put( PARAM_DESCRIBE_FEATURE_TYPE_NAMESPACE, namespaceValue );

    final String typenameValue = String.format( "%s:%s", prefix, qname.getLocalName() ); //$NON-NLS-1$
    params.put( PARAM_DESCRIBE_FEATURE_TYPE_TYPENAME, typenameValue );

    return UrlUtilities.addQuery( describeUrl, params );
  }

  public WFSCapabilities operationGetCapabilities( )
  {
    return m_wfsCapabilities;
  }

  public URL operationDescribeFeatureType( final QName name ) throws CoreException
  {
    try
    {
      return createDescribeFeatureTypeURL( name );
    }
    catch( final MalformedURLException e )
    {
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, Messages.getString( "org.kalypso.ogc.wfs.WFSClient.1", m_wfsURL.toExternalForm() ), e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
  }

  /**
   * Implementation of the 'GetFeature'-Operation. Returns XXXX?
   */
  // TODO: exception handling
  public GMLWorkspace operationGetFeature( final QName name, final String filter, final Integer maxFeatures ) throws CoreException
  {
    InputStream inputStream = null;
    String requestString = null;
    try
    {
      /* Create getFeature URL */
      final URL getFeatureURL = createGetFeatureURL( name );
      final URL describeFeatureTypeURL = createDescribeFeatureTypeURL( name );

      requestString = getFeatureURL.toURI().toString();
      final GetMethod getMethod = new GetMethod( requestString );

      final int statusCode = m_httpClient.executeMethod( getMethod );
      if( statusCode != 200 )
      {
        // REMARK: with OGC-Services, it's always 200, even on error...
        System.out.println( "Status Code: " + statusCode ); //$NON-NLS-1$
        throw new HttpException( "Connection error: " + statusCode ); //$NON-NLS-1$
      }

      inputStream = new BufferedInputStream( getMethod.getResponseBodyAsStream() );

      final InputSource inputSource = new InputSource( inputStream );
      final GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( inputSource, describeFeatureTypeURL, null, null );
      inputStream.close();
      return workspace;
    }
    catch( final URISyntaxException e )
    {
      final String message = Messages.getString( "org.kalypso.ogc.wfs.WFSClient.1", e.getInput() ); //$NON-NLS-1$
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, message, e );
      throw new CoreException( status );
    }
    catch( final UnsupportedEncodingException e )
    {
      // should never happen
      final String message = String.format( e.toString() );
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, message, e );
      throw new CoreException( status );
    }
    catch( final HttpException e )
    {
      final String message = String.format( Messages.getString( "org.kalypso.ogc.wfs.WFSClient.7" ), name, requestString ); //$NON-NLS-1$
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, message, e );
      throw new CoreException( status );
    }
    catch( final IOException e )
    {
      final String message = String.format( Messages.getString( "org.kalypso.ogc.wfs.WFSClient.7" ), name, requestString ); //$NON-NLS-1$
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, message, e );
      throw new CoreException( status );
    }
    catch( final GMLException e )
    {
      // TODO: we should distinguish parse errors and errors returned from wfs-requests.
      // But how in both cases, we get an GMLException...

      final String message = String.format( Messages.getString("WFSClient.0"), name, requestString ); //$NON-NLS-1$
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, message, e );
      throw new CoreException( status );
    }
    catch( final Exception e )
    {
      final String message = String.format( Messages.getString("WFSClient.1"), name, requestString ); //$NON-NLS-1$
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, message, e );
      throw new CoreException( status );
    }
    finally
    {
      IOUtils.closeQuietly( inputStream );
    }
  }

  public URL createGetFeatureURL( final QName name ) throws MalformedURLException
  {
    final WFSFeatureType featureType = getFeatureType( name );
    final QualifiedName qname = featureType.getName();

    final Map<String, String> params = new HashMap<>(); // UrlUtilities.parseQuery( getUrl );

    params.put( URL_PARAM_SERVICE, "WFS" ); //$NON-NLS-1$
    params.put( URL_PARAM_VERSION, m_wfsCapabilities.getVersion() );
    params.put( URL_PARAM_REQUEST, OPERATION_GET_FEATURE );
    params.put( PARAM_DESCRIBE_FEATURE_TYPE_FORMAT, PARAM_DESCRIBE_FEATURE_TYPE_FORMAT_DEFAULT );
    params.put( PARAM_DESCRIBE_FEATURE_TYPE_NAMESPACE, qname.getNamespace().toString() );
    params.put( PARAM_DESCRIBE_FEATURE_TYPE_TYPENAME, qname.getLocalName() );

    return getGetUrl( name, params );
  }

  public URL getGetUrl( final QName name, final Map<String, String> params ) throws MalformedURLException
  {
    final WFSFeatureType featureType = getFeatureType( name );
    final URL describeUrl = findGetOperationURL( WFSClient.OPERATION_DESCRIBE_FEATURE_TYPE );
    if( describeUrl == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.wfs.WFSClient.8" ) + WFSClient.OPERATION_DESCRIBE_FEATURE_TYPE ); //$NON-NLS-1$

    final QualifiedName qname = featureType.getName();

    final String namespaceUri = qname.getNamespace().toString();
    final String prefix = qname.getPrefix();
    final String namespaceValue = String.format( "xmlns(%s=%s)", prefix, namespaceUri ); //$NON-NLS-1$
    params.put( PARAM_DESCRIBE_FEATURE_TYPE_NAMESPACE, namespaceValue );

    final String typenameValue = String.format( "%s:%s", prefix, qname.getLocalName() ); //$NON-NLS-1$
    params.put( PARAM_DESCRIBE_FEATURE_TYPE_TYPENAME, typenameValue );

    return UrlUtilities.addQuery( describeUrl, params );
  }

  public IFeatureType getFeatureType( final WFSFeatureType type ) throws CoreException
  {
    try
    {
      if( m_schemaHash.containsKey( type ) )
        return m_schemaHash.get( type );

      final QualifiedName name = type.getName();
      final QName qname = new QName( name.getNamespace().toString(), name.getLocalName() );
      final URL schemaLocation = operationDescribeFeatureType( qname );

      final IGMLSchema schema = GMLSchemaFactory.createGMLSchema( "3.1.1", schemaLocation ); //$NON-NLS-1$
      final IFeatureType featureType = schema.getFeatureType( qname );

      m_schemaHash.put( type, featureType );

      return featureType;
    }
    catch( final GMLSchemaException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
  }
}