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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.logging.Level;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.IErrorHandler;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.swt.widgets.GetShellFromDisplay;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.contribs.java.util.logging.ILogger;
import org.kalypso.contribs.java.util.logging.LoggerUtilities;
import org.kalypso.contribs.java.util.logging.SystemOutLogger;
import org.kalypso.simulation.core.ant.FeatureVisitorOperation;
import org.kalypso.simulation.core.ant.IFeatureVisitorTask;
import org.kalypsodeegree.model.feature.FeatureVisitor;

/**
 * Abstract task for task which starts a visitor on some features.<br/>
 * TODO: give argument of accept-depth (default is DEPTH_INFINITE)<br/>
 * The featurePath argument supports multiple pathes separated by ';'.<br/>
 * 
 * @author Gernot Belger
 */
public abstract class AbstractFeatureVisitorTask extends Task implements IErrorHandler, IFeatureVisitorTask
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
  private String[] m_featurePathes;

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

  public final void setIgnoreIllegalFeaturePath( final boolean ignoreIllegalFeaturePath )
  {
    m_ignoreIllegalFeaturePath = ignoreIllegalFeaturePath;
  }

  public final void setRunAsync( final boolean runAsync )
  {
    m_runAsync = runAsync;
  }

  public final void setContext( final URL context )
  {
    m_context = context;
  }

  public final void setFeaturePath( final String featurePath )
  {
    m_featurePathes = featurePath.split( FEATURE_PATH_SEPARATOR );
  }

  public final void setGml( final String gml )
  {
    m_gml = gml;
  }

  public final void setDepth( final String depth )
  {
    m_depth = depth;
  }

  public synchronized ILogger getLogger( )
  {
    if( m_logger == null )
      m_logger = createLogger();
    return m_logger;
  }

  /**
   * @see org.kalypso.ant.AbstractFeatureVisitorTask#validateInput()
   */
  @Override
  public void validateInput( )
  {
  }

  /**
   * @see org.kalypso.simulation.core.ant.IFeatureVisitorTask#getContext()
   */
  @Override
  public final URL getContext( )
  {
    return m_context;
  }

  /**
   * @see org.kalypso.simulation.core.ant.IFeatureVisitorTask#getGmlLocation()
   */
  @Override
  public final URL getGmlLocation( ) throws MalformedURLException
  {
    return UrlResolverSingleton.getDefault().resolveURL( m_context, m_gml );
  }

  /**
   * @see org.kalypso.simulation.core.ant.IFeatureVisitorTask#getFeaturePathes()
   */
  @Override
  public final String[] getFeaturePathes( )
  {
    return m_featurePathes;
  }

  /**
   * @see org.kalypso.simulation.core.ant.IFeatureVisitorTask#doSaveGml()
   */
  @Override
  public boolean doSaveGml( )
  {
    return m_doSaveGml;
  }

  /**
   * @see org.kalypso.simulation.core.ant.IFeatureVisitorTask#doIgnoreIllegalFeaturePath()
   */
  @Override
  public boolean doIgnoreIllegalFeaturePath( )
  {
    return m_ignoreIllegalFeaturePath;
  }

  /**
   * @see org.kalypso.simulation.core.ant.IFeatureVisitorTask#getDepth()
   */
  @Override
  public final int getDepth( )
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
   * @see org.apache.tools.ant.Task#execute()
   */
  @Override
  public final void execute( ) throws BuildException
  {
    try
    {
      final String taskDescription = getDescription();
      if( taskDescription != null )
        getLogger().log( Level.INFO, LoggerUtilities.CODE_NEW_MSGBOX, taskDescription );

      final FeatureVisitorOperation operation = new FeatureVisitorOperation( this );

      if( m_runAsync )
        executeInDialog( operation );
      else
        executeSynchron( operation );
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

  public String getVisitorTaskDescription( )
  {
    final String taskDescription = getDescription();
    if( taskDescription == null )
      return getClass().getSimpleName();

    return taskDescription;
  }

  private void executeSynchron( final FeatureVisitorOperation operation ) throws InterruptedException
  {
    final IProgressMonitor monitor = getProgressMonitor();
    // IMPORTANT: we put the monitor into a SubPRogressMonitor but do not call beginTask
    // This is important, as the ant-monitor is already started and calling beginTask again will deactivate the monitor.
    final IStatus status = operation.execute( new SubProgressMonitor( monitor, 1 ) );
    if( status.isOK() )
      return;

    final String message = StatusUtilities.messageFromStatus( status );
    throw new BuildException( message, status.getException() );
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
  public IStatus statusFromVisitor( final FeatureVisitor visitor )
  {
    return Status.OK_STATUS;
  }

  private void executeInDialog( final FeatureVisitorOperation operation )
  {
    final Display display = PlatformUI.getWorkbench().getDisplay();
    final Shell shell = new GetShellFromDisplay( display ).getShell();

    RunnableContextHelper.executeInProgressDialog( shell, operation, this );
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