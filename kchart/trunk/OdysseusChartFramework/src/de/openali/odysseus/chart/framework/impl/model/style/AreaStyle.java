package de.openali.odysseus.chart.framework.impl.model.style;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

public class AreaStyle extends AbstractStyle
{

	private LineStyle m_outline;
	private RGB m_inlineRgb;
	private Color m_inlineColor;

	public AreaStyle(LineStyle outline, RGB inlineRGB, int alpha, boolean isVisible)
	{
		m_inlineRgb = inlineRGB;
		setAlpha(alpha);
		setVisible(isVisible);
	}

	public void setOutline(LineStyle outline)
	{
		m_outline = outline;
	}

	public void setInlineColor(RGB rgb)
	{
		m_inlineRgb = rgb;
	}

	public void apply(GC gc)
	{
		if (m_outline != null)
			m_outline.apply(gc);

		gc.setAlpha(getAlpha());

		if (m_inlineColor != null && !m_inlineColor.isDisposed())
		{
			m_inlineColor.dispose();
		}
		m_inlineColor = new Color(gc.getDevice(), m_inlineRgb);
		gc.setBackground(m_inlineColor);
	}

	public void dispose()
	{
		if (m_outline != null)
			m_outline.dispose();
		if (m_inlineColor != null && !m_inlineColor.isDisposed())
			m_inlineColor.dispose();
	}

}
