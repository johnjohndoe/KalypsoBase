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
package org.kalypsodeegree.graphics.sld;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree_impl.graphics.sld.PolygonColorMapEntry_Impl;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author Gernot Belger
 */
public final class PolygonSymbolizerUtils
{
  public static List<PolygonColorMapEntry> createColorMap( final PolygonColorMapEntry fromEntry, final PolygonColorMapEntry toEntry, final BigDecimal stepWidth, final BigDecimal minValue, final BigDecimal maxValue, final boolean useStroke )
  {
    final List<PolygonColorMapEntry> colorMapList = new LinkedList<PolygonColorMapEntry>();

    try
    {
      final Color fromPolygonColor = fromEntry.getFill().getFill( null );
      final Color toPolygonColor = toEntry.getFill().getFill( null );
      final double polygonOpacityFrom = fromEntry.getFill().getOpacity( null );
      final double polygonOpacityTo = toEntry.getFill().getOpacity( null );

      // Stroke
      final Color fromLineColor = fromEntry.getStroke().getStroke( null );
      final Color toLineColor = toEntry.getStroke().getStroke( null );
      final double lineOpacityFrom = fromEntry.getStroke().getOpacity( null );
      final double lineOpacityTo = toEntry.getStroke().getOpacity( null );
      final double lineWidthFrom = fromEntry.getStroke().getWidth( null );
      final double lineWidthTo = toEntry.getStroke().getWidth( null );

      // get rounded values below min and above max (rounded by first decimal)
      final BigDecimal minDecimal = minValue.setScale( 2, BigDecimal.ROUND_FLOOR );
      final BigDecimal maxDecimal = maxValue.setScale( 2, BigDecimal.ROUND_CEILING );

      final BigDecimal polygonStepWidth = stepWidth.setScale( 2, BigDecimal.ROUND_FLOOR );
      final int numOfClasses = (maxDecimal.subtract( minDecimal ).divide( polygonStepWidth, BigDecimal.ROUND_HALF_UP )).intValue();

      for( int currentClass = 0; currentClass < numOfClasses; currentClass++ )
      {
        final BigDecimal fromValue = new BigDecimal( minDecimal.doubleValue() + currentClass * polygonStepWidth.doubleValue() ).setScale( 2, BigDecimal.ROUND_HALF_UP );
        final BigDecimal toValue = new BigDecimal( minDecimal.doubleValue() + (currentClass + 1) * polygonStepWidth.doubleValue() ).setScale( 2, BigDecimal.ROUND_HALF_UP );

        // Stroke
        Color lineColor;
        if( fromLineColor == toLineColor )
          lineColor = fromLineColor;
        else
          lineColor = SldHelper.interpolateColor( fromLineColor, toLineColor, currentClass, numOfClasses );

        // Fill
        final Color polygonColor = SldHelper.interpolateColor( fromPolygonColor, toPolygonColor, currentClass, numOfClasses );
        final double polygonOpacity = SldHelper.interpolate( polygonOpacityFrom, polygonOpacityTo, currentClass, numOfClasses );
        final Fill fill = StyleFactory.createFill( polygonColor, polygonOpacity );

        // Stroke
        BigDecimal lineOpacity;
        BigDecimal lineWidth;

        if( useStroke == false )
        {
          lineOpacity = new BigDecimal( 0 ).setScale( 2, BigDecimal.ROUND_HALF_UP );
          lineWidth = new BigDecimal( 0.01 ).setScale( 2, BigDecimal.ROUND_HALF_UP );
        }
        else
        {
          lineOpacity = new BigDecimal( SldHelper.interpolate( lineOpacityFrom, lineOpacityTo, currentClass, numOfClasses ) ).setScale( 2, BigDecimal.ROUND_HALF_UP );
          lineWidth = new BigDecimal( SldHelper.interpolate( lineWidthFrom, lineWidthTo, currentClass, numOfClasses ) ).setScale( 2, BigDecimal.ROUND_HALF_UP );
          if( lineWidth == new BigDecimal( 0 ) )
            lineWidth = new BigDecimal( 0.01 ).setScale( 2, BigDecimal.ROUND_HALF_UP );
        }

        final Stroke stroke = StyleFactory.createStroke( lineColor, lineWidth.doubleValue(), lineOpacity.doubleValue() );
        stroke.setLineCap( java.awt.BasicStroke.CAP_ROUND );
        stroke.setLineJoin( java.awt.BasicStroke.JOIN_ROUND );
        final String labelStr = String.format( "%.2f - %.2f", fromValue, toValue ); //$NON-NLS-1$

        final ParameterValueType label = StyleFactory.createParameterValueType( labelStr );
        final ParameterValueType from = StyleFactory.createParameterValueType( fromValue.doubleValue() );
        final ParameterValueType to = StyleFactory.createParameterValueType( toValue.doubleValue() );

        final PolygonColorMapEntry colorMapEntry = new PolygonColorMapEntry_Impl( fill, stroke, label, from, to );

        colorMapList.add( colorMapEntry );
      }
    }
    catch( final FilterEvaluationException e )
    {
      e.printStackTrace();
    }

    return colorMapList;
  }
}
