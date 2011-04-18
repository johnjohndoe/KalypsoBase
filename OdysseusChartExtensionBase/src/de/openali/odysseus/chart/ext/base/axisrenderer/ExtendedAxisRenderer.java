package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.text.Format;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.img.ChartLabelRendererFactory;
import de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;

/**
 * @author kimwerner
 */
public class ExtendedAxisRenderer extends AbstractGenericAxisRenderer
{

  private ITickCalculator m_tickCalculator;

  private ILabelCreator m_labelCreator;

  public ExtendedAxisRenderer( final String id, final ILabelCreator labelCreator, final IChartLabelRenderer axisLabelRenderer, final IChartLabelRenderer tickLabelRenderer, final ITickCalculator tickCalculator, final AxisRendererConfig config )
  {
    super( id, axisLabelRenderer, tickLabelRenderer, config );
    setTickCalculator( tickCalculator );
    setLabelCreator( labelCreator );
    setMinTickInterval( config.minTickInterval );
    setHideCut( config.hideCut );
    setFixedWidth( config.fixedWidth );
  }

  public ExtendedAxisRenderer( final String id, final POSITION position, final ILabelCreator labelCreator, final ITickCalculator tickCalculator, final AxisRendererConfig config )
  {
    this( id, labelCreator, ChartLabelRendererFactory.getAxisLabelRenderer( position, config.labelInsets, config.labelStyle, config.textBorderStyle ), ChartLabelRendererFactory.getTickLabelRenderer( position, config.tickLabelInsets, config.tickLabelStyle, config.textBorderStyle ), tickCalculator, config );
  }

  public Point calcTickLabelSize( final IAxis axis )
  {

    final IDataRange<Number> range = axis.getNumericRange();
    if( range.getMin() == null || range.getMax() == null )
      return new Point( 0, 0 );
    final String logicalfrom = getLabelCreator().getLabel( range.getMin(), range );
    final String logicalto = getLabelCreator().getLabel( range.getMax(), range );
    final IChartLabelRenderer labelRenderer = getTickLabelRenderer();
    labelRenderer.getTitleTypeBean().setText( logicalfrom );
    final Point fromSize = labelRenderer.getSize();
    labelRenderer.getTitleTypeBean().setText( logicalto );
    final Point toSize = labelRenderer.getSize();

    return new Point( Math.max( fromSize.x, toSize.x ), Math.max( fromSize.y, toSize.y ) );

  }


  /**
   * draws the Axis ticks into the given GC
   */
  private void drawTicks( final GC gc, final int offset, final IAxis axis, final Number[] ticks )
  {

    if( (gc == null) || (axis == null) || (ticks == null) || ticks.length < 1 )
      return;

    for( int i = 0; i < ticks.length; i++ )
    {

      boolean drawTick = true;
      boolean drawTickLabel = true;
      final int tickPos = axis.numericToScreen( ticks[i] );
      if( tickPos < 0 || tickPos > axis.getScreenHeight() )
        drawTick = false;
      if( (/* m_hideCut || */!drawTick) )
        drawTickLabel = false;
      if( drawTick )
      {
        getTickLineStyle().apply( gc );
        gc.drawLine( tickPos, getTickLineStyle().getWidth() + getGap() + offset, tickPos, getTickLineStyle().getWidth() + getGap() + offset + getTickLength() );
      }
      if( drawTickLabel )
      {
        final IChartLabelRenderer labelRenderer = getTickLabelRenderer();
        labelRenderer.getTitleTypeBean().setLabel( getLabelCreator().getLabel( ticks, i, axis.getNumericRange() ) );
        labelRenderer.paint( gc, new Rectangle( tickPos, (getLineStyle().getWidth() + getGap() + getTickLength() + offset), -1, -1 ) );
      }
    }
  }

  /**
   * Gibt die Breite bzw. Tiefe einer Achse zurück. Dadurch wird die Grösse der AxisComponent bestimmt
   */
  @Override
  public int getAxisWidth( final IAxis axis )
  {
    if( !axis.isVisible() )
      return 0;

    // if width is fixed, return fixed width
    if( getFixedWidth() > 0 )
      return getFixedWidth();

    // Else: Calculate
    // check nullValue first
    final String axisLabel = axis.getLabel();
    final TitleTypeBean[] axisLabels = axis.getLabels();
    final boolean labelEmpty = axisLabel != null && axisLabel != null && (axisLabels.length == 0 ? true : axisLabel.trim().equals( "" ));

    int width = 0;
    final int gap = getGap();
    final int lineWidth = getLineStyle().getWidth();
    final int tickLength = getTickLength();

    final Point tls = calcTickLabelSize( axis );
    // wenn keine Ticks gezeichnet werden ist die ganze Achse unsichtbar
    if( tls.y == 0 )
      return 0;

    // Label: nur mit einrechnen, wenn nicht leer
    if( !labelEmpty )
    {
      int labelsWidth = 0;
      final IChartLabelRenderer labelRenderer = getAxisLabelRenderer();
      for( final TitleTypeBean title : axis.getLabels() )
      {
        labelRenderer.setTitleTypeBean( title );
        labelsWidth = Math.max( labelsWidth, labelRenderer.getSize().y );
      }
      width += labelsWidth;
    }

    width += tls.y;
    width += gap;
    width += lineWidth;
    width += tickLength;

    return width;
  }

  public int getFixedWidth( )
  {
    return getAxisConfig().fixedWidth;
  }

  public ILabelCreator getLabelCreator( )
  {
    return m_labelCreator;
  }

  public Number getMinTickInterval( )
  {
    return getAxisConfig().minTickInterval;
  }

  @Override
  protected Point getTextExtent( final GC gc, final Number value, final ITextStyle style, final Format format, final IDataRange<Number> range )
  {
    final String label = getLabelCreator().getLabel( value, range );
    final Point p = getTextExtent( gc, label, style );
    return p;
  }

  public ITickCalculator getTickCalculator( )
  {
    return m_tickCalculator;
  }

  @Override
  public Number[] getTicks( final IAxis axis, final GC gc )
  {
    final Point tickLabelSize = calcTickLabelSize( axis );
    final Number[] ticks = getTickCalculator().calcTicks( gc, axis, getMinTickInterval(), tickLabelSize );
    return ticks;
  }

  public boolean isHideCut( )
  {
    return getAxisConfig().hideCut;
  }

  @Override
  public void paint( final GC gc, final IAxis axis, final Rectangle screen )
  {
    if( (screen.width > 0) && (screen.height > 0) && axis.isVisible() )
    {

      getLineStyle().apply( gc );
      gc.drawLine( screen.x, getGap() + screen.y, axis.getScreenHeight(), getGap() + screen.y );
      drawTicks( gc, screen.y, axis, getTicks( axis, gc ) );
      final IChartLabelRenderer labelRenderer = getAxisLabelRenderer();
      for( final TitleTypeBean title : axis.getLabels() )
      {
        labelRenderer.setTitleTypeBean( title );
        labelRenderer.paint( gc, screen );
      }
    }

  }

  public void setFixedWidth( final int fixedWidth )
  {
    getAxisConfig().fixedWidth = fixedWidth;
  }

  public void setHideCut( final boolean hideCut )
  {
    getAxisConfig().hideCut = hideCut;
  }

  public void setLabelCreator( final ILabelCreator labelCreator )
  {
    m_labelCreator = labelCreator;
  }

  public void setMinTickInterval( final Number minTickInterval )
  {
    getAxisConfig().minTickInterval = minTickInterval;
  }

  public void setTickCalculator( final ITickCalculator tickCalculator )
  {
    m_tickCalculator = tickCalculator;
  }
}
