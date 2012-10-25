package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.OdysseusChartFramework;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IMarker;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

public class PointStyle extends AbstractStyle implements IPointStyle
{
  // TODO: hm... we use a stroke, but for the fill we use inlineColor etc.; wouldn't it be better to use a fill style?

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

  @Override
  public void setStroke( final ILineStyle stroke )
  {
    m_stroke = stroke;
  }

  @Override
  public void setWidth( final int width )
  {
    m_width = width;
  }

  @Override
  public void setHeight( final int height )
  {
    m_height = height;
  }

  @Override
  public int getWidth( )
  {
    return m_width;
  }

  @Override
  public int getHeight( )
  {
    return m_height;
  }

  @Override
  public void setInlineColor( final RGB rgb )
  {
    m_inlineRGB = rgb;
  }

  @Override
  public void setMarker( final IMarker marker )
  {
    m_marker = marker;
  }

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

    gc.setBackground( OdysseusChartFramework.getDefault().getColorRegistry().getResource( gc.getDevice(), getInlineColor() ) );
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

  @Override
  public boolean isFillVisible( )
  {
    return m_showFill;
  }
}
