package de.openali.odysseus.chart.framework.model.figure.impl;

import org.eclipse.swt.graphics.GC;

import de.openali.odysseus.chart.framework.model.figure.IFigure;
import de.openali.odysseus.chart.framework.model.figure.IPaintable;

public class MultiFigure implements IPaintable
{

	@SuppressWarnings("unchecked")
	private final IFigure[] m_figures;

	@SuppressWarnings("unchecked")
	public MultiFigure(IFigure[] figures)
	{
		m_figures = figures;
	}

	@SuppressWarnings("unchecked")
  public void paint(GC gc)
	{
		for (IFigure figure : m_figures)
		{
			figure.paint(gc);
		}

	}

}
