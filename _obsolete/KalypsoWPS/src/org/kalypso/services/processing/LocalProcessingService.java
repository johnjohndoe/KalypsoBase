/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.services.processing;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBException;

import net.opengeospatial.ows.CodeType;
import net.opengeospatial.ows.ExceptionReport;
import net.opengeospatial.ows.ExceptionType;
import net.opengeospatial.ows.GetCapabilitiesType;
import net.opengeospatial.ows.OnlineResourceType;
import net.opengeospatial.ows.ServiceIdentification;
import net.opengeospatial.ows.ServiceProvider;
import net.opengeospatial.wps.Capabilities;
import net.opengeospatial.wps.ComplexValueType;
import net.opengeospatial.wps.DataInputsType;
import net.opengeospatial.wps.DescribeProcess;
import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.ExecuteResponseType;
import net.opengeospatial.wps.IOValueType;
import net.opengeospatial.wps.InputDescriptionType;
import net.opengeospatial.wps.ProcessBriefType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessDescriptions;
import net.opengeospatial.wps.ProcessFailedType;
import net.opengeospatial.wps.ProcessOfferings;
import net.opengeospatial.wps.ProcessStartedType;
import net.opengeospatial.wps.StatusType;
import net.opengeospatial.wps.SupportedComplexDataType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.apache.commons.io.IOUtils;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.services.common.ServiceConfig;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.ISimulationConstants;
import org.kalypso.simulation.core.KalypsoSimulationCoreJaxb;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.SimulationInfo;
import org.kalypso.simulation.core.simspec.DataType;
import org.kalypso.simulation.core.simspec.Modelspec;

/**
 * This class wraps an instance of {@link ISimulation} to provide an {@link IWebProcessingService}
 * 
 * @author skurzbach
 */
public class LocalProcessingService implements IWebProcessingService
{
  private final ISimulation m_simulation;

  private final File m_tmpDir;

  private Modelspec m_data;

  private final String m_title;

  private final String m_simulationId;

  public LocalProcessingService( final String simulationId, final ISimulation simulation, final String title ) throws JAXBException
  {
    m_simulationId = simulationId;
    m_simulation = simulation;
    m_title = title;
    m_tmpDir = FileUtilities.createNewTempDir( "TemporaryProcessInputs", ServiceConfig.getTempDir() );
    m_tmpDir.deleteOnExit();
    final URL modelspecURL = m_simulation.getSpezifikation();
    // get modelspec
    m_data = (Modelspec) KalypsoSimulationCoreJaxb.JC.createUnmarshaller().unmarshal( modelspecURL );
  }

  /**
   * @see org.kalypso.services.processing.IWebProcessingService#getCapabilities(net.opengeospatial.wps.GetCapabilities)
   */
  public Capabilities getCapabilities( final GetCapabilitiesType capabilitiesRequest )
  {
    final Capabilities capabilities = new Capabilities();
    final ProcessOfferings processOfferings = new ProcessOfferings();
    final ProcessBriefType processBriefType = new ProcessBriefType();
    final CodeType processID = new CodeType();
    processID.setValue( getSimulationId() );
    processBriefType.setIdentifier( processID );
    processBriefType.setTitle( m_title );
    processOfferings.getProcess().add( processBriefType );
    capabilities.setProcessOfferings( processOfferings );
    final ServiceIdentification serviceIdentification = new ServiceIdentification();
    serviceIdentification.setTitle( "Local Simulation Service" );
    capabilities.setServiceIdentification( serviceIdentification );
    final ServiceProvider serviceProvider = new ServiceProvider();
    serviceProvider.setProviderName( "Kalypso" );
    final OnlineResourceType onlineResourceType = new OnlineResourceType();
    onlineResourceType.setHref( "localhost" );
    serviceProvider.setProviderSite( onlineResourceType );
    capabilities.setServiceProvider( serviceProvider );

    return capabilities;
  }

  /**
   * @see org.kalypso.services.processing.IWebProcessingService#describeProcess(net.opengeospatial.wps.DescribeProcess)
   */
  public ProcessDescriptions describeProcess( final DescribeProcess describeProcessRequest )
  {
    final ProcessDescriptions processDescriptions = new ProcessDescriptions();
    for( final CodeType processId : describeProcessRequest.getIdentifier() )
    {
      final ProcessDescriptionType processDescriptionType = new ProcessDescriptionType();
      processDescriptionType.setIdentifier( processId );
      processDescriptionType.setTitle( "Title" );
      processDescriptionType.setAbstract( "Abstract" );
      processDescriptionType.setProcessVersion( "Version" );

      final DataInputs dataInputs = new DataInputs();
      List<InputDescriptionType> input = dataInputs.getInput();
      final List<DataType> requiredInput = m_data.getInput();
      for( final DataType bean : requiredInput )
      {
        final InputDescriptionType inputDescriptionType = new InputDescriptionType();
        final CodeType inputID = new CodeType();
        inputID.setValue( bean.getId() );
        inputDescriptionType.setIdentifier( inputID );
        inputDescriptionType.setTitle( bean.getDescription() );
        inputDescriptionType.setAbstract( "Abstract" );
        final SupportedComplexDataType supportedComplexDataType = new SupportedComplexDataType();
        supportedComplexDataType.setDefaultSchema( bean.getType().toString() );
        inputDescriptionType.setComplexData( supportedComplexDataType );
        input.add( inputDescriptionType );
      }

      processDescriptionType.setDataInputs( dataInputs );

      processDescriptionType.setProcessOutputs( new ProcessOutputs() );
      processDescriptions.getProcessDescription().add( processDescriptionType );
    }
    return processDescriptions;
  }

  /**
   * @see org.kalypso.services.processing.IWebProcessingService#execute(net.opengeospatial.wps.Execute)
   */
  public Object execute( final Execute executeRequest )
  {
    final CodeType processId = executeRequest.getIdentifier();
    final DataInputsType dataInputs = executeRequest.getDataInputs();
    final String tempFileName = FileUtilities.validateName( "___" + executeRequest.getIdentifier(), "-" );
    final List<SimulationDataPath> simulationInputList = new ArrayList<SimulationDataPath>();
    File zipFile = null;
    SimulationInfo simulationInfo = null;
    try
    {
      zipFile = File.createTempFile( tempFileName, ".wps", m_tmpDir );
      final ZipOutputStream zos = new ZipOutputStream( new BufferedOutputStream( new FileOutputStream( zipFile ) ) );

      final List<IOValueType> inputList = dataInputs.getInput();
      for( final IOValueType input : inputList )
      {
        final String inputId = input.getIdentifier().getValue();
        simulationInputList.add( new SimulationDataPath( inputId, inputId ) );
        writeZipEntry( zos, input );
      }
    }
    catch( IOException e )
    {
      simulationInfo = new SimulationInfo( processId.getValue(), "Description", "Unknown Type", ISimulationConstants.STATE.ERROR, 0, "Could not gather data. Reason: " + e.getLocalizedMessage() );
      e.printStackTrace();
    }

    final SimulationDataPath[] simulationInputs = simulationInputList.toArray( new SimulationDataPath[simulationInputList.size()] );
    final SimulationDataPath[] simulationOutputs = null; // TODO: how to get these here??? no Modeldata is available

    if( simulationInfo == null )
    {
      final DataHandler dataHandler = new DataHandler( new FileDataSource( zipFile ) );
      // try
      // {
      // simulationInfo = m_simulation.startJob( processId.getValue(), "Description", dataHandler, simulationInputs,
      // simulationOutputs );
      // }
      // catch( SimulationException e )
      // {
      // e.printStackTrace();
      // simulationInfo = new SimulationInfo( processId.getValue(), "Description", "Unknown Type",
      // ISimulationConstants.STATE.ERROR, 0, "Simulation error. Reason: " + e.getLocalizedMessage() );
      // }
    }

    final String finishText = simulationInfo.getFinishText();
    final BigInteger progress = new BigInteger( Integer.toString( simulationInfo.getProgress() ) );

    final ProcessStartedType processStartedType = new ProcessStartedType();
    processStartedType.setPercentCompleted( progress );

    final ProcessFailedType processFailedType = new ProcessFailedType();
    final ExceptionReport exceptionReport = new ExceptionReport();
    processFailedType.setExceptionReport( exceptionReport );
    final ExceptionType exceptionType = new ExceptionType();
    exceptionType.getExceptionText().add( finishText );
    exceptionReport.getException().add( exceptionType );

    final StatusType status = new StatusType();
    switch( simulationInfo.getState() )
    {
      case UNKNOWN:
      case WAITING:
        status.setProcessAccepted( simulationInfo.getState().toString() );
        break;
      case RUNNING:
        status.setProcessStarted( processStartedType );
        break;
      case FINISHED:
        status.setProcessSucceeded( finishText );
        break;
      case CANCELED:
      case ERROR:
        status.setProcessFailed( processFailedType );
    }

    final ExecuteResponseType executeResponse = new ExecuteResponseType();
    executeResponse.setIdentifier( processId );
    executeResponse.setVersion( executeRequest.getVersion() );
    executeResponse.setDataInputs( dataInputs );
    executeResponse.setStatus( status );
    executeResponse.setStatusLocation( "Status location URL" ); // TODO
    executeResponse.setOutputDefinitions( executeRequest.getOutputDefinitions() );
    final ExecuteResponseType.ProcessOutputs processOutputs = new SimulationInfoWrapper( simulationInfo );
    executeResponse.setProcessOutputs( processOutputs );
    return executeResponse;
  }

  public String getSimulationId( )
  {
    return m_simulationId;
  }

  private static void writeZipEntry( final ZipOutputStream zos, final IOValueType input ) throws IOException, MalformedURLException
  {
    final ComplexValueReference complexValueReference = input.getComplexValueReference();
    final ComplexValueType complexValue = input.getComplexValue();
    final ZipEntry newEntry = new ZipEntry( input.getIdentifier().getValue() );
    zos.putNextEntry( newEntry );
    final InputStream contentStream;
    if( complexValueReference != null )
    {
      contentStream = new BufferedInputStream( new URL( complexValueReference.getReference() ).openStream() );
    }
    else
    {
      contentStream = new BufferedInputStream( new ByteArrayInputStream( ((String) complexValue.getContent().get( 0 )).getBytes() ) );
    }
    try
    {
      IOUtils.copy( contentStream, zos );
    }
    finally
    {
      IOUtils.closeQuietly( contentStream );
    }
    zos.closeEntry();
  }

  /**
   * Wraps a SimulationInfo to provide current results
   * 
   * @author skurzbach
   */
  private final class SimulationInfoWrapper extends ExecuteResponseType.ProcessOutputs
  {
    private final SimulationInfo m_info;

    public SimulationInfoWrapper( final SimulationInfo simulationInfo )
    {
      m_info = simulationInfo;
    }

    /**
     * @see net.opengeospatial.wps.ExecuteResponseType.ProcessOutputs#getOutput()
     */
    @Override
    public List<IOValueType> getOutput( )
    {
      final List<IOValueType> outputs = new ArrayList<IOValueType>();
      for( final String result : m_info.getCurrentResults() )
      {
        final IOValueType valueType = new IOValueType();
        final CodeType codeType = new CodeType();
        codeType.setValue( result );
        // TODO: where are the current results?
        valueType.setIdentifier( codeType );
        outputs.add( valueType );
      }
      return outputs;
    }
  }

}
