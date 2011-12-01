package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.GC;

import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.IFill;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

public class AreaStyle extends AbstractStyle implements IAreaStyle
{
  private ILineStyle m_stroke;

  private IFill m_fill;

  private final boolean m_isFillVisible;

  public AreaStyle( final IFill fill, final int alpha, final ILineStyle stroke, final boolean isFillVisible )
  {
    setFill( fill );
    setAlpha( alpha );
    setVisible( stroke.isVisible() || isFillVisible );
    m_isFillVisible = isFillVisible;
    setStroke( stroke );
  }

  /**
   * copy constructor
   * 
   * @param style
   *          template style
   */
  @Override
  public IAreaStyle clone( )
  {
    return new AreaStyle( getFill().clone(), getAlpha(), getStroke().clone(), isVisible() );
  }

  /*
   * (non-Javadoc)
   * @see
   * de.openali.odysseus.chart.framework.impl.model.style.IAreaStyle#setOutline(de.openali.odysseus.chart.framework.
   * impl.model.style.ILineStyle)
   */
  @Override
  public void setStroke( final ILineStyle stroke )
  {
    m_stroke = stroke;
  }

  @Override
  public void apply( final GC gc )
  {
    if( m_stroke != null && m_stroke.isVisible() )
      m_stroke.apply( gc );

    final int alpha = getAlpha();
    gc.setAlpha( alpha );

    if( m_fill != null && m_isFillVisible )
      m_fill.apply( gc );
  }

  @Override
  public void setFill( final IFill fill )
  {
    m_fill = fill;
  }

  @Override
  public IFill getFill( )
  {
    return m_fill;
  }

  @Override
  public ILineStyle getStroke( )
  {
    return m_stroke;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.style.IAreaStyle#isFillVisible()
   */
  @Override
  public boolean isFillVisible( )
  {
    return m_isFillVisible;
  }

}
