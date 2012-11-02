/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.ogc.core.utils;

import java.io.Reader;
import java.io.Writer;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.opengis.wps._1_0.ObjectFactory;
import net.opengis.wps._1_0.SupportedCRSsType;
import net.opengis.wps._1_0.SupportedCRSsType.Default;

import org.kalypso.commons.bind.JaxbUtilities;

/**
 * This utilities class provides functions for constructing binding objects of <code>net.opengis.wps._1_0</code>.
 * 
 * @author Toni DiNardo
 */
public class WPSUtilities
{
  /**
   * The version of the WPS specification.
   */
  public static final String WPS_VERSION = "1.0.0"; //$NON-NLS-1$

  /**
   * The name of the WPS service.
   */
  public static final String WPS_SERVICE = "WPS"; //$NON-NLS-1$

  /**
   * The factory for WPS objects.
   */
  private static final ObjectFactory WPS_OF = new ObjectFactory();

  /**
   * The JAXB context.
   */
  private static final JAXBContext JC = JaxbUtilities.createQuiet( ObjectFactory.class );

  /**
   * The contstructor.
   */
  private WPSUtilities( )
  {
  }

  /**
   * Marshal the content tree rooted at jaxbElement into a Writer.
   * 
   * @param jaxbElement
   *          The root of content tree to be marshalled.
   * @param writer
   *          XML will be sent to this writer.
   * @see Marshaller#marshal(Object, Writer)
   */
  public static void marshal( final Object jaxbElement, final Writer writer ) throws JAXBException
  {
    final Marshaller marshaller = JC.createMarshaller();
    marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    marshaller.marshal( jaxbElement, writer );
  }

  /**
   * Unmarshal XML data from the specified Reader and return the resulting content tree. Validation event location
   * information may be incomplete when using this form of the unmarshal API, because a Reader does not provide the
   * system ID.
   * 
   * @param reader
   *          The Reader to unmarshal XML data from.
   * @return The newly created root object of the java content tree.
   * @see Unmarshaller#unmarshal(java.io.Reader)
   */
  public static Object unmarshall( final Reader reader ) throws JAXBException
  {
    final Unmarshaller unmarshaller = JC.createUnmarshaller();
    return unmarshaller.unmarshal( reader );
  }

  /**
   * Note: The documentation is taken from wpsDescribeProcess_response.xsd.<br />
   * <br />
   * Listing of the Coordinate Reference System (CRS) support for this process input or output.
   * 
   * @param default Identifies the default CRS that will be used unless the Execute operation request specifies another
   *        supported CRS.
   * @param supported
   *          Unordered list of references to all of the CRSs supported for this Input/Output. The default CRS shall be
   *          included in this list.
   * @return
   */
  public static SupportedCRSsType buildSupportedCRSsType( final Default defaultValue, final List<String> supported )
  {
    /* Create the instance via the factory. */
    final SupportedCRSsType supportedCRSs = WPS_OF.createSupportedCRSsType();

    /* Elements. */
    supportedCRSs.setDefault( defaultValue );
    supportedCRSs.getSupported().getCRS().addAll( supported );

    return supportedCRSs;
  }
}