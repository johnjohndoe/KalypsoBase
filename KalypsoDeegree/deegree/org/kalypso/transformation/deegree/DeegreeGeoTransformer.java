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
package org.kalypso.transformation.deegree;

import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * This geo transformer uses deegree.
 * 
 * @author Holger Albert
 */
public class DeegreeGeoTransformer implements IGeoTransformer
{
  /**
   * The coordinate system, into which the transformations should be done.
   */
  private final String m_targetCRS;

  /**
   * The constructor.
   * 
   * @param targetCRS
   *          The coordinate system, into which the transformations should be done.
   */
  public DeegreeGeoTransformer( final String targetCRS )
  {
    m_targetCRS = targetCRS;
  }

  /**
   * @see org.kalypso.transformation.transformer.IGeoTransformer#getTarget()
   */
  @Override
  public String getTarget( )
  {
    return m_targetCRS;
  }

  /**
   * @see org.kalypso.transformation.transformer.IGeoTransformer#transform(org.kalypsodeegree.model.geometry.GM_Position,
   *      java.lang.String)
   */
  @Override
  public GM_Position transform( final GM_Position position, final String sourceCRS ) throws Exception
  {
    if( position == null )
      return null;

    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( m_targetCRS ) )
      return position;

    return DeegreeTransformUtilities.transform( position, sourceCRS, m_targetCRS );
  }


  /**
   * @see org.kalypso.transformation.transformer.IGeoTransformer#transform(org.kalypsodeegree.model.geometry.GM_Object)
   */
  @Override
  public GM_Object transform( final GM_Object geometry ) throws Exception
  {
    if( geometry == null )
      return null;

    final String sourceCRS = geometry.getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( m_targetCRS ) )
      return geometry;

    if( geometry instanceof GM_Point )
      return DeegreeTransformUtilities.transform( ((GM_Point) geometry), sourceCRS, m_targetCRS );
    else
      return geometry.transform( m_targetCRS );
  }

  /**
   * @see org.kalypso.transformation.transformer.IGeoTransformer#transform(org.kalypsodeegree.model.geometry.GM_Envelope)
   */
  @Override
  public GM_Envelope transform( final GM_Envelope envelope ) throws Exception
  {
    if( envelope == null )
      return null;

    final String sourceCRS = envelope.getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( m_targetCRS ) )
      return envelope;

    final GM_Position min = transform( envelope.getMin(), sourceCRS );
    final GM_Position max = transform( envelope.getMax(), sourceCRS );

    return GeometryFactory.createGM_Envelope( min, max, m_targetCRS );
  }

  /**
   * @see org.kalypso.transformation.transformer.IGeoTransformer#transform(org.kalypsodeegree.model.geometry.GM_SurfacePatch)
   */
  @Override
  public GM_SurfacePatch transform( final GM_SurfacePatch surfacePatch ) throws Exception
  {
    if( surfacePatch == null )
      return null;

    final String sourceCRS = surfacePatch.getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( m_targetCRS ) )
      return surfacePatch;

    return (GM_SurfacePatch) surfacePatch.transform( m_targetCRS );
  }
}