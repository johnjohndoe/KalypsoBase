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
package org.kalypso.service.wps.client.simulation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import net.opengeospatial.ows.CodeType;
import net.opengeospatial.wps.DataInputsType;
import net.opengeospatial.wps.IOValueType;
import net.opengeospatial.wps.InputDescriptionType;
import net.opengeospatial.wps.LiteralInputType;
import net.opengeospatial.wps.LiteralValueType;
import net.opengeospatial.wps.OutputDefinitionType;
import net.opengeospatial.wps.OutputDefinitionsType;
import net.opengeospatial.wps.OutputDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.SupportedCRSsType;
import net.opengeospatial.wps.SupportedComplexDataType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.VFSUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.ogc.OGCUtilities;
import org.kalypso.simulation.core.simspec.Modeldata;
import org.kalypso.simulation.core.simspec.Modeldata.Input;
import org.kalypso.simulation.core.simspec.Modeldata.Output;

/**
 * Provides functions, that should help handling the model specification and model data with the WPS.
 * 
 * @author Holger Albert
 */
public class SimulationUtilities
{
  /**
   * The constructor.
   */
  private SimulationUtilities( )
  {
  }

  /**
   * This function creates the data inputs which the client provides for the wps.
   * 
   * @param calcCaseFolder
   *            The folder of the calc case.
   * @param tmpServer
   *            The temp directory of the server.
   * @param inputClient
   *            The path to the place on the server, where the client can put his files, as configured in the config.ini
   *            from Kalypso.
   * @param data
   *            The modeldata.
   * @param description
   *            The decription of the process.
   * @return The data input from the model spec in wps format.
   */
  public static DataInputsType getDataInputs( final IContainer calcCaseFolder, final FileObject tmpServer, final String inputClient, final Modeldata data, final ProcessDescriptionType description ) throws CoreException, FileSystemException
  {
    // TODO At the moment only literals and references are supported from our model data.
    // The model data must be changed, in order to enable bounding boxes and complex values.
    // If this is done, the two new cases must be covered here as well.

    /* Need the filesystem manager. */
    final FileSystemManager fsManager = VFSUtilities.getManager();

    /* Get the list with the input. */
    final List<Input> inputList = data.getInput();

    /* The storage for the input values. */
    final List<IOValueType> inputValues = new LinkedList<IOValueType>();

    /* Get the input list. */
    final DataInputs dataInputs = description.getDataInputs();
    final List<InputDescriptionType> inputDescriptions = dataInputs.getInput();

    /* Iterate over all inputs and build the data inputs for the execute request. */
    for( final InputDescriptionType inputDescription : inputDescriptions )
    {
      final CodeType identifier = inputDescription.getIdentifier();

      /* Check if the input is in our model data, too. */
      final Input input = findInput( identifier.getValue(), inputList );
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

      /* Supported complex data type. */
      final SupportedComplexDataType complexData = inputDescription.getComplexData();
      if( complexData != null )
      {
        // TODO Add the complex value type here, how to decide when to use the reference and when the value?

        /* Get the URL if it is one. */
        URL url = isURL( inputPath );

        /* If the URL is null, it is a local file resource, otherwise it is a remote resource. */
        if( url == null )
        {
          /* It is a local file resource. */
          if( calcCaseFolder == null )
            throw new CoreException( StatusUtilities.createErrorStatus( "Complext Data specified but no base folder given" ) );

          /* Alles relativ zum Projekt auflösen! */
          final IContainer baseresource = input.isRelativeToCalcCase() ? calcCaseFolder : calcCaseFolder.getProject();

          final IResource inputResource = baseresource.findMember( inputPath );
          if( inputResource == null )
          {
            if( inputDescription.getMinimumOccurs().intValue() == 0 )
              continue;

            throw new CoreException( StatusUtilities.createErrorStatus( "Could not find input resource: " + inputPath + "\nPlease check your model data." ) );
          }

          /* Build the URL for this input. */
          final String relativePathTo = inputResource.getProjectRelativePath().toString();
          final FileObject destination = fsManager.resolveFile( tmpServer, relativePathTo );
          url = destination.getURL();
        }

        Debug.println( "Checking for server URL, where the input data can be copied ..." );
        if( inputClient == null )
        {
          Debug.println( "No URL to the server of the service is given, where the input data can be copied. Be sure to check the config.ini for the org.kalypso.service.wps.input property." );
          throw new CoreException( StatusUtilities.createErrorStatus( "No URL to the server of the service is given, where the input data can be copied. Be sure to check the config.ini for the org.kalypso.service.wps.input property." ) );
        }

        /* Build the complex value reference. */
        final CodeType code = OGCUtilities.buildCodeType( null, input.getId() );
        final ComplexValueReference valueReference = OGCUtilities.buildComplexValueReference( WPSUtilities.convertInternalToServer( url.toExternalForm(), inputClient ), null, null, null );
        final IOValueType ioValue = OGCUtilities.buildIOValueType( code, inputDescription.getTitle(), inputDescription.getAbstract(), valueReference );

        /* Add the input. */
        inputValues.add( ioValue );

        continue;
      }

      /* Literal input type */
      final LiteralInputType literalInput = inputDescription.getLiteralData();
      if( literalInput != null )
      {
        /* Build the literal value type. */
        final CodeType code = OGCUtilities.buildCodeType( null, input.getId() );
        final LiteralValueType literalValue = OGCUtilities.buildLiteralValueType( inputPath, literalInput.getDataType().getValue(), null );
        final IOValueType ioValue = OGCUtilities.buildIOValueType( code, inputDescription.getTitle(), inputDescription.getAbstract(), literalValue );

        /* Add the input. */
        inputValues.add( ioValue );

        continue;
      }

      /* Supported CRSs type. */
      final SupportedCRSsType supportedCRSsType = inputDescription.getBoundingBoxData();
      if( supportedCRSsType != null )
      {
        // TODO Add supported CRSs type (bounding boxes).
        continue;
      }
    }

    return OGCUtilities.buildDataInputsType( inputValues );
  }

  /**
   * This function creates the data outputs the clients expects for the wps.
   * 
   * @param description
   *            The description of the process.
   * @param data
   *            The modeldata.
   * @return The output of the model spec in wps format.
   */
  public static OutputDefinitionsType getOutputDefinitions( final Modeldata data, final ProcessDescriptionType description )
  {
    /* Get the list with the output. */
    final List<Output> outputList = data.getOutput();

    /* The storage for the output values. */
    final List<OutputDefinitionType> outputValues = new LinkedList<OutputDefinitionType>();

    /* Get the output list. */
    final ProcessOutputs processOutputs = description.getProcessOutputs();
    final List<OutputDescriptionType> outputDescriptions = processOutputs.getOutput();

    /* Iterate over all outputs and build the data inputs for the execute request. */
    for( final OutputDescriptionType outputDescription : outputDescriptions )
    {
      final CodeType identifier = outputDescription.getIdentifier();

      /* Check if the output is in our model data, too. */
      final Output output = findOutput( identifier.getValue(), outputList );

      if( output == null )
      {
        /* Ooops, it is missing in our model data. */
        // throw new CoreException( StatusUtilities.createErrorStatus( "The data output " + identifier.getValue() + " is missing. Check your model data." ) );
        continue;
      }

      final CodeType code = OGCUtilities.buildCodeType( null, identifier.getValue() );
      final OutputDefinitionType outputDefinition = OGCUtilities.buildOutputDefinitionType( code, outputDescription.getTitle(), outputDescription.getAbstract(), null, null, null, null );

      /* Add the output. */
      outputValues.add( outputDefinition );
    }

    return OGCUtilities.buildOutputDefinitionsType( outputValues );
  }

  /**
   * This function checks, if the path given is a remote URL.
   * 
   * @return An URL, if the path is an URL (and additionally no file URL), otherwise null.
   */
  public static URL isURL( final String path )
  {
    try
    {
      /* Check if it is an real URL. */
      final URL url = new URL( path );

      /* If it is a file URL, it is no remote URL. */
      if( url.getProtocol().equals( "file" ) )
      {
        Debug.println( "Input '" + path + "' was a local file URL ..." );
        return null;
      }

      /* It is a remote URL. */
      Debug.println( "Input '" + path + "' was a remote URL ..." );

      return url;
    }
    catch( final MalformedURLException e )
    {
      /* No need to give feedback on this error, because it was expected, if the path was no real URL. */
    }

    /* If it was no parseable URL, it is probably a file path. */
    Debug.println( "Input '" + path + "' was no URL ..." );

    return null;
  }

  /**
   * This function searches an input in the model data.
   * 
   * @param ID
   *            The ID of the input to search for.
   * @param inputList
   *            The input of the model data.
   * @return The found input or null, if none is found for that ID.
   */
  public static Input findInput( final String ID, final List<Input> inputList )
  {
    for( int i = 0; i < inputList.size(); i++ )
    {
      final Input input = inputList.get( i );
      if( input.getId().equals( ID ) )
        return input;
    }

    return null;
  }

  /**
   * This function searches an output out in the model data.
   * 
   * @param ID
   *            The ID of the output to search for.
   * @param outputList
   *            The output of the model data.
   * @return The found output or null, if none is found for that ID.
   */
  public static Output findOutput( final String ID, final List<Output> outputList )
  {
    for( int i = 0; i < outputList.size(); i++ )
    {
      final Output output = outputList.get( i );
      if( output.getId().equals( ID ) )
        return output;
    }

    return null;
  }
}