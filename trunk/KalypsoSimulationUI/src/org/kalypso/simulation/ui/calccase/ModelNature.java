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
package org.kalypso.simulation.ui.calccase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.kalypso.auth.KalypsoAuthPlugin;
import org.kalypso.auth.user.IKalypsoUser;
import org.kalypso.commons.runtime.LogAnalyzer;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypso.simulation.ui.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.visitors.FindPropertyByNameVisitor;

/**
 * @author belger
 */
public class ModelNature implements IProjectNature, IResourceChangeListener
{
  public static final String METADATA_KEY_CALCCASE_CONTINUE_ALLOWED = "CALCCASE_CONTINUE_ALLOWED"; //$NON-NLS-1$

  private static final String STR_MODELLRECHNUNG_WIRD_DURCHGEFUEHRT = Messages.getString( "org.kalypso.simulation.ui.calccase.ModelNature.0" ); //$NON-NLS-1$

  public static final String MODELLTYP_FOLDER = ".model"; //$NON-NLS-1$

  public static final String MODELLTYP_CALCCASECONFIG_XML = MODELLTYP_FOLDER + "/" + "calcCaseConfig.xml"; //$NON-NLS-1$ //$NON-NLS-2$

  public static final String ID = KalypsoSimulationUIPlugin.getID() + ".ModelNature"; //$NON-NLS-1$

  private static final String METADATA_FILE = ".metadata"; //$NON-NLS-1$

  public static final String CONTROL_NAME = ".calculation"; //$NON-NLS-1$

  public static final String CONTROL_TEMPLATE_NAME = ".calculation.template"; //$NON-NLS-1$

  public static final String CONTROL_VIEW_PATH = MODELLTYP_FOLDER + "/.calculation.view"; //$NON-NLS-1$

  public static final String MODELLTYP_CALCWIZARD_XML = MODELLTYP_FOLDER + "/" + "calcWizard.xml"; //$NON-NLS-1$ //$NON-NLS-2$

  private final Properties m_defaultMetadata = new Properties();

  private final Properties m_metadata = new Properties( m_defaultMetadata );

  private IProject m_project;

  public static final String PROGNOSE_FOLDER = "Rechenvarianten"; //$NON-NLS-1$

  public static final String CONTROL_TEMPLATE_GML_PATH = MODELLTYP_FOLDER + "/" + CONTROL_TEMPLATE_NAME; //$NON-NLS-1$

  private static final String META_PROP_VALID_HOURS = "VALID_FORECAST_HOURS"; //$NON-NLS-1$

  /** Standardddifferenz des Simulationsstarts vor dem Vorhersagezeitpunkt */
  private static final String META_PROP_DEFAULT_SIMHOURS = "DEFAULT_SIMHOURS"; //$NON-NLS-1$

  public ModelNature( )
  {
    // Set some default value for the metadata
    m_defaultMetadata.put( METADATA_KEY_CALCCASE_CONTINUE_ALLOWED, Boolean.FALSE.toString() );
  }

  /**
   * @see org.eclipse.core.resources.IProjectNature#configure()
   */
  @Override
  public void configure( )
  {
    // nothing to do
  }

  public final IFolder getPrognoseFolder( )
  {
    return m_project.getFolder( PROGNOSE_FOLDER );
  }

  private IFile getMetadataFile( )
  {
    return m_project.getFile( new Path( METADATA_FILE ) );
  }

  /**
   * @see org.eclipse.core.resources.IProjectNature#deconfigure()
   */
  @Override
  public void deconfigure( )
  {
    // nothing to do; only thing to do might be to delete the .metadata file
  }

  /**
   * @see org.eclipse.core.resources.IProjectNature#getProject()
   */
  @Override
  public IProject getProject( )
  {
    return m_project;
  }

  /**
   * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
   */
  @Override
  public void setProject( final IProject project )
  {
    if( m_project != null )
      ResourcesPlugin.getWorkspace().removeResourceChangeListener( this );

    m_project = project;

    if( m_project != null )
    {
      ResourcesPlugin.getWorkspace().addResourceChangeListener( this );
      try
      {
        reloadMetadata();
      }
      catch( final CoreException e )
      {
        // just ignore; this will happen when a new prjoject is created
      }
    }

  }

  public void dispose( )
  {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener( this );
  }

  public static String checkCanCreateCalculationCase( final IPath path )
  {
    final IWorkspaceRoot resourceRoot = ResourcesPlugin.getWorkspace().getRoot();
    final IResource resource = resourceRoot.findMember( path );

    if( resource == null || resource == resourceRoot )
      return Messages.getString( "org.kalypso.simulation.ui.calccase.ModelNature.1" ); //$NON-NLS-1$

    if( resource instanceof IFolder )
    {
      final IFolder folder = (IFolder) resource;
      if( isCalcCalseFolder( folder ) )
        return Messages.getString( "org.kalypso.simulation.ui.calccase.ModelNature.2" ); //$NON-NLS-1$

      return checkCanCreateCalculationCase( folder.getParent().getFullPath() );
    }
    else if( resource instanceof IProject )
    {
      final IProject project = (IProject) resource;
      try
      {
        project.isNatureEnabled( ModelNature.ID );
        return null;
      }
      catch( final CoreException e )
      {
        e.printStackTrace();

        return e.getMessage();
      }
    }

    return "???"; //$NON-NLS-1$
  }

  public static boolean isCalcCalseFolder( final IContainer folder )
  {
    final IResource calcFile = folder.findMember( CONTROL_NAME );

    return calcFile != null && calcFile.exists() && calcFile instanceof IFile;
  }

  public IStatus launchAnt( final String progressText, final String launchName, final Map<String, Object> antProps, final IContainer folder, final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( progressText, 1000 ); //$NON-NLS-1$

    final IStringVariableManager svm = VariablesPlugin.getDefault().getStringVariableManager();
    IValueVariable[] userVariables = null;

    try
    {
      monitor.subTask( Messages.getString( "org.kalypso.simulation.ui.calccase.ModelNature.3", launchName ) ); //$NON-NLS-1$

      final IFile launchFile = getLaunchFile( launchName );
      final Properties userProperties = createVariablesForAntLaunch( folder );
      if( antProps != null )
        userProperties.putAll( antProps );

      // add user-variables to variable-manager (so they can also be used within the launch-file
      userVariables = registerValueVariablesFromProperties( svm, userProperties );

      final AntLauncher antLauncher = new AntLauncher( launchFile, userProperties );
      antLauncher.init();

      monitor.worked( 10 );

      final IStatus status = antLauncher.execute( new SubProgressMonitor( monitor, 990 ) );

      final File logFile = antLauncher.getLogFile();
      if( logFile == null )
        return status;

      final IStatus[] logStati = LogAnalyzer.logfileToStatus( logFile, Charset.defaultCharset().name() );
      final IStatus[] groupedStati = LogAnalyzer.groupStati( logStati );
      return new MultiStatus( KalypsoSimulationUIPlugin.getID(), -1, groupedStati, "Log-File was analyzed: " + logFile.getAbsolutePath(), null ); //$NON-NLS-1$
    }
    catch( final CoreException e )
    {
      throw e;
    }
    catch( final InterruptedException e )
    {
      return Status.CANCEL_STATUS;
    }
    catch( final Throwable e )
    {
      e.printStackTrace();
      // sollte eigentlich nie auftreten
      return StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.simulation.ui.calccase.ModelNature.6" ) ); //$NON-NLS-1$
    }
    finally
    {
      // remove userVariables, to not pollute the Singleton
      if( userVariables != null )
        svm.removeVariables( userVariables );

      // alle resourcen des CalcCase refreshen
      // intern the parent of folder will used as rule

      /*
       * In order to have a good error message we need to check if the monitor is canceled (this is the case if an
       * exception was thrown before).
       */
      final IProgressMonitor subMon = monitor.isCanceled() ? new NullProgressMonitor() : new SubProgressMonitor( monitor, 1000 );
      // FIXME: This should not be necessary and is also a major performance problem, as the folder might be very big.
      // The ant-launch itself is responsible to refresh any changed resources.
      folder.refreshLocal( IResource.DEPTH_INFINITE, subMon );

      monitor.done();
    }
  }

  private static IValueVariable[] registerValueVariablesFromProperties( final IStringVariableManager svm, final Properties properties )
  {
    final IValueVariable[] variables = new IValueVariable[properties.size()];
    int count = 0;
    for( final Map.Entry<Object, Object> entry : properties.entrySet() )
    {
      final String name = (String) entry.getKey();
      final String value = (String) entry.getValue();

      final IValueVariable valueVariable = svm.newValueVariable( name, value, true, value );
      variables[count++] = valueVariable;

      try
      {
        final IValueVariable existingVariable = svm.getValueVariable( name );
        if( existingVariable == null )
        {
          // add each variable separatedly, because it may have allready been registered
          svm.addVariables( new IValueVariable[] { valueVariable } );
        }
        else
        {
          existingVariable.setValue( value );
        }
      }
      catch( final CoreException e )
      {
        System.out.println( "Variable already exists: " + name ); //$NON-NLS-1$
        e.printStackTrace();
        // ignore it, its already there -> ?
      }
    }

    return variables;
  }

  private Properties createVariablesForAntLaunch( final IContainer folder ) throws CoreException
  {
    final Properties attributes = new Properties();
    final IProject project = folder.getProject();

    final KalypsoAuthPlugin authPlugin = KalypsoAuthPlugin.getDefault();
    final IKalypsoUser currentUser = authPlugin.getCurrentUser();
    final Date now = new Date();

    // auf x stunden vorher runden! hängt von der Modellspec ab
    final Calendar cal = Calendar.getInstance();
    cal.setTimeZone( KalypsoCorePlugin.getDefault().getTimeZone() );
    cal.setTime( now );

    attributes.setProperty( "kalypso.currentTime", DatatypeConverter.printDateTime( cal ) ); //$NON-NLS-1$

    // erstmal auf die letzte Stunde runden
    cal.set( Calendar.MINUTE, 0 );
    cal.set( Calendar.SECOND, 0 );
    cal.set( Calendar.MILLISECOND, 0 );

    // jetzt solange ganze stunden abziehen, bis der Wert ins
    // Zeitvalidierungsschema passt
    int count = 0;
    while( !validateTime( cal ) )
    {
      cal.add( Calendar.HOUR_OF_DAY, -1 );

      // nach 24h spätestens abbrechen!
      count++;
      if( count == 24 )
        throw new CoreException( new Status( IStatus.ERROR, KalypsoSimulationUIPlugin.getID(), Messages.getString( "org.kalypso.simulation.ui.calccase.ModelNature.7" ) + cal ) ); //$NON-NLS-1$
    }

    attributes.setProperty( "kalypso.startforecast", DatatypeConverter.printDateTime( cal ) ); //$NON-NLS-1$

    // standardzeit abziehen
    final int simDiff = new Integer( m_metadata.getProperty( META_PROP_DEFAULT_SIMHOURS, "120" ) ).intValue(); //$NON-NLS-1$
    cal.add( Calendar.HOUR_OF_DAY, -simDiff );

    attributes.setProperty( "kalypso.startsim", DatatypeConverter.printDateTime( cal ) ); //$NON-NLS-1$

    attributes.setProperty( "kalypso.currentUser", currentUser.getUserName() ); //$NON-NLS-1$

    attributes.setProperty( "simulation_project_loc", project.getLocation().toPortableString() ); //$NON-NLS-1$

    attributes.setProperty( "calc.dir", folder.getLocation().toPortableString() ); //$NON-NLS-1$
    attributes.setProperty( "project.dir", project.getLocation().toPortableString() ); //$NON-NLS-1$

    attributes.setProperty( "calc.path", folder.getFullPath().toPortableString() ); //$NON-NLS-1$
    attributes.setProperty( "project.path", project.getFullPath().toPortableString() ); //$NON-NLS-1$

    try
    {
      attributes.setProperty( "calc.url", ResourceUtilities.createURL( folder ).toString() ); //$NON-NLS-1$
      attributes.setProperty( "project.url", ResourceUtilities.createURL( project ).toString() ); //$NON-NLS-1$
    }
    catch( final MalformedURLException e )
    {
      // should never happen
      e.printStackTrace();
    }

    return attributes;
  }

  private IFile getLaunchFile( final String launchName ) throws CoreException
  {
    final IFolder launchFolder = getLaunchFolder();
    if( launchFolder == null )
      throw new CoreException( new Status( IStatus.ERROR, KalypsoSimulationUIPlugin.getID(), Messages.getString( "org.kalypso.simulation.ui.calccase.ModelNature.8" ) ) ); //$NON-NLS-1$

    return launchFolder.getFile( launchName + ".launch" );
  }

  private IFolder getLaunchFolder( )
  {
    return getModelFolder().getFolder( "launch" ); //$NON-NLS-1$
  }

  /**
   * Erzeugt eine neue Rechenvariante im angegebenen Ordner
   */
  public IStatus createCalculationCaseInFolder( final IFolder folder, final Map<String, Object> antProperties, final IProgressMonitor monitor ) throws CoreException
  {
    final String message = Messages.getString( "org.kalypso.simulation.ui.calccase.ModelNature.9" ); //$NON-NLS-1$
    return launchAnt( message, "createCalcCase", antProperties, folder, monitor ); //$NON-NLS-1$
  }

  /**
   * Aktualisiert eine vorhandene Rechenvariante
   */
  public IStatus updateCalcCase( final IFolder folder, final IProgressMonitor monitor ) throws CoreException
  {
    final String message = Messages.getString( "org.kalypso.simulation.ui.calccase.ModelNature.10" ); //$NON-NLS-1$
    return launchAnt( message, "updateCalcCase", null, folder, monitor ); //$NON-NLS-1$
  }

  /**
   * Übernimmt das Modell aus der Rechenvariante in das Basismodell
   */
  public IStatus setBasicModel( final IFolder folder, final IProgressMonitor monitor ) throws CoreException
  {
    final String message = Messages.getString( "org.kalypso.simulation.ui.calccase.ModelNature.11" ); //$NON-NLS-1$

    return launchAnt( message, "setBasicModel", null, folder, monitor ); //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
   */
  @Override
  public void resourceChanged( final IResourceChangeEvent event )
  {
    final IResourceDelta delta = event.getDelta();
    final IFile metadataFile = getMetadataFile();
    if( delta == null || metadataFile == null )
      return;

    final IResourceDelta metadataDelta = delta.findMember( metadataFile.getFullPath() );
    if( metadataDelta == null )
      return;

    switch( metadataDelta.getKind() )
    {
      case IResourceDelta.ADDED:
      case IResourceDelta.REMOVED:
      case IResourceDelta.CHANGED:
      {
        try
        {
          // always test for existence before reloading data
          // since project might have been deleted by the user
          if( getMetadataFile().exists() )
            reloadMetadata();
        }
        catch( final CoreException e )
        {
          // todo: error handling? -->> als job absetzen?
          e.printStackTrace();
        }
        break;
      }
    }
  }

  private void reloadMetadata( ) throws CoreException
  {
    InputStream contents = null;
    try
    {
      m_metadata.clear();

      final IFile file = getMetadataFile();
      contents = file.getContents();
      m_metadata.load( contents );
      contents.close();
    }
    catch( final IOException e )
    {
      throw new CoreException( new Status( IStatus.ERROR, KalypsoSimulationUIPlugin.getID(), 0, Messages.getString( "org.kalypso.simulation.ui.calccase.ModelNature.12" ), e ) ); //$NON-NLS-1$
    }
    finally
    {
      IOUtils.closeQuietly( contents );
    }
  }

  /**
   * Returns a metadatum associated with this nature.
   * 
   * @param key
   *          One of the METADATA_KEY_ conmstants.
   * @return The value of the given key; or <code>null</code> if not set.
   */
  public String getMetadata( final String key )
  {
    return m_metadata.getProperty( key );
  }

  private IFolder getModelFolder( )
  {
    return m_project.getFolder( MODELLTYP_FOLDER );
  }

  public GMLWorkspace loadOrCreateControl( final IContainer folder ) throws CoreException
  {
    try
    {
      if( folder == null )
        throw new IllegalArgumentException();

      final IFile controlFile = folder.getFile( new Path( CONTROL_NAME ) );
      final String gmlPath = controlFile.getFullPath().toString();
      final URL gmlURL = new URL( "platform:/resource/" + gmlPath ); //$NON-NLS-1$

      return GmlSerializer.createGMLWorkspace( gmlURL, null );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      throw new CoreException( new Status( IStatus.ERROR, KalypsoSimulationUIPlugin.getID(), 0, Messages.getString( "org.kalypso.simulation.ui.calccase.ModelNature.14" ) + e.getLocalizedMessage(), e ) ); //$NON-NLS-1$
    }
  }

  /**
   * stellt fest, ob es sich um einen gültigen Zeitpunkt für den Start der Prognose handelt
   * 
   * @param cal
   * @return true when time is valid
   */
  private boolean validateTime( final Calendar cal )
  {
    // todo: wäre schöner, wenn das besser parametrisiert werden könnte
    // z.B. ein Groovy-Skript aus der Modelspec o.ä.
    final String validHours = m_metadata.getProperty( META_PROP_VALID_HOURS, "VALID_FORECAST_HOURS=0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23" ); //$NON-NLS-1$

    final int hour = cal.get( Calendar.HOUR_OF_DAY );

    return (" " + validHours + " ").indexOf( " " + hour + " " ) != -1; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  public IStatus runCalculation( final IContainer calcCaseFolder, final IProgressMonitor monitor ) throws CoreException
  {
    return launchAnt( STR_MODELLRECHNUNG_WIRD_DURCHGEFUEHRT, "runCalculation", null, calcCaseFolder, monitor ); //$NON-NLS-1$
  }

  /**
   * Load the calculation and read the value for the given property
   * 
   * @param calcCase
   * @param propertyName
   *          name of the property to read value for
   * @return value of the property to read
   */
  public Object loadCalculationAndReadProperty( final IContainer calcCase, final String propertyName ) throws CoreException
  {
    // load .calculation, dont create one if not existent
    final GMLWorkspace workspace = loadOrCreateControl( calcCase );
    if( workspace == null )
      return null;

    final Feature rootFeature = workspace.getRootFeature();

    final FindPropertyByNameVisitor vis = new FindPropertyByNameVisitor( propertyName );
    workspace.accept( vis, rootFeature, FeatureVisitor.DEPTH_INFINITE );

    return vis.getResult();
  }
}