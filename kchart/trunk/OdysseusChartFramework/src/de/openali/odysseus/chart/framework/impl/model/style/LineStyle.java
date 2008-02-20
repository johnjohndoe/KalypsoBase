package de.openali.odysseus.chart.framework.impl.model.style;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.impl.model.style.StyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.impl.model.style.StyleConstants.LINEJOIN;

public class LineStyle extends AbstractStyle
{
	private LINEJOIN m_lineJoin;
	private LINECAP m_lineCap;
	private RGB m_rgb;
	private int m_width;
	private Color m_color;
	private int m_dashOffset;
	private float[] m_dashArray;
	private int m_miterLimit;

	public LineStyle( int width, RGB rgb, int alpha, int dashOffset,
	        int[] dashArray, LINEJOIN lineJoin, LINECAP lineCap,
	        int miterLimit, boolean isVisible )
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

	public void setLineJoin(LINEJOIN join)
	{
		m_lineJoin = join;
	}

	public void setLineCap(LINECAP cap)
	{
		m_lineCap = cap;
	}

	public void setColor(RGB rgb)
	{
		m_rgb = rgb;
	}

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

	public void setWidth(int width)
	{
		m_width = width;
	}

	public void setMiterLimit(int miterLimit)
	{
		m_miterLimit = miterLimit;
	}

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

		LineAttributes la = new LineAttributes(m_width, lineCap, lineJoin,
		        SWT.LINE_CUSTOM, m_dashArray, m_dashOffset, m_miterLimit);
		gc.setLineAttributes(la);

	}

	public void dispose()
	{
		if (m_color != null && !m_color.isDisposed())
		{
			m_color.dispose();
		}
	}
}
