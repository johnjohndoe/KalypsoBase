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
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.ext.base.data.IAxisContentProvider;
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

  private int m_fixedWidth = -1;

  private ITextStyle m_tickStyle = null;

  private final Map<String, Object> m_dataMap = new HashMap<String, Object>();

  private final IAxisContentProvider m_contentProvider;

  private final AxisRendererConfig m_config;

  public OrdinalAxisRenderer( final String id, final AxisRendererConfig config, final IChartLabelRenderer tickLabelRenderer, final IChartLabelRenderer axisTitleRenderer, final IAxisContentProvider contentProvider )
  {
    m_tickLabelRenderer = tickLabelRenderer;

    if( axisTitleRenderer == null )
    {
      m_labelRenderer = new GenericChartLabelRenderer();
      m_labelRenderer.setInsets( config.labelInsets );
    }
    else
      m_labelRenderer = axisTitleRenderer;
    m_id = id;
    m_contentProvider = contentProvider;
    m_config = config;
  }

  public OrdinalAxisRenderer( final String id, final AxisRendererConfig config, final IChartLabelRenderer tickLabelRenderer, final IAxisContentProvider contentProvider )
  {
    this( id, config, tickLabelRenderer, null, contentProvider );
  }

  public OrdinalAxisRenderer( final String id, final AxisRendererConfig config, final IAxisContentProvider contentProvider )
  {
    this( id, config, null, null, contentProvider );
  }


  public OrdinalAxisRenderer( final String id, final AxisRendererConfig config )
  {
    this( id, config, null, null );
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
   * @see de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer#dispose()
   */
  @Override
  public void dispose( )
  {
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

    final ITextStyle tickLabelStyle = m_config.labelStyle;
    final ILineStyle tickLineStyle = m_config.tickLineStyle;

    for( int i = 0; i < m_contentProvider.size(); i++ )
    {
      final int y1, y2, x1, x2;

      final int textX;
      final int textY;

      getTickLabelRenderer( axis ).setLabel( m_contentProvider.getLabel( i ) );
      final boolean drawTick = true;

      final int tickPos = ticks[i].intValue();
      final int tickScreenDistance = i < m_contentProvider.size() - 1 ? ticks[i + 1].intValue() - tickPos : -1;
      // HORIZONTAL
      if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      {
        x1 = tickPos + offset;
        x2 = x1;
        y1 = startY;
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
        getTickLabelRenderer( axis ).paint( gc, new Rectangle( textX, textY, tickScreenDistance, -1 ) );
      }
    }
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
      if( i < 0 || i > m_contentProvider.size() - 1 )
        continue;
      tickRenderer.setLabel( m_contentProvider.getLabel( i ) );
      maxWidth = Math.max( getTickLabelRenderer( axis ).getSize().y, maxWidth );
    }

    return m_config.tickLength + m_config.axisLineStyle.getWidth() + maxWidth + m_labelRenderer.getSize().y;
  }

  public final Object getContent( final int index )
  {
    return m_contentProvider.getContent( index );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer#getData(java.lang.String)
   */
  @Override
  public Object getData( final String identifier )
  {
    return m_dataMap.get( identifier );
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
        m_tickLabelRenderer.setInsets( m_config.tickLabelInsets );
        m_tickLabelRenderer.setAlignment( ALIGNMENT.CENTERED_HORIZONTAL, ALIGNMENT.CENTERED_VERTICAL );
        m_tickLabelRenderer.setTextAnchor( ALIGNMENT.LEFT, ALIGNMENT.TOP );
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

    final int start = axis.getNumericRange().getMin().intValue();
    final int end = axis.getNumericRange().getMax().intValue();

    final int intervallCount = end - start + 1;

    final Number[] tickPos = new Number[m_contentProvider.size()];
    if( m_fixedWidth > 0 )
    {
      // FIXME
      final int tickDist = Math.max( m_fixedWidth, axis.getScreenHeight() / intervallCount ) - 1/* Pixel */;
      int pos = -tickDist * start;
      if( getTickLabelRenderer( axis ).getAlignmentX() == ALIGNMENT.TICK_CENTERED )
        pos -= tickDist / 2;
      for( int i = 0; i < m_contentProvider.size(); i++ )
      {
        tickPos[i] = pos;
        pos += tickDist;
      }
    }
    else
    {
      // FIXME : not tested anywhere
      Number sumWidth = 0;
      for( int i = start; i <= end; i++ )
      {
        getTickLabelRenderer( axis ).setLabel( m_contentProvider.getLabel( i ) );
        final int width = getTickLabelRenderer( axis ).getSize().x;
        tickPos[i] = sumWidth.intValue() + width / 2;
        sumWidth = sumWidth.intValue() + width;
      }
    }
    return tickPos;

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
   * @see de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer#setData(java.lang.String,
   *      java.lang.Object)
   */
  @Override
  public void setData( final String identifier, final Object data )
  {
    m_dataMap.put( identifier, data );

  }

  public void setFixedWidth( final int fixedWidth )
  {
    m_fixedWidth = fixedWidth;
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

}
