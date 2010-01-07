package org.kalypso.commons.arguments;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Arguments is a simple extension of Properties with a nesting-capability.
 * 
 * @author schlienger
 */
public class Arguments extends LinkedHashMap<String, Object>
{
  /** Fallback, if this arguments does not contain a key */
  private final Map<String, Object> m_parent;

  public Arguments( )
  {
    m_parent = new HashMap<String, Object>();
  }

  public Arguments( final Arguments parent )
  {
    m_parent = parent;
  }

  /**
   * @see java.util.LinkedHashMap#get(java.lang.Object)
   */
  @Override
  public Object get( final Object key )
  {
    final Object object = super.get( key );
    if( object == null )
      return m_parent.get( key );

    return object;
  }

  public String getProperty( final String key )
  {
    final Object object = get( key );
    if( object == null )
      return null;

    return object.toString();
  }

  public String getProperty( final String key, final String defaultValue )
  {
    final Object object = get( key );
    if( object == null )
      return defaultValue;

    return object.toString();
  }

  /**
   * @return the nested-arguments from the given key
   */
  public Arguments getArguments( final String key )
  {
    return (Arguments)get( key );
  }

  public boolean getBoolean( final String key, final boolean defaultValue )
  {
    final String value = getProperty( key );
    if( value == null )
      return defaultValue;

    return Boolean.parseBoolean( value );
  }

  public int getInteger( final String key, final int defaultValue )
  {
    final String value = getProperty( key );
    if( value == null )
      return defaultValue;

    try
    {
      return Integer.parseInt( value );
    }
    catch( final NumberFormatException e )
    {
      e.printStackTrace();
      return defaultValue;
    }
  }

  public String[] getAllKeys( )
  {
    final HashSet<String> allKeys = new HashSet<String>();

    allKeys.addAll( keySet() );
    allKeys.addAll( Arrays.asList( getParentKeys() ) );

    return allKeys.toArray( new String[allKeys.size()] );
  }

  private String[] getParentKeys( )
  {
    if( m_parent instanceof Arguments )
      return ((Arguments) m_parent).getAllKeys();

    final Set<String> parentSet = m_parent.keySet();
    return parentSet.toArray( new String[parentSet.size()] );
  }
}
