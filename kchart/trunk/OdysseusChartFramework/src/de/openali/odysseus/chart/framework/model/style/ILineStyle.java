package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;

public interface ILineStyle extends IStyle
{
  public void setLineJoin( LINEJOIN join );

  public void setLineCap( LINECAP cap );

  public void setColor( RGB rgb );

  public void setDash( float dashOffset, float[] dashArray );

  public void setWidth( int width );

  public int getWidth( );

  public void setMiterLimit( int miterLimit );

  public RGB getColor( );

  public float[] getDashArray( );

  public float getDashOffset( );

  public LINEJOIN getLineJoin( );

  public LINECAP getLineCap( );

  public int getMiterLimit( );

  public ILineStyle copy( );

}