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
package org.kalypso.model.wspm.core.profil;

import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.TupleResult;

/**
 * @author kimwerner
 */
public interface IProfilPointPropertyProvider
{
  /**
   * creates a new empty IProfil object - builders are registered over extension points
   */
  IProfil createProfil( );

  /**
   * create a new IProfil object, takes given Observation as profile data
   */
  IProfil createProfil( TupleResult observation );

  /**
   * FIXME: not used any more: either remove or use
   * 
   * @return all PointPropertyIds handled by this provider NOTE: the natural order in this Array is the initial
   *         columnsort used in the tableview
   */
  String[] getPointProperties( );

  IComponent getPointProperty( String propertyId );

  /**
   * @return true if the provider supports the propertyId
   */
  boolean providesPointProperty( final String property );

  /**
   * Check, if a given {@link TupleResult} is valid according to this profile type.
   */
  void checkComponents( TupleResult result );

  /**
   * markers maybe handled different in special cases (p.e. UI)
   */
  boolean isMarker( final String markerID );
  /**
   * Returns the default value for the given propertyID
   */
  Object getDefaultValue( final String propertyID );
}
