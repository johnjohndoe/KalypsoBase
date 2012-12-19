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
package org.kalypsodeegree_impl.model.geometry;

import org.kalypsodeegree.model.geometry.GM_Boundary;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Point;

/**
 * Constants for gm objects. The instantiation of geometries which extends {@link GM_Object_Impl} triggers the static
 * initialization of {@link GM_Object_Impl} the first time the class is used. In this static constants however, there
 * was also a {@link GM_Point_Impl}. This could lead to a lock (not sure this is a real dead lock). But inspecting the
 * traces show, that it happens during the static initialization and that all other instances of {@link GM_Object_Impl}
 * (if there were any) are waiting for it to finish.
 * 
 * @author Hogler Albert
 */
public class GM_Constants
{
  /**
   * Placeholder if the boundary cannot be created.
   */
  public static final GM_Boundary EMPTY_BOUNDARY = new GM_CurveBoundary_Impl( null, null, null );

  /**
   * Placeholder if the centroid cannot be created.
   */
  public static final GM_Point EMPTY_CENTROID = new GM_Point_Impl( Double.NaN, Double.NaN, null );

  /**
   * Placeholder if the envelope cannot be created.
   */
  public static final GM_Envelope EMPTY_ENVELOPE = new GM_Envelope_Impl( Double.NaN, Double.NaN, Double.NaN, Double.NaN, null );

  /**
   * The constructor.
   */
  private GM_Constants( )
  {
  }
}