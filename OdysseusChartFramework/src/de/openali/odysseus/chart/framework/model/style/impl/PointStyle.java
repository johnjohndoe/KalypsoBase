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

  public PointStyle( final ILineStyle outline, final int width, final int height, final int alpha, final RGB inlineColor, final boolean isFillVisible, final IMarker marker, final boolean isVisible )
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

  @Override
  public void setFillVisible( final boolean isFillVisible )
  {
    m_showFill = isFillVisible;
  }

  /*
   * (non-Javadoc)
   * @see
   * de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setOutline(de.openali.odysseus.chart.framework
   * .impl.model.style.ILineStyle)
   */
  @Override
  public void setStroke( final ILineStyle stroke )
  {
    m_stroke = stroke;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setWidth(int)
   */
  @Override
  public void setWidth( final int width )
  {
    m_width = width;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setHeight(int)
   */
  @Override
  public void setHeight( final int height )
  {
    m_height = height;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#getWidth()
   */
  @Override
  public int getWidth( )
  {
    return m_width;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#getHeight()
   */
  @Override
  public int getHeight( )
  {
    return m_height;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setInlineColor(org.eclipse.swt.graphics.RGB)
   */
  @Override
  public void setInlineColor( final RGB rgb )
  {
    m_inlineRGB = rgb;
  }

  /*
   * (non-Javadoc)
   * @see
   * de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setMarker(de.openali.odysseus.chart.framework.
   * model.style.IMarker)
   */
  @Override
  public void setMarker( final IMarker marker )
  {
    m_marker = marker;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#getMarker()
   */
  @Override
  public IMarker getMarker( )
  {
    return m_marker;
  }

  @Override
  public void apply( final GC gc )
  {
    m_stroke.apply( gc );

    gc.setAlpha( getAlpha() );

    gc.setBackground( OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( gc.getDevice(), getInlineColor() ) );

  }

  public void dispose( )
  {

  }

  @Override
  public RGB getInlineColor( )
  {
    return m_inlineRGB;
  }

  @Override
  public ILineStyle getStroke( )
  {
    return m_stroke;
  }

  @Override
  public IPointStyle clone( )
  {
    return new PointStyle( getStroke().clone(), getWidth(), getHeight(), getAlpha(), getInlineColor(), isFillVisible(), getMarker().copy(), isVisible() );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.style.IPointStyle#isFillVisible()
   */
  @Override
  public boolean isFillVisible( )
  {
    return m_showFill;
  }
}
