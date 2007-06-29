package org.kalypso.chart.framework.model.mapper.renderer;

import java.awt.Insets;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;


/**
 * @author alibu
 *
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
  private HashMap<IAxis<T_logical>, List<T_logical>> m_tickMap=new HashMap<IAxis<T_logical>, List<T_logical>>();



  public AbstractAxisRenderer( final RGB rgbForeground, final RGB rgbBackground, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final FontData fdLabel, final FontData fdTick)
  {
    m_rgbForeground = rgbForeground;
    m_rgbBackground = rgbBackground;
    m_tickLength = tickLength;
    m_lineWidth = lineWidth;
    m_tickLabelInsets = tickLabelInsets;
    m_labelInsets = labelInsets;
    m_gap = gap;
    m_fontDataLabel=fdLabel;
    m_fontDataTick=fdTick;
  }




  protected abstract Point getTextExtent( GCWrapper gcw, T_logical value, FontData fontData);


  protected Point getTextExtent( GCWrapper gc, final String value, FontData fd )
  {
    Font f = new Font( gc.getDevice(), fd );
    gc.setFont( f );
    Point point = gc.textExtent( value );
    gc.setFont( gc.getDevice().getSystemFont() );
    f.dispose();
    return point;
  }




  /**
   * draws a given text at the position using special fontData
   */
  protected void drawText( GCWrapper gc, String text, int x, int y, FontData fd )
  {
    Font f = new Font( gc.getDevice(), fd );
    gc.m_gc.setTextAntialias( SWT.ON );
    gc.setFont( f );
    gc.drawText( text, x, y );
    gc.setFont( gc.getDevice().getSystemFont() );
    f.dispose();
  }
  
  public int getTickLength()
  {
  	return m_tickLength;
  }
  
  public int getLineWidth()
  {
	  return m_lineWidth;
  }
  
  public int getGap()
  {
	  return m_gap;
  }
  
  public Insets getLabelInsets()
  {
	  return m_labelInsets;
  }
  
  public Insets getTickLabelInsets()
  {
	  return m_tickLabelInsets;
  }
  
  public FontData getFontDataLabel()
  {
	  return m_fontDataLabel;
  }
  
  public FontData getFontDataTickLabel()
  {
	  return m_fontDataTick;
  }
  
  public RGB getRgbForeground()
  {
	  return m_rgbForeground;
  }
  
  public RGB getRgbBackground()
  {
	  return m_rgbBackground;
  }
  
  public HashMap<IAxis<T_logical>, List<T_logical>> getTickMap()
  {
	  return m_tickMap;
  }
  
  public void setTickMapElement(IAxis<T_logical> axis, List<T_logical> ticks)
  {
	  m_tickMap.put(axis, ticks);
  }




}
