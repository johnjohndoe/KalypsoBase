package org.kalypso.commons.cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.kalypso.commons.serializer.ISerializer;

/**
 * CacheTest
 * 
 * @author schlienger
 */
public class FileCacheTest extends TestCase
{
  public void testGetObject( ) throws InvocationTargetException
  {
    final StringKeyFactory fact = new StringKeyFactory();
    final Comparator<String> kc = new StringComparator();
    final ISerializer<String> ser = new StringSerializer();
    final FileCache<String, String> cache = new FileCache<String, String>( fact, kc, ser, new File( System.getProperty( "java.io.tmpdir" ) ) ); //$NON-NLS-1$

    cache.addObject( "A", "A" ); //$NON-NLS-1$ //$NON-NLS-2$
    cache.addObject( "B", "B" ); //$NON-NLS-1$ //$NON-NLS-2$
    cache.addObject( "C", "C" ); //$NON-NLS-1$ //$NON-NLS-2$
    cache.addObject( "D", "D" ); //$NON-NLS-1$ //$NON-NLS-2$

    assertEquals( cache.getObject( "A" ), "A" ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( cache.getObject( "B" ), "B" ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( cache.getObject( "C" ), "C" ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( cache.getObject( "D" ), "D" ); //$NON-NLS-1$ //$NON-NLS-2$

    cache.remove( "C" ); //$NON-NLS-1$

    assertTrue( cache.getObject( "C" ) == null ); //$NON-NLS-1$

    cache.clear();

    assertTrue( cache.size() == 0 );
    assertTrue( cache.getObject( "B" ) == null ); //$NON-NLS-1$
  }

  protected static class StringComparator implements Comparator<String>
  {
    @Override
    public int compare( String s1, String s2 )
    {
      return s1.compareTo( s2 );
    }
  }

  protected static class StringKeyFactory implements IKeyFactory<String>
  {
    @Override
    public String createKey( final String string )
    {
      return string;
    }

    @Override
    public String toString( final String key )
    {
      return key;
    }
  }

  protected static class StringSerializer implements ISerializer<String>
  {
    @Override
    public String read( final InputStream ins ) throws IOException
    {
      BufferedReader r = null;
      try
      {
        r = new BufferedReader( new InputStreamReader( ins ) );

        return r.readLine();
      }
      finally
      {
        IOUtils.closeQuietly( r );
      }
    }

    @Override
    public void write( final String object, final OutputStream os ) throws IOException
    {
      BufferedWriter w = null;
      try
      {
        w = new BufferedWriter( new OutputStreamWriter( os ) );
        w.write( object.toString() );
      }
      finally
      {
        IOUtils.closeQuietly( w );
      }
    }
  }
}
