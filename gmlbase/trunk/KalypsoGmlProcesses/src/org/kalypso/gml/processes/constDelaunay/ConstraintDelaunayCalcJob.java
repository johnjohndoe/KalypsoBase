/**
 * 
 */
package org.kalypso.gml.processes.constDelaunay;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.java.lang.ProcessHelper;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.ISimulationDataProvider;
import org.kalypso.simulation.core.ISimulationMonitor;
import org.kalypso.simulation.core.ISimulationResultEater;
import org.kalypso.simulation.core.SimulationException;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_LineString;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

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

    final long lTimeout = PROCESS_TIMEOUT;

    final File simulogFile = new File( tmpdir, "simulation.log" );
    resultEater.addResult( "SimulationLog", simulogFile );

    PrintWriter pwSimuLog = null;
    FileOutputStream strmKernelLog = null;
    FileOutputStream strmKernelErr = null;
    BufferedOutputStream strmPolyInput = null;
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
      writePolyFile( strmPolyInput, (List) calcObject, pwSimuLog );
      strmPolyInput.close();

      // // unpack kernel into tmpDir
      // zipInputStream = WspmTuhhCalcJob.class.getResourceAsStream( "resources/rechenkern.zip" );
      // ZipUtilities.unzip( zipInputStream, tmpDir, false );
      //
      // prepare kernel logs (log and err)
      final File fleKernelLog = new File( tmpdir, "kernel.log" );
      resultEater.addResult( "KernelLog", fleKernelLog );
      strmKernelLog = new FileOutputStream( fleKernelLog );
      final File fleKernelErr = new File( tmpdir, "kernel.err" );
      resultEater.addResult( "KernelErr", fleKernelErr );
      strmKernelErr = new FileOutputStream( fleKernelErr );

      // ATTENTION triangle.exe must be found via PATH
      // TODO: in case security manager is used, catalina.policy must be adapted to allow access to cmd.exe (or even more...)
      final String sCmd = "cmd /c triangle.exe -c -p " + polyfile.getName();

      monitor.setProgress( 20 );
      monitor.setMessage( "Executing triangle.exe" );

      // start calculation
      ProcessHelper.startProcess( sCmd, null, tmpdir, monitor, lTimeout, strmKernelLog, strmKernelErr, null );
      
      // // load results + copy to result folder + unzip templates
      // monitor.setProgress( 80 );
      // monitor.setMessage( "Retrieving results" );
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

  private void writePolyFile( final OutputStream polyStream, final List list, final PrintWriter simLog )
  {
    final List<GM_LineString> breaklines = new ArrayList<GM_LineString>( list.size() );
    int totalPointCount = 0;
    int totalSegmentCount = 0;
    for( final Object geoObject : list )
    {
      if( geoObject instanceof GM_Curve )
      {
        try
        {
          final GM_Curve curve = (GM_Curve) geoObject;
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
  }

}
