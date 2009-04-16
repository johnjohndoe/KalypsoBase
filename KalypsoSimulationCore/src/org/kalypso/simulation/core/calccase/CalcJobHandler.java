/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.simulation.core.calccase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;

import org.apache.commons.httpclient.util.URIUtil;
import org.eclipse.core.internal.resources.PlatformURLResourceConnection;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.commons.java.util.zip.ZipResourceVisitor;
import org.kalypso.commons.java.util.zip.ZipResourceVisitor.PATH_TYPE;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.simulation.core.ISimulationService;
import org.kalypso.simulation.core.KalypsoSimulationCorePlugin;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.SimulationDescription;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.SimulationInfo;
import org.kalypso.simulation.core.simspec.Modeldata;
import org.kalypso.simulation.core.simspec.Modeldata.ClearAfterCalc;
import org.kalypso.simulation.core.simspec.Modeldata.Input;
import org.kalypso.simulation.core.simspec.Modeldata.Output;
import org.kalypso.simulation.core.util.SimulationUtilitites;

/**
 * @author belger
 */
public class CalcJobHandler
{
  private final Modeldata m_modelspec;

  private final CoreException m_cancelException = new CoreException( new Status( IStatus.CANCEL, KalypsoSimulationCorePlugin.getID(), 0, "Berechnung wurde vom Benutzer abgebrochen", null ) );

  private String m_jobID = null;

  private final ISimulationService m_calcService;

  private File m_zipFile = null;

  public CalcJobHandler( final Modeldata modelspec, final ISimulationService calcService )
  {
    m_modelspec = modelspec;
    m_calcService = calcService;
  }

  public IStatus runJob( final IContainer calcCaseFolder, final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( "Berechnung wird durchgeführt für Variante: " + calcCaseFolder.getName(), 5000 );
    try
    {
      // Daten zum Service schieben
      monitor.subTask( "Initialisiere Berechnung..." );
      m_jobID = startCalcJob( calcCaseFolder, new SubProgressMonitor( monitor, 1000 ) );

      if( monitor.isCanceled() )
        throw m_cancelException;

      final SubProgressMonitor calcMonitor = new SubProgressMonitor( monitor, 2000 );
      calcMonitor.beginTask( "Berechnung wird durchgeführt", 100 );
      int oldProgess = 0;
      while( true )
      {
        final SimulationInfo bean = m_calcService.getJob( m_jobID );

        boolean bStop = false;
        switch( bean.getState() )
        {
          case FINISHED:
          case ERROR:
          case CANCELED:
            bStop = true;
            break;

          default:
            break;
        }

        if( bStop )
        {
          break;
        }

        try
        {
          Thread.sleep( 1000 );
        }
        catch( final InterruptedException e1 )
        {
          e1.printStackTrace();

          throw new CoreException( StatusUtilities.statusFromThrowable( e1, "Kritischer Fehler" ) );
        }

        final int progress = bean.getProgress();
        final String message = bean.getMessage();
        calcMonitor.subTask( message );
        calcMonitor.worked( progress - oldProgess );
        oldProgess = progress;

        // ab hier bei cancel nicht mehr zurückkehren, sondern
        // erstmal den Job-Canceln und warten bis er zurückkehrt
        if( monitor.isCanceled() )
        {
          m_calcService.cancelJob( m_jobID );
        }
      }

      calcMonitor.done();

      final SimulationInfo jobBean = m_calcService.getJob( m_jobID );

      // Abhängig von den Ergebnissen was machen
      final String finishText = jobBean.getFinishText();
      final String message = finishText == null ? "" : finishText;
      switch( jobBean.getState() )
      {
        case FINISHED:
        {
          final IProject project = calcCaseFolder.getProject();
          // clear results as defined in modelspec
          clearResults( calcCaseFolder, new SubProgressMonitor( monitor, 500 ) );

          // Ergebniss abholen
          m_calcService.transferCurrentResults( project.getLocation().toFile(), m_jobID );
          project.refreshLocal( IResource.DEPTH_INFINITE, new SubProgressMonitor( monitor, 500 ) );
          return StatusUtilities.createMultiStatusFromMessage( jobBean.getFinishStatus(), KalypsoSimulationCorePlugin.getID(), 0, message, System.getProperty( "line.separator" ), null );
        }

        case CANCELED:
          throw m_cancelException;

        case ERROR:
        {
          final Throwable exception = jobBean.getException();
          final IStatus status = StatusUtilities.createStatus( jobBean.getFinishStatus(), message, exception );
          throw new CoreException( status );
        }

        default:
        {
          // darf eigentlich nie vorkommen
          final IStatus status = StatusUtilities.createMultiStatusFromMessage( IStatus.ERROR, KalypsoSimulationCorePlugin.getID(), 0, jobBean.getMessage(), System.getProperty( "line.separator" ), null );
          throw new CoreException( status );
        }
      }
    }
    catch( final Throwable e )
    {
      e.printStackTrace();
      throw new CoreException( StatusUtilities.statusFromThrowable( e, "Fehler beim Aufruf des Rechendienstes" ) );
    }
    finally
    {
      try
      {
        if( m_jobID != null )
        {
          if( !Boolean.valueOf( Platform.getDebugOption( SimulationUtilitites.DEBUG_KEEP_SIM_FILES ) ) )
          {
            m_calcService.disposeJob( m_jobID );

            if( m_zipFile != null )
            {
              m_zipFile.delete();
            }
          }
        }
      }
      catch( final SimulationException e1 )
      {
        e1.printStackTrace();

        throw new CoreException( StatusUtilities.statusFromThrowable( e1, "Kritischer Fehler bei Löschen des Rechen-Jobs" ) );
      }
      monitor.done();
    }
  }

  private void clearResults( final IContainer calcCaseFolder, final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      final IProject project = calcCaseFolder.getProject();

      final List<ClearAfterCalc> clearList = m_modelspec.getClearAfterCalc();
      monitor.beginTask( "Alte Ergebnisse werden gelöscht", clearList.size() );

      for( final ClearAfterCalc clearAfterCalc : clearList )
      {
        final Modeldata.ClearAfterCalc clearType = clearAfterCalc;

        final boolean relToCalc = clearType.isRelativeToCalcCase();
        final String path = clearType.getPath();
        final IResource resource = relToCalc ? calcCaseFolder.findMember( path ) : project.findMember( path );
        if( resource != null )
        {
          resource.delete( false, new SubProgressMonitor( monitor, 1 ) );
        }
      }
    }
    finally
    {
      monitor.done();
    }
  }

  private String startCalcJob( final IContainer calcCaseFolder, final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      final List<Modeldata.Input> inputList = m_modelspec.getInput();
      monitor.beginTask( "Eingangsdaten für Berechnungsdienst vorbereiten", inputList.size() );

      final SimulationDescription[] inputDescription = m_calcService.getRequiredInput( m_modelspec.getTypeID() );

      m_zipFile = File.createTempFile( "CalcJobData_", ".zip" );
      m_zipFile.deleteOnExit();

      final SimulationDataPath[] input = zipData( calcCaseFolder, monitor, inputDescription, inputList, m_zipFile );
      final SimulationDataPath[] output = createOutputBeans( calcCaseFolder );

      final DataSource jarSource = new FileDataSource( m_zipFile );
      final DataHandler jarHandler = new DataHandler( jarSource );

      final SimulationInfo bean = m_calcService.startJob( m_modelspec.getTypeID(), "Description", jarHandler, input, output );
      return bean.getId();
    }
    catch( final SimulationException se )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( se, "Fehler beim Starten der Berechnung. Kontrollieren Sie die Konfiguration des Rechendienstes." ) );
    }
    catch( final IOException e )
    {
      e.printStackTrace();
      throw new CoreException( StatusUtilities.statusFromThrowable( e, "Eingangsdaten konnten nicht erzeugt werden." ) );
    }
    finally
    {
      monitor.done();
    }
  }

  private SimulationDataPath[] createOutputBeans( final IContainer calcCaseFolder )
  {
    final List<Output> list = m_modelspec.getOutput();
    final SimulationDataPath[] output = new SimulationDataPath[list.size()];
    int count = 0;
    for( final Output ot : list )
    {
      final String outpath = ot.getPath();
      final boolean relCalcCase = ot.isRelativeToCalcCase();
      final String path = relCalcCase ? calcCaseFolder.getProjectRelativePath() + "/" + outpath : outpath;

      output[count++] = new SimulationDataPath( ot.getId(), path );
    }

    return output;
  }

  private SimulationDataPath[] zipData( final IContainer calcCaseFolder, final IProgressMonitor monitor, final SimulationDescription[] inputDescription, final List<Modeldata.Input> inputList, final File zipFile ) throws FileNotFoundException, CoreException, IOException
  {
    // hash input description
    final QName QNAME_ANY_URI = new QName( NS.XSD_SCHEMA, "anyURI" );
    final Map<String, SimulationDescription> inputdescriptionMap = new HashMap<String, SimulationDescription>( inputDescription.length );
    for( final SimulationDescription desc : inputDescription )
    {
      inputdescriptionMap.put( desc.getId(), desc );
    }

    ZipResourceVisitor zipper = null;
    try
    {
      final IProject project = calcCaseFolder.getProject();
      final List<SimulationDataPath> inputBeanList = new ArrayList<SimulationDataPath>();
      for( final Input input : inputList )
      {
        final String inputPath = input.getPath();
        final String inputId = input.getId();
        final SimulationDescription description = inputdescriptionMap.get( inputId );
        final QName inputType = description == null ? QNAME_ANY_URI : description.getType();

        final String beanValue;

        if( inputType.equals( QNAME_ANY_URI ) )
        {
          // if the type is a uri, put the content as file into the zip

          // alles relativ zum Projekt auflösen!
          IResource inputResource;
          if( inputPath.startsWith( PlatformURLResourceConnection.RESOURCE_URL_STRING ) )
          {
            final IContainer baseresource = project.getWorkspace().getRoot();
            final String path = ResourceUtilities.findPathFromURL( new URL( inputPath ) ).toPortableString();
            inputResource = baseresource.findMember( path );
            if( inputResource == null )
            {
              inputResource = baseresource.findMember( URIUtil.decode( path ) );
            }

          }
          else
          {
            final IContainer baseresource = input.isRelativeToCalcCase() ? calcCaseFolder : project;
            inputResource = baseresource.findMember( inputPath );
          }
          if( inputResource == null )
          {
            if( input.isOptional() )
            {
              continue;
            }

            throw new CoreException( StatusUtilities.createErrorStatus( "Konnte Input-Resource nicht finden: " + inputPath + "\nÜberprüfen Sie die Modellspezifikation." ) );
          }

          // final IPath projectRelativePath = inputResource.getProjectRelativePath();
          final IPath platformRelativePath = inputResource.getFullPath().makeRelative();

          /* Create zipper very lazy, in order to prevent exception because zip stream is empty */
          if( zipper == null )
          {
            zipper = new ZipResourceVisitor( zipFile, PATH_TYPE.PLATFORM_RELATIVE );
          }

          inputResource.accept( zipper );
// beanValue = projectRelativePath.toString();
          beanValue = platformRelativePath.toString();
        }
        else
        {
          // just put the value into the bean
          beanValue = inputPath;
        }

        final SimulationDataPath calcJobDataBean = new SimulationDataPath( inputId, beanValue );
        inputBeanList.add( calcJobDataBean );

        monitor.worked( 1 );
      }

      return inputBeanList.toArray( new SimulationDataPath[inputBeanList.size()] );
    }
    finally
    {
      if( zipper != null )
      {
        try
        {
          zipper.close();
        }
        catch( final IOException e )
        {
          e.printStackTrace();
        }
      }
    }
  }
}