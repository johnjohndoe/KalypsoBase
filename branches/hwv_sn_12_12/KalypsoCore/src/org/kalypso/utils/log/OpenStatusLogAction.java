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
package org.kalypso.utils.log;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.core.KalypsoCoreImages;
import org.kalypso.core.status.StatusComposite;
import org.kalypso.core.status.StatusDialog;

/**
 * This action opens a status log.
 * 
 * @author Holger Albert
 */
public class OpenStatusLogAction extends Action implements IUpdateable
{
  private final IResourceChangeListener m_resourceChangeListener = new IResourceChangeListener()
  {
    @Override
    public void resourceChanged( final IResourceChangeEvent event )
    {
      handleResourceChanged( event );
    }
  };

  protected final IFile m_statusLogFile;

  private final Collection<IAction> m_actions;

  private final String m_dialogTitle;

  protected final LoadStatusLogJob m_loadJob;

  /**
   * @param text
   *          The text.
   * @param tooltipText
   *          The tooltip text.
   * @param statusLogFile
   *          The status log file.
   * @param statusLabel
   *          The status label.
   */
  public OpenStatusLogAction( final String text, final String tooltipText, final IFile statusLogFile, final String statusLabel )
  {
    super( text, KalypsoCoreImages.id( KalypsoCoreImages.DESCRIPTORS.OPEN_STATUS_LOG_ACTION ) );

    setToolTipText( tooltipText );

    m_statusLogFile = statusLogFile;
    m_actions = new LinkedList<IAction>();
    m_dialogTitle = text;
    m_loadJob = new LoadStatusLogJob( statusLogFile, statusLabel );
    m_loadJob.addJobChangeListener( new JobChangeAdapter()
    {
      @Override
      public void done( final IJobChangeEvent event )
      {
        handleStatusLoaded();
      }
    } );

    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.addResourceChangeListener( m_resourceChangeListener );
  }

  @Override
  public void run( )
  {
    openLogDialog( true, IStatus.INFO | IStatus.WARNING | IStatus.ERROR | IStatus.CANCEL );
  }

  /**
   * @param statusMask
   *          The mask that the staus needs to match. Else the dialog is not opened.
   */
  public void openLogDialog( final boolean showOKstatus, final int statusMask )
  {
    try
    {
      m_loadJob.join();

      final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
      final IStatus statusLog = m_loadJob.getStatusLog();

      if( statusLog.isOK() )
      {
        if( !showOKstatus )
          return;
      }
      else if( !statusLog.matches( statusMask ) )
        return;

      final StatusDialog statusDialog = new StatusDialog( shell, statusLog, m_dialogTitle );
      statusDialog.setShowAsTree( true );

      for( final IAction action : m_actions )
        statusDialog.addAction( action );

      statusDialog.open();
    }
    catch( final InterruptedException ex )
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void update( )
  {
    final IFile statusLogFile = m_loadJob.getStatusLogFile();

    if( statusLogFile != null && statusLogFile.exists() )
      setEnabled( true );
    else
      setEnabled( false );

    for( final IAction action : m_actions )
    {
      if( action instanceof IUpdateable )
      {
        final IUpdateable updateableAction = (IUpdateable) action;
        updateableAction.update();
      }
    }
  }

  public void dispose( )
  {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.removeResourceChangeListener( m_resourceChangeListener );
  }

  public void addAction( final IAction action )
  {
    m_actions.add( action );
  }

  public void updateStatus( )
  {
    m_loadJob.schedule();
  }

  protected void handleStatusLoaded( )
  {
    final Display display = PlatformUI.getWorkbench().getDisplay();
    display.asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        final IStatus statusLog = m_loadJob.getStatusLog();
        final ImageDescriptor imageDescriptor = StatusComposite.getStatusImageDescriptor( statusLog.getSeverity() );
        setImageDescriptor( imageDescriptor );
      }
    } );
  }

  protected void handleResourceChanged( final IResourceChangeEvent event )
  {
    if( m_statusLogFile == null )
      return;

    final Display display = PlatformUI.getWorkbench().getDisplay();
    display.asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        final IResourceDelta rootDelta = event.getDelta();
        final IResourceDelta fileDelta = rootDelta.findMember( m_statusLogFile.getFullPath() );
        if( fileDelta != null )
        {
          update();
          updateStatus();
        }
      }
    } );
  }
}