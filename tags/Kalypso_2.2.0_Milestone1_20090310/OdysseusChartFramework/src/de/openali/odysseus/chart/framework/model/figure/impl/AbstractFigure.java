package de.openali.odysseus.chart.framework.model.figure.impl;

import org.eclipse.swt.graphics.GC;

import de.openali.odysseus.chart.framework.model.figure.IFigure;
import de.openali.odysseus.chart.framework.model.style.IStyle;

public abstract class AbstractFigure<T_style extends IStyle> implements IFigure<T_style>
{

	private T_style m_style;

	public void setStyle(T_style ts)
	{
		m_style = ts;
	}

	public T_style getStyle()
	{
		return m_style;
	}

	public final void paint(GC gc)
	{
		if (m_style.isVisible())
		{
			m_style.apply(gc);
			paintFigure(gc);
		}
	}

	protected abstract void paintFigure(GC gc);

}
