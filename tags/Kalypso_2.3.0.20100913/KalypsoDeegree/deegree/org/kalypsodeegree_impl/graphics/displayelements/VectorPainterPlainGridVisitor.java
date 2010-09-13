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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypsodeegree.graphics.displayelements.LineStringDisplayElement;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.Stroke;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.IPlainGridVisitor;
import org.kalypsodeegree_impl.graphics.displayelements.strokearrow.StrokeArrowHelper;
import org.kalypsodeegree_impl.graphics.displayelements.strokearrow.StrokeArrowPainter;
import org.kalypsodeegree_impl.graphics.sld.PolygonSymbolizer_Impl;
import org.kalypsodeegree_impl.graphics.sld.Stroke_Impl;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author ig
 */
public class VectorPainterPlainGridVisitor<T extends GM_Curve> implements IPlainGridVisitor<T>
{
  private final static double DOUBLE_STROKE_WIDTH = 3;

  private final Graphics m_gc;

  private final GeoTransform m_projection;

  private final Feature m_feature;

  private Map<String, Object> m_cssParams = null;

  public VectorPainterPlainGridVisitor( final Graphics gc, final GeoTransform projection, final Feature pFeature )
  {
    m_gc = gc;
    m_projection = projection;
    m_feature = pFeature;
  }

  @SuppressWarnings("unchecked")
  public VectorPainterPlainGridVisitor( final Graphics gc, final GeoTransform projection, final Feature pFeature, final Map pMapCssParams )
  {
    m_gc = gc;
    m_projection = projection;
    m_feature = pFeature;
    try
    {
      m_cssParams = pMapCssParams;
    }
    catch( Exception e )
    {

    }
  }

  /**
   * @see org.kalypsodeegree.model.geometry.IPlainGridVisitor#visit(org.kalypsodeegree.model.geometry.GM_MultiCurve,
   *      int)
   */
  @Override
  public boolean visit( T actualGridNode )
  {
    try
    {
      paintVector( actualGridNode );
    }
    catch( Exception e )
    {
      return false;
    }
    return true;
  }

  private void paintVector( final T pCurve )
  {
    paintLinePart( pCurve );
    paintArrowPart( pCurve );
  }

  private void paintArrowPart( final T pCurve )
  {
    final PolygonSymbolizer symb = new PolygonSymbolizer_Impl();
    final Stroke stroke = new Stroke_Impl( new HashMap<Object, Object>(), null, null );
    stroke.setWidth( DOUBLE_STROKE_WIDTH );
    Color strokeColor = new Color( 0, 0, 0 );
    stroke.setStroke( strokeColor );
    symb.setStroke( stroke );

    if( m_cssParams == null )
      fillCssDefaultsMap( pCurve );

    Double lDoubleVectorSize = getVectorSize( pCurve );

    m_cssParams.put( StrokeArrowHelper.STROKE_ARROW_SIZE, StyleFactory.createCssParameter( StrokeArrowHelper.STROKE_ARROW_SIZE, lDoubleVectorSize ) );

    StrokeArrowPainter lPainter = new StrokeArrowPainter( m_cssParams, m_projection, symb.getUom() );

//    java.awt.Stroke oldStroke = ( ( Graphics2D ) m_gc ).getStroke(); 
//    Color oldColor = ( ( Graphics2D ) m_gc ).getColor();
    //prepare graphics 
    ( ( Graphics2D ) m_gc ).setColor( strokeColor );
//    ( ( Graphics2D ) m_gc ).setStroke( (java.awt.Stroke) stroke );
    
    lPainter.paint( (Graphics2D) m_gc, pCurve, null );

    //restore graphics 
//    ( ( Graphics2D ) m_gc ).setColor( oldColor );
//    ( ( Graphics2D ) m_gc ).setStroke( oldStroke );
  }

  private void fillCssDefaultsMap( final GM_Curve pCurve )
  {
    Double lDoubleVectorSize = getVectorSize( pCurve );
    m_cssParams = new HashMap<String, Object>();
    m_cssParams.put( StrokeArrowHelper.STROKE_ARROW_TYPE, StyleFactory.createCssParameter( StrokeArrowHelper.STROKE_ARROW_TYPE, "line" ) ); //$NON-NLS-1$
    m_cssParams.put( StrokeArrowHelper.STROKE_ARROW_WIDGET, StyleFactory.createCssParameter( StrokeArrowHelper.STROKE_ARROW_WIDGET, "open" ) ); //$NON-NLS-1$
    m_cssParams.put( StrokeArrowHelper.STROKE_ARROW_ALIGNMENT, StyleFactory.createCssParameter( StrokeArrowHelper.STROKE_ARROW_ALIGNMENT, "end" ) ); //$NON-NLS-1$
    m_cssParams.put( StrokeArrowHelper.STROKE_ARROW_SIZE, StyleFactory.createCssParameter( StrokeArrowHelper.STROKE_ARROW_SIZE, lDoubleVectorSize ) );
    m_cssParams.put( StrokeArrowHelper.STROKE_WIDTH, StyleFactory.createCssParameter( StrokeArrowHelper.STROKE_WIDTH, new Double( DOUBLE_STROKE_WIDTH ) ) );

  }

  private Double getVectorSize( final GM_Curve curve )
  {
    double lX = m_projection.getDestPoint( curve.getEndPoint().getPosition() ).getX() - m_projection.getDestPoint( curve.getStartPoint().getPosition() ).getX();
    double lY = m_projection.getDestPoint( curve.getEndPoint().getPosition() ).getY() - m_projection.getDestPoint( curve.getStartPoint().getPosition() ).getY();
    return Math.sqrt( lX * lX + lY * lY );
  }

  private void paintLinePart( final T pCurve )
  {
    LineStringDisplayElement lLineElement = new LineStringDisplayElement_Impl( m_feature, new GM_Curve[] { pCurve } );
    try
    {
      lLineElement.paint( m_gc, m_projection, new NullProgressMonitor() );
    }
    catch( CoreException e )
    {
      e.printStackTrace();
    }
  }
}
