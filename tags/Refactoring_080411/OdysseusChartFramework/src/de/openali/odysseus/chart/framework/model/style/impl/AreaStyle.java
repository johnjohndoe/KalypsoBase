package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.GC;

import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.IFill;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

public class AreaStyle extends AbstractStyle implements IAreaStyle
{

	private ILineStyle m_stroke;
	private IFill m_fill;

	public AreaStyle(ILineStyle outline, IFill fill, int alpha, ILineStyle stroke, boolean isVisible)
	{
		setFill(fill);
		setAlpha(alpha);
		setVisible(isVisible);
		setStroke(stroke);
	}

	/**
	 * copy constructor
	 * 
	 * @param style
	 *            template style
	 */
	public IAreaStyle copy()
	{
		return new AreaStyle(getStroke(), getFill().copy(), getAlpha(), getStroke().copy(), isVisible());
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

	public void apply(GC gc)
	{
		if (m_stroke != null)
		{
			m_stroke.apply(gc);
		}

		int alpha = getAlpha();
		gc.setAlpha(alpha);

		if (m_fill != null)
		{
			m_fill.apply(gc);
		}
	}

	public void dispose()
	{
		if (m_stroke != null)
		{
			m_stroke.dispose();
		}
		if (m_fill != null)
		{
			m_fill.dispose();
		}
	}

	public void setFill(IFill fill)
	{
		m_fill = fill;
	}

	public IFill getFill()
	{
		return m_fill;
	}

	public ILineStyle getStroke()
	{
		return m_stroke;
	}

}
