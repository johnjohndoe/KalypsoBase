package de.openali.odysseus.chart.framework.model.style.impl;

import java.util.HashMap;
import java.util.Map;

import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.util.StyleUtils;

public class StyleSet implements IStyleSet
{
	private final Map<String, IStyle> m_styleMap = new HashMap<String, IStyle>();

	public void addStyle(String id, IStyle style)
	{
		m_styleMap.put(id, style);
	}

	/**
	 * if no style is found, a default style is created, saved for later use and
	 * returned
	 */
	@SuppressWarnings("unchecked")
	public <T extends IStyle> T getStyle(String id, Class<T> styleClass)
	{
		IStyle style = m_styleMap.get(id);
		if (style == null)
		{
			Logger.logWarning(Logger.TOPIC_LOG_STYLE, "No style for id '" + id + "'. Needed " + styleClass + ". Returning default style");
			T myStyle = StyleUtils.getDefaultStyle(styleClass);
			m_styleMap.put(id, myStyle);
			return myStyle;
		}

		T castedStyle = null;
		try
		{
			castedStyle = (T) style;
		}
		catch (ClassCastException e)
		{
			Logger.logWarning(Logger.TOPIC_LOG_STYLE, "Wrong style type for id '" + id + "'. Expected " + styleClass + ", got " + style.getClass() + ". Using default style");
			return StyleUtils.getDefaultStyle(styleClass);
		}
		return castedStyle;
	}

	public void dispose()
	{
		for (IStyle style : m_styleMap.values())
		{
			style.dispose();
		}
	}

	public Map<String, IStyle> getStyles()
	{
		return m_styleMap;
	}

}
