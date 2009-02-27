package org.kalypso.chart.ext.base.axisrenderer;

import java.awt.Insets;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;
import org.eclipse.swt.graphics.GC;

/**
 * @author alibu
 */
public abstract class AbstractAxisRenderer<T_logical> implements IAxisRenderer<T_logical>
{

  private final int m_tickLength;

  private final int m_lineWidth;

  private final int m_gap;

  private final Insets m_labelInsets;

  private final Insets m_tickLabelInsets;

  private final FontData m_fontDataLabel;

  private final FontData m_fontDataTick;

  private final RGB m_rgbForeground;

  private final RGB m_rgbBackground;

  private final String m_id;

  private final Map<FontData, Font> m_fontMap = new HashMap<FontData, Font>();

  /**
   * Hashmap to store arbitrary key value pairs
   */
  private final HashMap<String, Object> m_data = new HashMap<String, Object>();

  private final HashMap<IAxis<T_logical>, T_logical[]> m_tickMap = new HashMap<IAxis<T_logical>, T_logical[]>();

  public AbstractAxisRenderer( final String id, final RGB rgbForeground, final RGB rgbBackground, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final FontData fdLabel, final FontData fdTick )
  {
    m_id = id;
    m_rgbForeground = rgbForeground;
    m_rgbBackground = rgbBackground;
    m_tickLength = tickLength;
    m_lineWidth = lineWidth;
    m_tickLabelInsets = tickLabelInsets;
    m_labelInsets = labelInsets;
    m_gap = gap;
    m_fontDataLabel = fdLabel;
    m_fontDataTick = fdTick;
  }

  protected abstract Point getTextExtent( GC gcw, T_logical value, FontData fontData, Format format );

  protected Point getTextExtent( GC gc, final String value, FontData fd )
  {
    Font oldFont = gc.getFont();
    final Font f = getFont( fd, gc.getDevice() );
    gc.setFont( f );
    final Point point = gc.textExtent( value );
    gc.setFont( oldFont );
    return point;
  }

  /**
   * draws a given text at the position using special fontData
   */
  protected void drawText( GC gc, String text, int x, int y, FontData fd )
  {
    gc.setTextAntialias( SWT.ON );
    Font oldfont = gc.getFont();
    gc.setFont( getFont( fd, gc.getDevice() ) );
    gc.drawText( text, x, y );
    gc.setFont( oldfont );
  }

  public int getTickLength( )
  {
    return m_tickLength;
  }

  public int getLineWidth( )
  {
    return m_lineWidth;
  }

  public int getGap( )
  {
    return m_gap;
  }

  public Insets getLabelInsets( )
  {
    return m_labelInsets;
  }

  public Insets getTickLabelInsets( )
  {
    return m_tickLabelInsets;
  }

  public FontData getFontDataLabel( )
  {
    return m_fontDataLabel;
  }

  public FontData getFontDataTickLabel( )
  {
    return m_fontDataTick;
  }

  public RGB getRgbForeground( )
  {
    return m_rgbForeground;
  }

  public RGB getRgbBackground( )
  {
    return m_rgbBackground;
  }

  public void setTickMapElement( IAxis<T_logical> axis, T_logical[] ticks )
  {
    m_tickMap.put( axis, ticks );
  }

  protected T_logical[] getTickMapElement( IAxis<T_logical> axis )
  {
    T_logical[] tickMap = m_tickMap.get( axis );
    return tickMap;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#setData()
   */
  public void setData( String id, Object data )
  {
    m_data.put( id, data );
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getData()
   */
  public Object getData( String id )
  {
    return m_data.get( id );
  }

  public String getId( )
  {
    return m_id;
  }

  @SuppressWarnings("unchecked")
  public void invalidateTicks( IAxis axis )
  {
    m_tickMap.put( axis, null );
  }

  public void dispose( )
  {
    for( Font f : m_fontMap.values() )
    {
      if( !f.isDisposed() )
        f.dispose();
    }
  }

  public synchronized Font getFont( FontData fd, Device dev )
  {
    Font font = m_fontMap.get( fd );
    if( font == null )
    {
      font = new Font( dev, fd );
      m_fontMap.put( fd, font );
    }
    return font;

  }
}
