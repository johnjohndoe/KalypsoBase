package com.bce.gis.io.hmo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.MessageFormat;

import org.kalypso.gml.processes.i18n.Messages;

import junit.framework.TestCase;

import com.bce.util.MessageFormatUtility;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.ParseException;

/**
 * @author belger
 */
public class HMOReaderTest extends TestCase
{
  private final static GeometryFactory gf = new GeometryFactory();

  private final static Coordinate c0 = new Coordinate( 0.0, 0.0, 1.0 );

  private final static Coordinate c1 = new Coordinate( 0.0, 1.0, 2.0 );

  private final static Coordinate c2 = new Coordinate( 1.0, 0.0, 3.0 );

  private static final LinearRing triangle = gf.createLinearRing( new Coordinate[] { c0, c1, c2, c0 } );

  private static final String s_testHmo = "P: 1  0.0 0.0 1.0\n" + "P: 2  0.0 1.0 2.0\n" + "P: 3  1.0 0.0 3.0\n" + "D: 1 1 2 3"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

  private static final String s_errorHmo1 = "*\n" + "*blubb\n" + "   \n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

  private static final String s_errorHmo2 = "P: 1 0.0 0.0 0.0\n" + "X: blubb"; //$NON-NLS-1$ //$NON-NLS-2$

  private final HMOReader hmoReader = new HMOReader( gf );

  public void testReadSimple( ) throws IOException, ParseException
  {
    final LinearRing[] g = readString( s_testHmo );
    assertEquals( 1, g.length );
    assertTrue( triangle.equalsExact( g[0] ) );
  }

  private LinearRing[] readString( final String s ) throws IOException, ParseException
  {
    final StringReader sr = new StringReader( s );
    final LinearRing[] g = hmoReader.read( sr );
    sr.close();
    return g;
  }

  public void testReadComplex( ) throws IOException, ParseException
  {
    final Reader r = new InputStreamReader( HMOReaderTest.class.getResourceAsStream( "test/medium.hmo" ) ); //$NON-NLS-1$
    final LinearRing[] g = hmoReader.read( r );
    r.close();

    assertEquals( 38, g.length );
  }

  public void testBig( ) throws IOException, ParseException
  {
    final Reader r = new InputStreamReader( HMOReaderTest.class.getResourceAsStream( "test/big.hmo" ) ); //$NON-NLS-1$
    final LinearRing[] g = hmoReader.read( r );
    r.close();

    assertEquals( 63864, g.length );
  }

  public void testMissingSemikolon( ) throws IOException
  {
    checkError( s_errorHmo1, MessageFormat.format( HMOReader.ERROR_SEMIKOLON, new Object[] { new Integer( 3 ) } ) );
  }

  public void testNonMonotonPointNumbers( ) throws InterruptedIOException, IOException, ParseException
  {
    // Bug: nicht monoton ansteigende Punktnummern ergaben Lesefehler
    final Reader r = new InputStreamReader( HMOReaderTest.class.getResourceAsStream( "test/nonMonotone.hmo" ) ); //$NON-NLS-1$
    final LinearRing[] g = hmoReader.read( r );
    r.close();

    assertEquals( 1, g.length );
  }

  public void testWrongType( ) throws IOException
  {
    checkError( s_errorHmo2, MessageFormatUtility.formatMessage( HMOReader.ERROR_TYPE, 2 ) );
  }

  public void testWrongPoint( ) throws IOException
  {
    checkError( "P: 0.5 1.0 1.0 1.0", MessageFormatUtility.formatMessage( HMOReader.ERROR_POINT, 1 ) ); //$NON-NLS-1$
    checkError( "P: 1 xy 1.0 1.0", MessageFormatUtility.formatMessage( HMOReader.ERROR_POINT, 1 ) ); //$NON-NLS-1$
    checkError( "P: 1 1.0 ab 1.0", MessageFormatUtility.formatMessage( HMOReader.ERROR_POINT, 1 ) ); //$NON-NLS-1$
    checkError( "P: 1 1.0 1.0 de", MessageFormatUtility.formatMessage( HMOReader.ERROR_POINT, 1 ) ); //$NON-NLS-1$
    checkError( "P: 1 0.0 0.0 0.0\nP: 1 1.0 1.0 1.0", MessageFormatUtility.formatMessage( HMOReader.ERROR_POINT_DOUBLE, 2 ) ); //$NON-NLS-1$
  }

  public void testWrongTriangle( ) throws IOException
  {
    checkError( "D: 1 a 2 3", MessageFormatUtility.formatMessage( HMOReader.ERROR_TRIANGLE, 1 ) ); //$NON-NLS-1$
    checkError( "D: 1 1 b 3", MessageFormatUtility.formatMessage( HMOReader.ERROR_TRIANGLE, 1 ) ); //$NON-NLS-1$
    checkError( "D: 1 1 2 c", MessageFormatUtility.formatMessage( HMOReader.ERROR_TRIANGLE, 1 ) ); //$NON-NLS-1$
    checkError( "D: 1 1 2 3", MessageFormatUtility.formatMessage( HMOReader.ERROR_TRIANGLE_NOPOINT, 1 ) ); //$NON-NLS-1$
  }

  private void checkError( final String hmoString, final String exceptionString ) throws IOException
  {
    try
    {
      readString( hmoString );
      fail( Messages.getString("com.bce.gis.io.hmo.HMOReaderTest.21") ); //$NON-NLS-1$
    }
    catch( final ParseException pe )
    {
      assertTrue( pe.getLocalizedMessage().startsWith( exceptionString ) );
    }
  }
}
