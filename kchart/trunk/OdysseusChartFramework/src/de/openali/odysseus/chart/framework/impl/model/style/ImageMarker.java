package de.openali.odysseus.chart.framework.impl.model.style;

import java.io.File;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;

public class ImageMarker extends AbstractMarker
{

	private final File m_file;
	private final ImageData m_id;

	public ImageMarker(File imgFile)
	{
		m_file = imgFile;

		ImageLoader il = new ImageLoader();
		ImageData[] ids = il.load(m_file.getAbsolutePath());
		m_id = ids[0];
	}

	public void paint(GC gc, Point pos, int width, int height)
	{
		Image img = new Image(gc.getDevice(), m_id.scaledTo(width, height));
		gc.fillRectangle(pos.x, pos.y, width, height);
		gc.drawImage(img, pos.x, pos.y);
		gc.drawRectangle(pos.x, pos.y, width, height);
		img.dispose();
	}

	public void dispose()
	{

	}

}
