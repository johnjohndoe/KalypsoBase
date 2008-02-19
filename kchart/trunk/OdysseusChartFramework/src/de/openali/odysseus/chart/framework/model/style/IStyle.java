package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.GC;

public interface IStyle
{
	public void apply(GC gc);

	public void setVisible(boolean isVisible);

	public boolean isVisible();

	public void dispose();
}
