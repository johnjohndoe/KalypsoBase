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
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.java.lang.ProcessHelper;
import org.kalypso.gml.processes.schemata.GmlProcessesUrlCatalog;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.ISimulationDataProvider;
import org.kalypso.simulation.core.ISimulationMonitor;
import org.kalypso.simulation.core.ISimulationResultEater;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.util.LogHelper;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
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
  @SuppressWarnings("unchecked")
  public void run( final File tmpdir, final ISimulationDataProvider inputProvider, final ISimulationResultEater resultEater, final ISimulationMonitor monitor ) throws SimulationException
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

      final LogHelper log = new LogHelper( pwSimuLog, monitor, System.out );

      log.log( true, "Parsing GMLXPath: %s", geometryXPath );

      final GMLXPath calcpath = new GMLXPath( geometryXPath );

      // load gml
      log.log( true, "Loading GML: %s", gmlURL );

      final GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( gmlURL, null );

      // get calculation via path
      log.log( true, "Loading geometries: %s", geometryXPath );

      /* Retrieve list of GM_Curves from workspace */
      final Object calcObject = GMLXPathUtilities.query( calcpath, workspace );
      if( !(calcObject instanceof List) )
      {
        monitor.setFinishInfo( IStatus.ERROR, "GMLXPath does not point to a list: " + calcObject );
        return;
      }

      monitor.setProgress( 10 );

      if( log.checkCanceled() )
        return;

      log.log( true, "Schreibe .poly Datei für triangle.exe..." );

      /* Write .poly file for triangle.exe */
      final File polyfile = new File( tmpdir, "input.poly" );
      strmPolyInput = new BufferedOutputStream( new FileOutputStream( polyfile ) );
      final String crs = ConstraintDelaunayHelper.writePolyFileForLinestrings( strmPolyInput, (List) calcObject, pwSimuLog );
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
      if( log.checkCanceled() )
        return;

      // start calculation
      log.log( true, "Triangle.exe wird ausgeführt..." );

      try
      {
        ProcessHelper.startProcess( cmd.toString(), null, tmpdir, monitor, lTimeout, strmKernelLog, strmKernelErr, null );
      }
      catch( final Throwable e )
      {
        log.log( false, "Fehler beim Ausführen von triangle.exe: %s", e.getLocalizedMessage() );
        log.log( e, "Stellen Sie sicher, dass triangle.exe über die Windows PATH-Variable auffindbar ist." );
        monitor.setFinishInfo( IStatus.ERROR, "Fehler: triangle.exe konnte nicht ausgeführt werden." );
        return;
      }

      // load results + copy to result folder
      monitor.setProgress( 60 );
      if( log.checkCanceled() )
        return;

      log.log( true, "Netz wird als GML geschrieben..." );

      final File nodeFile = new File( tmpdir, "input.1.node" );
      final File eleFile = new File( tmpdir, "input.1.ele" );

      if( !nodeFile.exists() || !eleFile.exists() )
      {
        log.log( false, "Fehler beim Ausführen von triangle.exe" );
        log.log( false, "Stellen Sie sicher, dass triangle.exe über die Windows PATH-Variable auffindbar ist." );
        monitor.setFinishInfo( IStatus.ERROR, "Fehler: triangle.exe konnte nicht ausgeführt werden." );
        return;
      }

      nodeReader = new BufferedReader( new InputStreamReader( new FileInputStream( nodeFile ) ) );
      eleReader = new BufferedReader( new InputStreamReader( new FileInputStream( eleFile ) ) );

      final GMLWorkspace elementWorkspace = readElements( nodeReader, eleReader, crs );
      ConstraintDelaunayHelper.writePolyFileForLinestrings( strmPolyInput, (List) calcObject, pwSimuLog );

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

  @SuppressWarnings("unchecked")
  private GMLWorkspace readElements( final BufferedReader nodeReader, final BufferedReader eleReader, final String crs ) throws InvocationTargetException, IOException, GM_Exception, GMLSchemaException
  {
    final GM_Position[] points = ConstraintDelaunayHelper.parseTriangleNodeOutput( nodeReader );

    final GMLWorkspace workspace = FeatureFactory.createGMLWorkspace( new QName( GmlProcessesUrlCatalog.NS_MESH, "TriangleCollection" ), null, null );
    final Feature rootFeature = workspace.getRootFeature();
    final QName memberName = new QName( GmlProcessesUrlCatalog.NS_MESH, "triangleMember" );
    final QName featureName = new QName( GmlProcessesUrlCatalog.NS_MESH, "Triangle" );
    final QName geomName = new QName( GmlProcessesUrlCatalog.NS_MESH, "triangle" );

    final List<GM_Surface> surfaces = ConstraintDelaunayHelper.parseTriangleElementOutput( eleReader, crs, points );

    for( final GM_Surface surface : surfaces )
    {
      final Feature newFeature = FeatureHelper.addFeature( rootFeature, memberName, featureName );
      newFeature.setProperty( geomName, surface );
    }

    return workspace;
  }

}
