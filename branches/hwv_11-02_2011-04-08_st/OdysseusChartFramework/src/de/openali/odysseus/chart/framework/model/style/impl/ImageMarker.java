package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.OdysseusChartFramework;

public class ImageMarker extends AbstractMarker
{

  private final ImageDescriptor m_id;

  public ImageMarker( final ImageDescriptor id )
  {
    m_id = id;
  }

  @Override
  public void paint( final GC gc, final Point pos, final int width, final int height, final boolean drawForeground, final boolean drawBackground )
  {
    final Image img = OdysseusChartFramework.getDefault().getImageRegistry().getResource( gc.getDevice(), m_id );
    if( drawBackground )
      gc.fillRectangle( pos.x, pos.y, width, height );
    gc.drawImage( img, 0, 0, img.getBounds().width, img.getBounds().height, pos.x, pos.y, width, height );
    if( drawForeground )
      gc.drawRectangle( pos.x, pos.y, width, height );
  }

  @Override
  public ImageMarker copy( )
  {
    return new ImageMarker( m_id );
  }

}
