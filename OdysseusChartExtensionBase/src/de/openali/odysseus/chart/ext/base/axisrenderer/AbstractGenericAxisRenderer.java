package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.awt.Insets;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * @author alibu
 */
public abstract class AbstractGenericAxisRenderer implements IAxisRenderer
{

  private final Insets m_labelInsets;

  private final Insets m_tickLabelInsets;

  private final String m_id;

  /**
   * Hashmap to store arbitrary key value pairs
   */
  private final Map<String, Object> m_data = new HashMap<String, Object>();

  private ILineStyle m_axisLineStyle;

  private ITextStyle m_labelStyle;

  private ITextStyle m_tickLabelStyle;

  private ILineStyle m_tickLineStyle;

  public AbstractGenericAxisRenderer( final String id, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final ILineStyle axisLineStyle, final ITextStyle labelStyle, final ILineStyle tickLineStyle, final ITextStyle tickLabelStyle )
  {
    this( id, tickLength, tickLabelInsets, labelInsets, gap, axisLineStyle, labelStyle, tickLineStyle, tickLabelStyle, 0 );

  }

  public AbstractGenericAxisRenderer( final String id, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final ILineStyle axisLineStyle, final ITextStyle labelStyle, final ILineStyle tickLineStyle, final ITextStyle tickLabelStyle, final int borderSize )
  {
    m_id = id;
    m_data.put( IAxisConstants.TICK_LENGTH, tickLength );
    m_tickLabelInsets = tickLabelInsets;
    m_labelInsets = labelInsets;
    m_data.put( IAxisConstants.AXIS_GAP, gap );
    m_axisLineStyle = axisLineStyle;
    m_labelStyle = labelStyle;
    m_tickLabelStyle = tickLabelStyle;
    m_tickLineStyle = tickLineStyle;
    m_data.put( IAxisConstants.BORDER_SIZE, borderSize );

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
    // BUG in SWT (linux): Wenn Font-Gr��e ge�ndert wird, besitzt der Hintergrund immer noch die Gr��e
    // des alten Texts - funktioniert aber komischerweise, wenn man den TextExtent nochmal anfragt
    getTextExtent( gc, text, style );
    gc.drawText( text, x, y );
  }

  public int getBorderSize( )
  {
    return (Integer) m_data.get( IAxisConstants.BORDER_SIZE );
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getData()
   */
  @Override
  public Object getData( final String id )
  {
    return m_data.get( id );
  }

  public int getGap( )
  {
    return (Integer) m_data.get( IAxisConstants.AXIS_GAP );
  }

  @Override
  public String getId( )
  {
    return m_id;
  }

  public Insets getLabelInsets( )
  {
    return m_labelInsets;
  }

  public ITextStyle getLabelStyle( )
  {
    if( m_labelStyle == null )
      m_labelStyle = StyleUtils.getDefaultTextStyle();
    return m_labelStyle;
  }

  public ILineStyle getLineStyle( )
  {
    if( m_axisLineStyle == null )
      m_axisLineStyle = StyleUtils.getDefaultLineStyle();
    return m_axisLineStyle;
  }

  protected abstract Point getTextExtent( GC gcw, Number value, ITextStyle style, Format format, IDataRange<Number> range );

  protected Point getTextExtent( final GC gc, final String value, final ITextStyle style )
  {
    style.apply( gc );

    return gc.textExtent( value == null ? StringUtils.EMPTY : value );
  }

  public Insets getTickLabelInsets( )
  {
    return m_tickLabelInsets;
  }

  public ITextStyle getTickLabelStyle( )
  {
    if( m_tickLabelStyle == null )
      m_tickLabelStyle = StyleUtils.getDefaultTextStyle();
    return m_tickLabelStyle;
  }

  public int getTickLength( )
  {
    return (Integer) m_data.get( IAxisConstants.TICK_LENGTH );
  }

  public ILineStyle getTickLineStyle( )
  {
    if( m_tickLineStyle == null )
      m_tickLineStyle = StyleUtils.getDefaultLineStyle();
    return m_tickLineStyle;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#setData()
   */
  @Override
  public void setData( final String id, final Object data )
  {
    m_data.put( id, data );
  }

  @Override
  public void setLabelStyle( final ITextStyle style )
  {
    m_labelStyle = style;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer#setTickLabelStyle(de.openali.odysseus.chart.framework.model.style.ITextStyle)
   */
  @Override
  public void setTickLabelStyle( final ITextStyle style )
  {
    m_tickLabelStyle = style;

  }

}
