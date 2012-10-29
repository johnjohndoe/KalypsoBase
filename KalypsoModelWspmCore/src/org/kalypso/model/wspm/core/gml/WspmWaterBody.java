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
package org.kalypso.model.wspm.core.gml;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree_impl.gml.binding.commons.Image;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * @author Gernot Belger
 */
public class WspmWaterBody extends Feature_Impl implements IWspmConstants, IProfileSelectionProvider
{
  public static final QName FEATURE_WSPM_WATER_BODY = new QName( NS_WSPM, "WaterBody" ); //$NON-NLS-1$

  private static final QName MEMBER_WSP_FIX = new QName( NS_WSPM, "waterlevelFixationMember" ); //$NON-NLS-1$

  public static final QName MEMBER_REACH = new QName( NS_WSPM, "reachMember" ); //$NON-NLS-1$

  public static final QName MEMBER_PROFILE = new QName( NS_WSPM, "profileMember" ); //$NON-NLS-1$

  private static final QName PROPERTY_REFNR = new QName( NS_WSPM, "refNr" );//$NON-NLS-1$

  public static final QName PROPERTY_CENTER_LINE = new QName( NS_WSPM, "centerLine" );//$NON-NLS-1$

  public static final QName MEMBER_RUNOFF = new QName( NS_WSPM, "runOffEventMember" ); //$NON-NLS-1$

  public static final QName MEMBER_IMAGE = new QName( NS_WSPM, "imageMember" ); //$NON-NLS-1$

  private final IFeatureBindingCollection<IProfileFeature> m_profileMembers;

  private final IFeatureBindingCollection<WspmFixation> m_fixations;

  private final IFeatureBindingCollection<IRunOffEvent> m_runoffEvents;

  private final IFeatureBindingCollection<WspmReach> m_reaches;

  private IFeatureBindingCollection<Image> m_images = null;

  public WspmWaterBody( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );

    m_profileMembers = new FeatureBindingCollection<>( this, IProfileFeature.class, MEMBER_PROFILE );
    m_fixations = new FeatureBindingCollection<>( this, WspmFixation.class, MEMBER_WSP_FIX );
    m_runoffEvents = new FeatureBindingCollection<>( this, IRunOffEvent.class, MEMBER_RUNOFF );
    m_reaches = new FeatureBindingCollection<>( this, WspmReach.class, MEMBER_REACH );
  }

  public IFeatureBindingCollection<IProfileFeature> getProfiles( )
  {
    return m_profileMembers;
  }

  public IProfileFeature createNewProfile( )
  {
    try
    {
      final Feature profile = FeatureHelper.addFeature( this, MEMBER_PROFILE, IProfileFeature.FEATURE_PROFILE );
      if( profile instanceof IProfileFeature )
        return (IProfileFeature)profile;
    }
    catch( final GMLSchemaException e )
    {
      // should never happen
      e.printStackTrace();
    }

    throw new IllegalStateException();
  }

  public void setRefNr( final String refNr )
  {
    setProperty( PROPERTY_REFNR, refNr );
  }

  public String getRefNr( )
  {
    return getProperty( PROPERTY_REFNR, String.class ); //$NON-NLS-1$
  }

  public void setDirectionUpstreams( final boolean directionIsUpstream )
  {
    setProperty( new QName( NS_WSPM, "isDirectionUpstream" ), Boolean.valueOf( directionIsUpstream ) ); //$NON-NLS-1$
  }

  public IFeatureBindingCollection<IRunOffEvent> getRunoffEvents( )
  {
    return m_runoffEvents;
  }

  public IFeatureBindingCollection<WspmFixation> getWspFixations( )
  {
    return m_fixations;
  }

  public boolean isDirectionUpstreams( )
  {
    return getProperty( new QName( NS_WSPM, "isDirectionUpstream" ), Boolean.class ); //$NON-NLS-1$
  }

  public IFeatureBindingCollection<WspmReach> getReaches( )
  {
    return m_reaches;
  }

  @Override
  public IProfileFeature[] getSelectedProfiles( final IRelationType selectionHint )
  {
    final List<IProfileFeature> profile = new ArrayList<>();
    {
      final FeatureList property = (FeatureList)getProperty( selectionHint );
      for( final Object object : property )
      {
        if( object instanceof IProfileFeature )
        {
          profile.add( (IProfileFeature)object );
        }
      }
    }

    return profile.toArray( new IProfileFeature[profile.size()] );
  }

  public WspmProject getProject( )
  {
    final Feature owner = getOwner();
    if( owner instanceof WspmProject )
      return (WspmProject)owner;

    return null;
  }

  public GM_Curve getCenterLine( )
  {
    return getProperty( PROPERTY_CENTER_LINE, GM_Curve.class );
  }

  public void setCenterLine( final GM_Curve centerLine )
  {
    setProperty( PROPERTY_CENTER_LINE, centerLine );
  }

  public WspmReach findReachByName( final String name )
  {
    final IFeatureBindingCollection<WspmReach> reaches = getReaches();
    for( final WspmReach reach : reaches )
    {
      if( name.equals( reach.getName() ) )
        return reach;
    }

    return null;
  }

  public Object findFixationByName( final String name )
  {
    final List<WspmFixation> fixations = getWspFixations();
    for( final WspmFixation fixation : fixations )
    {
      if( name.equals( fixation.getName() ) )
        return fixation;
    }

    return null;
  }

  public synchronized IFeatureBindingCollection<Image> getImages( )
  {
    if( m_images == null )
      m_images = new FeatureBindingCollection<>( this, Image.class, MEMBER_IMAGE, true );

    return m_images;
  }

  public Image addImage( final URI imageURI )
  {
    final IFeatureType featureType = getFeatureType();
    final IFeatureType ft = featureType.getGMLSchema().getFeatureType( Image.FEATURE_IMAGE );
    final IRelationType rt = (IRelationType)featureType.getProperty( MEMBER_IMAGE );
    final Image imageFeature = (Image)getWorkspace().createFeature( this, rt, ft );

    try
    {
      getWorkspace().addFeatureAsComposition( this, rt, -1, imageFeature );
      imageFeature.setUri( imageURI == null ? null : imageURI );
    }
    catch( final Exception e )
    {
      KalypsoModelWspmCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return imageFeature;
  }
}