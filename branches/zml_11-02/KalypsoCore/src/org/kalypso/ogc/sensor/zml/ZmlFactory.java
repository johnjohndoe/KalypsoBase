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

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;

import javax.xml.bind.JAXBContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.commons.factory.FactoryException;
import org.kalypso.commons.parser.IParser;
import org.kalypso.commons.parser.ParserFactory;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.FilterFactory;
import org.kalypso.ogc.sensor.proxy.AutoProxyFactory;
import org.kalypso.ogc.sensor.proxy.RequestObservationProxy;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.request.RequestFactory;
import org.kalypso.zml.request.Request;
import org.xml.sax.InputSource;

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
  public static final JAXBContext JC = JaxbUtilities.createQuiet( org.kalypso.zml.filters.ObjectFactory.class, org.kalypso.wechmann.ObjectFactory.class, org.kalypso.zml.filters.valuecomp.ObjectFactory.class, org.kalypso.zml.ObjectFactory.class, org.w3._1999.xlinkext.ObjectFactory.class );

  private ZmlFactory( )
  {
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
  // TODO: bad method name
  public static IObservation parseXML( final URL url ) throws SensorException
  {
    final IObservation observation = readZml( url );
    final String href = url.toExternalForm();
    return decorateObservation( observation, href, url );
  }

  private static IObservation readZml( final URL url ) throws SensorException
  {
    final IObservation observation = ObservationRepositoryFetcher.loadObservation( url );
    if( observation != null )
      return observation;

    return ObservationStreamFetcher.loadObservation( url );
  }

  /**
   * FIXME: check: why do we have two parse methods? There should be only one access point...! <br/>
   * Parse the XML and create an IObservation instance.
   * 
   * @param source
   *          contains the zml
   * @param context
   *          [optional] the context of the source in order to resolve relative url
   */
  public static IObservation parseXML( final InputSource source, final URL context ) throws SensorException
  {
    final ObservationUnmarshaller unmarshaller = new ObservationUnmarshaller( source, context );
    final IObservation observation = unmarshaller.unmarshall();

    final String href = observation.getHref();
    return decorateObservation( observation, href, context );
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

    return AutoProxyFactory.proxyObservation( proxyObs );
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
   * @return valid parser for the given axis
   */
  public static IParser createParser( final IAxis axis ) throws FactoryException
  {
    final ParserFactory pf = ZmlParserFactory.getParserFactory();

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
    writeToFile( obs, file, null );
  }

  /**
   * Helper method for simply writing the observation to an IFile
   * 
   * @throws SensorException
   *           if an IOException or a FactoryException is thrown internally
   */
  public static void writeToFile( final IObservation obs, final IFile file, final IRequest request ) throws SensorException, CoreException
  {
    writeToFile( obs, file.getLocation().toFile(), request );
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
    final ObservationMarshaller marshaller = new ObservationMarshaller( obs, request );
    marshaller.marshall( file );
  }

  /**
   * Writes an {@link IObservation} as zml into the given stream. The stream WILL NOT be closed after this operations,
   * this is the responsibility of the caller.
   */
  public static void writeToStream( final IObservation obs, final OutputStream os, final IRequest request ) throws SensorException
  {
    final ObservationMarshaller marshaller = new ObservationMarshaller( obs, request );
    marshaller.marshall( os );
  }

  public static void writeToWriter( final IObservation obs, final Writer writer, final IRequest request ) throws SensorException
  {
    final ObservationMarshaller marshaller = new ObservationMarshaller( obs, request );
    marshaller.marshall( writer );
  }

  public static String writeToString( final IObservation value, final IRequest request )
  {
    try
    {
      final ObservationMarshaller marshaller = new ObservationMarshaller( value, request );
      return marshaller.asString();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return e.toString();
    }
  }
}