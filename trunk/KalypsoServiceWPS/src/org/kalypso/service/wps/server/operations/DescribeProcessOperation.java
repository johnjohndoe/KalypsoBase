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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import net.opengeospatial.ows.CodeType;
import net.opengeospatial.wps.ComplexDataType;
import net.opengeospatial.wps.InputDescriptionType;
import net.opengeospatial.wps.LiteralInputType;
import net.opengeospatial.wps.OutputDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.SupportedComplexDataType;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.ogc.DescribeProcessMediator;
import org.kalypso.service.wps.utils.ogc.ProcessDescriptionMediator;
import org.kalypso.service.wps.utils.ogc.WPS040ObjectFactoryUtilities;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.KalypsoSimulationCoreJaxb;
import org.kalypso.simulation.core.simspec.DataType;
import org.kalypso.simulation.core.simspec.Modelspec;

/**
 * This operation will describe a process.
 * 
 * @author Holger Albert
 */
public class DescribeProcessOperation implements IOperation
{
  /**
   * The describe process request. If this variable is set, the describe process request came via xml. Otherwise it was
   * only a get.
   */
  private Object m_describeProcessRequest = null;

  /**
   * The constructor.
   */
  public DescribeProcessOperation( )
  {
  }

  /**
   * @see org.kalypso.service.wps.operations.IOperation#executeOperation(org.kalypso.service.ogc.RequestBean)
   */
  public StringBuffer executeOperation( final RequestBean request ) throws OWSException
  {
    try
    {
      /* Start the operation. */
      Debug.println( "Operation \"DescribeProcess\" started." );


      /* Get the identifiers out of the request. */
      final List<String> identifiers = getIdentifier( request );

      /* Build the process descriptions response. */
      final ProcessDescriptionMediator processDescriptionMediator = new ProcessDescriptionMediator( m_describeProcessRequest );
      final Object processDescriptionsResponse = processDescriptionMediator.getProcessDescriptions( identifiers );

      /* Marshall it into one XML string. */
      final String xml = processDescriptionMediator.marshall( processDescriptionsResponse );

      /* Build the response. */
      final StringBuffer response = new StringBuffer();
      response.append( xml );

      return response;
    }
    catch( final Exception e )
    {
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e, "" );
    }
  }

  /**
   * Checks for the parameter or attribute Identifier and returns it. Furthermore it sets the member m_xxxRequest.
   * 
   * @param request
   *          The request.
   * @return A list with Identifier parameter or null if not present.
   */
  private List<String> getIdentifier( final RequestBean request ) throws OWSException
  {
    final List<String> simulationTypes;
    if( request.isPost() )
    {
      try
      {
        /* POST with XML. */
        final String xml = request.getBody();

        /* Check if ALL parameter are available. */
        m_describeProcessRequest = MarshallUtilities.unmarshall( xml );

        final DescribeProcessMediator describeProcessMediator = new DescribeProcessMediator( m_describeProcessRequest );

        /* Need the XML attribute service. */
        simulationTypes = describeProcessMediator.getProcessIdentifiers();
      }
      catch( final JAXBException e )
      {
        throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getLocalizedMessage(), "" );
      }
    }
    else
    {
      /* GET or simple POST. */
      simulationTypes = new ArrayList<String>();

      /* Search for the parameter Identifier. */
      final String parameterValue = request.getParameterValue( "Identifier" );
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

  /**
   * This function builds the process description for one simulation.
   * 
   * @param simulation
   *          The simulation
   * @param identifier
   *          The identifier of the simulation.
   * @return The process description.
   * @deprecated Use {@link ProcessDescriptionMediator#getProcessDescription(String)} instead
   */
  public static ProcessDescriptionType buildProcessDescriptionType( final String typeID ) throws OWSException
  {
    try
    {
      /* Get the Simulation.. */
      final ISimulation simulation = WPSUtilities.getSimulation( typeID );

      // TODO At the moment only literals and references are supported from our model spec.
      // The spec must be changed, in order to enable bounding boxes and complex values.
      // If this is done, the two new cases must be covered here as well.

      /* Get the specification for that simulation. */
      final URL spezifikation = simulation.getSpezifikation();
      final Modelspec modelData = (Modelspec) KalypsoSimulationCoreJaxb.JC.createUnmarshaller().unmarshal( spezifikation );

      /* Build all content for the process description. */
      final String identifier = modelData.getTypeID();
      final CodeType code = WPS040ObjectFactoryUtilities.buildCodeType( "", identifier );
      final String title = identifier;

      /* Get the input from the model spec. */
      final List<DataType> input = modelData.getInput();

      /* Build the data inputs. */
      final List<InputDescriptionType> inputDescriptions = new LinkedList<InputDescriptionType>();
      for( int j = 0; j < input.size(); j++ )
      {
        final DataType data = input.get( j );
        final CodeType inputCode = WPS040ObjectFactoryUtilities.buildCodeType( "", data.getId() );
        final String inputTitle = data.getId();
        final String inputAbstrakt = data.getDescription();

        InputDescriptionType inputDescription = null;
        final QName type = data.getType();
        if( !type.equals( WPSUtilities.QNAME_ANY_URI ) )
        {
          /* Literal. */
          final LiteralInputType inputFormChoice = WPS040ObjectFactoryUtilities.buildLiteralInputType( WPS040ObjectFactoryUtilities.buildDomainMetadataType( type.getLocalPart(), null ), null, WPS040ObjectFactoryUtilities.buildAnyValue(), null );
          if( data.isOptional() )
            inputDescription = WPS040ObjectFactoryUtilities.buildInputDescriptionType( inputCode, inputTitle, inputAbstrakt, inputFormChoice, 0 );
          else
            inputDescription = WPS040ObjectFactoryUtilities.buildInputDescriptionType( inputCode, inputTitle, inputAbstrakt, inputFormChoice, 1 );
        }
        else
        {
          /* Reference. */
          final ComplexDataType complexData = WPS040ObjectFactoryUtilities.buildComplexDataType( "", "", "" );
          final List<ComplexDataType> complexDatas = new LinkedList<ComplexDataType>();
          complexDatas.add( complexData );

          final SupportedComplexDataType inputFormChoice = WPS040ObjectFactoryUtilities.buildSupportedComplexDataType( complexDatas, null, null, null );
          if( data.isOptional() )
            inputDescription = WPS040ObjectFactoryUtilities.buildInputDescriptionType( inputCode, inputTitle, inputAbstrakt, inputFormChoice, 0 );
          else
            inputDescription = WPS040ObjectFactoryUtilities.buildInputDescriptionType( inputCode, inputTitle, inputAbstrakt, inputFormChoice, 1 );
        }

        /* Füge die neue Beschreibung hinzu. */
        inputDescriptions.add( inputDescription );
      }

      final DataInputs dataInputs = WPS040ObjectFactoryUtilities.buildDataInputs( inputDescriptions );

      /* Get the output from the model spec. */
      final List<DataType> output = modelData.getOutput();

      /* Build the output descriptions. */
      final List<OutputDescriptionType> outputDescriptions = new LinkedList<OutputDescriptionType>();
      for( int j = 0; j < output.size(); j++ )
      {
        final DataType data = output.get( j );

        final CodeType outputCode = WPS040ObjectFactoryUtilities.buildCodeType( "", data.getId() );
        final String outputTitle = data.getId();
        final String outputAbstrakt = data.getDescription();

        OutputDescriptionType outputDescription = null;
        if( !data.getType().equals( WPSUtilities.QNAME_ANY_URI ) )
        {
          /* Literal. */
          outputDescription = WPS040ObjectFactoryUtilities.buildOutputDescriptionType( outputCode, outputTitle, outputAbstrakt, WPS040ObjectFactoryUtilities.buildLiteralOutputType( WPS040ObjectFactoryUtilities.buildDomainMetadataType( data.getType().getLocalPart(), null ), null ) );
        }
        else
        {
          /* Reference. */
          final ComplexDataType complexData = WPS040ObjectFactoryUtilities.buildComplexDataType( "", "", "" );
          final List<ComplexDataType> complexDatas = new LinkedList<ComplexDataType>();
          complexDatas.add( complexData );

          final SupportedComplexDataType outputFormChoice = WPS040ObjectFactoryUtilities.buildSupportedComplexDataType( complexDatas, null, null, null );
          outputDescription = WPS040ObjectFactoryUtilities.buildOutputDescriptionType( outputCode, outputTitle, outputAbstrakt, outputFormChoice );
        }

        /* Füge die neue Beschreibung hinzu. */
        outputDescriptions.add( outputDescription );
      }

      final ProcessOutputs processOutputs = WPS040ObjectFactoryUtilities.buildProcessDescriptionTypeProcessOutputs( outputDescriptions );

      return WPS040ObjectFactoryUtilities.buildProcessDescriptionType( code, title, "", null, null, dataInputs, processOutputs, true, true );
    }
    catch( final Exception e )
    {
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e, "Problem building process description for " + typeID );
    }
  }
}