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
package org.kalypso.model.wspm.ui.profil.wizard.importProfile;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.model.wspm.core.gml.WspmWaterBody;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ui.editor.gmleditor.ui.FeatureAssociationTypeElement;

/**
 * A wizard to import profile data (right now just as trippel) into a WSPM Model.
 * 
 * @author Thomas Jung
 */
public class ImportProfileWizard extends Wizard implements IWizard
{
  public static String PROFIL_TYPE_PASCHE = "org.kalypso.model.wspm.tuhh.profiletype"; //$NON-NLS-1$

  protected ImportProfilePage m_profilePage;

  private final CommandableWorkspace m_workspace;

  private final WspmWaterBody m_water;

  public ImportProfileWizard( final FeatureAssociationTypeElement fate, final CommandableWorkspace workspace )
  {
    m_water = (WspmWaterBody) fate.getParentFeature();
    m_workspace = workspace;
    setWindowTitle( "Kalypso Profil Import" ); //$NON-NLS-1$

    setNeedsProgressMonitor( true );
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages( )
  {
    /* Choose profile data */
    m_profilePage = new ImportProfilePage( "chooseProfileData", org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.wizard.ImportProfileWizard.0" ), null ); //$NON-NLS-1$ //$NON-NLS-2$
    m_profilePage.setDescription( org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.wizard.ImportProfileWizard.1" ) ); //$NON-NLS-1$

    addPage( m_profilePage );
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    final File trippelFile = m_profilePage.getFile();
    final String separator = m_profilePage.getSeparator();
    final String crs = m_profilePage.getCoordinateSystem();

    /* Do import */
    final ImportTrippleOperation op = new ImportTrippleOperation( trippelFile, separator, crs, m_water, m_workspace );

    final IStatus status = RunnableContextHelper.execute( getContainer(), true, false, op );
    if( !status.isOK() )
      KalypsoModelWspmUIPlugin.getDefault().getLog().log( status );
    ErrorDialog.openError( getShell(), getWindowTitle(), "Probleme beim Profil-Import", status );

    return !status.matches( IStatus.ERROR );
  }
}
