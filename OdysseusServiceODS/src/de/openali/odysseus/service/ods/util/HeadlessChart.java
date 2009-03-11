package de.openali.odysseus.service.ods.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author alibu this class contains all objects (e.g. shell) needed to keep a
 *         chart in the cache
 */
public class HeadlessChart
{

	private final Shell m_shell;

	private final ChartComposite m_chart;

	private final boolean m_isDisposed = false;

	public HeadlessChart( IChartModel chartModel, RGB bgRGB )
	{
		final Display d = DisplayHelper.getInstance().getDisplay();
		m_shell = new Shell(d);
		m_chart = new ChartComposite(m_shell, SWT.NONE, chartModel, bgRGB);
	}

	public void dispose()
	{
		if (m_shell != null && !m_shell.isDisposed())
		{
			m_shell.dispose();
		}
		if (m_chart != null && !m_chart.isDisposed())
		{
			m_chart.dispose();
		}
	}

	public ChartComposite getChart()
	{
		return m_chart;
	}

}
