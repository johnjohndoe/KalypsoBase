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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.ide.IDE;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.debug.KalypsoModelWspmCoreDebug;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.reparator.IProfileMarkerResolution;
import org.kalypso.model.wspm.core.profil.validator.IValidatorMarkerCollector;

public final class ResourceValidatorMarkerCollector implements IValidatorMarkerCollector
{
  private final IResource m_resource;

  private static final String[] USED_ATTRIBUTES = new String[] { IMarker.MESSAGE, IMarker.LOCATION, IMarker.SEVERITY, IMarker.TRANSIENT, IDE.EDITOR_ID_ATTR,
      IValidatorMarkerCollector.MARKER_ATTRIBUTE_POINTPOS, IValidatorMarkerCollector.MARKER_ATTRIBUTE_POINTPROPERTY, IValidatorMarkerCollector.MARKER_ATTRIBUTE_QUICK_FIX_RESOLUTIONS,
      IValidatorMarkerCollector.MARKER_ATTRIBUTE_PROFILE_ID, IValidatorMarkerCollector.MARKER_ATTRIBUTE_STATION };

  private final String m_editorID;

  private final List<IMarker> m_markers = new ArrayList<>();

  private final String m_profileFeatureID;

  private final String m_station;

  public ResourceValidatorMarkerCollector( final IResource resource, final String editorID, final String profileStation, final String profileID )
  {
    m_resource = resource;
    m_editorID = editorID;
    m_profileFeatureID = profileID;
    m_station = profileStation;
  }

  @Override
  public void createProfilMarker( final int severity, final String message, final String location, final int pointPos, final String pointProperty ) throws CoreException
  {
    createProfilMarker( severity, message, location, pointPos, pointProperty, new IProfileMarkerResolution[] {} );
  }

  @Override
  public void reset( ) throws CoreException
  {
    m_resource.deleteMarkers( KalypsoModelWspmCorePlugin.MARKER_ID, true, IResource.DEPTH_ZERO );
  }

  @Override
  public void reset( final String profilFeatureID ) throws CoreException
  {
    final IMarker[] markers = m_resource.findMarkers( KalypsoModelWspmCorePlugin.MARKER_ID, true, IResource.DEPTH_ZERO );
    final ArrayList<IMarker> toDelete = new ArrayList<>();
    for( final IMarker marker : markers )
    {
      if( marker == null )
      {
        continue;
      }

      final Object attribute = marker.getAttribute( IValidatorMarkerCollector.MARKER_ATTRIBUTE_PROFILE_ID );
      if( attribute == null )
      {
        continue;
      }

      if( attribute.equals( profilFeatureID ) )
      {
        toDelete.add( marker );
      }
    }
    if( !toDelete.isEmpty() )
    {
      for( final IMarker marker : toDelete )
      {
        marker.delete();
      }
    }
  }

  @Override
  public IMarker[] getMarkers( )
  {
    return m_markers.toArray( new IMarker[m_markers.size()] );
  }

  @Override
  public void createProfilMarker( final int severityError, final String msg, final IProfile profile, final IProfileMarkerResolution... markerResolutions ) throws CoreException
  {
    final String location = String.format( "km %.4f", profile.getStation() ); //$NON-NLS-1$

    createProfilMarker( severityError, msg, location, 0, null, markerResolutions );
  }

  @Override
  public void createProfilMarker( final int severity, final String message, final String location, final int pointPos, final String pointProperty, final IProfileMarkerResolution... markerResolutions ) throws CoreException
  {
    KalypsoModelWspmCoreDebug.DEBUG_VALIDATION_MARKER.printf( "%s - %s - %s - %s", severity, message, location, pointPos ); //$NON-NLS-1$

    final IMarker marker = m_resource.createMarker( KalypsoModelWspmCorePlugin.MARKER_ID );
    final String[] resMarkerStrings = new String[markerResolutions.length];
    for( int i = 0; i < markerResolutions.length; i++ )
    {
      resMarkerStrings[i] = markerResolutions[i].getSerializedParameter();
    }

    final String resMarkerSerialized = StringUtils.join( resMarkerStrings, '\u0000' );
    final Object[] values = new Object[] { message, location, severity, true, m_editorID, pointPos, pointProperty, resMarkerSerialized == "" ? null : resMarkerSerialized, //$NON-NLS-1$
        m_profileFeatureID, m_station };

    marker.setAttributes( USED_ATTRIBUTES, values );

    m_markers.add( marker );
  }
}