/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.service.wps.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.util.zip.ZipResourceVisitor;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.jwsdp.JaxbUtilities;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.KalypsoSimulationCoreExtensions;
import org.kalypso.simulation.core.simspec.Modeldata;
import org.kalypso.simulation.core.simspec.Modeldata.Input;

// TODO Get the simulation via the extension manager of simulation core.
// TODO Transform modelspec into an OGC conform xml.
// TODO Copy each single file separately to the dav. Do not zip them.
// TODO Therefore use a unique temp-directory for this calc job.
// TODO The response of the server should have an OGC conform format.
// TODO The response should be evaluated, not the status.properties.
// TODO Change the parameters for adressing the server. Use OGC conform parameters.
// TODO The server should be able to handle more than one simulation simultanliously. Needs a thread manager.
// TODO More important as any other operation is the execute operation. Do it first.
// TODO Change monitor update function, that it understands percent values.
// TODO The server should answer with xml-responses, as described in OGC standarts.
// TODO Therefore use an output stream he should get with the parameters in his execute function.
// TODO The main service has to be changed to understood that. (Think of Alex' ODSService!).

/**
 * This class manages the connect between the client and the server.<br>
 * It polls regularly and checks the status of the calculation, that can be retrieved from it then.<br>
 * Furthermore it has the ability to cancel its running simulation.<br>
 * <br>
 * It is somehow a wrapper on an ISimulation on the server side.
 * 
 * @author Holger Albert
 */
public class SimulationJob extends WorkspaceJob
{
  /**
   * The amount from the max monitor value, which is reserved for the server side task.
   */
  private int MONITOR_SERVER_VALUE = 100;

  /**
   * The calc case folder.
   */
  private final IContainer m_calcCaseFolder;

  /**
   * The model specifications.
   */
  private final File m_modelSpec;

  /**
   * The id identifying the simulation.
   */
  private final String m_simulation;

  /**
   * After this period of time, the job gives up, waiting for a result of the service.
   */
  private final long m_timeout;

  /**
   * The id for this simulation running on the server.
   */
  private String m_id;

  /**
   * The modelspec for this simulation.
   */
  private Modeldata m_data;

  /**
   * The filesystem manager.
   */
  private FileSystemManager m_fsManager;

  /**
   * The path to the webdav, as configured in the config.ini from Kalypso.
   */
  private String m_dav;

  /**
   * The URL to the wps service.
   */
  private String m_service;

  /**
   * Necessary for the monitor update functinality. Stores the last updated value, to prevent, that the monitor is
   * updated to often.
   */
  private Integer m_alreadyWorked;

  /**
   * The constructor.
   * 
   * @param simulation
   *          The id identifying the simulation.
   * @param timeout
   *          After this period of time, the job gives up, waiting for a result of the service.
   */
  public SimulationJob( final IContainer calcCaseFolder, final File modelSpec, final String simulation, final long timeout )
  {
    super( simulation );

    m_calcCaseFolder = calcCaseFolder;
    m_modelSpec = modelSpec;

    m_simulation = simulation;
    m_timeout = timeout;
    m_id = null;

    m_data = null;
    m_fsManager = null;

    // TODO Remove default, it is only for testing now!
    m_dav = System.getProperty( "org.kalypso.service.wps.dav", "file://c:/temp/webdav" );
    m_service = System.getProperty( "org.kalypso.service.wps.service", "http://localhost:8080/ods" );

    m_alreadyWorked = new Integer( 0 );
  }

  /**
   * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus runInWorkspace( IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( "Berechnung vorbereiten ...", 600 );

    /* Check if the simulation is available. */
    Debug.println( "Checking for simulation \"" + m_simulation + "\" ..." );
    ISimulation simulation = KalypsoSimulationCoreExtensions.createSimulation( m_simulation );

    if( simulation == null )
    {
      Debug.println( "No simulation with the id \"" + m_simulation + " available.\"" );
      throw new CoreException( StatusUtilities.createErrorStatus( "No simulation with the id \"" + m_simulation + " available.\"" ) );
    }

    Debug.println( "Checking for webdav URL ..." );
    if( m_dav == null )
    {
      Debug.println( "No URL to the webdav of the service is given. Be sure to check the config.ini for the org.kalypso.service.wps.dav property." );
      throw new CoreException( StatusUtilities.createErrorStatus( "No URL to the webdav of the service is given. Be sure to check the config.ini for the org.kalypso.service.wps.dav property." ) );
    }

    /* If the user aborted the job. */
    monitor.worked( 100 );
    if( monitor.isCanceled() )
      return canceled();

    /* Copy the data to the dav. */
    // TODO
    
    /* Convert the input data to the ogc conform request. */

    /* Build the execute request. */

    /* Send the request. */

    /* Handle the execute response. */

    /* Polling. */

    /* Handle updated execute response. */

    /* Copy all data from the given URLs to the client. */

    /* Finish. */

    try
    {
      /* Get input data. */
      final Unmarshaller unmarshaller = JaxbUtilities.createQuiet( org.kalypso.simulation.core.simspec.ObjectFactory.class ).createUnmarshaller();
      m_data = (Modeldata) unmarshaller.unmarshal( m_modelSpec );

      /* Zip everything. */
      monitor.setTaskName( "Zippe die Eingangs-Daten ..." );
      Debug.println( "Zip everything ..." );
      File zipFile = zipData( m_calcCaseFolder, monitor );

      /* If the user aborted the job. */
      monitor.worked( 100 );
      if( monitor.isCanceled() )
        return canceled();

      /* The filesystem manager needs to be set. */
      m_fsManager = VFS.getManager();

      monitor.setTaskName( "Kopiere auf den Server ..." );
      Debug.println( "Copying ..." );

      /* Convert the zip file. */
      FileObject zip = m_fsManager.resolveFile( zipFile.getParentFile(), zipFile.getName() );

      /* Get the directory for dav access. */
      FileObject davDirectory = m_fsManager.resolveFile( m_dav );
      FileObject davTmpDirectory = m_fsManager.resolveFile( davDirectory, "CalcJob-" + m_id );

      if( !davTmpDirectory.exists() )
      {
        Debug.println( "Creating folder " + davTmpDirectory.getName().getPath() + " ..." );
        davTmpDirectory.createFolder();
      }

      /* Copy the input data to the dav. */
      copy( zip, davTmpDirectory );

      /* Convert the modelspec file. */
      FileObject modelSpec = m_fsManager.resolveFile( m_modelSpec.toURL().toExternalForm() );

      /* Copy the modelspec to the dav. */
      copy( modelSpec, davTmpDirectory );

      /* If the user aborted the job. */
      monitor.worked( 100 );
      if( monitor.isCanceled() )
        return canceled();

      /* Start calculation. */
      monitor.setTaskName( "Starte die Simulation ..." );
      Debug.println( "Calling " + m_service + "?SERVICE=WPS&REQUEST=ExecuteSimulation&SIMULATION=" + m_simulation + " ..." );

      /* TODO: Proxy? */
      HttpClient client = new HttpClient();
      client.getParams().setParameter( HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler( 0, true ) );
      GetMethod get = new GetMethod( m_service + "?SERVICE=WPS&REQUEST=ExecuteSimulation&SIMULATION=" + m_simulation );
      client.executeMethod( get );

      /* If the user aborted the job. */
      monitor.worked( 100 );
      if( monitor.isCanceled() )
        return canceled();

      /* Poll all few seconds to update the status. */
      boolean run = true;
      long executed = 0;

      /* Loop, until an result is available, a timeout is reached or the user has canceled the job. */
      monitor.setTaskName( "Warte auf Status ..." );
      while( run )
      {
        Debug.println( "Checking state file on dav ..." );
        FileObject status = m_fsManager.resolveFile( davTmpDirectory, "status.properties" );

        if( status.exists() )
        {
          monitor.setTaskName( "Status" );
          InputStream inputStream = status.getContent().getInputStream();
          Properties prop = new Properties();
          prop.load( inputStream );
          inputStream.close();

          Debug.println( prop.toString() );

          /* Update the monitor values. */
          updateMonitor( prop, monitor );

          /* The state of the simulation. */
          String simulation_status = prop.getProperty( "SIMULATION_STATUS" );
          if( simulation_status.equals( "FINISHED" ) )
          {
            Debug.println( "The simulation has finished ..." );
            break;
          }
        }
        else
          Debug.println( "Not started yet ..." );

        Thread.sleep( 1000 );
        executed += 1000;

        /* If the timeout is reached. */
        if( executed >= m_timeout )
        {
          Debug.println( "Timeout reached ..." );
          return StatusUtilities.createErrorStatus( "Timeout reached ..." );
        }

        /* If the user aborted the job. */
        if( monitor.isCanceled() )
          return canceled();
      }

      /* Check if the results are ready. */
      monitor.setTaskName( "Prüfe Ergebnisse ..." );
      Debug.println( "Check the results ..." );
      FileObject resultDir = m_fsManager.resolveFile( davTmpDirectory, "Result" );
      if( !resultDir.exists() )
      {
        Debug.println( "The result directory does not exist." );
        throw new CoreException( StatusUtilities.createErrorStatus( "The result directory does not exist." ) );
      }

      FileObject[] results = resultDir.getChildren();
      if( results.length == 0 )
      {
        Debug.println( "No results found." );
        throw new CoreException( StatusUtilities.createErrorStatus( "No results found." ) );
      }

      /* If the user aborted the job. */
      if( monitor.isCanceled() )
        return canceled();

      /* Retrieve the results and copy them to their specific destination. */
      for( int i = 0; i < results.length; i++ )
        copy( results[i], zip.getParent() );

      monitor.worked( 100 );
    }
    catch( Exception e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
    finally
    {
      monitor.done();
    }

    /* Leave the job. */
    return Status.OK_STATUS;
  }

  /**
   * This function will zip all input data.
   * 
   * @param calcCaseFolder
   *          The folder of the calc case.
   * @param monitor
   *          The progress monitor.
   * @return The new temporary zip file.
   */
  private File zipData( final IContainer calcCaseFolder, final IProgressMonitor monitor ) throws CoreException, IOException
  {
    /* Get the list with the input. */
    List<Modeldata.Input> inputList = m_data.getInput();

    /* Create the zip file. */
    File zipFile = File.createTempFile( "CalcJobData_", ".zip" );
    zipFile.deleteOnExit();

    /* Zip everything. */
    ZipResourceVisitor zipper = null;

    try
    {
      final IProject project = calcCaseFolder.getProject();
      for( final Input input : inputList )
      {
        final String inputPath = input.getPath();

        /* alles relativ zum Projekt auflösen! */
        final IContainer baseresource = input.isRelativeToCalcCase() ? calcCaseFolder : project;
        final IResource inputResource = baseresource.findMember( inputPath );
        if( inputResource == null )
        {
          if( input.isOptional() )
            continue;

          throw new CoreException( StatusUtilities.createErrorStatus( "Konnte Input-Resource nicht finden: " + inputPath + "\nÜberprüfen Sie die Modellspezifikation." ) );
        }

        /* Create zipper very lazy, in order to prevent exception because zip stream is empty. */
        if( zipper == null )
          zipper = new ZipResourceVisitor( zipFile );

        inputResource.accept( zipper );
      }
    }
    finally
    {
      if( zipper != null )
      {
        try
        {
          zipper.close();
        }
        catch( final IOException e )
        {
          e.printStackTrace();
        }
      }
    }

    return zipFile;
  }

  /**
   * This function copies a source file to a given destination. If no filename is given in the destination file handle,
   * the filename of the source is used.
   * 
   * @param source
   *          The source file.
   * @param destination
   *          The destination file or path.
   */
  private void copy( FileObject source, FileObject destination ) throws IOException
  {
    InputStream is = null;
    OutputStream os = null;

    if( source.getType().equals( FileType.FOLDER ) )
      throw new IllegalArgumentException( "The source-file is a directory ..." );

    /* If the destination is only a directory, use the sources filename for the destination file. */
    if( destination.getType().equals( FileType.FOLDER ) )
      destination = m_fsManager.resolveFile( destination, source.getName().getBaseName() );

    try
    {
      is = source.getContent().getInputStream();
      os = destination.getContent().getOutputStream();

      /* Copying ... */
      IOUtils.copy( is, os );

      is.close();
      os.close();
    }
    finally
    {
      IOUtils.closeQuietly( is );
      IOUtils.closeQuietly( os );
    }
  }

  /**
   * This function refreshs the monitor.
   * 
   * @param prop
   *          The properties, containing the status of the simulation in the server.
   * @param monitor
   *          The progressmonitor to be updated.
   */
  private void updateMonitor( Properties prop, IProgressMonitor monitor )
  {
    /* Get the properties. */
    String monitor_min = prop.getProperty( "MONITOR_MIN" );
    String monitor_max = prop.getProperty( "MONITOR_MAX" );
    String monitor_worked = prop.getProperty( "MONITOR_WORKED" );
    String monitor_message = prop.getProperty( "MONITOR_MESSAGE" );

    /* Cast to Integers. */
    Integer min = new Integer( monitor_min );
    Integer max = new Integer( monitor_max );
    Integer worked = new Integer( monitor_worked );

    /* Calculating the monitor values on the server side. */
    Integer xMax = max - min;
    Integer xWorked = worked - min;

    /* If the already updated value is not equal to the new value. Update the monitor values. */
    if( !xWorked.equals( m_alreadyWorked ) )
    {
      /* Check, what is already updated here, and update the rest. */
      Integer xRest = xWorked - m_alreadyWorked;
      Integer percentWorked = 100 * xRest / xMax;

      /* Project percentWorked to the left monitor value. */
      Integer realValue = MONITOR_SERVER_VALUE * percentWorked / 100;
      monitor.worked( realValue );

      m_alreadyWorked = xWorked;
    }

    /* Set the message. */
    monitor.subTask( monitor_message );
  }

  /**
   * This function is called, when the job is canceled by the user.<br>
   * A cancel request has to be send to the server, so that he can stop executing the simulation.
   */
  private IStatus canceled( )
  {
    // TODO Tell the server, that the job should be canceled.
    return Status.OK_STATUS;
  }
}