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
package org.kalypso.model.wspm.ui.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.contribs.eclipse.jface.wizard.WizardDialog2;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;

/**
 * @author Gernot Belger
 */
public abstract class AbstractProfilesHandler extends AbstractHandler
{
  /**
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public final Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final Shell shell = HandlerUtil.getActiveShellChecked( event );
    final ISelection selection = HandlerUtil.getCurrentSelectionChecked( event );

    final ProfileSelection profileSelection = new ProfileSelection( selection );

    if( !profileSelection.hasProfiles() )
    {
      final String title = getTitle();
      final String errorMessage = getErrorMessage();
      final String message = errorMessage == null ? "No profiles found in selection. Please select one or more profiles." : errorMessage;
      MessageDialog.openWarning( shell, title, message ); //$NON-NLS-1$ //$NON-NLS-2$
      return null;
    }

    final IWizard wizard = createWizard( profileSelection );
    if( wizard instanceof Wizard )
    {
      final Wizard wizard2 = (Wizard) wizard;
      wizard2.setWindowTitle( getTitle() );
      wizard2.setDialogSettings( PluginUtilities.getDialogSettings( KalypsoModelWspmUIPlugin.getDefault(), wizard.getClass().getName() ) );
    }

    /* show wizard */
    final WizardDialog2 dialog = new WizardDialog2( shell, wizard );
    dialog.setRememberSize( true );
    dialog.open();

    return null;
  }

  protected abstract String getTitle( );

  protected abstract IWizard createWizard( final ProfileSelection profileSelection );

  /**
   * @deprecated We should always show the same error message here
   */
  @Deprecated
  protected abstract String getErrorMessage( );
}
