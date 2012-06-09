/**
 *
 */
package org.kalypso.afgui.internal;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.model.ICommandPoster;
import org.kalypso.afgui.scenarios.IScenarioDatum;
import org.kalypso.afgui.scenarios.ScenarioDataExtension;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.IPoolListener;
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
  private static final class KeyPoolListener implements IPoolListener
  {
    private final IPoolableObjectType m_key;

    private final Set<IScenarioDataListener> m_controller;

    private final Class< ? extends IModel> m_modelClass;

    public KeyPoolListener( final IPoolableObjectType key, final Set<IScenarioDataListener> controller, final Class< ? extends IModel> modelClass )
    {
      m_key = key;
      m_controller = controller;
      m_modelClass = modelClass;
    }

    public IPoolableObjectType getKey( )
    {
      return m_key;
    }

    @Override
    public void dirtyChanged( final IPoolableObjectType key, final boolean isDirty )
    {
    }

    @Override
    public boolean isDisposed( )
    {
      return false;
    }

    @Override
    public void objectInvalid( final IPoolableObjectType key, final Object oldValue )
    {
      System.out.println( Messages.getString( "org.kalypso.afgui.scenarios.SzenarioDataProvider.2" ) + key ); //$NON-NLS-1$
    }

    @Override
    public void objectLoaded( final IPoolableObjectType key, final Object newValue, final IStatus status )
    {
      if( newValue instanceof GMLWorkspace )
      {
        final GMLWorkspace workspace = (GMLWorkspace) newValue;

        // Adapting directly to IModel is dangerous because the mapping is not unique
        // (for example, 1d2d adapter factory as well as risk adapter factory are registered to adapt Feature to IModel)
        // TODO remove mappings to IModel from the factories

        final IModel model = adaptModel( m_modelClass, workspace );
        if( model != null )
        {
          fireModelLoaded( model, status );
        }

        // notify user once about messages during loading
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if( workbench != null && !workbench.isClosing() && !status.isOK() )
        {
          final Display display = workbench.getDisplay();
          display.asyncExec( new Runnable()
          {

            @Override
            public void run( )
            {
              final Shell activeShell = display.getActiveShell();
              ErrorDialog.openError( activeShell, Messages.getString( "org.kalypso.afgui.scenarios.SzenarioDataProvider.0" ), Messages.getString( "org.kalypso.afgui.scenarios.SzenarioDataProvider.1" ), status ); //$NON-NLS-1$ //$NON-NLS-2$
            }
          } );
        }
      }
    }

    private void fireModelLoaded( final IModel model, final IStatus status )
    {
      // REMARK: copy current listeners into array to avoid ConcurrentModificationException
      final IScenarioDataListener[] listeners = m_controller.toArray( new IScenarioDataListener[m_controller.size()] );
      for( final IScenarioDataListener listener : listeners )
      {
        listener.modelLoaded( model, status );
      }
    }
  }

  /**
   * Maps the (adapted) feature-wrapper-classes onto the corresponding pool key.
   * <p>
   * At the moment this works, because each gml-file corresponds to exactly one (different) wrapper class.
   */
  final Map<String, KeyPoolListener> m_keyMap = new HashMap<String, KeyPoolListener>();

  private final Set<IScenarioDataListener> m_controller = new LinkedHashSet<IScenarioDataListener>();

  private IScenario m_scenario = null;

  private String m_dataSetScope;

  /**
   * Returns the current data set scope
   */
  public String getDataSetScope( )
  {
    return m_dataSetScope;
  }

  @Override
  public synchronized void addScenarioDataListener( final IScenarioDataListener listener )
  {
    m_controller.add( listener );
  }

  @Override
  public synchronized void removeScenarioDataListener( final IScenarioDataListener listener )
  {
    m_controller.remove( listener );
  }

  @Override
  public void setCurrent( final IScenario scenario )
  {
    /* Nothing to do if scenario folder stays the same */
    if( ObjectUtils.equals( m_scenario, scenario ) )
      return;

    /* Release current models && reset state */
    reset();

    // FIXME: this synchronized block is dubious! Probably, reset() should be synchronized as well...
    synchronized( this )
    {
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
    }

    fireCazeChanged( m_scenario );
    setDataScope( m_scenario );
  }

  private void setDataScope( final IScenario scenario )
  {
    if( scenario == null || m_dataSetScope == null )
      return;

    final String dataSetScope = m_dataSetScope;

    final Job job = new Job( Messages.getString( "org.kalypso.afgui.scenarios.SzenarioDataProvider.4" ) ) //$NON-NLS-1$
    {
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        final List<IStatus> statusList = new ArrayList<IStatus>();
        final Map<String, IScenarioDatum> locationMap = ScenarioDataExtension.getScenarioDataMap( dataSetScope );
        if( locationMap != null )
        {
          synchronized( m_keyMap )
          {
            for( final IScenarioDatum entry : locationMap.values() )
            {
              try
              {
                final String id = entry.getID();
                final Class< ? extends IModel> wrapperClass = entry.getModelClass();
                final String gmlLocation = entry.getModelPath();

                /* @hack resolve "gloabal" gml file from parent scenario. */
                final IFolder dataFolder = resolveFolder( scenario, gmlLocation );
                if( dataFolder == null )
                {
                  resetKeyForProject( scenario.getFolder(), id, wrapperClass, gmlLocation );
                }
                else
                {
                  resetKeyForProject( dataFolder, id, wrapperClass, gmlLocation );
                }
              }
              catch( final CoreException e )
              {
                statusList.add( e.getStatus() );
              }
            }
          }
        }

        return StatusUtilities.createStatus( statusList, Messages.getString( "org.kalypso.afgui.scenarios.SzenarioDataProvider.5" ) ); //$NON-NLS-1$
      }

      private IFolder resolveFolder( final IScenario scene, final String gmlLocation ) throws CoreException
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
    };

    job.setRule( scenario.getProject() );
    job.schedule();
  }

  /**
   * Releases all models and clears all cached data.
   */
  private void reset( )
  {
    m_scenario = null;
    m_dataSetScope = null;

    KeyPoolListener[] keys;
    synchronized( m_keyMap )
    {
      final Collection<KeyPoolListener> values = m_keyMap.values();
      keys = values.toArray( new KeyPoolListener[values.size()] );
      m_keyMap.clear();
    }

    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    for( final KeyPoolListener key : keys )
    {
      if( key != null )
      {
        pool.removePoolListener( key );
      }
    }
  }

  private synchronized void fireCazeChanged( final IScenario scenario )
  {
    for( final IScenarioDataListener listener : m_controller )
    {
      listener.scenarioChanged( scenario );
    }
  }

  /**
   * Reloads all models.
   *
   * @see de.renew.workflow.connector.cases.ICaseDataProvider#reloadModel()
   */
  @Override
  public void reloadModel( )
  {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();

    final KeyPoolListener[] keys;
    synchronized( m_keyMap )
    {
      final Collection<KeyPoolListener> values = m_keyMap.values();
      keys = values.toArray( new KeyPoolListener[values.size()] );
    }

    for( final KeyPoolListener listener : keys )
    {
      final KeyInfo keyInfo = pool.getInfoForKey( listener.getKey() );
      keyInfo.reload();
    }
  }

  /**
   * Resets the pool-key for the given folder.
   *
   * @param szenarioFolder
   *          If <code>null</code>, just releases the existing key.
   */
  /* protected */void resetKeyForProject( final IFolder szenarioFolder, final String id, final Class< ? extends IModel> wrapperClass, final String gmlLocation )
  {
    final IPoolableObjectType newKey = keyForLocation( szenarioFolder, gmlLocation );

    /* If nothing changed, return */
    final KeyPoolListener oldListener = m_keyMap.get( wrapperClass );
    final IPoolableObjectType oldKey = oldListener == null ? null : oldListener.getKey();
    if( ObjectUtils.equals( oldKey, newKey ) )
      return;

    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();

    if( oldKey != null )
    {
      pool.removePoolListener( oldListener );
    }

    if( newKey == null )
    {
      m_keyMap.put( id, null );
    }
    else
    {
      final KeyPoolListener newListener = new KeyPoolListener( newKey, m_controller, wrapperClass );
      m_keyMap.put( id, newListener );
      pool.addPoolListener( newListener, newKey );
    }
  }

  private IPoolableObjectType keyForLocation( final IFolder szenarioFolder, final String gmlLocation )
  {
    try
    {
      final URL szenarioURL = ResourceUtilities.createURL( szenarioFolder );
      return new PoolableObjectType( "gml", gmlLocation, szenarioURL ); //$NON-NLS-1$
    }
    catch( final MalformedURLException e )
    {
      // should never happen
      e.printStackTrace();
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <D extends IModel> D getModel( final String id ) throws CoreException
  {
    final CommandableWorkspace workspace = getCommandableWorkSpace( id );
    if( workspace == null )
      return null;

    final Feature rootFeature = workspace.getRootFeature();
    return (D) rootFeature;
  }

  @SuppressWarnings("unchecked")
  static <T> T adaptModel( final Class<T> modelClass, final GMLWorkspace workspace )
  {
    if( workspace == null )
      return null;

    final Feature rootFeature = workspace.getRootFeature();
    if( modelClass.isAssignableFrom( rootFeature.getClass() ) )
      return (T) rootFeature;

    return (T) rootFeature.getAdapter( modelClass );
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
    synchronized( m_keyMap )
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
    final KeyPoolListener keyPoolListener = getKeyPoolListener( id );

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

    final KeyPoolListener keyPoolListener = getKeyPoolListener( id );

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
        if( infoForKey.isDirty() )
        {
          infoForKey.saveObject( progress.newChild( 100 ) );
        }
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

    final KeyPoolListener keyPoolListener = getKeyPoolListener( id );
    if( keyPoolListener == null )
      return null;

    final IPoolableObjectType key = keyPoolListener.getKey();
    if( key == null )
      return null;

    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    return (CommandableWorkspace) pool.getObject( key );
  }

  private KeyPoolListener getKeyPoolListener( final String id )
  {
    synchronized( m_keyMap )
    {
      return m_keyMap.get( id );
    }
  }

  /**
   * Returns the current scenario's base folder
   */
  @Override
  public IContainer getScenarioFolder( )
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
  public synchronized boolean isLoaded( final String id )
  {
    final KeyPoolListener keyPoolListener = getKeyPoolListener( id );
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
    return "Active data set scope: [ " + m_dataSetScope + " ]";
  }
}