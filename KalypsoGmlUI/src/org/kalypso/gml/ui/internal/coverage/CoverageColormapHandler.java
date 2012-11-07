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
package org.kalypso.gml.ui.internal.coverage;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.Range;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.commons.java.lang.Doubles;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoFeatureTypeStyle;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ogc.gml.IKalypsoUserStyle;
import org.kalypsodeegree.graphics.sld.ColorMapEntry;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.PolygonColorMapEntry;
import org.kalypsodeegree.graphics.sld.RasterSymbolizer;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.graphics.sld.SldHelper;
import org.kalypsodeegree.graphics.sld.SurfacePolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.model.elevation.ElevationUtilities;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.graphics.sld.PolygonColorMap;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;
import org.kalypsodeegree_impl.graphics.sld.SurfacePolygonSymbolizer_Impl;

/**
 * Responsible for management of the color map in the coverage management widget.
 * 
 * @author Gernot Belger
 */
public class CoverageColormapHandler
{
  private final RasterSymbolizer m_rasterSymbolizer;

  private final IKalypsoFeatureTheme[] m_allCoverageThemes;

  private final Symbolizer[] m_symbolizers;

  public CoverageColormapHandler( final IKalypsoFeatureTheme coverageTheme, final IKalypsoFeatureTheme[] allCoverageThemes )
  {
    m_allCoverageThemes = allCoverageThemes;

    m_symbolizers = findSymbolizers( coverageTheme );
    m_rasterSymbolizer = findRasterSymbolizer( coverageTheme );
  }

  public RasterSymbolizer getRasterSymbolizer( )
  {
    return m_rasterSymbolizer;
  }

  /**
   * returns all {@link Symbolizer}s from the given theme.
   * 
   * @return all defined {@link Symbolizer}s
   */
  private Symbolizer[] findSymbolizers( final IKalypsoFeatureTheme coverageTheme )
  {
    final Collection<Symbolizer> allSymbolizers = new ArrayList<>();

    final IKalypsoStyle[] styles = coverageTheme.getStyles();

    for( final IKalypsoStyle style : styles )
    {
      final FeatureTypeStyle[] featureTypeStyles = findFeatureTypeStyles( style );
      for( final FeatureTypeStyle fts : featureTypeStyles )
      {
        final Rule[] rules = fts.getRules();
        for( final Rule rule : rules )
        {
          final Symbolizer[] symbolizers = rule.getSymbolizers();
          allSymbolizers.addAll( Arrays.asList( symbolizers ) );
        }
      }
    }

    return allSymbolizers.toArray( new Symbolizer[allSymbolizers.size()] );
  }

  /**
   * returns the first {@link RasterSymbolizer} found in the given array of symbolizers.
   * 
   * @param styles
   *          The styles in which the raster symbolizer is
   * @return a {@link RasterSymbolizer}
   */
  private RasterSymbolizer findRasterSymbolizer( final IKalypsoFeatureTheme coverageTheme )
  {
    final IKalypsoStyle[] styles = coverageTheme.getStyles();

    for( final IKalypsoStyle style : styles )
    {
      final FeatureTypeStyle[] featureTypeStyles = findFeatureTypeStyles( style );
      for( final FeatureTypeStyle fts : featureTypeStyles )
      {
        final Rule[] rules = fts.getRules();
        for( final Rule rule : rules )
        {
          final Symbolizer[] symbolizers = rule.getSymbolizers();
          for( final Symbolizer symbolizer : symbolizers )
          {
            if( symbolizer instanceof RasterSymbolizer )
            {
              ensurePolygonSymbolizer( rule );

              return (RasterSymbolizer)symbolizer;
            }
          }
        }
      }
    }

    return null;
  }

  /**
   * REMARK: quite hack for now: ensure, that we also have a polygon symbolizer (in the same rule as the raster
   * symbolizer).
   */
  private void ensurePolygonSymbolizer( final Rule rule )
  {
    final Symbolizer[] symbolizers = rule.getSymbolizers();
    for( final Symbolizer symbolizer : symbolizers )
    {
      if( symbolizer instanceof SurfacePolygonSymbolizer )
        return;
    }

    /* No polygon symbolizer -> create one */
    final SurfacePolygonSymbolizer newPolygonSymbolizer = new SurfacePolygonSymbolizer_Impl();

    // TODO: should use domain geometry

    // final PropertyName propertyName = new PropertyName( MultiSurfaceCoverage. );
    // final Geometry geom = StyleFactory.createGeometry( propertyName );
    // newPolygonSymbolizer.setGeometry( geom );

    rule.addSymbolizer( newPolygonSymbolizer );
  }

  private FeatureTypeStyle[] findFeatureTypeStyles( final IKalypsoStyle style )
  {
    if( style instanceof IKalypsoUserStyle )
      return ((IKalypsoUserStyle)style).getFeatureTypeStyles();

    if( style instanceof IKalypsoFeatureTypeStyle )
      return new FeatureTypeStyle[] { (FeatureTypeStyle)style };

    return new FeatureTypeStyle[] {};
  }

  /**
   * update the colorMap of the {@link RasterSymbolizer}
   */
  public void updateRasterSymbolizer( final Shell shell, final ColorMapEntry[] entries )
  {
    final TreeMap<Double, ColorMapEntry> newRasterColorMap = new TreeMap<>();

    // FIXME: move this code to the place, where the entries are created
    for( final ColorMapEntry colorMapEntry : entries )
    {
      // WHY? why do we not just ignore duplicate entries
      if( !newRasterColorMap.containsKey( new Double( colorMapEntry.getQuantity() ) ) )
        newRasterColorMap.put( new Double( colorMapEntry.getQuantity() ), colorMapEntry.clone() );
    }

    final List<PolygonColorMapEntry> newPolygonColorMap = translateColormap( newRasterColorMap );

    /* Update the symbolizers */
    for( final Symbolizer symbolizer : m_symbolizers )
    {
      if( symbolizer instanceof RasterSymbolizer )
        ((RasterSymbolizer)symbolizer).setColorMap( newRasterColorMap );
      else if( symbolizer instanceof SurfacePolygonSymbolizer )
      {
        final PolygonColorMap colorMap = ((SurfacePolygonSymbolizer)symbolizer).getColorMap();
        colorMap.replaceColorMap( newPolygonColorMap );
      }
    }

    saveStyle( shell );
  }

  private static List<PolygonColorMapEntry> translateColormap( final TreeMap<Double, ColorMapEntry> newRasterColorMap )
  {
    /* Translate raster colormap to polygon colormap with same classification */
    final List<PolygonColorMapEntry> newPolygonColorMap = new ArrayList<>( newRasterColorMap.size() );

    Entry<Double, ColorMapEntry> lastEntry = null;

    for( final Entry<Double, ColorMapEntry> entry : newRasterColorMap.entrySet() )
    {
      if( lastEntry != null )
      {
        final ColorMapEntry value = entry.getValue();
        final Color color = value.getColorAndOpacity();

        final BigDecimal fromValue = new BigDecimal( lastEntry.getValue().getQuantity() );
        final BigDecimal toValue = new BigDecimal( entry.getValue().getQuantity() );

        final PolygonColorMapEntry newEntry = StyleFactory.createPolygonColorMapEntry( color, color, fromValue, toValue );

        // REMARK: always use width '2': hides ugly artifacts in map (because fill and outline do not exactly match).
        newEntry.getStroke().setWidth( 2 );

        // TODO: use should decide if triangles are visible (no stroke, faster), or hidden (stroke with same color),
        // slower.

        newPolygonColorMap.add( newEntry );
      }

      lastEntry = entry;
    }

    return newPolygonColorMap;
  }

  private void saveStyle( final Shell shell )
  {
    /* Find all relevant styles */
    final Set<IKalypsoStyle> allStyles = new HashSet<>();

    for( final IKalypsoFeatureTheme theme : m_allCoverageThemes )
    {
      final IKalypsoStyle[] themeStyles = theme.getStyles();
      allStyles.addAll( Arrays.asList( themeStyles ) );
    }

    final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws CoreException
      {
        monitor.beginTask( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.5" ), allStyles.size() ); //$NON-NLS-1$

        for( final IKalypsoStyle style : allStyles )
        {
          style.fireStyleChanged();
          style.save( new SubProgressMonitor( monitor, 1 ) );
        }

        return Status.OK_STATUS;
      }
    };

    final IStatus result = ProgressUtilities.busyCursorWhile( operation );
    ErrorDialog.openError( shell, Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.7" ), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.8" ), result ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public void guessInitialColormap( final Shell shell, final ICoverage[] coverages )
  {
    final RasterSymbolizer symb = getRasterSymbolizer();
    if( symb == null )
      return;

    final SortedMap<Double, ColorMapEntry> colorMap = symb.getColorMap();
    if( !colorMap.isEmpty() )
      return;

    try
    {
      /* In order to show anything to the user, create a default color map, if no colors have been defined yet */
      final Range<Double> minMax = ElevationUtilities.calculateRange( coverages );
      final Double min = minMax.getMinimum();
      final Double max = minMax.getMaximum();

      // TODO: check for infinity
      if( Doubles.isNullOrInfinite( min, max ) )
        return;

      // TODO: find better step...
      final BigDecimal stepWidth = new BigDecimal( "0.1" ); //$NON-NLS-1$
      final Color fromColor = new Color( 0, 255, 0, 200 );
      final Color toColor = new Color( 255, 0, 0, 200 );

      // TODO: scale?! -> get max scale from all coverages
      final BigDecimal minDecimal = new BigDecimal( min );
      final BigDecimal maxDecimal = new BigDecimal( max );

      final ColorMapEntry[] colors = SldHelper.createColorMap( fromColor, toColor, stepWidth, minDecimal, maxDecimal, 250 );
      updateRasterSymbolizer( shell, colors );
    }
    catch( final CoreException e )
    {
      // will not happen, as we do not define a class limit in createColorMap
    }
  }
}