package de.openali.odysseus.chart.framework.model.figure.impl;

import org.eclipse.swt.graphics.GC;

import de.openali.odysseus.chart.framework.model.figure.IFigure;
import de.openali.odysseus.chart.framework.model.figure.IPaintable;

public class MultiFigure implements IPaintable
{
  private final IFigure< ? >[] m_figures;

  public MultiFigure( final IFigure< ? >[] figures )
  {
    m_figures = figures;
  }

  @Override
  public void paint( final GC gc )
  {
    for( final IFigure< ? > figure : m_figures )
      figure.paint( gc );
  }

}