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
package org.kalypso.contribs.eclipse.jface.wizard;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.ModalRunnableContext;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;

/**
 * Extension of the {@link WizardDialog}.
 * <p>
 * Enhances the existing class by the following features:
 * </p>
 * <ul>
 * <li>remember the size of the dialog in the wizard' s dialog settings</li>
 * <li>allow access to buttons</li>
 * <li>allow running operations without blocking the user interface</li>
 * </ul>
 * 
 * @author Gernot Belger
 */
public class WizardDialog2 extends WizardDialog
{
  private static final String SECTION_BOUNDS = WizardDialog2.class.getName() + "_bounds";

  private boolean m_doRememberSize;

  public WizardDialog2( final Shell parentShell, final IWizard newWizard )
  {
    super( parentShell, newWizard );
  }

  /** If set to true, the wizard remembers the size of the wizard. */
  public void setRememberSize( final boolean doRemember )
  {
    m_doRememberSize = doRemember;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
   */
  @Override
  protected IDialogSettings getDialogBoundsSettings( )
  {
    final IDialogSettings wizardSettings = getWizard().getDialogSettings();
    if( !m_doRememberSize || wizardSettings == null )
      return super.getDialogBoundsSettings();

    final IDialogSettings boundsSettings = wizardSettings.getSection( SECTION_BOUNDS );
    if( boundsSettings == null )
      return wizardSettings.addNewSection( SECTION_BOUNDS );

    return boundsSettings;
  }

  @Override
  /**
   * Overridden in order to make public.
   * 
   * @see WizardDialog#getButton(int id)
   */
  public Button getButton( final int id )
  {
    return super.getButton( id );
  }

  /**
   * Executes the given runnable in this dialog (similar to
   * {@link org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)},
   * but does NOT block the user interface while the operation is running.
   */
  public IStatus executeUnblocked( final boolean cancelable, final boolean enablePageNavigation, final ICoreRunnableWithProgress runnable )
  {
    final ProgressMonitorPart progressMonitorPart = (ProgressMonitorPart) getProgressMonitor();
    final boolean needsProgressMonitor = getWizard().needsProgressMonitor();
    final Button cancelButton = getButton( IDialogConstants.CANCEL_ID );

    try
    {
      /* Disable buttons */
      enableButton( IDialogConstants.FINISH_ID, false );
      if( !cancelable )
        enableButton( IDialogConstants.CANCEL_ID, false );

      if( !enablePageNavigation )
      {
        enableButton( IDialogConstants.NEXT_ID, false );
        enableButton( IDialogConstants.BACK_ID, false );
      }

      if( needsProgressMonitor )
      {
        progressMonitorPart.attachToCancelComponent( cancelButton );
        progressMonitorPart.setVisible( true );
      }

      final ModalRunnableContext rc = new ModalRunnableContext( progressMonitorPart, getShell().getDisplay() );
      return RunnableContextHelper.execute( rc, true, cancelable, runnable );
    }
    finally
    {
      // shell may be disposed, if dialog was canceled
      final Shell shell = getShell();
      if( shell != null && !shell.isDisposed() )
      {
        enableButton( IDialogConstants.CANCEL_ID, true );

        progressMonitorPart.setVisible( false );
        progressMonitorPart.removeFromCancelComponent( cancelButton );

        updateButtons();
      }
    }
  }

  private void enableButton( final int buttonId, final boolean enabled )
  {
    final Button button = getButton( buttonId );
    if( button != null )
      button.setEnabled( enabled );
  }
}
