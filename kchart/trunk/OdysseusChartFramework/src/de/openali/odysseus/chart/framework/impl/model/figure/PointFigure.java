package de.openali.odysseus.chart.framework.impl.model.figure;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.impl.model.style.PointStyle;
import de.openali.odysseus.chart.framework.impl.util.FigureUtilities;
import de.openali.odysseus.chart.framework.impl.util.StyleUtils;
import de.openali.odysseus.chart.framework.model.style.IMarker;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyle;

public class PointFigure extends AbstractFigure<IPointStyle> {

	private Point m_leftTopPos = new Point(0, 0);

	/**
	 * 
	 * @param pos
	 *            center position of the figure
	 */
	public void setCenterPosition(Point pos) {
		IPointStyle style = getStyle();
		setLeftTopPoint(FigureUtilities.centerToLeftTo(pos, style.getWidth(),
				style.getHeight()));
	}

	/**
	 * 
	 * @param point
	 *            left top position of the figure
	 */
	public void setLeftTopPoint(Point pos) {
		m_leftTopPos = pos;
	}

	@Override
	public void paintFigure(GC gc) {
		IStyle style = getStyle();
		if (style != null && m_leftTopPos != null) {
			IPointStyle ps = (IPointStyle) style;
			IMarker marker = ps.getMarker();
			if (marker == null) {
				marker = IDefaultStyles.DEFAULT_MARKER;
			}
			marker.paint(gc, m_leftTopPos, getStyle().getWidth(), getStyle()
					.getHeight());
		}

	}

	/**
	 * returns the Style if it is set correctly; otherwise returns the default
	 * point style
	 */
	@Override
	public IPointStyle getStyle() {
		IPointStyle style = super.getStyle();
		if (style != null) {
			return style;
		} else {
			return StyleUtils.getDefaultStyle(PointStyle.class);
		}
	}

	public void dispose() {
		// nothing to do;
	}

}
