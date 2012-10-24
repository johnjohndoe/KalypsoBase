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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.displayelements.PolygonDisplayElement;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree_impl.graphics.sld.PolygonSymbolizer_Impl;
import org.kalypsodeegree_impl.graphics.sld.Symbolizer_Impl.UOM;
import org.kalypsodeegree_impl.graphics.sld.awt.FillPainter;
import org.kalypsodeegree_impl.graphics.sld.awt.SldAwtUtilities;
import org.kalypsodeegree_impl.graphics.sld.awt.StrokePainter;

/**
 * DisplayElement for handling polygons
 * <p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @version $Revision$ $Date$
 */
public class PolygonDisplayElement_Impl extends GeometryDisplayElement_Impl implements PolygonDisplayElement
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -2980154437699081214L;

  /**
   * Creates a new PolygonDisplayElement_Impl object.
   *
   * @param feature
   * @param geometry
   */
  protected PolygonDisplayElement_Impl( final Feature feature, final GM_Polygon[] surfaces )
  {
    this( feature, surfaces, new PolygonSymbolizer_Impl() );
  }

  /**
   * Creates a new PolygonDisplayElement_Impl object.
   *
   * @param feature
   * @param geometry
   * @param symbolizer
   */
  protected PolygonDisplayElement_Impl( final Feature feature, final GM_Polygon[] surfaces, final PolygonSymbolizer symbolizer )
  {
    super( feature, surfaces, symbolizer );
  }

  /**
   * renders the DisplayElement to the submitted graphic context
   */
  @Override
  public void paint( final Graphics g, final GeoTransform projection, final IProgressMonitor monitor )
  {
    final Graphics2D g2 = (Graphics2D) g;

    final Color oColor = g2.getColor();
    final Stroke oStroke = g2.getStroke();

    final PolygonSymbolizer sym = (PolygonSymbolizer) getSymbolizer();
    final org.kalypsodeegree.graphics.sld.Fill fill = sym.getFill();
    final org.kalypsodeegree.graphics.sld.Stroke stroke = sym.getStroke();
    final UOM uom = sym.getUom();

    // no fill defined -> don't draw anything
    if( fill == null )
      return;

    final GM_Polygon[] surfaces = (GM_Polygon[]) getGeometry();

    try
    {
      final Feature feature = getFeature();
      final StrokePainter strokePainter = new StrokePainter( stroke, feature, uom, projection );
      final FillPainter fillPainter = new FillPainter( fill, feature, uom, projection );

      for( final GM_Polygon element : surfaces )
      {
        SldAwtUtilities.paintSurface( g2, element, projection, fillPainter, strokePainter );
      }
    }
    catch( final FilterEvaluationException e )
    {
      e.printStackTrace();
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
    }

    g2.setColor( oColor );
    g2.setStroke( oStroke );
  }

}