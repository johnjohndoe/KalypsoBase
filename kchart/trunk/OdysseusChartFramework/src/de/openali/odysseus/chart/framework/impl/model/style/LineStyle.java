package de.openali.odysseus.chart.framework.impl.model.style;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;

public class LineStyle extends AbstractStyle implements ILineStyle
{
	private LINEJOIN m_lineJoin;
	private LINECAP m_lineCap;
	private RGB m_rgb;
	private int m_width;
	private Color m_color;
	private int m_dashOffset;
	private float[] m_dashArray;
	private int m_miterLimit;

	public LineStyle( int width, RGB rgb, int alpha, int dashOffset, int[] dashArray, LINEJOIN lineJoin, LINECAP lineCap, int miterLimit, boolean isVisible )
	{
		setWidth(width);
		setColor(rgb);
		setAlpha(alpha);
		setDash(dashOffset, dashArray);
		setLineJoin(lineJoin);
		setLineCap(lineCap);
		setMiterLimit(miterLimit);
		setVisible(isVisible);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#setLineJoin(de.openali.odysseus.chart.framework.impl.model.style.StyleConstants.LINEJOIN)
	 */
	public void setLineJoin(LINEJOIN join)
	{
		m_lineJoin = join;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#setLineCap(de.openali.odysseus.chart.framework.impl.model.style.StyleConstants.LINECAP)
	 */
	public void setLineCap(LINECAP cap)
	{
		m_lineCap = cap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#setColor(org.eclipse.swt.graphics.RGB)
	 */
	public void setColor(RGB rgb)
	{
		m_rgb = rgb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#setDash(int,
	 *      int[])
	 */
	public void setDash(int dashOffset, int[] dashArray)
	{
		m_dashOffset = dashOffset;
		// in float-Array umwandeln
		if (dashArray != null)
		{
			m_dashArray = new float[dashArray.length];
			for (int i = 0; i < dashArray.length; i++)
			{
				m_dashArray[i] = dashArray[i];
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#setWidth(int)
	 */
	public void setWidth(int width)
	{
		m_width = width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#setMiterLimit(int)
	 */
	public void setMiterLimit(int miterLimit)
	{
		m_miterLimit = miterLimit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#apply(org.eclipse.swt.graphics.GC)
	 */
	public void apply(GC gc)
	{
		if (m_color != null && !m_color.isDisposed())
		{
			m_color.dispose();
		}
		m_color = new Color(gc.getDevice(), m_rgb);
		gc.setForeground(m_color);

		gc.setAlpha(getAlpha());

		int lineCap = m_lineCap.toSWT();

		int lineJoin = m_lineJoin.toSWT();

		LineAttributes la = new LineAttributes(m_width, lineCap, lineJoin, SWT.LINE_CUSTOM, m_dashArray, m_dashOffset, m_miterLimit);
		gc.setLineAttributes(la);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.ILineStyle#dispose()
	 */
	public void dispose()
	{
		if (m_color != null && !m_color.isDisposed())
		{
			m_color.dispose();
		}
	}

	public int getWidth()
	{
		return m_width;
	}
}
