package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;

public interface ILineStyle extends IStyle
{
  @Override
  ILineStyle copy( );

  RGB getColor( );

  float[] getDashArray( );

  float getDashOffset( );

  LINECAP getLineCap( );

  LINEJOIN getLineJoin( );

  int getMiterLimit( );

  int getWidth( );

  void setColor( RGB rgb );

  void setDash( float dashOffset, float[] dashArray );

  void setLineCap( LINECAP cap );

  void setLineJoin( LINEJOIN join );

  void setMiterLimit( int miterLimit );

  void setWidth( int width );
}