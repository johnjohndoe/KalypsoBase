package de.openali.odysseus.chart.framework.impl.model.figure;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.impl.model.style.LineStyle;
import de.openali.odysseus.chart.framework.impl.util.FigureUtilities;
import de.openali.odysseus.chart.framework.impl.util.StyleUtils;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

public class PolylineFigure extends AbstractFigure<ILineStyle>
{

	private Point[] m_points = new Point[] { new Point(10, 10), new Point(100, 100) };

	@Override
	public void paintFigure(GC gc)
	{
		if (m_points != null)
		{
			int[] path = FigureUtilities.pointArrayToIntArray(m_points);
			// if too many points are set, swt crashes
			if (m_points.length > 500)
			{
				for (int i = 0; i < m_points.length - 2; i++)
				{
					Point p1 = m_points[i];
					Point p2 = m_points[i + 1];
					gc.drawLine(p1.x, p1.y, p2.x, p2.y);
				}
			}
			else
			{
				gc.drawPolyline(path);
			}
		}
	}

	public void setPoints(Point[] points)
	{
		m_points = points;
	}

	/**
	 * returns the Style if it is set correctly; otherwise returns the default
	 * point style
	 */
	@Override
	public ILineStyle getStyle()
	{
		ILineStyle style = super.getStyle();
		if (style != null)
		{
			return style;
		}
		else
		{
			return StyleUtils.getDefaultStyle(LineStyle.class);
		}
	}
}
