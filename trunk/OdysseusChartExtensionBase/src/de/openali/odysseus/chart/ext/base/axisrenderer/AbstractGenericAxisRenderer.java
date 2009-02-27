package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.awt.Insets;
import java.text.Format;
import java.util.HashMap;

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
  private final HashMap<String, Object> m_data = new HashMap<String, Object>();

  private final HashMap<IAxis, Number[]> m_tickMap = new HashMap<IAxis, Number[]>();

  private ILineStyle m_axisLineStyle;

  private ITextStyle m_labelStyle;

  private ITextStyle m_tickLabelStyle;

  private ILineStyle m_tickLineStyle;

  public AbstractGenericAxisRenderer( final String id, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, ILineStyle axisLineStyle, ITextStyle labelStyle, ILineStyle tickLineStyle, ITextStyle tickLabelStyle )
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

  protected Point getTextExtent( GC gc, final String value, ITextStyle style )
  {
    style.apply( gc );
    final Point point = gc.textExtent( value );
    return point;
  }

  /**
   * draws a given text at the position using special fontData
   */
  protected void drawText( GC gc, String text, int x, int y, ITextStyle style )
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

  public void setTickMapElement( IAxis axis, Number[] ticks )
  {
    m_tickMap.put( axis, ticks );
  }

  protected Number[] getTickMapElement( IAxis axis )
  {
    Number[] tickMap = m_tickMap.get( axis );
    return tickMap;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#setData()
   */
  public void setData( String id, Object data )
  {
    m_data.put( id, data );
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getData()
   */
  public Object getData( String id )
  {
    return m_data.get( id );
  }

  public String getId( )
  {
    return m_id;
  }

  @SuppressWarnings("unchecked")
  public void invalidateTicks( IAxis axis )
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
