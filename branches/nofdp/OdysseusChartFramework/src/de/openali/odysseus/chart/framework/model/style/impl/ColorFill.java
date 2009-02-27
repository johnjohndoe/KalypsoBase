package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.model.style.IFill;

public class ColorFill implements IFill
{

  private Color m_color;

  private final RGB m_rgb;

  public ColorFill( RGB rgb )
  {
    m_rgb = rgb;
    setFillColor( rgb );
  }

  public void setFillColor( RGB rgb )
  {
    if( (m_color != null) && !m_color.isDisposed() )
      m_color.dispose();
    m_color = new Color( Display.getDefault(), rgb );
  }

  public void apply( GC gc )
  {
    if( (m_color != null) & !m_color.isDisposed() )
      gc.setBackground( m_color );

  }

  public void dispose( )
  {
    if( (m_color != null) && !m_color.isDisposed() )
      m_color.dispose();
  }

  public ColorFill copy( )
  {
    return new ColorFill( getFillColor() );
  }

  private RGB getFillColor( )
  {
    return m_rgb;
  }

}
