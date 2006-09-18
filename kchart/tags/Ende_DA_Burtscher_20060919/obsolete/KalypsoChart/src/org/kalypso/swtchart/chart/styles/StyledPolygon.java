/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.swtchart.chart.styles;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.ChartUtilities;
import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_TYPE;

/**
 * @author alibu
 */
public class StyledPolygon implements IStyledElement
{
  ArrayList<Point> m_path;

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

  public void setPath( ArrayList<Point> path )
  {
    m_path = path;
  }

  /**
   * @see org.kalypso.swtchart.styles.IStyledElement#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GCWrapper gc, Device dev )
  {
    ChartUtilities.resetGC( gc.m_gc, dev );
    gc.setAlpha( m_alpha );
    Color fillColor = new Color( dev, m_fillColor );
    Color borderColor = null;
    if( m_borderColor != null && m_borderWidth > 0 )
    {
      borderColor = new Color( dev, m_borderColor );
    }

    int[] intPath = StyleHelper.pointListToIntArray( m_path );

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
   * @see org.kalypso.swtchart.styles.IStyledElement#getType()
   */
  public SE_TYPE getType( )
  {
    return SE_TYPE.POLYGON;
  }

}
