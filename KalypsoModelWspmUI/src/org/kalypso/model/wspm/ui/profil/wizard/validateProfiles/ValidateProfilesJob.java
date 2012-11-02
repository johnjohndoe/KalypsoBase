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
package org.kalypso.model.wspm.ui.profil.wizard.validateProfiles;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.validation.ResourceValidatorMarkerCollector;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.reparator.IProfileMarkerResolution;
import org.kalypso.model.wspm.core.profil.validator.IValidatorMarkerCollector;
import org.kalypso.model.wspm.core.profil.validator.IValidatorRule;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.ogc.gml.command.ChangeFeaturesCommand;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;

/**
 * @author Dirk Kuch
 */
public class ValidateProfilesJob extends UIJob
{
  private final IProfileFeature[] m_profileFeatures;

  private final IValidatorRule[] m_rules;

  private final Object[] m_quickFixes;

  private final CommandableWorkspace m_workspace;

  private IFile m_resource;

  public ValidateProfilesJob( final IProfileFeature[] profileFeatures, final IValidatorRule[] rules, final Object[] quickFixes, final CommandableWorkspace workspace )
  {
    super( Messages.getString("ValidateProfilesJob_0") ); //$NON-NLS-1$
    m_profileFeatures = profileFeatures;
    m_rules = rules;
    m_quickFixes = quickFixes;
    m_workspace = workspace;

    setSystem( false );
    setUser( true );
  }

  @Override
  public IStatus runInUIThread( final IProgressMonitor monitor )
  {
    final List<IStatus> stati = new ArrayList<>();

    doInit();

    for( final IProfileFeature profileFeature : m_profileFeatures )
    {
      if( Objects.isNull( profileFeature ) )
        continue;

      final IProfile profile = profileFeature.getProfile();
      if( Objects.isNull( profile ) )
        continue;

      try
      {
        final IMarker[] markers = m_resource.findMarkers( KalypsoModelWspmCorePlugin.MARKER_ID, true, IResource.DEPTH_ZERO );
        for( final IMarker marker : markers )
        {
          if( marker.getAttribute( IValidatorMarkerCollector.MARKER_ATTRIBUTE_PROFILE_ID ).equals( profileFeature.getId() ) )
          {
            marker.delete();
          }
        }
      }
      catch( final CoreException e1 )
      {
        stati.add( new Status( IStatus.ERROR, KalypsoModelWspmUIPlugin.ID, "Failed to delete old problem markers.", e1 ) ); //$NON-NLS-1$
      }

      final IValidatorMarkerCollector collector = new ResourceValidatorMarkerCollector( m_resource, null, "" + profile.getStation(), profileFeature.getId() ); //$NON-NLS-1$
      for( final IValidatorRule rule : m_rules )
      {
        try
        {
          rule.validate( profile, collector );
        }
        catch( final CoreException e )
        {
          stati.add( new Status( IStatus.ERROR, KalypsoModelWspmUIPlugin.ID, "Profile valdiation rule failed", e ) ); //$NON-NLS-1$
        }
      }

      final IMarker[] markers = collector.getMarkers();
      final HashMap<String, String> uiResults = new HashMap<>();

      for( final IMarker marker : markers )
      {
        final String quickFixRes = marker.getAttribute( IValidatorMarkerCollector.MARKER_ATTRIBUTE_QUICK_FIX_RESOLUTIONS, null );
        if( StringUtils.isNotEmpty( quickFixRes ) && ArrayUtils.isNotEmpty( m_quickFixes ) )
        {
          final IProfileMarkerResolution[] resultions = toProfileMarkerResultions( quickFixRes );

          for( final IProfileMarkerResolution resultion : resultions )
          {

            final Object quickFix = findQuickFix( resultion );
            final String quickFixClazz = quickFix.getClass().getName();

            if( resultion != null && resultion.getClass().getName().equals( quickFixClazz ) )
            {
              if( resultion.hasUI() )
              {
                final String uiResult = uiResults.get( quickFixClazz );
                if( uiResult == null )
                {
                  final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                  uiResults.put( quickFixClazz, resultion.getUIresult( shell, profile ) );
                }
                resultion.setUIresult( uiResults.get( quickFixClazz ) );
              }

              final boolean resolved = resultion.resolve( profile );
              if( resolved )
              {
                try
                {
                  marker.delete();
                }
                catch( final CoreException e )
                {
                  stati.add( new Status( IStatus.ERROR, KalypsoModelWspmUIPlugin.ID, "Deletion of marker failed.", e ) ); //$NON-NLS-1$
                }
              }
            }
          }
        }
      }
    }

    try
    {
      m_workspace.postCommand( new ChangeFeaturesCommand( m_workspace, new FeatureChange[] {} ) );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return StatusUtilities.createStatus( stati, Messages.getString("ValidateProfilesJob_1") ); //$NON-NLS-1$
  }

  private Object findQuickFix( final IProfileMarkerResolution resultion )
  {
    if( Objects.isNull( resultion ) )
      return null;

    for( final Object quickFix : m_quickFixes )
    {
      if( resultion.getClass().getName().equals( quickFix.getClass().getName() ) )
        return quickFix;
    }

    return null;
  }

  private IProfileMarkerResolution[] toProfileMarkerResultions( final String quickFixRes )
  {
    if( StringUtils.isEmpty( quickFixRes ) )
      return new IProfileMarkerResolution[] {};

    final Set<IProfileMarkerResolution> result = new LinkedHashSet<>();

    final String[] resolutions = StringUtils.split( quickFixRes, '\u0000' );
    for( final String resultion : resolutions )
    {
      result.add( KalypsoModelWspmCoreExtensions.getReparatorRule( resultion ) );
    }

    return result.toArray( new IProfileMarkerResolution[] {} );
  }

  private void doInit( )
  {
    final URL workspaceContext = m_workspace.getContext();
    m_resource = workspaceContext == null ? null : ResourceUtilities.findFileFromURL( workspaceContext );
  }

}
