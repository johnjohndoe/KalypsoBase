package de.openali.odysseus.chart.ext.base.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.figure.impl.FullRectangleFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;

/**
 * @author alibu
 */
public abstract class AbstractBarLayer extends AbstractChartLayer implements ITooltipChartLayer
{
  private ILegendEntry[] m_legendEntries;

  private HoverIndex m_hoverIndex;

  public AbstractBarLayer( final ILayerProvider provider, final IStyleSet styleSet )
  {
    super( provider, styleSet );
  }

  private ILegendEntry[] createLegendEntries( )
  {
    final List<ILegendEntry> entries = new ArrayList<>();
    // TODO: use all styles that are available
    final FullRectangleFigure pf = getRectangleFigure();
    if( pf.getStyle().isVisible() )
    {
      final LegendEntry entry = new LegendEntry( this, getTitle() )
      {
        @Override
        public void paintSymbol( final GC gc, final Point size )
        {
          final int height = size.x;
          final int width = size.y;
          paint( gc, new Rectangle( 0, height / 2, width / 2, height / 2 ) );
          paint( gc, new Rectangle( width / 2, 0, width / 2, height ) );
        }
      };

      entries.add( entry );
    }
    return entries.toArray( new ILegendEntry[] {} );
  }

  protected IAreaStyle getAreaStyle( )
  {
    final IStyleSet styleSet = getStyleSet();
    return styleSet.getStyle( "area", IAreaStyle.class ); //$NON-NLS-1$
  }

  @Override
  public synchronized ILegendEntry[] getLegendEntries( )
  {
    if( m_legendEntries == null )
      m_legendEntries = createLegendEntries();

    return m_legendEntries;
  }

  /**
   * @deprecated This is just too simple...
   */
  @Deprecated
  protected FullRectangleFigure getRectangleFigure( )
  {
    final IAreaStyle as = getAreaStyle();
    final FullRectangleFigure rectangleFigure = new FullRectangleFigure();

    rectangleFigure.setStyle( as );

    return rectangleFigure;
  }

  /**
   * @deprecated: Bad: leads to memory problems for big data. Inline and directly paint each rectangle
   */
  @Deprecated
  protected final void paint( final GC gc, final Rectangle... rectangles )
  {
    final FullRectangleFigure rf = getRectangleFigure();
    for( final Rectangle rect : rectangles )
    {
      rf.setRectangle( rect );
      rf.paint( gc );
    }
  }

  @Override
  public void paint( final GC gc, final ChartImageInfo chartImageInfo, final IProgressMonitor monitor )
  {
    final String taskName = String.format( "Painting %s", getTitle() );
    monitor.beginTask( taskName, 100 );

    try
    {
      final BarPaintManager paintManager = new BarPaintManager( gc, getStyleSet() );

      final IBarLayerPainter painter = createPainter( paintManager );
      if( painter == null )
        return;

      painter.execute( new SubProgressMonitor( monitor, 50 ) );

      if( monitor.isCanceled() )
        return;

      m_hoverIndex = paintManager.getIndex();
    }
    catch( final OperationCanceledException e )
    {
      return;
    }
  }

  protected abstract IBarLayerPainter createPainter( final BarPaintManager paintManager );

  @Override
  public EditInfo getHover( final Point pos )
  {
    if( m_hoverIndex == null )
      return null;

    return m_hoverIndex.findElement( pos );
  }

  protected void invalidateHoverIndex( )
  {
    m_hoverIndex = null;
  }
}