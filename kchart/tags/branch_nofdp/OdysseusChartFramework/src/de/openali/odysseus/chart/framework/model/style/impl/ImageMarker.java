package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

public class ImageMarker extends AbstractMarker
{

	private final ImageData m_id;

	public ImageMarker(ImageData id)
	{
		m_id = id;
	}

	public void paint(GC gc, Point pos, int width, int height)
	{
		Image img = new Image(gc.getDevice(), m_id.scaledTo(width, height));
		gc.fillRectangle(pos.x, pos.y, width, height);
		gc.drawImage(img, pos.x, pos.y);
		gc.drawRectangle(pos.x, pos.y, width, height);
		img.dispose();
	}

	public ImageMarker copy()
	{
		return new ImageMarker((ImageData) m_id.clone());
	}

}
