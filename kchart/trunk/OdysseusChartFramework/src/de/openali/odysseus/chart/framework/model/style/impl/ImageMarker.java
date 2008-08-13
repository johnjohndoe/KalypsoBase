package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;

public class ImageMarker extends AbstractMarker
{

  private final ImageDescriptor m_id;

  public ImageMarker( ImageDescriptor id )
  {
    m_id = id;
  }

  public void paint( GC gc, Point pos, int width, int height, boolean drawForeground, boolean drawBackground )
  {
    Image img = OdysseusChartFrameworkPlugin.getDefault().getImageRegistry().getResource( gc.getDevice(), m_id );
    if( drawBackground )
      gc.fillRectangle( pos.x, pos.y, width, height );
    gc.drawImage( img, 0, 0, img.getBounds().width, img.getBounds().height, pos.x, pos.y, width, height );
    if( drawForeground )
      gc.drawRectangle( pos.x, pos.y, width, height );
  }

  public ImageMarker copy( )
  {
    return new ImageMarker( m_id );
  }

}
