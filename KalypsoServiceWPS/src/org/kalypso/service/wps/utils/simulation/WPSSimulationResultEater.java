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
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManagerWrapper;
import org.apache.commons.vfs.FileType;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.io.VFSUtilities;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.ogc.gml.serialize.GmlSerializeException;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.ogc.ExecuteMediator;
import org.kalypso.service.wps.utils.ogc.ProcessDescriptionMediator;
import org.kalypso.service.wps.utils.ogc.WPS040ObjectFactoryUtilities;
import org.kalypso.simulation.core.ISimulationResultEater;
import org.kalypso.simulation.core.SimulationException;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * Manages the results for the client. Understands only Files at the moment.
 * 
 * @author Holger Albert
 */
public class WPSSimulationResultEater implements ISimulationResultEater
{
  private final FileSystemManagerWrapper m_vfsManager;

  /**
   * The process descriptions are containing the output data.
   */
  private final ProcessDescriptionType m_processDescription;

  /**
   * The execute request.
   */
  private final Execute m_execute;

  /**
   * The temporary directory.
   */
  private final File m_tmpDir;

  /**
   * The directory, where the results should be copied. The client is told that URL + [path to files].
   */
  private final FileObject m_resultDir;

  /**
   * The references that need to be copied when results are requested
   */
  private final Map<File, FileObject> m_references;

  /**
   * All current results. This could be files, literals and so on. Should be synchronized
   */
  private final Map<String, IOValueType> m_results;

  /**
   * Contains the id of the outputs as key and the description of the output the server can provide.
   */
  private final Map<String, OutputDescriptionType> m_outputList;

  /**
   * Contains the id of the output as key and the definition of the output that the client expects. TODO: This list is
   * currently mostly ignored.
   */
  private final Map<String, OutputDefinitionType> m_outputListClient;

  /**
   * The constructor.
   * 
   * @param processDescription
   *          The process description.
   * @param execute
   *          The execute request.
   * @param tmpDir
   *          The temporary directory for that simulation.
   * @param resultDir
   *          The FileObject contains information, where the results should be put, so that the client can read them.
   */
  public WPSSimulationResultEater( final ProcessDescriptionMediator processDescriptionMediator, final ExecuteMediator executeMediator, final File tmpDir, final String resultSpace ) throws SimulationException
  {
    try
    {
      m_processDescription = (ProcessDescriptionType) processDescriptionMediator.getProcessDescription( executeMediator.getProcessId() );
    }
    catch( final CoreException e1 )
    {
      throw new SimulationException( "Could not get process description", e1 );
    }

    m_execute = executeMediator.getV04();
    m_tmpDir = tmpDir;
    m_results = new LinkedHashMap<String, IOValueType>();
    m_references = new LinkedHashMap<File, FileObject>();

    m_outputList = index( m_processDescription );
    m_outputListClient = indexClient( m_execute );

    try
    {
      m_vfsManager = VFSUtilities.getNewManager();
      final String resultDirectoryName = tmpDir.getName();
      final FileObject resultRoot;
      if( resultSpace != null )
        resultRoot = m_vfsManager.resolveFile( resultSpace );
      else
        resultRoot = m_vfsManager.toFileObject( FileUtilities.TMP_DIR );
      m_resultDir = resultRoot.resolveFile( resultDirectoryName );
      m_resultDir.createFolder();
    }
    catch( final Exception e )
    {
      throw new SimulationException( "Error resolving the result directory for this job", e );
    }
    checkExpectedOutput();
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationResultEater#addResult(java.lang.String, java.lang.Object)
   */
  public void addResult( final String id, final Object result ) throws SimulationException
  {
    if( !m_outputList.containsKey( id ) )
      throw new SimulationException( "Server doesn't expect the output with the ID: " + id, null );

    // if( !m_outputListClient.containsKey( id ) )
    // throw new SimulationException( "Client doesn't expect the output with the ID: " + id, null );

    /* What type is that output? Get the description from the server, to check it. */
    final OutputDescriptionType outputDescription = m_outputList.get( id );

    final SupportedComplexDataType complexOutput = outputDescription.getComplexOutput();
    final LiteralOutputType literalOutput = outputDescription.getLiteralOutput();
    final SupportedCRSsType boundingBoxOutput = outputDescription.getBoundingBoxOutput();

    /* Build the output value. */
    final Object valueFormChoice;
    if( complexOutput != null )
    {
      if( result instanceof URI )
      {
        final URI urlResult = (URI) result;
        valueFormChoice = WPS040ObjectFactoryUtilities.buildComplexValueReference( urlResult.toString(), null, null, null );
      }
      else if( result instanceof URL )
      {
        final URL urlResult = (URL) result;
        final String clientUrlResult = WPSUtilities.convertInternalToClient( urlResult.toExternalForm() );
        valueFormChoice = WPS040ObjectFactoryUtilities.buildComplexValueReference( clientUrlResult, null, null, null );
      }
      else if( result instanceof File )
      {
        final File fileResult = (File) result;
        valueFormChoice = addComplexValueReference( fileResult );
      }
      else
      {
        String schema = null;
        String format = null;
        Object complexResult;
        if( result instanceof GMLWorkspace )
        {
          // 0.5 MB text file default buffer
          final StringWriter stringWriter = new StringWriter( 512 * 1024 );
          final GMLWorkspace gmlWorkspace = (GMLWorkspace) result;
          try
          {
            format = WPSSimulationDataProvider.TYPE_GML;
            final String schemaLocationString = gmlWorkspace.getGMLSchema().getContext().toString();
            gmlWorkspace.setSchemaLocation( schemaLocationString );
            schema = schemaLocationString;
            GmlSerializer.serializeWorkspace( stringWriter, gmlWorkspace, "UTF-8", true );
            complexResult = stringWriter.toString();
          }
          catch( final GmlSerializeException e )
          {
            throw new SimulationException( "Problem serializing gml output.", e );
          }
        }
        else
        {
          complexResult = result;
        }
        // REMARK: hack/convention: the result must now be the raw input for the anyType element
        valueFormChoice = addComplexValueType( complexResult, format, schema );
      }
    }
    else if( literalOutput != null )
    {
      final String value = literalOutput.getDataType().getValue();
      if( value.endsWith( "string" ) )
      {
        if( result instanceof String )
          valueFormChoice = addLiteralValueType( result );
        else
          throw new SimulationException( "The type of the output with the identifier '" + id + "' must be a String (Literal): " + result, null );
      }
      else if( value.endsWith( "int" ) )
      {
        if( result instanceof Integer )
          valueFormChoice = addLiteralValueType( result );
        else
          throw new SimulationException( "The type of the output with the identifier '" + id + "' must be an Integer (Literal): " + result, null );
      }
      else if( value.endsWith( "double" ) )
      {
        if( result instanceof Double )
          valueFormChoice = addLiteralValueType( result );
        else
          throw new SimulationException( "The type of the output with the identifier '" + id + "' must be a Double (Literal) ...", null );
      }
      else if( value.endsWith( "boolean" ) )
      {
        if( result instanceof String )
          valueFormChoice = addLiteralValueType( result );
        else
          throw new SimulationException( "The type of the output with the identifier '" + id + "' must be a Boolean (Literal) ...", null );
      }
      else
        throw new SimulationException( "The type of the output (which is: " + value + ") with the identifier '" + id + "' is not supported (Literal) ...", null );
    }
    else if( boundingBoxOutput != null )
    {
      if( result instanceof BoundingBoxType )
        valueFormChoice = result;
      else
        throw new SimulationException( "The type of the output with the identifier '" + id + "' must be a BoundingBoxType ...", null );
    }
    else
      throw new SimulationException( "The type of the output with the identifier '" + id + "' is not correctly defined by the simulation ...", null );

    /* Build io value. */
    final IOValueType ioValue = WPS040ObjectFactoryUtilities.buildIOValueType( outputDescription.getIdentifier(), outputDescription.getTitle(), outputDescription.getAbstract(), valueFormChoice );
    m_results.put( id, ioValue );
  }

  public List<IOValueType> getCurrentResults( ) throws SimulationException
  {
    checkResultDir();

    // copy all source files (references) to their destination
    for( final File sourceFile : m_references.keySet() )
    {
      final FileObject destination = m_references.get( sourceFile );
      try
      {
        /* Converting the source file to a file object from VFS. */
        final FileObject source = m_vfsManager.toFileObject( sourceFile );
        if( FileType.FOLDER.equals( source.getType() ) )
        {
          /* Directory copy. */
          Debug.println( "Copy directory " + source.getName() + " to " + destination.getName() + " ..." );
          VFSUtilities.copyDirectoryToDirectory( source, destination );
        }
        else if( FileType.FILE.equals( source.getType() ) )
        {
          /* File copy. */
          Debug.println( "Copy file " + source.getName() + " to " + destination.getName() + " ..." );
          VFSUtilities.copyFileTo( source, destination );
        }
      }
      catch( final IOException e )
      {
        throw new SimulationException( "Could not update reference to " + sourceFile, e );
      }
    }

    final Collection<IOValueType> values = m_results.values();
    return new ArrayList<IOValueType>( values );
  }

  /**
   * This function will create ComplexValueReference with the given file and copies it directly to the result directory.
   * 
   * @param sourceFile
   *          The file to reference in the ComplexValueReference.
   * @return A ComplexValueReference with the given file.
   */
  private ComplexValueReference addComplexValueReference( final File sourceFile ) throws SimulationException
  {
    checkResultDir();

    try
    {
      /* Getting the relative path to the source file. */
      final String relativePathToSource = FileUtilities.getRelativePathTo( m_tmpDir, sourceFile );
      if( relativePathToSource == null )
        throw new SimulationException( "The output to be copied is not inside the temporary directory: " + sourceFile );
      final String uri = m_resultDir.getURL().toExternalForm() + "/" + relativePathToSource;

      final FileObject destination = m_vfsManager.resolveFile( uri );

      /* assure old behavior - for none existing source files! */
      if( sourceFile.exists() )
      {
        final FileObject source = m_vfsManager.toFileObject( sourceFile );
        if( !source.equals( destination ) )
          VFSUtilities.copy( source, destination );
      }

      // keep track of file references
      m_references.put( sourceFile, destination );

      /* Build complex value reference. */
      return WPS040ObjectFactoryUtilities.buildComplexValueReference( WPSUtilities.convertInternalToClient( destination.getURL().toExternalForm() ), null, null, null );
    }
    catch( final IOException e )
    {
      throw new SimulationException( "Could not add ComplexValueReference for file " + sourceFile, e );
    }
  }

  private void checkResultDir( ) throws SimulationException
  {
    /* Resolving the result file object. */
    if( m_resultDir == null )
      throw new SimulationException( "Error resolving the result directory for this job: The property org.kalypso.service.wps.results is not set ...", null );
  }

  /**
   * This function will create ComplexValueType with the given object.
   * 
   * @param result
   *          An object, which should be added.
   * @return A ComplexValueType with the given file.
   */
  private ComplexValueType addComplexValueType( final Object result, final String format, final String schema )
  {
    // REMARK: hack/convention: the input must now be the raw input for the anyType element
    final List<Object> value = new ArrayList<Object>( 1 );
    value.add( result );

    /* Build the complex value. */
    return WPS040ObjectFactoryUtilities.buildComplexValueType( format, null, schema, value );
  }

  /**
   * This function will create a LiteralValueType with the given object (String, Integer, Double, Boolean).
   * 
   * @param result
   *          One of the types String, Integer, Double and Boolean.
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
    return WPS040ObjectFactoryUtilities.buildLiteralValueType( value, dataType, null );
  }

  /**
   * Indexes the output values with their id.
   * 
   * @param processDescription
   *          The process description, containing the input data.
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
   *          The execute request contains the ouput expected from the client.
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
    m_references.clear();
    try
    {
      m_resultDir.close();
    }
    catch( final FileSystemException e )
    {
      // gobble
    }
    m_vfsManager.close();
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