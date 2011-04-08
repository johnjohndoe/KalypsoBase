package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public class OvalMarker extends AbstractMarker
{
  @Override
  public void paint( final GC gc, final Point p, final int width, final int height, final boolean drawForeground, final boolean drawBackground )
  {
    if( drawBackground )
      gc.fillOval( p.x, p.y, width, height );
    // REMARK: need to subtract 1, else outside does not fit inside
    if( drawForeground )
      gc.drawOval( p.x, p.y, width - 1, height - 1 );
  }

  @Override
  public OvalMarker copy( )
  {
    return new OvalMarker();
  }
}