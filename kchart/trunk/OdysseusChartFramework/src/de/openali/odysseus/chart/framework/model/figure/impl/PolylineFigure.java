package de.openali.odysseus.chart.framework.model.figure.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.impl.LineStyle;
import de.openali.odysseus.chart.framework.util.FigureUtilities;
import de.openali.odysseus.chart.framework.util.StyleUtils;

public class PolylineFigure extends AbstractFigure<ILineStyle>
{

  private Point[] m_points = new Point[] { new Point( 10, 10 ), new Point( 100, 100 ) };

  @Override
  public void paintFigure( GC gc )
  {
    if( m_points != null )
    {
      // if too many points are set, swt crashes on GTK; for GTK, 500
      // seems ok
      int limit = 500;

      if( m_points.length > limit )
      {
        for( int j = 0; j < m_points.length; j += limit - 1 )
        {
          int newLength = Math.min( limit, m_points.length - j );
          Point[] limitedPoints = new Point[newLength];
          System.arraycopy( m_points, j, limitedPoints, 0, newLength );
          int[] path = FigureUtilities.pointArrayToIntArray( limitedPoints );
          gc.drawPolyline( path );
        }
      }
      else
      {
        int[] path = FigureUtilities.pointArrayToIntArray( m_points );
        gc.drawPolyline( path );
      }
    }
  }

  public void setPoints( Point[] points )
  {
    m_points = points;
  }

  /**
   * returns the Style if it is set correctly; otherwise returns the default point style
   */
  @Override
  public ILineStyle getStyle( )
  {
    ILineStyle style = super.getStyle();
    if( style != null )
    {
      return style;
    }
    else
    {
      return StyleUtils.getDefaultStyle( LineStyle.class );
    }
  }

}
