package com.bce.gis.io.hmo;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.gml.processes.i18n.Messages;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.ParseException;

/**
 * Reader for HMO-Files<br/>
 * Creates a {@link com.vividsolutions.jts.geom.Geometry} from a HMO file<br/>
 * Format der HMO Datei<br/>
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
 * @author Gernot Belger
 */
public class HMOReader
{
  private static final String ERROR_FORMAT = Messages.getString( "com.bce.gis.io.hmo.HMOReader.0" ); //$NON-NLS-1$

  static final String ERROR_POINT = ERROR_FORMAT + Messages.getString( "com.bce.gis.io.hmo.HMOReader.1" ); //$NON-NLS-1$

  static final String ERROR_POINT_DOUBLE = ERROR_FORMAT + Messages.getString( "com.bce.gis.io.hmo.HMOReader.2" ); //$NON-NLS-1$

  static final String ERROR_TYPE = ERROR_FORMAT + Messages.getString( "com.bce.gis.io.hmo.HMOReader.3" ); //$NON-NLS-1$

  static final String ERROR_SEMIKOLON = ERROR_FORMAT + Messages.getString( "com.bce.gis.io.hmo.HMOReader.4" ); //$NON-NLS-1$

  static final String ERROR_TRIANGLE = ERROR_FORMAT + Messages.getString( "com.bce.gis.io.hmo.HMOReader.5" ); //$NON-NLS-1$

  static final String ERROR_TRIANGLE_NOPOINT = ERROR_FORMAT + Messages.getString( "com.bce.gis.io.hmo.HMOReader.6" ); //$NON-NLS-1$

  private final GeometryFactory m_gf;

  public HMOReader( final GeometryFactory gf )
  {
    m_gf = gf;
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
    final Vector<Coordinate> points = new Vector<Coordinate>();

    final LineNumberReader lnr = new LineNumberReader( r );
    while( lnr.ready() )
    {
      final String line = lnr.readLine();
      if( line == null )
        break;

      if( line.length() < 3 || line.charAt( 0 ) == '*' )
        continue;

      if( line.charAt( 1 ) != ':' )
        throw new ParseException( MessageFormat.format( ERROR_SEMIKOLON, lnr.getLineNumber() ) );

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
          throw new ParseException( MessageFormat.format( ERROR_TYPE, lnr.getLineNumber() ) );
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
        throw new ParseException( MessageFormat.format( ERROR_TRIANGLE_NOPOINT, lineNumber ) );

      return m_gf.createLinearRing( new Coordinate[] { p1, p2, p3, p1 } );
    }
    catch( final ArrayIndexOutOfBoundsException aiobe )
    {
      throw new ParseException( MessageFormat.format( ERROR_TRIANGLE_NOPOINT, lineNumber ) );
    }
    catch( final NumberFormatException nfe )
    {
      throw new ParseException( MessageFormat.format( ERROR_TRIANGLE, lineNumber ) + "\n" + nfe.getLocalizedMessage() ); //$NON-NLS-1$
    }
  }

  private void parsePoint( final Vector<Coordinate> points, final String[] data, final int lineNumber ) throws ParseException
  {
    try
    {
      final int n = Integer.parseInt( data[0] );

      if( points.size() > n && points.get( n ) != null )
        throw new ParseException( MessageFormat.format( ERROR_POINT_DOUBLE, lineNumber ) );

      final double x = NumberUtils.parseDouble( data[1] );
      final double y = NumberUtils.parseDouble( data[2] );
      final double z = NumberUtils.parseDouble( data[3] );

      final Coordinate p = new Coordinate( x, y, z );

      if( points.size() <= n )
        points.setSize( n + 1 );
      points.set( n, p );
    }
    catch( final NumberFormatException e )
    {
      throw new ParseException( MessageFormat.format( ERROR_POINT, lineNumber ) + "\n" + e.getLocalizedMessage() ); //$NON-NLS-1$
    }
  }
}
