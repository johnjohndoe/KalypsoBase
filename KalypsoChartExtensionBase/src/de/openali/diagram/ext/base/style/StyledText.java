package de.openali.diagram.ext.base.style;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.openali.diagram.framework.model.styles.IStyledElement;
import de.openali.diagram.framework.model.styles.IStyleConstants.SE_TYPE;
import de.openali.diagram.framework.util.ChartUtilities;

/**
 * @author burtscher
 */
public class StyledText implements IStyledElement
{

  private List<Point> m_path;


  private final int m_alpha;

  private RGB m_foregroundColor;

  private RGB m_backgroundColor;

  private FontData m_fontData;

  private String m_text;

  public StyledText( RGB foregroundColor, RGB backgroundColor, String fontName, int fontStyle, int fontSize, int alpha )
  {
    m_alpha=alpha;
    m_path = new ArrayList<Point>();
    m_foregroundColor=foregroundColor;
    m_backgroundColor=backgroundColor;
    m_fontData=new FontData(fontName, fontSize, fontStyle);
  }


  /**
   * @see de.openali.diagram.framework.styles.IStyledElement#setPath(java.util.List)
   * The path is used draw a styled point at each point of the path; the Point is regarded
   * as the center position of the element
   */
  public void setPath( List<Point> path )
  {
    m_path = path;
  }

  /**
   * @see de.openali.diagram.framework.styles.IStyledElement#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GCWrapper gc)
  {
    ChartUtilities.resetGC( gc.m_gc );
    gc.setAlpha( m_alpha );
    Font gcFont=gc.getFont();

    Font styledFont=new Font(gc.getDevice(), m_fontData);
    gc.setFont(styledFont);
    
    //Farben speichern, damit sie später - nach dem disposen der neuen Farben - nicht leer sind
    Color oldForeground=gc.getForeground();
    Color oldBackground=gc.getBackground();

    Color foregroundColor = new Color( gc.getDevice(), m_foregroundColor );
    Color backgroundColor = new Color( gc.getDevice(), m_backgroundColor );
    gc.setForeground( foregroundColor );
    gc.setBackground( backgroundColor );
    if( m_path != null )
    {
      for( int i = 0; i < m_path.size(); i++ )
      {
        if (m_text.trim().compareTo( "" )==0)
        {
            Point point = m_path.get(i);
            gc.drawText( m_text.trim(), point.x, point.y );
        }
      }
    }
    gc.setForeground( oldForeground );
    gc.setBackground( oldBackground );

    foregroundColor.dispose();
    backgroundColor.dispose();
    
    gc.setFont(gcFont);
    styledFont.dispose();
  }

  /**
   * @see de.openali.diagram.framework.styles.IStyledElement#getType()
   */
  public SE_TYPE getType( )
  {
    return SE_TYPE.TEXT;
  }

  public void setText(String text)
  {
    m_text=text;
  }

  public static StyledText getDefault()
  {
    return new StyledText( new RGB(0,0,0), new RGB(255,255,255), "Arial", SWT.NORMAL, 10, 255 );
  }

}
