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
package org.kalypso.project.database.client.core.base.actions;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.extension.IKalypsoModule;
import org.kalypso.project.database.client.extension.database.handlers.ILocalProject;
import org.kalypso.project.database.client.extension.database.handlers.ITranscendenceProject;
import org.kalypso.project.database.client.extension.project.IKalypsoModuleProjectOpenAction;
import org.kalypso.project.database.common.nature.IRemoteProjectPreferences;

/**
 * @author Dirk Kuch
 */
public class ProjectOpenAction implements IProjectAction
{
  protected static final Image IMG_PROJECT_LOCAL = new Image( null, ProjectOpenAction.class.getResourceAsStream( "images/project_local.gif" ) );

  protected static final Image IMG_PROJECT_TRANSCENDENCE = new Image( null, ProjectOpenAction.class.getResourceAsStream( "images/project_transcendence.gif" ) );

  protected static final Image iMG_PROJECT_TRANSCENDENCE_OFFLINE = new Image( null, ProjectOpenAction.class.getResourceAsStream( "images/project_transcendence_offline.gif" ) );

  protected static final Image IMG_PROJECT_TRANSCENDENCE_REMOTE_LOCK = new Image( null, ProjectOpenAction.class.getResourceAsStream( "images/project_remote_locked.gif" ) );

  protected static final Image IMG_PROJECT_TRANSCENDENCE_LOCAL_LOCK = new Image( null, ProjectOpenAction.class.getResourceAsStream( "images/project_transcendence_local_lock.gif" ) );

  private OPEN_TYPE m_type = null;

  protected final ILocalProject m_handler;

  protected final IKalypsoModule m_module;

  public enum OPEN_TYPE
  {
    eLocal,
    eLocalOffline,
    eTranscendenceReadable,
    eTranscendenceReadableServerLocked,
    eTranscendenceWriteable;

    Image getImage( )
    {
      final OPEN_TYPE type = valueOf( name() );
      if( eLocal.equals( type ) )
        return IMG_PROJECT_LOCAL;
      else if( eLocalOffline.equals( type ) )
        return iMG_PROJECT_TRANSCENDENCE_OFFLINE;
      else if( eTranscendenceReadable.equals( type ) )
        return IMG_PROJECT_TRANSCENDENCE;
      else if( eTranscendenceReadableServerLocked.equals( type ) )
        return IMG_PROJECT_TRANSCENDENCE_REMOTE_LOCK;
      else if( eTranscendenceWriteable.equals( type ) )
        return IMG_PROJECT_TRANSCENDENCE_LOCAL_LOCK;

      throw new NotImplementedException();
    }

    public String getStatus( )
    {
      final OPEN_TYPE type = valueOf( name() );
      if( eLocal.equals( type ) )
        return "Lokales Projekt";
      else if( eLocalOffline.equals( type ) )
        return "Lokales Datenbankprojekt - Achtung: Modelldatenbankserver ist nicht erreichbar!";
      else if( eTranscendenceReadable.equals( type ) )
        return "Lokales Datenbankprojekt im Lesemodus";
      else if( eTranscendenceReadableServerLocked.equals( type ) )
        return "Lokales Datenbankprojekt im Lesemodus - zur Zeit in Bearbeitung";
      else if( eTranscendenceWriteable.equals( type ) )
        return "Lokales Datenbankprojekt im Schreibmodus";

      throw new NotImplementedException();
    }
  }

  public ProjectOpenAction( final IKalypsoModule module, final ILocalProject handler )
  {
    m_module = module;
    m_handler = handler;

    try
    {
      if( handler instanceof ITranscendenceProject )
      {
        final ITranscendenceProject transcendence = (ITranscendenceProject) handler;
        final boolean localLock = transcendence.getRemotePreferences().isLocked();
        final Boolean serverLock = transcendence.getBean().isProjectLockedForEditing();
        if( localLock )
          m_type = OPEN_TYPE.eTranscendenceWriteable;
        else if( serverLock )
          m_type = OPEN_TYPE.eTranscendenceReadableServerLocked;
        else
          m_type = OPEN_TYPE.eTranscendenceReadable;
      }
      else
      {
        final IRemoteProjectPreferences remotePreferences = handler.getRemotePreferences();
        final boolean onServer = remotePreferences.isOnServer();
        if( onServer )
          m_type = OPEN_TYPE.eLocalOffline;
        else
          m_type = OPEN_TYPE.eLocal;
      }

    }
    catch( final CoreException e )
    {
      KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

  }

  /**
   * @see org.kalypso.project.database.client.extension.database.handlers.actions.IProjectAction#render(org.eclipse.swt.widgets.Composite,
   *      org.eclipse.ui.forms.widgets.FormToolkit)
   */
  @Override
  public void render( final Composite body, final FormToolkit toolkit )
  {
    final ImageHyperlink link = toolkit.createImageHyperlink( body, SWT.NULL );
    link.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    link.setImage( m_type.getImage() );
    link.setText( m_handler.getName() );

    link.setToolTipText( String.format( "�ffne Projekt: %s - Status: %s", m_handler.getName(), m_type.getStatus() ) );

    link.addHyperlinkListener( new HyperlinkAdapter()
    {
      /**
       * @see org.eclipse.ui.forms.events.HyperlinkAdapter#linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent)
       */
      @Override
      public void linkActivated( final HyperlinkEvent e )
      {
        final IKalypsoModuleProjectOpenAction action = m_module.getDatabaseSettings().getProjectOpenAction();
        action.open( m_handler.getProject() );
      }
    } );
  }
}
