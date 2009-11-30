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

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import net.opengeospatial.ows.AnyValue;
import net.opengeospatial.ows.CodeType;
import net.opengeospatial.ows.DomainMetadataType;
import net.opengeospatial.wps.ComplexDataType;
import net.opengeospatial.wps.InputDescriptionType;
import net.opengeospatial.wps.LiteralInputType;
import net.opengeospatial.wps.OutputDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.SupportedComplexDataType;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaCatalog;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.KalypsoGMLSchemaPlugin;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ISimpleMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.i18n.Messages;
import org.kalypso.service.wps.internal.KalypsoServiceWPSDebug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.ogc.DescribeProcessMediator;
import org.kalypso.service.wps.utils.ogc.ProcessDescriptionMediator;
import org.kalypso.service.wps.utils.ogc.WPS040ObjectFactoryUtilities;
import org.kalypso.service.wps.utils.simulation.WPSSimulationDataProvider;
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
  private String m_version = null;

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
      KalypsoServiceWPSDebug.DEBUG.printf( "Operation \"DescribeProcess\" started.\n" ); //$NON-NLS-1$

      /* Get the identifiers out of the request. */
      final List<String> identifiers = getIdentifier( request );

      /* Build the process descriptions response. */
      final ProcessDescriptionMediator processDescriptionMediator = new ProcessDescriptionMediator( m_version );
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
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e, "" ); //$NON-NLS-1$
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
        final String xml = request.getBody();
        final Object describeProcessRequest = MarshallUtilities.unmarshall( xml );
        final DescribeProcessMediator describeProcessMediator = new DescribeProcessMediator( describeProcessRequest );
        simulationTypes = describeProcessMediator.getProcessIdentifiers();

        m_version = describeProcessMediator.getVersion().toString();

      }
      catch( final JAXBException e )
      {
        throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e, "" ); //$NON-NLS-1$
      }
    }
    else
    {
      /* GET or simple POST. */
      simulationTypes = new ArrayList<String>();

      /* Search for the parameter Identifier. */
      final String parameterValue = request.getParameterValue( "Identifier" ); //$NON-NLS-1$
      if( parameterValue != null )
      {
        simulationTypes.add( parameterValue );
      }

      m_version = request.getParameterValue( "Version" ); //$NON-NLS-1$
    }

    if( simulationTypes.size() == 0 )
    {
      KalypsoServiceWPSDebug.DEBUG.printf( "Missing parameter Identifier!\n" ); //$NON-NLS-1$
      throw new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, Messages.getString( "org.kalypso.service.wps.server.operations.DescribeProcessOperation.0" ), "Identifier" ); //$NON-NLS-1$ //$NON-NLS-2$
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
  @Deprecated
  public static ProcessDescriptionType buildProcessDescriptionType( final String typeID ) throws CoreException
  {
    /* Get the Simulation.. */
    final ISimulation simulation = WPSUtilities.getSimulation( typeID );

    // TODO At the moment only literals and references are supported from our model spec.
    // The spec must be changed, in order to enable bounding boxes and complex values.
    // If this is done, the two new cases must be covered here as well.

    /* Get the specification for that simulation. */
    final URL spezifikation = simulation.getSpezifikation();
    Modelspec modelData;
    try
    {
      modelData = (Modelspec) KalypsoSimulationCoreJaxb.JC.createUnmarshaller().unmarshal( spezifikation );
    }
    catch( final JAXBException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }

    /* Build all content for the process description. */
    final String identifier = modelData.getTypeID();
    final CodeType code = WPS040ObjectFactoryUtilities.buildCodeType( "", identifier ); //$NON-NLS-1$
    final String title = identifier;

    /* Get the input from the model spec. */
    final List<DataType> input = modelData.getInput();

    /* Build the data inputs. */
    final List<InputDescriptionType> inputDescriptions = new LinkedList<InputDescriptionType>();
    final String gmlVersion = "3.1.1"; //$NON-NLS-1$
    for( int j = 0; j < input.size(); j++ )
    {
      final DataType data = input.get( j );
      final CodeType inputCode = WPS040ObjectFactoryUtilities.buildCodeType( "", data.getId() ); //$NON-NLS-1$
      final String inputTitle = data.getId();
      final String inputAbstrakt = data.getDescription();
      final int minOccurs = data.isOptional() ? 0 : 1;

      InputDescriptionType inputDescription = null;
      final QName type = data.getType();
      final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
      final IMarshallingTypeHandler handler = typeRegistry.getTypeHandlerForTypeName( type );
      final Object inputFormChoice;
      if( !type.equals( WPSUtilities.QNAME_ANY_URI ) && handler instanceof ISimpleMarshallingTypeHandler )
      {
        inputFormChoice = getLiteralData( type );
      }
      else
      {
        inputFormChoice = getComplexData( gmlVersion, type );
      }
      inputDescription = WPS040ObjectFactoryUtilities.buildInputDescriptionType( inputCode, inputTitle, inputAbstrakt, inputFormChoice, minOccurs );

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

      final CodeType outputCode = WPS040ObjectFactoryUtilities.buildCodeType( "", data.getId() ); //$NON-NLS-1$
      final String outputTitle = data.getId();
      final String outputAbstrakt = data.getDescription();

      OutputDescriptionType outputDescription = null;
      final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
      final QName type = data.getType();
      final IMarshallingTypeHandler handler = typeRegistry.getTypeHandlerForTypeName( type );
      if( !type.equals( WPSUtilities.QNAME_ANY_URI ) && handler instanceof ISimpleMarshallingTypeHandler )
      {
        /* Literal. */
        outputDescription = WPS040ObjectFactoryUtilities.buildOutputDescriptionType( outputCode, outputTitle, outputAbstrakt, WPS040ObjectFactoryUtilities.buildLiteralOutputType( WPS040ObjectFactoryUtilities.buildDomainMetadataType( type.getLocalPart(), null ), null ) );
      }
      else
      {
        final SupportedComplexDataType outputFormChoice = getComplexData( gmlVersion, type );
        outputDescription = WPS040ObjectFactoryUtilities.buildOutputDescriptionType( outputCode, outputTitle, outputAbstrakt, outputFormChoice );
      }
      /* Füge die neue Beschreibung hinzu. */
      outputDescriptions.add( outputDescription );
    }

    final ProcessOutputs processOutputs = WPS040ObjectFactoryUtilities.buildProcessDescriptionTypeProcessOutputs( outputDescriptions );

    return WPS040ObjectFactoryUtilities.buildProcessDescriptionType( code, title, "", null, null, dataInputs, processOutputs, true, true ); //$NON-NLS-1$
  }

  private static LiteralInputType getLiteralData( final QName type )
  {
    final DomainMetadataType buildDomainMetadataType = WPS040ObjectFactoryUtilities.buildDomainMetadataType( type.getLocalPart(), null );
    final AnyValue buildAnyValue = WPS040ObjectFactoryUtilities.buildAnyValue();
    return WPS040ObjectFactoryUtilities.buildLiteralInputType( buildDomainMetadataType, null, buildAnyValue, null );
  }

  private static SupportedComplexDataType getComplexData( final String gmlVersion, final QName type )
  {
    final String format = mimeTypeFromDataType( gmlVersion, type );
    final String schema = format == null ? null : schemaLocationFromDataType( gmlVersion, type );
    final ComplexDataType complexData1 = WPS040ObjectFactoryUtilities.buildComplexDataType( format, "", schema ); //$NON-NLS-1$
    final ComplexDataType complexData = complexData1;
    final List<ComplexDataType> complexDatas = new LinkedList<ComplexDataType>();
    complexDatas.add( complexData );
    final SupportedComplexDataType outputFormChoice = WPS040ObjectFactoryUtilities.buildSupportedComplexDataType( complexDatas, null, null, null );
    return outputFormChoice;
  }

  private static String schemaLocationFromDataType( final String gmlVersion, final QName type )
  {
    String schema;
    try
    {
      final GMLSchemaCatalog schemaCatalog = KalypsoGMLSchemaPlugin.getDefault().getSchemaCatalog();
      final GMLSchema gmlSchema = schemaCatalog.getSchema( type.getNamespaceURI(), gmlVersion );
      final URL context = gmlSchema.getContext();
      schema = context.toString();
    }
    catch( final InvocationTargetException e )
    {
      // gobble
      schema = null;
    }
    return schema;
  }

  private static String mimeTypeFromDataType( final String gmlVersion, final QName type )
  {
    String format = ""; //$NON-NLS-1$
    if( GMLSchemaUtilities.isKnownType( type, gmlVersion ) )
    {
      format = WPSSimulationDataProvider.TYPE_GML;
    }
    return format;
  }
}