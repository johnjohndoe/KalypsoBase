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
package de.renew.workflow.connector.context;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;
import de.renew.workflow.connector.internal.WorkflowConnectorPlugin;

/**
 * @author Gernot Belger
 */
public class ActiveWorkContextResourceListener implements IResourceChangeListener
{
  private final ActiveWorkContext m_activeWorkContext;

  public ActiveWorkContextResourceListener( final ActiveWorkContext activeWorkContext )
  {
    m_activeWorkContext = activeWorkContext;
  }

  @Override
  public void resourceChanged( final IResourceChangeEvent event )
  {
    // TODO:
    // Handles cases where parts of a project (scenario folder, data files) are deleted

    if( event.getType() == IResourceChangeEvent.PRE_DELETE || event.getType() == IResourceChangeEvent.PRE_CLOSE )
    {
      // if the currently active project is deleted or closed
      final ScenarioHandlingProjectNature currentNature = m_activeWorkContext.getCurrentProject();
      if( currentNature != null && currentNature.getProject().equals( event.getResource() ) )
      {
        try
        {
          // project was closed or deleted, deactivate the current case
          m_activeWorkContext.setCurrentCase( null );
        }
        catch( final CoreException e )
        {
          final Display display = PlatformUI.getWorkbench().getDisplay();
          final Shell activeShell = display.getActiveShell();
          final IStatus status = e.getStatus();
          ErrorDialog.openError( activeShell, "Problem beim Löschen des Projektes", "Projekt wurde nicht deaktiviert.", status );
          WorkflowConnectorPlugin.getDefault().getLog().log( status );
        }
      }
    }
  }
}