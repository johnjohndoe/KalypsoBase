package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.OdysseusChartFramework;
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

  public LineStyle( final int width, final RGB rgb, final int alpha, final float dashOffset, final float[] dashArray, final LINEJOIN lineJoin, final LINECAP lineCap, final int miterLimit, final boolean isVisible )
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
  @Override
  public LineStyle clone( )
  {
    final LineStyle lineStyle = new LineStyle( getWidth(), getColor(), getAlpha(), getDashOffset(), getDashArray(), getLineJoin(), getLineCap(), getMiterLimit(), isVisible() );
    // lineStyle.setTitle( getTitle() );
    return lineStyle;
  }

  @Override
  public void setLineJoin( final LINEJOIN join )
  {
    m_lineJoin = join;
  }

  @Override
  public void setLineCap( final LINECAP cap )
  {
    m_lineCap = cap;
  }

  @Override
  public void setColor( final RGB rgb )
  {
    m_rgb = rgb;
  }

  @Override
  public void setDash( final float dashOffset, final float[] dashArray )
  {
    m_dashOffset = dashOffset;
    /**
     * INTERESTING: JVM (1.5, 1.6) crashes on Windows (2000, XP), if LineAttributes' dashArray is empty array
     */
    if( dashArray != null && dashArray.length == 0 )
      m_dashArray = null;
    else
      m_dashArray = dashArray;
  }

  @Override
  public void setWidth( final int width )
  {
    m_width = width;
  }

  @Override
  public void setMiterLimit( final int miterLimit )
  {
    m_miterLimit = miterLimit;
  }

  @Override
  public void apply( final GC gc )
  {
    gc.setForeground( OdysseusChartFramework.getDefault().getColorRegistry().getResource( gc.getDevice(), m_rgb ) );

    gc.setAlpha( getAlpha() );

    final int lineCap = m_lineCap.toSWT();
    final int lineJoin = m_lineJoin.toSWT();

    if( m_dashArray == null )
    {
      final LineAttributes la = new LineAttributes( m_width, lineCap, lineJoin );
      gc.setLineAttributes( la );
    }
    else
    {
      final LineAttributes la = new LineAttributes( m_width, lineCap, lineJoin, SWT.LINE_CUSTOM, m_dashArray, m_dashOffset, m_miterLimit );
      gc.setLineAttributes( la );
    }
  }

  @Override
  public int getWidth( )
  {
    return m_width;
  }

  @Override
  public RGB getColor( )
  {
    return m_rgb;
  }

  @Override
  public float[] getDashArray( )
  {
    return m_dashArray;
  }

  @Override
  public float getDashOffset( )
  {
    return m_dashOffset;
  }

  @Override
  public LINECAP getLineCap( )
  {
    return m_lineCap;
  }

  @Override
  public LINEJOIN getLineJoin( )
  {
    return m_lineJoin;
  }

  @Override
  public int getMiterLimit( )
  {
    return m_miterLimit;
  }

}
