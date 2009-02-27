package de.openali.odysseus.chart.framework.model.figure;

import de.openali.odysseus.chart.framework.model.style.IStyle;

public interface IFigure<T_style extends IStyle> extends IPaintable
{

  public void setStyle( T_style style );

  public T_style getStyle( );

}
