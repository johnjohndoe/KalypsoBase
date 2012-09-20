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

import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.progress.IProgressConstants;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.debug.KalypsoModelWspmCoreDebug;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileListener;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;
import org.kalypso.model.wspm.core.profil.validator.IValidatorMarkerCollector;
import org.kalypso.model.wspm.core.profil.validator.ValidatorRuleSet;

/**
 * Profil-listener which repairs and validates the profile, each time it changes.
 *
 * @author Gernot Belger
 */
@SuppressWarnings("restriction")
public class ValidationProfilListener implements IProfileListener
{
  private final IPropertyChangeListener m_propertyListener;

  private final WorkspaceJob m_validateJob;

  public ValidationProfilListener( final IProfile profile, final IFile file, final String editorID, final String featureID )
  {
    if( file == null ) // start calculation
    {
      m_validateJob = null;
      m_propertyListener = null;

      return;
    }

    final String profiletype = profile.getType();
    final ValidatorRuleSet rules = KalypsoModelWspmCorePlugin.getValidatorSet( profiletype );

    m_validateJob = new WorkspaceJob( Messages.getString( "ValidationProfilListener_0" ) ) //$NON-NLS-1$
    {
      @Override
      public IStatus runInWorkspace( final IProgressMonitor monitor )
      {
        if( monitor.isCanceled() )
          return Status.CANCEL_STATUS;

        final IPreferenceStore preferenceStore = KalypsoCorePlugin.getDefault().getPreferenceStore();
        final boolean validate = preferenceStore.getBoolean( ValidationPreferenceConstants.P_VALIDATE_PROFILE );
        final String excludes = preferenceStore.getString( ValidationPreferenceConstants.P_VALIDATE_RULES_TO_EXCLUDE );

        final IValidatorMarkerCollector collector = new ResourceValidatorMarkerCollector( file, editorID, "" + profile.getStation(), featureID ); //$NON-NLS-1$

        try
        {
          collector.reset( featureID );
        }
        catch( final ResourceException e1 )
        {
          // ignore: this kind of exception is thrown, if the marker id to be deleted is already out of scope.
        }
        catch( final CoreException e )
        {
          return e.getStatus();
        }

        KalypsoModelWspmCoreDebug.DEBUG_VALIDATION_MARKER.printf( " (validation_performance_check)    startValidation : %s\n", DateFormat.getTimeInstance().format( Calendar.getInstance().getTime() ) ); //$NON-NLS-1$

        // TODO: use monitor and check for cancel
        final IStatus status = rules.validateProfile( profile, collector, validate, excludes.split( ";" ), monitor ); //$NON-NLS-1$

        final IMarker[] markers = collector.getMarkers();
        profile.setProblemMarker( markers );
        return status;
      }
    };

    m_validateJob.setSystem( true );
    m_validateJob.setRule( file.getWorkspace().getRuleFactory().markerRule( file ) );
    m_validateJob.setProperty( IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE );

    m_propertyListener = new IPropertyChangeListener()
    {
      @Override
      public void propertyChange( final PropertyChangeEvent event )
      {
        if( ValidationPreferenceConstants.P_VALIDATE_PROFILE.equals( event.getProperty() ) || ValidationPreferenceConstants.P_VALIDATE_RULES_TO_EXCLUDE.equals( event.getProperty() ) )
        {
          revalidate(); // TODO: validate all profiles... in that case!
        }
      }
    };

    KalypsoCorePlugin.getDefault().getPreferenceStore().addPropertyChangeListener( m_propertyListener );

    revalidate();
  }

  public void dispose( )
  {
    KalypsoCorePlugin.getDefault().getPreferenceStore().removePropertyChangeListener( m_propertyListener );
  }

  protected void revalidate( )
  {
    KalypsoModelWspmCoreDebug.DEBUG_VALIDATION_MARKER.printf( "(validation_performance_check)Revalidate : %s\n", DateFormat.getTimeInstance().format( Calendar.getInstance().getTime() ) ); //$NON-NLS-1$

    if( Objects.isNotNull( m_validateJob ) )
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
    // Ignored
  }
}
