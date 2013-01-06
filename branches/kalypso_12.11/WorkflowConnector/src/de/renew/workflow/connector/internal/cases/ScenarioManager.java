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
package de.renew.workflow.connector.internal.cases;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.afgui.scenarios.ObjectFactory;
import org.kalypso.afgui.scenarios.Scenario;
import org.kalypso.afgui.scenarios.ScenarioList;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.contribs.eclipse.core.resources.FolderUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;

import de.renew.workflow.cases.Case;
import de.renew.workflow.cases.CaseList;
import de.renew.workflow.connector.cases.CopyScenarioContentsOperation;
import de.renew.workflow.connector.cases.ICaseManagerListener;
import de.renew.workflow.connector.cases.IDerivedScenarioCopyFilter;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioList;
import de.renew.workflow.connector.cases.IScenarioManager;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;
import de.renew.workflow.connector.internal.WorkflowConnectorPlugin;
import de.renew.workflow.connector.internal.i18n.Messages;

/**
 * @author Stefan Kurzbach
 */
public class ScenarioManager implements IScenarioManager
{
  public static final String METADATA_FOLDER = ".metadata"; //$NON-NLS-1$

  public static final String METADATA_FILENAME = "cases.xml"; //$NON-NLS-1$

  private static final JAXBContext JC = JaxbUtilities.createQuiet( org.kalypso.afgui.scenarios.ObjectFactory.class, de.renew.workflow.cases.ObjectFactory.class );

  private static final ObjectFactory OF = new org.kalypso.afgui.scenarios.ObjectFactory();

  private final List<ICaseManagerListener> m_listeners = Collections.synchronizedList( new ArrayList<ICaseManagerListener>() );

  private ICaseList m_caseList = null;

  private IScenario m_currentCase;

  protected final IProject m_project;

  private final IFile m_metaDataFile;

  private IStatus m_status = Status.OK_STATUS;

  /**
   * Initializes the {@link ICaseManager} on the given project
   * 
   * @param project
   *          the project, must not be <code>null</code>
   * @exception CoreException
   *              if this method fails. Reasons include:
   *              <ul>
   *              <li>The metadata folder is not accessible.</li>
   *              <li>There is a problem loading the database.</li>
   */
  public ScenarioManager( final IProject project )
  {
    m_project = project;

    final IFolder folder = project.getFolder( METADATA_FOLDER );
    m_metaDataFile = folder.getFile( METADATA_FILENAME );
  }

  private ICaseList getCaseList( )
  {
    try
    {
      if( m_caseList == null )
        m_caseList = loadModel( m_metaDataFile, m_project );

      m_status = Status.OK_STATUS;

      return m_caseList;
    }
    catch( final CoreException e )
    {
      // ignore
      // evil: if this fails, the next step will fail too, so we could also throw the exception...
      // e.printStackTrace();
      m_status = e.getStatus();

      return new CaseListHandler( new CaseList(), m_project );
    }
  }

  @Override
  public void resetCaseList( )
  {
    m_caseList = null;
  }

  @Override
  public IStatus getStatus( )
  {
    return m_status;
  }

  private static CaseListHandler loadModel( final IFile metaDataFile, final IProject project ) throws CoreException
  {
    if( metaDataFile.exists() )
    {
      try
      {
        final URL url = metaDataFile.getRawLocationURI().toURL();
        final CaseList cases = (CaseList)JC.createUnmarshaller().unmarshal( url );
        return new CaseListHandler( cases, project );
      }
      catch( final Throwable e )
      {
        final IStatus status = new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, "Failed to load case file", e ); //$NON-NLS-1$
        throw new CoreException( status );
      }
    }

    final IStatus status = new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, "Failed to load case file" ); //$NON-NLS-1$
    throw new CoreException( status );
  }

  protected final void internalAddCase( final IScenario caze )
  {
    getCaseList().getCaseList().getCases().add( caze.getCase() );
  }

  protected final void internalRemoveCase( final IScenario caze )
  {
    getCaseList().getCaseList().getCases().remove( caze.getCase() );
  }

  @Override
  public IScenario getCurrentCase( )
  {
    return m_currentCase;
  }

  @Override
  public void setCurrentCase( final IScenario caze )
  {
    m_currentCase = caze;
  }

  protected List<IScenario> internalGetCases( )
  {
    return getCaseList().getCases();
  }

  /**
   * Saves the changes in the scenario structure to the database.
   * 
   * @param monitor
   *          the progess monitor to report progress to or <code>null</code> if no progress reporting is desired
   * @exception CoreException
   *              if this method fails. Reasons include:
   *              <ul>
   *              <li>The database is not accessible or writable.</li>
   *              <li>An error specific to the kind of database has occured. It will be included in the cause of the exception.</li>
   */
  public void persist( final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      monitor.beginTask( Messages.getString( "ScenarioManager.0" ), 5000 ); //$NON-NLS-1$

      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final Marshaller marshaller = JC.createMarshaller();
      marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
      marshaller.marshal( getCaseList().getCaseList(), bos );
      bos.close();

      monitor.worked( 2000 );

      final ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() );

      // m_metaDataFile.refreshLocal( 0, null );

      if( m_metaDataFile.exists() )
        m_metaDataFile.setContents( bis, false, true, null );
      else
      {
        FolderUtilities.mkdirs( m_metaDataFile.getParent() );
        m_metaDataFile.create( bis, false, null );
      }

      bis.close();
    }
    catch( final Exception e )
    {
      final IStatus status = new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, "Failed to save scenario file", e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
    finally
    {
      monitor.done();
    }
  }

  @Override
  public void addCaseManagerListener( final ICaseManagerListener l )
  {
    if( l == null )
    {
      return;
    }
    else
    {
      if( m_listeners.contains( l ) )
      {
        return;
      }
      else
      {
        m_listeners.add( l );
      }
    }
  }

  @Override
  public void removeCaseManagerListener( final ICaseManagerListener l )
  {
    if( l == null )
    {
      return;
    }
    else
    {
      m_listeners.remove( l );
    }
  }

  protected void fireCaseAdded( final IScenario caze )
  {
    for( final ICaseManagerListener l : m_listeners )
    {
      l.caseAdded( caze );
    }
  }

  protected void fireCaseRemoved( final IScenario caze )
  {
    for( final ICaseManagerListener l : m_listeners )
    {
      l.caseRemoved( caze );
    }
  }

  @Override
  public void dispose( )
  {
    m_listeners.clear();
  }

  @Override
  public void removeCase( final IScenario scenario, IProgressMonitor monitor ) throws CoreException
  {
    if( monitor == null )
      monitor = new NullProgressMonitor();

    try
    {
      monitor.beginTask( Messages.getString( "org.kalypso.afgui.scenarios.ScenarioManager.4" ), 100 ); //$NON-NLS-1$

      // only remove if no derived scenarios
      final IScenarioList derivedScenarios = scenario.getDerivedScenarios();
      if( derivedScenarios != null && !derivedScenarios.getScenarios().isEmpty() )
        throw new CoreException( new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, Messages.getString( "org.kalypso.afgui.scenarios.ScenarioManager.5" ) ) ); //$NON-NLS-1$

      /* Only remove if not base scenario */
      final IScenario parentScenario = scenario.getParentScenario();
      if( parentScenario == null )
        throw new CoreException( new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, "Cannot remove base scenario" ) ); //$NON-NLS-1$

      parentScenario.getDerivedScenarios().getScenarios().remove( scenario );

      /* Save scenario file */
      persist( new NullProgressMonitor() );

      monitor.worked( 5 );

      /* Delete scenario content */
      final IFolder folder = scenario.getFolder();
      folder.delete( true, null );
    }
    finally
    {
      monitor.done();
    }

    fireCaseRemoved( scenario );
  }

  @Override
  public IScenario getCase( final String id )
  {
    for( final IScenario scenario : getCases() )
    {
      final IScenario result = findScenario( scenario, id );
      if( result != null )
        return result;
    }

    return null;
  }

  @Override
  public List<IScenario> getCases( )
  {
    final List<IScenario> resultList = new ArrayList<>();
    final List<IScenario> internalCases = internalGetCases();
    for( final IScenario caze : internalCases )
    {
      final Case myCaze = caze.getCase();
      if( myCaze instanceof Scenario )
        resultList.add( new ScenarioHandler( (Scenario)myCaze, caze.getProject() ) );
    }

    return resultList;
  }

  /**
   * Returns a scenario with the given id in the context of the parentScenario or null if no such scenario exists
   */
  private IScenario findScenario( final IScenario parentScenario, final String id )
  {
    if( parentScenario.getURI().equals( id ) )
      return parentScenario;

    final IScenarioList derivedScenarios = parentScenario.getDerivedScenarios();
    if( derivedScenarios != null )
    {
      for( final IScenario derivedScenario : derivedScenarios.getScenarios() )
      {
        if( derivedScenario.getURI().equals( id ) )
          return derivedScenario;

        final IScenario result = findScenario( derivedScenario, id );
        if( result != null )
          return result;
      }
    }

    return null;
  }

  private void validateScenarioName( final IScenario parentScenario, final String scenarioName ) throws CoreException
  {
    final IFolder parentFolder = parentScenario.getDerivedFolder();
    final IStatus validateStatus = parentFolder.getWorkspace().validateName( scenarioName, IResource.FOLDER );
    if( !validateStatus.isOK() )
      throw new CoreException( validateStatus );
  }

  @Override
  public IScenario createScenario( final IScenario parentScenario, final String scenarioName, final String scenarioDescription, final IScenario templateScenario, final boolean copySubScenarios, final IProgressMonitor monitor ) throws CoreException
  {
    if( parentScenario == null )
    {
      final IStatus status = new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, "Cannot clone base scenario" ); //$NON-NLS-1$
      throw new CoreException( status );
    }

    /* Validate the path first. */
    validateScenarioName( parentScenario, scenarioName );

    /* Get the derived folder. */
    final IFolder derivedFolder = parentScenario.getDerivedFolder();

    /* Get the scenario folder. */
    final IFolder scenarioFolder = derivedFolder.getFolder( scenarioName );
    if( scenarioFolder.exists() )
      throw new CoreException( new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, String.format( Messages.getString( "ScenarioManager.1" ), scenarioName ) ) ); //$NON-NLS-1$

    monitor.beginTask( Messages.getString( "ScenarioManager.2" ), 100 ); //$NON-NLS-1$

    /* First copy data, then register it to avoid registered scenarios without data */
    copyScenarioData( templateScenario, scenarioFolder, copySubScenarios, new SubProgressMonitor( monitor, 90 ) );

    /* Create the uri */
    final String uri = createUri( scenarioName, parentScenario );

    /* Create the new scenario in xml. */
    final Scenario newScenario = OF.createScenario();
    newScenario.setURI( uri );
    newScenario.setName( scenarioName );
    newScenario.setDescription( scenarioDescription );
    newScenario.setParentScenario( parentScenario.getScenario() );

    /* Add new scenario to list of derived scenarios of the parent */
    final ScenarioList derivedScenarios = parentScenario.getScenario().getDerivedScenarios();
    if( derivedScenarios == null )
    {
      final ScenarioList newDerivedScenarios = OF.createScenarioList();
      newDerivedScenarios.getScenarios().add( newScenario );
      parentScenario.getScenario().setDerivedScenarios( newDerivedScenarios );
    }
    else
      derivedScenarios.getScenarios().add( newScenario );

    /* Save scenario meta file */
    persist( new SubProgressMonitor( monitor, 90 ) );

    /* Create the scenario handler. */
    final ScenarioHandler scenario = new ScenarioHandler( newScenario, m_project );

    /* Fire the case added event. */
    fireCaseAdded( scenario );

    return scenario;
  }

  private void copyScenarioData( final IScenario templateScenario, final IFolder targetFolder, final boolean copySubScenarios, final IProgressMonitor monitor ) throws CoreException
  {
    FolderUtilities.mkdirs( targetFolder );

    /* This are the folders, to be ignored. */
    /* The target folder should always be ignored. */
    final List<IFolder> scenarioFolders = new ArrayList<>();
    scenarioFolders.add( targetFolder );

    /* If no sub scenarios should be copied, ignore these as well. */
    if( !copySubScenarios )
    {
      final List<IScenario> templateDerivedScenarios = templateScenario.getDerivedScenarios().getScenarios();
      for( final IScenario derivedScenario : templateDerivedScenarios )
        scenarioFolders.add( derivedScenario.getFolder() );
    }

    final IFolder templateFolder = templateScenario.getFolder();

    final ScenarioHandlingProjectNature nature = ScenarioHandlingProjectNature.toThisNature( m_project );
    final IDerivedScenarioCopyFilter filter = nature.getDerivedScenarioCopyFilter();

    final CopyScenarioContentsOperation copyScenarioContentsOperation = new CopyScenarioContentsOperation( templateFolder, targetFolder, scenarioFolders.toArray( new IFolder[] {} ), filter );
    final IStatus status = copyScenarioContentsOperation.execute( monitor );
    if( !status.isOK() )
      throw new CoreException( status );
  }

  private String createUri( final String scenarioName, final IScenario parentScenario ) throws CoreException
  {
    try
    {
      final IFolder parentFolder = parentScenario.getFolder();
      final IFolder derivedFolder = parentScenario.getDerivedFolder();

      if( derivedFolder.equals( parentFolder ) )
        return String.format( "%s/%s", parentScenario.getURI(), URLEncoder.encode( scenarioName, "UTF-8" ) ); //$NON-NLS-1$ //$NON-NLS-2$

      return String.format( "%s/%s", parentScenario.getURI(), URLEncoder.encode( derivedFolder.getName() + "/" + scenarioName, "UTF-8" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    catch( final UnsupportedEncodingException ex )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( ex ) );
    }
  }

  @Override
  public void renameScenario( final IScenario scenario, final String name, final String comment, final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( Messages.getString( "ScenarioManager.3" ), 100 ); //$NON-NLS-1$

    final IScenario parentScenario = scenario.getParentScenario();

    /* update description */
    scenario.getScenario().setDescription( comment );

    /* update name, if changed */
    final String oldName = scenario.getName();
    if( !oldName.equals( name ) )
    {
      validateScenarioName( parentScenario, name );

      /* rename folder */
      final IFolder folder = scenario.getFolder();
      final IPath newPath = folder.getFullPath().removeLastSegments( 1 ).append( name );
      folder.move( newPath, false, new SubProgressMonitor( monitor, 90 ) );

      scenario.getScenario().setName( name );

      /* Create the uri */
      final String uri = createUri( name, parentScenario );
      scenario.getScenario().setURI( uri );
    }

    persist( new SubProgressMonitor( monitor, 10 ) );
  }
}