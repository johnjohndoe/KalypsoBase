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
package org.kalypso.module.welcome;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.jface.wizard.WizardDialog2;
import org.kalypso.module.welcome.actions.ImportProjectAction;

/**
 * Action for calling the new project wizard
 * 
 * @author Dirk Kuch
 */
public class SpecialImportProjectAction extends Action
{
  private final INewProjectWizardProvider m_wizardProvider;

  public SpecialImportProjectAction( final String label, final INewProjectWizardProvider wizardProvider )
  {
    m_wizardProvider = wizardProvider;
    setImageDescriptor( ImageDescriptor.createFromURL( ImportProjectAction.class.getResource( "icons/project_import.gif" ) ) ); //$NON-NLS-1$
    setText( label );
  }

  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final Event event )
  {
    final Shell shell = event.widget.getDisplay().getActiveShell();
    final IWizard importWizard = m_wizardProvider.createWizard();
    if( importWizard instanceof IWorkbenchWizard )
      ((IWorkbenchWizard) importWizard).init( PlatformUI.getWorkbench(), null );

    final WizardDialog2 dialog = new WizardDialog2( shell, importWizard );
    dialog.setRememberSize( true );
    dialog.open();
  }
}
