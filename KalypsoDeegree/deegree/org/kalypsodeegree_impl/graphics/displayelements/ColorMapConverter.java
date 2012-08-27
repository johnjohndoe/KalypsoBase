/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.graphics.displayelements;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.Fill;
import org.kalypsodeegree.graphics.sld.LineColorMapEntry;
import org.kalypsodeegree.graphics.sld.PolygonColorMapEntry;
import org.kalypsodeegree.graphics.sld.Stroke;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.graphics.sld.LineColorMap;
import org.kalypsodeegree_impl.graphics.sld.PolygonColorMap;
import org.kalypsodeegree_impl.graphics.sld.Symbolizer_Impl.UOM;

import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.intervalrtree.SortedPackedIntervalRTree;

/**
 * Converts a LineColorMap or a PolygonColorMap into an ElevationColorModel TODO: zwei klassen draus machen, f�r
 * isolines und isofl�chen!
 * 
 * @author Thomas Jung
 */
public class ColorMapConverter implements IElevationColorModel
{
  private final List<ElevationColorEntry> m_entries = new LinkedList<ElevationColorEntry>();

  private final SortedPackedIntervalRTree m_entryIndex = new SortedPackedIntervalRTree();

  public ColorMapConverter( final LineColorMap colorMap, final Feature feature, final UOM uom, final GeoTransform projection )
  {
    try
    {
      convertLineColorMap( colorMap, feature, uom, projection );
    }
    catch( final FilterEvaluationException e )
    {
      e.printStackTrace();
    }
  }

  public ColorMapConverter( final PolygonColorMap colorMap, final Feature feature, final UOM uom, final GeoTransform projection )
  {
    try
    {
      convertPolygonColorMap( colorMap, feature, uom, projection );
    }
    catch( final FilterEvaluationException e )
    {
      e.printStackTrace();
    }
  }

  private void convertLineColorMap( final LineColorMap colorMap, final Feature feature, final UOM uom, final GeoTransform projection ) throws FilterEvaluationException
  {
    final LineColorMapEntry[] entries = colorMap.getColorMap();
    for( final LineColorMapEntry element : entries )
    {
      final Stroke stroke = element.getStroke();
      final String label = element.getLabel( feature );
      final double quantity = element.getQuantity( feature );
      final ElevationColorEntry data = new ElevationColorEntry( stroke, feature, uom, projection, label, quantity );
      m_entries.add( data );
    }
  }

  private void convertPolygonColorMap( final PolygonColorMap colorMap, final Feature feature, final UOM uom, final GeoTransform projection ) throws FilterEvaluationException
  {
    final PolygonColorMapEntry[] entries = colorMap.getColorMap();
    for( final PolygonColorMapEntry element : entries )
    {
      final Fill fill = element.getFill();

      // REMARK: NOT using a stroke for two reasons:
      // - paint speed-up by ~40%
      // - looks better in most cases (before, always some white pixels where visible)
      final Stroke stroke = null;
      // final Stroke stroke = element.getStroke();

      final String label = element.getLabel( feature );
      final double from = element.getFrom( feature );
      final double to = element.getTo( feature );
      final ElevationColorEntry data = new ElevationColorEntry( fill, stroke, feature, uom, projection, label, from, to );
      m_entries.add( data );
      m_entryIndex.insert( from, to, data );
    }
  }

  @Override
  public Color getColor( final double elevation )
  {
    final ElevationColorEntry colorEntry = getColorEntry( elevation );
    if( colorEntry == null )
      return null;

    return colorEntry.getPolygonPainter().getFillColor();
  }

  @Override
  public ElevationColorEntry getColorEntry( final double elevation )
  {
    // REMARK: necessary! query to empty index leads to endless loop.
    if( m_entries.size() == 0 )
      return null;

    final ElevationColorEntry[] result = new ElevationColorEntry[1];

    final ItemVisitor visitor = new ItemVisitor()
    {
      @Override
      public void visitItem( final Object item )
      {
        result[0] = (ElevationColorEntry) item;
      }
    };
    m_entryIndex.query( elevation, elevation, visitor );

    return result[0];
  }

  @Override
  public void setElevationMinMax( final double min, final double max )
  {
  }

  @Override
  public double[] getElevationMinMax( )
  {
    return null;
  }

  @Override
  public void setProjection( final GeoTransform projection )
  {
  }

  @Override
  public ElevationColorEntry[] getColorEntries( )
  {
    return m_entries.toArray( new ElevationColorEntry[m_entries.size()] );
  }
}