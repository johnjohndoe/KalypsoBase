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
package org.kalypso.model.wspm.core.gml.validation;

import java.text.DateFormat;
import java.util.Calendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.progress.IProgressConstants;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.debug.KalypsoModelWspmCoreDebug;
import org.kalypso.model.wspm.core.preferences.WspmCorePreferences;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileListener;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;
import org.kalypso.model.wspm.core.profil.validator.ValidatorRuleSet;

/**
 * Profil-listener which repairs and validates the profile, each time it changes.
 *
 * @author Gernot Belger
 */
public class ValidationProfilListener implements IProfileListener
{
  private final IPropertyChangeListener m_preferencesListener;

  private final WorkspaceJob m_validateJob;

  public ValidationProfilListener( final IProfile profile, final IFile file, final String editorID, final String featureID )
  {
    if( file == null ) // start calculation
    {
      m_validateJob = null;
      m_preferencesListener = null;

      return;
    }

    final String profiletype = profile.getType();
    final ValidatorRuleSet rules = KalypsoModelWspmCorePlugin.getValidatorSet( profiletype );

    m_validateJob = new ValidateProfileJob( editorID, file, featureID, profile, rules );
    m_validateJob.setSystem( true );
    m_validateJob.setRule( file.getWorkspace().getRuleFactory().markerRule( file ) );
    m_validateJob.setProperty( IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE );

    m_preferencesListener = new IPropertyChangeListener()
    {
      @Override
      public void propertyChange( final PropertyChangeEvent event )
      {
        if( WspmCorePreferences.P_VALIDATE_PROFILE.equals( event.getProperty() ) || WspmCorePreferences.P_VALIDATE_RULES_TO_EXCLUDE.equals( event.getProperty() ) )
          revalidate();
      }
    };

    // FIXME: listener never removed....
    WspmCorePreferences.addPreferenceListener( m_preferencesListener );

    revalidate();
  }

  public void dispose( )
  {
    KalypsoCorePlugin.getDefault().getPreferenceStore().removePropertyChangeListener( m_preferencesListener );
  }

  protected void revalidate( )
  {
    if( m_validateJob == null )
      return;

    KalypsoModelWspmCoreDebug.DEBUG_VALIDATION_MARKER.printf( "(validation_performance_check)Revalidate : %s\n", DateFormat.getTimeInstance().format( Calendar.getInstance().getTime() ) ); //$NON-NLS-1$

    m_validateJob.cancel(); // Just in case, to avoid too much validations

    m_validateJob.schedule( 100 );
  }

  @Override
  public void onProfilChanged( final ProfileChangeHint hint )
  {
    if( (hint.getEvent() & ProfileChangeHint.DATA_CHANGED) != 0 )
      revalidate();
  }

  @Override
  public void onProblemMarkerChanged( final IProfile source )
  {
    // Ignored, we are the cause for that...
  }
}
