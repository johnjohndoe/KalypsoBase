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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * @author Gernot Belger
 */
public class WspmWaterBody extends Feature_Impl implements IWspmConstants, IProfileSelectionProvider
{
  public final static QName QNAME = new QName( NS_WSPM, "WaterBody" ); //$NON-NLS-1$

  public final static QName QNAME_WSP_FIX_MEMBER = new QName( NS_WSPM, "waterlevelFixationMember" ); //$NON-NLS-1$

  public static final QName QNAME_REACH_MEMBER = new QName( NS_WSPM, "reachMember" ); //$NON-NLS-1$

  public static final QName QNAME_PROP_PROFILEMEMBER = new QName( NS_WSPM, "profileMember" ); //$NON-NLS-1$

  private static final QName QNAME_REFNR = new QName( NS_WSPM, "refNr" );//$NON-NLS-1$

  public WspmWaterBody( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  public IProfileFeature createNewProfile( )
  {
    try
    {
      final Feature profile = FeatureHelper.addFeature( this, QNAME_PROP_PROFILEMEMBER, IProfileFeature.QN_TYPE );
      if( profile instanceof IProfileFeature )
        return (IProfileFeature) profile;
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
    setProperty( QNAME_REFNR, refNr );
  }

  public String getRefNr( )
  {
    return getProperty( QNAME_REFNR, String.class ); //$NON-NLS-1$
  }

  public void setDirectionUpstreams( final boolean directionIsUpstream )
  {
    setProperty( new QName( NS_WSPM, "isDirectionUpstream" ), new Boolean( directionIsUpstream ) ); //$NON-NLS-1$
  }

  public Feature createRunOffEvent( ) throws GMLSchemaException
  {
    return FeatureHelper.addFeature( this, new QName( NS_WSPM, "runOffEventMember" ), new QName( NS_WSPMRUNOFF, "RunOffEvent" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public Feature createWspFix( ) throws GMLSchemaException
  {
    return FeatureHelper.addFeature( this, QNAME_WSP_FIX_MEMBER, new QName( NS_WSPMRUNOFF, "WaterlevelFixation" ) ); //$NON-NLS-1$
  }

  public List< ? > getWspFixations( )
  {
    return getProperty( QNAME_WSP_FIX_MEMBER, List.class );
  }

  public boolean isDirectionUpstreams( )
  {
    return getProperty( new QName( NS_WSPM, "isDirectionUpstream" ), Boolean.class ); //$NON-NLS-1$
  }

  public WspmReach[] getReaches( )
  {
    final FeatureList reaches = (FeatureList) getProperty( QNAME_REACH_MEMBER );
    final List<WspmReach> reachList = new ArrayList<WspmReach>( reaches.size() );
    for( final Object object : reaches )
    {
      final Feature f = (Feature) object;
      reachList.add( (WspmReach) f );
    }

    return reachList.toArray( new WspmReach[reachList.size()] );
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IProfileSelectionProvider#getSelectedProfiles(org.kalypso.gmlschema.property.relation.IRelationType)
   */
  @Override
  public IProfileFeature[] getSelectedProfiles( final IRelationType selectionHint )
  {
    final List<IProfileFeature> profile = new ArrayList<IProfileFeature>();
    if( selectionHint != null && selectionHint.isList() )
    {
      final FeatureList property = (FeatureList) getProperty( selectionHint );
      for( final Object object : property )
      {
        if( object instanceof IProfileFeature )
          profile.add( (IProfileFeature) object );
      }
    }

    return profile.toArray( new IProfileFeature[profile.size()] );
  }

  public WspmProject getProject( )
  {
    final Feature owner = getOwner();
    if( owner instanceof WspmProject )
      return (WspmProject) owner;

    return null;
  }
}
