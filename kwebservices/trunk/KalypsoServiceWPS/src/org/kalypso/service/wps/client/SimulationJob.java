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
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.opengeospatial.ows.CodeType;
import net.opengeospatial.wps.DataInputsType;
import net.opengeospatial.wps.DescribeProcess;
import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.IOValueType;
import net.opengeospatial.wps.InputDescriptionType;
import net.opengeospatial.wps.OutputDefinitionType;
import net.opengeospatial.wps.OutputDefinitionsType;
import net.opengeospatial.wps.OutputDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessDescriptions;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.resources.CollectFilesVisitor;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.jwsdp.JaxbUtilities;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.OGCUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.simulation.core.simspec.Modeldata;
import org.kalypso.simulation.core.simspec.Modeldata.Input;
import org.kalypso.simulation.core.simspec.Modeldata.Output;

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
  private final String m_simulationType;

  /**
   * After this period of time, the job gives up, waiting for a result of the service.
   */
  private final long m_timeout;

  /**
   * The modelspec for this simulation.
   */
  private Modeldata m_data;

  /**
   * The filesystem manager.
   */
  private FileSystemManager m_fsManager;

  /**
   * The path to the place on the server, where the client can put his files, as configured in the config.ini from
   * Kalypso.
   */
  private String m_server;

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
   * The temporary directory on the server.
   */
  private FileObject m_serverTmpDirectory;

  /**
   * The constructor.
   * 
   * @param calcCaseFolder
   *          The folder of the calc case.
   * @param modelSpec
   *          The file to the model specification.
   * @param simulation
   *          The id identifying the simulation.
   * @param timeout
   *          After this period of time, the job gives up, waiting for a result of the service.
   */
  public SimulationJob( final IContainer calcCaseFolder, final File modelSpec, final String simulationType, final long timeout )
  {
    super( simulationType );

    /* Initializing of the given variables. */
    m_calcCaseFolder = calcCaseFolder;
    m_modelSpec = modelSpec;
    m_simulationType = simulationType;
    m_timeout = timeout;

    /* Initializing the other variables. */
    // TODO Remove default, it is only for testing now!
    m_service = System.getProperty( "org.kalypso.service.wps.service", "http://localhost:8080/ods" );
    m_server = System.getProperty( "org.kalypso.service.wps.server", "file://c:/temp/webdav" );
    m_alreadyWorked = new Integer( 0 );

    /* Variables that are initialized during run time. */
    m_data = null;
    m_fsManager = null;
    m_serverTmpDirectory = null;
  }

  /**
   * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus runInWorkspace( IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( "Berechnung vorbereiten ...", 600 );

    Debug.println( "Checking for server URL, where the input data can be copied ..." );
    if( m_server == null )
    {
      Debug.println( "No URL to the server of the service is given. Be sure to check the config.ini for the org.kalypso.service.wps.server property." );
      throw new CoreException( StatusUtilities.createErrorStatus( "No URL to the server of the service is given. Be sure to check the config.ini for the org.kalypso.service.wps.server property." ) );
    }

    /* If the user aborted the job. */
    monitor.worked( 100 );
    if( monitor.isCanceled() )
      return canceled();

    try
    {
      /* Init the neccessary components. */
      init();

      /* Build the describe process request. */
      List<CodeType> identifier = new LinkedList<CodeType>();
      identifier.add( OGCUtilities.buildCodeType( "", m_simulationType ) );
      DescribeProcess descripeProcess = OGCUtilities.buildDescribeProcess( identifier );

      /* Send the request. */
      monitor.setTaskName( "Frage nach der Prozess-Beschreibung ..." );
      Debug.println( "Asking for a process describtion ..." );
      String describeProcessResponse = WPSUtilities.send( MarshallUtilities.marshall( descripeProcess ), m_service );

      /* Handle the response. */
      Debug.println( "Response:\n" + describeProcessResponse );

      /* Use the process description for building the DataInputs and the OutputDefinitions. */
      ProcessDescriptions processDescriptions = (ProcessDescriptions) MarshallUtilities.unmarshall( describeProcessResponse );

      /* Collect the data. */
      IFile[] inputFiles = getInputFiles( m_calcCaseFolder );

      /* Copy the data to the server. */
      monitor.setTaskName( "Kopiere auf den Server ..." );
      Debug.println( "Copy to the server ..." );

      /* Copy ... */
      copyInputFiles( inputFiles );

      /* If the user aborted the job. */
      monitor.worked( 100 );
      if( monitor.isCanceled() )
        return canceled();

      /* Get the input data. */
      DataInputsType dataInputs = getDataInputs( m_calcCaseFolder, processDescriptions );

      /* Get the output data. */
      OutputDefinitionsType outputDefinitions = getOutputDefinitions( processDescriptions );

      /* Build the execute request. */
      Execute execute = OGCUtilities.buildExecute( OGCUtilities.buildCodeType( "", m_simulationType ), dataInputs, outputDefinitions, true, true );

      /* If the user aborted the job. */
      monitor.worked( 100 );
      if( monitor.isCanceled() )
        return canceled();

      /* Send the request. */
      monitor.setTaskName( "Starte die Simulation ..." );
      Debug.println( "Start the simulation ..." );
      String executeResponse = WPSUtilities.send( MarshallUtilities.marshall( execute ), m_service );

      /* Handle the execute response. */

      /* Polling. */

      /* Handle updated execute response. */

      /* Copy all data from the given URLs to the client. */

      /* Finish. */

      // /* TODO: Proxy? */
      // HttpClient client = new HttpClient();
      // client.getParams().setParameter( HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler( 0, true )
      // );
      // GetMethod get = new GetMethod( m_service + "?SERVICE=WPS&REQUEST=ExecuteSimulation&SIMULATION=" + m_simulation
      // );
      // client.executeMethod( get );
      //
      // /* If the user aborted the job. */
      // monitor.worked( 100 );
      // if( monitor.isCanceled() )
      // return canceled();
      //
      // /* Poll all few seconds to update the status. */
      // boolean run = true;
      // long executed = 0;
      //
      // /* Loop, until an result is available, a timeout is reached or the user has canceled the job. */
      // monitor.setTaskName( "Warte auf Status ..." );
      // while( run )
      // {
      // Debug.println( "Checking state file on dav ..." );
      // FileObject status = m_fsManager.resolveFile( davTmpDirectory, "status.properties" );
      //
      // if( status.exists() )
      // {
      // monitor.setTaskName( "Status" );
      // InputStream inputStream = status.getContent().getInputStream();
      // Properties prop = new Properties();
      // prop.load( inputStream );
      // inputStream.close();
      //
      // Debug.println( prop.toString() );
      //
      // /* Update the monitor values. */
      // updateMonitor( prop, monitor );
      //
      // /* The state of the simulation. */
      // String simulation_status = prop.getProperty( "SIMULATION_STATUS" );
      // if( simulation_status.equals( "FINISHED" ) )
      // {
      // Debug.println( "The simulation has finished ..." );
      // break;
      // }
      // }
      // else
      // Debug.println( "Not started yet ..." );
      //
      // Thread.sleep( 1000 );
      // executed += 1000;
      //
      // /* If the timeout is reached. */
      // if( executed >= m_timeout )
      // {
      // Debug.println( "Timeout reached ..." );
      // return StatusUtilities.createErrorStatus( "Timeout reached ..." );
      // }
      //
      // /* If the user aborted the job. */
      // if( monitor.isCanceled() )
      // return canceled();
      // }
      //
      // /* Check if the results are ready. */
      // monitor.setTaskName( "Prüfe Ergebnisse ..." );
      // Debug.println( "Check the results ..." );
      // FileObject resultDir = m_fsManager.resolveFile( davTmpDirectory, "Result" );
      // if( !resultDir.exists() )
      // {
      // Debug.println( "The result directory does not exist." );
      // throw new CoreException( StatusUtilities.createErrorStatus( "The result directory does not exist." ) );
      // }
      //
      // FileObject[] results = resultDir.getChildren();
      // if( results.length == 0 )
      // {
      // Debug.println( "No results found." );
      // throw new CoreException( StatusUtilities.createErrorStatus( "No results found." ) );
      // }
      //
      // /* If the user aborted the job. */
      // if( monitor.isCanceled() )
      // return canceled();
      //
      // /* Retrieve the results and copy them to their specific destination. */
      // for( int i = 0; i < results.length; i++ )
      // copy( results[i], zip.getParent() );
      //
      // monitor.worked( 100 );
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
   * This function initializes everything.
   */
  private void init( ) throws FileSystemException, JAXBException
  {
    /* Init the model spec. */
    initData();

    /* The filesystem manager needs to be set. */
    initVFSManager();

    /* Get the directory for server access. */
    initServerTmpDirectory();
  }

  /**
   * This function inits the model data.
   */
  private void initData( ) throws JAXBException
  {
    /* Init the model spec. */
    if( m_data == null )
    {
      final Unmarshaller unmarshaller = JaxbUtilities.createQuiet( org.kalypso.simulation.core.simspec.ObjectFactory.class ).createUnmarshaller();
      m_data = (Modeldata) unmarshaller.unmarshal( m_modelSpec );
    }
  }

  /**
   * This function inits the VFS manager.
   */
  private void initVFSManager( ) throws FileSystemException
  {
    if( m_fsManager == null )
    {
      /* The filesystem manager needs to be set. */
      m_fsManager = VFS.getManager();
    }
  }

  /**
   * This function inits the temporary directory of the server, where the client has to put his files.
   */
  private void initServerTmpDirectory( ) throws FileSystemException
  {
    /* If the directory is not initialized, it will be generated and created (if necessary). */
    if( m_serverTmpDirectory == null )
    {
      /* Get the directory for server access. */
      FileObject davDirectory = m_fsManager.resolveFile( m_server );
      m_serverTmpDirectory = m_fsManager.resolveFile( davDirectory, "CalcJob-" + "1234" );

      if( !m_serverTmpDirectory.exists() )
      {
        Debug.println( "Creating folder " + m_serverTmpDirectory.getName().getPath() + " ..." );
        m_serverTmpDirectory.createFolder();
      }
    }
  }

  /**
   * This function collects all input files.
   * 
   * @param calcCaseFolder
   *          The folder of the calc case.
   * @return The input files list.
   */
  private IFile[] getInputFiles( final IContainer calcCaseFolder ) throws CoreException
  {
    /* Get the list with the input. */
    List<Modeldata.Input> inputList = m_data.getInput();

    /* Create a resource visitor. */
    CollectFilesVisitor visitor = new CollectFilesVisitor();

    final IProject project = calcCaseFolder.getProject();
    for( final Input input : inputList )
    {
      final String inputPath = input.getPath();

      /* Alles relativ zum Projekt auflösen! */
      final IContainer baseresource = input.isRelativeToCalcCase() ? calcCaseFolder : project;
      final IResource inputResource = baseresource.findMember( inputPath );
      if( inputResource == null )
      {
        if( input.isOptional() )
          continue;

        throw new CoreException( StatusUtilities.createErrorStatus( "Konnte Input-Resource nicht finden: " + inputPath + "\nÜberprüfen Sie die Modellspezifikation." ) );
      }

      inputResource.accept( visitor );
    }

    return visitor.getFiles();
  }

  /**
   * This function copies all input data to a place, where the WPS can read it.
   * 
   * @param files
   *          The files to copy.
   */
  private void copyInputFiles( IFile[] files ) throws IOException
  {
    Debug.println( "Copying ..." );
    for( int i = 0; i < files.length; i++ )
    {
      File file = files[i].getLocation().toFile();
      final String relativePathTo = files[i].getProjectRelativePath().toString();

      FileObject source = m_fsManager.resolveFile( file.toURL().toExternalForm() );
      FileObject destination = m_fsManager.resolveFile( m_serverTmpDirectory, relativePathTo );

      /* Copy file. */
      copy( source, destination );
    }
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
   * This function creates the data inputs for the wps.
   * 
   * @param calcCaseFolder
   *          The folder of the calc case.
   * @return The data input from the model spec in wps format.
   */
  private DataInputsType getDataInputs( final IContainer calcCaseFolder, ProcessDescriptions processDescriptions ) throws CoreException, FileSystemException
  {
    /* Get the list with the input. */
    List<Input> inputList = m_data.getInput();

    /* The storage for the input values. */
    List<IOValueType> inputValues = new LinkedList<IOValueType>();

    /* The descriptions of all processes from the process descriptions response. */
    List<ProcessDescriptionType> processDescription = processDescriptions.getProcessDescription();

    /* There could be only one description, because we have asked for a special one. */
    ProcessDescriptionType description = processDescription.get( 0 );

    /* Get the input list. */
    DataInputs dataInputs = description.getDataInputs();
    List<InputDescriptionType> inputDescriptions = dataInputs.getInput();

    /* Iterate over all inputs and build the data inputs for the execute request. */
    final IProject project = calcCaseFolder.getProject();
    for( InputDescriptionType inputDescription : inputDescriptions )
    {
      CodeType identifier = inputDescription.getIdentifier();

      /* Check if the input is in our model data, too. */
      Input input = findInput( identifier.getValue(), inputList );
      if( input == null )
      {
        /* Check, if it is an optional one. */
        if( inputDescription.getMinimumOccurs().intValue() == 1 )
        {
          /* Ooops, it is a mandatory one, but it is missing in our model data. */
          throw new CoreException( StatusUtilities.createErrorStatus( "The data input " + identifier.getValue() + " is mandatory. Check your model data." ) );
        }

        continue;
      }

      /* Input is here. */
      final String inputPath = input.getPath();

      /* Alles relativ zum Projekt auflösen! */
      final IContainer baseresource = input.isRelativeToCalcCase() ? calcCaseFolder : project;
      final IResource inputResource = baseresource.findMember( inputPath );
      if( inputResource == null )
      {
        if( inputDescription.getMinimumOccurs().intValue() == 0 )
          continue;

        throw new CoreException( StatusUtilities.createErrorStatus( "Konnte Input-Resource nicht finden: " + inputPath + "\nÜberprüfen Sie die Modellspezifikation." ) );
      }

      /* Build the URL to this input. */
      String relativePathTo = inputResource.getProjectRelativePath().toString();
      FileObject destination = m_fsManager.resolveFile( m_serverTmpDirectory, relativePathTo );
      URL url = destination.getURL();

      /* Build the complex value reference. */
      CodeType code = OGCUtilities.buildCodeType( null, input.getId() );
      ComplexValueReference valueReference = OGCUtilities.buildComplexValueReference( url.toExternalForm(), null, null, null );
      IOValueType ioValue = OGCUtilities.buildIOValueType( code, inputDescription.getTitle(), inputDescription.getAbstract(), valueReference );

      /* Add the input. */
      inputValues.add( ioValue );
    }

    return OGCUtilities.buildDataInputsType( inputValues );
  }

  /**
   * This function searches an input in the model data.
   * 
   * @param ID
   *          The ID of the input to search for.
   * @return The found input or null, if none is found for that ID.
   */
  private Input findInput( String ID, List<Input> inputList )
  {
    for( int i = 0; i < inputList.size(); i++ )
    {
      Input input = inputList.get( i );
      if( input.getId().equals( ID ) )
        return input;
    }

    return null;
  }

  /**
   * This function creates the data outputs for the wps.
   * 
   * @return The output of the model spec in wps format.
   */
  private OutputDefinitionsType getOutputDefinitions( ProcessDescriptions processDescriptions ) throws CoreException
  {
    /* Get the list with the output. */
    List<Output> outputList = m_data.getOutput();

    /* The storage for the output values. */
    List<OutputDefinitionType> outputValues = new LinkedList<OutputDefinitionType>();

    /* The descriptions of all processes from the process descriptions response. */
    List<ProcessDescriptionType> processDescription = processDescriptions.getProcessDescription();

    /* There could be only one description, because we have asked for a special one. */
    ProcessDescriptionType description = processDescription.get( 0 );

    /* Get the output list. */
    ProcessOutputs processOutputs = description.getProcessOutputs();
    List<OutputDescriptionType> outputDescriptions = processOutputs.getOutput();

    /* Iterate over all outputs and build the data inputs for the execute request. */
    for( OutputDescriptionType outputDescription : outputDescriptions )
    {
      CodeType identifier = outputDescription.getIdentifier();

      /* Check if the output is in our model data, too. */
      Output output = findOutput( identifier.getValue(), outputList );

      if( output == null )
      {
        /* Ooops, it is missing in our model data. */
        throw new CoreException( StatusUtilities.createErrorStatus( "The data output " + identifier.getValue() + " is missing. Check your model data." ) );
      }

      CodeType code = OGCUtilities.buildCodeType( null, identifier.getValue() );
      OutputDefinitionType outputDefinition = OGCUtilities.buildOutputDefinitionType( code, outputDescription.getTitle(), outputDescription.getAbstract(), null, null, null, null );

      /* Add the output. */
      outputValues.add( outputDefinition );
    }

    return OGCUtilities.buildOutputDefinitionsType( outputValues );
  }

  /**
   * This function searches an output out in the model data.
   * 
   * @param ID
   *          The ID of the output to search for.
   * @return The found output or null, if none is found for that ID.
   */
  private Output findOutput( String ID, List<Output> outputList )
  {
    for( int i = 0; i < outputList.size(); i++ )
    {
      Output output = outputList.get( i );
      if( output.getId().equals( ID ) )
        return output;
    }

    return null;
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