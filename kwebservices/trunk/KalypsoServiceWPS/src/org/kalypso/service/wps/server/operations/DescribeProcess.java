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
package org.kalypso.service.wps.server.operations;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.opengeospatial.ows.CodeType;
import net.opengeospatial.wps.ComplexDataType;
import net.opengeospatial.wps.InputDescriptionType;
import net.opengeospatial.wps.LiteralInputType;
import net.opengeospatial.wps.OutputDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessDescriptions;
import net.opengeospatial.wps.SupportedComplexDataType;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.OGCUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.KalypsoSimulationCoreJaxb;
import org.kalypso.simulation.core.simspec.DataType;
import org.kalypso.simulation.core.simspec.Modelspec;

/**
 * This operation will describe a processF.
 * 
 * @author Holger Albert
 */
public class DescribeProcess implements IOperation
{
  /**
   * The describe process request. If this variable is set, the describe process request came via xml. Otherwise it was
   * only a get.
   */
  private net.opengeospatial.wps.DescribeProcess m_describeProcessRequest = null;

  /**
   * The constructor.
   */
  public DescribeProcess( )
  {
  }

  /**
   * @see org.kalypso.service.wps.operations.IOperation#executeOperation(org.kalypso.service.ogc.RequestBean)
   */
  public StringBuffer executeOperation( RequestBean request ) throws OWSException
  {
    try
    {
      /* Start the operation. */
      Debug.println( "Operation \"DescribeProcess\" started." );

      /* Get the simulation. */
      List<ISimulation> simulations = new LinkedList<ISimulation>();

      /* Get the identifiers out of the request. */
      List<String> identifier = getIdentifier( request );

      /* Collect all simulations asked for. */
      Debug.println( "Collect the simulations ..." );
      for( int i = 0; i < identifier.size(); i++ )
        simulations.add( WPSUtilities.getSimulation( identifier.get( i ) ) );

      /* Build the process description for all simulations. */
      /* It is vital, that the two lists have the same order and length. */
      List<ProcessDescriptionType> processDescriptions = new LinkedList<ProcessDescriptionType>();
      for( int i = 0; i < simulations.size(); i++ )
      {
        /* This is the simulation. */
        ISimulation simulation = simulations.get( i );

        /* Build all content for the process description. */
        CodeType code = OGCUtilities.buildCodeType( "", identifier.get( i ) );
        String title = identifier.get( i );

        /* Get the specification for that simulation. */
        URL spezifikation = simulation.getSpezifikation();
        Modelspec modelData;
        modelData = (Modelspec) KalypsoSimulationCoreJaxb.JC.createUnmarshaller().unmarshal( spezifikation );

        /* Get the input from the model spec. */
        List<DataType> input = modelData.getInput();

        /* Build the data inputs. */
        List<InputDescriptionType> inputDescriptions = new LinkedList<InputDescriptionType>();
        for( int j = 0; j < input.size(); j++ )
        {
          DataType data = input.get( i );
          CodeType inputCode = OGCUtilities.buildCodeType( "", data.getId() );
          String inputTitle = data.getId();
          String inputAbstrakt = data.getDescription();

          InputDescriptionType inputDescription = null;
          if( !data.getType().equals( WPSUtilities.QNAME_ANY_URI ) )
          {
            /* Literal. */
            LiteralInputType inputFormChoice = OGCUtilities.buildLiteralInputType( OGCUtilities.buildDomainMetadata( "Any", null ), null, OGCUtilities.buildAnyValue(), null );
            if( data.isOptional() )
              inputDescription = OGCUtilities.buildInputDescriptionType( inputCode, inputTitle, inputAbstrakt, inputFormChoice, 0 );
            else
              inputDescription = OGCUtilities.buildInputDescriptionType( inputCode, inputTitle, inputAbstrakt, inputFormChoice, 1 );
          }
          else
          {
            /* Reference. */
            ComplexDataType complexData = OGCUtilities.buildComplexDataType( "", "", "" );
            List<ComplexDataType> complexDatas = new LinkedList<ComplexDataType>();
            complexDatas.add( complexData );

            SupportedComplexDataType inputFormChoice = OGCUtilities.buildSupportedComplexDataType( complexDatas, null, null, null );
            if( data.isOptional() )
              inputDescription = OGCUtilities.buildInputDescriptionType( inputCode, inputTitle, inputAbstrakt, inputFormChoice, 0 );
            else
              inputDescription = OGCUtilities.buildInputDescriptionType( inputCode, inputTitle, inputAbstrakt, inputFormChoice, 1 );
          }

          /* Füge die neue Beschreibung hinzu. */
          inputDescriptions.add( inputDescription );
        }
        DataInputs dataInputs = OGCUtilities.buildDataInputs( inputDescriptions );

        /* Get the output from the model spec. */
        List<DataType> output = modelData.getOutput();

        /* Build the output descriptions. */
        List<OutputDescriptionType> outputDescriptions = new LinkedList<OutputDescriptionType>();
        for( int j = 0; j < output.size(); j++ )
        {
          DataType data = output.get( i );

          CodeType outputCode = OGCUtilities.buildCodeType( "", data.getId() );
          String outputTitle = data.getId();
          String outputAbstrakt = data.getDescription();

          OutputDescriptionType outputDescription = null;
          if( !data.getType().equals( WPSUtilities.QNAME_ANY_URI ) )
          {
            /* Literal. */
            outputDescription = OGCUtilities.buildOutputDescriptionType( outputCode, outputTitle, outputAbstrakt, OGCUtilities.buildLiteralOutputType( OGCUtilities.buildDomainMetadata( "Any", null ), null ) );
          }
          else
          {
            /* Reference. */
            ComplexDataType complexData = OGCUtilities.buildComplexDataType( "", "", "" );
            List<ComplexDataType> complexDatas = new LinkedList<ComplexDataType>();
            complexDatas.add( complexData );

            outputDescription = OGCUtilities.buildOutputDescriptionType( outputCode, outputTitle, outputAbstrakt, complexDatas );
          }

          /* Füge die neue Beschreibung hinzu. */
          outputDescriptions.add( outputDescription );
        }
        ProcessOutputs processOutputs = OGCUtilities.buildProcessOutputs( outputDescriptions );

        ProcessDescriptionType processDescription = OGCUtilities.buildProcessDescriptionType( code, title, "", null, null, dataInputs, processOutputs, true, true );

        /* Save the process description. */
        processDescriptions.add( processDescription );
      }

      /* Build the process descriptions response. */
      ProcessDescriptions processDescriptionsResponse = OGCUtilities.buildProcessDescriptions( processDescriptions );

      String xml = MarshallUtilities.marshall( processDescriptionsResponse );

      StringBuffer response = new StringBuffer();
      response.append( xml );

      return response;
    }
    catch( Exception e )
    {
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getMessage(), "" );
    }
  }

  /**
   * Checks for the parameter or attribute Identifier and returns it. Furthermore it sets the member m_xxxRequest.
   * 
   * @param request
   *          The request.
   * @return A list with Identifier parameter or null if not present.
   */
  private List<String> getIdentifier( RequestBean request ) throws OWSException
  {
    List<String> simulationTypes = new LinkedList<String>();
    if( request.isPost() )
    {
      try
      {
        /* POST with XML. */
        String xml = request.getBody();

        /* Check if ALL parameter are available. */
        m_describeProcessRequest = (net.opengeospatial.wps.DescribeProcess) MarshallUtilities.unmarshall( xml );

        /* Need the XML attribute service. */
        List<CodeType> identifier = m_describeProcessRequest.getIdentifier();

        if( identifier != null )
        {
          for( int i = 0; i < identifier.size(); i++ )
          {
            String value = identifier.get( i ).getValue();
            if( value != null )
              simulationTypes.add( value );
          }
        }
      }
      catch( JAXBException e )
      {
        throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getMessage(), "" );
      }
    }
    else
    {
      /* GET or simple POST. */

      /* Search for the parameter Identifier. */
      String parameterValue = request.getParameterValue( "Identifier" );
      if( parameterValue != null )
        simulationTypes.add( parameterValue );
    }

    if( simulationTypes.size() == 0 )
    {
      Debug.println( "Missing parameter Identifier!" );
      throw new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Parameter Identifier is missing ...", "Identifier" );
    }

    return simulationTypes;
  }
}