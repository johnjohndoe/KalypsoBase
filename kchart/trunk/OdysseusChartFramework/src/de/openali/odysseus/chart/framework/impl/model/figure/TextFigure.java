package de.openali.odysseus.chart.framework.impl.model.figure;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.impl.model.style.TextStyle;
import de.openali.odysseus.chart.framework.impl.util.FigureUtilities;
import de.openali.odysseus.chart.framework.model.style.IStyle;

public class TextFigure extends AbstractFigure<TextStyle>
{

	private Point m_centerPos;
	private String m_text = "";

	/**
	 * 
	 * @param pos
	 *            center position of the figure
	 */
	public void setCenterPosition(Point pos)
	{
		m_centerPos = pos;
	}

	public void setText(String text)
	{
		m_text = text;
	}

	@Override
	public void paintFigure(GC gc)
	{
		IStyle style = getStyle();
		if (style != null && m_centerPos != null)
		{
			TextStyle ts = (TextStyle) style;
			style.apply(gc);

			Point textExtent = gc.textExtent(m_text);
			Point leftTopPos = FigureUtilities.centerToLeftTo(m_centerPos, textExtent.x, textExtent.y);

			if (m_text != null)
				gc.drawText(m_text, leftTopPos.x, leftTopPos.y);
		}

	}

	/**
	 * returns the Style if it is set correctly; otherwise returns the default
	 * point style
	 */
	@Override
	public TextStyle getStyle()
	{
		TextStyle style = super.getStyle();
		if (style != null)
			return style;
		else
			return FigureUtilities.getDefaultStyle(TextStyle.class);
	}
}
