package de.openali.odysseus.service.ows.exception;

import net.opengis.ows.ExceptionReportDocument;
import net.opengis.ows.ExceptionReportDocument.ExceptionReport;
import net.opengis.ows.ExceptionType;

public class OWSException extends Exception
{

	public static enum ExceptionCode
	{
		OPERATION_NOT_SUPPORTED, MISSING_PARAMETER_VALUE, INVALID_PARAMETER_VALUE, VERSION_NEGOTIOATON_FAILED, INVALID_UPDATE_SEQUENCE, NO_APPLICABLE_CODE
	}

	private static final long serialVersionUID = 1L;

	private final ExceptionCode m_exceptionCode;

	private final String m_locator;

	public OWSException(ExceptionCode exceptionCode, String exceptionText,
	        String locator)
	{
		super(exceptionText);
		m_exceptionCode = exceptionCode;
		m_locator = locator;
	}

	public ExceptionCode getExceptionCode()
	{
		return m_exceptionCode;
	}

	public String getExceptionText()
	{
		return getMessage();
	}

	public String getLocator()
	{
		return m_locator;
	}

	/**
	 * @return error document as formatted XML string
	 */
	public String toXMLString()
	{
		ExceptionReportDocument erd = ExceptionReportDocument.Factory
		        .newInstance();
		ExceptionReport er = erd.addNewExceptionReport();
		ExceptionType et = er.addNewException();
		et.setExceptionCode(m_exceptionCode.toString());
		if (m_locator != null)
			et.setLocator(m_locator);
		et.setExceptionTextArray(new String[] { getMessage() });
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		return header + erd.toString();
	}
}