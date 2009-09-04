package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.RGB;

public interface IPointStyle extends IStyle
{

  public void setStroke( ILineStyle stroke );

  public void setWidth( int width );

  public void setHeight( int height );

  public int getWidth( );

  public int getHeight( );

  public void setInlineColor( RGB rgb );

  public void setFillVisible( boolean isFillVisible );

  public boolean isFillVisible( );

  public void setMarker( IMarker marker );

  public IMarker getMarker( );

  public ILineStyle getStroke( );

  public RGB getInlineColor( );

  public IPointStyle copy( );

}