/**
 * 
 */
package org.kalypso.gml.processes.constDelaunay;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.java.lang.ProcessHelper;
import org.kalypso.commons.performance.TimeLogger;
import org.kalypso.gml.processes.schemata.GmlProcessesUrlCatalog;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.ISimulationDataProvider;
import org.kalypso.simulation.core.ISimulationMonitor;
import org.kalypso.simulation.core.ISimulationResultEater;
import org.kalypso.simulation.core.SimulationException;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_LineString;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.opengis.cs.CS_CoordinateSystem;

/**
 * @author thuel2
 */
public class ConstraintDelaunayCalcJob implements ISimulation
{
  private static final String CALCJOB_SPEC = "ConstraintDelaunayCalcJob_spec.xml";

  private static final long PROCESS_TIMEOUT = 50000;

  /**
   * @see org.kalypso.simulation.core.ISimulation#getSpezifikation()
   */
  public URL getSpezifikation( )
  {
    return getClass().getResource( CALCJOB_SPEC );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulation#run(java.io.File, org.kalypso.simulation.core.ISimulationDataProvider,
   *      org.kalypso.simulation.core.ISimulationResultEater, org.kalypso.simulation.core.ISimulationMonitor)
   */
  public void run( final File tmpdir, final ISimulationDataProvider inputProvider, final ISimulationResultEater resultEater, ISimulationMonitor monitor ) throws SimulationException
  {
    final URL gmlURL = (URL) inputProvider.getInputForID( "BREAKLINES_GML" );
    final String geometryXPath = (String) inputProvider.getInputForID( "BREAKLINES_PATH" );
    final Double qualityMinAngle = inputProvider.hasID( "QUALITY_MIN_ANGLE" ) ? (Double) inputProvider.getInputForID( "QUALITY_MIN_ANGLE" ) : null;

    final long lTimeout = PROCESS_TIMEOUT;

    final File simulogFile = new File( tmpdir, "simulation.log" );
    resultEater.addResult( "SimulationLog", simulogFile );

    PrintWriter pwSimuLog = null;
    FileOutputStream strmKernelLog = null;
    FileOutputStream strmKernelErr = null;
    BufferedOutputStream strmPolyInput = null;
    BufferedReader nodeReader = null;
    BufferedReader eleReader = null;
    try
    {
      pwSimuLog = new PrintWriter( new BufferedWriter( new FileWriter( simulogFile ) ) );

      pwSimuLog.println( "Parsing GMLXPath: " + geometryXPath );
      monitor.setMessage( "Parsing GMLXPath: " + geometryXPath );
      final GMLXPath calcpath = new GMLXPath( geometryXPath );

      // load gml
      pwSimuLog.println( "Loading GML: " + gmlURL );
      monitor.setMessage( "Loading GML: " + gmlURL );
      final GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( gmlURL );

      // get calculation via path
      pwSimuLog.println( "Loading geometries: " + geometryXPath );
      monitor.setMessage( "Loading geometries: " + geometryXPath );

      final Object calcObject = GMLXPathUtilities.query( calcpath, workspace );
      if( !(calcObject instanceof List) )
      {
        monitor.setFinishInfo( IStatus.ERROR, "GMLXPath does not point to a list: " + calcObject );
        return;
      }

      monitor.setProgress( 10 );
      pwSimuLog.println( "Writing .poly file for triangle.exe" );
      monitor.setMessage( "Writing .poly file for triangle.exe" );
      final File polyfile = new File( tmpdir, "input.poly" );
      strmPolyInput = new BufferedOutputStream( new FileOutputStream( polyfile ) );
      final CS_CoordinateSystem crs = writePolyFile( strmPolyInput, (List) calcObject, pwSimuLog );
      strmPolyInput.close();

      // prepare kernel logs (log and err)
      final File fleKernelLog = new File( tmpdir, "kernel.log" );
      resultEater.addResult( "KernelLog", fleKernelLog );
      strmKernelLog = new FileOutputStream( fleKernelLog );
      final File fleKernelErr = new File( tmpdir, "kernel.err" );
      resultEater.addResult( "KernelErr", fleKernelErr );
      strmKernelErr = new FileOutputStream( fleKernelErr );

      // ATTENTION triangle.exe must be found via PATH
      // TODO: in case security manager is used, catalina.policy must be adapted to allow access to cmd.exe (or even
      // more...)
      final StringBuffer cmd = new StringBuffer( "cmd /c triangle.exe -c -p" );
      if( qualityMinAngle != null )
      {
        cmd.append( "-q" );
        cmd.append( qualityMinAngle.doubleValue() );
      }

      cmd.append( ' ' );
      cmd.append( polyfile.getName() );

      monitor.setProgress( 20 );
      monitor.setMessage( "Executing triangle.exe" );

      // start calculation
      ProcessHelper.startProcess( cmd.toString(), null, tmpdir, monitor, lTimeout, strmKernelLog, strmKernelErr, null );

      // load results + copy to result folder
      monitor.setProgress( 60 );
      monitor.setMessage( "Loading mesh into gml." );
      pwSimuLog.println( "Loading mesh into gml." );

      final File nodeFile = new File( tmpdir, "input.1.node" );
      final File eleFile = new File( tmpdir, "input.1.ele" );
      nodeReader = new BufferedReader( new InputStreamReader( new FileInputStream( nodeFile ) ) );
      eleReader = new BufferedReader( new InputStreamReader( new FileInputStream( eleFile ) ) );

      final GMLWorkspace elementWorkspace = readElements( nodeReader, eleReader, crs );
      writePolyFile( strmPolyInput, (List) calcObject, pwSimuLog );

      nodeReader.close();
      eleReader.close();

      final File triangleFile = new File( tmpdir, "triangles.gml" );
      GmlSerializer.serializeWorkspace( triangleFile, elementWorkspace, "UTF-8" );
      resultEater.addResult( "ResultFileGML", triangleFile );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new SimulationException( "Fehler bei der Berechnung", e );
    }
    finally
    {
      IOUtils.closeQuietly( pwSimuLog );
      IOUtils.closeQuietly( strmKernelLog );
      IOUtils.closeQuietly( strmKernelErr );
      IOUtils.closeQuietly( strmPolyInput );
    }
  }

  private GMLWorkspace readElements( final BufferedReader nodeReader, final BufferedReader eleReader, CS_CoordinateSystem crs ) throws InvocationTargetException, IOException, GM_Exception, GMLSchemaException
  {
    final TimeLogger logger = new TimeLogger( "Start reading mesh points" );

    // ignore first line, we don't check the file format
    final String firstLine = nodeReader.readLine();
    final StringTokenizer firstTokenizer = new StringTokenizer( firstLine );
    final int pointCount = Integer.parseInt( firstTokenizer.nextToken() );

    final GM_Position[] points = new GM_Position[pointCount + 1];

    while( nodeReader.ready() )
    {
      final String string = nodeReader.readLine();
      if( string == null )
        break;

      if( string.startsWith( "#" ) )
        continue;

      final StringTokenizer tokenizer = new StringTokenizer( string );
      final int id = Integer.parseInt( tokenizer.nextToken() );
      final double x = Double.parseDouble( tokenizer.nextToken() );
      final double y = Double.parseDouble( tokenizer.nextToken() );
      final double z = Double.parseDouble( tokenizer.nextToken() );

      final GM_Position position = GeometryFactory.createGM_Position( x, y, z );
      points[id] = position;
    }

    logger.showElapsedTime( "Nodes read." );

    final GMLWorkspace workspace = FeatureFactory.createGMLWorkspace( new QName( GmlProcessesUrlCatalog.NS_MESH, "TriangleCollection" ) );
    final Feature rootFeature = workspace.getRootFeature();
    final QName memberName = new QName( GmlProcessesUrlCatalog.NS_MESH, "triangleMember" );
    final QName featureName = new QName( GmlProcessesUrlCatalog.NS_MESH, "Triangle" );

    logger.showElapsedTime( "Start reading elements." );

    eleReader.readLine(); // ignore first line
    final QName geomName = new QName( GmlProcessesUrlCatalog.NS_MESH, "triangle" );
    while( eleReader.ready() )
    {
      final String string = eleReader.readLine();
      if( string == null )
        break;

      if( string.startsWith( "#" ) )
        continue;

      final StringTokenizer tokenizer = new StringTokenizer( string );
      tokenizer.nextToken(); // ele id - ignore
      final int p1 = Integer.parseInt( tokenizer.nextToken() );
      final int p2 = Integer.parseInt( tokenizer.nextToken() );
      final int p3 = Integer.parseInt( tokenizer.nextToken() );

      final GM_Position[] triangle = new GM_Position[] { points[p1], points[p2], points[p3], points[p1] };

      final GM_Surface surface = GeometryFactory.createGM_Surface( triangle, null, null, crs );

      final Feature newFeature = FeatureHelper.addFeature( rootFeature, memberName, featureName );

      newFeature.setProperty( geomName, surface );
    }

    logger.showElapsedTime( "Elements read." );

    return workspace;
  }

  /**
   * @return the first coordinate system it encounters
   */
  private CS_CoordinateSystem writePolyFile( final OutputStream polyStream, final List list, final PrintWriter simLog )
  {
    final List<GM_LineString> breaklines = new ArrayList<GM_LineString>( list.size() );
    int totalPointCount = 0;
    int totalSegmentCount = 0;

    CS_CoordinateSystem crs = null;

    for( final Object geoObject : list )
    {
      if( geoObject instanceof GM_Curve )
      {
        try
        {
          final GM_Curve curve = (GM_Curve) geoObject;

          if( crs == null )
            crs = curve.getCoordinateSystem();

          final GM_LineString lineString;
          lineString = curve.getAsLineString();
          breaklines.add( lineString );
          totalPointCount += lineString.getNumberOfPoints();
          totalSegmentCount += lineString.getNumberOfPoints() - 1;
        }
        catch( final GM_Exception e )
        {
          e.printStackTrace();
          simLog.println( "Bad geometry object:  " + geoObject + " - " + e.getLocalizedMessage() );
        }
      }
      else
      {
        simLog.println( "Geometry object cannot be handled:  " + geoObject );
      }
    }

    final PrintWriter writer = new PrintWriter( new OutputStreamWriter( polyStream ) );
    writer.print( totalPointCount );
    writer.println( " 2 1 0" ); // Dimension 2, Value-Dimension 1, Border Markers 0
    int pointIndex = 1;
    for( final GM_LineString breakline : breaklines )
    {
      for( int i = 0; i < breakline.getNumberOfPoints(); i++ )
      {
        final GM_Position pos = breakline.getPositionAt( i );
        writer.print( pointIndex++ );
        writer.print( ' ' );
        writer.print( pos.getX() );
        writer.print( ' ' );
        writer.print( pos.getY() );
        writer.print( ' ' );
        writer.print( pos.getZ() );
        writer.println();
      }
    }

    writer.print( totalSegmentCount );
    writer.println( " 0" ); // Border Markers 0

    int segmentIndex = 1;
    // we reuse pointindex and iterate through the segments in the same way as before
    pointIndex = 1;
    for( final GM_LineString breakline : breaklines )
    {
      for( int i = 0; i < breakline.getNumberOfPoints() - 1; i++ )
      {
        writer.print( segmentIndex++ );
        writer.print( ' ' );
        writer.print( pointIndex++ );
        writer.print( ' ' );
        writer.print( pointIndex );
        writer.println();
      }

      // increase pointIndex one more, because we have one segment less than points per line string
      pointIndex++;
    }

    writer.println( "0" );
    writer.println( "" );

    writer.flush();

    return crs;
  }

}
