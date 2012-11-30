/**
 *
 */
package org.kalypso.afgui.internal;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.model.ICommandPoster;
import org.kalypso.afgui.scenarios.IScenarioDatum;
import org.kalypso.afgui.scenarios.ScenarioDataExtension;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.PoolableObjectType;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.loader.LoaderException;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;

import de.renew.workflow.connector.cases.IModel;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioDataListener;
import de.renew.workflow.connector.cases.IScenarioDataProvider;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;

/**
 * Objects of this class are responsible for loading the gml-workspaces for the current selected simulation model and
 * provide them to the commands.
 * 
 * @author Gernot Belger
 */
public class SzenarioDataProvider implements IScenarioDataProvider, ICommandPoster
{
  /**
   * Maps the (adapted) feature-wrapper-classes onto the corresponding pool key.
   * <p>
   * At the moment this works, because each gml-file corresponds to exactly one (different) wrapper class.
   */
  final Map<String, ScenarioDataPoolListener> m_keyMap = new HashMap<>();

  private final Set<IScenarioDataListener> m_controller = Collections.synchronizedSet( new LinkedHashSet<IScenarioDataListener>() );

  private IScenario m_scenario = null;

  private String m_dataSetScope;

  private boolean m_fireModelLoaded = true;

  @Override
  public void addScenarioDataListener( final IScenarioDataListener listener )
  {
    m_controller.add( listener );
  }

  @Override
  public void removeScenarioDataListener( final IScenarioDataListener listener )
  {
    m_controller.remove( listener );
  }

  @Override
  public void setCurrent( final IScenario scenario, final IProgressMonitor monitor )
  {
    /* Nothing to do if scenario folder stays the same */
    if( ObjectUtils.equals( m_scenario, scenario ) )
      return;

    /* Release current models && reset state */
    final Map<IModel, IStatus> loadedModels;
    synchronized( this )
    {
      reset();

      if( scenario != null )
      {
        final IProject project = scenario.getProject();
        final ScenarioHandlingProjectNature nature = ScenarioHandlingProjectNature.toThisNatureQuiet( project );
        final IEclipsePreferences afguiNode = nature.getProjectPreference();
        if( afguiNode == null )
          m_dataSetScope = null;
        else
          m_dataSetScope = afguiNode.get( "dataSetScope", null ); //$NON-NLS-1$
      }

      m_scenario = scenario;

      loadedModels = setDataScope( m_scenario, monitor );
    }

    /* fire all events after synchronize block */
    fireCazeChanged( m_scenario );

    for( final Entry<IModel, IStatus> entry : loadedModels.entrySet() )
    {
      final IModel model = entry.getKey();
      final IStatus status = entry.getValue();
      fireModelLoaded( model, status );
    }
  }

  private Map<IModel, IStatus> setDataScope( final IScenario scenario, final IProgressMonitor monitor )
  {
    // Prohibit events during initializing of model loading. Prevent event before new scenario is set.
    m_fireModelLoaded = false;

    final Map<IModel, IStatus> loadedModels = new LinkedHashMap<>();

    if( scenario == null || m_dataSetScope == null )
      return loadedModels;

    final Map<String, IScenarioDatum> locationMap = ScenarioDataExtension.getScenarioDataMap( m_dataSetScope );
    if( locationMap == null )
      return loadedModels;

    monitor.beginTask( Messages.getString("SzenarioDataProvider.0"), locationMap.size() ); //$NON-NLS-1$

    for( final IScenarioDatum entry : locationMap.values() )
    {
      try
      {
        final String id = entry.getID();
        final Class< ? extends IModel> wrapperClass = entry.getModelClass();
        final String gmlLocation = entry.getModelPath();

        monitor.subTask( String.format( Messages.getString("SzenarioDataProvider.1"), gmlLocation ) ); //$NON-NLS-1$

        final IFolder dataFolder = getDataFolder( scenario, gmlLocation );
        final Pair<IModel, IStatus> result = loadModel( dataFolder, id, wrapperClass, gmlLocation );
        if( result != null )
          loadedModels.put( result.getKey(), result.getValue() );
      }
      catch( final CoreException e )
      {
        KalypsoAFGUIFrameworkPlugin.getDefault().getLog().log( e.getStatus() );
      }

      monitor.worked( 1 );
    }

    monitor.done();
    m_fireModelLoaded = true;

    return loadedModels;
  }

  /** HACK: resolve "global" gml file from parent scenario. */
  private IFolder getDataFolder( final IScenario scenario, final String gmlLocation )
  {
    final IFolder dataFolder = resolveFolder( scenario, gmlLocation );
    if( dataFolder == null )
      return scenario.getFolder();

    return dataFolder;
  }

  private static IFolder resolveFolder( final IScenario scene, final String gmlLocation )
  {
    final IFolder folder = scene.getFolder();
    final IFile file = folder.getFile( gmlLocation );
    if( file.exists() )
      return folder;

    final IScenario parent = scene.getParentScenario();
    if( parent != null )
      return resolveFolder( parent, gmlLocation );

    return null;
  }

  /**
   * Releases all models and clears all cached data.
   */
  private void reset( )
  {
    m_scenario = null;
    m_dataSetScope = null;

    ScenarioDataPoolListener[] keys;

    synchronized( this )
    {
      final Collection<ScenarioDataPoolListener> values = m_keyMap.values();
      keys = values.toArray( new ScenarioDataPoolListener[values.size()] );
      m_keyMap.clear();
    }

    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    for( final ScenarioDataPoolListener key : keys )
    {
      if( key != null )
      {
        pool.removePoolListener( key );
      }
    }
  }

  private void fireCazeChanged( final IScenario scenario )
  {
    for( final IScenarioDataListener listener : m_controller )
    {
      listener.scenarioChanged( scenario );
    }
  }

  /**
   * Find all pool key-infos for our currently held data models.
   */
  private KeyInfo[] findKeyInfos( )
  {
    final Collection<KeyInfo> result = new ArrayList<>();
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();

    final ScenarioDataPoolListener[] keys;
    synchronized( this )
    {
      final Collection<ScenarioDataPoolListener> values = m_keyMap.values();
      keys = values.toArray( new ScenarioDataPoolListener[values.size()] );
    }

    for( final ScenarioDataPoolListener listener : keys )
    {
      final KeyInfo keyInfo = pool.getInfoForKey( listener.getKey() );
      if( keyInfo != null )
        result.add( keyInfo );
    }

    return result.toArray( new KeyInfo[result.size()] );
  }

  /**
   * Reloads all models.
   */
  @Override
  public void reloadModel( )
  {
    final KeyInfo[] infos = findKeyInfos();
    for( final KeyInfo info : infos )
      info.reload();
  }

  /**
   * Resets the dirty state of all data models. Only used internally, when the model should be released during platform
   * shutdown.
   */
  @Override
  public void resetDirty( )
  {
    final KeyInfo[] infos = findKeyInfos();
    for( final KeyInfo info : infos )
      info.setDirty( false );
  }

  /**
   * Resets the pool-key for the given folder.
   * 
   * @param szenarioFolder
   *          If <code>null</code>, just releases the existing key.
   */
  Pair<IModel, IStatus> loadModel( final IFolder szenarioFolder, final String id, final Class< ? extends IModel> wrapperClass, final String gmlLocation )
  {
    final IPoolableObjectType newKey = keyForLocation( szenarioFolder, gmlLocation );

    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();

    /* If nothing changed, return */
    final ScenarioDataPoolListener oldListener = m_keyMap.get( wrapperClass );
    final IPoolableObjectType oldKey = oldListener == null ? null : oldListener.getKey();
    // if( ObjectUtils.equals( oldKey, newKey ) )
    // {
    //
    // return null;
    // }

    if( oldKey != null )
      pool.removePoolListener( oldListener );

    final ScenarioDataPoolListener newListener = new ScenarioDataPoolListener( this, newKey, wrapperClass );
    m_keyMap.put( id, newListener );

    try
    {
      final CommandableWorkspace workspace = (CommandableWorkspace)pool.loadObject( newListener, newKey );
      final Feature rootFeature = workspace.getRootFeature();
      if( rootFeature instanceof IModel )
      {
        final IModel model = (IModel)rootFeature;
        return Pair.of( model, Status.OK_STATUS );
      }

      throw new IllegalArgumentException();
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  private IPoolableObjectType keyForLocation( final IFolder szenarioFolder, final String gmlLocation )
  {
    try
    {
      final URL szenarioURL = ResourceUtilities.createURL( szenarioFolder );
      return new PoolableObjectType( "gml", gmlLocation, szenarioURL ); //$NON-NLS-1$
    }
    catch( final MalformedURLException | URIException e )
    {
      // should never happen
      e.printStackTrace();
      return null;
    }
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <D extends IModel> D getModel( final String id ) throws CoreException
  {
    final CommandableWorkspace workspace = getCommandableWorkSpace( id );
    if( workspace == null )
      return null;

    final Feature rootFeature = workspace.getRootFeature();
    return (D)rootFeature;
  }

  @SuppressWarnings( "unchecked" )
  static <T> T adaptModel( final Class<T> modelClass, final GMLWorkspace workspace )
  {
    if( workspace == null )
      return null;

    final Feature rootFeature = workspace.getRootFeature();
    if( modelClass.isAssignableFrom( rootFeature.getClass() ) )
      return (T)rootFeature;

    return (T)rootFeature.getAdapter( modelClass );
  }

  @Override
  public void postCommand( final String id, final ICommand command ) throws InvocationTargetException
  {
    try
    {
      final CommandableWorkspace modelWorkspace = getCommandableWorkSpace( id );
      modelWorkspace.postCommand( command );
    }
    catch( final Exception e )
    {
      throw new InvocationTargetException( e );
    }
  }

  @Override
  public boolean isDirty( )
  {
    synchronized( this )
    {
      for( final String modelClass : m_keyMap.keySet() )
      {
        if( isDirty( modelClass ) )
          return true;
      }
    }
    return false;
  }

  @Override
  public boolean isDirty( final String id )
  {
    final ScenarioDataPoolListener keyPoolListener = getKeyPoolListener( id );

    if( keyPoolListener == null )
      return false;

    final IPoolableObjectType key = keyPoolListener.getKey();
    if( key == null )
      // TODO throw (core/other) exception?
      return false;

    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    final KeyInfo infoForKey = pool.getInfoForKey( key );
    if( infoForKey == null )
      // .
      return false;

    return infoForKey.isDirty();
  }

  @Override
  public synchronized void saveModel( final String id, final IProgressMonitor monitor ) throws CoreException
  {
    final SubMonitor progress = SubMonitor.convert( monitor, Messages.getString( "org.kalypso.afgui.scenarios.SzenarioDataProvider.14" ) + id + Messages.getString( "org.kalypso.afgui.scenarios.SzenarioDataProvider.15" ), 110 ); //$NON-NLS-1$ //$NON-NLS-2$

    final ScenarioDataPoolListener keyPoolListener = getKeyPoolListener( id );

    try
    {
      if( keyPoolListener == null )
        throw new IllegalArgumentException( Messages.getString( "org.kalypso.afgui.scenarios.SzenarioDataProvider.7" ) + id ); //$NON-NLS-1$

      final IPoolableObjectType key = keyPoolListener.getKey();
      if( key != null )
      {
        final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
        final KeyInfo infoForKey = pool.getInfoForKey( key );

        progress.worked( 10 );

        if( infoForKey != null && infoForKey.isDirty() )
        {
          infoForKey.saveObject( progress.newChild( 100 ) );
        }
        else
          progress.worked( 100 );
      }
    }
    catch( final LoaderException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
    finally
    {
      progress.done();
    }
  }

  @Override
  public synchronized void saveModel( final IProgressMonitor monitor ) throws CoreException
  {
    final SubMonitor progress = SubMonitor.convert( monitor, Messages.getString( "org.kalypso.afgui.scenarios.SzenarioDataProvider.16" ), m_keyMap.size() * 100 ); //$NON-NLS-1$
    try
    {
      for( final String id : m_keyMap.keySet() )
      {
        saveModel( id, progress.newChild( 100 ) );
      }
    }
    finally
    {
      progress.done();
    }
  }

  @Override
  public CommandableWorkspace getCommandableWorkSpace( final String id ) throws IllegalArgumentException, CoreException
  {
    final Map<String, IScenarioDatum> locationMap = ScenarioDataExtension.getScenarioDataMap( m_dataSetScope );

    if( locationMap == null || !locationMap.containsKey( id ) )
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.afgui.scenarios.SzenarioDataProvider.13" ) + id ); //$NON-NLS-1$

    final ScenarioDataPoolListener keyPoolListener = getKeyPoolListener( id );
    if( keyPoolListener == null )
      return null;

    final IPoolableObjectType key = keyPoolListener.getKey();
    if( key == null )
      return null;

    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    return (CommandableWorkspace)pool.getObject( key );
  }

  private synchronized ScenarioDataPoolListener getKeyPoolListener( final String id )
  {
    return m_keyMap.get( id );
  }

  /**
   * Returns the current scenario's base folder
   */
  @Override
  public IFolder getScenarioFolder( )
  {
    if( m_scenario != null )
      return m_scenario.getFolder();

    return null;
  }

  @Override
  public IScenario getScenario( )
  {
    return m_scenario;
  }

  /**
   * Checks if a model has already been loaded. Returns <code>true</code>, as soon as the data object has been loaded by
   * the pool, regardless of the success of that operation. throws {@link IllegalArgumentException} If the given data id
   * is not known.
   */
  private synchronized boolean isLoaded( final String id )
  {
    final ScenarioDataPoolListener keyPoolListener = getKeyPoolListener( id );
    if( keyPoolListener == null )
    {
      final String msg = String.format( "Unknown data id '%s'", id ); //$NON-NLS-1$
      throw new IllegalArgumentException( msg );
    }

    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    final KeyInfo info = pool.getInfoForKey( keyPoolListener.getKey() );
    if( info == null )
      return false;

    final IStatus result = info.getResult();
    return result != null;
  }

  private static final long WAIT_TIME = 100;

  /**
   * Waits until a model is loaded.
   */
  @Override
  public boolean waitForModelToLoad( final String id, final int maxWaitTimeInMillis ) throws InterruptedException
  {
    int waitTime = 0;
    while( true )
    {
      if( isLoaded( id ) )
        return true;

      Thread.sleep( WAIT_TIME );
      waitTime += WAIT_TIME;

      if( waitTime > maxWaitTimeInMillis )
        return false;
    }
  }

  @Override
  public String toString( )
  {
    return String.format( "Active data set scope: [ %s ]", m_dataSetScope ); //$NON-NLS-1$
  }

  void fireModelLoaded( final IModel model, final IStatus status )
  {
    if( !m_fireModelLoaded )
      return;

    // REMARK: copy current listeners into array to avoid ConcurrentModificationException
    final IScenarioDataListener[] listeners = m_controller.toArray( new IScenarioDataListener[m_controller.size()] );
    for( final IScenarioDataListener listener : listeners )
    {
      listener.modelLoaded( model, status );
    }
  }
}