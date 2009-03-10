package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IMarker;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

public class PointStyle extends AbstractStyle implements IPointStyle
{
  private RGB m_inlineRGB;

  private IMarker m_marker;

  private int m_width;

  private int m_height;

  private ILineStyle m_stroke;

  private boolean m_showFill;

  public PointStyle( ILineStyle outline, int width, int height, int alpha, RGB inlineColor, boolean isFillVisible, IMarker marker, boolean isVisible )
  {
    setStroke( outline );
    setWidth( width );
    setHeight( height );
    setInlineColor( inlineColor );
    setMarker( marker );
    setAlpha( alpha );
    setVisible( isVisible );
    setFillVisible( isFillVisible );
  }

  public void setFillVisible( boolean isFillVisible )
  {
    m_showFill = isFillVisible;
  }

  /*
   * (non-Javadoc)
   * @see
   * de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setOutline(de.openali.odysseus.chart.framework
   * .impl.model.style.ILineStyle)
   */
  public void setStroke( ILineStyle stroke )
  {
    m_stroke = stroke;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setWidth(int)
   */
  public void setWidth( int width )
  {
    m_width = width;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setHeight(int)
   */
  public void setHeight( int height )
  {
    m_height = height;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#getWidth()
   */
  public int getWidth( )
  {
    return m_width;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#getHeight()
   */
  public int getHeight( )
  {
    return m_height;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setInlineColor(org.eclipse.swt.graphics.RGB)
   */
  public void setInlineColor( RGB rgb )
  {
    m_inlineRGB = rgb;
  }

  /*
   * (non-Javadoc)
   * @see
   * de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setMarker(de.openali.odysseus.chart.framework.
   * model.style.IMarker)
   */
  public void setMarker( IMarker marker )
  {
    m_marker = marker;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#getMarker()
   */
  public IMarker getMarker( )
  {
    return m_marker;
  }

  public void apply( GC gc )
  {
    m_stroke.apply( gc );

    gc.setAlpha( getAlpha() );

    gc.setBackground( OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( gc.getDevice(), getInlineColor() ) );

  }

  public void dispose( )
  {

  }

  public RGB getInlineColor( )
  {
    return m_inlineRGB;
  }

  public ILineStyle getStroke( )
  {
    return m_stroke;
  }

  public IPointStyle copy( )
  {
    return new PointStyle( getStroke().copy(), getWidth(), getHeight(), getAlpha(), getInlineColor(), isFillVisible(), getMarker().copy(), isVisible() );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.style.IPointStyle#isFillVisible()
   */
  public boolean isFillVisible( )
  {
    return m_showFill;
  }
}
