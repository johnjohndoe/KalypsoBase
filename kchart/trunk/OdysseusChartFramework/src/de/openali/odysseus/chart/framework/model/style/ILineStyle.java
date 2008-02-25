package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;

public interface ILineStyle extends IStyle
{

  public void setLineJoin( LINEJOIN join );

  public void setLineCap( LINECAP cap );

  public void setColor( RGB rgb );

  public void setDash( int dashOffset, int[] dashArray );

  public void setWidth( int width );

  public void setMiterLimit( int miterLimit );

}