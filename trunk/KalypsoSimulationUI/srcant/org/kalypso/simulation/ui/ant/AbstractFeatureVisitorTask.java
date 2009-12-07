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

package org.kalypso.simulation.ui.ant;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.IErrorHandler;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.swt.widgets.GetShellFromDisplay;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.contribs.java.util.logging.ILogger;
import org.kalypso.contribs.java.util.logging.LoggerUtilities;
import org.kalypso.contribs.java.util.logging.SystemOutLogger;
import org.kalypso.ogc.gml.serialize.GmlSerializeException;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.visitors.CountFeatureVisitor;
import org.kalypsodeegree_impl.model.feature.visitors.MonitorFeatureVisitor;

/**
 * Abstract task for task which starts a visitor on some features.<br/>
 * TODO: give argument of accept-depth (default is DEPTH_INFINITE)<br/>
 * The featurePath argument supports multiple pathes separated by ';'.<br/>
 * 
 * @author Gernot Belger
 */
public abstract class AbstractFeatureVisitorTask extends Task implements ICoreRunnableWithProgress, IErrorHandler
{
  /**
   * Separator between feature-pathes ; can be used to give this Task multiple feature-pathes. The task will then
   * iterate over every feature in every given feature-path
   */
  public static final String FEATURE_PATH_SEPARATOR = ";";

  /** Href to GML */
  private String m_gml;

  /**
   * Depth for visiting features.
   * <p>
   * Supported values are:
   * <ul>
   * <li>zero</li>
   * <li>infinite</li> x
   * <li>infinite_links</li>
   * </ul>
   * </p>
   * 
   * @see FeatureVisitor
   */
  private String m_depth = "infinite";

  /**
   * Feature-Path innerhalb des GMLs. Alle durch diesen Pfad denotierten Features werden behandelt.
   */
  private String[] m_featurePath;

  /** Kontext (=URL), gegen welche die Links innerhalb des GML aufgelöst werden. */
  private URL m_context;

  /** if true, the task is executed whithin a progress dialog */
  private boolean m_runAsync;

  private boolean m_doSaveGml = false;

  /** if true, illegal feature pathes are ignored */
  private boolean m_ignoreIllegalFeaturePath = false;

  private ILogger m_logger;

  /**
   * @param doSaveGml
   *          If true, the read gml will be safed at the end of the process.
   */
  public AbstractFeatureVisitorTask( final boolean doSaveGml )
  {
    m_doSaveGml = doSaveGml;
  }

  public void setIgnoreIllegalFeaturePath( final boolean ignoreIllegalFeaturePath )
  {
    m_ignoreIllegalFeaturePath = ignoreIllegalFeaturePath;
  }

  public void setRunAsync( final boolean runAsync )
  {
    m_runAsync = runAsync;
  }

  public final void setContext( final URL context )
  {
    m_context = context;
  }

  public final void setFeaturePath( final String featurePath )
  {
    m_featurePath = featurePath.split( FEATURE_PATH_SEPARATOR );
  }

  public final void setGml( final String gml )
  {
    m_gml = gml;
  }

  public void setDepth( final String depth )
  {
    m_depth = depth;
  }

  private synchronized ILogger getLogger( )
  {
    if( m_logger == null )
      m_logger = createLogger();
    return m_logger;
  }

  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  @Override
  public final void execute( ) throws BuildException
  {
    try
    {
      if( m_runAsync )
        executeInDialog();
      else
      {
        final IProgressMonitor monitor = getProgressMonitor();
        final IStatus status = execute( monitor );
        if( status.isOK() )
          return;

        final String message = StatusUtilities.messageFromStatus( status );
        throw new BuildException( message, status.getException() );
      }
    }
    catch( final BuildException be )
    {
      throw be;
    }
    catch( final Throwable e )
    {
      // Catch even throwables, else we never get an stack trace for errors
      e.printStackTrace();

      throw new BuildException( e.getLocalizedMessage(), e );
    }
  }

  protected abstract FeatureVisitor createVisitor( final URL context, final IUrlResolver resolver, final ILogger logger, final IProgressMonitor monitor ) throws CoreException, InvocationTargetException, InterruptedException;

  protected abstract void validateInput( );

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  public IStatus execute( final IProgressMonitor monitor ) throws InterruptedException
  {
    try
    {
      final String taskDescription = getDescription();
      monitor.beginTask( taskDescription, m_featurePath.length );

      final String taskDesk = getDescription();
      if( taskDesk != null )
        getLogger().log( Level.INFO, LoggerUtilities.CODE_NEW_MSGBOX, taskDesk );

      monitor.subTask( " - Input wird validiert" );
      validateInput();

      monitor.subTask( " - Lese GML" );
      final URL gmlURL = UrlResolverSingleton.getDefault().resolveURL( m_context, m_gml );
      final GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( gmlURL, null );

      final List<IStatus> stati = new ArrayList<IStatus>();
      for( final String featurePath : m_featurePath )
      {
        if( monitor.isCanceled() )
          throw new InterruptedException();

        try
        {   if( m_featurePath.length > 1)
            monitor.subTask( String.format( " - Bearbeite %s", featurePath ) );
        else
          monitor.subTask( " - Zeitreihenabruf" );
          final IStatus result = visitPath( workspace, featurePath, new SubProgressMonitor( monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK ) );
          if( !result.isOK() )
            stati.add( result );
        }
        catch( final IllegalArgumentException e )
        {
          final IStatus status = StatusUtilities.statusFromThrowable( e );
          if( m_ignoreIllegalFeaturePath )
          {
            getLogger().log( Level.WARNING, -1, "Feature wird ignoriert (" + status.getMessage() + ")" );
          }
          else
          {
            getLogger().log( Level.WARNING, -1, status.getMessage() );
            stati.add( status );
          }
        }
        catch( final Throwable t )
        {
          final IStatus status = StatusUtilities.statusFromThrowable( t );
          getLogger().log( Level.SEVERE, -1, status.getMessage() );
          stati.add( status );
        }
      }

      saveGML( gmlURL, workspace );

      return new MultiStatus( KalypsoGisPlugin.getId(), 0, stati.toArray( new IStatus[stati.size()] ), "", null );
    }
    catch( final Exception e )
    {
      if( e instanceof InterruptedException )
        throw (InterruptedException) e;

      return StatusUtilities.statusFromThrowable( e );
    }
    finally
    {
      monitor.done();
    }
  }

  private void saveGML( final URL gmlURL, final GMLWorkspace workspace ) throws IOException, GmlSerializeException
  {
    if( !m_doSaveGml )
      return;

    OutputStreamWriter writer = null;
    try
    {
      writer = UrlResolverSingleton.getDefault().createWriter( gmlURL );
      GmlSerializer.serializeWorkspace( writer, workspace );
      writer.close();
    }
    finally
    {
      IOUtils.closeQuietly( writer );
    }
  }

  private IStatus visitPath( final GMLWorkspace workspace, final String featurePath, final IProgressMonitor monitor ) throws CoreException, InvocationTargetException, InterruptedException
  {
    // count features
    final int count = countFeatures( workspace, featurePath );

    monitor.beginTask( featurePath, count );

    FeatureVisitor visitor;
    try
    {
      final IUrlResolver resolver = UrlResolverSingleton.getDefault();
      final ILogger logger = getLogger();
      visitor = createVisitor( m_context, resolver, logger, new SubProgressMonitor( monitor, 1 ) );
      final MonitorFeatureVisitor wrappedVisitor = new MonitorFeatureVisitor( monitor, visitor );
      workspace.accept( wrappedVisitor, featurePath, getDepth() );
    }
    catch( final OperationCanceledException e )
    {
      throw new InterruptedException();
    }
    finally
    {
      monitor.done();
    }

    return statusFromVisitor( visitor );
  }

  private int countFeatures( final GMLWorkspace workspace, final String featurePath )
  {
    final CountFeatureVisitor countFeatureVisitor = new CountFeatureVisitor();
    workspace.accept( countFeatureVisitor, featurePath, getDepth() );
    return 0;
  }

  private int getDepth( )
  {
    if( m_depth.compareToIgnoreCase( "infinite" ) == 0 )
      return FeatureVisitor.DEPTH_INFINITE;

    if( m_depth.compareToIgnoreCase( "infinite_links" ) == 0 )
      return FeatureVisitor.DEPTH_INFINITE;

    if( m_depth.compareToIgnoreCase( "zero" ) == 0 )
      return FeatureVisitor.DEPTH_ZERO;

    throw new BuildException( "Unsupported value of 'depth': " + m_depth );
  }

  /**
   * REMARK: It is NOT possible to put this inner class into an own .class file (at least not inside the plugin code)
   * else we get an LinkageError when accessing the Project class.
   */
  private ILogger createLogger( )
  {
    final Project antProject = getProject();

    if( antProject == null )
    {
      // final String outString = LoggerUtilities.formatLogStylish( level, msgCode, message );
      return new SystemOutLogger();
    }

    return createProjectLogger( antProject );
  }

  private ILogger createProjectLogger( final Project antProject )
  {
    return new ILogger()
    {
      /**
       * @see org.kalypso.contribs.java.util.logging.ILogger#log(java.util.logging.Level, int, java.lang.String)
       */
      public void log( final Level level, final int msgCode, final String message )
      {
        final String outString = LoggerUtilities.formatLogStylish( level, msgCode, message );
        antProject.log( outString );
      }
    };
  }

  /**
   * Returns a status for the used visitor. Default implementation returns OK-status.<br>
   * Should be overwritten by implementors.<br>
   * 
   * @param visitor
   *          The visitor previously created by {@link #createVisitor(URL, IUrlResolver, ILogger, IProgressMonitor)}.
   */
  protected IStatus statusFromVisitor( final FeatureVisitor visitor )
  {
    if( visitor != null )
    {
      // only, to avoid yellow thingies
    }

    return Status.OK_STATUS;
  }

  private void executeInDialog( )
  {
    final Display display = PlatformUI.getWorkbench().getDisplay();
    final Shell shell = new GetShellFromDisplay( display ).getShell();

    RunnableContextHelper.executeInProgressDialog( shell, this, this );
  }

  /**
   * Cannot be moved into helper class, else we get a linkage error.
   */
  protected IProgressMonitor getProgressMonitor( )
  {
    final Project antProject = getProject();
    if( antProject == null )
      return new NullProgressMonitor();

    final Hashtable< ? , ? > references = antProject.getReferences();
    if( references == null )
      return new NullProgressMonitor();

    final IProgressMonitor monitor = (IProgressMonitor) references.get( AntCorePlugin.ECLIPSE_PROGRESS_MONITOR );
    if( monitor == null )
      return new NullProgressMonitor();

    return monitor;
  }
}