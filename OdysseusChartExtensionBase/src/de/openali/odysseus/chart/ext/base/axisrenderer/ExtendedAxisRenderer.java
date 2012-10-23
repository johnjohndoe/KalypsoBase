package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.text.Format;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

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

  private final boolean m_intervallLabeledTick;

  private boolean isIntervallLabeledTick( )
  {
    return m_intervallLabeledTick;
  }

  public ExtendedAxisRenderer( final String id, final ILabelCreator labelCreator, final IChartLabelRenderer axisLabelRenderer, final IChartLabelRenderer tickLabelRenderer, final ITickCalculator tickCalculator, final AxisRendererConfig config )
  {
    super( id, axisLabelRenderer, tickLabelRenderer, config );
    setTickCalculator( tickCalculator );
    /**
     * statt des labelcreators @link DataOperator#toString(Object) nutzen
     */
    setLabelCreator( labelCreator );
    setMinTickInterval( config.minTickInterval );
    setHideCut( config.hideCut );
    setFixedWidth( config.fixedWidth );
    m_intervallLabeledTick = config.intervallLabeledTick;
  }

  public ExtendedAxisRenderer( final String id, final POSITION position, final ILabelCreator labelCreator, final ITickCalculator tickCalculator, final AxisRendererConfig config )
  {
    this( id, labelCreator, ChartLabelRendererFactory.getAxisLabelRenderer( position, config.labelInsets, config.labelStyle, config.textBorderStyle ), ChartLabelRendererFactory.getTickLabelRenderer( position, config.tickLabelInsets, config.tickLabelStyle, config.textBorderStyle ), tickCalculator, config );
  }

  @SuppressWarnings( "rawtypes" )
  public Point calcTickLabelSize( final IAxis axis )
  {
    final IDataRange<Number> range = axis.getNumericRange();
    if( range.getMin() == null || range.getMax() == null )
      return new Point( 0, 0 );

    final String logicalfrom = getLabelCreator().getLabel( range.getMin(), range );
    final String logicalto = getLabelCreator().getLabel( range.getMax(), range );
    final IChartLabelRenderer labelRenderer = getTickLabelRenderer();
    labelRenderer.getTitleTypeBean().setText( logicalfrom );
    final Rectangle fromSize = labelRenderer.getSize();
    labelRenderer.getTitleTypeBean().setText( logicalto );
    final Rectangle toSize = labelRenderer.getSize();

    return new Point( Math.max( fromSize.width, toSize.width ), Math.max( fromSize.height, toSize.height ) );

  }

  /**
   * draws the Axis ticks into the given GC
   */
  @SuppressWarnings( "rawtypes" )
  private void drawTicks( final GC gc, final Rectangle screen, final IAxis axis, final Double[] ticks )
  {

    // final Insets inset = new Insets( screen.y, screen.x, -1, screen.width - screen.x - axis.getScreenHeight() );
    if( gc == null || axis == null || ticks == null || ticks.length < 1 )
      return;
    getTickLineStyle().apply( gc );
    final IChartLabelRenderer labelRenderer = getTickLabelRenderer();
    int tickDistance = -1;

    // TODO axisrenderer don't render labels, move isIntervallLabeledTick to labelRenderer
    if( isIntervallLabeledTick() )
    {
      if( ticks.length > 1 )
      {
        tickDistance = Math.abs( axis.numericToScreen( ticks[1] ) - axis.numericToScreen( ticks[0] ) );
      }
      else
      {
        tickDistance = axis.getScreenHeight();
      }
    }
    for( int i = 0; i < ticks.length; i++ )
    {
      final int tickPos = axis.numericToScreen( ticks[i] ) - axis.getScreenOffset();

      if( tickPos < screen.x || tickPos > screen.x + screen.width )
        continue;
      final int tickStart = screen.y+getLineStyle().getWidth();
      final int tickEnd = tickStart + getTickLength();
      final int oldAlias = gc.getAntialias();
      gc.setAntialias( SWT.OFF );
      // gc.setForeground( new Color(gc.getDevice(), new RGB(255,0,0)) );
      gc.drawLine( tickPos, tickStart, tickPos, tickEnd );
      gc.setAntialias( oldAlias );
      // draw Ticklabel

      labelRenderer.getTitleTypeBean().setLabel( getLabelCreator().getLabel( ticks, i, axis.getNumericRange() ) );
      final Rectangle textSize = labelRenderer.getSize();
      // hide cut
      if( isHideCut() )
      {
        if( isIntervallLabeledTick() || (tickPos + textSize.x > screen.x && tickPos + textSize.x + textSize.width < screen.width) )
          labelRenderer.paint( gc, new Rectangle( tickPos, tickEnd, tickDistance, -1 ) );
        // else
        // labelRenderer.paint( gc, new Rectangle( tickPos, (getLineStyle().getWidth() + getGap() + getTickLength() +
// inset.top), tickDistance, -1 ) );
      }
      else
      {
        // if( tickPos + textSize.x > -inset.left && tickPos + textSize.x + textSize.width < axis.getScreenHeight() +
// inset.right )
        labelRenderer.paint( gc, new Rectangle( tickPos, tickEnd, tickDistance, -1 ) );
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
    final TitleTypeBean[] axisLabels = axis.getLabels();
    boolean labelEmpty = axisLabels.length == 0;
    if( !labelEmpty )
    {
      for( final TitleTypeBean titleType : axisLabels )
      {
        if( titleType.getText().trim().length() > 0 )
        {
          labelEmpty = false;
          break;
        }
      }
    }
    int width = 0;
    // final int gap = getGap();
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
        labelsWidth = Math.max( labelsWidth, labelRenderer.getSize().height );
      }
      width += labelsWidth;
    }

    width += tls.y;
    width += getAxisConfig().axisInsets.bottom;
    width += getAxisConfig().axisInsets.top;
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
  public Double[] getTicks( final IAxis axis, final GC gc )
  {
    final Point tickLabelSize = calcTickLabelSize( axis );
    final Double[] ticks = getTickCalculator().calcTicks( gc, axis, getMinTickInterval(), tickLabelSize );
    return ticks;
  }

  public boolean isHideCut( )
  {
    return getAxisConfig().hideCut;
  }

  @Override
  public void paint( final GC gc, final IAxis axis, final Rectangle screen )
  {
    final Rectangle axisRect = RectangleUtils.inflateRect( screen, getAxisConfig().axisInsets );
    if( screen.width > 0 && screen.height > 0 && axis.isVisible() )
    {
      final int oldAlias = gc.getAntialias();
      gc.setAntialias( SWT.OFF );
      getLineStyle().apply( gc );

      final int lineY = axisRect.y + (getLineStyle().getWidth() / 2);

      gc.drawLine( 0, lineY, axis.getScreenHeight(), lineY );
      gc.setAntialias( oldAlias );

      drawTicks( gc, axisRect, axis, getTicks( axis, gc ) );
      final IChartLabelRenderer labelRenderer = getAxisLabelRenderer();
      for( final TitleTypeBean title : axis.getLabels() )
      {
        labelRenderer.setTitleTypeBean( title );
        final Rectangle textsize = labelRenderer.getSize();
        labelRenderer.paint( gc, new Rectangle( axisRect.x, axisRect.y + axisRect.height - textsize.height, axisRect.width, textsize.height ) );
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
