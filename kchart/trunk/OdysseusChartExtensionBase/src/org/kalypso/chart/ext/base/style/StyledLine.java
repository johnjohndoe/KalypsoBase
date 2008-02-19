package org.kalypso.chart.ext.base.style;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.framework.impl.util.ChartUtilities;
import org.kalypso.chart.framework.impl.util.StyleUtils;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.eclipse.swt.graphics.GC;

/**
 * @author burtscher
 */
public class StyledLine extends AbstractStyledElement implements IStyledElement
{

  private int m_width = 0;

  private final int m_swtStyle;

  private final RGB m_lineColor;

  private final int m_alpha;

  public StyledLine( String id, int width, RGB lineColor, int swtLineStyle, int alpha )
  {
    super( id );
    m_width = width;
    m_lineColor = lineColor;
    m_swtStyle = swtLineStyle;
    m_alpha = alpha;
  }

  /**
   * @see org.kalypso.chart.framework.styles.IStyledElement#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GC gc )
  {
    ChartUtilities.resetGC( gc);
    gc.setAlpha( m_alpha );
    final Color lineColor = new Color( gc.getDevice(), m_lineColor );

    final int[] intPath = StyleUtils.pointListToIntArray( getPath() );
    gc.setForeground( lineColor );
    gc.setLineWidth( m_width );
    gc.setLineStyle( m_swtStyle );
    gc.drawPolyline( intPath );

    lineColor.dispose();

  }

  /**
   * @see org.kalypso.chart.framework.styles.IStyledElement#getType()
   */
  public SE_TYPE getType( )
  {
    return SE_TYPE.LINE;
  }

  public static StyledLine getDefault( )
  {
    String defaultId = "defaultLineStyle_" + System.currentTimeMillis() + Math.random() * 10000;
    return new StyledLine( defaultId, 1, new RGB( 0, 0, 0 ), SWT.LINE_SOLID, 255 );
  }

}
