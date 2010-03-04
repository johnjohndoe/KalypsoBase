package org.kalypso.auth.user;


/**
 * A Kalypso User, default implementation of IKalypsoUser.
 * 
 * @author schlienger
 */
public class KalypsoUser implements IKalypsoUser
{
  private final String[] m_rights;

  private final String m_userName;
  
  public KalypsoUser( final String userName, final String[] rights )
  {
    m_userName = userName;
    m_rights = rights;
  }
  
  public String getUserName()
  {
    return m_userName;
  }

  @Override
  public String toString()
  {
    return m_userName;
  }

  public boolean hasRight( final String right )
  {
    for( final String mRight : m_rights )
    {
      if( right.equals( mRight ) )
        return true;
    }

    return false;
  }
}
