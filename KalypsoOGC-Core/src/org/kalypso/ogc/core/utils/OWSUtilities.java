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

import net.opengis.ows._1.AllowedValues;
import net.opengis.ows._1.AnyValue;
import net.opengis.ows._1.CodeType;
import net.opengis.ows._1.DomainMetadataType;
import net.opengis.ows._1.DomainType;
import net.opengis.ows._1.ExceptionReport;
import net.opengis.ows._1.ExceptionType;
import net.opengis.ows._1.MetadataType;
import net.opengis.ows._1.NoValues;
import net.opengis.ows._1.ObjectFactory;
import net.opengis.ows._1.ValueType;
import net.opengis.ows._1.ValuesReference;

import org.kalypso.commons.bind.JaxbUtilities;

/**
 * This utilities class provides functions for constructing binding objects of <code>net.opengis.ows._1</code>.
 * 
 * @author Toni DiNardo
 */
public class OWSUtilities
{
  /**
   * The version of the OWS specification.
   */
  public static final String OWS_VERSION = "1.1.0"; //$NON-NLS-1$

  /**
   * The factory for OWS objects.
   */
  private static final ObjectFactory OWS_OF = new ObjectFactory();

  /**
   * The JAXB context.
   */
  private static final JAXBContext JC = JaxbUtilities.createQuiet( ObjectFactory.class );

  /**
   * The contstructor.
   */
  private OWSUtilities( )
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
   *          The reader to unmarshal XML data from.
   * @return The newly created root object of the java content tree.
   * @see Unmarshaller#unmarshal(java.io.Reader)
   */
  public static Object unmarshall( final Reader reader ) throws JAXBException
  {
    final Unmarshaller unmarshaller = JC.createUnmarshaller();
    return unmarshaller.unmarshal( reader );
  }

  /**
   * Note: The documentation is taken from owsExceptionReport.xsd.<br />
   * <br />
   * Report message returned to the client that requested any OWS operation when the server detects an error while
   * processing that operation request.
   * 
   * @param exceptions
   *          Unordered list of one or more Exception elements that each describes an error. These Exception elements
   *          shall be interpreted by clients as being independent of one another (not hierarchical).
   * @param version
   *          Specification version for OWS operation. The string value shall contain one x.y.z "version" value (e.g.,
   *          "2.1.3"). A version number shall contain three non-negative integers separated by decimal points, in the
   *          form "x.y.z". The integers y and z shall not exceed 99. Each version shall be for the Implementation
   *          Specification (document) and the associated XML Schemas to which requested operations will conform. An
   *          Implementation Specification version normally specifies XML Schemas against which an XML encoded operation
   *          response must conform and should be validated. See Version negotiation subclause for more information.
   * @param lang
   *          [optional] Identifier of the language used by all included exception text values. These language
   *          identifiers shall be as specified in IETF RFC 4646. When this attribute is omitted, the language used is
   *          not identified.
   * @return
   */
  public static ExceptionReport buildExeptionReport( final ExceptionType[] exceptions, final String version, final String lang )
  {
    /* Create the instance via the factory. */
    final ExceptionReport exceptionReport = OWS_OF.createExceptionReport();

    /* Elements. */
    for( final ExceptionType exception : exceptions )
      exceptionReport.getException().add( exception );

    /* Attributes. */
    exceptionReport.setVersion( version );

    if( lang != null && lang.length() > 0 )
      exceptionReport.setLang( lang );

    return exceptionReport;
  }

  /**
   * Note: The documentation is taken from owsExceptionReport.xsd.<br />
   * <br />
   * An Exception element describes one detected error that a server chooses to convey to the client.
   * 
   * @param exceptionTexts
   *          Ordered sequence of text strings that describe this specific exception or error. The contents of these
   *          strings are left open to definition by each server implementation. A server is strongly encouraged to
   *          include at least one ExceptionText value, to provide more information about the detected error than
   *          provided by the exceptionCode. When included, multiple ExceptionText values shall provide hierarchical
   *          information about one detected error, with the most significant information listed first.
   * @param exceptionCode
   *          A code representing the type of this exception, which shall be selected from a set of exceptionCode values
   *          specified for the specific service operation and server.
   * @param locator
   *          [optional] When included, this locator shall indicate to the client where an exception was encountered in
   *          servicing the client's operation request. This locator should be included whenever meaningful information
   *          can be provided by the server. The contents of this locator will depend on the specific exceptionCode and
   *          OWS service, and shall be specified in the OWS Implementation Specification.
   * @return
   */
  public static ExceptionType buildExceptionType( final String[] exceptionTexts, final String exceptionCode, final String locator )
  {
    /* Create the instance via the factory. */
    final ExceptionType exception = OWS_OF.createExceptionType();

    /* Elements. */
    for( final String exceptionText : exceptionTexts )
      exception.getExceptionText().add( exceptionText );

    /* Attributes. */
    exception.setExceptionCode( exceptionCode );

    if( locator != null && locator.length() > 0 )
      exception.setLocator( locator );

    return exception;
  }

  /**
   * Note: The documentation is taken from ows1911subset.xsd.<br />
   * <br />
   * Name or code with an (optional) authority. If the codeSpace attribute is present, its value shall reference a
   * dictionary, thesaurus, or authority for the name or code, such as the organisation who assigned the value, or the
   * dictionary from which it is taken. Type copied from basicTypes.xsd of GML 3 with documentation edited, for possible
   * use outside the ServiceIdentification section of a service metadata document.
   * 
   * @param codeSpace
   *          [optional]
   * @param value
   * @return
   */
  public static CodeType buildCodeType( final String codeSpace, final String value )
  {
    /* Create the instance via the factory. */
    final CodeType code = OWS_OF.createCodeType();

    /* Attributes. */
    if( codeSpace != null )
      code.setCodeSpace( codeSpace );

    /* Values. */
    code.setValue( value );

    return code;
  }

  /**
   * Note: The documentation is taken from owsDomainType.xsd.<br />
   * <br />
   * Valid domain (or allowed set of values) of one quantity, with its name or identifier.
   * 
   * @param possibleValues
   *          [optional] Specifies the possible values of this quantity.<br />
   *          Possible are
   *          <ol>
   *          <li>AllowedValues</li>
   *          <li>AnyValue</li>
   *          <li>NoValues</li>
   *          <li>ValuesReference</li>
   *          </ol>
   * @param defaultValue
   *          [optional] Optional default value for this quantity, which should be included when this quantity has a
   *          default value.
   * @param meaning
   *          [optional] Meaning metadata should be referenced or included for each quantity.
   * @param dataType
   *          [optional] This data type metadata should be referenced or included for each quantity.
   * @param reference
   *          True, if the valuesUnit is a reference. False, if it is an UOM.
   * @param valuesUnit
   *          [optional] Unit of measure, which should be included when this set of PossibleValues has units or a more
   *          complete reference system.
   * @param metadata
   *          [optional] Optional unordered list of other metadata about this quantity. A list of required and optional
   *          other metadata elements for this quantity should be specified in the Implementation Specification for this
   *          service.
   * @param name
   *          Name or identifier of this quantity.
   * @return
   */
  public static DomainType buildDomainType( final Object possibleValues, final ValueType defaultValue, final DomainMetadataType meaning, final DomainMetadataType dataType, final boolean reference, final DomainMetadataType valuesUnit, final List<MetadataType> metadata, final String name )
  {
    /* Create the instance via the factory. */
    final DomainType domain = OWS_OF.createDomainType();

    /* Elements. */
    if( possibleValues != null )
    {
      if( possibleValues instanceof AllowedValues )
        domain.setAllowedValues( (AllowedValues) possibleValues );
      else if( possibleValues instanceof AnyValue )
        domain.setAnyValue( (AnyValue) possibleValues );
      else if( possibleValues instanceof NoValues )
        domain.setNoValues( (NoValues) possibleValues );
      else if( possibleValues instanceof ValuesReference )
        domain.setValuesReference( (ValuesReference) possibleValues );
    }

    if( defaultValue != null )
      domain.setDefaultValue( defaultValue );

    if( meaning != null )
      domain.setMeaning( meaning );

    if( dataType != null )
      domain.setDataType( dataType );

    if( valuesUnit != null )
    {
      if( reference )
        domain.setReferenceSystem( valuesUnit );
      else
        domain.setUOM( valuesUnit );
    }

    if( metadata != null )
      domain.getMetadata().addAll( metadata );

    /* Attributes. */
    domain.setName( name );

    return domain;
  }

  /**
   * Note: The documentation is taken from owsDomainType.xsd.<br />
   * <br />
   * References metadata about a quantity, and provides a name for this metadata. (Informative: This element was
   * simplified from the metaDataProperty element in GML 3.0.)
   * 
   * @param reference
   *          [optional] Reference to data or metadata recorded elsewhere, either external to this XML document or
   *          within it. Whenever practical, this attribute should be a URL from which this metadata can be
   *          electronically retrieved. Alternately, this attribute can reference a URN for well-known metadata. For
   *          example, such a URN could be a URN defined in the "ogc" URN namespace.
   * @param value
   *          Human-readable name of the metadata described by associated referenced document.
   * @return
   */
  public static DomainMetadataType buildDomainMetadataType( final String reference, final String value )
  {
    /* Create the instance via the factory. */
    final DomainMetadataType domainMetadata = OWS_OF.createDomainMetadataType();

    /* Attributes. */
    if( reference != null )
      domainMetadata.setReference( reference );

    /* Values. */
    domainMetadata.setValue( value );

    return domainMetadata;
  }
}