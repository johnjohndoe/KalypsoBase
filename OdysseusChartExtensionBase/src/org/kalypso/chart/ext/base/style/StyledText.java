package org.kalypso.chart.ext.base.style;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.framework.impl.util.ChartUtilities;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.eclipse.swt.graphics.GC;

/**
 * @author burtscher
 */
public class StyledText extends AbstractStyledElement implements IStyledElement
{

  private final int m_alpha;

  private final RGB m_foregroundColor;

  private final RGB m_backgroundColor;

  private final FontData m_fontData;

  private String m_text;

  public StyledText( String id, RGB foregroundColor, RGB backgroundColor, String fontName, int fontStyle, int fontSize, int alpha )
  {
    super( id );
    m_alpha = alpha;
    m_foregroundColor = foregroundColor;
    m_backgroundColor = backgroundColor;
    m_fontData = new FontData( fontName, fontSize, fontStyle );
  }

  /**
   * @see org.kalypso.chart.framework.styles.IStyledElement#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GC gc )
  {
    ChartUtilities.resetGC( gc );
    gc.setAlpha( m_alpha );
    final Font gcFont = gc.getFont();

    final Font styledFont = new Font( gc.getDevice(), m_fontData );
    gc.setFont( styledFont );

    // Farben speichern, damit sie später - nach dem disposen der neuen Farben - nicht leer sind
    final Color oldForeground = gc.getForeground();
    final Color oldBackground = gc.getBackground();

    final Color foregroundColor = new Color( gc.getDevice(), m_foregroundColor );
    final Color backgroundColor = new Color( gc.getDevice(), m_backgroundColor );
    gc.setForeground( foregroundColor );
    gc.setBackground( backgroundColor );
    List<Point> path = getPath();
    if( path != null )
    {
      for( int i = 0; i < path.size(); i++ )
      {
        if( m_text.trim().compareTo( "" ) == 0 )
        {
          final Point point = path.get( i );
          gc.drawText( m_text.trim(), point.x, point.y );
        }
      }
    }
    gc.setForeground( oldForeground );
    gc.setBackground( oldBackground );

    foregroundColor.dispose();
    backgroundColor.dispose();

    gc.setFont( gcFont );
    styledFont.dispose();
  }

  /**
   * @see org.kalypso.chart.framework.styles.IStyledElement#getType()
   */
  public SE_TYPE getType( )
  {
    return SE_TYPE.TEXT;
  }

  public void setText( String text )
  {
    m_text = text;
  }

  public static StyledText getDefault( )
  {
    String defaultId = "defaultTextElement_" + System.currentTimeMillis() + Math.random() * 10000;
    return new StyledText( defaultId, new RGB( 0, 0, 0 ), new RGB( 255, 255, 255 ), "Arial", SWT.NORMAL, 10, 255 );
  }

}
