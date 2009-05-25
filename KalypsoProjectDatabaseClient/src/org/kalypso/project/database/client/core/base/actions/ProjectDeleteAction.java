/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.kalypso.project.database.client.extension.database.IProjectDatabaseUiLocker;
import org.kalypso.project.database.client.extension.database.handlers.ILocalProject;

/**
 * @author kuch
 */
public class ProjectDeleteAction implements IProjectAction
{
  private static final Image IMG_DELETE = new Image( null, ProjectInfoAction.class.getResourceAsStream( "images/action_delete.gif" ) );

  protected final ILocalProject m_handler;

  protected final IProjectDatabaseUiLocker m_locker;

  public ProjectDeleteAction( final ILocalProject handler, final IProjectDatabaseUiLocker locker )
  {
    m_handler = handler;
    m_locker = locker;
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.handlers.actions.IProjectAction#render(org.eclipse.swt.widgets.Composite,
   *      org.eclipse.ui.forms.widgets.FormToolkit)
   */
  @Override
  public void render( final Composite body, final FormToolkit toolkit )
  {
    final ImageHyperlink link = toolkit.createImageHyperlink( body, SWT.NULL );
    link.setLayoutData( new GridData( GridData.FILL, GridData.FILL, false, false ) );
    link.setImage( IMG_DELETE );
    link.setToolTipText( "Lösche lokales Projekt" );

    link.addHyperlinkListener( new HyperlinkAdapter()
    {
      /**
       * @see org.eclipse.ui.forms.events.HyperlinkAdapter#linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent)
       */
      @Override
      public void linkActivated( final HyperlinkEvent e )
      {
        // FIXME if m_handler is ITranscendeProject and locally locked -> remove lock and then delete project

        try
        {
          m_locker.acquireUiUpdateLock();

          final DeleteResourceAction action = new DeleteResourceAction( new SameShellProvider( link.getShell() ) );
          action.selectionChanged( new StructuredSelection( m_handler.getProject() ) );
          action.run();
        }
        finally
        {
          m_locker.releaseUiUpdateLock();
        }

      }
    } );

  }

}
