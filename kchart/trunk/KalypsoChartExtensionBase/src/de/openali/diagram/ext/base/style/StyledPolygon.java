package de.openali.diagram.ext.base.style;

import java.util.ArrayList;
import java.util.List;

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
public class StyledPolygon implements IStyledElement
{
  List<Point> m_path;

  private int m_borderWidth;

  private RGB m_borderColor;

  private RGB m_fillColor;

  private final int m_alpha;

  public StyledPolygon( RGB fillColor, int borderWidth, RGB borderColor, int alpha )
  {
    m_alpha = alpha;
    m_path = new ArrayList<Point>();
    m_borderWidth = borderWidth;
    m_borderColor = borderColor;
    m_fillColor = fillColor;
  }

  /**
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
  public void paint( GCWrapper gc )
  {
    ChartUtilities.resetGC( gc.m_gc);
    gc.setAlpha( m_alpha );
    Color fillColor = new Color( gc.getDevice(), m_fillColor );
    Color borderColor = null;
    if( m_borderColor != null && m_borderWidth > 0 )
    {
      borderColor = new Color( gc.getDevice(), m_borderColor );
    }

    int[] intPath = StyleUtils.pointListToIntArray( m_path );

    // Linienbreite, die verwendet wird, falls alle Punkt auf einer Achsenparallelen oder einem Punkt liegen
    int lineWidth = m_borderWidth;
    if( lineWidth == 0 )
    {
      lineWidth = 1;
    }
    if( borderColor != null )
      gc.setForeground( borderColor );
    gc.setBackground( fillColor );

    /*
     * Punkte durchlaufen, um zu überprüfen, ob alle x oder alle y Werte gleich sind. Sollte das der Fall sein, so muss
     * eine Linie bzw. ein Punkt gezeichnet werden, um die Daten dennoch anzuzeigen
     */
    boolean xsEqual = true;
    boolean ysEqual = true;
    // Hier werden die ersten Werte des Pfads gespeichert
    int eqX = 0;
    int eqY = 0;
    for( int i = 0; i < m_path.size(); i++ )
    {
      Point p = m_path.get( i );
      // erster Punkt: Werte initialisieren
      if( i == 0 )
      {
        eqX = p.x;
        eqY = p.y;
      }
      // Sonst: Vergleichen
      else
      {
        // bei Abweichung wird Equal auf false gesetz;
        if( eqX != p.x )
          xsEqual = false;
        if( eqY != p.y )
          ysEqual = false;
        // Wen beide verschieden sind, kann abgebrochen werden
        if( !xsEqual && !ysEqual )
          break;
      }
    }
    // alle Punkte gleich => Punkt zeichnen
    if( xsEqual & ysEqual )
    {
      gc.fillOval( eqX, eqY, lineWidth, lineWidth );
    }
    else
    {
      if( xsEqual || ysEqual )
      {
        // Linienfarbe wird auf Füllfarbe des Polygons gesetzt
        gc.setForeground( fillColor );
        gc.setLineWidth( lineWidth );
        gc.drawPolyline( intPath );
      }
      else
        gc.fillPolygon( intPath );
    }

    if( m_borderWidth > 0 )
    {
      gc.setLineWidth( m_borderWidth );
      gc.drawPolygon( intPath );
    }

    fillColor.dispose();
    if( borderColor != null && !borderColor.isDisposed() )
      borderColor.dispose();

  }

  /**
   * @see de.openali.diagram.framework.styles.IStyledElement#getType()
   */
  public SE_TYPE getType( )
  {
    return SE_TYPE.POLYGON;
  }

  public static StyledPolygon getDefault( )
  {
    return new StyledPolygon( new RGB( 230, 230, 230 ), 1, new RGB( 0, 0, 0 ), 255 );
  }

  /* to set alternating colors of an style, for instance needed for a histogram */
  public void setFillColor( RGB fillColor )
  {
    m_fillColor = fillColor;
  }

}
