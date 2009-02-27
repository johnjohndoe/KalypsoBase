package org.kalypso.chart.framework.trash;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.chart.framework.model.layer.ILayerManager;

/**
 * @author burtscher Creates an Image containing the legend of a specified chart from configuration data. Right now this
 *         is done in some long winded way: first, the chart widget has to be created, then the charts layers need to be
 *         traversed and filled into the legend. This has to be done as the layer provider is needed to create the
 *         layers and is to be initialized with a chart widget. The object has to be disposed when not needed any more
 */
public class LegendImageContainer
{

  Image m_img = null;

  /**
   * @param chart
   *            (preloaded) Chart widget whose legend shall be created
   * @param dev
   *            Device for which the image shall be created
   */
  public LegendImageContainer( ILayerManager layerManager, Device dev )
  {
    final Shell shell = new Shell();
    shell.setLayout( new FillLayout() );
    final Legend legend = new Legend( shell, SWT.NONE );
    // Layer umfüllen
    legend.addLayers( layerManager.getLayers() );
    // Größe berechnen lassen
    final Point size = legend.computeSize( 0, 0 );
    m_img = new Image( dev, size.x, size.y );
    // Legende zeichnen
    legend.drawImage( m_img, Display.getDefault() );
    legend.dispose();
    shell.dispose();
  }

  /**
   * @return Legend image
   */
  public Image getImage( )
  {
    return m_img;
  }

  public void dispose( )
  {
    if( m_img != null )
      m_img.dispose();
    m_img = null;
  }
}
