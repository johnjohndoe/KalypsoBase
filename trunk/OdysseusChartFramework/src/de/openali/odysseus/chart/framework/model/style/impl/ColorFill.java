package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;
import de.openali.odysseus.chart.framework.model.style.IFill;

public class ColorFill implements IFill
{

  private RGB m_rgb;

  public ColorFill( final RGB rgb )
  {
    m_rgb = rgb;
    setFillColor( rgb );
  }

  public void setFillColor( final RGB rgb )
  {
    m_rgb = rgb;
  }

  @Override
  public void apply( final GC gc )
  {
    gc.setBackground( OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( gc.getDevice(), getFillColor() ) );

  }

  @Override
  public void dispose( )
  {

  }

  @Override
  public ColorFill copy( )
  {
    return new ColorFill( getFillColor() );
  }

  private RGB getFillColor( )
  {
    return m_rgb;
  }

}
