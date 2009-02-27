package org.kalypso.service.ogc.exception;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.opengis.ows.ExceptionReport;
import net.opengis.ows.ExceptionType;
import net.opengis.ows.ObjectFactory;

import org.kalypso.jwsdp.JaxbUtilities;

public class OWSException extends Exception
{
  private static ObjectFactory OF = new ObjectFactory();

  private static final JAXBContext JC = JaxbUtilities.createQuiet( ObjectFactory.class );

  public static enum ExceptionCode
  {
    OPERATION_NOT_SUPPORTED,
    MISSING_PARAMETER_VALUE,
    INVALID_PARAMETER_VALUE,
    VERSION_NEGPTIOATON_FAILED,
    INVALID_UPDATE_SEQUENCE,
    NO_APPLICABLE_CODE
  }

  private static final long serialVersionUID = 1L;

  private final ExceptionCode m_exceptionCode;

  private final String m_locator;

  public OWSException( final ExceptionCode exceptionCode, final Exception cause, final String locator )
  {
    super( cause );
    m_exceptionCode = exceptionCode;
    m_locator = locator;
  }

  public OWSException( final ExceptionCode exceptionCode, final String exceptionText, final String locator )
  {
    super( exceptionText );
    m_exceptionCode = exceptionCode;
    m_locator = locator;
  }

  public ExceptionCode getExceptionCode( )
  {
    return m_exceptionCode;
  }

  public String getExceptionText( )
  {
    return getMessage();
  }

  public String getLocator( )
  {
    return m_locator;
  }

  /**
   * @return error document as formatted XML string
   */
  public String toXMLString( )
  {
    final ExceptionReport report = OF.createExceptionReport();
    final ExceptionType extype = OF.createExceptionType();
    extype.setExceptionCode( m_exceptionCode.toString() );
    extype.setLocator( m_locator );
    final StringWriter ew = new StringWriter();
    final PrintWriter pw = new PrintWriter( ew, true );
    printStackTrace( pw );
    extype.getExceptionText().add( ew.toString() );
    report.getException().add( extype );
    Marshaller m;
    String excString = "";
    StringWriter sw = null;
    try
    {
      sw = new StringWriter();
      m = JC.createMarshaller();
      m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
      m.marshal( report, sw );
      excString = sw.toString();
    }
    catch( final JAXBException e1 )
    {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    finally
    {
      if( sw != null )
        try
      {
          sw.close();
      }
      catch( final IOException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return excString;
  }
}