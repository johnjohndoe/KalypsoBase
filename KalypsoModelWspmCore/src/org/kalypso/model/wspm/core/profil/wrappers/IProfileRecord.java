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
package org.kalypso.model.wspm.core.profil.wrappers;

import org.apache.commons.lang3.Range;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Dirk Kuch
 */
public interface IProfileRecord extends IRecord
{
  int indexOfProperty( IComponent pointProperty );

  int indexOfProperty( String id );

  /**
   * @return true if the profile contains the property
   * @see org.kalypso.model.wspm.core.profil.IProfilPointPropertyProvider to get addable properties
   */
  boolean hasPointProperty( IComponent property );

  /**
   * @return the FIRST component with the given Id, if the profile contains the property otherwise null
   * @note the Id maybe NOT unique in the profiles TupleResult
   * @see #hasPointProperty(IComponent)
   * @see org.kalypso.model.wspm.core.profil.IProfilPointPropertyProvider to get addable properties
   */
  IComponent hasPointProperty( String propertyId );

  Double getHoehe( );

  Double getBreite( );

  Range<Double> getBreiteAsRange( );

  Double getHochwert( );

  Double getRechtswert( );

  void setBreite( final Double width );

  void setHoehe( final Double hoehe );

  void setKsValue( final Double ksValue );

  void setKstValue( final Double kstValue );

  /**
   * Returns the geo coordinate (hochwert, rechtswert) of this point. The returned coordinate is in the coordinate
   * system of the profile.
   */
  Coordinate getCoordinate( );

  Double getKsValue( );

  Double getKstValue( );

  Double getRoughnessFactor( );

  /**
   * FIXME use BigDecimal
   */
  void setBewuchsAx( final Double bewuchsAx );

  /**
   * FIXME use BigDecimal
   */
  void setBewuchsAy( final Double bewuchsAy );

  /**
   * FIXME use BigDecimal
   */
  void setBewuchsDp( final Double bewuchsDp );

  void setRechtswert( final double x );

  void setHochwert( final double y );

  /**
   * FIXME use BigDecimal
   */
  Double getBewuchsAx( );

  /**
   * FIXME use BigDecimal
   */
  Double getBewuchsAy( );

  /**
   * FIXME use BigDecimal
   */
  Double getBewuchsDp( );

  Point toPoint( );

  IProfile getProfile( );

  IProfileRecord getNextPoint( );

  IProfileRecord getPreviousPoint( );

  /**
   * @return point is part of selected range
   */
  boolean isSelected( );

  String getComment( );

  String getCode( );
}