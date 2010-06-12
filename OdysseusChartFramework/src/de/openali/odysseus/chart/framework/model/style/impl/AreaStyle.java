package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.GC;

import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.IFill;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

public class AreaStyle extends AbstractStyle implements IAreaStyle
{
  private ILineStyle m_stroke;

  private IFill m_fill;

  public AreaStyle( final IFill fill, final int alpha, final ILineStyle stroke, final boolean isVisible )
  {
    setFill( fill );
    setAlpha( alpha );
    setVisible( isVisible );
    setStroke( stroke );
  }

  /**
   * copy constructor
   * 
   * @param style
   *          template style
   */
  @Override
  public IAreaStyle copy( )
  {
    return new AreaStyle( getFill().copy(), getAlpha(), getStroke().copy(), isVisible() );
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
    if( m_stroke != null )
      m_stroke.apply( gc );

    final int alpha = getAlpha();
    gc.setAlpha( alpha );

    if( (m_fill != null) )
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

}
