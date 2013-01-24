package org.kalypso.contribs.eclipse.core.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Yet another MultiStatus that is more directed to be used for error messages that should be delivered to the user
 * using the ErrorDialog. If no error messages have been added to it, isOK() returns true.
 * 
 * @author schlienger
 * @deprecated Use {@link org.eclipse.core.runtime.MultiStatus} instead.
 */
@Deprecated
public class MultiStatus extends Status
{
  private final Map<String, Throwable> m_errorMessages = new HashMap<String, Throwable>();

  public MultiStatus( final int severity, final String pluginId, final int code, final String message )
  {
    super( severity, pluginId, code, message, null );
  }

  /**
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize( ) throws Throwable
  {
    m_errorMessages.clear();

    super.finalize();
  }

  /**
   * @return true if at least one message is available
   */
  public boolean hasMessages( )
  {
    return m_errorMessages.size() > 0;
  }

  /**
   * @see org.eclipse.core.runtime.Status#isOK()
   */
  @Override
  public boolean isOK( )
  {
    return !hasMessages();
  }

  /**
   * @see org.eclipse.core.runtime.Status#isMultiStatus()
   */
  @Override
  public boolean isMultiStatus( )
  {
    return true;
  }

  /**
   * @see org.eclipse.core.runtime.MultiStatus#getChildren()
   */
  @Override
  public IStatus[] getChildren( )
  {
    final IStatus[] stati = new IStatus[m_errorMessages.size()];
    int i = 0;
    for( final Entry<String, Throwable> entry : m_errorMessages.entrySet() )
      stati[i++] = new Status( getSeverity(), getPlugin(), getCode(), entry.getKey(), entry.getValue() );

    return stati;
  }

  /**
   * @see org.eclipse.core.runtime.Status#getException()
   */
  @Override
  public Throwable getException( )
  {
    return new Exception( "Siehe details" );
  }

  /**
   * Adds a message to this multi status. Same effect as calling addMessage( message, null ).
   * 
   * @param message
   */
  public void addMessage( final String message )
  {
    addMessage( message, null );
  }

  /**
   * Adds a message associated with a throwable to this multi status.
   * 
   * @param message
   * @param t
   */
  public void addMessage( final String message, final Throwable t )
  {
    m_errorMessages.put( message, t );
  }
}