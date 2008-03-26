package de.openali.odysseus.chart.framework.model.figure;

import org.eclipse.swt.graphics.GC;

import de.openali.odysseus.chart.framework.model.style.IStyle;

public interface IFigure<T_style extends IStyle>
{

  public void setStyle( T_style style );

  public T_style getStyle( );

  public void paint( GC gc );

  public void paintFigure( GC gc );

  public void dispose( );

}
