package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.awt.Insets;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;
import de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer;

/**
 * @author alibu
 */
public abstract class AbstractGenericAxisRenderer implements IAxisRenderer
{
  private final String m_id;

  private IChartLabelRenderer m_axisLabelRender = null;

  private IChartLabelRenderer m_tickLabelRender = null;

  private final AxisRendererConfig m_axisConfig;

  /**
   * Hashmap to store arbitrary key value pairs
   */
  /**
   * @deprecated
   */
  @Deprecated
  private final Map<String, Object> m_data = new HashMap<>();

  public AbstractGenericAxisRenderer( final String id, final AxisRendererConfig axisConfig )
  {
    m_id = id;
    m_axisConfig = axisConfig;
  }

  public AbstractGenericAxisRenderer( final String id, final IChartLabelRenderer axisLabelRenderer, final IChartLabelRenderer tickLabelRenderer, final AxisRendererConfig axisConfig )
  {
    m_id = id;
    m_axisConfig = axisConfig;
    m_axisLabelRender = axisLabelRenderer;
    m_tickLabelRender = tickLabelRenderer;
  }

  public AbstractGenericAxisRenderer( final String id, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final ILineStyle axisLineStyle, final ITextStyle labelStyle, final ILineStyle tickLineStyle, final ITextStyle tickLabelStyle )
  {
    this( id, tickLength, tickLabelInsets, labelInsets, gap, axisLineStyle, labelStyle, tickLineStyle, tickLabelStyle, 0 );

  }

  public AbstractGenericAxisRenderer( final String id, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final ILineStyle axisLineStyle, final ITextStyle labelStyle, final ILineStyle tickLineStyle, final ITextStyle tickLabelStyle, final int borderSize )
  {
    m_id = id;
    m_axisConfig = new AxisRendererConfig();
    m_axisConfig.tickLength = tickLength;
    m_axisConfig.axisLineStyle = axisLineStyle;
    m_axisConfig.tickLineStyle = tickLineStyle;
    m_axisConfig.axisInsets = new Insets( gap, 0, borderSize, 0 );

    setTickLabelStyle( tickLabelStyle );
    setTickLabelInsets( tickLabelInsets );
    setLabelStyle( labelStyle );
    setAxisLabelInsets( labelInsets );
  }

  @Override
  public void dispose( )
  {
  }

  /**
   * draws a given text at the position using special fontData
   */
  protected void drawText( final GC gc, final String text, final int x, final int y, final ITextStyle style )
  {
    style.apply( gc );
    // BUG in SWT (linux): Wenn Font-Größe geändert wird, besitzt der Hintergrund immer noch die Größe
    // des alten Texts - funktioniert aber komischerweise, wenn man den TextExtent nochmal anfragt
    getTextExtent( gc, text, style );
    gc.drawText( text, x, y );
  }

  public AxisRendererConfig getAxisConfig( )
  {
    return m_axisConfig;
  }

  public final IChartLabelRenderer getAxisLabelRenderer( )
  {
    return m_axisLabelRender;
  }

  @Override
  public Object getData( final String id )
  {
    return m_data.get( id );
  }

  @Deprecated
  public int getGap( )
  {
    return m_axisConfig.axisInsets.top;
  }

  @Override
  public String getId( )
  {
    return m_id;
  }

  public Insets getLabelInsets( )
  {
    if( getAxisLabelRenderer() == null )
      return null;
    return getAxisLabelRenderer().getTitleTypeBean().getInsets();
  }

  public ITextStyle getLabelStyle( )
  {
    if( getAxisLabelRenderer() == null )
      return null;
    return getAxisLabelRenderer().getTitleTypeBean().getTextStyle();
  }

  @Override
  public ILineStyle getLineStyle( )
  {
    return m_axisConfig.axisLineStyle;
  }

  protected abstract Point getTextExtent( GC gcw, Number value, ITextStyle style, Format format, IDataRange<Number> range );

  protected Point getTextExtent( final GC gc, final String value, final ITextStyle style )
  {
    style.apply( gc );

    return gc.textExtent( value == null ? StringUtils.EMPTY : value );
  }

  public Insets getTickLabelInsets( )
  {
    if( getTickLabelRenderer() == null )
      return getAxisConfig().tickLabelInsets;
    return getTickLabelRenderer().getTitleTypeBean().getInsets();
  }

  public final IChartLabelRenderer getTickLabelRenderer( )
  {
    return m_tickLabelRender;
  }

  public ITextStyle getTickLabelStyle( )
  {
    if( getTickLabelRenderer() == null )
      return StyleUtils.getDefaultTextStyle();
    return getTickLabelRenderer().getTitleTypeBean().getTextStyle();
  }

  public int getTickLength( )
  {
    return m_axisConfig.tickLength;
  }

  public ILineStyle getTickLineStyle( )
  {
    return m_axisConfig.tickLineStyle;
  }

  public void setAxisLabelInsets( final Insets insets )
  {
    if( getAxisLabelRenderer() == null )
      return;
    getAxisLabelRenderer().getTitleTypeBean().setInsets( insets );
  }

  @Override
  public void setData( final String id, final Object data )
  {
    m_data.put( id, data );
  }

  @Override
  public void setLabelStyle( final ITextStyle style )
  {
    if( getAxisLabelRenderer() == null )
      return;
    getAxisLabelRenderer().getTitleTypeBean().setTextStyle( style );
  }

  public void setTickLabelInsets( final Insets insets )
  {
    if( getTickLabelRenderer() == null )
      return;
    getTickLabelRenderer().getTitleTypeBean().setInsets( insets );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer#setTickLabelStyle(de.openali.odysseus.chart.framework.model.style.ITextStyle)
   */
  @Override
  public void setTickLabelStyle( final ITextStyle style )
  {
    if( getTickLabelRenderer() == null )
      return;
    getTickLabelRenderer().getTitleTypeBean().setTextStyle( style );
  }

}
