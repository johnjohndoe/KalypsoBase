package de.openali.diagram.ext.base.style;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.openali.diagram.framework.model.styles.IStyledElement;
import de.openali.diagram.framework.model.styles.IStyleConstants.SE_TYPE;
import de.openali.diagram.framework.util.ChartUtilities;
import de.openali.diagram.framework.util.StyleUtils;

/**
 * @author burtscher
 */
public class StyledLine implements IStyledElement
{
  List<Point> m_path;

  private int m_width = 0;

  private int m_swtStyle;

  private RGB m_lineColor;

  private int m_alpha;

  public StyledLine( int width, RGB lineColor, int swtLineStyle, int alpha )
  {
    m_width = width;
    m_path = new ArrayList<Point>();
    m_lineColor = lineColor;
    m_swtStyle = swtLineStyle;
    m_alpha = alpha;
  }

  /**
   * todo. why setPath instead of paint( ..., path ) ??
   * 
   * @see de.openali.diagram.framework.model.styles.IStyledElement#setPath(java.util.ArrayList)
   */
  public void setPath( List<Point> path )
  {
    m_path = path;
  }

  /**
   * @see de.openali.diagram.framework.styles.IStyledElement#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GCWrapper gcw)
  {
    ChartUtilities.resetGC( gcw.m_gc);
    gcw.setAlpha( m_alpha );
    Color lineColor = new Color( gcw.getDevice(), m_lineColor );


    int[] intPath = StyleUtils.pointListToIntArray( m_path );
    gcw.setForeground( lineColor );
    gcw.setLineWidth( m_width );
    gcw.setLineStyle( m_swtStyle );
    gcw.drawPolyline( intPath );

    lineColor.dispose();

  }

  /**
   * @see de.openali.diagram.framework.styles.IStyledElement#getType()
   */
  public SE_TYPE getType( )
  {
    return SE_TYPE.LINE;
  }

  public static StyledLine getDefault()
  {
    return new StyledLine( 1, new RGB(0,0,0), SWT.LINE_SOLID, 255 );
  }

}
