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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.filters.ReplaceTokens.Token;
import org.eclipse.ant.internal.ui.launchConfigurations.IAntLaunchConfigurationConstants;
import org.eclipse.core.internal.variables.ValueVariable;
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsUtil;
import org.kalypso.auth.KalypsoAuthPlugin;
import org.kalypso.auth.scenario.IScenario;
import org.kalypso.auth.scenario.Scenario;
import org.kalypso.auth.user.IKalypsoUser;
import org.kalypso.commons.java.net.UrlResolver;
import org.kalypso.commons.runtime.LogStatusWrapper;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.io.CharsetUtilities;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.gmlschema.Mapper;
import org.kalypso.jwsdp.JaxbUtilities;
import org.kalypso.model.xml.CalcCaseConfig;
import org.kalypso.model.xml.Modeldata;
import org.kalypso.model.xml.ObjectFactory;
import org.kalypso.model.xml.TransformationList;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.simulation.core.ISimulationService;
import org.kalypso.simulation.core.KalypsoSimulationCorePlugin;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.util.transformation.TransformationHelper;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.visitors.FindPropertyByNameVisitor;
import org.xml.sax.InputSource;

/**
 * @author belger
 */
public class ModelNature implements IProjectNature, IResourceChangeListener
{
  private static final JAXBContext JC = JaxbUtilities.createQuiet( ObjectFactory.class );

  private static final String STR_MODELLRECHNUNG_WIRD_DURCHGEFUEHRT = "Modellrechnung wird durchgeführt";

  public static final String MODELLTYP_FOLDER = ".model";

  public static final String MODELLTYP_CALCCASECONFIG_XML = MODELLTYP_FOLDER + "/" + "calcCaseConfig.xml";

  public static final String MODELLTYP_MODELSPEC_XML = "modelspec.xml";

  public static final String ID = KalypsoSimulationUIPlugin.getID() + ".ModelNature";

  private static final String METADATA_FILE = ".metadata";

  public static final String CONTROL_NAME = ".calculation";

  public static final String CONTROL_TEMPLATE_NAME = ".calculation.template";

  public static final String CONTROL_VIEW_PATH = MODELLTYP_FOLDER + "/.calculation.view";

  public static final String MODELLTYP_CALCWIZARD_XML = MODELLTYP_FOLDER + "/" + "calcWizard.xml";

  private final Properties m_metadata = new Properties();

  private IProject m_project;

  public static final String PROGNOSE_FOLDER = ".prognose";

  public static final String CONTROL_TEMPLATE_GML_PATH = MODELLTYP_FOLDER + "/" + CONTROL_TEMPLATE_NAME;

  private static final String META_PROP_VALID_HOURS = "VALID_FORECAST_HOURS";

  /** Standardddifferenz des Simulationsstarts vor dem Vorhersagezeitpunkt */
  private static final String META_PROP_DEFAULT_SIMHOURS = "DEFAULT_SIMHOURS";

  private static final int TRANS_TYPE_UPDTAE = 0;

  private static final int TRANS_TYPE_CREATE = 1;

  /**
   * 2005-09-01 - Schlienger - added this constant in order to get a static dependency to the org.eclipse.ant.ui plugin
   * (else it is only a runtime-dependency), thus the dependency is checked by the manifest editor and the plugin is
   * kept in the dependency list. Not doing this results in problems at runtime if the org.eclipse.ant.ui plugin is
   * missing.
   * <p>
   * This is the id to use in your own launch files.
   * <p>
   * Example of an launch-file:
   * 
   * <pre>
   *    &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
   *    &lt;launchConfiguration type=&quot;org.eclipse.ant.AntLaunchConfigurationType&quot;&gt;
   *    &lt;stringAttribute key=&quot;process_factory_id&quot; value=&quot;org.eclipse.ant.ui.remoteAntProcessFactory&quot;/&gt;
   *    &lt;booleanAttribute key=&quot;org.eclipse.debug.ui.ATTR_LAUNCH_IN_BACKGROUND&quot; value=&quot;false&quot;/&gt;
   *    &lt;stringAttribute key=&quot;org.eclipse.ui.externaltools.ATTR_TOOL_ARGUMENTS&quot; value=&quot;-q -e -logfile ${calc.dir}/afterCalc.log&quot;/&gt;
   *    &lt;booleanAttribute key=&quot;org.eclipse.ui.externaltools.ATTR_CAPTURE_OUTPUT&quot; value=&quot;false&quot;/&gt;
   *    &lt;stringAttribute key=&quot;org.eclipse.ui.externaltools.ATTR_LOCATION&quot; value=&quot;${project_loc}/.model/launch/build.xml&quot;/&gt;
   *    &lt;booleanAttribute key=&quot;org.eclipse.debug.core.appendEnvironmentVariables&quot; value=&quot;true&quot;/&gt;
   *    &lt;stringAttribute key=&quot;org.eclipse.ant.ui.ATTR_BUILD_SCOPE&quot; value=&quot;${none}&quot;/&gt;
   *    &lt;stringAttribute key=&quot;org.eclipse.jdt.launching.CLASSPATH_PROVIDER&quot; value=&quot;org.eclipse.ant.ui.AntClasspathProvider&quot;/&gt;
   *    &lt;stringAttribute key=&quot;org.eclipse.ui.externaltools.ATTR_ANT_TARGETS&quot; value=&quot;afterCalc,&quot;/&gt;
   *    &lt;/launchConfiguration&gt;
   *  &lt;pre&gt;
   * 
   */
  public static final String ANT_LAUNCH_CONFIGURATION_TYPE = IAntLaunchConfigurationConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE;

  /**
   * @see org.eclipse.core.resources.IProjectNature#configure()
   */
  public void configure( )
  {
    // nix tun
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
  public void deconfigure( ) throws CoreException
  {
    // todo: wird nie aufgerufen!
    try
    {
      final IFile file = getMetadataFile();

      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      m_metadata.store( bos, "Modell-Projekt Metadata Information" );
      bos.close();

      final ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() );

      if( file.exists() )
        file.setContents( bis, false, true, new NullProgressMonitor() );
      else
        file.create( bis, false, new NullProgressMonitor() );

      bis.close();
    }
    catch( IOException e )
    {
      e.printStackTrace();
    }
  }

  /**
   * @see org.eclipse.core.resources.IProjectNature#getProject()
   */
  public IProject getProject( )
  {
    return m_project;
  }

  /**
   * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
   */
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
        // todo: als job absetzen?
        e.printStackTrace();
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
      return "Rechenvariante muss innerhalb eines Projektordners angelegt werden";

    if( resource instanceof IFolder )
    {
      final IFolder folder = (IFolder) resource;
      if( isCalcCalseFolder( folder ) )
        return "Rechenvariante darf nicht innerhalb einer anderen Rechenvariante angelegt werden";

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
      catch( CoreException e )
      {
        e.printStackTrace();

        return e.getMessage();
      }
    }

    return "???";
  }

  public static boolean isCalcCalseFolder( final IFolder folder )
  {
    final IResource calcFile = folder.findMember( CONTROL_NAME );
    return (calcFile != null && calcFile.exists() && calcFile instanceof IFile);
  }

  public CalcCaseConfig readCalcCaseConfig( final IFolder folder ) throws CoreException
  {
    final IFile tranformerConfigFile = getTransformerConfigFile();
    try
    {
      // Protokolle ersetzen
      final ReplaceTokens replaceReader = new ReplaceTokens( new InputStreamReader( tranformerConfigFile.getContents(), tranformerConfigFile.getCharset() ) );

      configureReplaceTokensForCalcCase( folder, replaceReader );

      return (CalcCaseConfig) JC.createUnmarshaller().unmarshal( new InputSource( replaceReader ) );
    }
    catch( final UnsupportedEncodingException e )
    {
      e.printStackTrace();

      throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), 0, "Fehler beim Lesen der Konfiguration: " + tranformerConfigFile.getProjectRelativePath().toString(), e ) );
    }
    catch( final JAXBException e )
    {
      e.printStackTrace();
      throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), 0, "Fehler beim Lesen der Konfiguration: " + tranformerConfigFile.getProjectRelativePath().toString(), e ) );
    }
  }

  private IFile getTransformerConfigFile( )
  {
    final IProject project = getProject();
    final IFile tranformerConfigFile = project.getFile( ModelNature.MODELLTYP_CALCCASECONFIG_XML );
    return tranformerConfigFile;
  }

  public IStatus launchAnt( final String progressText, final String launchName, final Map<String, Object> antProps, final IFolder folder, final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( progressText, 5000 );

    final IStringVariableManager svm = VariablesPlugin.getDefault().getStringVariableManager();
    IValueVariable[] userVariables = null;

    try
    {
      final Properties userProperties = createVariablesForAntLaunch( folder );

      final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();

      final IFile launchFile = getLaunchFile( launchName );
      final ILaunchConfigurationWorkingCopy lc = launchManager.getLaunchConfiguration( launchFile ).getWorkingCopy();

      // add user-variables to LaunchConfiguration
      final Map<Object, Object> attribute = lc.getAttribute( "org.eclipse.ui.externaltools.ATTR_ANT_PROPERTIES", new HashMap() );
      attribute.putAll( userProperties );
      if( antProps != null )
        attribute.putAll( antProps );
      lc.setAttribute( "org.eclipse.ui.externaltools.ATTR_ANT_PROPERTIES", attribute );

      // add user-variables to variable-manager (so they can also be used within
      // the launch-file
      userVariables = registerValueVariablesFromProperties( svm, userProperties );
      monitor.worked( 1000 );

      // TODO prüfen ob der launch überhaupt asynchron laufen kann?
      final ILaunch launch = lc.launch( ILaunchManager.RUN_MODE, new SubProgressMonitor( monitor, 3000 ) );

      // TODO: timeout konfigurierbar machen?
      final int minutes = 4;
      for( int i = 0; i < 60 * minutes; i++ )
      {
        if( launch.isTerminated() )
        {
          final String[] arguments = ExternalToolsUtil.getArguments( lc );
          for( int j = 0; j < arguments.length; j++ )
          {
            if( arguments[j].equals( "-l" ) || arguments[j].equals( "-logfile" ) && j != arguments.length - 1 )
            {
              final String logfile = arguments[j + 1];
              return new LogStatusWrapper( new File( logfile ), CharsetUtilities.getDefaultCharset() ).toStatus();
            }
          }
          return Status.OK_STATUS;
        }
        Thread.sleep( 1000 );
      }

      // TODO better ask for termination, but continue task in background
      launch.terminate();

      return StatusUtilities.createErrorStatus( "Die Operation hat über " + minutes + " Minuten gedauert und wird deshalb abgebrochen." );
    }
    catch( final InterruptedException e )
    {
      e.printStackTrace();
      // sollte eigentlich nie auftreten
      return StatusUtilities.statusFromThrowable( e, "Operation konnte nicht durchgef[hrt werden." );
    }
    finally
    {
      // remove userVariables, to not pollute the Singleton
      if( userVariables != null )
        svm.removeVariables( userVariables );

      // alle resourcen des CalcCase refreshen
      // intern the parent of folder will used as rule
      folder.refreshLocal( IResource.DEPTH_INFINITE, new SubProgressMonitor( monitor, 1000 ) );

      monitor.done();
    }
  }

  private static final IValueVariable[] registerValueVariablesFromProperties( final IStringVariableManager svm, final Properties properties )
  {
    final IValueVariable[] variables = new IValueVariable[properties.size()];
    int count = 0;
    for( final Iterator iter = properties.entrySet().iterator(); iter.hasNext(); )
    {
      final Map.Entry entry = (Entry) iter.next();
      final String name = (String) entry.getKey();
      final String value = (String) entry.getValue();

      final IValueVariable valueVariable = new ValueVariable( name, value, null );
      valueVariable.setValue( value );
      variables[count++] = valueVariable;

      try
      {
        // add each variable separatedly, because it may have allready been registered
        svm.addVariables( new IValueVariable[] { valueVariable } );
      }
      catch( final CoreException e )
      {
        e.printStackTrace();
        // ignore it, its already there -> ?
      }
    }

    return variables;
  }

  private Properties createVariablesForAntLaunch( final IFolder folder ) throws CoreException
  {
    final Properties attributes = new Properties();
    final IProject project = folder.getProject();
    final IScenario scenario = getScenario( folder );

    attributes.setProperty( "kalypso.scenario", scenario.getId() );

    attributes.setProperty( "simulation_project_loc", project.getLocation().toOSString() );

    attributes.setProperty( "calc.dir", folder.getLocation().toOSString() );
    attributes.setProperty( "project.dir", project.getLocation().toOSString() );

    attributes.setProperty( "calc.path", folder.getFullPath().toOSString() );
    attributes.setProperty( "project.path", project.getFullPath().toOSString() );

    try
    {
      attributes.setProperty( "calc.url", ResourceUtilities.createURL( folder ).toString() );
      attributes.setProperty( "project.url", ResourceUtilities.createURL( project ).toString() );
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
      throw new CoreException( StatusUtilities.createErrorStatus( "Launch Verzeichnis im Modellverzeichnis existiert nicht." ) );

    final IFile file = launchFolder.getFile( launchName + ".launch" );
    return file;
  }

  private IFolder getLaunchFolder( )
  {
    return getModelFolder().getFolder( "launch" );
  }

  /**
   * Erzeugt eine neue Rechenvariante im angegebenen Ordner
   */
  public IStatus createCalculationCaseInFolder( final IFolder folder, final IProgressMonitor monitor ) throws CoreException
  {
    final String message = "Rechenvariante wird erzeugt";

    if( getTransformerConfigFile().exists() )
      return doCalcTransformation( message, TRANS_TYPE_CREATE, folder, monitor );

    return launchAnt( message, "createCalcCase", null, folder, monitor );
  }

  /**
   * Aktualisiert eine vorhandene Rechenvariante
   */
  public IStatus updateCalcCase( final IFolder folder, final IProgressMonitor monitor ) throws CoreException
  {
    final String message = "Rechenvariante wird aktualisiert";

    if( getTransformerConfigFile().exists() )
      return doCalcTransformation( message, TRANS_TYPE_UPDTAE, folder, monitor );

    return launchAnt( message, "updateCalcCase", null, folder, monitor );
  }

  /**
   * Führt eine Transformation auf einer Rechenvariante durch
   * 
   * @deprecated use ant launch framework instead. Leave here until all models have been ported to the new system.
   */
  @Deprecated
  private IStatus doCalcTransformation( final String taskName, final int type, final IFolder folder, final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( taskName, 2000 );

    try
    {
      final CalcCaseConfig trans = readCalcCaseConfig( folder );

      monitor.worked( 1000 );

      // Daten transformieren
      TransformationList transList = null;
      switch( type )
      {
        case TRANS_TYPE_UPDTAE:
          transList = trans.getUpdateTransformations();
          break;

        case TRANS_TYPE_CREATE:
          transList = trans.getCreateTransformations();
          break;

        default:
          transList = null;
          break;
      }

      if( transList == null )
        return Status.OK_STATUS;

      return TransformationHelper.doTranformations( folder, transList, new SubProgressMonitor( monitor, 1000 ) );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();

      throw e;
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), 0, taskName + ": " + e.getLocalizedMessage(), e ) );
    }
    finally
    {
      monitor.done();
    }
  }

  /**
   * fügt eine Reihe von Tokens zum ReplaceToken hinzu. Unter anderem für :project: etc Ausserdem werden die Start,
   * Mittel und Endzeit der Simulation aus der Rechenvariante ausgelesen (.calculation) und als Token hinzugefgügt.
   */
  private void configureReplaceTokensForCalcCase( final IFolder calcFolder, final ReplaceTokens replaceTokens ) throws CoreException
  {
    replaceTokens.setBeginToken( ':' );
    replaceTokens.setEndToken( ':' );

    final Token timeToken = new ReplaceTokens.Token();
    timeToken.setKey( "SYSTEM_TIME" );
    timeToken.setValue( new SimpleDateFormat( "dd.MM.yyyy HH:mm" ).format( new Date( System.currentTimeMillis() ) ) );
    replaceTokens.addConfiguredToken( timeToken );

    final Token calcdirToken = new ReplaceTokens.Token();
    calcdirToken.setKey( "calcdir" );
    calcdirToken.setValue( calcFolder.getFullPath().toString() + "/" );
    replaceTokens.addConfiguredToken( calcdirToken );

    final Token projectToken = new ReplaceTokens.Token();
    projectToken.setKey( "project" );
    projectToken.setValue( calcFolder.getProject().getFullPath().toString() + "/" );
    replaceTokens.addConfiguredToken( projectToken );

    // jetzt werte aus der .calculation des aktuellen Rechenfalls lesen und
    // bestimmte Werte
    // zum Ersetzen auslesen

    final GMLWorkspace workspace = loadOrCreateControl( calcFolder );
    if( workspace != null )
    {
      final Feature rootFeature = workspace.getRootFeature();

      final FindPropertyByNameVisitor startsimFinder = new FindPropertyByNameVisitor( "startsimulation" );
      workspace.accept( startsimFinder, rootFeature, FeatureVisitor.DEPTH_INFINITE );

      final Object startSim = startsimFinder.getResult();
      if( startSim instanceof Date )
      {
        final String startSimString = Mapper.mapJavaValueToXml( startSim );
        final Token startSimToken = new ReplaceTokens.Token();
        startSimToken.setKey( "startsim" );
        startSimToken.setValue( startSimString );
        replaceTokens.addConfiguredToken( startSimToken );
      }

      final FindPropertyByNameVisitor startforecastFinder = new FindPropertyByNameVisitor( "startforecast" );
      workspace.accept( startforecastFinder, rootFeature, FeatureVisitor.DEPTH_INFINITE );
      final Object startForecast = startforecastFinder.getResult();
      if( startForecast instanceof Date )
      {
        final String startForecastString = Mapper.mapJavaValueToXml( startForecast );
        final Token startForecastToken = new ReplaceTokens.Token();
        startForecastToken.setKey( "startforecast" );
        startForecastToken.setValue( startForecastString );
        replaceTokens.addConfiguredToken( startForecastToken );

        // TODO: ziemlicher hack für den Endzeitpunkt: er ist immer fix
        // 48 Stunden nach dem startzeitpunkt
        final Calendar cal = Calendar.getInstance();
        cal.setTime( (Date) startForecast );
        cal.add( Calendar.HOUR_OF_DAY, 48 );

        final Date endSim = cal.getTime();
        final String endSimString = Mapper.mapJavaValueToXml( endSim );
        final Token endSimToken = new ReplaceTokens.Token();
        endSimToken.setKey( "endsim" );
        endSimToken.setValue( endSimString );
        replaceTokens.addConfiguredToken( endSimToken );
      }
    }
  }

  public String getCalcType( ) throws CoreException
  {
    final Modeldata modelspec = getModelspec( MODELLTYP_MODELSPEC_XML );

    return modelspec.getTypeID();
  }

  /**
   * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
   */
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
    try
    {
      m_metadata.clear();

      final IFile file = getMetadataFile();
      m_metadata.load( file.getContents() );
    }
    catch( final IOException e )
    {
      throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), 0, "Error loading Metadata", e ) );
    }
  }

  private IFolder getModelFolder( )
  {
    return m_project.getFolder( MODELLTYP_FOLDER );
  }

  private Modeldata getModelspec( final String modelSpec ) throws CoreException
  {
    try
    {
      final IFile file = getModelFolder().getFile( modelSpec );
      if( !file.exists() )
        return null;

      final Unmarshaller unmarshaller = JC.createUnmarshaller();
      return (Modeldata) unmarshaller.unmarshal( file.getContents() );
    }
    catch( final JAXBException e )
    {
      e.printStackTrace();

      throw new CoreException( StatusUtilities.statusFromThrowable( e, "Fehler beim Laden der Modell-Spezifikation" ) );
    }
  }

  public GMLWorkspace loadDefaultControl( ) throws CoreException
  {
    return loadOrCreateControl( null );
  }

  public GMLWorkspace loadOrCreateControl( final IFolder folder ) throws CoreException
  {
    try
    {
      // gibts das file schon, dann laden
      String gmlPath = getProject().getName() + "/" + CONTROL_TEMPLATE_GML_PATH;

      if( folder != null )
      {
        final IFile controlFile = folder.getFile( CONTROL_NAME );
        if( controlFile.exists() )
          gmlPath = controlFile.getFullPath().toString();
      }

      final URL gmlURL = new URL( "platform:/resource/" + gmlPath );

      final IUrlResolver urlResolver = configureTokensForcontrol();

      return GmlSerializer.createGMLWorkspace( gmlURL, urlResolver );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), 0, "Konnte Standard-Steuerparameter nicht laden:" + e.getLocalizedMessage(), e ) );
    }
  }

  public IUrlResolver configureTokensForcontrol( ) throws CoreException
  {
    final IUrlResolver urlResolver = new UrlResolver();

    final KalypsoAuthPlugin authPlugin = KalypsoAuthPlugin.getDefault();
    final IKalypsoUser currentUser = authPlugin.getCurrentUser();
    final String scenarioId = currentUser.getScenario();
    final IScenario scenario = authPlugin.getScenario( scenarioId );

    urlResolver.addReplaceToken( "scenarioId", scenario.getId() );
    urlResolver.addReplaceToken( "scenarioName", scenario.getName() );
    urlResolver.addReplaceToken( "scenarioDescription", scenario.getDescription() );
    urlResolver.addReplaceToken( "user", currentUser.getUserName() );

    final Date now = new Date();
    urlResolver.addReplaceToken( "time", Mapper.mapJavaValueToXml( now ) );

    // auf x stunden vorher runden! hängt von der Modellspec ab
    final Calendar cal = Calendar.getInstance();
    cal.setTime( now );
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
        throw new CoreException( StatusUtilities.createErrorStatus( "Zeit konnte nicht vailidiert werden: " + cal ) );
    }

    final Date forecastTime = cal.getTime();
    urlResolver.addReplaceToken( "startforecast", Mapper.mapJavaValueToXml( forecastTime ) );

    // standardzeit abziehen
    final int simDiff = new Integer( m_metadata.getProperty( META_PROP_DEFAULT_SIMHOURS, "120" ) ).intValue();
    cal.add( Calendar.HOUR_OF_DAY, -simDiff );
    final Date simTime = cal.getTime();
    urlResolver.addReplaceToken( "startsim", Mapper.mapJavaValueToXml( simTime ) );

    return urlResolver;
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
    final String validHours = m_metadata.getProperty( META_PROP_VALID_HOURS, "VALID_FORECAST_HOURS=0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23" );

    final int hour = cal.get( Calendar.HOUR_OF_DAY );

    return (" " + validHours + " ").indexOf( " " + hour + " " ) != -1;
  }

  public IStatus runCalculation( final IFolder folder, final IProgressMonitor monitor ) throws CoreException
  {
    return runCalculation( folder, monitor, MODELLTYP_MODELSPEC_XML );
  }

  public IStatus runCalculation( final IFolder calcCaseFolder, final IProgressMonitor monitor, final String modelSpec ) throws CoreException
  {
    if( modelSpec == null )
      return runCalculation( calcCaseFolder, monitor );

    // for backwards compability
    // new models always should use the ant task
    final Modeldata modelspec = getModelspec( modelSpec );
    if( modelspec == null )
    {
      // if there is no 'modelspec' file
      // we try to call the ant task
      return launchAnt( STR_MODELLRECHNUNG_WIRD_DURCHGEFUEHRT, "runCalculation", null, calcCaseFolder, monitor );
    }

    return runCalculation( calcCaseFolder, monitor, modelspec );
  }

  public IStatus runCalculation( final IFolder calcCaseFolder, final IProgressMonitor monitor, final Modeldata modelspec ) throws CoreException
  {
    monitor.beginTask( STR_MODELLRECHNUNG_WIRD_DURCHGEFUEHRT, 5500 );

    try
    {
      if( !isCalcCalseFolder( calcCaseFolder ) )
        throw new CoreException( StatusUtilities.createErrorStatus( "Verzeichnis ist keine Rechenvariante:" + calcCaseFolder.getName() ) );

      final String typeID = modelspec.getTypeID();
      final ISimulationService calcService = KalypsoSimulationCorePlugin.findCalculationServiceForType( typeID );

      final CalcJobHandler cjHandler = new CalcJobHandler( modelspec, calcService );
      return cjHandler.runJob( calcCaseFolder, new SubProgressMonitor( monitor, 5000 ) );
    }
    finally
    {
      monitor.done();
    }
  }

  /**
   * Returns the scenario for the given calcCase
   */
  public IScenario getScenario( final IFolder calcCase ) throws CoreException
  {
    final Object result = loadCalculationAndReadProperty( calcCase, "scenarioId" );
    final String scenarioId = result == null ? "" : result.toString();

    final IScenario scenario = KalypsoAuthPlugin.getDefault().getScenario( scenarioId );
    if( scenario == null )
      return Scenario.DEFAULT_SCENARIO;

    return scenario;
  }

  /**
   * Load the calculation and read the value for the given property
   * 
   * @param calcCase
   * @param propertyName
   *          name of the property to read value for
   * @return value of the property to read
   */
  public Object loadCalculationAndReadProperty( final IFolder calcCase, final String propertyName ) throws CoreException
  {
    // load .calculation
    final GMLWorkspace workspace = loadOrCreateControl( calcCase );
    final Feature rootFeature = workspace.getRootFeature();

    final FindPropertyByNameVisitor vis = new FindPropertyByNameVisitor( propertyName );
    workspace.accept( vis, rootFeature, FeatureVisitor.DEPTH_INFINITE );

    final Object result = vis.getResult();
    return result;
  }
}