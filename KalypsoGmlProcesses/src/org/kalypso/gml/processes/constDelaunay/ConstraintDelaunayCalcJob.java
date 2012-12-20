/**
 *
 */
package org.kalypso.gml.processes.constDelaunay;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.java.lang.ProcessHelper;
import org.kalypso.gml.processes.i18n.Messages;
import org.kalypso.gml.processes.schemata.GmlProcessesUrlCatalog;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.ISimulationDataProvider;
import org.kalypso.simulation.core.ISimulationMonitor;
import org.kalypso.simulation.core.ISimulationResultEater;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.util.BufferedAndOtherOutputStream;
import org.kalypso.simulation.core.util.LogHelper;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_LineString;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.FeaturePath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

/**
 * @author thuel2
 */
public class ConstraintDelaunayCalcJob implements ISimulation
{
  private static final String CALCJOB_SPEC = "ConstraintDelaunayCalcJob_spec.xml"; //$NON-NLS-1$

  private static final long PROCESS_TIMEOUT = 50000;

  @Override
  public URL getSpezifikation( )
  {
    return getClass().getResource( CALCJOB_SPEC );
  }

  @Override
  public void run( final File tmpdir, final ISimulationDataProvider inputProvider, final ISimulationResultEater resultEater, final ISimulationMonitor monitor ) throws SimulationException
  {
    final URL gmlURL = (URL)inputProvider.getInputForID( "BREAKLINES_GML" ); //$NON-NLS-1$
    final String geometryXPath = (String)inputProvider.getInputForID( "BREAKLINES_PATH" ); //$NON-NLS-1$
    final Double qualityMinAngle = inputProvider.hasID( "QUALITY_MIN_ANGLE" ) ? (Double)inputProvider.getInputForID( "QUALITY_MIN_ANGLE" ) : null; //$NON-NLS-1$ //$NON-NLS-2$

    final long lTimeout = PROCESS_TIMEOUT;

    final File simulogFile = new File( tmpdir, "simulation.log" ); //$NON-NLS-1$
    resultEater.addResult( "SimulationLog", simulogFile ); //$NON-NLS-1$

    FileOutputStream strmKernelLog = null;
    FileOutputStream strmKernelErr = null;
    BufferedOutputStream strmPolyInput = null;
    BufferedReader nodeReader = null;
    BufferedReader eleReader = null;
    BufferedAndOtherOutputStream osSimuLog = null;
    try
    {
      osSimuLog = new BufferedAndOtherOutputStream( new FileOutputStream( simulogFile ), System.out );
      final LogHelper log = new LogHelper( osSimuLog, monitor );

      log.log( true, Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayCalcJob.8", geometryXPath ) ); //$NON-NLS-1$

      // load gml
      log.log( true, Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayCalcJob.9", gmlURL ) ); //$NON-NLS-1$

      final GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( gmlURL, null );
      final GMLXPath calcpath = new GMLXPath( geometryXPath, null );

      // get calculation via path
      log.log( true, Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayCalcJob.10", geometryXPath ) ); //$NON-NLS-1$

      /* Retrieve list of GM_Curves from workspace */
      Object calcObject = GMLXPathUtilities.query( calcpath, workspace );
      if( calcObject == null )
      {
        // try feature path instead
        final FeaturePath fPath = new FeaturePath( geometryXPath );
        calcObject = fPath.getFeature( workspace );
      }

      if( !(calcObject instanceof List) )
      {
        monitor.setFinishInfo( IStatus.ERROR, Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayCalcJob.11" ) + calcObject ); //$NON-NLS-1$
        return;
      }

      monitor.setProgress( 10 );

      if( log.checkCanceled() )
        return;

      log.log( true, Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayCalcJob.12" ) ); //$NON-NLS-1$

      /* Write .poly file for triangle.exe */
      final File polyfile = new File( tmpdir, "input.poly" ); //$NON-NLS-1$
      strmPolyInput = new BufferedOutputStream( new FileOutputStream( polyfile ) );
      final String crs = ConstraintDelaunayCalcJob.writePolyFileForLinestrings( strmPolyInput, (List< ? >)calcObject, log.getOutputStream() );
      strmPolyInput.close();

      // prepare kernel logs (log and err)
      final File fleKernelLog = new File( tmpdir, "kernel.log" ); //$NON-NLS-1$
      resultEater.addResult( "KernelLog", fleKernelLog ); //$NON-NLS-1$
      strmKernelLog = new FileOutputStream( fleKernelLog );
      final File fleKernelErr = new File( tmpdir, "kernel.err" ); //$NON-NLS-1$
      resultEater.addResult( "KernelErr", fleKernelErr ); //$NON-NLS-1$
      strmKernelErr = new FileOutputStream( fleKernelErr );

      final URL resource = getClass().getResource( "resources/triangle.exe" ); //$NON-NLS-1$
      FileUtils.copyURLToFile( resource, new File( tmpdir, "triangle.exe" ) ); //$NON-NLS-1$

      //final StringBuffer cmd = new StringBuffer( "cmd /c triangle.exe -c -p" ); //$NON-NLS-1$
      final StringBuffer cmd = new StringBuffer( "cmd /c triangle.exe -p" ); //$NON-NLS-1$
      if( qualityMinAngle != null )
      {
        cmd.append( "-q" ); //$NON-NLS-1$
        cmd.append( qualityMinAngle.doubleValue() );
      }

      cmd.append( ' ' );
      cmd.append( polyfile.getName() );

      monitor.setProgress( 20 );
      if( log.checkCanceled() )
        return;

      // start calculation
      log.log( true, Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayCalcJob.20" ) ); //$NON-NLS-1$

      try
      {

        ProcessHelper.startProcess( cmd.toString(), null, tmpdir, monitor, lTimeout, strmKernelLog, strmKernelErr, null );
      }
      catch( final Throwable e )
      {
        log.log( false, Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayCalcJob.21", e.getLocalizedMessage() ) ); //$NON-NLS-1$
        log.log( e, Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayCalcJob.22" ) ); //$NON-NLS-1$
        monitor.setFinishInfo( IStatus.ERROR, Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayCalcJob.23" ) ); //$NON-NLS-1$
        return;
      }

      // load results + copy to result folder
      monitor.setProgress( 60 );
      if( log.checkCanceled() )
        return;

      log.log( true, Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayCalcJob.24" ) ); //$NON-NLS-1$

      final File nodeFile = new File( tmpdir, "input.1.node" ); //$NON-NLS-1$
      final File eleFile = new File( tmpdir, "input.1.ele" ); //$NON-NLS-1$

      if( !nodeFile.exists() || !eleFile.exists() )
      {
        log.log( false, Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayCalcJob.27" ) ); //$NON-NLS-1$
        log.log( false, Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayCalcJob.28" ) ); //$NON-NLS-1$
        monitor.setFinishInfo( IStatus.ERROR, Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayCalcJob.29" ) ); //$NON-NLS-1$
        return;
      }

      nodeReader = new BufferedReader( new InputStreamReader( new FileInputStream( nodeFile ) ) );
      eleReader = new BufferedReader( new InputStreamReader( new FileInputStream( eleFile ) ) );

      final GMLWorkspace elementWorkspace = readElements( nodeReader, eleReader, crs );

      nodeReader.close();
      eleReader.close();

      final File triangleFile = new File( tmpdir, "triangles.gml" ); //$NON-NLS-1$
      GmlSerializer.serializeWorkspace( triangleFile, elementWorkspace, "UTF-8" ); //$NON-NLS-1$
      resultEater.addResult( "ResultFileGML", triangleFile ); //$NON-NLS-1$
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new SimulationException( Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayCalcJob.33" ), e ); //$NON-NLS-1$
    }
    finally
    {
      IOUtils.closeQuietly( osSimuLog );
      IOUtils.closeQuietly( strmKernelLog );
      IOUtils.closeQuietly( strmKernelErr );
      IOUtils.closeQuietly( strmPolyInput );
    }
  }

  private GMLWorkspace readElements( final BufferedReader nodeReader, final BufferedReader eleReader, final String crs ) throws IOException, GMLSchemaException
  {
    final GM_Position[] points = TriangleExe.parseTriangleNodeOutput( nodeReader );

    final GMLWorkspace workspace = FeatureFactory.createGMLWorkspace( new QName( GmlProcessesUrlCatalog.NS_MESH, "TriangleCollection" ), null, null ); //$NON-NLS-1$
    final Feature rootFeature = workspace.getRootFeature();
    final QName memberName = new QName( GmlProcessesUrlCatalog.NS_MESH, "triangleMember" ); //$NON-NLS-1$
    final QName featureName = new QName( GmlProcessesUrlCatalog.NS_MESH, "Triangle" ); //$NON-NLS-1$
    final QName geomName = new QName( GmlProcessesUrlCatalog.NS_MESH, "triangle" ); //$NON-NLS-1$

    final List<GM_Triangle> surfaces = TriangleExe.parseTriangleElementOutput( eleReader, crs, points );

    for( final GM_Triangle surface : surfaces )
    {
      final Feature newFeature = FeatureHelper.addFeature( rootFeature, memberName, featureName );
      newFeature.setProperty( geomName, surface );
    }

    return workspace;
  }

  /**
   * writes out a triangle-polyfile with linestrings for the console program Triangle.exe
   */
  private static String writePolyFileForLinestrings( final OutputStream polyStream, final List< ? > list, final PrintStream simLog )
  {
    final List<GM_LineString> breaklines = new ArrayList<>( list.size() );
    int totalPointCount = 0;
    int totalSegmentCount = 0;

    String crs = null;

    for( final Object geoObject : list )
    {
      if( geoObject instanceof GM_Curve )
      {
        try
        {
          final GM_Curve curve = (GM_Curve)geoObject;

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
          simLog.println( Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayHelper.1" ) + geoObject + " - " + e.getLocalizedMessage() ); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
      else if( geoObject instanceof Feature )
      {
        try
        {
          final Feature feature = (Feature)geoObject;
          final GM_Curve curve = (GM_Curve)feature.getDefaultGeometryPropertyValue();

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
          simLog.println( Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayHelper.1" ) + geoObject + " - " + e.getLocalizedMessage() ); //$NON-NLS-1$ //$NON-NLS-2$
        }

      }
      else
      {
        simLog.println( Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayHelper.3" ) + geoObject ); //$NON-NLS-1$
      }
    }

    final PrintWriter writer = new PrintWriter( new OutputStreamWriter( polyStream ) );

    /* node file */

    /* write header */
    // First line: <# of vertices> <dimension (must be 2)> <# of attributes> <# of boundary markers (0 or 1)>
    writer.print( totalPointCount );
    writer.println( " 2 1 0" ); // Dimension 2, Value-Dimension 1, Border Markers 0 //$NON-NLS-1$

    // Remaining lines: <vertex #> <x> <y> [attributes] [boundary marker]
    int pointIndex = 1;
    writeNodes( breaklines, writer, pointIndex );

    // First line: <# of vertices> <dimension (must be 2)> <# of attributes> <# of boundary markers (0 or 1)>
    writer.print( totalSegmentCount );
    writer.println( " 0" ); // Border Markers 0 //$NON-NLS-1$

    // Following lines: <vertex #> <x> <y> [attributes] [boundary marker]
    final int segmentIndex = 1;
    // we reuse pointindex and iterate through the segments in the same way as before
    pointIndex = 1;
    writeElements( breaklines, writer, pointIndex, segmentIndex );

    // One line: <# of segments> <# of boundary markers (0 or 1)>
    // Following lines: <segment #> <endpoint> <endpoint> [boundary marker]
    writer.println( "0" ); //$NON-NLS-1$
    writer.println( "" ); //$NON-NLS-1$

    // One line: <# of holes>
    // Following lines: <hole #> <x> <y>

    // Optional line: <# of regional attributes and/or area constraints>
    // Optional following lines: <region #> <x> <y> <attribute> <maximum area>

    writer.flush();

    return crs;
  }

  /**
   * writes the elements of the given {@link GM_LineString} to the given {@link PrintWriter}
   */
  private static void writeElements( final List<GM_LineString> lines, final PrintWriter writer, int pointIndex, int segmentIndex )
  {
    for( final GM_LineString line : lines )
    {
      for( int i = 0; i < line.getNumberOfPoints() - 1; i++ )
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
  }

  /**
   * writes the points of the given {@link GM_LineString} to the given {@link PrintWriter}
   */
  private static void writeNodes( final List<GM_LineString> lines, final PrintWriter writer, int pointIndex )
  {
    for( final GM_LineString line : lines )
    {
      for( int i = 0; i < line.getNumberOfPoints(); i++ )
      {
        final GM_Position pos = line.getPositionAt( i );
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
  }

}
