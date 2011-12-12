/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.model.wspm.ui.view.chart.layer.wsp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.math.DoubleRange;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilPointMarker;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.model.wspm.core.util.WaterlevelIntersectionWorker;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.preferences.Preferences;
import org.kalypso.observation.result.IRecord;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Calculates the {@link WaterlevelRenderData} for a given profile and water levels.
 *
 * @author Gernot Belger
 */
public class WaterlevelRenderWorker
{
  private final GeometryFactory m_factory = new GeometryFactory();

  private final Collection<WaterlevelRenderSegment> m_result = new ArrayList<WaterlevelRenderSegment>();

  private final IProfil m_profile;

  private final double m_value;

  private final int m_widthComponent;


  public WaterlevelRenderWorker( final IProfil profile, final double value )
  {
    m_profile = profile;
    m_value = value;
    m_widthComponent = m_profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_BREITE );
  }

  public WaterlevelRenderSegment[] getResult( )
  {
    return m_result.toArray( new WaterlevelRenderSegment[m_result.size()] );
  }

  public IStatus execute( )
  {
    if( Double.isNaN( m_value ) )
      return Status.OK_STATUS;

    final DoubleRange restriction = getRestriction();

    final WaterlevelIntersectionWorker worker = new WaterlevelIntersectionWorker( m_profile, m_value );
    worker.execute();
    final LineSegment[] segments = worker.getSegments();
    for( final LineSegment lineSegment : segments )
    {
      try
      {
        final WaterlevelRenderSegment segment = buildSegment( lineSegment, restriction );
        if( segment != null )
          m_result.add( segment );
      }
      catch( final Exception e )
      {
        e.printStackTrace();
        return new Status( IStatus.ERROR, KalypsoModelWspmUIPlugin.ID, "Failed to build segment", e ); //$NON-NLS-1$
      }
    }

    return Status.OK_STATUS;
  }

  private WaterlevelRenderSegment buildSegment( final LineSegment segment, final DoubleRange restriction ) throws Exception
  {
    final DoubleRange segmentRange = new DoubleRange( segment.p0.x, segment.p1.x );

    if( restriction != null && !restriction.overlapsRange( segmentRange ) )
      return null;

    final Polygon area = buildSegmentArea( segment );

    return new WaterlevelRenderSegment( segment, area );
  }

  private DoubleRange getRestriction( )
  {
    final String markerType = Preferences.getWaterlevelRestrictionMarker();
    final IProfilPointMarker[] markers = m_profile.getPointMarkerFor( markerType );

    if( ArrayUtils.isEmpty( markers ) )
      return null;

    final double leftLimit = findLimit( markers, 0 );
    final double rightLimit = findLimit( markers, markers.length - 1 );

    if( Double.isNaN( leftLimit ) || Double.isNaN( rightLimit ) )
      return null;

    return new DoubleRange( leftLimit, rightLimit );
  }

  private double findLimit( final IProfilPointMarker[] markers, final int index )
  {
    final IProfilPointMarker leftMarker = markers[index];
    final Object value = leftMarker.getPoint().getValue( m_widthComponent );
    if( value instanceof Number )
      return ((Number) value).doubleValue();

    return Double.NaN;
  }

  private Polygon buildSegmentArea( final LineSegment segment ) throws Exception
  {
    final double x1 = segment.p0.x;
    final double x2 = segment.p1.x;

    // FIXME: not yet perfekt: we need to cut at d1/d2 instead of finding the nearest point.
    // So this is nt yet to be used to show final the area, but it final can be used final to calculate the final area.

    final IRecord point1 = ProfilUtil.findNearestPoint( m_profile, x1 );
    final IRecord point2 = ProfilUtil.findNearestPoint( m_profile, x2 );

    if( point1 == null || point2 == null )
      return null;

    final int index1 = m_profile.indexOfPoint( point1 );
    final int index2 = m_profile.indexOfPoint( point2 );

    final Double[] widths = ProfilUtil.getValuesFor( m_profile, IWspmConstants.POINT_PROPERTY_BREITE, Double.class );
    final Double[] heights = ProfilUtil.getValuesFor( m_profile, IWspmConstants.POINT_PROPERTY_HOEHE, Double.class );

    final List<Coordinate> area = new ArrayList<Coordinate>( index2 - index1 + 3 );

    for( int i = index1; i <= index2; i++ )
    {
      final Double width = widths[i];
      final Double height = heights[i];

      if( width != null && height != null )
      {
        area.add( new Coordinate( width, height ) );
      }
    }

    area.add( new Coordinate( x2, m_value ) );
    area.add( new Coordinate( x1, m_value ) );
    area.add( area.get( 0 ) );

    final LinearRing shell = m_factory.createLinearRing( area.toArray( new Coordinate[area.size()] ) );

    return m_factory.createPolygon( shell, null );
  }
}