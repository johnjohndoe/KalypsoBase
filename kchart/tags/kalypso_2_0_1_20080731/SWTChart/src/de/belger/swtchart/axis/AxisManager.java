package de.belger.swtchart.axis;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.belger.swtchart.layer.IChartLayer;
import de.belger.swtchart.util.LogicalRange;
import de.belger.swtchart.util.SwitchDelegate;

/**
 * <p>
 * Manages the axises of an {@link de.belger.swtchart.ChartCanvas}.
 * </p>
 * 
 * @author Gernot Belger
 */
public class AxisManager
{
  protected static class AxisInfo
  {
    public final Collection<IChartLayer> layers = new LinkedList<IChartLayer>();

    public IAxisRenderer renderer = null;
  }

  private final Map<AxisRange, AxisInfo> m_map = new HashMap<AxisRange, AxisInfo>();

  public void addRange( final AxisRange range, final IChartLayer layer )
  {
    final AxisInfo info = getInfo( range );
    info.layers.add( layer );
  }

  public void removeRange( final AxisRange range, final IChartLayer layer )
  {
    final AxisInfo info = m_map.get( range );
    info.layers.remove( layer );
    if( info.layers == null )
      m_map.remove( range );
  }

  public void clear( )
  {
    m_map.clear();
  }

  public void clearLayers( )
  {
    for( final AxisInfo info : m_map.values() )
      info.layers.clear();
  }

  public void maxLogicalRanges( )
  {
    for( final Map.Entry<AxisRange, AxisInfo> entry : m_map.entrySet() )
    {
      final AxisRange axis = entry.getKey();
      final AxisInfo info = entry.getValue();

      LogicalRange range = null;
      for( final IChartLayer layer : info.layers )
      {
        final Rectangle2D bounds = layer.getBounds();
        if( bounds != null )
        {
          final Point2D topleft = new Point2D.Double( bounds.getX(), bounds.getY() );
          final Point2D bottomright = new Point2D.Double( bounds.getX() + bounds.getWidth(), bounds.getY() + bounds.getHeight() );
          final SwitchDelegate crdSwitch = axis.getSwitch();
          final LogicalRange logRange = new LogicalRange( crdSwitch.getX( topleft ), crdSwitch.getX( bottomright ) );
          if( range == null )
            range = logRange;
          else
            range.add( logRange );
        }
      }
      if( range != null )
        axis.setLogicalRange( range );
    }
  }

  public void setScreenArea( final Rectangle screen )
  {
    final Point topleft = new Point( screen.x, screen.y );
    final Point bottomright = new Point( screen.x + screen.width, screen.y + screen.height );

    for( final AxisRange axis : m_map.keySet() )
    {
      final SwitchDelegate crdSwitch = axis.getSwitch();
      final int screenFrom = crdSwitch.getX( topleft );
      final int screenTo = crdSwitch.getX( bottomright );

      axis.setScreenRange( screenFrom, screenTo );
    }
  }

  public Rectangle reduceByAxisSize( final GCWrapper gc, final Rectangle screenArea )
  {
    Rectangle newsize = new Rectangle( screenArea.x, screenArea.y, screenArea.width, screenArea.height );
    for( final Map.Entry<AxisRange, AxisInfo> entry : m_map.entrySet() )
    {
      final AxisRange axis = entry.getKey();
      final AxisInfo info = entry.getValue();

      if( info.renderer != null )
        newsize = info.renderer.reduceScreenSize( gc, axis, newsize );
    }

    return newsize;
  }

  public void paintAxises( final GCWrapper gc, final Rectangle screenArea )
  {
    for( final Map.Entry<AxisRange, AxisInfo> entry : m_map.entrySet() )
    {
      final AxisRange axis = entry.getKey();
      final AxisInfo info = entry.getValue();
      if( info.renderer != null )
        info.renderer.paint( gc, axis, screenArea );
    }
  }

  public void setRenderer( final AxisRange range, final IAxisRenderer renderer )
  {
    final AxisInfo info = getInfo( range );
    info.renderer = renderer;
  }

  private AxisInfo getInfo( final AxisRange range )
  {
    final AxisInfo info = m_map.get( range );
    if( info != null )
      return info;

    final AxisInfo newinfo = new AxisInfo();
    m_map.put( range, newinfo );

    return newinfo;
  }
}
