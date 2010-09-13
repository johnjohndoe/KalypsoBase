/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.zml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.commons.factory.FactoryException;
import org.kalypso.commons.java.util.PropertiesHelper;
import org.kalypso.commons.java.util.StringUtilities;
import org.kalypso.commons.parser.IParser;
import org.kalypso.commons.parser.ParserException;
import org.kalypso.commons.parser.ParserFactory;
import org.kalypso.commons.parser.impl.DateParser;
import org.kalypso.commons.xml.XmlTypes;
import org.kalypso.contribs.java.xml.XMLUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.FilterFactory;
import org.kalypso.ogc.sensor.impl.DefaultAxis;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.metadata.IObservationConstants;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.proxy.AutoProxyFactory;
import org.kalypso.ogc.sensor.proxy.RequestObservationProxy;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.request.RequestFactory;
import org.kalypso.ogc.sensor.zml.values.IZmlValues;
import org.kalypso.ogc.sensor.zml.values.ZmlArrayValues;
import org.kalypso.ogc.sensor.zml.values.ZmlLinkValues;
import org.kalypso.ogc.sensor.zml.values.ZmlTupleModel;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.repository.RepositoryException;
import org.kalypso.repository.utils.RepositoryUtils;
import org.kalypso.zml.AxisType;
import org.kalypso.zml.AxisType.ValueArray;
import org.kalypso.zml.AxisType.ValueLink;
import org.kalypso.zml.MetadataListType;
import org.kalypso.zml.MetadataType;
import org.kalypso.zml.ObjectFactory;
import org.kalypso.zml.Observation;
import org.kalypso.zml.request.Request;
import org.xml.sax.InputSource;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Factory for ZML-Files. ZML is a flexible format that covers following possibilities:
 * <ul>
 * <li>inlined: values are stored as array of items in each axis definition
 * <li>linked: values are stored in an external CSV-like file
 * <li>block-inlined: values are stored CSV-like, but in the zml file itself
 * </ul>
 * The block-inlined Format is used with valueLink elements and if the Href-Attribute is not specified, is empty, or
 * contains "#data".
 * 
 * @author schlienger
 */
public final class ZmlFactory
{
  public static final NamespacePrefixMapper ZML_PREFIX_MAPPER = new ZmlNamespacePrefixMapper();

  public static final ObjectFactory OF = new ObjectFactory();

  public static final JAXBContext JC = JaxbUtilities.createQuiet( org.kalypso.zml.filters.ObjectFactory.class, org.kalypso.wechmann.ObjectFactory.class, org.kalypso.zml.filters.valuecomp.ObjectFactory.class, org.kalypso.zml.ObjectFactory.class, org.w3._1999.xlinkext.ObjectFactory.class );

  private static ParserFactory PARSER_FACTORY = null;

  private static Properties PARSER_PROPERTIES = null;

  private static Logger LOG = Logger.getLogger( ZmlFactory.class.getName() );

  private static final Comparator<MetadataType> METADATA_COMPERATOR = new Comparator<MetadataType>()
  {
    @Override
    public int compare( final MetadataType o1, final MetadataType o2 )
    {
      return o1.getName().compareTo( o2.getName() );
    }
  };

  private ZmlFactory( )
  {
    // not to be instanciated
  }

  private static synchronized Properties getProperties( )
  {
    if( PARSER_PROPERTIES == null )
    {
      InputStream ins = null;

      try
      {
        PARSER_PROPERTIES = new Properties();

        ins = ZmlFactory.class.getResourceAsStream( "resource/types2parser.properties" ); //$NON-NLS-1$

        PARSER_PROPERTIES.load( ins );

        return PARSER_PROPERTIES;
      }
      catch( final IOException e )
      {
        throw new RuntimeException( e );
      }
      finally
      {
        IOUtils.closeQuietly( ins );
      }
    }

    return PARSER_PROPERTIES;
  }

  /**
   * Helper, man sollte es benutzen um auf die ParserFactory zugreifen zu können
   * 
   * @return parser factory
   */
  private static synchronized ParserFactory getParserFactory( )
  {
    if( PARSER_FACTORY == null )
      PARSER_FACTORY = new ParserFactory( getProperties(), ZmlFactory.class.getClassLoader() );

    return PARSER_FACTORY;
  }

  /**
   * Supported types are listed in the types2parser.properties file. TODO: noch das default format (_format) hinzufügen
   * und eventuell die xs: Zeugs wegmachen Siehe properties datei
   * 
   * @return the XSD-Type for the given Java-Class
   */
  private static String getXSDTypeFor( final String className )
  {
    return getProperties().getProperty( className );
  }

  /**
   * Parses the XML and creates an IObservation object.
   * 
   * @see ZmlFactory#parseXML(InputSource, String, URL)
   * @param url
   *          the url specification of the zml
   * @param identifier
   *          [optional] ID für Repository
   * @return IObservation object
   * @throws SensorException
   *           in case of parsing or creation problem
   */
  public static IObservation parseXML( final URL url ) throws SensorException
  {
    final IObservation observation = fetchObservationFromRegisteredRepository( url );
    if( observation != null )
      return decorateObservation( observation, url.toExternalForm(), url );

    InputStream inputStream = null;

    try
    {
      final String zmlId = ZmlURL.getIdentifierPart( url );

      if( ZmlURL.isUseAsContext( url ) )
      {
        /*
         * if there is a fragment called "useascontext" then we are dealing with a special kind of zml-url: the scheme
         * denotes solely a context, the observation is strictly built using the query part and the context.
         */

        // create the real context
        final URL context = new URL( zmlId );

        // directly return the observation
        return decorateObservation( null, url.toExternalForm(), context );
      }

      final String scheme = ZmlURL.getSchemePart( url );
      if( scheme.startsWith( "file" ) || scheme.startsWith( "platform" ) || scheme.startsWith( "jar" ) || scheme.startsWith( "bundleresource" ) ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      {
        /*
         * if this is a local url, we remove the query part because Eclipse Platform's URLStreamHandler cannot deal with
         * it.
         */

        // only take the simple part of the url
        final URL tmpUrl = new URL( zmlId );

        // stream is closed in finally
        inputStream = tmpUrl.openStream();
      }
      else
      {
        // default behaviour (might use a specific stream handler like
        // the OCSUrlStreamHandler )
        inputStream = url.openStream();
      }

      inputStream = new BufferedInputStream( inputStream );

      // url is given as an argument here (and not tmpUrl) in order not to
      // loose the query part we might have removed because of Eclipse's
      // url handling.
      return parseXML( new InputSource( inputStream ), url );
    }
    catch( final IOException e )
    {
      throw new SensorException( Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.5" ) + url.toExternalForm(), e ); //$NON-NLS-1$
    }
    finally
    {
      IOUtils.closeQuietly( inputStream );
    }
  }

  private static IObservation fetchObservationFromRegisteredRepository( final URL url ) throws SensorException
  {
    try
    {
      final String urlBase = url.toExternalForm();
      if( ZmlURL.isEmpty( urlBase ) )
        return RequestFactory.createDefaultObservation( urlBase );

      final IRepository registeredRepository = RepositoryUtils.findRegisteredRepository( url.toExternalForm() );
      if( registeredRepository == null )
        return null;

      final String[] splittedUrlBase = urlBase.split( "\\?" ); //$NON-NLS-1$
      if( splittedUrlBase.length > 2 )
        throw new IllegalStateException( String.format( "Unknown URL format. Format = zml-proxy://itemId?parameter. Given %s", urlBase ) ); //$NON-NLS-1$

      final String itemId = splittedUrlBase[0];

      return fetchZmlFromRepository( registeredRepository, itemId );
    }
    catch( final SensorException e )
    {
      throw e;
    }
    catch( final Exception ex )
    {
      throw new SensorException( "Parsing zml-proxy observation failed.", ex ); //$NON-NLS-1$
    }
  }

  private static IObservation fetchZmlFromRepository( final IRepository repository, final String itemId ) throws RepositoryException
  {
    final IRepositoryItem item = repository.findItem( itemId );
    if( item == null )
      throw new RepositoryException( String.format( "Unknown ID: %s", itemId ) );

    return (IObservation) item.getAdapter( IObservation.class );
  }

  /**
   * Parse the XML and create an IObservation instance.
   * 
   * @param source
   *          contains the zml
   * @param context
   *          [optional] the context of the source in order to resolve relative url
   */
  public static IObservation parseXML( final InputSource source, final URL context ) throws SensorException
  {
    try
    {
      final Unmarshaller u = JC.createUnmarshaller();
      final Observation obs = (Observation) u.unmarshal( source );
      return binding2Obs( obs, context );
    }
    catch( final JAXBException e )
    {
      throw new SensorException( e );
    }
  }

  private static IObservation binding2Obs( final Observation obs, final URL context ) throws SensorException
  {
    // metadata
    final MetadataList metadata = new MetadataList();
    metadata.put( IObservationConstants.MD_NAME, obs.getName() );
    TimeZone timeZone = null;
    if( obs.getMetadataList() != null )
    {
      final List<MetadataType> mdList = obs.getMetadataList().getMetadata();
      for( final MetadataType md : mdList )
      {
        final String value;
        if( md.getValue() != null )
          value = md.getValue();
        else if( md.getData() != null )
          value = md.getData().replaceAll( XMLUtilities.CDATA_BEGIN_REGEX, "" ).replaceAll( XMLUtilities.CDATA_END_REGEX, "" ); //$NON-NLS-1$ //$NON-NLS-2$
        else
          value = ""; //$NON-NLS-1$
        if( md.getName().equals( ITimeseriesConstants.MD_TIMEZONE ) && md.getValue().length() > 0 )
          timeZone = TimeZone.getTimeZone( md.getValue() );
        metadata.put( md.getName(), value );
      }
    }

    // axes and values
    final List<AxisType> tmpList = obs.getAxis();
    final Map<IAxis, IZmlValues> valuesMap = new HashMap<IAxis, IZmlValues>( tmpList.size() );

    final String data = obs.getData(); // data is optional and can be null

    for( int i = 0; i < tmpList.size(); i++ )
    {
      final AxisType tmpAxis = tmpList.get( i );

      final Properties props = PropertiesHelper.parseFromString( tmpAxis.getDatatype(), '#' );
      final String type = props.getProperty( "TYPE" ); //$NON-NLS-1$
      String format = props.getProperty( "FORMAT" ); //$NON-NLS-1$

      final IParser parser;
      final IZmlValues values;
      try
      {
        // if format not specified, then we use the default specification
        // found in the properties file. Every type can have a default format
        // declared in this file using the convention that the property
        // must be build using the type name followed by the '_format' string.
        if( format == null || format == "" ) //$NON-NLS-1$
          format = getProperties().getProperty( type + "_format" ); //$NON-NLS-1$

        parser = getParserFactory().createParser( type, format );

        // if we have a date parser, set the right timezone to read the values
        if( parser instanceof DateParser )
        {
          if( timeZone != null )
            ((DateParser) parser).setTimezone( timeZone );

          final String tzString = metadata.getProperty( ITimeseriesConstants.MD_TIMEZONE, "UTC" ); //$NON-NLS-1$
          ((DateParser) parser).setTimezone( TimeZone.getTimeZone( tzString ) );
        }

        values = createValues( context, tmpAxis, parser, data );
      }
      catch( final Exception e ) // generic exception caught for simplicity
      {
        throw new SensorException( e );
      }

      final IAxis axis = new DefaultAxis( tmpAxis.getName(), tmpAxis.getType(), tmpAxis.getUnit(), parser.getObjectClass(), tmpAxis.isKey() );

      valuesMap.put( axis, values );
    }

    final ZmlTupleModel model = new ZmlTupleModel( valuesMap );

    final String contextHref = context != null ? context.toExternalForm() : ""; //$NON-NLS-1$
    final SimpleObservation zmlObs = new SimpleObservation( contextHref, obs.getName(), metadata, model );

    return decorateObservation( zmlObs, contextHref, context );
  }

  /**
   * Central method for decorating the observation according to its context and identifier. It internally checks for:
   * <ol>
   * <li>a filter specification (for example: interpolation filter)
   * <li>a proxy specification (for example: from-to)
   * <li>an auto-proxy possibility (for example: WQ-Metadata)
   * </ol>
   */
  public static IObservation decorateObservation( final IObservation observation, final String href, final URL context ) throws SensorException
  {
    // tricky: maybe make a filtered observation out of this one
    final IObservation filteredObs = FilterFactory.createFilterFrom( href, observation, context );

    // tricky: check if a proxy has been specified in the url
    final IObservation proxyObs = createProxyFrom( href, filteredObs );

    return AutoProxyFactory.getInstance().proxyObservation( proxyObs );
  }

  /**
   * Helper: may create a proxy observation depending on the information coded in the url.
   * 
   * @return proxy or original observation
   */
  private static IObservation createProxyFrom( final String href, final IObservation baseObs ) throws SensorException
  {
    if( href == null || href.length() == 0 )
      return baseObs;

    // check if a request based proxy can be created
    final Request requestType = RequestFactory.parseRequest( href );
    if( requestType != null )
      return new RequestObservationProxy( ObservationRequest.createWith( requestType ), baseObs );

    return baseObs;
  }

  /**
   * Parses the values and create the corresponding objects.
   * 
   * @param context
   *          context into which the original file exists
   * @param axisType
   *          binding object for axis
   * @param parser
   *          configured parser enabled for parsing the values according to axis spec
   * @param data
   *          [optional] contains the data-block if observation is block-inline
   * @return corresponding values depending on value axis type
   * @throws ParserException
   * @throws MalformedURLException
   * @throws IOException
   */
  private static IZmlValues createValues( final URL context, final AxisType axisType, final IParser parser, final String data ) throws ParserException, IOException
  {
    final ValueArray va = axisType.getValueArray();
    if( va != null )
      return new ZmlArrayValues( va, parser );

    // loader for linked values, here we specify where base location is
    final ValueLink vl = axisType.getValueLink();
    if( vl != null )
      return new ZmlLinkValues( vl, parser, context, data );

    throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.14" ) + axisType.toString() ); //$NON-NLS-1$
  }

  /**
   * Create an XML-Observation ready for marshalling.
   * 
   * @param timezone
   *          the timezone into which dates should be converted before serialized
   * @deprecated Use one of the writeXXX methods.
   */
  @Deprecated
  public static Observation createXML( final IObservation obs, final IRequest args ) throws FactoryException
  {
    try
    {
      // first of all fetch values
      final ITupleModel values = obs.getValues( args );

      final Observation obsType = OF.createObservation();
      obsType.setName( obs.getName() );

      final MetadataListType metadataListType = OF.createMetadataListType();
      obsType.setMetadataList( metadataListType );
      final List<MetadataType> metadataList = metadataListType.getMetadata();

      String metaName = null;
      final MetadataList obsMetadataList = obs.getMetadataList();
      for( final Entry<Object, Object> entry : obsMetadataList.entrySet() )
      {
        final String mdKey = (String) entry.getKey();
        final String mdValue = (String) entry.getValue();

        final MetadataType mdType = OF.createMetadataType();
        mdType.setName( mdKey );

        // TRICKY: if this looks like an xml-string then pack it
        // into a CDATA section and use the 'data'-Element instead
        if( mdValue.startsWith( XMLUtilities.XML_HEADER_BEGIN ) )
          mdType.setData( XMLUtilities.encapsulateInCDATA( mdValue ) );
        else
          mdType.setValue( mdValue );

        if( IObservationConstants.MD_NAME.equals( mdKey ) )
          metaName = mdValue;

        metadataList.add( mdType );
      }

      Collections.sort( metadataList, METADATA_COMPERATOR );

      final TimeZone timezone = KalypsoCorePlugin.getDefault().getTimeZone();

      // write timezone info into metadata
      final MetadataType mdType = OF.createMetadataType();
      mdType.setName( ITimeseriesConstants.MD_TIMEZONE );
      mdType.setValue( timezone.getID() );
      // Check, if value already exists and remove first
      for( final Iterator<MetadataType> iterator = metadataList.iterator(); iterator.hasNext(); )
      {
        if( iterator.next().getName().equals( ITimeseriesConstants.MD_TIMEZONE ) )
          iterator.remove();
      }
      metadataList.add( mdType );

      final List<AxisType> axisList = obsType.getAxis();
      // sort axes, this is not needed from a xml view, but very usefull when comparing marshalled files (e.g.
      // Junit-Test)
      final TreeSet<IAxis> sortedAxis = new TreeSet<IAxis>( new Comparator<IAxis>()
          {
        @Override
        public int compare( final IAxis a1, final IAxis a2 )
        {
          String type1 = a1.getType();
          String type2 = a2.getType();
          if( type1 == null )
            type1 = ""; //$NON-NLS-1$
          if( type2 == null )
            type2 = ""; //$NON-NLS-1$
          if( type1.equals( type2 ) )
          {
            String n1 = a1.getName();
            String n2 = a2.getName();
            if( n1 == null )
              n1 = ""; //$NON-NLS-1$
            if( n2 == null )
              n2 = ""; //$NON-NLS-1$
            return n1.compareTo( n2 );
          }
          return type1.compareTo( type2 );
        }
          } );

      for( final IAxis axis : obs.getAxisList() )
      {
        sortedAxis.add( axis );
      }

      for( final IAxis axis : sortedAxis )
      {
        if( axis.isPersistable() )
        {
          final AxisType axisType = OF.createAxisType();

          final String xsdType = getXSDTypeFor( axis.getDataClass().getName() );

          axisType.setDatatype( xsdType );
          axisType.setName( axis.getName() );
          axisType.setUnit( axis.getUnit() );
          axisType.setType( axis.getType() );
          axisType.setKey( axis.isKey() );

          final ValueArray valueArrayType = OF.createAxisTypeValueArray();

          valueArrayType.setSeparator( ";" ); //$NON-NLS-1$
          valueArrayType.setValue( buildValueString( values, axis, timezone ) );

          axisType.setValueArray( valueArrayType );

          axisList.add( axisType );
        }
      }

      if( metaName != null )
        obsType.setName( metaName );

      return obsType;
    }
    catch( final Exception e )
    {
      throw new FactoryException( e );
    }
  }

  /**
   * @return string that contains the serialized values
   */
  private static String buildValueString( final ITupleModel model, final IAxis axis, final TimeZone timezone ) throws SensorException
  {
    if( model.getCount() == 0 )
      return "";

    if( java.util.Date.class.isAssignableFrom( axis.getDataClass() ) )
      return buildStringDateAxis( model, axis, timezone );
    else if( Number.class.isAssignableFrom( axis.getDataClass() ) || Boolean.class.isAssignableFrom( axis.getDataClass() ) )
      return buildStringNumberAxis( model, axis );
    else if( String.class.isAssignableFrom( axis.getDataClass() ) )
      return buildStringAxis( model, axis );
    else
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.21" ) ); //$NON-NLS-1$
  }

  private static String buildStringAxis( final ITupleModel model, final IAxis axis ) throws SensorException
  {
    final StringBuffer buffer = new StringBuffer();
    for( int i = 0; i < model.getCount(); i++ )
    {
      buffer.append( model.getElement( i, axis ) ).append( ";" ); //$NON-NLS-1$
    }

    return StringUtilities.chomp( buffer.toString() );
  }

  private static String buildStringDateAxis( final ITupleModel model, final IAxis axis, final TimeZone timezone ) throws SensorException
  {
    final StringBuffer buffer = new StringBuffer();
    final DateParser dateParser = XmlTypes.getDateParser( timezone );

    for( int i = 0; i < model.getCount(); i++ )
    {
      buffer.append( dateParser.toString( model.getElement( i, axis ) ) ).append( ";" ); //$NON-NLS-1$
    }

    return StringUtilities.chomp( buffer.toString() );
  }

  /**
   * Uses the default toString() method of the elements
   */
  private static String buildStringNumberAxis( final ITupleModel model, final IAxis axis ) throws SensorException
  {
    final StringBuffer buffer = new StringBuffer();

    for( int i = 0; i < model.getCount(); i++ )
    {
      final Object elt = model.getElement( i, axis );
      if( elt == null )
        LOG.warning( Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.24" ) + i + Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.25" ) + axis ); //$NON-NLS-1$ //$NON-NLS-2$

      buffer.append( elt ).append( ";" ); //$NON-NLS-1$
    }

    return StringUtilities.chomp( buffer.toString() );
  }

  /**
   * @deprecated Do not use any more, except from this class.<br>
   *             Introduce and use helper methods in this class instead (for streams, files, etc.). We should especially
   *             use an {@link javax.xml.stream.XMLStreamWriter} configured with standard namespaces to write zml.
   */
  @Deprecated
  public static Marshaller getMarshaller( ) throws JAXBException
  {
    final Marshaller marshaller = JaxbUtilities.createMarshaller( JC );
    marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    marshaller.setProperty( "com.sun.xml.bind.namespacePrefixMapper", ZML_PREFIX_MAPPER );
    return marshaller;
  }

  /**
   * @return valid parser for the given axis
   */
  public static IParser createParser( final IAxis axis ) throws FactoryException
  {
    final ParserFactory pf = getParserFactory();

    return pf.createParser( "JAVA_" + axis.getDataClass().getName(), null ); //$NON-NLS-1$
  }

  /**
   * Helper method for simply writing the observation to an IFile
   * 
   * @throws SensorException
   *           if an IOException or a FactoryException is thrown internally
   */
  public static void writeToFile( final IObservation obs, final IFile file ) throws SensorException, CoreException
  {
    writeToFile( obs, file.getLocation().toFile(), null );
    file.refreshLocal( IResource.DEPTH_ONE, new NullProgressMonitor() );
  }

  /**
   * Helper method for simply writing the observation to a file
   * 
   * @throws SensorException
   *           if an IOException or a FactoryException is thrown internally
   */
  public static void writeToFile( final IObservation obs, final File file ) throws SensorException
  {
    writeToFile( obs, file, null );
  }

  /**
   * Helper method for simply writing the observation to a file
   * 
   * @param request
   *          If non-<code>null</code>, this request will be applied to the access to the values of the given
   *          observation.
   * @throws SensorException
   *           if an IOException or a FactoryException is thrown internally
   */
  public static void writeToFile( final IObservation obs, final File file, final IRequest request ) throws SensorException
  {
    OutputStream outs = null;
    try
    {
      outs = new BufferedOutputStream( new FileOutputStream( file ) );

      writeToStream( obs, outs, request );

      outs.close();
    }
    catch( final IOException e )
    {
      LOG.log( Level.WARNING, Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.30" ), e ); //$NON-NLS-1$

      throw new SensorException( e );
    }
    finally
    {
      IOUtils.closeQuietly( outs );
    }
  }

  /**
   * Writes an {@link IObservation} as zml into the given stream. The stream WILL NOT be closed after this operations,
   * this is the responsibility of the caller.
   */
  public static void writeToStream( final IObservation obs, final OutputStream os, final IRequest request ) throws SensorException
  {
    try
    {
      final Observation xml = createXML( obs, request );
      final Marshaller marshaller = getMarshaller();
      marshaller.marshal( xml, os );
    }
    catch( final JAXBException e )
    {
      LOG.log( Level.WARNING, Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.30" ), e ); //$NON-NLS-1$

      throw new SensorException( e );
    }
    catch( final FactoryException e )
    {
      LOG.log( Level.WARNING, Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.30" ), e ); //$NON-NLS-1$

      throw new SensorException( e );
    }

  }

  public static String writeToString( final IObservation value, final IRequest request )
  {
    try
    {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ZmlFactory.writeToStream( value, bos, request );
      bos.close();
      return new String( bos.toByteArray(), "UTF-8" );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return e.toString();
    }
  }

}