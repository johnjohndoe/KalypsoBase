package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IMarker;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

public class PointStyle extends AbstractStyle implements IPointStyle
{
	private RGB m_inlineRGB;
	private IMarker m_marker;
	private int m_width;
	private int m_height;
	private ILineStyle m_stroke;
	private Color m_inlineColor;

	public PointStyle(ILineStyle outline, int width, int height, int alpha, RGB inlineColor, IMarker marker, boolean isVisible)
	{
		setStroke(outline);
		setWidth(width);
		setHeight(height);
		setInlineColor(inlineColor);
		setMarker(marker);
		setAlpha(alpha);
		setVisible(isVisible);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setOutline(de.openali.odysseus.chart.framework.impl.model.style.ILineStyle)
	 */
	public void setStroke(ILineStyle stroke)
	{
		m_stroke = stroke;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setWidth(int)
	 */
	public void setWidth(int width)
	{
		m_width = width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setHeight(int)
	 */
	public void setHeight(int height)
	{
		m_height = height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#getWidth()
	 */
	public int getWidth()
	{
		return m_width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#getHeight()
	 */
	public int getHeight()
	{
		return m_height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setInlineColor(org.eclipse.swt.graphics.RGB)
	 */
	public void setInlineColor(RGB rgb)
	{
		m_inlineRGB = rgb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#setMarker(de.openali.odysseus.chart.framework.model.style.IMarker)
	 */
	public void setMarker(IMarker marker)
	{
		m_marker = marker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.IPointStyle#getMarker()
	 */
	public IMarker getMarker()
	{
		return m_marker;
	}

	public void apply(GC gc)
	{
		m_stroke.apply(gc);

		gc.setAlpha(getAlpha());

		if (m_inlineColor != null && !m_inlineColor.isDisposed())
		{
			m_inlineColor.dispose();
		}
		m_inlineColor = new Color(gc.getDevice(), m_inlineRGB);
		gc.setBackground(m_inlineColor);

	}

	public void dispose()
	{
		if (m_inlineColor != null && !m_inlineColor.isDisposed())
		{
			m_inlineColor.dispose();
		}
		if (m_stroke != null)
		{
			m_stroke.dispose();
		}
		if (m_marker != null)
		{
			m_marker.dispose();
		}
	}

	public RGB getInlineColor()
	{
		return m_inlineRGB;
	}

	public ILineStyle getStroke()
	{
		return m_stroke;
	}

	public IPointStyle copy()
	{
		return new PointStyle(getStroke().copy(), getWidth(), getHeight(), getAlpha(), getInlineColor(), getMarker().copy(), isVisible());
	}
}
