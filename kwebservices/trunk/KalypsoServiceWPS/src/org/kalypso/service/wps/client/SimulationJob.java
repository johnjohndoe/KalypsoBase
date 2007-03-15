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
import java.math.BigInteger;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.opengeospatial.ows.CodeType;
import net.opengeospatial.ows.ExceptionReport;
import net.opengeospatial.ows.ExceptionType;
import net.opengeospatial.wps.DataInputsType;
import net.opengeospatial.wps.DescribeProcess;
import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.ExecuteResponseType;
import net.opengeospatial.wps.IOValueType;
import net.opengeospatial.wps.InputDescriptionType;
import net.opengeospatial.wps.OutputDefinitionType;
import net.opengeospatial.wps.OutputDefinitionsType;
import net.opengeospatial.wps.OutputDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessDescriptions;
import net.opengeospatial.wps.ProcessFailedType;
import net.opengeospatial.wps.ProcessStartedType;
import net.opengeospatial.wps.StatusType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.resources.CollectFilesVisitor;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.jwsdp.JaxbUtilities;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.VFSUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.ogc.OGCUtilities;
import org.kalypso.simulation.core.simspec.Modeldata;
import org.kalypso.simulation.core.simspec.Modeldata.Input;
import org.kalypso.simulation.core.simspec.Modeldata.Output;

/**
 * This class manages the connect between the client and the server.<br>
 * It polls regularly and checks the status of the calculation, that can be retrieved from it then.<br>
 * Furthermore it has the ability to cancel its running simulation.<br>
 * <br>
 * It is somehow a wrapper on an ISimulation on the server side.<br>
 * <br>
 * Currently it understands only files in sending and receiving.
 * 
 * @author Holger Albert
 */
public class SimulationJob
{
  /**
   * The amount from the max monitor value, which is reserved for the server side task.
   */
  private int MONITOR_SERVER_VALUE = 500;

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
  private String m_input;

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
    /* Initializing of the given variables. */
    m_calcCaseFolder = calcCaseFolder;
    m_modelSpec = modelSpec;
    m_simulationType = simulationType;
    m_timeout = timeout;

    /* Initializing the other variables. */
    m_service = System.getProperty( "org.kalypso.service.wps.service", "http://informdss.bafg.de/bridge/ogc" );
    m_input = System.getProperty( "org.kalypso.service.wps.input", "webdav://WebDAV:webdav@informdss.bafg.de/webdav/input" );
    m_alreadyWorked = new Integer( 0 );

    /* Variables that are initialized during run time. */
    m_data = null;
    m_fsManager = null;
    m_serverTmpDirectory = null;
  }

  /**
   * Starts the simulation.
   * 
   * @param monitor
   *          The progress monitor.
   */
  @SuppressWarnings("unchecked")
  public IStatus run( IProgressMonitor monitor )
  {
    monitor.beginTask( "Berechnung vorbereiten ...", 1000 );

    Debug.println( "Checking for service URL ..." );
    if( m_service == null )
    {
      Debug.println( "No URL to the service is given. Be sure to check the config.ini for the org.kalypso.service.wps.service property." );
      return StatusUtilities.createErrorStatus( "No URL to the the service is given. Be sure to check the config.ini for the org.kalypso.service.wps.service property." );
    }

    Debug.println( "Checking for server URL, where the input data can be copied ..." );
    if( m_input == null )
    {
      Debug.println( "No URL to the server of the service is given. Be sure to check the config.ini for the org.kalypso.service.wps.server property." );
      return StatusUtilities.createErrorStatus( "No URL to the server of the service is given. Be sure to check the config.ini for the org.kalypso.service.wps.server property." );
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

      /* Try to unmarshall. */
      Object describeProcessObject = MarshallUtilities.unmarshall( describeProcessResponse );
      if( describeProcessObject instanceof ExceptionReport )
      {
        ExceptionReport report = (ExceptionReport) describeProcessObject;
        return StatusUtilities.createErrorStatus( createErrorString( report ) );
      }

      /* Use the process description for building the DataInputs and the OutputDefinitions. */
      ProcessDescriptions processDescriptions = (ProcessDescriptions) describeProcessObject;

      /* Get the directory for server access. */
      initServerTmpDirectory();

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
      Debug.println( "Response:\n" + executeResponse );

      if( executeResponse == null || executeResponse.length() == 0 )
        return StatusUtilities.createErrorStatus( "Got an empty response ..." );

      Object response = MarshallUtilities.unmarshall( executeResponse );
      if( response instanceof ExceptionReport )
        return StatusUtilities.createErrorStatus( createErrorString( (ExceptionReport) response ) );

      JAXBElement<ExecuteResponseType> exResponse = (JAXBElement<ExecuteResponseType>) response;
      ExecuteResponseType value = exResponse.getValue();
      StatusType status = value.getStatus();

      ProcessFailedType processFailed = status.getProcessFailed();
      if( processFailed != null )
        return StatusUtilities.createErrorStatus( createErrorString( processFailed.getExceptionReport() ) );

      /* If the user aborted the job. */
      monitor.worked( 100 );
      if( monitor.isCanceled() )
        return canceled();

      /* Retrieve the path to the status file of the process. */
      String statusLocation = value.getStatusLocation();
      if( statusLocation == null || statusLocation.length() == 0 )
        return StatusUtilities.createErrorStatus( "The server responded without a status-location." );

      /* Poll all few seconds to update the status. */
      boolean run = true;
      long executed = 0;
      ExecuteResponseType exState = null;

      /* Loop, until an result is available, a timeout is reached or the user has canceled the job. */
      monitor.setTaskName( "Warte auf Status ..." );
      while( run )
      {
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

        Debug.println( "Checking state file of the server ..." );
        FileObject statusFile = VFSUtilities.checkProxyFor( statusLocation );

        if( statusFile.exists() )
        {
          /* Some variables for handling the errors. */
          boolean success = false;
          int cnt = 0;

          /* Try to read the status at least 3 times, before exiting. */
          JAXBElement<ExecuteResponseType> executeState = null;
          while( success == false )
          {
            try
            {
              String xml = MarshallUtilities.fromInputStream( statusFile.getContent().getInputStream() );
              Object object = MarshallUtilities.unmarshall( xml );
              executeState = (JAXBElement<ExecuteResponseType>) object;

              success = true;
            }
            catch( Exception e )
            {
              /* An error has occured while copying the file. */
              Debug.println( "An error has occured with the message: " + e.getLocalizedMessage() );

              /* If a certain amount (here 2) of retries was reached before, rethrow the error. */
              if( cnt >= 2 )
              {
                Debug.println( "The second retry has failed, rethrowing the error ..." );
                throw e;
              }

              /* Retry the copying of the file. */
              cnt++;
              Debug.println( "Retry: " + String.valueOf( cnt ) );
              success = false;

              /* Wait for some milliseconds. */
              Thread.sleep( 1000 );
            }
          }

          /* Satus was read successfull. */
          exState = executeState.getValue();
          StatusType state = exState.getStatus();

          if( state.getProcessAccepted() != null )
          {
            /* Do nothing, but waiting of an other response. */
            continue;
          }
          else if( state.getProcessFailed() != null )
          {
            String messages = createErrorString( state.getProcessFailed().getExceptionReport() );
            return StatusUtilities.createErrorStatus( messages );
          }
          else if( state.getProcessStarted() != null )
          {
            ProcessStartedType processStarted = state.getProcessStarted();
            String description = processStarted.getValue();
            BigInteger percent = processStarted.getPercentCompleted();
            int percentCompleted = 0;
            if( percent != null )
              percentCompleted = percent.intValue();

            /* Update the monitor values. */
            updateMonitor( percentCompleted, description, monitor );
          }
          else if( state.getProcessSucceeded() != null )
          {
            Debug.println( "The simulation has finished ..." );
            break;
          }
          else
            return StatusUtilities.createErrorStatus( "The server responded with an unknown state ..." );
        }
        else
          Debug.println( "Not started yet ..." );
      }

      /* Check if the results are ready. */
      monitor.setTaskName( "Prüfe Ergebnisse ..." );
      Debug.println( "Check the results ..." );

      /* Get the process outputs. */
      net.opengeospatial.wps.ExecuteResponseType.ProcessOutputs processOutputs = exState.getProcessOutputs();
      if( processOutputs == null )
        return StatusUtilities.createErrorStatus( "The server responded without any outputs ..." );

      /* Copy all process output to their destination. */
      monitor.setTaskName( "Kopiere Ergebnisse zu Client ..." );
      copyOutputFiles( m_calcCaseFolder, processOutputs );

      /* Finish. */
      monitor.worked( 100 );
      finish();
    }
    catch( Exception e )
    {
      e.printStackTrace();
      Debug.println( "Error occured with the message: " + e.getLocalizedMessage() );
      return StatusUtilities.statusFromThrowable( e );
    }
    finally
    {
      monitor.done();
    }

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
      m_fsManager = VFSUtilities.getManager();
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
      FileObject serverDirectory = m_fsManager.resolveFile( m_input );
      m_serverTmpDirectory = VFSUtilities.createTempDirectory( "Simulation_", serverDirectory );

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
  private void copyInputFiles( IFile[] files ) throws Exception
  {
    for( int i = 0; i < files.length; i++ )
    {
      File file = files[i].getLocation().toFile();
      final String relativePathTo = files[i].getProjectRelativePath().toString();

      FileObject source = m_fsManager.toFileObject( file );
      FileObject destination = m_fsManager.resolveFile( m_serverTmpDirectory, relativePathTo );

      /* Copy file. */
      VFSUtilities.copy( source, destination );
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
      ComplexValueReference valueReference = OGCUtilities.buildComplexValueReference( WPSUtilities.convertInternalToServer( url.toExternalForm(), m_input ), null, null, null );
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
   * @param percentCompleted
   *          The amount of work done, reaching from 0 to 100.
   * @param description
   *          The description to set.
   * @param monitor
   *          The progressmonitor to be updated.
   */
  private void updateMonitor( Integer percentCompleted, String description, IProgressMonitor monitor )
  {
    /* If the already updated value is not equal to the new value. Update the monitor values. */
    if( !percentCompleted.equals( m_alreadyWorked ) )
    {
      /* Check, what is already updated here, and update the rest. */
      Integer percentWorked = percentCompleted - m_alreadyWorked;

      /* Project percentWorked to the left monitor value. */
      Integer realValue = MONITOR_SERVER_VALUE * percentWorked / 100;
      monitor.worked( realValue );

      m_alreadyWorked = percentCompleted;
    }

    /* Set the message. */
    monitor.setTaskName( description );
  }

  /**
   * Copys the process outputs to their destination.
   * 
   * @param calcCaseFolder
   *          The calc case folder.
   * @param processOutputs
   *          The process outputs contains the info of the files, which are to copy.
   */
  private void copyOutputFiles( final IContainer calcCaseFolder, net.opengeospatial.wps.ExecuteResponseType.ProcessOutputs processOutputs ) throws Exception
  {
    /* Get the model spec outputs. */
    List<Output> outputs = m_data.getOutput();

    final IProject project = calcCaseFolder.getProject();

    /* Copy all data from the given URLs to the client. */
    List<IOValueType> ioValues = processOutputs.getOutput();
    for( IOValueType ioValue : ioValues )
    {
      ComplexValueReference complexValueReference = ioValue.getComplexValueReference();
      if( complexValueReference == null )
        continue;

      /* Building the source object. */
      String reference = complexValueReference.getReference();
      FileObject source = VFSUtilities.checkProxyFor( reference );

      /* Building the destination object. */
      String id = ioValue.getIdentifier().getValue();
      Output output = findOutput( id, outputs );

      /* Everything should be relative to the project! */
      String outputPath = output.getPath();
      final IContainer baseresource = output.isRelativeToCalcCase() ? calcCaseFolder : project;
      File baseFile = baseresource.getLocation().toFile();
      File outputFile = new File( baseFile, outputPath );
      FileObject destination = m_fsManager.toFileObject( outputFile );

      /* Copy ... */
      VFSUtilities.copy( source, destination );

      IResource destResource = baseresource.getFile( new Path( outputPath ) );
      if( destResource != null && destResource.getType() == IResource.FILE )
      {
        IFile destFile = (IFile) destResource;
        destFile.getParent().refreshLocal( IResource.DEPTH_INFINITE, new NullProgressMonitor() );
        destFile.setCharset( "UTF-8", new NullProgressMonitor() );
      }
    }

    // project.refreshLocal( IResource.DEPTH_INFINITE, new NullProgressMonitor() );
  }

  /**
   * Deletes all input files, which have been copied so far.
   */
  private void deleteInputFiles( ) throws FileSystemException
  {
    if( m_serverTmpDirectory == null )
      return;

    if( m_serverTmpDirectory.exists() )
      VFSUtilities.deleteFiles( m_serverTmpDirectory );
  }

  /**
   * This function is called, when the job is canceled by the user.<br>
   * A cancel request has to be send to the server, so that he can stop executing the simulation.
   */
  private IStatus canceled( )
  {
    /* TODO Tell the server, that the job should be canceled. */

    /* TODO Delete all input files with deleteInputFiles(). */
    /* This is omitted now, because the server isn't told, to cancel its operation, so it is likely, */
    /* he will run in an error then, if we delete the files now. */
    return Status.CANCEL_STATUS;
  }

  /**
   * This function will finish the operations on this simulation.
   * <ol>
   * <li>All input files will be deleted.</li>
   * <li>Other things can be disposed here.</li>
   * </ol>
   */
  private void finish( )
  {
    try
    {
      /* Delete the input files. */
      deleteInputFiles();
    }
    catch( FileSystemException e )
    {
      /* If an error has occured while deleting the files, finish the job without error. A warning should be ok. */
      // TODO
    }
  }

  /**
   * This function will create an error string, containing all error messages.
   * 
   * @param exceptionReport
   *          The exception report.
   * @return The error messages as one string.
   */
  private String createErrorString( ExceptionReport exceptionReport )
  {
    List<ExceptionType> exceptions = exceptionReport.getException();
    String messages = "";
    for( ExceptionType exception : exceptions )
      messages = messages + "Code: " + exception.getExceptionCode() + "\nMessage: " + exception.getExceptionText() + "\nLocator: " + exception.getLocator();

    return messages;
  }
}