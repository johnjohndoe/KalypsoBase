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

import java.math.BigDecimal;
import java.net.URL;

import javax.xml.namespace.QName;

import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.binding.IFeatureWrapper2;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree_impl.gml.binding.commons.Image;

/**
 * @author Dirk Kuch
 */
public interface IProfileFeature extends Feature, IFeatureWrapper2
{
  QName QN_PROFILE = new QName( IWspmConstants.NS_WSPMPROF, "Profile" ); //$NON-NLS-1$

  QName QN_PROPERTY_STATION = new QName( IWspmConstants.NS_WSPMPROF, "station" ); //$NON-NLS-1$

  QName QN_PROPERTY_TYPE = new QName( IWspmConstants.NS_WSPMPROF, "type" ); //$NON-NLS-1$

  QName QN_PROPERTY_LINE = new QName( IWspmConstants.NS_WSPMPROF, "profileLocation" ); //$NON-NLS-1$

  QName QN_PROPERTY_SRS = new QName( IWspmConstants.NS_WSPMPROF, "srsName" ); //$NON-NLS-1$

  QName QN_PROPERTY_OBS_MEMBERS = new QName( IWspmConstants.NS_WSPMPROF, "member" ); //$NON-NLS-1$

  QName QN_PROPERTY_IMAGE_MEMBER = new QName( IWspmConstants.NS_WSPMPROF, "imageMember" ); //$NON-NLS-1$

  /**
   * The scale (i.e. fraction digits) for station values.
   * 
   * @see BigDecimal
   */
  int STATION_SCALE = 4;

  /**
   * @Deprecated Use {@link #getBigStation()} instead.
   */
  double getStation( );

  /**
   * @deprecated Use {@link #setBigStation(BigDecimal)} instead.
   */
  @Deprecated
  void setStation( double station );

  BigDecimal getBigStation( );

  void setBigStation( BigDecimal bigStation );

  IProfil getProfil( );

  /**
   * Returns the profile geometry.<br>
   * IMPORTANT: this geometry is (in contrast to {@link #getSrsName()} always in the Kalypso-Coorindate-System.
   */
  GM_Curve getLine( );

  String getSrsName( );

  void setSrsName( String srsName );

  WspmWaterBody getWater( );

  String getProfileType( );

  void setProfileType( String type );

  Image addImage( URL photoURL );
}
