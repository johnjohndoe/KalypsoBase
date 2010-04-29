package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;

public class LineStyle extends AbstractStyle implements ILineStyle
{
  private LINEJOIN m_lineJoin;

  private LINECAP m_lineCap;

  private RGB m_rgb;

  private int m_width;

  private float m_dashOffset;

  private float[] m_dashArray;

  private int m_miterLimit;

  public LineStyle( int width, RGB rgb, int alpha, float dashOffset, float[] dashArray, LINEJOIN lineJoin, LINECAP lineCap, int miterLimit, boolean isVisible )
  {
    setWidth( width );
    setColor( rgb );
    setAlpha( alpha );
    setDash( dashOffset, dashArray );
    setLineJoin( lineJoin );
    setLineCap( lineCap );
    setMiterLimit( miterLimit );
    setVisible( isVisible );
  }

  /**
   * returns a deep copy of the object
   */
  public LineStyle copy( )
  {
    LineStyle lineStyle = new LineStyle( getWidth(), getColor(), getAlpha(), getDashOffset(), getDashArray(), getLineJoin(), getLineCap(), getMiterLimit(), isVisible() );
    lineStyle.setTitle( getTitle() );
    return lineStyle;
  }

  /*
   * (non-Javadoc)
   * @see
   * de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#setLineJoin(de.openali.odysseus.chart.framework
   * .impl.model.style.StyleConstants.LINEJOIN)
   */
  public void setLineJoin( LINEJOIN join )
  {
    m_lineJoin = join;
  }

  /*
   * (non-Javadoc)
   * @see
   * de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#setLineCap(de.openali.odysseus.chart.framework.
   * impl.model.style.StyleConstants.LINECAP)
   */
  public void setLineCap( LINECAP cap )
  {
    m_lineCap = cap;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#setColor(org.eclipse.swt.graphics.RGB)
   */
  public void setColor( RGB rgb )
  {
    m_rgb = rgb;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#setDash(int, int[])
   */
  public void setDash( float dashOffset, float[] dashArray )
  {
    m_dashOffset = dashOffset;
    /**
     * INTERESTING: JVM (1.5, 1.6) crashes on Windows (2000, XP), if LineAttributes' dashArray is empty array
     */
    if( (dashArray != null) && (dashArray.length == 0) )
      m_dashArray = null;
    else
      m_dashArray = dashArray;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#setWidth(int)
   */
  public void setWidth( int width )
  {
    m_width = width;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#setMiterLimit(int)
   */
  public void setMiterLimit( int miterLimit )
  {
    m_miterLimit = miterLimit;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#apply(org.eclipse.swt.graphics.GC)
   */
  public void apply( GC gc )
  {

    gc.setForeground( OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( gc.getDevice(), m_rgb ) );

    gc.setAlpha( getAlpha() );

    int lineCap = m_lineCap.toSWT();

    int lineJoin = m_lineJoin.toSWT();
    LineAttributes la = new LineAttributes( m_width, lineCap, lineJoin, SWT.LINE_CUSTOM, m_dashArray, m_dashOffset, m_miterLimit );
    gc.setLineAttributes( la );
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#dispose()
   */
  public void dispose( )
  {

  }

  public int getWidth( )
  {
    return m_width;
  }

  public RGB getColor( )
  {
    return m_rgb;
  }

  public float[] getDashArray( )
  {
    return m_dashArray;
  }

  public float getDashOffset( )
  {
    return m_dashOffset;
  }

  public LINECAP getLineCap( )
  {
    return m_lineCap;
  }

  public LINEJOIN getLineJoin( )
  {
    return m_lineJoin;
  }

  public int getMiterLimit( )
  {
    return m_miterLimit;
  }

}
