package de.openali.odysseus.chart.framework.model.figure.impl;

import org.eclipse.swt.graphics.GC;

import de.openali.odysseus.chart.framework.model.figure.IFigure;
import de.openali.odysseus.chart.framework.model.style.IStyle;

public abstract class AbstractFigure<T_style extends IStyle> implements IFigure<T_style>
{
  private T_style m_style;

  @Override
  public void setStyle( final T_style ts )
  {
    m_style = ts;
  }

  @Override
  public T_style getStyle( )
  {
    return m_style;
  }

  @Override
  public final void paint( final GC gc )
  {
    if( m_style != null && m_style.isVisible() )
    {
      m_style.apply( gc );
      paintFigure( gc );
    }
  }

  protected abstract void paintFigure( GC gc );

}
