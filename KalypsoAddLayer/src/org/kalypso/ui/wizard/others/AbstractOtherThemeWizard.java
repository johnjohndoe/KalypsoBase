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
package org.kalypso.ui.wizard.others;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.addlayer.dnd.MapDropData;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.ui.wizard.AbstractDataImportWizard;

/**
 * @author Gernot Belger
 */
public abstract class AbstractOtherThemeWizard extends AbstractDataImportWizard
{
  private final ThemeNameWizardPage m_themeNameWizardPage;

  public AbstractOtherThemeWizard( final ThemeNameWizardPage themeNameWizardPage )
  {
    m_themeNameWizardPage = themeNameWizardPage;
    m_themeNameWizardPage.setDescription( Messages.getString( "org.kalypso.ui.wizard.others.AbstractOtherThemeWizard.0" ) ); //$NON-NLS-1$
  }

  @Override
  public void addPages( )
  {
    addPage( m_themeNameWizardPage );
  }

  /**
   * Throws {@link UnsupportedOperationException} by default,. Overwrite to implement.
   */
  @Override
  public void initFromDrop( final MapDropData data )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean performFinish( )
  {
    final IKalypsoLayerModell mapModell = getMapModel();
    final String themeName = m_themeNameWizardPage.getThemeName();
    final ICommandTarget commandTarget = getCommandTarget();
    final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
      {
        try
        {
          if( mapModell == null )
            return new Status( IStatus.ERROR, KalypsoAddLayerPlugin.getId(), Messages.getString( "org.kalypso.ui.wizard.others.AbstractOtherThemeWizard.1" ) ); //$NON-NLS-1$

          final ICommand command = createCommand( mapModell, themeName );
          commandTarget.postCommand( command, null );
        }
        catch( final Throwable t )
        {
          throw new InvocationTargetException( t );
        }

        return Status.OK_STATUS;
      }
    };

    final IStatus status = RunnableContextHelper.execute( getContainer(), true, false, operation );
    KalypsoAddLayerPlugin.getDefault().getLog().log( status );
    ErrorDialog.openError( getShell(), getWindowTitle(), Messages.getString( "org.kalypso.ui.wizard.others.AbstractOtherThemeWizard.2" ), status ); //$NON-NLS-1$

    return status.isOK();
  }

  protected abstract ICommand createCommand( IKalypsoLayerModell mapModell, String themeName );
}