package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;
import de.openali.odysseus.chart.framework.model.style.IFill;

public class ColorFill implements IFill
{

  private RGB m_rgb;

  public ColorFill( RGB rgb )
  {
    m_rgb = rgb;
    setFillColor( rgb );
  }

  public void setFillColor( RGB rgb )
  {
    m_rgb = rgb;
  }

  public void apply( GC gc )
  {
    gc.setBackground( OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( gc.getDevice(), getFillColor() ) );

  }

  public void dispose( )
  {

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
