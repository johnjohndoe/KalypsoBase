package kalypso.service.ogc.exception;


public class OWSException extends Exception{

	public static enum ExceptionCode{
		OPERATION_NOT_SUPPORTED,
		MISSING_PARAMETER_VALUE,
		INVALID_PARAMETER_VALUE,
		VERSION_NEGPTIOATON_FAILED,
		INVALID_UPDATE_SEQUENCE,
		NO_APPLICABLE_CODE
	};
	
	private static final long serialVersionUID = 1L;
	private ExceptionCode m_exceptionCode;
	private String m_locator;
	
	public OWSException(ExceptionCode exceptionCode, String exceptionText, String locator)
	{
		super(exceptionText);
		m_exceptionCode=exceptionCode;
		m_locator=locator;
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
	
}
