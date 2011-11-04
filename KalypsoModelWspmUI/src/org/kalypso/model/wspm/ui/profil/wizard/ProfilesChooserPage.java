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
package org.kalypso.model.wspm.ui.profil.wizard;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.kalypso.commons.java.lang.Arrays;
import org.kalypso.contribs.eclipse.jface.wizard.ArrayChooserPage;
import org.kalypso.model.wspm.ui.action.ProfileSelection;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.results.IResultInterpolationSettings;
import org.kalypso.model.wspm.ui.profil.wizard.results.ResultInterpolationSettingsComposite;
import org.kalypso.ui.editor.gmleditor.ui.GMLLabelProvider;

/**
 * @author kimwerner
 */
public class ProfilesChooserPage extends ArrayChooserPage
{
  private boolean m_showResultInterpolationSettings = false;

  private ResultInterpolationSettingsComposite m_resultInterpolationSettingsComposite;

  public ProfilesChooserPage( final String message, final ProfileSelection selection, final boolean useDialogSettings )
  {
    this( message, selection, useDialogSettings, 1 );
  }

  public ProfilesChooserPage( final String message, final ProfileSelection selection, final boolean useDialogSettings, final int numToSelect )
  {
    this( message, selection.getProfiles(), new Object[0], selection.getSelectedProfiles(), numToSelect, useDialogSettings );//$NON-NLS-1$//$NON-NLS-2$
  }

  public ProfilesChooserPage( final String message, final Object chooseables, final Object[] selected, final Object[] checked, final int numToSelect, final boolean useDialogSettings )
  {
    super( chooseables, selected, checked, numToSelect, "profilesChooserPage", Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.ProfilesChooserPage.1" ), null, useDialogSettings );//$NON-NLS-1$//$NON-NLS-2$
    setLabelProvider( new GMLLabelProvider() );
    setDescription( message );

    if( chooseables instanceof Object[] && Arrays.isEmpty( (Object[]) chooseables ) )
    {
      setMessage( Messages.getString( "ProfilesChooserPage.0" ), IMessageProvider.WARNING ); //$NON-NLS-1$
    }
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.ArrayChooserPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    super.createControl( parent );

    final Composite panel = (Composite) getControl();
    final Control interpolationGroup = createInterpolationGroup( panel );
    if( interpolationGroup != null )
    {
      interpolationGroup.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    }
  }

  private Control createInterpolationGroup( final Composite parent )
  {
    if( !m_showResultInterpolationSettings )
      return null;

    m_resultInterpolationSettingsComposite = new ResultInterpolationSettingsComposite( getDialogSettings() );
    return m_resultInterpolationSettingsComposite.createControl( parent );
  }

  public void setShowResultInterpolationSettings( final boolean showResultInterpolationSettings )
  {
    m_showResultInterpolationSettings = showResultInterpolationSettings;
  }

  public IResultInterpolationSettings getResultInterpolationSettings( )
  {
    return m_resultInterpolationSettingsComposite;
  }
}
