package de.openali.odysseus.chart.framework.model.style.impl;

import java.util.HashMap;
import java.util.Map;

import de.openali.odysseus.chart.framework.model.style.IStyle;

public abstract class AbstractStyle implements IStyle
{

	private int m_alpha;
	private boolean m_isVisible;
	private String m_title;
	private final Map<String, Object> m_data = new HashMap<String, Object>();

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
		}
		else
		{
			m_alpha = alpha;
		}
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

	public void setTitle(String title)
	{
		m_title = title;
	}

	public String getTitle()
	{
		return m_title;
	}

	/**
	 * @see org.kalypso.chart.framework.model.layer.IChartLayer#setData()
	 */
	public void setData(String id, Object data)
	{
		m_data.put(id, data);
	}

	/**
	 * @see org.kalypso.chart.framework.model.layer.IChartLayer#getData()
	 */
	public Object getData(String id)
	{
		return m_data.get(id);
	}

}
