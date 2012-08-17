package com.bce.gis.io.hmo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.gml.processes.i18n.Messages;

import com.bce.util.MessageFormatUtility;
import com.bce.util.progressbar.Progressable;
import com.bce.util.progressbar.ProgressableInputStream;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.ParseException;

/**
 * <p>
 * Reader for HMO-Files
 * </p>
 * <p>
 * Creates a {@link com.vividsolutions.jts.geom.Geometry Geometrie} from a HMO file
 * </p>
 * <p>
 * Format der HMO Datei
 * </p>
 *
 * <pre>
 *
 *   * mit '*' gekennzeichnete Zeilen oder Zeilen mit weniger als drei Zeichen sind Kommentarzeilen
 *   * zuerst kommen die Punkte mit Nummer, x,y,z-Koordinate (Leerzeichengetrennt)
 *   P: &lt;Nummer:int&gt;	&lt;x:double&gt;  &lt;y:double&gt;  &lt;z:double&gt;
 *   * nach de Punkten kommen die Dreiecke mit Nummer, Ecke1, Ecke2 und Ecke3 (Referenzen auf die Punktnummern)
 *   D: &lt;Nummer:int&gt; &lt;e1:int&gt;  &lt;e2:int&gt;  &lt;e3:int&gt;
 *
 * </pre>
 *
 * @author belger
 */
public class HMOReader
{
  public static final String ERROR_FORMAT = Messages.getString( "com.bce.gis.io.hmo.HMOReader.0" ); //$NON-NLS-1$

  public static final String ERROR_POINT = ERROR_FORMAT + Messages.getString( "com.bce.gis.io.hmo.HMOReader.1" ); //$NON-NLS-1$

  public static final String ERROR_POINT_DOUBLE = ERROR_FORMAT + Messages.getString( "com.bce.gis.io.hmo.HMOReader.2" ); //$NON-NLS-1$

  public static final String ERROR_TYPE = ERROR_FORMAT + Messages.getString( "com.bce.gis.io.hmo.HMOReader.3" ); //$NON-NLS-1$

  public static final String ERROR_SEMIKOLON = ERROR_FORMAT + Messages.getString( "com.bce.gis.io.hmo.HMOReader.4" ); //$NON-NLS-1$

  public static final String ERROR_TRIANGLE = ERROR_FORMAT + Messages.getString( "com.bce.gis.io.hmo.HMOReader.5" ); //$NON-NLS-1$

  public static final String ERROR_TRIANGLE_NOPOINT = ERROR_FORMAT + Messages.getString( "com.bce.gis.io.hmo.HMOReader.6" ); //$NON-NLS-1$

  private static final Logger LOG = Logger.getLogger( HMOReader.class.getName() );

  private final GeometryFactory m_gf;

  public HMOReader( final GeometryFactory gf )
  {
    m_gf = gf;
  }

  /**
   * @throws HMOReaderException
   */
  public final LinearRing[] readFile( final File f, final Progressable p ) throws HMOReaderException
  {
    if( f == null )
      throw new HMOReaderException( Messages.getString( "com.bce.gis.io.hmo.HMOReader.7" ), null ); //$NON-NLS-1$

    try
    {
      LOG.info( Messages.getString( "com.bce.gis.io.hmo.HMOReader.8" ) + f.getAbsolutePath() ); //$NON-NLS-1$

      if( p != null )
        p.setNote( f.getAbsolutePath() + Messages.getString( "com.bce.gis.io.hmo.HMOReader.9" ) ); //$NON-NLS-1$

      final FileInputStream fis = new FileInputStream( f );
      final InputStream is = p != null ? (InputStream) new ProgressableInputStream( fis, p ) : fis;
      final Reader r = new InputStreamReader( is, "US-ASCII" ); //$NON-NLS-1$
      final LinearRing[] triangles = read( r );
      r.close();

      LOG.info( Messages.getString( "com.bce.gis.io.hmo.HMOReader.11" ) + f.getAbsolutePath() ); //$NON-NLS-1$

      return triangles;
    }
    catch( final ParseException pe )
    {
      throw new HMOReaderException( Messages.getString( "com.bce.gis.io.hmo.HMOReader.12" ) + pe.getLocalizedMessage(), pe ); //$NON-NLS-1$
    }
    catch( final InterruptedIOException ioe )
    {
      throw new HMOReaderException( Messages.getString( "com.bce.gis.io.hmo.HMOReader.13" ), ioe ); //$NON-NLS-1$
    }
    catch( final Throwable t )
    {
      throw new HMOReaderException( Messages.getString( "com.bce.gis.io.hmo.HMOReader.14" ) + f.getAbsolutePath() + "\n" + t.getLocalizedMessage(), t ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * Reads a .hmo Datei in form of linar rings. Each ring is garanteed to have exactly 3 vertices.
   * <p>
   * The given reader will be wrapped into a {@link java.io.BufferedReader}, so it is not required to to so by the
   * caller.
   * </p>
   *
   * @throws IOException
   * @throws ParseException
   * @throws InterruptedIOException
   */
  public final LinearRing[] read( final Reader r ) throws IOException, ParseException, InterruptedIOException
  {
    final List<LinearRing> triangles = new ArrayList<LinearRing>();
    final ArrayList<Coordinate> points = new ArrayList<Coordinate>();

    final LineNumberReader lnr = new LineNumberReader( r );
    while( lnr.ready() )
    {
      final String line = lnr.readLine();
      if( line == null )
        break;

      if( line.length() < 3 || line.charAt( 0 ) == '*' )
        continue;

      if( line.charAt( 1 ) != ':' )
        throw new ParseException( MessageFormatUtility.formatMessage( ERROR_SEMIKOLON, lnr.getLineNumber() ) );

      final char c0 = line.charAt( 0 );

      final String data = line.substring( 2 ).trim();

      final String[] split = StringUtils.split( data, ' ' );

      switch( c0 )
      {
        case 'P':
          parsePoint( points, split, lnr.getLineNumber() );
          break;

        case 'D':
          final LinearRing triangle = parseTriangle( points, split, lnr.getLineNumber() );
          triangles.add( triangle );
          break;

        default:
          throw new ParseException( MessageFormatUtility.formatMessage( ERROR_TYPE, lnr.getLineNumber() ) );
      }
    }

    return triangles.toArray( new LinearRing[triangles.size()] );
  }

  private LinearRing parseTriangle( final List<Coordinate> points, final String[] data, final int lineNumber ) throws ParseException
  {
    try
    {
      /* final int n = */Integer.parseInt( data[0] );
      final int n1 = Integer.parseInt( data[1] );
      final int n2 = Integer.parseInt( data[2] );
      final int n3 = Integer.parseInt( data[3] );

      final Coordinate p1 = points.get( n1 );
      final Coordinate p2 = points.get( n2 );
      final Coordinate p3 = points.get( n3 );

      if( p1 == null || p2 == null || p3 == null )
        throw new ParseException( MessageFormatUtility.formatMessage( ERROR_TRIANGLE_NOPOINT, lineNumber ) );

      return m_gf.createLinearRing( new Coordinate[] { p1, p2, p3, p1 } );
    }
    catch( final ArrayIndexOutOfBoundsException aiobe )
    {
      throw new ParseException( MessageFormatUtility.formatMessage( ERROR_TRIANGLE_NOPOINT, lineNumber ) );
    }
    catch( final NumberFormatException nfe )
    {
      throw new ParseException( MessageFormatUtility.formatMessage( ERROR_TRIANGLE, lineNumber ) + "\n" + nfe.getLocalizedMessage() ); //$NON-NLS-1$
    }
  }

  private void parsePoint( final ArrayList<Coordinate> points, final String[] data, final int lineNumber ) throws ParseException
  {
    try
    {
      final int n = Integer.parseInt( data[0] );

      if( points.size() > n && points.get( n ) != null )
        throw new ParseException( MessageFormatUtility.formatMessage( ERROR_POINT_DOUBLE, lineNumber ) );

      final double x = NumberUtils.parseDouble( data[1] );
      final double y = NumberUtils.parseDouble( data[2] );
      final double z = NumberUtils.parseDouble( data[3] );

      final Coordinate p = new Coordinate( x, y, z );

      if( points.size() <= n )
        points.ensureCapacity( n + 1 );
      points.set( n, p );
    }
    catch( final NumberFormatException e )
    {
      throw new ParseException( MessageFormatUtility.formatMessage( ERROR_POINT, lineNumber ) + "\n" + e.getLocalizedMessage() ); //$NON-NLS-1$
    }
  }

  /** Performance test */
  public static void main( final String[] args ) throws HMOReaderException
  {
    LOG.info( Messages.getString( "com.bce.gis.io.hmo.HMOReader.18" ) ); //$NON-NLS-1$
    final HMOReader hmoR = new HMOReader( new GeometryFactory() );
    hmoR.readFile( new File( "C:/tmp/mueller/fliti/DT514582.hmo" ), null ); //$NON-NLS-1$
    LOG.info( Messages.getString( "com.bce.gis.io.hmo.HMOReader.20" ) ); //$NON-NLS-1$
  }
}
