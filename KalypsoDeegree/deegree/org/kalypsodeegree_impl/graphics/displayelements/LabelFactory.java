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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.displayelements.Label;
import org.kalypsodeegree.graphics.displayelements.LabelDisplayElement;
import org.kalypsodeegree.graphics.sld.Halo;
import org.kalypsodeegree.graphics.sld.LabelPlacement;
import org.kalypsodeegree.graphics.sld.LinePlacement;
import org.kalypsodeegree.graphics.sld.LinePlacement.PlacementType;
import org.kalypsodeegree.graphics.sld.PointPlacement;
import org.kalypsodeegree.graphics.sld.Stroke;
import org.kalypsodeegree.graphics.sld.TextSymbolizer;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * Does the labeling, i.e. creates (screen) <tt>Label</tt> representations from <tt>LabelDisplayElement</tt>s.
 * <p>
 * Different geometry-types (of the LabelDisplayElement) imply different strategies concerning the way the
 * <tt>Labels</tt> are generated.
 * <p>
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */
public class LabelFactory
{
  private static final int MAX_CURVE_LABELS_COUNT = 100;

  private static final double DEFAULT_ROTATION = 0.0;

  private static final double[] DEFAULT_POINT_DISPLACEMENT = new double[] { 0.0, 0.0 };

  private static final double[] DEFAULT_POINT_ANCHOR = new double[] { 0.0, 0.5 };

  private static final double[] DEFAULT_LINE_ANCHOR = new double[] { 0.5, 0.5 };

  private static final Label[] EMPTY_LABELS = new Label[0];

  private final LabelDisplayElement m_element;

  private final GeoTransform m_projection;

  private final Graphics2D m_graphics;

  private final TextSymbolizer m_symbolizer;

  public LabelFactory( final LabelDisplayElement element, final GeoTransform projection, final Graphics2D g )
  {
    m_element = element;
    m_projection = projection;
    m_graphics = g;
    m_symbolizer = m_element.getSymbolizer();
  }

  /**
   * Generates <tt>Label</tt> -representations for a given <tt>LabelDisplayElement</tt>.
   */
  public Label[] createLabels( ) throws GM_Exception
  {
    try
    {
      final Feature feature = m_element.getFeature();

      final String caption = m_element.getLabel().evaluate( feature );
      // sanity check: empty labels are ignored
      if( StringUtils.isBlank( caption ) )
        return EMPTY_LABELS;

      // gather font information
      final org.kalypsodeegree.graphics.sld.Font sldFont = m_symbolizer.getFont();
      final java.awt.Font font = new java.awt.Font( sldFont.getFamily( feature ), sldFont.getStyle( feature ) | sldFont.getWeight( feature ), sldFont.getSize( feature ) );
      m_graphics.setFont( font );

      final Color color = sldFont.getColor( feature );

      final FontRenderContext frc = m_graphics.getFontRenderContext();

      // bugstories...
      // I got the following error in the next line:
      // # An unexpected error has been detected by HotSpot Virtual Machine:
      // # [...]
      // # Problematic frame:
      // # C [libfontmanager.so+0x2ecd5]
      //
      // while running kalypso on linux, kubuntu-Dapper.
      // The error was caused by some buggy fonts in Dapper (Rekha-normal and aakar-MagNet ).
      // Work-around is to remove the toxic fonts by removing the package ttf-gujarati-fonts from the distribution.
      // this error was not easy to locate, so do not remove this notice !
      // ( v.doemming@tuhh.de )

      final Rectangle2D bounds = font.getStringBounds( caption, frc );
      final Dimension size = bounds.getBounds().getSize();
      final LineMetrics metrics = font.getLineMetrics( caption, frc );

      final GM_Object[] geometries = m_element.getGeometry();
      final List<Label> allLabels = new ArrayList<Label>();
      for( final GM_Object geometry : geometries )
      {
        final Label[] labels = createLabels( feature, caption, geometry, font, color, metrics, size );
        allLabels.addAll( Arrays.asList( labels ) );
      }

      return allLabels.toArray( new Label[allLabels.size()] );
    }
    catch( final FilterEvaluationException e )
    {
      // if properties are unknown to features, this should be ignored!
      return EMPTY_LABELS;
    }
  }

  private Label[] createLabels( final Feature feature, final String caption, final GM_Object geometry, final java.awt.Font font, final Color color, final LineMetrics metrics, final Dimension bounds ) throws FilterEvaluationException, GM_Exception
  {
    if( geometry instanceof GM_Point )
      return createPointLabels( feature, caption, (GM_Point) geometry, font, color, metrics, bounds );

    if( geometry instanceof GM_Curve || geometry instanceof GM_MultiCurve )
      return createCurveLabels( feature, caption, geometry, font, color, metrics, bounds );

    if( geometry instanceof GM_Surface || geometry instanceof GM_MultiSurface )
      return createSurfaceLabels( feature, caption, geometry, font, color, metrics, bounds );

    throw new IllegalArgumentException( "LabelFactory does not implement generation of Labels from geometries of type: '" + geometry.getClass().getName() + "'!" );
  }

  private Label[] createPointLabels( final Feature feature, final String caption, final GM_Point geometry, final java.awt.Font font, final Color color, final LineMetrics metrics, final Dimension bounds ) throws FilterEvaluationException
  {
    // get screen coordinates
    final int[] coords = LabelUtils.calcScreenCoordinates( m_projection, geometry );
    final int x = coords[0];
    final int y = coords[1];

    final PointPlacement pPlacement = getPointPlacement();

    final double rotation = getRotation( pPlacement, feature );
    final double[] anchorPoint = getAnchor( pPlacement, feature, DEFAULT_POINT_ANCHOR );
    final double[] displacement = getDisplacement( pPlacement, feature );

    final Label label = createLabel( caption, font, color, metrics, feature, x, y, bounds, Math.toRadians( rotation ), anchorPoint, displacement );
    return new Label[] { label };
  }

  private Label[] createCurveLabels( final Feature feature, final String caption, final GM_Object geometry, final Font font, final Color color, final LineMetrics metrics, final Dimension bounds ) throws GM_Exception, FilterEvaluationException
  {
    final GM_Surface< ? extends GM_SurfacePatch> screenSurface = GeometryFactory.createGM_Surface( m_projection.getSourceRect(), null );
    final GM_Object intersection = screenSurface.intersection( geometry );

    if( intersection instanceof GM_Curve )
      return createCurveLabels( feature, caption, (GM_Curve) intersection, font, color, metrics, bounds );

    if( intersection instanceof GM_MultiCurve )
      return createMultiCurveLabels( feature, caption, (GM_MultiCurve) intersection, font, color, metrics, bounds );

    return EMPTY_LABELS;
  }

  private Label[] createSurfaceLabels( final Feature feature, final String caption, final GM_Object geometry, final Font font, final Color color, final LineMetrics metrics, final Dimension bounds ) throws GM_Exception, FilterEvaluationException
  {
    // use placement information from SLD
    final PointPlacement pPlacement = getPointPlacement();

    final GM_Object geometryForLabel = adjustSurfaceGeometry( pPlacement, geometry );
    // get screen coordinates
    final int[] coords = LabelUtils.calcScreenCoordinates( m_projection, geometryForLabel );
    final int x = coords[0];
    final int y = coords[1];

    final double rotation = getRotation( pPlacement, feature );
    final double[] anchorPoint = getAnchor( pPlacement, feature, DEFAULT_LINE_ANCHOR );
    final double[] displacement = getDisplacement( pPlacement, feature );

    final Label label = createLabel( caption, font, color, metrics, feature, x, y, bounds, Math.toRadians( rotation ), anchorPoint, displacement );
    return new Label[] { label };
  }

  private double[] getAnchor( final PointPlacement pPlacement, final Feature feature, final double[] defaultValue ) throws FilterEvaluationException
  {
    if( pPlacement == null )
      return defaultValue;

    return pPlacement.getAnchorPoint( feature );
  }

  private double[] getDisplacement( final PointPlacement pPlacement, final Feature feature ) throws FilterEvaluationException
  {
    if( pPlacement == null )
      return DEFAULT_POINT_DISPLACEMENT;

    return pPlacement.getDisplacement( feature );
  }

  private double getRotation( final PointPlacement pPlacement, final Feature feature ) throws FilterEvaluationException
  {
    if( pPlacement == null )
      return DEFAULT_ROTATION;

    return pPlacement.getRotation( feature );
  }

  private GM_Object adjustSurfaceGeometry( final PointPlacement pPlacement, final GM_Object geometry ) throws GM_Exception
  {
    if( pPlacement == null )
      return geometry;

    if( !pPlacement.isAuto() )
      return geometry;

    // TODO: we need to do this only onlce
    final GM_Surface< ? extends GM_SurfacePatch> screenSurface = GeometryFactory.createGM_Surface( m_projection.getSourceRect(), null );

    final GM_Object intersection = screenSurface.intersection( geometry );
    if( intersection == null )
      return geometry;

    return intersection;
  }

  private Label createLabel( final String caption, final Font font, final Color color, final LineMetrics metrics, final Feature feature, final int x, final int y, final Dimension bounds, final double rotation, final double[] anchorPoint, final double[] displacement )
  {
    final Halo halo = m_symbolizer.getHalo();

    return new RotatedLabel( caption, font, color, metrics, feature, halo, x, y, bounds, rotation, anchorPoint, displacement );
  }

  private PointPlacement getPointPlacement( )
  {
    final LabelPlacement lPlacement = m_symbolizer.getLabelPlacement();
    if( lPlacement == null )
      return null;

    return lPlacement.getPointPlacement();
  }

  private LinePlacement getLinePlacement( )
  {
    final LabelPlacement lPlacement = m_symbolizer.getLabelPlacement();
    if( lPlacement == null )
      return null;

    return lPlacement.getLinePlacement();
  }

  /**
   * Determines positions on the given <tt>GM_MultiCurve</tt> where a caption could be drawn. For each of this positons,
   * three candidates are produced; one on the line, one above of it and one below.
   * <p>
   * 
   * @param multiCurve
   * @param element
   * @param g
   * @param projection
   * @return ArrayList containing Arrays of Label-objects
   * @throws FilterEvaluationException
   */
  private Label[] createMultiCurveLabels( final Feature feature, final String caption, final GM_MultiCurve multiCurve, final Font font, final Color color, final LineMetrics metrics, final Dimension bounds ) throws FilterEvaluationException, GM_Exception
  {
    final List<Label> placements = new ArrayList<Label>();
    for( int i = 0; i < multiCurve.getSize(); i++ )
    {
      final GM_Curve curve = multiCurve.getCurveAt( i );
      final Label[] labels = createCurveLabels( feature, caption, curve, font, color, metrics, bounds );
      placements.addAll( Arrays.asList( labels ) );
    }

    return placements.toArray( new Label[placements.size()] );
  }

  /**
   * Determines positions on the given <tt>GM_Curve</tt> where a caption could be drawn. For each of this positons,
   * three candidates are produced; one on the line, one above of it and one below.
   * 
   * @param curve
   * @param element
   * @param g
   * @param projection
   * @return ArrayList containing Arrays of Label-objects
   * @throws FilterEvaluationException
   */
  private Label[] createCurveLabels( final Feature feature, final String caption, final GM_Curve curve, final Font font, final Color color, final LineMetrics metrics, final Dimension size ) throws FilterEvaluationException, GM_Exception
  {
    final double radius = getLineRadius( feature );

    // determine the placement type and parameters from the TextSymbolizer
    final double perpendicularOffset;
    final PlacementType placementType;
    final double lineWidth;
    final int gap;

    final LinePlacement linePlacement = getLinePlacement();
    if( linePlacement != null )
    {
      placementType = linePlacement.getPlacementType( feature );
      perpendicularOffset = linePlacement.getPerpendicularOffset( feature );
      lineWidth = linePlacement.getLineWidth( feature );
      gap = linePlacement.getGap( feature );
    }
    else
    {
      placementType = PlacementType.absolute;
      perpendicularOffset = 0.0;
      lineWidth = 3.0;
      gap = 6;
    }

    // get screen coordinates of the line
    final int[][] pos = LabelUtils.calcScreenCoordinates( m_projection, curve );

    // get width & height of the caption
    final double labelWidth = size.getWidth() + 2 * radius;
    final double labelHeight = size.getHeight() + 2 * radius;

    // ideal distance from the line
    final double delta = labelHeight / 2.0 + lineWidth / 2.0;

    // walk along the linestring and "collect" possible placement positions
    final int w = (int) labelWidth;
    int lastX = pos[0][0];
    int lastY = pos[1][0];
    int boxStartX = lastX;
    int boxStartY = lastY;

    final List<Label> labels = new ArrayList<Label>( MAX_CURVE_LABELS_COUNT );
    final List<int[]> eCandidates = new ArrayList<int[]>( MAX_CURVE_LABELS_COUNT );

    int i = 0;
    for( int kk = 0; kk < MAX_CURVE_LABELS_COUNT && kk < pos[2][0]; kk++ )
    {
      final int screenX = pos[0][i];
      final int screenY = pos[1][i];

      // segment found where endpoint of box should be located?
      if( getDistance( boxStartX, boxStartY, screenX, screenY ) >= w )
      {
        final int[] boxStart = new int[] { boxStartX, boxStartY };
        final int[] last = new int[] { lastX, lastY };
        final int[] current = new int[] { screenX, screenY };

        final int[] p = findPointWithDistance( boxStart, last, current, w );

        lastX = p[0];
        lastY = p[1];

        int boxEndX = p[0];
        int boxEndY = p[1];

        // does the linesegment run from right to left?
        if( boxEndX < boxStartX )
        {
          final int helpX = boxStartX;
          final int helpY = boxStartY;

          boxStartX = boxEndX;
          boxStartY = boxEndY;

          boxEndX = helpX;
          boxEndY = helpY;
        }

        final double rotation = getRotation( boxStartX, boxStartY, boxEndX, boxEndY );
        final double[] deviation = calcDeviation( new int[] { boxStartX, boxStartY }, new int[] { boxEndX, boxEndY }, eCandidates );

        final double[] displacement = calculateDisplacement( placementType, 0.0, perpendicularOffset, delta, deviation );
        final double[] anchorPoint = DEFAULT_POINT_ANCHOR;

        final Label label = createLabel( caption, font, color, metrics, feature, (int) (boxStartX + radius), boxStartY, size, rotation, anchorPoint, displacement );
        labels.add( label );

        boxStartX = lastX;
        boxStartY = lastY;

        eCandidates.clear();
      }
      else
      {
        eCandidates.add( new int[] { screenX, screenY } );
        lastX = screenX;
        lastY = screenY;
        i++;
      }
    }

    // pick lists of boxes on the linestring
    // FIXME: strange: is the gap really the number of labels? shouldn't it be the minimum gap in pixels betwen to
    // labels?
    final List<Label> pick = new ArrayList<Label>( 100 );

    final int n = labels.size();
    for( int j = n / 2; j < n; j += (gap + 1) )
      pick.add( labels.get( j ) );

    for( int j = n / 2 - (gap + 1); j > 0; j -= (gap + 1) )
      pick.add( labels.get( j ) );

    return pick.toArray( new Label[pick.size()] );
  }

  private double getLineRadius( final Feature feature ) throws FilterEvaluationException
  {
    final Halo halo = m_symbolizer.getHalo();

    if( halo == null )
      return 0;

    final double radius = halo.getRadius( feature );
    final Stroke haloStroke = halo.getStroke();
    if( haloStroke == null )
      return radius;

    return radius + haloStroke.getWidth( feature );
  }

  private static double[] calculateDisplacement( final PlacementType placementType, final double ww, final double perpendicularOffset, final double delta, final double[] deviation )
  {
    switch( placementType )
    {
      case absolute:
        return new double[] { ww, perpendicularOffset };

      case above:
        return new double[] { ww, delta + deviation[0] };

      case below:
      case auto:
        return new double[] { ww, -delta - deviation[1] };

      case center:
      default:
        return new double[] { ww, 0.0 };
    }
  }

  /**
   * Calculates the maximum deviation that points on a linestring have to the ideal line between the starting point and
   * the end point.
   * <p>
   * The ideal line is thought to be running from left to right, the left deviation value generally is above the line,
   * the right value is below.
   * <p>
   * 
   * @param start
   *          starting point of the linestring
   * @param end
   *          end point of the linestring
   * @param points
   *          points in between
   */
  private static double[] calcDeviation( final int[] start, final int[] end, final List<int[]> points )
  {
    // extreme deviation to the left
    double d1 = 0.0;
    // extreme deviation to the right
    double d2 = 0.0;

    // eventually swap start and end point
    Assert.isTrue( end[0] >= start[0] );

    if( start[0] == end[0] )
    {
      // label orientation is completely vertical
      for( final int[] point : points )
      {
        final double d = point[0] - start[0];
        d1 = Math.max( d1, -d );
        d2 = Math.max( d2, d );
      }
    }
    else if( start[1] == end[1] )
    {
      // label orientation is not completely vertical
      // label orientation is completely horizontal
      for( final int[] point : points )
      {
        final double d = point[1] - start[1];
        d1 = Math.max( d1, -d );
        d2 = Math.max( d2, d );
      }
    }
    else
    {
      // label orientation is not completely horizontal
      for( final int[] point : points )
      {
        final double u = ((double) end[1] - (double) start[1]) / ((double) end[0] - (double) start[0]);
        final double x = (u * u * start[0] - u * ((double) start[1] - (double) point[1]) + point[0]) / (1.0 + u * u);
        final double y = (x - start[0]) * u + start[1];
        final double d = getDistance( point, new int[] { (int) (x + 0.5), (int) (y + 0.5) } );
        if( y >= point[1] )
          d1 = Math.max( d1, d );
        else
          d2 = Math.max( d2, d );
      }
    }
    return new double[] { d1, d2 };
  }

  /**
   * Finds a point on the line between p1 and p2 that has a certain distance from point p0 (provided that there is such
   * a point).
   * <p>
   * 
   * @param p0
   *          point that is used as reference point for the distance
   * @param p1
   *          starting point of the line
   * @param p2
   *          end point of the line
   * @param d
   *          distance
   */
  private static int[] findPointWithDistance( final int[] p0, final int[] p1, final int[] p2, final int d )
  {
    final double x0 = p0[0];
    final double y0 = p0[1];
    final double x1 = p1[0];
    final double y1 = p1[1];
    final double x2 = p2[0];
    final double y2 = p2[1];

    double x, y;
    if( x1 != x2 )
    {
      // line segment does not run vertical
      final double u = (y2 - y1) / (x2 - x1);
      final double p = -2 * (x0 + u * u * x1 - u * (y1 - y0)) / (u * u + 1);
      final double q = ((y1 - y0) * (y1 - y0) + u * u * x1 * x1 + x0 * x0 - 2 * u * x1 * (y1 - y0) - d * d) / (u * u + 1);
      double minX = x1;
      double maxX = x2;
      double minY = y1;
      double maxY = y2;
      if( minX > maxX )
      {
        minX = x2;
        maxX = x1;
      }
      if( minY > maxY )
      {
        minY = y2;
        maxY = y1;
      }
      x = -p / 2 - Math.sqrt( (p / 2) * (p / 2) - q );
      if( x < minX || x > maxX )
      {
        x = -p / 2 + Math.sqrt( (p / 2) * (p / 2) - q );
      }
      y = (x - x1) * u + y1;
    }
    else
    {
      // vertical line segment
      x = x1;
      double minY = y1;
      double maxY = y2;

      if( minY > maxY )
      {
        minY = y2;
        maxY = y1;
      }

      final double p = -2 * y0;
      final double q = y0 * y0 + (x1 - x0) * (x1 - x0) - d * d;

      y = -p / 2 - Math.sqrt( (p / 2) * (p / 2) - q );
      if( y < minY || y > maxY )
      {
        y = -p / 2 + Math.sqrt( (p / 2) * (p / 2) - q );
      }
    }
    return new int[] { (int) (x + 0.5), (int) (y + 0.5) };
  }

  private static double getRotation( final int x1, final int y1, final int x2, final int y2 )
  {
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    return Math.atan( dy / dx );
  }

  private static double getDistance( final int[] p1, final int[] p2 )
  {
    final double dx = p1[0] - p2[0];
    final double dy = p1[1] - p2[1];
    return Math.sqrt( dx * dx + dy * dy );
  }

  private static double getDistance( final int x1, final int y1, final int x2, final int y2 )
  {
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    return Math.sqrt( dx * dx + dy * dy );
  }
}