package de.openali.odysseus.chart.framework.impl.model.style;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

public class AreaStyle extends AbstractStyle implements IAreaStyle
{

	private ILineStyle m_stroke;
	private RGB m_inlineRgb;
	private Color m_inlineColor;

	public AreaStyle( ILineStyle outline, RGB inlineRGB, int alpha, ILineStyle stroke, boolean isVisible )
	{
		m_inlineRgb = inlineRGB;
		setAlpha(alpha);
		setVisible(isVisible);
		setStroke(stroke);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.IAreaStyle#setOutline(de.openali.odysseus.chart.framework.impl.model.style.ILineStyle)
	 */
	public void setStroke(ILineStyle stroke)
	{
		m_stroke = stroke;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.openali.odysseus.chart.framework.impl.model.style.IAreaStyle#setInlineColor(org.eclipse.swt.graphics.RGB)
	 */
	public void setInlineColor(RGB rgb)
	{
		m_inlineRgb = rgb;
	}

	public void apply(GC gc)
	{
		if (m_stroke != null)
		{
			m_stroke.apply(gc);
		}

		int alpha = getAlpha();
		gc.setAlpha(alpha);

		if (m_inlineColor != null && !m_inlineColor.isDisposed())
		{
			m_inlineColor.dispose();
		}
		m_inlineColor = new Color(gc.getDevice(), m_inlineRgb);
		gc.setBackground(m_inlineColor);
	}

	public void dispose()
	{
		if (m_stroke != null)
		{
			m_stroke.dispose();
		}
		if (m_inlineColor != null && !m_inlineColor.isDisposed())
		{
			m_inlineColor.dispose();
		}
	}

}
