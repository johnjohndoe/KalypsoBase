/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import org.kalypso.model.wspm.core.debug.KalypsoModelWspmCoreDebug;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.preferences.WspmCorePreferences;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.validator.IValidatorMarkerCollector;
import org.kalypso.model.wspm.core.profil.validator.ValidatorRuleSet;

/**
 * @author Gernot Belger
 */
public class ValidateProfileJob extends WorkspaceJob
{
  private final ValidatorRuleSet m_rules;

  private final String m_featureID;

  private final IFile m_file;

  private final IProfile m_profile;

  private final String m_editorID;

  public ValidateProfileJob( final String editorID, final IFile file, final String featureID, final IProfile profile, final ValidatorRuleSet rules )
  {
    super( Messages.getString( "ValidationProfilListener_0" ) ); //$NON-NLS-1$

    m_editorID = editorID;
    m_file = file;
    m_featureID = featureID;
    m_profile = profile;

    m_rules = rules;
  }

  @Override
  public IStatus runInWorkspace( final IProgressMonitor monitor )
  {
    if( monitor.isCanceled() )
      return Status.CANCEL_STATUS;

    final boolean validate = WspmCorePreferences.getValidateProfiles();
    final String[] excludes = WspmCorePreferences.getExcludedRules();

    final String profileStation = Double.toString( m_profile.getStation() );
    final IValidatorMarkerCollector collector = new ResourceValidatorMarkerCollector( m_file, m_editorID, profileStation, m_featureID );

    try
    {
      collector.reset( m_featureID );
    }
    catch( @SuppressWarnings( "restriction" ) final ResourceException e1 )
    {
      // ignore: this kind of exception is thrown, if the marker id to be deleted is already out of scope.
    }
    catch( final CoreException e )
    {
      return e.getStatus();
    }

    KalypsoModelWspmCoreDebug.DEBUG_VALIDATION_MARKER.printf( " (validation_performance_check)    startValidation : %s\n", DateFormat.getTimeInstance().format( Calendar.getInstance().getTime() ) ); //$NON-NLS-1$

    final IStatus status = doValidate( monitor, validate, excludes, collector );

    final IMarker[] markers = collector.getMarkers();
    m_profile.setProblemMarker( markers );

    return status;
  }

  private IStatus doValidate( final IProgressMonitor monitor, final boolean validate, final String[] excludes, final IValidatorMarkerCollector collector )
  {
    if( !validate )
      return Status.OK_STATUS;

    return m_rules.validateProfile( m_profile, collector, excludes, monitor ); //$NON-NLS-1$
  }
}