package de.openali.odysseus.chart.framework.model.layer.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;

public abstract class LegendEntry implements ILegendEntry
{

	private final String m_description;
	private final IChartLayer m_parent;

	public LegendEntry( IChartLayer parent, String description )
	{
		m_parent = parent;
		m_description = description;
	}

	public String getDescription()
	{
		return m_description;
	}

	public abstract void paintSymbol(GC gc, Point size);

	public ImageData getSymbol(Point size)
	{
		Point realSize = computeSize(size);
		Image img = new Image(Display.getDefault(), realSize.x, realSize.y);
		GC gc = new GC(img);
		paintSymbol(gc, realSize);
		ImageData id = img.getImageData();
		gc.dispose();
		img.dispose();
		return id;
	}

	/**
	 * @param size
	 *            the preferred size
	 * @return the minimum size or null if any
	 */
	public Point computeSize(Point size)
	{
		Point neededSize = getMinimumSize();
		Point realSize = new Point(Math.max(neededSize.x, size.x), Math.max(neededSize.y, size.y));
		return realSize;
	}

	/**
	 * 
	 * implementations need to override this method if the symbol needs a
	 * special size;
	 * 
	 * @return Point (16, 16)
	 */
	public Point getMinimumSize()
	{
		return new Point(16, 16);
	}

	public IChartLayer getParentLayer()
	{
		return m_parent;
	}
}
