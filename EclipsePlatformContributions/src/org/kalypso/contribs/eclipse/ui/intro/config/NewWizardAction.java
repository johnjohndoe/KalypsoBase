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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.kalypso.contribs.eclipse.EclipsePlatformContributionsPlugin;
import org.kalypso.contribs.eclipse.i18n.Messages;

/**
 * This intro action closes the intro view and opens the specified new-wizard.
 * <p>
 * Action paramaters:
 * <ul>
 * <li>wizardId: The id of the new-wizard (see {@link org.eclipse.ui.INewWizard}) to open.</li>
 * </ul>
 * </p>
 * 
 * @see org.eclipse.ui.intro.config.IIntroAction
 * @author Gernot Belger
 */
@SuppressWarnings("restriction")
public class NewWizardAction implements IIntroAction
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
      final String wizardId = params.getProperty( "wizardId", null ); //$NON-NLS-1$
      if( wizardId == null )
      {
        final IStatus status = new Status( IStatus.ERROR, EclipsePlatformContributionsPlugin.getID(), Messages.getString( "org.kalypso.contribs.eclipse.ui.intro.config.NewWizardAction.1" ) ); //$NON-NLS-1$
        throw new CoreException( status );
      }

      final IWorkbench workbench = PlatformUI.getWorkbench();
      final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

      // hide intro
      final IIntroManager introManager = workbench.getIntroManager();
      introManager.closeIntro( introManager.getIntro() );

      // open new-wizard
      final IWizardDescriptor wizardDesc = WorkbenchPlugin.getDefault().getNewWizardRegistry().findWizard( wizardId );

      if( wizardDesc == null )
      {
        final IStatus status = new Status( IStatus.ERROR, EclipsePlatformContributionsPlugin.getID(), Messages.getString( "org.kalypso.contribs.eclipse.ui.intro.config.NewWizardAction.2" ) + wizardId ); //$NON-NLS-1$
        throw new CoreException( status );
      }

      final IAction action = new NewWizardShortcutAction( workbenchWindow, wizardDesc );
      action.run();
    }
    catch( final CoreException e )
    {
      final IStatus status = e.getStatus();
      ErrorDialog.openError( site.getShell(), Messages.getString( "org.kalypso.contribs.eclipse.ui.intro.config.NewWizardAction.3" ), Messages.getString( "org.kalypso.contribs.eclipse.ui.intro.config.NewWizardAction.4" ), status ); //$NON-NLS-1$ //$NON-NLS-2$
      EclipsePlatformContributionsPlugin.getDefault().getLog().log( status );
    }
  }

}
