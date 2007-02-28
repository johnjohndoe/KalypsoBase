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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.OutputDefinitionType;
import net.opengeospatial.wps.OutputDefinitionsType;
import net.opengeospatial.wps.OutputDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.apache.commons.vfs.FileObject;
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
   * The temporary directory, where the results should be copied. The client is told that URL + [path to files].
   */
  private FileObject m_resultDir = null;

  /**
   * The result files. Should be synchronized
   */
  private Vector<OutputDefinitionType> m_results = null;

  /**
   * Contains the id of the outputs as key and the output itself as value.
   */
  private Map<String, Object> m_outputList = null;

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
   * @param resultDir
   *          The FileObject contains information, where the results should be put, so that the client can read them.
   */
  public WPSSimulationResultEater( ProcessDescriptionType processDescription, Execute execute, FileObject resultDir )
  {
    m_processDescription = processDescription;
    m_execute = execute;
    m_resultDir = resultDir;
    m_results = new Vector<OutputDefinitionType>();

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
      m_results.add( outputDefinition );
  }

  public String[] getCurrentResults( )
  {
    final String[] results = new String[m_results.size()];
    for( int i = 0; i < results.length; i++ )
    {
      OutputDefinitionType result = m_results.get( i );
      results[i] = result.getIdentifier().getValue();
    }

    return results;
  }

  /**
   * This function copys the result to the result dir, along with a status.
   */
  public void copyCurrentResults( )
  {

  }

  /**
   * Indexes the output values with their id.
   * 
   * @param processDescription
   *          The process description, containing the input data.
   * @return The indexed map.
   */
  private Map<String, Object> index( ProcessDescriptionType processDescription )
  {
    Map<String, Object> outputList = new LinkedHashMap<String, Object>();

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
}