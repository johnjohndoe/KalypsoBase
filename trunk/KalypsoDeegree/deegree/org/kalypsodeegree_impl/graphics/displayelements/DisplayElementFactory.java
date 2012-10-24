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

import java.awt.Graphics;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.displayelements.DisplayElement;
import org.kalypsodeegree.graphics.displayelements.DisplayElementDecorator;
import org.kalypsodeegree.graphics.displayelements.IncompatibleGeometryTypeException;
import org.kalypsodeegree.graphics.displayelements.LabelDisplayElement;
import org.kalypsodeegree.graphics.displayelements.LineStringDisplayElement;
import org.kalypsodeegree.graphics.displayelements.PointDisplayElement;
import org.kalypsodeegree.graphics.displayelements.PolygonDisplayElement;
import org.kalypsodeegree.graphics.displayelements.RasterDisplayElement;
import org.kalypsodeegree.graphics.sld.Geometry;
import org.kalypsodeegree.graphics.sld.LineSymbolizer;
import org.kalypsodeegree.graphics.sld.PointSymbolizer;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.RasterSymbolizer;
import org.kalypsodeegree.graphics.sld.SurfaceLineSymbolizer;
import org.kalypsodeegree.graphics.sld.SurfacePolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.graphics.sld.TextSymbolizer;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.elevation.IElevationModel;
import org.kalypsodeegree.model.elevation.IElevationModelProvider;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree.model.geometry.GM_MultiPoint;
import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree.model.geometry.ISurfacePatchVisitor;
import org.kalypsodeegree.model.tin.ITin;
import org.kalypsodeegree_impl.filterencoding.PropertyName;
import org.kalypsodeegree_impl.graphics.displayelements.SurfacePatchVisitableDisplayElement.IVisitorFactory;
import org.kalypsodeegree_impl.graphics.sld.LineColorMap;
import org.kalypsodeegree_impl.graphics.sld.LineSymbolizer_Impl;
import org.kalypsodeegree_impl.graphics.sld.PointSymbolizer_Impl;
import org.kalypsodeegree_impl.graphics.sld.PolygonColorMap;
import org.kalypsodeegree_impl.graphics.sld.PolygonSymbolizer_Impl;
import org.kalypsodeegree_impl.graphics.sld.Symbolizer_Impl.UOM;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

/**
 * Factory class for the different kinds of <tt>DisplayElement</tt>s.
 * <p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */
public final class DisplayElementFactory
{
  private DisplayElementFactory( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Builds a <tt>DisplayElement</tt> using the given <tt>Feature</tt> or raster and <tt>Symbolizer</tt>.
   * <p>
   *
   * @param o
   *          contains the geometry or raster information (Feature or raster)
   * @param symbolizer
   *          contains the drawing (style) information and selects the geometry property of the <tt>Feature</tt> to be
   *          drawn
   * @throws IncompatibleGeometryTypeException
   *           if the selected geometry of the <tt>Feature</tt> is not compatible with the <tt>Symbolizer</tt>
   * @return constructed <tt>DisplayElement</tt>
   */
  public static DisplayElement buildDisplayElement( final Feature feature, final Symbolizer symbolizer, final ILabelPlacementStrategy strategy ) throws IncompatibleGeometryTypeException, FilterEvaluationException
  {
    // determine the geometry property to be used
    final Object geoObject = findGeometryObject( feature, symbolizer );

    // if the geometry property is null, do not build a DisplayElement
    // Only for RasterSymbolizer and SurfacePolygonSymbolizer's a null geoObject is allowed, as it only depends on the
    // feature
    if( geoObject == null && !(symbolizer instanceof RasterSymbolizer) && !(symbolizer instanceof SurfacePolygonSymbolizer) )
      return null;

    final DisplayElement displayElement = buildDisplayElement( feature, symbolizer, geoObject, strategy );
    if( displayElement == null )
      return null;

    // decorate the display with another get through adapation
    final DisplayElement displayElementDecorator = (DisplayElement) feature.getAdapter( DisplayElementDecorator.class );
    if( displayElementDecorator != null )
    {
      if( displayElementDecorator instanceof DisplayElementDecorator )
        ((DisplayElementDecorator) displayElementDecorator).setDecorated( displayElement );
      return displayElementDecorator;
    }

    return displayElement;
  }

  /**
   * Internally build the display element, without decoration and other stuff.
   */
  public static DisplayElement buildDisplayElement( final Feature feature, final Symbolizer symbolizer, final Object geoObject, final ILabelPlacementStrategy strategy ) throws IncompatibleGeometryTypeException
  {
    if( symbolizer instanceof PointSymbolizer )
      return buildPointDisplayElement( feature, geoObject, (PointSymbolizer) symbolizer );

    if( symbolizer instanceof LineSymbolizer )
      return buildLineStringDisplayElement( feature, geoObject, (LineSymbolizer) symbolizer );

    if( symbolizer instanceof PolygonSymbolizer )
      return buildPolygonDisplayElement( feature, geoObject, (PolygonSymbolizer) symbolizer );

    if( symbolizer instanceof TextSymbolizer )
      return buildLabelDisplayElement( feature, geoObject, (TextSymbolizer) symbolizer, strategy );

    if( symbolizer instanceof RasterSymbolizer )
      return buildRasterDisplayElement( feature, geoObject, (RasterSymbolizer) symbolizer );

    if( symbolizer instanceof SurfacePolygonSymbolizer )
      return buildSurfacePolygonDisplayElement( feature, geoObject, (SurfacePolygonSymbolizer) symbolizer );

    if( symbolizer instanceof SurfaceLineSymbolizer )
      return buildSurfaceLineDisplayElement( feature, geoObject, (SurfaceLineSymbolizer) symbolizer );

    System.out.println( "symbolizer...?: " + symbolizer );
    return null;
  }

  /**
   * Finds the geometry object for the given symbolizer and feature.
   *
   * @return Either a {@link GM_Object} or a {@link List} of {@link GM_Object}'s.
   */
  private static Object findGeometryObject( final Feature feature, final Symbolizer symbolizer ) throws FilterEvaluationException, IncompatibleGeometryTypeException
  {
    final Geometry geometry = symbolizer == null ? null : symbolizer.getGeometry();
    if( geometry == null )
      return feature.getDefaultGeometryPropertyValue();

    final PropertyName propertyName = geometry.getPropertyName();
    final Object value = propertyName.evaluate( feature );
    if( value == null || value instanceof GM_Object || value instanceof List )
    {
      return value;
    }

    final String msg = String.format( "PropertyName '%s' does not evaluate to a geometry: %s", propertyName, value );
    throw new IncompatibleGeometryTypeException( msg );
  }

  public static DisplayElement buildSurfaceLineDisplayElement( final Feature feature, final Object geoProperty, final SurfaceLineSymbolizer symbolizer ) throws IncompatibleGeometryTypeException
  {
    if( !(geoProperty instanceof GM_TriangulatedSurface) )
      throw new IncompatibleGeometryTypeException( "Tried to create a SurfaceDisplayElement from a geometry with an incompatible / unsupported type: '" + geoProperty.getClass().getName() + "'!" );

    final LineColorMap colorMap = symbolizer.getColorMap();
    final GM_TriangulatedSurface tin = (GM_TriangulatedSurface) geoProperty;

    final IVisitorFactory<GM_Triangle> visitorFactory = new SurfacePatchVisitableDisplayElement.IVisitorFactory<GM_Triangle>()
    {
      @Override
      public ISurfacePatchVisitor<GM_Triangle> createVisitor( final Graphics g, final GeoTransform projection )
      {
        final UOM uom = symbolizer.getUom();
        return new SurfacePaintIsolinesVisitor( g, projection, new ColorMapConverter( colorMap, feature, uom, projection ) );
      }
    };

    return new SurfacePatchVisitableDisplayElement<>( feature, tin, visitorFactory );
  }

  public static DisplayElement buildSurfacePolygonDisplayElement( final Feature feature, final Object geoProperty, final SurfacePolygonSymbolizer symbolizer )
  {
    final Pair<Feature, GM_TriangulatedSurface> tin = findTin( feature, geoProperty );
    if( tin == null )
      return null;

    if( true )
    {
      final PolygonColorMap colorMap = symbolizer.getColorMap();
      final ColorMapConverter colorModel = new ColorMapConverter( colorMap, feature, null, null );
      return new TriangulatedSurfacePolygonDisplayElement( tin.getLeft(), tin.getRight(), colorModel );
    }
    else
    {
      final PolygonColorMap colorMap = symbolizer.getColorMap();

      final IVisitorFactory<GM_Triangle> visitorFactory = new SurfacePatchVisitableDisplayElement.IVisitorFactory<GM_Triangle>()
      {
        @Override
        public ISurfacePatchVisitor<GM_Triangle> createVisitor( final Graphics g, final GeoTransform projection )
        {
          final UOM uom = symbolizer.getUom();
          final ColorMapConverter converter = new ColorMapConverter( colorMap, feature, uom, projection );
          return new SurfacePaintPolygonVisitor( g, converter );
        }
      };

      return new SurfacePatchVisitableDisplayElement<>( tin.getLeft(), tin.getRight(), visitorFactory );
    }
  }

  // Hacky,, is there a better way to access the real geometry...?
  private static Pair<Feature, GM_TriangulatedSurface> findTin( final Feature feature, final Object geoProperty )
  {
    if( feature instanceof IElevationModelProvider )
    {
      final IElevationModel elevationModel = ((IElevationModelProvider) feature).getElevationModel();
      if( elevationModel instanceof ITin )
      {
        final GM_TriangulatedSurface surface = ((ITin) elevationModel).getTriangulatedSurface();
        if( surface == null )
          return null;

        return Pair.of( null, surface );
      }
      else
        return null;
    }
    else
    {
      if( !(geoProperty instanceof GM_TriangulatedSurface) )
        return null;

      // Remark: This actually happens for coverage collection with grids and tins; just ignore for now
      // throw new IncompatibleGeometryTypeException(
      // "Tried to create a SurfaceDisplayElement from a geometry with an incompatible / unsupported type: '" +
      // geoProperty.getClass().getName() + "'!" );

      return Pair.of( feature, (GM_TriangulatedSurface) geoProperty );
    }
  }

  /**
   * Builds a <tt>DisplayElement</tt> using the given <tt>Feature</tt> or Raster and a default <tt>Symbolizer</tt>.
   *
   * @param o
   *          contains the geometry or raster information (Feature or raster)
   * @throws IncompatibleGeometryTypeException
   *           if the selected geometry of the <tt>Feature</tt> is not compatible with the <tt>Symbolizer</tt>
   * @return constructed <tt>DisplayElement</tt>
   */
  public static DisplayElement buildDisplayElement( final Object o ) throws IncompatibleGeometryTypeException
  {
    DisplayElement displayElement = null;

    final Feature feature = (Feature) o;
    // determine the geometry property to be used
    final GM_Object geoProperty = feature.getDefaultGeometryPropertyValue();

    // if the geometry property is null, do not build a DisplayElement
    if( geoProperty == null )
    {
      return null;
    }

    // PointSymbolizer
    if( geoProperty instanceof GM_Point || geoProperty instanceof GM_MultiPoint )
    {
      final PointSymbolizer symbolizer = new PointSymbolizer_Impl();
      displayElement = buildPointDisplayElement( feature, geoProperty, symbolizer );
    } // LineSymbolizer
    else if( geoProperty instanceof GM_Curve || geoProperty instanceof GM_MultiCurve )
    {
      final LineSymbolizer symbolizer = new LineSymbolizer_Impl();
      displayElement = buildLineStringDisplayElement( feature, geoProperty, symbolizer );
    } // PolygonSymbolizer
    else if( geoProperty instanceof GM_Polygon || geoProperty instanceof GM_MultiSurface )
    {
      final PolygonSymbolizer symbolizer = new PolygonSymbolizer_Impl();
      displayElement = buildPolygonDisplayElement( feature, geoProperty, symbolizer );
    }
    else
    {
      throw new IncompatibleGeometryTypeException( "not a valid geometry type" );
    }

    return displayElement;
  }

  /**
   * Creates a <tt>PointDisplayElement</tt> using the given geometry and style information.
   * <p>
   *
   * @param feature
   *          associated <tt>Feature<tt>
   * @param geom
   *          geometry object or list of geometries
   * @param sym
   *          style information
   * @return constructed <tt>PointDisplayElement</tt>
   */
  public static PointDisplayElement buildPointDisplayElement( final Feature feature, final Object geomOrList, final PointSymbolizer sym )
  {
    final GM_Point[] points = GeometryUtilities.findGeometries( geomOrList, GM_Point.class );
    if( ArrayUtils.isEmpty( points ) )
      return null;
    return new PointDisplayElement_Impl( feature, points, sym );
  }

  /**
   * Creates a <tt>LineStringDisplayElement</tt> using the given geometry and style information.
   * <p>
   *
   * @param feature
   *          associated <tt>Feature<tt>
   * @param geom
   *          geometry information
   * @param sym
   *          style information
   * @return constructed <tt>LineStringDisplayElement</tt>
   */
  public static LineStringDisplayElement buildLineStringDisplayElement( final Feature feature, final Object geomOrList, final LineSymbolizer sym )
  {
    final GM_Curve[] curves = GeometryUtilities.findGeometries( geomOrList, GM_Curve.class );
    if( ArrayUtils.isEmpty( curves ) )
      return null;
    return new LineStringDisplayElement_Impl( feature, curves, sym );
  }

  /**
   * Creates a <tt>PolygonDisplayElement</tt> using the given geometry and style information.
   * <p>
   *
   * @param feature
   *          associated <tt>Feature<tt>
   * @param gmObject
   *          geometry information
   * @param sym
   *          style information
   * @return constructed <tt>PolygonDisplayElement</tt>
   */
  public static PolygonDisplayElement buildPolygonDisplayElement( final Feature feature, final Object geomOrList, final PolygonSymbolizer sym )
  {
    final GM_Polygon[] surfaces = GeometryUtilities.findGeometries( geomOrList, GM_Polygon.class );
    if( ArrayUtils.isEmpty( surfaces ) )
      return null;
    return new PolygonDisplayElement_Impl( feature, surfaces, sym );
  }

  /**
   * Creates a <tt>LabelDisplayElement</tt> using the given geometry and style information.
   * <p>
   *
   * @param feature
   *          <tt>Feature</tt> to be used (necessary for evaluation of the label expression)
   * @param gmObject
   *          geometry information
   * @param sym
   *          style information
   * @throws IncompatibleGeometryTypeException
   *           if the geometry property is not a <tt>GM_Point</tt>, a <tt>GM_Surface</tt> or <tt>GM_MultiSurface</tt>
   * @return constructed <tt>PolygonDisplayElement</tt>
   */
  public static LabelDisplayElement buildLabelDisplayElement( final Feature feature, final Object geomOrList, final TextSymbolizer sym, final ILabelPlacementStrategy strategy )
  {
    final GM_Object[] objects = GeometryUtilities.findGeometries( geomOrList, GM_Object.class );
    if( ArrayUtils.isEmpty( objects ) )
      return null;

    return new LabelDisplayElement_Impl( feature, objects, sym, strategy );
  }

  /**
   * Creates a <tt>RasterDisplayElement</tt> from the submitted image. The submitted <tt>GM_Envelope</tt> holds the
   * bounding box of the imgae/raster data.
   *
   * @param feature
   *          grid coverage as feature
   * @param sym
   *          raster symbolizer
   * @return RasterDisplayElement
   */
  public static RasterDisplayElement buildRasterDisplayElement( final Feature feature, final Object geomOrList, final RasterSymbolizer sym )
  {
    // REMARK: not really necessary at the moment, as the raster symbolizer does nothing with its geometries
    // maybe it would be better to always reference the gridDomain property and give it to the symbolizer?
    final GM_Object[] objects = GeometryUtilities.findGeometries( geomOrList, GM_Object.class );
    if( ArrayUtils.isEmpty( objects ) )
      return null;

    return new RasterDisplayElement_Impl( feature, objects, sym );
  }

  public static GM_Object[] findGeometries( final Feature feature, final Symbolizer symbolizer ) throws FilterEvaluationException, IncompatibleGeometryTypeException
  {
    final Object geom = findGeometryObject( feature, symbolizer );
    return GeometryUtilities.findGeometries( geom, GM_Object.class );
  }
}