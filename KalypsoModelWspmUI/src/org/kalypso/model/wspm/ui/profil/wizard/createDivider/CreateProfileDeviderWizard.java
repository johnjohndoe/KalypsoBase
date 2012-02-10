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
package org.kalypso.model.wspm.ui.profil.wizard.createDivider;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.jface.wizard.ArrayChooserPage;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.action.ProfileSelection;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.ProfilesChooserPage;
import org.kalypso.observation.result.IComponent;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypsodeegree.model.feature.FeatureList;

/**
 * A wizard to create profile deviders from lines/polygones.
 * 
 * @author Gernot Belger
 */
public class CreateProfileDeviderWizard extends Wizard
{
  private final ArrayChooserPage m_profileChooserPage;

  private final CreateProfileDeviderPage m_deviderPage;

  private final IKalypsoFeatureTheme m_theme;

  public CreateProfileDeviderWizard( final IKalypsoFeatureTheme theme, final ProfileSelection profileSelection )
  {
    m_theme = theme;

    setWindowTitle( Messages.getString( "org.kalypso.model.wspm.ui.wizard.CreateProfileDeviderWizard.0" ) ); //$NON-NLS-1$
    setNeedsProgressMonitor( true );
    setDialogSettings( PluginUtilities.getDialogSettings( KalypsoModelWspmUIPlugin.getDefault(), getClass().getName() ) );

    final String msg = Messages.getString( "org.kalypso.model.wspm.ui.wizard.CreateProfileDeviderWizard.3" ); //$NON-NLS-1$
    m_profileChooserPage = new ProfilesChooserPage( msg, profileSelection, false );

    final IProfileFeature firstProfile = profileSelection.getProfiles()[0];
    final String type = firstProfile.getProfileType();

    m_deviderPage = new CreateProfileDeviderPage( m_theme, type );

    addPage( m_profileChooserPage );
    addPage( m_deviderPage );
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#dispose()
   */
  @Override
  public void dispose( )
  {
    m_profileChooserPage.getLabelProvider().dispose();

    super.dispose();
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    final Object[] choosen = m_profileChooserPage.getChoosen();
    if( choosen.length == 0 )
      return true;

    final FeatureList lineFeatures = m_deviderPage.getFeatures();
    final IPropertyType lineGeomProperty = m_deviderPage.getGeomProperty();
    final IComponent deviderType = m_deviderPage.getDeviderType();
    final boolean useExisting = m_deviderPage.isUseExisting();

    final CreateDividerOperation operation = new CreateDividerOperation( choosen, lineFeatures, lineGeomProperty, deviderType, useExisting, m_theme );

    final IStatus status = RunnableContextHelper.execute( getContainer(), true, true, operation );
    ErrorDialog.openError( getShell(), getWindowTitle(), Messages.getString( "org.kalypso.model.wspm.ui.wizard.CreateProfileDeviderWizard.5" ), status ); //$NON-NLS-1$

    return status.isOK();
  }
}