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
package org.kalypso.model.wspm.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;

/**
 * Access to wspm ui preferences.
 *
 * @author Gernot Belger
 */
public final class WspmUiPreferences
{
  /**
   * If set to the id of a marker type, water levels are only painted in the profile view if they overlap the
   * corresponding flow area.<br/>
   * If the type is set to {@link org.kalypso.model.wspm.tuhh.core.IWspmTuhhConstants#MARKER_TYP_DURCHSTROEMTE}, only
   * water levels within the model boundary are painted.<br/>
   * Not set by default to maintain backwards compatibility.
   */
  public static final String WATERLEVEL_RESTRICTION_MARKER = "waterlevelRestrictionMarker"; //$NON-NLS-1$

  private WspmUiPreferences( )
  {
    throw new UnsupportedOperationException();
  }

  public static String getWaterlevelRestrictionMarker( )
  {
    final IPreferenceStore store = KalypsoModelWspmUIPlugin.getDefault().getPreferenceStore();
    return store.getString( WATERLEVEL_RESTRICTION_MARKER );
  }

  public static void setWaterlevelRestrictionMarker( final String markerType )
  {
    final IPreferenceStore store = KalypsoModelWspmUIPlugin.getDefault().getPreferenceStore();
    store.setValue( WATERLEVEL_RESTRICTION_MARKER, markerType );
  }
}