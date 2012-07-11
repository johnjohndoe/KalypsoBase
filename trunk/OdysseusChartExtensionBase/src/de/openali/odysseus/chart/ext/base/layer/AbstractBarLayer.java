package de.openali.odysseus.chart.ext.base.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.figure.impl.FullRectangleFigure;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSet;

/**
 * @author alibu
 */
public abstract class AbstractBarLayer extends AbstractChartLayer
{
  private ILegendEntry[] m_legendEntries;

  public AbstractBarLayer( final ILayerProvider provider, final IAreaStyle areaStyle )
  {
    super( provider, new StyleSet() );
    getStyleSet().addStyle( "area", areaStyle );
  }

  public AbstractBarLayer( final ILayerProvider provider, final IStyleSet styleSet )
  {
    super( provider, styleSet );
  }

  private ILegendEntry[] createLegendEntries( )
  {
    final List<ILegendEntry> entries = new ArrayList<ILegendEntry>();
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
    return styleSet.getStyle( "area", IAreaStyle.class );
  }

  @Override
  public synchronized ILegendEntry[] getLegendEntries( )
  {
    if( m_legendEntries == null )
      m_legendEntries = createLegendEntries();

    return m_legendEntries;
  }

  protected FullRectangleFigure getRectangleFigure( )
  {
    final IAreaStyle as = getAreaStyle();
    final FullRectangleFigure rectangleFigure = new FullRectangleFigure();

    rectangleFigure.setStyle( as );

    return rectangleFigure;
  }

  /**
   * @deprecated: Bad: leads to memory problems for big data. Inline and diretly paint each rectangle
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
}