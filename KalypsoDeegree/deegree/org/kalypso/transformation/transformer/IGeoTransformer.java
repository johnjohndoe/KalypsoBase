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
package org.kalypso.transformation.transformer;

import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_AbstractSurfacePatch;

/**
 * This interface provides functions for transforming geometries into different coordinate systems.
 *
 * @author Holger Albert
 */
public interface IGeoTransformer
{
  /**
   * This function returns the target coordinate system.
   *
   * @return The target coordinate system.
   */
  String getTarget( );

  /**
   * This function transforms the position into the target coordinate system.
   *
   * @param position
   *          The position.
   * @param sourceCRS
   *          The coordinate system of the position. Since positions does not have a coordinate system, it must be
   *          provided here.
   * @return The transformed position.
   */
  GM_Position transform( GM_Position position, String sourceCRS ) throws GeoTransformerException;

  /**
   * This function transforms the geometry into the target coordinate system.
   *
   * @param geometry
   *          The geometry.
   * @return The transformed geometry.
   */
  <T extends GM_Object> T transform( T geometry ) throws GeoTransformerException;

  /**
   * This function transforms the envelope into the target coordinate system.
   *
   * @param envelope
   *          The envelope.
   * @return The transformed envelope.
   */
  GM_Envelope transform( GM_Envelope envelope ) throws GeoTransformerException;

  /**
   * This function transforms the surface patch into the target coordinate system.
   *
   * @param surfacePatch
   *          The surface patch.
   * @return The transformed surface patch.
   */
  <T extends GM_AbstractSurfacePatch> T transform( T surfacePatch ) throws GeoTransformerException;
}