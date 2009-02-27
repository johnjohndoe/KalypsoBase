package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public class OvalMarker extends AbstractMarker
{

  public void paint( GC gc, Point p, int width, int height )
  {

    gc.fillOval( p.x, p.y, width, height );
    gc.drawOval( p.x, p.y, width, height );
  }

  public OvalMarker copy( )
  {
    return new OvalMarker();
  }

}
