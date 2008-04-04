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
package org.kalypso.service.wps.client.simulation;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.opengeospatial.ows.CodeType;
import net.opengeospatial.wps.DataInputsType;
import net.opengeospatial.wps.InputDescriptionType;
import net.opengeospatial.wps.OutputDefinitionsType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.SupportedComplexDataType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.kalypso.contribs.eclipse.core.resources.CollectFilesVisitor;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.service.wps.client.IWPSDelegate;
import org.kalypso.service.wps.client.exceptions.WPSException;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.VFSUtilities;
import org.kalypso.simulation.core.simspec.Modeldata;
import org.kalypso.simulation.core.simspec.Modeldata.Input;
import org.kalypso.simulation.core.simspec.Modeldata.Output;

/**
 * This client can work with simulations.
 * 
 * @author Holger Albert
 */
public class SimulationDelegate implements IWPSDelegate
{
  /**
   * The id identifying the simulation.
   */
  private String m_identifier;

  /**
   * The calc case folder.
   */
  private IContainer m_calcCaseFolder;

  /**
   * The model data for this simulation.
   */
  private Modeldata m_data;

  /**
   * The URL to the wps service.
   */
  private String m_service;

  /**
   * The path to the place on the server, where the client can put his files, as configured in the config.ini from
   * Kalypso.
   */
  private String m_input;

  /**
   * The filesystem manager.
   */
  private FileSystemManager m_fsManager;

  /**
   * The temporary directory on the server.
   */
  private FileObject m_serverTmpDirectory;

  /**
   * The constructor.
   * 
   * @param identifier
   *            The id identifying the simulation.
   * @param calcCaseFolder
   *            The folder of the calc case.
   * @param data
   *            The model data.
   */
  public SimulationDelegate( String identifier, IContainer calcCaseFolder, Modeldata data )
  {
    /* Initializing the other variables. */
    m_identifier = identifier;
    m_calcCaseFolder = calcCaseFolder;
    m_data = data;

    /* Initilizing the variables from the properties of the config.ini. */
    m_service = System.getProperty( "org.kalypso.service.wps.service" );
    m_input = System.getProperty( "org.kalypso.service.wps.input" );

    /* Variables that are initialized during run time. */
    m_fsManager = null;
    m_serverTmpDirectory = null;
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSDelegate#init()
   */
  public void init( ) throws WPSException
  {
    /* The filesystem manager needs to be set. */
    initVFSManager();

    Debug.println( "Checking for service URL ..." );
    if( m_service == null )
    {
      Debug.println( "No URL to the service is given. Be sure to check the config.ini for the org.kalypso.service.wps.service property." );
      throw new WPSException( "No URL to the the service is given. Be sure to check the config.ini for the org.kalypso.service.wps.service property." );
    }

    Debug.println( "Checking for server URL, where the input data can be copied ..." );
    if( m_input == null )
    {
      Debug.println( "No URL to the server of the service is given, where the input data can be copied. Be sure to check the config.ini for the org.kalypso.service.wps.input property." );
      throw new WPSException( "No URL to the server of the service is given, where the input data can be copied. Be sure to check the config.ini for the org.kalypso.service.wps.input property." );
    }
  }

  /**
   * This function inits the VFS manager.
   */
  private void initVFSManager( ) throws WPSException
  {
    try
    {
      if( m_fsManager == null )
      {
        /* The filesystem manager needs to be set. */
        m_fsManager = VFSUtilities.getManager();
      }
    }
    catch( Exception ex )
    {
      throw new WPSException( "Initializing the the VFS manager has failed ...", ex );
    }
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSDelegate#copyInputFiles(net.opengeospatial.wps.ProcessDescriptionType)
   */
  public void copyInputFiles( ProcessDescriptionType description ) throws WPSException
  {
    /* Get the list with the input. */
    List<Modeldata.Input> inputList = m_data.getInput();

    /* Get the input list. */
    DataInputs dataInputs = description.getDataInputs();
    List<InputDescriptionType> inputDescriptions = dataInputs.getInput();

    /* Create a resource visitor. */
    CollectFilesVisitor visitor = new CollectFilesVisitor();

    try
    {
      /* Iterate over all inputs and build the data inputs for the execute request. */
      IProject project = m_calcCaseFolder.getProject();
      for( InputDescriptionType inputDescription : inputDescriptions )
      {
        CodeType identifier = inputDescription.getIdentifier();

        /* Check if the input is in our model data, too. */
        Input input = SimulationUtilities.findInput( identifier.getValue(), inputList );
        if( input == null )
        {
          /* Check, if it is an optional one. */
          if( inputDescription.getMinimumOccurs().intValue() == 1 )
          {
            /* Ooops, it is a mandatory one, but it is missing in our model data. */
            throw new Exception( "The data input " + identifier.getValue() + " is mandatory. Check your model data." );
          }

          continue;
        }

        /* Input is here. */
        String inputPath = input.getPath();

        /* Supported complex data type. */
        SupportedComplexDataType complexData = inputDescription.getComplexData();
        if( complexData != null )
        {
          // TODO Add the complex value type here, how to decide when to use the reference and when the value?
          // The complex value type should be skipped, as literals and bounding boxes.

          /* Get the URL if it is one. */
          URL url = SimulationUtilities.isURL( inputPath );

          /* If the URL is null, it is a local file resource, otherwise it is a remote resource. */
          if( url == null )
          {
            /* It is a local file resource. */

            /* Alles relativ zum Projekt auflˆsen! */
            IContainer baseresource = input.isRelativeToCalcCase() ? m_calcCaseFolder : project;
            IResource inputResource = baseresource.findMember( inputPath );
            if( inputResource == null )
            {
              if( inputDescription.getMinimumOccurs().intValue() == 0 )
                continue;

              throw new CoreException( StatusUtilities.createErrorStatus( "Could not find input resource: " + inputPath + "\nPlease check your model data." ) );
            }

            inputResource.accept( visitor );
          }
        }

        continue;
      }
    }
    catch( Exception ex )
    {
      throw new WPSException( "Could not collect the input files ...", ex );
    }

    /* Copy all collected files. */
    IFile[] files = visitor.getFiles();
    if( files.length > 0 )
      copyInputFiles( files );
  }

  /**
   * This function copies all input files.
   * 
   * @param files
   *            The files to copy.
   */
  private void copyInputFiles( IFile[] files ) throws WPSException
  {
    if( files.length > 0 )
    {
      /* Get the directory for server access. */
      initServerTmpDirectory();

      /* Copy the files. */
      for( int i = 0; i < files.length; i++ )
        copyInputFile( files[i] );
    }
  }

  /**
   * This function copies one input file to the place, where the server can read it.
   */
  private void copyInputFile( IFile ifile ) throws WPSException
  {
    try
    {
      /* Get the relative path of the file. */
      File file = ifile.getLocation().toFile();
      String relativePathTo = ifile.getProjectRelativePath().toString();

      /* Create the file objects. */
      FileObject source = m_fsManager.toFileObject( file );
      FileObject destination = VFSUtilities.checkProxyFor( m_serverTmpDirectory.getName() + "/" + relativePathTo );

      /* Copy file. */
      VFSUtilities.copy( source, destination );
    }
    catch( Exception ex )
    {
      throw new WPSException( "Could not copy file '" + ifile.getName() + "' to the server ...", ex );
    }
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSDelegate#getDataInputs(net.opengeospatial.wps.ProcessDescriptionType)
   */
  public DataInputsType getDataInputs( ProcessDescriptionType description ) throws WPSException
  {
    try
    {
      return SimulationUtilities.getDataInputs( m_calcCaseFolder, m_serverTmpDirectory, m_input, m_data, description );
    }
    catch( Exception ex )
    {
      throw new WPSException( "Could not create the date inputs ...", ex );
    }
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSDelegate#getOutputDefinitions(net.opengeospatial.wps.ProcessDescriptionType)
   */
  public OutputDefinitionsType getOutputDefinitions( ProcessDescriptionType description ) throws WPSException
  {
    try
    {
      return SimulationUtilities.getOutputDefinitions( m_data, description );
    }
    catch( Exception ex )
    {
      throw new WPSException( "Could not create the output definitions ...", ex );
    }
  }

  /**
   * This function inits the temporary directory of the server, where the client has to put his files.
   */
  private void initServerTmpDirectory( ) throws WPSException
  {
    try
    {
      /* If the directory is not initialized, it will be generated and created (if necessary). */
      if( m_serverTmpDirectory == null )
      {
        /* Get the directory for server access. */
        FileObject serverDirectory = VFSUtilities.checkProxyFor( m_input );
        m_serverTmpDirectory = VFSUtilities.createTempDirectory( "Simulation_", serverDirectory );

        if( !m_serverTmpDirectory.exists() )
        {
          Debug.println( "Creating folder " + m_serverTmpDirectory.getName().getPath() + " ..." );
          m_serverTmpDirectory.createFolder();
        }
      }
    }
    catch( Exception ex )
    {
      throw new WPSException( "Could not init the temp directory on the server, where the client can put his input files ...", ex );
    }
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSDelegate#copyResults(java.util.Map)
   */
  public void copyResults( Map<String, ComplexValueReference> references ) throws WPSException
  {
    /* If there are no references, do nothing. */
    if( references == null )
      return;

    try
    {
      /* The project is needed. */
      IProject project = m_calcCaseFolder.getProject();

      /* Get the model data outputs. */
      List<Output> outputs = m_data.getOutput();

      /* All results, which are references. */
      Iterator<String> keys = references.keySet().iterator();
      while( keys.hasNext() )
      {
        /* Get the next key. */
        String key = keys.next();

        /* Get the complex value reference. */
        ComplexValueReference complexValueReference = references.get( key );
        if( complexValueReference != null )
        {
          /* Building the source object. */
          String reference = complexValueReference.getReference();
          FileObject source = VFSUtilities.checkProxyFor( reference );

          /* Building the destination object. */
          Output output = SimulationUtilities.findOutput( key, outputs );

          /* Everything should be relative to the project! */
          String outputPath = output.getPath();
          IContainer baseresource = output.isRelativeToCalcCase() ? m_calcCaseFolder : project;
          File baseFile = baseresource.getLocation().toFile();
          File outputFile = new File( baseFile, outputPath );
          FileObject destination = m_fsManager.toFileObject( outputFile );

          /* Copy ... */
          VFSUtilities.copy( source, destination );

          IResource destResource = baseresource.getFile( new Path( outputPath ) );
          if( destResource != null && destResource.getType() == IResource.FILE )
          {
            IFile destFile = (IFile) destResource;
            destFile.getParent().refreshLocal( IResource.DEPTH_INFINITE, new NullProgressMonitor() );
            destFile.setCharset( "UTF-8", new NullProgressMonitor() );
          }
        }
      }
    }
    catch( Exception ex )
    {
      throw new WPSException( "Could not copy the result data to the client ...", ex );
    }
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSDelegate#finish()
   */
  public void finish( )
  {
    try
    {
      /* Delete the input files. */
      deleteInputFiles();
    }
    catch( WPSException e )
    {
      /* If an error has occured while deleting the files, finish the job without error. A warning should be ok. */
      // TODO Perhaps show a warning.
    }
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSDelegate#canceled()
   */
  public void canceled( )
  {
    /* TODO Tell the server, that the job should be canceled. */

    /* TODO Delete all input files with deleteInputFiles(). */
    /* This is omitted now, because the server isn't told, to cancel its operation, so it is likely, */
    /* he will run in an error then, if we delete the files now. */
  }

  /**
   * Deletes all input files, which have been copied so far.
   */
  private void deleteInputFiles( ) throws WPSException
  {
    if( m_serverTmpDirectory == null )
      return;

    try
    {
      if( m_serverTmpDirectory.exists() )
        VFSUtilities.deleteFiles( m_serverTmpDirectory );
    }
    catch( Exception ex )
    {
      throw new WPSException( "Could not finish the deinit operation ...", ex );
    }
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSDelegate#getIdentifier()
   */
  public String getIdentifier( )
  {
    return m_identifier;
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSDelegate#getServiceURL()
   */
  public String getServiceURL( )
  {
    return m_service;
  }
}