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
package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;
import de.openali.odysseus.chart.framework.util.img.GenericChartLabelRenderer;
import de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer;

/**
 * @author kimwerner
 */
public class OrdinalAxisRenderer implements IAxisRenderer
{
  private IChartLabelRenderer m_tickLabelRenderer;

  private final IChartLabelRenderer m_labelRenderer;

  private final String m_id;

  private final boolean m_fixedWidth = true;

  private ITextStyle m_tickStyle = null;

  private final Map<String, Object> m_dataMap = new HashMap<String, Object>();

  private final String[] m_labels;

  private final AxisRendererConfig m_config;

  public OrdinalAxisRenderer( final String id, final AxisRendererConfig config, final String[] labels )
  {

    this( id, config, null, null, labels );

  }

  public OrdinalAxisRenderer( final String id, final AxisRendererConfig config, final IChartLabelRenderer tickLabelRenderer, final String[] labels )
  {

    this( id, config, tickLabelRenderer, null, labels );

  }

  public OrdinalAxisRenderer( final String id, final AxisRendererConfig config, final IChartLabelRenderer tickLabelRenderer, final IChartLabelRenderer axisTitleRenderer, final String[] labels )
  {
    m_tickLabelRenderer = tickLabelRenderer;
    m_labelRenderer = axisTitleRenderer == null ? new GenericChartLabelRenderer() : axisTitleRenderer;
    m_id = id;
    m_labels = labels;
    m_config = config;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer#dispose()
   */
  @Override
  public void dispose( )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer#getAxisWidth(de.openali.odysseus.chart.framework.model.mapper.IAxis)
   */
  @Override
  public int getAxisWidth( final IAxis axis )
  {
    final IDataRange<Number> range = axis.getNumericRange();

    if( range.getMin() == null || range.getMax() == null )
      return 0;
    int maxWidth = 0;
    final IChartLabelRenderer tickRenderer = getTickLabelRenderer( axis );
    for( int i = range.getMin().intValue(); i <= range.getMax().intValue(); i++ )
    {
      if( i < 0 || m_labels.length < i - 1 )
        continue;
      tickRenderer.setLabel( m_labels[i] );
      maxWidth = Math.max( getTickLabelRenderer( axis ).getSize().y, maxWidth );
    }

    return m_config.tickLength + m_config.axisLineStyle.getWidth() + maxWidth + m_labelRenderer.getSize().y;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer#getId()
   */
  @Override
  public String getId( )
  {
    return m_id;
  }

  private IChartLabelRenderer getTickLabelRenderer( final IAxis axis )
  {
    if( m_tickLabelRenderer == null )
    {
      m_tickLabelRenderer = new GenericChartLabelRenderer();
      if( m_config != null )
      {
        m_tickLabelRenderer.setInsets( m_config.labelInsets );
        m_tickLabelRenderer.setAlignment( m_config.labelPosition, ALIGNMENT.TOP );
        m_tickLabelRenderer.setTextAnchor( m_config.labelPosition, ALIGNMENT.TOP );
        m_tickLabelRenderer.setTextStyle( m_config.labelStyle );
      }
      if( axis != null )
      {
        m_tickLabelRenderer.setRotation( axis.getPosition().getOrientation() == ORIENTATION.VERTICAL ? 90 : 0 );
      }
    }
    return m_tickLabelRenderer;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer#getTicks(de.openali.odysseus.chart.framework.model.mapper.IAxis,
   *      org.eclipse.swt.graphics.GC)
   */
  @Override
  public Number[] getTicks( final IAxis axis, final GC gc )
  {
    if( axis.getNumericRange().getMin() == null || axis.getNumericRange().getMax() == null )
      return new Number[] {};
    final int start = Math.max( 0, axis.getNumericRange().getMin().intValue() );
    final int end = Math.min( m_labels.length - 1, axis.getNumericRange().getMax().intValue() );

    final Number[] labelPosLeft = new Number[end - start + 1];
    if( m_fixedWidth )
    {
      final int tickDist = axis.getScreenHeight() / (end - start + 1);
      int pos = tickDist/2;
      for( int i = start; i <= end; i++ )
      {
        labelPosLeft[i] =pos;
        pos += tickDist;
      }
    }
    else
    {

      Number sumWidth = 0;
      for( int i = start; i <= end; i++ )
      {
        getTickLabelRenderer( axis ).setLabel( m_labels[i] );
        final int width = getTickLabelRenderer( axis ).getSize().x;
        labelPosLeft[i] = sumWidth.intValue() + width / 2;
        sumWidth = sumWidth.intValue() + width;
      }
// final int spacer = (axis.getScreenHeight() - sumWidth.intValue()) / (end - start + 1);
// final int fixedWidth = spacer < 0 ? axis.getScreenHeight() / (end - start + 1) : -1;
//
// sumWidth = 0;
// for( Number width : labelPosLeft )
// {
// final int midPos = fixedWidth > 0 ? fixedWidth / 2 : width.intValue() / 2;
// width = sumWidth.intValue() + midPos;
// sumWidth = midPos + spacer < 0 ? 0 : spacer;
// }
    }
    return labelPosLeft;

  }

  public ITextStyle getTickStyle( )
  {
    return m_tickStyle == null ? StyleUtils.getDefaultTextStyle() : m_tickStyle;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer#paint(org.eclipse.swt.graphics.GC,
   *      de.openali.odysseus.chart.framework.model.mapper.IAxis, org.eclipse.swt.graphics.Rectangle)
   */
  @Override
  public void paint( final GC gc, final IAxis axis, final Rectangle screenArea )
  {
    if( (screenArea.width > 0) && (screenArea.height > 0) && axis.isVisible() )
    {

      gc.setBackground( gc.getDevice().getSystemColor( SWT.COLOR_GRAY ) );
      // gc.fillRectangle( screen );

      // draw axis line
      final int[] coords = createAxisSegment( axis, screenArea );
      assert (coords != null) && (coords.length == 4);
      final int tmpWidth = gc.getLineWidth();
      gc.setLineWidth( m_config.tickLineStyle.getWidth() );
      gc.drawLine( coords[0], coords[1], coords[2], coords[3] );
      gc.setLineWidth( tmpWidth );

      int offset = 0;
      if( axis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
      {
        if( axis.getDirection().equals( DIRECTION.POSITIVE ) )
          offset = coords[0];
        else
          offset = coords[2];
      }
      else if( axis.getDirection().equals( DIRECTION.POSITIVE ) )
        offset = coords[3];
      else
        offset = coords[1];

      final Number[] ticks = getTicks( axis, gc );

      drawTicks( gc, axis, coords[0], coords[1], ticks, offset );
    }

  }

  /**
   * draws the Axis ticks into the given GC
   */
  private void drawTicks( final GC gc, final IAxis axis, final int startX, final int startY, final Number[] ticks, final int offset )
  {

    if( (gc == null) || (axis == null) || (ticks == null) )
      return;

    final int tickLength = m_config.tickLength;
    final Insets tickLabelInsets = m_config.labelInsets;

    final IDataRange<Number> range = axis.getNumericRange();
    if( range.getMin() == null || range.getMax() == null )
      return;
    final double numericMin = range.getMin().doubleValue();
    final double numericMax = range.getMax().doubleValue();
    final int axisMin = axis.numericToScreen( numericMin );
    final int axisMax = axis.numericToScreen( numericMax );
    final int screenMin = Math.min( axisMin, axisMax );
    final int screenMax = Math.max( axisMin, axisMax );

    final ITextStyle tickLabelStyle = m_config.labelStyle;
    final ILineStyle tickLineStyle = m_config.tickLineStyle;
    final int tickScreenDistance = (screenMax - screenMin) / ticks.length == 1 ? 1 : (ticks.length - 1);

    for( int i = 0; i < ticks.length; i++ )
    {
      final int y1, y2, x1, x2, tickPos;

      final int textX;
      final int textY;

      getTickLabelRenderer( axis ).setLabel( m_labels[i] );
      final boolean drawTick = true;

      tickPos = ticks[i].intValue();

// if( i < ticks.length - 1 )
// tickScreenDistance = axis.numericToScreen( ticks[i + 1] ) - tickPos;
      // final Point labelSize = getTickLabelRenderer( axis ).getSize();//getTextExtent( gc, label, tickLabelStyle );
      // HORIZONTAL
      if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      {
        x1 = tickPos + offset;
        x2 = x1;
        y1 = startY;
        // textX = tickPos- labelSize.x / 2 + offset;
        textX = tickPos + offset;
        // BOTTOM
        if( axis.getPosition() == POSITION.BOTTOM )
        {
          y2 = y1 + tickLength;
          textY = y2 + tickLabelInsets.top;
        }
        // TOP
        else
        {
          y2 = y1 - tickLength;
          textY = y2 - tickLabelInsets.top;
        }

      }
      // VERTICAL
      else
      {
        x1 = startX;
        y1 = tickPos + offset;
        y2 = y1;
        // textY = tickPos - labelSize.y / 2 + offset;
        textY = y1;

        // LEFT
        if( axis.getPosition() == POSITION.LEFT )
        {
          x2 = x1 - tickLength;
          textX = x2 - tickLabelInsets.top;
        }
        // RIGHT
        else
        {
          x2 = x1 + tickLength;
          textX = x2 + tickLabelInsets.top;
        }
      }

      tickLineStyle.apply( gc );
      gc.drawLine( x1, y1, x2, y2 );
      if( drawTick )
      {
        getTickLabelRenderer( axis ).paint( gc, new Point( textX, textY ) );
      }
    }
  }

  /**
   * @return Array of 4 int-Values: startX, startY, endX, endY - where startX/Y are the start coordinates for the axis
   *         line and endX/Y its end coordinates within the given Rectangle
   */
  private int[] createAxisSegment( final IAxis axis, final Rectangle screen )
  {
    int startX;
    int startY;
    int endX;
    int endY;

    final int gap = m_config.gap;

    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      startX = screen.x;
      endX = screen.x + screen.width;

      if( axis.getPosition() == POSITION.BOTTOM )
        startY = screen.y + gap + 1;
      else
        startY = screen.y + screen.height - 1 - gap;
      endY = startY;

      if( axis.getDirection() == DIRECTION.NEGATIVE )
      {
        final int tmp = startX;
        startX = endX;
        endX = tmp;
      }
    }
    else
    {
      startY = screen.y;
      endY = screen.y + screen.height;

      if( axis.getPosition() == POSITION.RIGHT )
        startX = screen.x + gap + 1;
      else
        startX = screen.x + screen.width - 1 - gap;
      endX = startX;

      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        final int tmp = startY;
        startY = endY;
        endY = tmp;
      }
    }

    return new int[] { startX, startY, endX, endY };
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer#setLabelStyle(de.openali.odysseus.chart.framework.model.style.ITextStyle)
   */
  @Override
  public void setLabelStyle( final ITextStyle style )
  {
    m_labelRenderer.setTextStyle( style );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer#setTickLabelStyle(de.openali.odysseus.chart.framework.model.style.ITextStyle)
   */
  @Override
  public void setTickLabelStyle( final ITextStyle style )
  {
    m_tickStyle = style;
    if( m_tickLabelRenderer != null )
      m_tickLabelRenderer.setTextStyle( style );
  }

  public void setTickStyle( final ITextStyle tickStyle )
  {
    m_tickStyle = tickStyle;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer#setData(java.lang.String,
   *      java.lang.Object)
   */
  @Override
  public void setData( final String identifier, final Object data )
  {
    m_dataMap.put( identifier, data );

  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer#getData(java.lang.String)
   */
  @Override
  public Object getData( final String identifier )
  {
    return m_dataMap.get( identifier );
  }

}
