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
package org.kalypso.contribs.eclipse.ui.intro.config;

import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
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
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
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
        throw new CoreException( StatusUtilities.createErrorStatus( Messages.getString("org.kalypso.contribs.eclipse.ui.intro.config.NewWizardAction.1") ) ); //$NON-NLS-1$

      final IWorkbench workbench = PlatformUI.getWorkbench();
      final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

      // hide intro
      final IIntroManager introManager = workbench.getIntroManager();
      introManager.closeIntro( introManager.getIntro() );

      // open new-wizard
      final IWizardDescriptor wizardDesc = WorkbenchPlugin.getDefault().getNewWizardRegistry().findWizard( wizardId );

      if( wizardDesc == null )
        throw new CoreException( StatusUtilities.createErrorStatus( Messages.getString("org.kalypso.contribs.eclipse.ui.intro.config.NewWizardAction.2") + wizardId ) ); //$NON-NLS-1$

      final IAction action = new NewWizardShortcutAction( workbenchWindow, wizardDesc );
      action.run();
    }
    catch( final CoreException e )
    {
      final IStatus status = e.getStatus();
      ErrorDialog.openError( site.getShell(), Messages.getString("org.kalypso.contribs.eclipse.ui.intro.config.NewWizardAction.3"), Messages.getString("org.kalypso.contribs.eclipse.ui.intro.config.NewWizardAction.4"), status ); //$NON-NLS-1$ //$NON-NLS-2$
      EclipsePlatformContributionsPlugin.getDefault().getLog().log( status );
    }
  }

}
