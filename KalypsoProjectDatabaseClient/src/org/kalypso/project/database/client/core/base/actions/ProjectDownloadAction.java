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
package org.kalypso.project.database.client.core.base.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;
import org.kalypso.contribs.eclipse.core.resources.ProjectTemplate;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.core.base.worker.RemoteImportWorker;
import org.kalypso.project.database.client.core.model.projects.IRemoteProject;
import org.kalypso.project.database.client.i18n.Messages;
import org.kalypso.project.database.sei.beans.KalypsoProjectBean;

/**
 * @author Dirk Kuch
 */
public class ProjectDownloadAction extends Action
{
  private static final ImageDescriptor IMG_DOWNLOAD = ImageDescriptor.createFromURL( ProjectDownloadAction.class.getResource( "images/action_download.gif" ) ); //$NON-NLS-1$

  protected final IKalypsoModule m_module;

  protected final IRemoteProject m_handler;

  public ProjectDownloadAction( final IKalypsoModule module, final IRemoteProject handler )
  {
    m_module = module;
    m_handler = handler;

    setToolTipText( Messages.getString( "org.kalypso.project.database.client.core.base.actions.ProjectDownloadAction.1" ) ); //$NON-NLS-1$
    setImageDescriptor( IMG_DOWNLOAD );
  }

  @Override
  public void runWithEvent( final Event event )
  {
    try
    {
      /* sort beans */
      final KalypsoProjectBean bean = m_handler.getBean();
      // FIXME: this is crude! Instead, introduce an interface 'IRrojectProvider' with two implementations:
      // categoryId-based (for 'normal' projects) and another that is based on remote projects
      final ProjectTemplate template = new ProjectTemplate( null, String.format( "%s - Version %d", bean.getName(), bean.getProjectVersion() ), bean.getUnixName(), bean.getDescription(), null, bean.getUrl() ); //$NON-NLS-1$

      final Map<ProjectTemplate, KalypsoProjectBean> mapping = new HashMap<>();
      mapping.put( template, bean );

      final RemoteImportWorker worker = new RemoteImportWorker( new ProjectTemplate[] { template }, mapping, m_module );
      worker.execute();
    }
    catch( final Exception e1 )
    {
      KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e1 ) );

    }
  }
}