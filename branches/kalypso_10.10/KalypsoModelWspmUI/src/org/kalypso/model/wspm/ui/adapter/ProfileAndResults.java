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
package org.kalypso.model.wspm.ui.adapter;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.IProfileFeatureProvider;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypsodeegree.model.feature.Feature;

/**
 * Helper class that receives a feature form a selection.
 * 
 * @author Gernot Belger
 */
public class ProfileAndResults
{
  private final IProfileFeature m_profile;

  private final Object m_result;

  public static ProfileAndResults search( final IFeatureSelection selection )
  {
    final EasyFeatureWrapper[] features = selection.getAllFeatures();

    try
    {
      for( final EasyFeatureWrapper eft : features )
      {
        final Feature feature = eft.getFeature();

        if( feature != null )
        {
          final IProfileFeature profileMember = findProfile( feature );
          if( profileMember != null )
          {
            final Object result = findResultNode( feature );

            // HACK: If type not set, force it to be the tuhh-profile. We need this, as tuhh-profile are created via
            // the gml-tree which knows nothing about profiles... Everyone else should create profile programatically
            // and directly set the prefered type.
            if( profileMember.getProfileType() == null )
              profileMember.setProfileType( "org.kalypso.model.wspm.tuhh.profiletype" ); //$NON-NLS-1$

            return new ProfileAndResults( profileMember, result );
          }
        }
      }
    }
    catch( final Exception e )
    {
      final KalypsoModelWspmUIPlugin wspmPlugin = KalypsoModelWspmUIPlugin.getDefault();
      wspmPlugin.getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return null;
  }

  /**
   * Finds a {@link WspmProfile} from a given feature using the org.kalypso.model.wspm.core.profileFeatureProvider
   * extension-point.
   */
  private static IProfileFeature findProfile( final Feature feature )
  {
    final IProfileFeatureProvider[] profileFeatureProvider = KalypsoModelWspmCoreExtensions.getProfileFeatureProvider();
    for( final IProfileFeatureProvider provider : profileFeatureProvider )
    {
      final IProfileFeature profile = provider.getProfile( feature );
      if( profile != null )
        return profile;
    }

    return null;
  }

  private static Object findResultNode( final Feature feature )
  {
    final IProfileFeatureProvider[] profileFeatureProvider = KalypsoModelWspmCoreExtensions.getProfileFeatureProvider();
    for( final IProfileFeatureProvider provider : profileFeatureProvider )
    {
      // FIXME: do not return an simple object
      final Object result = provider.getResult( feature );
      if( result != null )
        return result;
    }

    return null;
  }

  public ProfileAndResults( final IProfileFeature profile, final Object result )
  {
    m_profile = profile;
    m_result = result;
  }

  public IProfileFeature getProfile( )
  {
    return m_profile;
  }

  public Object getResult( )
  {
    return m_result;
  }

}
