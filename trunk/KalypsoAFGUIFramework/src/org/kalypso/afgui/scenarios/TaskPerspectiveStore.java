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
package org.kalypso.afgui.scenarios;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.WorkflowHelper;
import org.osgi.framework.Version;

import com.google.common.base.Charsets;

import de.renew.workflow.base.ITask;
import de.renew.workflow.base.IWorkflow;

/**
 * This class is responsible for loading / storing the perspective configuration for a task.
 *
 * @author Gernot Belger
 */
@SuppressWarnings("restriction")
class TaskPerspectiveStore
{
  private static final String USER_AREA_DIR = "taskPerspectives"; //$NON-NLS-1$

  /* Attribute name of root element that holds the version number added by Kalypso. */
  private static final String KEY_KALYPSO_VERISON = "kalypsoVersion"; //$NON-NLS-1$

  private final IWindowListener m_windowListener = new IWindowListener()
  {
    @Override
    public void windowOpened( final IWorkbenchWindow window )
    {
      handleWindowOpened( window );
    }

    @Override
    public void windowDeactivated( final IWorkbenchWindow window )
    {
    }

    @Override
    public void windowClosed( final IWorkbenchWindow window )
    {
      handleWindowClosed( window );
    }

    @Override
    public void windowActivated( final IWorkbenchWindow window )
    {
    }
  };

  private final IPerspectiveListener m_perspectiveListener = new PerspectiveAdapter()
  {
    @Override
    public void perspectiveChanged( final IWorkbenchPage page, final IPerspectiveDescriptor perspective, final String changeId )
    {
      if( IWorkbenchPage.CHANGE_RESET.equals( changeId ) )
        handlePerspectiveReset( perspective );
    }
  };

  private boolean m_resetInProgress = false;

  public TaskPerspectiveStore( )
  {
    final IWorkbench workbench = PlatformUI.getWorkbench();

    workbench.addWindowListener( m_windowListener );

    final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
    if( window != null )
      handleWindowOpened( window );
  }

  /**
   * Returns the location of the perspective file in the system location for a given task; i.e. the pre-defined
   * perspective configuration, if any.
   */
  private XMLMemento loadSystemDefinition( final ITask task )
  {
    if( task == null )
      return null;

    try
    {
      final IWorkflow workflow = task.getWorkflow();
      final URL resourceContext = workflow.getResourceContext();

      final String filename = getFilename( task );
      final URL systemLocation = new URL( resourceContext, filename );

      try (Reader reader = new InputStreamReader( systemLocation.openStream(), Charsets.UTF_8 ))
      {
        return XMLMemento.createReadRoot( reader );
      }
      catch( final IOException e )
      {
        // ignored, happens if file does not exist
        return null;
      }
      catch( final WorkbenchException e )
      {
        e.printStackTrace();
        return null;
      }
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  private XMLMemento loadUserDefinition( final ITask task )
  {
    final File userFile = getUserFile( task );
    if( !userFile.exists() )
      return null;

    try (Reader reader = new InputStreamReader( new FileInputStream( userFile ), Charsets.UTF_8 ))
    {
      return XMLMemento.createReadRoot( reader );
    }
    catch( IOException | WorkbenchException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Returns the location of the perspective file in the user location for a given task.
   */
  private File getUserFile( final ITask task )
  {
    final File userDir = getUserArea();
    final String filename = getFilename( task );
    final File taskPerspectiveFile = new File( userDir, filename );
    return taskPerspectiveFile;
  }

  private String getFilename( final ITask task )
  {
    final String uri = task.getURI();
    return uri + "_persp.xml";
  }

  private File getUserArea( )
  {
    final IPath stateLocation = KalypsoAFGUIFrameworkPlugin.getDefault().getStateLocation();
    final File stateDir = stateLocation.toFile();

    final File userAreaDir = new File( stateDir, USER_AREA_DIR );

    /* Create this directory */
    if( !userAreaDir.exists() )
      userAreaDir.mkdir();

    return userAreaDir;
  }

  private Perspective getCurrentPerspective( )
  {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
    final WorkbenchPage page = (WorkbenchPage) window.getActivePage();
    if( page == null )
      return null;

    return page.getActivePerspective();
  }

  /**
   * Stores the current perspective settings under the name of the given task into the user area.
   */
  void saveTaskPerspective( final ITask task )
  {
    /* Save current configuration to user file */
    final File taskPerspectiveFile = getUserFile( task );

    final Perspective perspective = getCurrentPerspective();
    if( perspective != null )
      savePerspective( perspective, taskPerspectiveFile );
  }

  private void savePerspective( final Perspective perspective, final File targetFile )
  {
    final XMLMemento memento = XMLMemento.createWriteRoot( "perspective" );//$NON-NLS-1$

    /* Persist current state */
    final IStatus status = perspective.saveState( memento );
    if( !status.isOK() )
    {
      KalypsoAFGUIFrameworkPlugin.getDefault().getLog().log( status );
      return;
    }

    /* add Kalypso specific version information */
    final IProduct product = Platform.getProduct();
    final Version kalypsoVersion = product.getDefiningBundle().getVersion();
    memento.putString( KEY_KALYPSO_VERISON, kalypsoVersion.toString() );

    try (final Writer writer = new OutputStreamWriter( new FileOutputStream( targetFile ), Charsets.UTF_8 ))
    {
      memento.save( writer );
    }
    catch( final IOException e )
    {
      e.printStackTrace();
    }
  }

  boolean restoreTaskPerspective( final ITask task )
  {
    final XMLMemento configuration = findDefinition( task );
    if( configuration == null )
      return false;

    try
    {
      final Perspective perspective = getCurrentPerspective();
      applyPerspectiveConfiguration( perspective, configuration );
      return true;
    }
    catch( final IOException e )
    {
      // will probably never happen, happens only if bundle store could not be written
      e.printStackTrace();
      return false;
    }
  }

  private XMLMemento findDefinition( final ITask task )
  {
    final XMLMemento systemDefinition = loadSystemDefinition( task );

    final XMLMemento userDefinition = loadUserDefinition( task );

    /* Fall back to system definition if no user definition exists yet */
    if( userDefinition == null )
      return systemDefinition;

    if( systemDefinition == null )
      return null;

    /* Both, user and system exist; chose the one which is more current */
    final Version systemVersion = getVersion( systemDefinition );
    final Version userVersion = getVersion( userDefinition );

    // TODO: check: what to do in the null case?
    /* Only use userDefinition if it is not outdated regarding its version number */
    if( systemVersion != null && systemVersion.compareTo( userVersion ) <= 0 )
      return userDefinition;

    return systemDefinition;
  }

  private Version getVersion( final XMLMemento definition )
  {
    final String versionText = definition.getString( KEY_KALYPSO_VERISON );
    if( StringUtils.isBlank( versionText ) )
      return null;

    try
    {
      return new Version( versionText );
    }
    catch( final IllegalArgumentException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  private void applyPerspectiveConfiguration( final Perspective perspective, final XMLMemento memento ) throws IOException
  {
    final PerspectiveRegistry registry = getPerspectiveRegistry();
    final PerspectiveDescriptor desc = (PerspectiveDescriptor) perspective.getDesc();

    System.out.println( "Apply custom perspective: " + desc.getId() );

    registry.saveCustomPersp( desc, memento );

    final IWorkbench workbench = PlatformUI.getWorkbench();
    final WorkbenchWindow window = (WorkbenchWindow) workbench.getActiveWorkbenchWindow();
    final WorkbenchPage page = (WorkbenchPage) window.getActivePage();

    m_resetInProgress = true;
    try
    {
      page.resetPerspective();
    }
    finally
    {
      m_resetInProgress = false;
    }
  }

  private PerspectiveRegistry getPerspectiveRegistry( )
  {
    return (PerspectiveRegistry) WorkbenchPlugin.getDefault().getPerspectiveRegistry();
  }

  protected void handleWindowOpened( final IWorkbenchWindow window )
  {
    window.addPerspectiveListener( m_perspectiveListener );
  }

  protected void handleWindowClosed( final IWorkbenchWindow window )
  {
    window.removePerspectiveListener( m_perspectiveListener );
  }

  protected void handlePerspectiveReset( final IPerspectiveDescriptor perspective )
  {
    if( m_resetInProgress )
      return;

    clearUserEntries();
    resetSystemSettings( (PerspectiveDescriptor) perspective );
  }

  private void clearUserEntries( )
  {
    final File userDir = getUserArea();
    FileUtils.deleteQuietly( userDir );
  }

  private void resetSystemSettings( final PerspectiveDescriptor desc )
  {
    final ITask activeTask = WorkflowHelper.getActiveTask();
    final PerspectiveRegistry registry = getPerspectiveRegistry();

    final XMLMemento systemDefinition = loadSystemDefinition( activeTask );
    if( systemDefinition == null )
    {
      /* clear custom definition, so perspective is really reset from perspective factory */
      desc.revertToPredefined();
    }
    else
    {
      try
      {
        /* Reset to system definition */
        registry.saveCustomPersp( desc, systemDefinition );
      }
      catch( final IOException e )
      {
        e.printStackTrace();
      }
    }
  }
}