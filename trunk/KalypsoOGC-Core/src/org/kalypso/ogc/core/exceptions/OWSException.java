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
package org.kalypso.ogc.core.exceptions;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.opengis.ows._1.ExceptionReport;
import net.opengis.ows._1.ExceptionType;

import org.apache.commons.io.IOUtils;
import org.kalypso.ogc.core.utils.OWSUtilities;

/**
 * This exception can generate an OWS error xml.
 *
 * @author Toni DiNardo
 */
public class OWSException extends Exception
{
  /**
   * Specification version for OWS operation. The string value shall contain one x.y.z "version" value (e.g., "2.1.3").
   * A version number shall contain three non-negative integers separated by decimal points, in the form "x.y.z". The
   * integers y and z shall not exceed 99. Each version shall be for the Implementation Specification (document) and the
   * associated XML Schemas to which requested operations will conform. An Implementation Specification version normally
   * specifies XML Schemas against which an XML encoded operation response must conform and should be validated. See
   * Version negotiation subclause for more information.
   */
  private String m_version;

  /**
   * [optional] Identifier of the language used by all included exception text values. These language identifiers shall
   * be as specified in IETF RFC 4646. When this attribute is omitted, the language used is not identified.
   */
  private String m_lang;

  /**
   * A code representing the type of this exception, which shall be selected from a set of exceptionCode values
   * specified for the specific service operation and server.
   */
  private ExceptionCode m_exceptionCode;

  /**
   * [optional] When included, this locator shall indicate to the client where an exception was encountered in servicing
   * the client's operation request. This locator should be included whenever meaningful information can be provided by
   * the server. The contents of this locator will depend on the specific exceptionCode and OWS service, and shall be
   * specified in the OWS Implementation Specification.
   */
  private String m_locator;

  /**
   * The constructor.
   *
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
   * @param exceptionCode
   *          A code representing the type of this exception, which shall be selected from a set of exceptionCode values
   *          specified for the specific service operation and server.
   * @param locator
   *          [optional] When included, this locator shall indicate to the client where an exception was encountered in
   *          servicing the client's operation request. This locator should be included whenever meaningful information
   *          can be provided by the server. The contents of this locator will depend on the specific exceptionCode and
   *          OWS service, and shall be specified in the OWS Implementation Specification.
   */
  public OWSException( final String version, final String lang, final ExceptionCode exceptionCode, final String locator )
  {
    /* Initialize the OWS exception. */
    init( version, lang, exceptionCode, locator );
  }

  /**
   * The constructor.
   *
   * @param message
   *          The detail message. The detail message is saved for later retrieval by the getMessage() method.
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
   * @param exceptionCode
   *          A code representing the type of this exception, which shall be selected from a set of exceptionCode values
   *          specified for the specific service operation and server.
   * @param locator
   *          [optional] When included, this locator shall indicate to the client where an exception was encountered in
   *          servicing the client's operation request. This locator should be included whenever meaningful information
   *          can be provided by the server. The contents of this locator will depend on the specific exceptionCode and
   *          OWS service, and shall be specified in the OWS Implementation Specification.
   */
  public OWSException( final String message, final String version, final String lang, final ExceptionCode exceptionCode, final String locator )
  {
    super( message );

    /* Initialize the OWS exception. */
    init( version, lang, exceptionCode, locator );
  }

  /**
   * The constructor.
   *
   * @param cause
   *          The cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
   *          indicates that the cause is nonexistent or unknown.)
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
   * @param exceptionCode
   *          A code representing the type of this exception, which shall be selected from a set of exceptionCode values
   *          specified for the specific service operation and server.
   * @param locator
   *          [optional] When included, this locator shall indicate to the client where an exception was encountered in
   *          servicing the client's operation request. This locator should be included whenever meaningful information
   *          can be provided by the server. The contents of this locator will depend on the specific exceptionCode and
   *          OWS service, and shall be specified in the OWS Implementation Specification.
   */
  public OWSException( final Throwable cause, final String version, final String lang, final ExceptionCode exceptionCode, final String locator )
  {
    super( cause );

    /* Initialize the OWS exception. */
    init( version, lang, exceptionCode, locator );
  }

  /**
   * The constructor.
   *
   * @param message
   *          The detail message. The detail message is saved for later retrieval by the getMessage() method.
   * @param cause
   *          The cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
   *          indicates that the cause is nonexistent or unknown.)
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
   * @param exceptionCode
   *          A code representing the type of this exception, which shall be selected from a set of exceptionCode values
   *          specified for the specific service operation and server.
   * @param locator
   *          [optional] When included, this locator shall indicate to the client where an exception was encountered in
   *          servicing the client's operation request. This locator should be included whenever meaningful information
   *          can be provided by the server. The contents of this locator will depend on the specific exceptionCode and
   *          OWS service, and shall be specified in the OWS Implementation Specification.
   */
  public OWSException( final String message, final Throwable cause, final String version, final String lang, final ExceptionCode exceptionCode, final String locator )
  {
    super( message, cause );

    /* Initialize the OWS exception. */
    init( version, lang, exceptionCode, locator );
  }

  /**
   * This function serializes the OWS exception into XML form.
   *
   * @return The OWS exception in XML form.
   */
  public String toXML( )
  {
    /* The string writer. */
    StringWriter writer = null;

    try
    {
      /* Create the string writer. */
      writer = new StringWriter();

      /* Retrieve the exception types. */
      final ExceptionType[] exceptions = getExceptionTypes();

      /* Build the exception report. */
      final ExceptionReport exeptionReport = OWSUtilities.buildExeptionReport( exceptions, m_version, m_lang );

      /* Marshall into the string writer. */
      OWSUtilities.marshal( exeptionReport, writer );

      /* Return the marshalled OWS exception as XML. */
      return writer.toString();
    }
    catch( final JAXBException ex )
    {
      /* Ignore this exception. */
      ex.printStackTrace();

      /* There was an error marshalling the OWS exception into a XML. */
      /* We can only return the exception text as fallback. */
      return getLocalizedMessage();
    }
    finally
    {
      /* Close the string writer. */
      IOUtils.closeQuietly( writer );
    }
  }

  /**
   * This function returns the version.
   *
   * @return The version.
   */
  public String getVersion( )
  {
    return m_version;
  }

  /**
   * This function returns the language.
   *
   * @return The language.
   */
  public String getLang( )
  {
    return m_lang;
  }

  /**
   * This function returns the exception code.
   *
   * @return The exception code.
   */
  public ExceptionCode getExceptionCode( )
  {
    return m_exceptionCode;
  }

  /**
   * This function returns the locator.
   *
   * @return The locator.
   */
  public String getLocator( )
  {
    return m_locator;
  }

  /**
   * This function initializes the OWS exception.
   *
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
   * @param exceptionCode
   *          A code representing the type of this exception, which shall be selected from a set of exceptionCode values
   *          specified for the specific service operation and server.
   * @param locator
   *          [optional] When included, this locator shall indicate to the client where an exception was encountered in
   *          servicing the client's operation request. This locator should be included whenever meaningful information
   *          can be provided by the server. The contents of this locator will depend on the specific exceptionCode and
   *          OWS service, and shall be specified in the OWS Implementation Specification.
   */
  private void init( final String version, final String lang, final ExceptionCode exceptionCode, final String locator )
  {
    m_version = version;
    m_lang = lang;
    m_exceptionCode = exceptionCode;
    m_locator = locator;
  }

  /**
   * This function returns the exception types.
   *
   * @return The exception types.
   */
  private ExceptionType[] getExceptionTypes( )
  {
    /* Memory for the exception types. */
    final List<ExceptionType> exceptions = new ArrayList<>();

    /* Collect the exception types recursively. */
    collectExceptionTypes( this, exceptions );

    return exceptions.toArray( new ExceptionType[] {} );
  }

  /**
   * This function collects the exception types and adds them to the list of exception types.
   *
   * @param throwable
   *          The base throwable.
   * @param exceptions
   *          The list of exception types.
   */
  private void collectExceptionTypes( final Throwable throwable, final List<ExceptionType> exceptions )
  {
    /* Build the exception type. */
    final ExceptionType exception = buildExceptionType( throwable );

    /* Add to the list of exception types. */
    exceptions.add( exception );

    /* Recurse deeper, if neccessary. */
    final Throwable cause = throwable.getCause();
    if( cause != null )
      collectExceptionTypes( cause, exceptions );

    /* End recursion. */
    return;
  }

  /**
   * This function builds the exception type for the given throwable.
   *
   * @param throwable
   *          The throwable, the exception type will be build for.
   * @return The exception type.
   */
  private ExceptionType buildExceptionType( final Throwable throwable )
  {
    if( throwable instanceof OWSException )
    {
      /* Cast. */
      final OWSException owsException = (OWSException) throwable;

      /* Build the exception type. */
      final ExceptionType exception = OWSUtilities.buildExceptionType( new String[] { owsException.getMessage() }, owsException.getExceptionCode().toString(), owsException.getLocator() );

      return exception;
    }

    /* Build the exception type. */
    final ExceptionType exception = OWSUtilities.buildExceptionType( new String[] { throwable.getMessage() }, ExceptionCode.NO_APPLICABLE_CODE.toString(), null );

    return exception;
  }
}