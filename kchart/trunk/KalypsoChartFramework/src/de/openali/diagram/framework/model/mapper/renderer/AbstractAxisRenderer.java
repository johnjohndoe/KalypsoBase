package de.openali.diagram.framework.model.mapper.renderer;

import java.awt.Insets;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.openali.diagram.framework.model.mapper.IAxis;

/**
 * @author alibu
 *
 */
public abstract class AbstractAxisRenderer<T_logical extends Comparable> implements IAxisRenderer<T_logical>
{

  protected int m_tickLength=0;
  protected int m_lineWidth=0;
  protected int m_gap=0;
  protected Insets m_labelInsets=null;
  protected Insets m_tickLabelInsets=null;
  protected FontData m_fontDataLabel=null;
  protected FontData m_fontDataTick=null;
  protected HashMap<IAxis<T_logical>, List<T_logical>> m_tickMap=new HashMap<IAxis<T_logical>, List<T_logical>>();
  protected final RGB m_rgbForeground;
  protected final RGB m_rgbBackground;



  public AbstractAxisRenderer( final RGB rgbForegroundRGB, final RGB rgbBackground, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final FontData fdLabel, final FontData fdTick)
  {
    m_rgbForeground = rgbForegroundRGB;
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





}
