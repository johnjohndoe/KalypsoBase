package de.openali.odysseus.chart.framework.impl.model.style;

import de.openali.odysseus.chart.framework.model.style.IStyle;

public abstract class AbstractStyle implements IStyle
{

	private int m_alpha;
	private boolean m_isVisible;

	/**
	 * 
	 * @param alpha
	 *            0 <= alpha <= 255
	 */
	public void setAlpha(int alpha)
	{
		if (alpha < 0 || alpha > 255)
		{
			m_alpha = 255;
		} else
			m_alpha = alpha;
	}

	public int getAlpha()
	{
		return m_alpha;
	}

	public void setVisible(boolean isVisible)
	{
		m_isVisible = isVisible;
	}

	public boolean isVisible()
	{
		return m_isVisible;
	}
}
