package de.openali.odysseus.chart.framework.model.figure.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.impl.AreaStyle;
import de.openali.odysseus.chart.framework.util.FigureUtilities;
import de.openali.odysseus.chart.framework.util.StyleUtils;

public class PolygonFigure extends AbstractFigure<IAreaStyle>
{
  private Point[] m_points = null;

  @Override
  protected void paintFigure( final GC gc )
  {
    if( m_points == null )
      return;

    final int[] intArray = FigureUtilities.pointArrayToIntArray( m_points );
    gc.fillPolygon( intArray );
    gc.drawPolygon( intArray );
  }

  public void setPoints( final Point[] points )
  {
    m_points = points;
  }

  @Override
  public IAreaStyle getStyle( )
  {
    final IAreaStyle style = super.getStyle();

    if( style == null )
      return StyleUtils.getDefaultStyle( AreaStyle.class );

    return style;
  }
}