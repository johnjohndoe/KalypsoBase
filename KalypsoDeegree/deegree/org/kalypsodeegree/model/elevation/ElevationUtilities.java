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
package org.kalypsodeegree.model.elevation;

import org.apache.commons.lang3.Range;
import org.kalypso.grid.GeoGridUtilities;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridCoverage;

/**
 * Helper class for {@link IElevationModel}s.
 *
 * @author Gernot Belger
 */
public final class ElevationUtilities
{
  /**
   * Wraps an coverage into an {@link IElevationModel}. The caller is responsible for disposing the returned
   * {@link IElevationModel}.
   */
  public static IElevationModel toElevationModel( final ICoverage coverage )
  {
    if( coverage instanceof IElevationModelProvider )
      return ((IElevationModelProvider) coverage).getElevationModel();

    if( coverage instanceof RectifiedGridCoverage )
      return GeoGridUtilities.toGrid( coverage );

    throw new IllegalArgumentException();
  }

  /**
   * This function returns the envelope of the elevation model in the target coordinate system.
   *
   * @param elevationModel
   *          The elevation model.
   * @param targetSrs
   *          The target coordinate system.
   * @return The envelope of the elevation model in the target coordinate system.
   */
  public static GM_Envelope getEnvelope( final IElevationModel elevationModel, final String targetSrs ) throws Exception
  {
    final GM_Envelope boundingBox = elevationModel.getBoundingBox();
    if( boundingBox == null )
      return null;

    final IGeoTransformer geoTransformer = GeoTransformerFactory.getGeoTransformer( targetSrs );

    return geoTransformer.transform( boundingBox );
  }

  public static Range<Double> calculateRange( final ICoverage[] coverages )
  {
    // get min / max
    double min = Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;

    for( final ICoverage coverage : coverages )
    {
      try
      {
        final IElevationModel model = toElevationModel( coverage );
        min = Math.min( min, model.getMinElevation() );
        max = Math.max( max, model.getMaxElevation() );

        // dispose it
        model.dispose();
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
    }

    return Range.between( min, max );
  }
}