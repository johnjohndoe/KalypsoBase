package de.openali.odysseus.chart.framework.model.layer.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;

public abstract class LegendEntry implements ILegendEntry
{

  private final String m_description;

  private final IChartLayer m_parent;

  public LegendEntry( final IChartLayer parent, final String description )
  {
    m_parent = parent;
    m_description = description;
  }

  @Override
  public String getDescription( )
  {
    return m_description;
  }

  public abstract void paintSymbol( GC gc, Point size );

  @Override
  public ImageData getSymbol( final Point size )
  {
    final Point realSize = computeSize( size );
    final Image img = new Image( Display.getDefault(), realSize.x, realSize.y );
    final GC gc = new GC( img );
    paintSymbol( gc, realSize );
    final ImageData id = img.getImageData();
    gc.dispose();
    img.dispose();
    return id;
  }

  /**
   * @param size
   *          the preferred size
   * @return the minimum size or null if any
   */
  @Override
  public Point computeSize( final Point size )
  {
    final Point neededSize = getMinimumSize();
    final Point realSize = new Point( Math.max( neededSize.x, size.x ), Math.max( neededSize.y, size.y ) );
    return realSize;
  }

  /**
   * implementations need to override this method if the symbol needs a special size;
   * 
   * @return Point (16, 16)
   */
  public Point getMinimumSize( )
  {
    return new Point( 16, 16 );
  }

  @Override
  public IChartLayer getParentLayer( )
  {
    return m_parent;
  }
}
