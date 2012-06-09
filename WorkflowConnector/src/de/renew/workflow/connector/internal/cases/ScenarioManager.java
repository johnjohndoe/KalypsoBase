/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.afgui.scenarios.ObjectFactory;
import org.kalypso.afgui.scenarios.Scenario;
import org.kalypso.afgui.scenarios.ScenarioList;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.contribs.eclipse.core.resources.FolderUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;

import de.renew.workflow.base.IWorkflow;
import de.renew.workflow.cases.Case;
import de.renew.workflow.cases.CaseList;
import de.renew.workflow.connector.WorkflowProjectNature;
import de.renew.workflow.connector.cases.ICaseManagerListener;
import de.renew.workflow.connector.cases.IDerivedScenarioCopyFilter;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioList;
import de.renew.workflow.connector.cases.IScenarioManager;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;
import de.renew.workflow.connector.internal.WorkflowConnectorPlugin;
import de.renew.workflow.connector.internal.i18n.Messages;
import de.renew.workflow.utils.ScenarioConfiguration;

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

  private ICaseList m_cases;

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

    /* Prepare for exception: case-list with not cases */
    m_cases = new CaseListHandler( new CaseList(), m_project );

    try
    {
      if( !folder.exists() )
        folder.create( false, true, null );

      loadModel();
    }
    catch( final CoreException e )
    {
      // ignore
      // evil: if this fails, the next step wil fail too, so we could also throw the exception...
      e.printStackTrace();
      m_status = e.getStatus();
    }
  }

  /**
   * @see de.renew.workflow.connector.cases.ICaseManager#getStatus()
   */
  @Override
  public IStatus getStatus( )
  {
    return m_status;
  }

  private void loadModel( ) throws CoreException
  {
    if( !m_metaDataFile.exists() )
    {
      final CaseList cases = new de.renew.workflow.cases.ObjectFactory().createCaseList();
      m_cases = new CaseListHandler( cases, m_project );
      createCase( "Basis" );
    }
    else
    {
      try
      {
        final URL url = m_metaDataFile.getRawLocationURI().toURL();
        final CaseList cases = (CaseList) JC.createUnmarshaller().unmarshal( url );
        m_cases = new CaseListHandler( cases, m_project );
      }
      catch( final Throwable e )
      {
        final IStatus status = new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, "", e );
        throw new CoreException( status );
      }
    }
  }

  protected final void internalAddCase( final IScenario caze )
  {
    m_cases.getCaseList().getCases().add( caze.getCase() );
  }

  protected final void internalRemoveCase( final IScenario caze )
  {
    m_cases.getCaseList().getCases().remove( caze.getCase() );
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
    return m_cases.getCases();
  }

  @Override
  public void persist( final IProgressMonitor monitor )
  {
    // TODO: comment, why in a job?!
    final Job job = new Job( "Szenarien speichern." )
    {
      @SuppressWarnings("synthetic-access")
      @Override
      protected IStatus run( @SuppressWarnings("hiding") final IProgressMonitor monitor )
      {
        try
        {
          monitor.beginTask( "Szenarien speichern.", 5000 );
          final ByteArrayOutputStream bos = new ByteArrayOutputStream();
          JC.createMarshaller().marshal( m_cases.getCaseList(), bos );
          bos.close();
          monitor.worked( 2000 );
          final ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() );
          m_metaDataFile.refreshLocal( 0, null );
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
          return new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, "", e );
        }
        finally
        {
          monitor.done();
        }
        return Status.OK_STATUS;
      }
    };
    job.setRule( m_metaDataFile.getParent().getParent() );
    job.schedule();
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
  public IScenario createCase( final String name )
  {
    final Scenario newScenario = OF.createScenario();
    newScenario.setName( name );

    final IScenario scenario = new ScenarioHandler( newScenario, m_project );
    internalAddCase( scenario );

    persist( null );

    fireCaseAdded( scenario );

    return scenario;
  }

  @Override
  public IScenario deriveScenario( final String scenarioName, final String scenarioDescription, final IScenario parentScenario ) throws CoreException
  {
    try
    {
      /* Validate the path first. */
      final IFolder parentFolder = parentScenario.getFolder();
      final IStatus validateStatus = parentFolder.getWorkspace().validateName( scenarioName, IResource.FOLDER );
      if( !validateStatus.isOK() )
        throw new CoreException( validateStatus );

      /* Get the derived folder. */
      final IFolder derivedFolder = getDerivedFolder( parentFolder );

      /* Get the scenario folder. */
      final IFolder scenarioFolder = getScenarioFolder( scenarioName, parentFolder, derivedFolder );
      if( scenarioFolder.exists() )
        throw new CoreException( new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, String.format( "Unable to create new scenario: Parent scenario already contains a folder with name '%s'", scenarioName ) ) );

      /* Create the uri. */
      final String uri = createUri( scenarioName, parentScenario, derivedFolder );

      /* Create the new scenario. */
      final Scenario newScenario = OF.createScenario();
      newScenario.setURI( uri );
      newScenario.setName( scenarioName );
      newScenario.setDescription( scenarioDescription );
      newScenario.setParentScenario( parentScenario.getScenario() );

      /* Get the list of derived scenarios of the parent. */
      final ScenarioList derivedScenarios = parentScenario.getScenario().getDerivedScenarios();
      if( derivedScenarios == null )
      {
        final ScenarioList newDerivedScenarios = OF.createScenarioList();
        newDerivedScenarios.getScenarios().add( newScenario );
        parentScenario.getScenario().setDerivedScenarios( newDerivedScenarios );
      }
      else
        derivedScenarios.getScenarios().add( newScenario );

      /* Save. */
      persist( null );

      /* Create the scenario handler. */
      final ScenarioHandler scenario = new ScenarioHandler( newScenario, m_project );

      /* Fire the case added event. */
      fireCaseAdded( scenario );

      return scenario;
    }
    catch( final Exception ex )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( ex ) );
    }
  }

  private IFolder getDerivedFolder( final IFolder parentFolder )
  {
    try
    {
      final WorkflowProjectNature workflowProjectNature = WorkflowProjectNature.toThisNature( m_project );
      final IWorkflow workflow = workflowProjectNature.getCurrentWorklist();
      final ScenarioConfiguration scenarioConfiguration = workflow.getScenarioConfiguration();
      if( scenarioConfiguration == null )
        return null;

      final String derivedFolder = scenarioConfiguration.getDerivedFolder();
      if( derivedFolder == null || derivedFolder.length() == 0 )
        return null;

      return parentFolder.getFolder( derivedFolder );
    }
    catch( final CoreException ex )
    {
      ex.printStackTrace();
      return null;
    }
  }

  private IFolder getScenarioFolder( final String scenarioName, final IFolder parentFolder, final IFolder derivedFolder )
  {
    IFolder scenarioFolder = null;
    if( derivedFolder != null )
      scenarioFolder = derivedFolder.getFolder( scenarioName );
    else
      scenarioFolder = parentFolder.getFolder( scenarioName );

    return scenarioFolder;
  }

  private String createUri( final String scenarioName, final IScenario parentScenario, final IFolder derivedFolder ) throws UnsupportedEncodingException
  {
    if( derivedFolder != null )
      return String.format( "%s/%s", parentScenario.getURI(), URLEncoder.encode( derivedFolder.getName() + "/" + scenarioName, "UTF-8" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    return String.format( "%s/%s", parentScenario.getURI(), URLEncoder.encode( scenarioName, "UTF-8" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * FIXME handling of sub scenarios
   */
  @Override
  public IScenario cloneScenario( final String name, final IScenario toClone ) throws CoreException
  {
    final IScenario parentScenario = toClone.getParentScenario();
    try
    {
      final IProjectNature nature = toClone.getProject().getNature( ScenarioHandlingProjectNature.ID );
      final ScenarioHandlingProjectNature scenarioNature = (ScenarioHandlingProjectNature) nature;
      final IDerivedScenarioCopyFilter filter = scenarioNature.getDerivedScenarioCopyFilter();
      scenarioNature.setDerivedScenarioCopyFilter( new IDerivedScenarioCopyFilter()
      {
        @Override
        public boolean copy( final IResource resource )
        {
          return false;
        }
      } );

      final String description = toClone.getDescription();
      final IScenario derived = deriveScenario( name, description, parentScenario );
      /** problem - project nature copies project files -> take data from toClone and not from parent!!! */

      final IFolder destinationFolder = derived.getFolder();

      // TODO works with subscenarios - i don't believe?!?
      final IFolder srcFolder = toClone.getFolder();
      final IResource[] members = srcFolder.members( false );
      for( final IResource resource : members )
      {
        if( resource.getName().equals( destinationFolder.getName() ) )
        {
          // ignore scenario folder and .* resources
          continue;
        }
        else
        {
          resource.copy( destinationFolder.getFullPath().append( resource.getName() ), false, null );
        }
      }

      persist( null );

      new UIJob( "" ) //$NON-NLS-1$
      {
        @Override
        public IStatus runInUIThread( final IProgressMonitor monitor )
        {
          scenarioNature.setDerivedScenarioCopyFilter( filter );

          return Status.OK_STATUS;
        }
      }.schedule( 250 );

      return derived;
    }
    catch( final Exception e )
    {
      WorkflowConnectorPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );

      throw new CoreException( new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, Messages.getString( "org.kalypso.afgui.scenarios.ScenarioManager.3" ), e ) ); //$NON-NLS-1$
    }
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

      final IScenario parentScenario = scenario.getParentScenario();
      if( parentScenario == null )
      {
        // base scenario
        internalRemoveCase( scenario );
      }
      else
      {
        parentScenario.getDerivedScenarios().getScenarios().remove( scenario );
      }

      monitor.worked( 5 );

      persist( null );
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
    final List<IScenario> resultList = new ArrayList<IScenario>();
    final List<IScenario> internalCases = internalGetCases();
    for( final IScenario caze : internalCases )
    {
      final Case myCaze = caze.getCase();
      if( myCaze instanceof Scenario )
        resultList.add( new ScenarioHandler( (Scenario) myCaze, caze.getProject() ) );
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
}