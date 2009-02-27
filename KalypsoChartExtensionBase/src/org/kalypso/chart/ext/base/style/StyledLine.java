package org.kalypso.chart.ext.base.style;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.kalypso.chart.framework.util.ChartUtilities;
import org.kalypso.chart.framework.util.StyleUtils;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

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
   * @see org.kalypso.chart.framework.styles.IStyledElement#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GCWrapper gcw )
  {
    ChartUtilities.resetGC( gcw.m_gc );
    gcw.setAlpha( m_alpha );
    final Color lineColor = new Color( gcw.getDevice(), m_lineColor );

    final int[] intPath = StyleUtils.pointListToIntArray( getPath() );
    gcw.setForeground( lineColor );
    gcw.setLineWidth( m_width );
    gcw.setLineStyle( m_swtStyle );
    gcw.drawPolyline( intPath );

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
