package de.openali.odysseus.chart.framework.model.style;

public interface IAreaStyle extends IStyle
{
  @Override
  IAreaStyle clone( );

  IFill getFill( );

  ILineStyle getStroke( );

  boolean isFillVisible( );

  void setFillVisible( boolean visible );

  void setFill( IFill fill );

  void setStroke( ILineStyle stroke );
}