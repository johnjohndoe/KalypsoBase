package de.openali.odysseus.chart.framework.model.figure.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.TextStyle;
import de.openali.odysseus.chart.framework.util.FigureUtilities;
import de.openali.odysseus.chart.framework.util.StyleUtils;

public class TextFigure extends AbstractFigure<ITextStyle>
{

  private Point[] m_centerPoints;

  private String m_text = "";

  private Point[] m_leftTopPoints;

  /**
   * @param points
   *          center position of the figure
   */
  public void setPoints( final Point[] points )
  {
    m_centerPoints = points;
    m_leftTopPoints = null;

  }

  public void setText( final String text )
  {
    m_text = text;
  }

  @Override
  protected void paintFigure( final GC gc )
  {
    final IStyle style = getStyle();
    if( style != null && m_centerPoints != null )
    {
      style.apply( gc );

      if( m_leftTopPoints == null )
      {
        m_leftTopPoints = new Point[m_centerPoints.length];
        for( int i = 0; i < m_centerPoints.length; i++ )
        {
          final Point centerPoint = m_centerPoints[i];
          final Point textExtent = gc.textExtent( m_text );
          m_leftTopPoints[i] = FigureUtilities.centerToLeftTop( centerPoint, textExtent.x, textExtent.y );
        }
      }

      for( final Point p : m_leftTopPoints )
        if( m_text != null )
        {
          gc.drawText( m_text, p.x, p.y );
        }
    }
  }

  /**
   * returns the Style if it is set correctly; otherwise returns the default point style
   */
  @Override
  public ITextStyle getStyle( )
  {
    final ITextStyle style = super.getStyle();
    if( style != null )
      return style;
    else
      return StyleUtils.getDefaultStyle( TextStyle.class );
  }

  @Override
  public void setStyle( final ITextStyle ts )
  {
    super.setStyle( ts );
    m_leftTopPoints = null;
  }
}
