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
package org.kalypso.services.calculation.test;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import net.opengeospatial.ows.AnyValue;
import net.opengeospatial.ows.CodeType;
import net.opengeospatial.ows.DCP;
import net.opengeospatial.ows.GetCapabilitiesType;
import net.opengeospatial.ows.HTTP;
import net.opengeospatial.ows.Operation;
import net.opengeospatial.ows.OperationsMetadata;
import net.opengeospatial.ows.RequestMethodType;
import net.opengeospatial.ows.ServiceIdentification;
import net.opengeospatial.ows.ServiceProvider;
import net.opengeospatial.wps.Capabilities;
import net.opengeospatial.wps.DescribeProcess;
import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.ExecuteResponseType;
import net.opengeospatial.wps.InputDescriptionType;
import net.opengeospatial.wps.LiteralInputType;
import net.opengeospatial.wps.LiteralOutputType;
import net.opengeospatial.wps.OutputDescriptionType;
import net.opengeospatial.wps.ProcessBriefType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessDescriptions;
import net.opengeospatial.wps.ProcessOfferings;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.kalypso.services.processing.IWebProcessingService;

/**
 * @author skurzbach
 */
public class DummyProcessService implements IWebProcessingService
{

  private static final CodeType PROCESS_ID = new CodeType();

  public DummyProcessService( )
  {
    PROCESS_ID.setValue( "ProcessID" );
  }

  /**
   * @see org.kalypso.services.calculation.IProcessService#describeProcess(net.opengeospatial.wps.DescribeProcess)
   */
  public ProcessDescriptions describeProcess( final DescribeProcess describeProcessRequest )
  {
    final ProcessDescriptionType processDescription = new ProcessDescriptionType();
    final DataInputs dataInputs = new DataInputs();
    final InputDescriptionType inputDescription = new InputDescriptionType();
    inputDescription.setTitle( "InputTitle" );
    final CodeType inputCodeType = new CodeType();
    inputCodeType.setValue( "InputID" );
    inputDescription.setIdentifier( inputCodeType );
    final LiteralInputType literalInputType = new LiteralInputType();
    literalInputType.setAnyValue( new AnyValue() );
    inputDescription.setLiteralData( literalInputType );
    dataInputs.getInput().add( inputDescription );
    processDescription.setDataInputs( dataInputs );

    final ProcessOutputs processOutputs = new ProcessOutputs();
    final OutputDescriptionType outputDescriptionType = new OutputDescriptionType();
    outputDescriptionType.setTitle( "OutputTitle" );
    final CodeType outputCodeType = new CodeType();
    outputCodeType.setValue( "OutputID" );
    outputDescriptionType.setIdentifier( outputCodeType );
    final LiteralOutputType literalOutputType = new LiteralOutputType();
    outputDescriptionType.setLiteralOutput( literalOutputType );
    processOutputs.getOutput().add( outputDescriptionType );
    processDescription.setProcessOutputs( processOutputs );
    processDescription.setIdentifier( PROCESS_ID );

    final ProcessDescriptions processDescriptions = new ProcessDescriptions();
    processDescriptions.getProcessDescription().add( processDescription );
    return processDescriptions;
  }

  /**
   * @see org.kalypso.services.calculation.IProcessService#execute(net.opengeospatial.wps.Execute)
   */
  public ExecuteResponseType execute( final Execute executeRequest )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypso.services.calculation.IProcessService#getCapabilities(net.opengeospatial.ows.GetCapabilitiesType)
   */
  public Capabilities getCapabilities( final GetCapabilitiesType capabilitiesRequest )
  {
    final Capabilities capabilities = new Capabilities();
    final ProcessOfferings processOfferings = new ProcessOfferings();
    final ProcessBriefType processBriefType = new ProcessBriefType();
    processBriefType.setIdentifier( PROCESS_ID );
    processBriefType.setTitle( "ProcessTitle" );
    processOfferings.getProcess().add( processBriefType );
    capabilities.setProcessOfferings( processOfferings );
    final ServiceIdentification serviceIdentification = new ServiceIdentification();
    serviceIdentification.setTitle( "ServiceTitle" );
    capabilities.setServiceIdentification( serviceIdentification );
    final ServiceProvider serviceProvider = new ServiceProvider();
    serviceProvider.setProviderName( "ServiceProviderName" );
    capabilities.setServiceProvider( serviceProvider );
    final OperationsMetadata operationsMetadata = new OperationsMetadata();
    final List<Operation> operations = operationsMetadata.getOperation();
    final Operation operation = new Operation();
    final DCP dcp = new DCP();
    final HTTP http = new HTTP();
    final RequestMethodType requestMethodType = new RequestMethodType();
    requestMethodType.setHref( "aURL" );
    http.getGetOrPost().add( new JAXBElement<RequestMethodType>( new QName( "http://www.opengeospatial.net/ows", "Get" ), RequestMethodType.class, requestMethodType ) );
    dcp.setHTTP( http );
    operation.getDCP().add( dcp );
    operations.add( operation );
    capabilities.setOperationsMetadata( operationsMetadata );
    capabilities.setVersion( "0.0.0" );
    return capabilities;
  }
}
