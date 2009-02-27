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
package org.kalypso.service.wps.utils.simulation;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import net.opengeospatial.ows.BoundingBoxType;
import net.opengeospatial.wps.ComplexValueType;
import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.IOValueType;
import net.opengeospatial.wps.LiteralOutputType;
import net.opengeospatial.wps.LiteralValueType;
import net.opengeospatial.wps.OutputDefinitionType;
import net.opengeospatial.wps.OutputDefinitionsType;
import net.opengeospatial.wps.OutputDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.SupportedCRSsType;
import net.opengeospatial.wps.SupportedComplexDataType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.VFSUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.ogc.OGCUtilities;
import org.kalypso.simulation.core.ISimulationResultEater;
import org.kalypso.simulation.core.SimulationException;

/**
 * Manages the results for the client. Understands only Files at the moment.
 * 
 * @author Holger Albert
 */
public class WPSSimulationResultEater implements ISimulationResultEater
{
  /**
   * The process descriptions are containing the output data.
   */
  private final ProcessDescriptionType m_processDescription;

  /**
   * The execute request.
   */
  private Execute m_execute = null;

  /**
   * The temporary directory.
   */
  private final File m_tmpDir;

  /**
   * The directory, where the results should be copied. The client is told that URL + [path to files].
   */
  private FileObject m_resultDir = null;

  /**
   * All current results. This could be files, literals and so on. Should be synchronized
   */
  private Map<String, Object> m_results = null;

  /**
   * Contains the id of the outputs as key and the output itself as value.
   */
  private Map<String, OutputDescriptionType> m_outputList = null;

  /**
   * Contains the id of the output as key and the output that is expected from the client as value.
   */
  private Map<String, OutputDefinitionType> m_outputListClient = null;

  /**
   * The constructor.
   * 
   * @param processDescription
   *            The process description.
   * @param execute
   *            The execute request.
   * @param tmpDir
   *            The temporary directory for that simulation.
   * @param resultDir
   *            The FileObject contains information, where the results should be put, so that the client can read them.
   */
  public WPSSimulationResultEater( final ProcessDescriptionType processDescription, final Execute execute, final File tmpDir, final FileObject resultDir ) throws SimulationException
  {
    m_processDescription = processDescription;
    m_execute = execute;
    m_tmpDir = tmpDir;
    m_resultDir = resultDir;
    m_results = new LinkedHashMap<String, Object>();

    m_outputList = index( m_processDescription );
    m_outputListClient = indexClient( m_execute );

    checkExpectedOutput();
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationResultEater#addResult(java.lang.String, java.lang.Object)
   */
  public void addResult( final String id, final Object result ) throws SimulationException
  {
    if( !m_outputList.containsKey( id ) )
      throw new SimulationException( "Server doesn't expect the output with the ID: " + id, null );

    if( !m_outputListClient.containsKey( id ) )
      throw new SimulationException( "Client doesn't expect the output with the ID: " + id, null );

    final OutputDefinitionType outputDefinition = m_outputListClient.get( id );
    if( outputDefinition != null )
      m_results.put( id, result );
  }

  public String[] getCurrentResults( )
  {
    return m_results.keySet().toArray( new String[] {} );
  }

  /**
   * This function copys the contained files in the results to the result dir and returns them as ComplexValueReferences
   * with the other types in a IOValueType list.
   * 
   * @return The descriptions of the outputs with the URLs.
   */
  public List<IOValueType> copyCurrentResults( ) throws SimulationException
  {
    try
    {
      /* Debug-Information. */
      Debug.println( "Copying the results ..." );

      /* Stores the output descriptions. */
      final List<IOValueType> ioValues = new LinkedList<IOValueType>();

      /* Get the expected output from the client. */
      final Iterator<String> clientKeys = m_outputListClient.keySet().iterator();

      /* Check each expected output with the one, the server can provide. */
      while( clientKeys.hasNext() )
      {
        /* The identifier for output to check. */
        final String clientKey = clientKeys.next();

        /* First, is the output already available? */
        if( !m_results.containsKey( clientKey ) )
        {
          /* It is not available at the moment, continue. */
          continue;
        }

        /* Getting the result. */
        final Object result = m_results.get( clientKey );

        /* Second, is the result added with a null value? */
        if( result == null )
        {
          /* Ignore it at the moment. */
          continue;
        }

        /* What type is that output? Get the description from the server, to check it. */
        final OutputDescriptionType outputDescription = m_outputList.get( clientKey );

        final SupportedComplexDataType complexOutput = outputDescription.getComplexOutput();
        final LiteralOutputType literalOutput = outputDescription.getLiteralOutput();
        final SupportedCRSsType boundingBoxOutput = outputDescription.getBoundingBoxOutput();

        /* Build the output value. */
        Object valueFormChoice = null;
        if( complexOutput != null )
        {
          if( result instanceof File )
            valueFormChoice = addComplexValueReference( (File) result );
          else if( result instanceof ComplexValueType )
            valueFormChoice = result;
          else
            throw new SimulationException( "The type of the output with the identifier '" + clientKey + "' must be a File (ComplexValueReference) or a ComplexValueType ...", null );
        }
        else if( literalOutput != null )
        {
          final String value = literalOutput.getDataType().getValue();
          if( "string".equals( value ) )
          {
            if( result instanceof String )
              valueFormChoice = addLiteralValueType( result );
            else
              throw new SimulationException( "The type of the output with the identifier '" + clientKey + "' must be a String (Literal) ...: " + result, null );
          }
          else if( "int".equals( value ) )
          {
            if( result instanceof Integer )
              valueFormChoice = addLiteralValueType( result );
            else
              throw new SimulationException( "The type of the output with the identifier '" + clientKey + "' must be an Integer (Literal) ...", null );

          }
          else if( "double".equals( value ) )
          {
            if( result instanceof Double )
              valueFormChoice = addLiteralValueType( result );
            else
              throw new SimulationException( "The type of the output with the identifier '" + clientKey + "' must be a Double (Literal) ...", null );
          }
          else if( "boolean".equals( value ) )
          {
            if( result instanceof String )
              valueFormChoice = addLiteralValueType( result );
            else
              throw new SimulationException( "The type of the output with the identifier '" + clientKey + "' must be a Boolean (Literal) ...", null );
          }
          else
            throw new SimulationException( "The type of the output (which is: " + value + ") with the identifier '" + clientKey + "' is not supported (Literal) ...", null );
        }
        else if( boundingBoxOutput != null )
        {
          if( result instanceof BoundingBoxType )
            valueFormChoice = result;
          else
            throw new SimulationException( "The type of the output with the identifier '" + clientKey + "' must be a BoundingBoxType ...", null );
        }
        else
          throw new SimulationException( "The type of the output with the identifier '" + clientKey + "' is not correctly defined by the simulation ...", null );

        /* Add it to an io value, if not null. */
        if( valueFormChoice != null )
        {
          /* Build io value. */
          final IOValueType ioValue = OGCUtilities.buildIOValueType( outputDescription.getIdentifier(), outputDescription.getTitle(), outputDescription.getAbstract(), valueFormChoice );
          ioValues.add( ioValue );
        }
      }

      return ioValues;
    }
    catch( final Exception e )
    {
      throw new SimulationException( e.getLocalizedMessage(), e );
    }
  }

  /**
   * This function will create ComplexValueReference with the given file and copies it directly to the result directory.
   * 
   * @param sourceFile
   *            The file to reference in the ComplexValueReference.
   * @return A ComplexValueReference with the given file.
   */
  private ComplexValueReference addComplexValueReference( final File sourceFile ) throws Exception
  {
    /* Get a file system manager. */
    final FileSystemManager manager = VFSUtilities.getManager();

    /* Getting the relative path to the source file. */
    final String relativePathToSource = FileUtilities.getRelativePathTo( m_tmpDir, sourceFile );
    if( relativePathToSource == null )
      throw new SimulationException( "The output to be copied is not inside the temporary directory: " + sourceFile );

    /* Converting the source file to a file object from VFS. */
    final FileObject source = manager.toFileObject( sourceFile );

    /* Resolving the result file object. */
    if( m_resultDir == null )
      throw new SimulationException( "Error resolving the result directory for this job: The property org.kalypso.service.wps.results is not set ...", null );

    final FileObject destination = manager.resolveFile( m_resultDir.getURL().toExternalForm() + "/" + relativePathToSource );

    if( FileType.FOLDER.equals( source.getType() ) )
    {
      /* Directory copy. */
      Debug.println( "Copy directory " + source.getName() + " to " + destination.getName() + " ..." );
      VFSUtilities.copyDirectoryToDirectory( source, destination );

      /* Build complex value reference. */
      return OGCUtilities.buildComplexValueReference( WPSUtilities.convertInternalToClient( destination.getURL().toExternalForm() ), null, null, null );
    }
    else if( FileType.FILE.equals( source.getType() ) )
    {
      /* File copy. */
      Debug.println( "Copy file " + source.getName() + " to " + destination.getName() + " ..." );
      VFSUtilities.copy( source, destination );

      /* Build complex value reference. */
      return OGCUtilities.buildComplexValueReference( WPSUtilities.convertInternalToClient( destination.getURL().toExternalForm() ), null, null, null );
    }

    return null;
  }

  /**
   * This function will add create with the given object (String, Integer, Double, Boolean) a LiteralValueType.
   * 
   * @param result
   *            One of the types String, Integer, Double and Boolean.
   * @return A LiteralValueType with the given value.
   */
  private LiteralValueType addLiteralValueType( final Object result )
  {
    String value = "";
    String dataType = "";
    if( result instanceof String )
    {
      value = DatatypeConverter.printString( (String) result );
      dataType = "string";
    }
    else if( result instanceof Integer )
    {
      value = DatatypeConverter.printInt( ((Integer) result).intValue() );
      dataType = "int";
    }
    else if( result instanceof Double )
    {
      value = DatatypeConverter.printDouble( ((Double) result).doubleValue() );
      dataType = "double";
    }
    else if( result instanceof Boolean )
    {
      value = DatatypeConverter.printBoolean( ((Boolean) result).booleanValue() );
      dataType = "boolean";
    }
    else
    {
      /* Other types will be ignored. */
      return null;
    }

    /* Build the literal value type. */
    return OGCUtilities.buildLiteralValueType( value, dataType, null );
  }

  /**
   * Indexes the output values with their id.
   * 
   * @param processDescription
   *            The process description, containing the input data.
   * @return The indexed map.
   */
  private Map<String, OutputDescriptionType> index( final ProcessDescriptionType processDescription )
  {
    final Map<String, OutputDescriptionType> outputList = new LinkedHashMap<String, OutputDescriptionType>();

    final ProcessOutputs processOutputs = processDescription.getProcessOutputs();
    final List<OutputDescriptionType> outputs = processOutputs.getOutput();

    for( final OutputDescriptionType output : outputs )
      outputList.put( output.getIdentifier().getValue(), output );

    return outputList;
  }

  /**
   * This function indexes the expected output from the client with their id.
   * 
   * @param execute
   *            The execute request contains the ouput expected from the client.
   * @return The indexed map.
   */
  private Map<String, OutputDefinitionType> indexClient( final Execute execute )
  {
    final Map<String, OutputDefinitionType> outputListClient = new LinkedHashMap<String, OutputDefinitionType>();

    final OutputDefinitionsType outputDefinitions = execute.getOutputDefinitions();
    final List<OutputDefinitionType> outputs = outputDefinitions.getOutput();

    for( final OutputDefinitionType output : outputs )
      outputListClient.put( output.getIdentifier().getValue(), output );

    return outputListClient;
  }

  /**
   * This function checks, if the expected output matches the output, that the server can provide. In other words, the
   * server must be able to provide the output, which the client wants.
   */
  private void checkExpectedOutput( ) throws SimulationException
  {
    /* Check the, if the output, the client wants, is available. */
    final Iterator<String> clientKeys = m_outputListClient.keySet().iterator();
    while( clientKeys.hasNext() )
    {
      final String clientKey = clientKeys.next();
      if( !m_outputList.containsKey( clientKey ) )
        throw new SimulationException( "The output for the identifier '" + clientKey + "' can not be provided from the server ...", null );
    }

    /* Ok, everything is fine. The client did not expect things, the server cannot do. */
  }

  /**
   * Disposes everything.
   */
  public void dispose( )
  {
    /* The result data will not be deleted, because the client must get the chance to copy them. */
  }

  /**
   * This function returns the result dir of this result eater.
   * 
   * @return The result dir.
   */
  public FileObject getResultDir( )
  {
    return m_resultDir;
  }
}