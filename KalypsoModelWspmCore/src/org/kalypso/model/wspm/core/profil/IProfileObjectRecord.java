/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Bj�rnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universit�t Hamburg-Harburg, Institut f�r Wasserbau, Hamburg, Germany
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
package org.kalypso.model.wspm.core.profil;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author Holger Albert
 */
public interface IProfileObjectRecord
{
  String getId( );

  void setId( String id );

  String getComment( );

  void setComment( String comment );

  Double getBreite( );

  void setBreite( Double breite );

  Double getHoehe( );

  void setHoehe( Double hoehe );

  Double getRechtswert( );

  void setRechtswert( Double rechtswert );

  Double getHochwert( );

  void setHochwert( Double hochwert );

  String getCode( );

  void setCode( String code );

  /**
   * Returns location of the record in the width/height coordinate system.
   */
  Coordinate getWidthHeightLocation( );

  /**
   * Returns location of the record in the geographic coordinate system (easting/northing)
   */
  Coordinate getGeoLocation( );
}