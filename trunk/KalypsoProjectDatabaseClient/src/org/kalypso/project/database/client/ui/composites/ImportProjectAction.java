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
package org.kalypso.project.database.client.ui.composites;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;
import org.eclipse.ui.wizards.datatransfer.ExternalProjectImportWizard;
import org.kalypso.contribs.eclipse.jface.wizard.WizardDialog2;
import org.kalypso.project.database.client.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class ImportProjectAction extends Action
{
  public ImportProjectAction( )
  {
    setText( Messages.getString( "org.kalypso.project.database.client.ui.project.wizard.imp.ImportProjectComposite.1" ) ); //$NON-NLS-1$

    // FIXME: ugly: move to common place
    final ImageDescriptor IMG_IMPORT = ImageDescriptor.createFromURL( ImportProjectAction.class.getResource( "icons/project_import.gif" ) ); //$NON-NLS-1$
    setImageDescriptor( IMG_IMPORT );
  }

  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final Event event )
  {
    final Shell shell = event.widget.getDisplay().getActiveShell();

    final ExternalProjectImportWizard wizard = new ExternalProjectImportWizard();
    wizard.init( PlatformUI.getWorkbench(), null );

    final WizardDialog2 dialog = new WizardDialog2( shell, wizard );
    dialog.setRememberSize( true );

    /* @hack */
    // TODO: hack for what?! Do we really need this?
    dialog.addPageChangedListener( new IPageChangedListener()
    {
      @Override
      public void pageChanged( final PageChangedEvent pageEvent )
      {
        final Object page = pageEvent.getSelectedPage();
        if( page instanceof WizardProjectsImportPage )
        {
          final WizardProjectsImportPage importPage = (WizardProjectsImportPage) page;

          final Composite control = (Composite) importPage.getControl();
          final Control[] children = control.getChildren();

          /* composite with radio buttons - import from directory, import from zip */
          final Composite composite = (Composite) children[0];
          final Control[] radios = composite.getChildren();

          ((Button) radios[0]).setSelection( false );

          ((Button) radios[3]).setSelection( true );
          radios[4].setEnabled( true );
          radios[5].setEnabled( true );

          importPage.getCopyCheckbox().setSelection( true );
          importPage.getCopyCheckbox().setEnabled( false );
        }
      }
    } );

    dialog.open();

    // FIXME: ask use to open the project now
    // Problems: which project was imported? there could also be several new projects as well.}
  }
}