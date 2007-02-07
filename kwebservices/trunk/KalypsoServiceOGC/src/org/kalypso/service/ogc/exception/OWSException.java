package org.kalypso.service.ogc.exception;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.kalypso.jwsdp.JaxbUtilities;

import sun.security.action.GetLongAction;


import net.opengis.ows.ExceptionReport;
import net.opengis.ows.ExceptionType;
import net.opengis.ows.ObjectFactory;



public class OWSException extends Exception
{
  
  private static ObjectFactory OF=new ObjectFactory();
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

  private ExceptionCode m_exceptionCode;

  private String m_locator;

  public OWSException( ExceptionCode exceptionCode, String exceptionText, String locator )
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
  public String toXMLString()
  {
    ExceptionReport report = OF.createExceptionReport();
    ExceptionType extype = OF.createExceptionType();
    extype.setExceptionCode( m_exceptionCode.toString() );
    extype.setLocator( m_locator );
    extype.getExceptionText().add( getMessage() );
    report.getException().add( extype );
    Marshaller m;
    String excString="";
    StringWriter sw=null;
    try
    {
      sw=new StringWriter();
      m = JC.createMarshaller();
      m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
      m.marshal(report, sw);
      excString=sw.toString();
    }
    catch( JAXBException e1 )
    {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    finally
    {
      if (sw!=null)
        try
        {
          sw.close();
        }
        catch( IOException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
    }
    return excString;
  }


}
