package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;

public interface ILineStyle extends IStyle
{
  void setLineJoin( LINEJOIN join );

  void setLineCap( LINECAP cap );

  void setColor( RGB rgb );

  void setDash( float dashOffset, float[] dashArray );

  void setWidth( int width );

  int getWidth( );

  void setMiterLimit( int miterLimit );

  RGB getColor( );

  float[] getDashArray( );

  float getDashOffset( );

  LINEJOIN getLineJoin( );

  LINECAP getLineCap( );

  int getMiterLimit( );

  @Override
  ILineStyle copy( );

}