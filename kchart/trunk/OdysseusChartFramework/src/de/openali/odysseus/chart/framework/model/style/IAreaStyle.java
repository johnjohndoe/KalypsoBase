package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.RGB;

public interface IAreaStyle extends IStyle
{

  public void setStroke( ILineStyle stroke );

  public void setInlineColor( RGB rgb );

}