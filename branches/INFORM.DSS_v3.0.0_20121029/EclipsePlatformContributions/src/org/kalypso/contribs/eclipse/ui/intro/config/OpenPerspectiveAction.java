/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse.ui.intro.config;

import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.eclipse.ui.part.FileEditorInput;
import org.kalypso.contribs.eclipse.EclipsePlatformContributionsPlugin;
import org.kalypso.contribs.eclipse.i18n.Messages;

/**
 * This intro action closes the intro view and opens the specified perspective.
 * <p>
 * Action paramaters:
 * <ul>
 * <li>perspectiveId: The id of the perspective to open.</li>
 * </ul>
 * </p>
 * 
 * @see org.eclipse.ui.intro.config.IIntroAction
 * @author Gernot Belger
 */
public class OpenPerspectiveAction implements IIntroAction
{
  /**
   * @see org.eclipse.ui.intro.config.IIntroAction#run(org.eclipse.ui.intro.IIntroSite, java.util.Properties)
   */
  @Override
  public void run( final IIntroSite site, final Properties params )
  {
    try
    {
      /* Get perspective id */
      final String perspectiveID = params.getProperty( "perspectiveId", null ); //$NON-NLS-1$
      final String filePathStr = params.getProperty( "file", null ); //$NON-NLS-1$
      final String editorID = params.getProperty( "editorId", null ); //$NON-NLS-1$
      if( perspectiveID == null )
      {
        final IStatus status = new Status( IStatus.ERROR, EclipsePlatformContributionsPlugin.getID(), Messages.getString( "org.kalypso.contribs.eclipse.ui.intro.config.OpenPerspectiveAction.3" ) ); //$NON-NLS-1$
        throw new CoreException( status );
      }

      final IWorkbench workbench = PlatformUI.getWorkbench();

      // hide intro
      final IIntroManager introManager = workbench.getIntroManager();
      introManager.closeIntro( introManager.getIntro() );

      // show intro view
      final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
      workbench.showPerspective( perspectiveID, window );

      if( filePathStr != null )
      {
        final IPath filePath = Path.fromPortableString( filePathStr );
        final IWorkbenchPage page = window.getActivePage();

        final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile( filePath );
        page.openEditor( new FileEditorInput( file ), editorID );
      }
    }
    catch( final CoreException e )
    {
      final IStatus status = e.getStatus();
      ErrorDialog.openError( site.getShell(), Messages.getString( "org.kalypso.contribs.eclipse.ui.intro.config.OpenPerspectiveAction.4" ), Messages.getString( "org.kalypso.contribs.eclipse.ui.intro.config.OpenPerspectiveAction.5" ), status ); //$NON-NLS-1$ //$NON-NLS-2$
      EclipsePlatformContributionsPlugin.getDefault().getLog().log( status );
    }
  }

}
