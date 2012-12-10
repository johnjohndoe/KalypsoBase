package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.RGB;

public interface IPointStyle extends IStyle
{
  @Override
  IPointStyle clone( );

  int getHeight( );

  RGB getInlineColor( );

  IMarker getMarker( );

  ILineStyle getStroke( );

  int getWidth( );

  boolean isFillVisible( );

  void setFillVisible( boolean isFillVisible );

  void setHeight( int height );

  void setInlineColor( RGB rgb );

  void setMarker( IMarker marker );

  void setStroke( ILineStyle stroke );

  void setWidth( int width );
}