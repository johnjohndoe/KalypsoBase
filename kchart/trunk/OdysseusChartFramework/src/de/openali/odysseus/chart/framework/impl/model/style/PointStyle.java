package de.openali.odysseus.chart.framework.impl.model.style;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.style.IMarker;

public class PointStyle extends AbstractStyle
{
	private RGB m_inlineRGB;
	private IMarker m_marker;
	private int m_width;
	private int m_height;
	private LineStyle m_outline;
	private Color m_inlineColor;

	public PointStyle(LineStyle outline, int width, int height, int alpha, RGB inlineColor, IMarker marker, boolean isVisible)
	{
		setOutline(outline);
		setWidth(width);
		setHeight(height);
		setInlineColor(inlineColor);
		setMarker(marker);
		setAlpha(alpha);
		setVisible(isVisible);
	}

	public void setOutline(LineStyle outline)
	{
		m_outline = outline;
	}

	public void setWidth(int width)
	{
		m_width = width;
	}

	public void setHeight(int height)
	{
		m_height = height;
	}

	public int getWidth()
	{
		return m_width;
	}

	public int getHeight()
	{
		return m_height;
	}

	public void setInlineColor(RGB rgb)
	{
		m_inlineRGB = rgb;
	}

	public void setMarker(IMarker marker)
	{
		m_marker = marker;
	}

	public IMarker getMarker()
	{
		return m_marker;
	}

	public void apply(GC gc)
	{
		m_outline.apply(gc);

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
		if (m_outline != null)
			m_outline.dispose();
	}
}
