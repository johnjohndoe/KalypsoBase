package de.openali.odysseus.chart.framework.model.style;

public interface IAreaStyle extends IStyle
{

  public void setStroke( ILineStyle stroke );

  public void setFill( IFill fill );

  public IFill getFill( );

  public ILineStyle getStroke( );

  public IAreaStyle copy( );

}