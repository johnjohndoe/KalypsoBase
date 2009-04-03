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
package org.kalypso.service.wps.utils.ogc;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import net.opengeospatial.ows.AnyValue;
import net.opengeospatial.ows.DomainMetadataType;
import net.opengeospatial.ows.MetadataType;
import net.opengeospatial.wps.InputDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ISimpleMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.WPSUtilities.WPS_VERSION;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.KalypsoSimulationCoreJaxb;
import org.kalypso.simulation.core.simspec.DataType;
import org.kalypso.simulation.core.simspec.Modelspec;

/**
 * @author kurzbach
 */
public class ProcessDescriptionMediator extends AbstractWPSMediator<net.opengis.wps._1_0.ProcessDescriptions, net.opengeospatial.wps.ProcessDescriptions>
{

  public ProcessDescriptionMediator( final String version )
  {
    super( version );
  }

  public ProcessDescriptionMediator( final WPS_VERSION version )
  {
    super( version );
  }

  /**
   * Returns the process descriptions wrapper with descriptions for all processes in the list
   */
  public Object getProcessDescriptions( final List<String> identifiers ) throws CoreException
  {
    /* Build the process description for all simulations. */
    /* It is vital, that the two lists have the same order and length. */
    final List<Object> processDescriptions = new ArrayList<Object>( identifiers.size() );
    for( final String typeID : identifiers )
    {
      /* Build the process description. */
      final Object processDescription = getProcessDescription( typeID );

      /* Save the process description. */
      processDescriptions.add( processDescription );
    }
    return getProcessDescriptionsWrapper( processDescriptions );
  }

  /**
   * Returns the process descriptions wrapper for the given process descriptions
   */
  @SuppressWarnings("unchecked")
  private Object getProcessDescriptionsWrapper( final List processDescriptions )
  {
    switch( getVersion() )
    {
      case V040:
        return WPS040ObjectFactoryUtilities.buildProcessDescriptions( processDescriptions );
    }
    return null;
  }

  /**
   * Returns the complete process description for a process. It will look up the process in the registered simulations.
   * 
   * @throws CoreException
   *           If the simulation could not be found or if there is a problem parsing the simulation specification.
   */
  public Object getProcessDescription( final String typeID ) throws CoreException
  {
    /* Get the Simulation.. */
    final ISimulation simulation = WPSUtilities.getSimulation( typeID );

    // TODO At the moment only literals and references are supported from our model spec.
    // The spec must be changed, in order to enable bounding boxes and complex values.
    // If this is done, the two new cases must be covered here as well.

    /* Get the specification for that simulation. */
    final URL spezifikation = simulation.getSpezifikation();
    Modelspec modelSpec;
    try
    {
      modelSpec = (Modelspec) KalypsoSimulationCoreJaxb.JC.createUnmarshaller().unmarshal( spezifikation );
    }
    catch( final JAXBException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }

    /* Build all content for the process description. */
    final String identifier = modelSpec.getTypeID();

    switch( getVersion() )
    {
      case V040:
        /* Get the input from the model spec. */
        final List<DataType> input = modelSpec.getInput();

        /* Build the data inputs. */
        final List<Object> inputDescriptions = new ArrayList<Object>( input.size() );

        for( final DataType data : input )
        {
          final Object inputDescription = getInputDescription( data );
          inputDescriptions.add( inputDescription );
        }

        /* Get the output from the model spec. */
        final List<DataType> output = modelSpec.getOutput();

        /* Build the output descriptions. */
        final List<Object> outputDescriptions = new ArrayList<Object>( output.size() );
        for( final DataType data : output )
        {
          final Object outputDescription = getOutputDescription( data );
          outputDescriptions.add( outputDescription );
        }

        final String title = identifier;
        final String abstrakt = null;
        final boolean storeSupported = true;
        final boolean statusSupported = true;
        return getProcessDescription( identifier, title, abstrakt, inputDescriptions, outputDescriptions, storeSupported, statusSupported );
    }
    return null;
  }

  /**
   * Returns the process description wrapper object for input and output descriptions
   */
  @SuppressWarnings("unchecked")
  private Object getProcessDescription( final String identifier, final String title, final String abstrakt, final List inputDescriptions, final List outputDescriptions, boolean storeSupported, boolean statusSupported )
  {
    switch( getVersion() )
    {
      case V040:
        final net.opengeospatial.wps.ProcessDescriptionType.DataInputs dataInputs = WPS040ObjectFactoryUtilities.buildDataInputs( inputDescriptions );
        final net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs processOutputs = WPS040ObjectFactoryUtilities.buildProcessDescriptionTypeProcessOutputs( outputDescriptions );
        final net.opengeospatial.ows.CodeType code = WPS040ObjectFactoryUtilities.buildCodeType( "", identifier );
        final String version = WPSUtilities.WPS_VERSION.V040.toString();
        final List<MetadataType> metaDatas = null; // TODO: metadata
        return WPS040ObjectFactoryUtilities.buildProcessDescriptionType( code, title, abstrakt, metaDatas, version, dataInputs, processOutputs, storeSupported, statusSupported );
    }
    return null;
  }

  /**
   * Returns the output description for a single output
   */
  private Object getOutputDescription( final DataType data )
  {
    switch( getVersion() )
    {
      case V040:
        final net.opengeospatial.ows.CodeType outputCode = WPS040ObjectFactoryUtilities.buildCodeType( "", data.getId() );
        final String outputTitle = data.getId();
        final String outputAbstrakt = data.getDescription();

        final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
        final QName type = data.getType();
        final IMarshallingTypeHandler handler = typeRegistry.getTypeHandlerForTypeName( type );
        final Object outputFormChoice;
        if( !type.equals( WPSUtilities.QNAME_ANY_URI ) && handler instanceof ISimpleMarshallingTypeHandler )
        {
          /* Literal. */
          final DomainMetadataType buildDomainMetadataType = WPS040ObjectFactoryUtilities.buildDomainMetadataType( type.getLocalPart(), null );
          outputFormChoice = WPS040ObjectFactoryUtilities.buildLiteralOutputType( buildDomainMetadataType, null );
        }
        else
        {
          /* Reference. */
          final net.opengeospatial.wps.ComplexDataType complexData = WPS040ObjectFactoryUtilities.buildComplexDataType( "", "", "" );
          final List<net.opengeospatial.wps.ComplexDataType> complexDatas = new LinkedList<net.opengeospatial.wps.ComplexDataType>();
          complexDatas.add( complexData );

          outputFormChoice = WPS040ObjectFactoryUtilities.buildSupportedComplexDataType( complexDatas, null, null, null );
        }
        return WPS040ObjectFactoryUtilities.buildOutputDescriptionType( outputCode, outputTitle, outputAbstrakt, outputFormChoice );
    }
    return null;
  }

  /**
   * Returns the input description for a single input
   */
  private Object getInputDescription( final DataType data )
  {
    final net.opengeospatial.ows.CodeType inputCode = WPS040ObjectFactoryUtilities.buildCodeType( "", data.getId() );
    final String inputTitle = data.getId();
    final String inputAbstrakt = data.getDescription();
    final int minOccurs = data.isOptional() ? 0 : 1;

    final QName type = data.getType();
    final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    final IMarshallingTypeHandler handler = typeRegistry.getTypeHandlerForTypeName( type );

    switch( getVersion() )
    {
      case V040:
        final Object inputFormChoice;
        if( !type.equals( WPSUtilities.QNAME_ANY_URI ) && handler instanceof ISimpleMarshallingTypeHandler )
        {
          /* Literal. */
          final DomainMetadataType metaData = WPS040ObjectFactoryUtilities.buildDomainMetadataType( type.getLocalPart(), null );
          final AnyValue anyValue = WPS040ObjectFactoryUtilities.buildAnyValue();
          inputFormChoice = WPS040ObjectFactoryUtilities.buildLiteralInputType( metaData, null, anyValue, null );
        }
        else
        {
          /* Reference. */
          final net.opengeospatial.wps.ComplexDataType complexData = WPS040ObjectFactoryUtilities.buildComplexDataType( "", "", "" );
          final List<net.opengeospatial.wps.ComplexDataType> complexDatas = new LinkedList<net.opengeospatial.wps.ComplexDataType>();
          complexDatas.add( complexData );
          inputFormChoice = WPS040ObjectFactoryUtilities.buildSupportedComplexDataType( complexDatas, null, null, null );
        }
        return WPS040ObjectFactoryUtilities.buildInputDescriptionType( inputCode, inputTitle, inputAbstrakt, inputFormChoice, minOccurs );
    }
    return null;
  }

  public Map<String, Object> getInputDescriptions( final String typeID ) throws CoreException
  {
    final Object processDescription = getProcessDescription( typeID );
    final Map<String, Object> inputDescriptions = new HashMap<String, Object>();
    switch( getVersion() )
    {
      case V040:
        final ProcessDescriptionType processDescription04 = (ProcessDescriptionType) processDescription;
        final List<InputDescriptionType> inputList = processDescription04.getDataInputs().getInput();
        for( final InputDescriptionType inputDescriptionType : inputList )
        {
          final String inputId = inputDescriptionType.getIdentifier().getValue();
          inputDescriptions.put( inputId, inputDescriptionType );
        }
        break;
    }
    return inputDescriptions;
  }
}
