package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.awt.Insets;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * @author alibu
 */
public abstract class AbstractGenericAxisRenderer implements IAxisRenderer
{
  private final int m_tickLength;

  private final int m_gap;

  private final Insets m_labelInsets;

  private final Insets m_tickLabelInsets;

  private final String m_id;

  /**
   * Hashmap to store arbitrary key value pairs
   */
  private final Map<String, Object> m_data = new HashMap<String, Object>();

  private final Map<IAxis, Number[]> m_tickMap = new HashMap<IAxis, Number[]>();

  private ILineStyle m_axisLineStyle;

  private ITextStyle m_labelStyle;

  private ITextStyle m_tickLabelStyle;

  private ILineStyle m_tickLineStyle;

  public AbstractGenericAxisRenderer( final String id, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final ILineStyle axisLineStyle, final ITextStyle labelStyle, final ILineStyle tickLineStyle, final ITextStyle tickLabelStyle )
  {
    m_id = id;
    m_tickLength = tickLength;
    m_tickLabelInsets = tickLabelInsets;
    m_labelInsets = labelInsets;
    m_gap = gap;
    m_axisLineStyle = axisLineStyle;
    m_labelStyle = labelStyle;
    m_tickLabelStyle = tickLabelStyle;
    m_tickLineStyle = tickLineStyle;
  }

  protected abstract Point getTextExtent( GC gcw, Number value, ITextStyle style, Format format, IDataRange<Number> range );

  protected Point getTextExtent( final GC gc, final String value, final ITextStyle style )
  {
    style.apply( gc );
    final Point point = gc.textExtent( value == null ? "" : value );
    return point;
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

  public int getTickLength( )
  {
    return m_tickLength;
  }

  public int getGap( )
  {
    return m_gap;
  }

  public Insets getLabelInsets( )
  {
    return m_labelInsets;
  }

  public Insets getTickLabelInsets( )
  {
    return m_tickLabelInsets;
  }

  public void setTickMapElement( final IAxis axis, final Number[] ticks )
  {
    m_tickMap.put( axis, ticks );
  }

  protected Number[] getTickMapElement( final IAxis axis )
  {
    final Number[] tickMap = m_tickMap.get( axis );
    return tickMap;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#setData()
   */
  public void setData( final String id, final Object data )
  {
    m_data.put( id, data );
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getData()
   */
  public Object getData( final String id )
  {
    return m_data.get( id );
  }

  public String getId( )
  {
    return m_id;
  }

  public void invalidateTicks( final IAxis axis )
  {
    m_tickMap.put( axis, null );
  }

  public void dispose( )
  {
  }

  public ILineStyle getTickLineStyle( )
  {
    if( m_tickLineStyle == null )
      m_tickLineStyle = StyleUtils.getDefaultLineStyle();
    return m_tickLineStyle;
  }

  public ILineStyle getLineStyle( )
  {
    if( m_axisLineStyle == null )
      m_axisLineStyle = StyleUtils.getDefaultLineStyle();
    return m_axisLineStyle;
  }

  public ITextStyle getLabelStyle( )
  {
    if( m_labelStyle == null )
      m_labelStyle = StyleUtils.getDefaultTextStyle();
    return m_labelStyle;
  }

  public ITextStyle getTickLabelStyle( )
  {
    if( m_tickLabelStyle == null )
      m_tickLabelStyle = StyleUtils.getDefaultTextStyle();
    return m_tickLabelStyle;
  }

}
