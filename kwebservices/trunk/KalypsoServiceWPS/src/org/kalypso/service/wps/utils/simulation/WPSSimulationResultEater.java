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

import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.IOValueType;
import net.opengeospatial.wps.OutputDefinitionType;
import net.opengeospatial.wps.OutputDefinitionsType;
import net.opengeospatial.wps.OutputDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.OGCUtilities;
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
  private ProcessDescriptionType m_processDescription;

  /**
   * The execute request.
   */
  private Execute m_execute = null;

  /**
   * The temporary directory.
   */
  private File m_tmpDir;

  /**
   * The directory, where the results should be copied. The client is told that URL + [path to files].
   */
  private FileObject m_resultDir = null;

  /**
   * The result files. Should be synchronized
   */
  private Map<String, File> m_resultFiles = null;

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
   *          The process description.
   * @param execute
   *          The execute request.
   * @param tmpDir
   *          The temporary directory for that simulation.
   * @param resultDir
   *          The FileObject contains information, where the results should be put, so that the client can read them.
   */
  public WPSSimulationResultEater( ProcessDescriptionType processDescription, Execute execute, File tmpDir, FileObject resultDir )
  {
    m_processDescription = processDescription;
    m_execute = execute;
    m_tmpDir = tmpDir;
    m_resultDir = resultDir;
    m_resultFiles = new LinkedHashMap<String, File>();

    m_outputList = index( m_processDescription );
    m_outputListClient = indexClient( m_execute );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationResultEater#addResult(java.lang.String, java.io.File)
   */
  public void addResult( String id, File file ) throws SimulationException
  {
    if( !m_outputList.containsKey( id ) )
      throw new SimulationException( "Server doesn't expect the output with the ID: " + id, null );

    if( !m_outputListClient.containsKey( id ) )
      throw new SimulationException( "Client doesn't expect the output with the ID: " + id, null );

    OutputDefinitionType outputDefinition = m_outputListClient.get( id );

    if( outputDefinition != null )
      m_resultFiles.put( id, file );
  }

  public String[] getCurrentResults( )
  {
    return m_resultFiles.keySet().toArray( new String[] {} );
  }

  /**
   * This function copys the result to the result dir.
   * 
   * @return The descriptions of the outputs with the URLs.
   */
  public List<IOValueType> copyCurrentResults( ) throws SimulationException
  {
    try
    {
      Debug.println( "Copying the results ..." );

      /* Get a file system manager. */
      FileSystemManager manager = VFS.getManager();

      /* Stores the output descriptions. */
      List<IOValueType> ioValues = new LinkedList<IOValueType>();

      Iterator<String> itr = m_resultFiles.keySet().iterator();
      while( itr.hasNext() )
      {
        String key = itr.next();

        /* Getting the source file. */
        File sourceFile = m_resultFiles.get( key );

        /* Getting the relative path to the source file. */
        String relativePathToSource = FileUtilities.getRelativePathTo( m_tmpDir, sourceFile );

        /* Converting the source file to a file object from VFS. */
        String url = sourceFile.toURL().toExternalForm();
        FileObject source = manager.resolveFile( url );

        /* Resolving the result file object. */
        FileObject destination = m_resultDir.resolveFile( relativePathToSource );

        if( FileType.FOLDER.equals( source.getType() ) )
        {
          /* Directory copy. */
          Debug.println( "Copy Directory " + source.getName() + " to " + destination.getName() + " ..." );

          // TODO Copy the directorys as well ...
        }
        else if( FileType.FILE.equals( source.getType() ) )
        {
          /* File copy. */
          Debug.println( "Copy file " + source.getName() + " to " + destination.getName() + " ..." );
          destination.copyFrom( source, new AllFileSelector() );

          /* Get for reference. */
          OutputDescriptionType outputDescription = m_outputList.get( key );

          /* Build complex value reference. */
          ComplexValueReference complexValueReference = OGCUtilities.buildComplexValueReference( destination.getURL().toExternalForm(), null, null, null );

          /* Build io value. */
          IOValueType ioValue = OGCUtilities.buildIOValueType( outputDescription.getIdentifier(), outputDescription.getTitle(), outputDescription.getAbstract(), complexValueReference );
          ioValues.add( ioValue );
        }
      }

      return ioValues;
    }
    catch( Exception e )
    {
      throw new SimulationException( e.getMessage(), e );
    }
  }

  /**
   * Indexes the output values with their id.
   * 
   * @param processDescription
   *          The process description, containing the input data.
   * @return The indexed map.
   */
  private Map<String, OutputDescriptionType> index( ProcessDescriptionType processDescription )
  {
    Map<String, OutputDescriptionType> outputList = new LinkedHashMap<String, OutputDescriptionType>();

    ProcessOutputs processOutputs = processDescription.getProcessOutputs();
    List<OutputDescriptionType> outputs = processOutputs.getOutput();

    for( OutputDescriptionType output : outputs )
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
  private Map<String, OutputDefinitionType> indexClient( Execute execute )
  {
    Map<String, OutputDefinitionType> outputListClient = new LinkedHashMap<String, OutputDefinitionType>();

    OutputDefinitionsType outputDefinitions = execute.getOutputDefinitions();
    List<OutputDefinitionType> outputs = outputDefinitions.getOutput();

    for( OutputDefinitionType output : outputs )
    {
      outputListClient.put( output.getIdentifier().getValue(), output );
    }

    return outputListClient;
  }

  /**
   * Disposes everything.
   */
  public void dispose( )
  {
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