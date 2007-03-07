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
package org.kalypso.swtchart.chart.axis;

import java.util.Comparator;

import org.eclipse.swt.graphics.Point;
import org.kalypso.swtchart.chart.axis.registry.IAxisRegistry;
import org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer;

/**
 * @author schlienger
 * @author burtscher
 */
public interface IAxis<T>
{
  /**
   * @return the axis' unique identifier
   */
  public String getIdentifier( );

  /**
   * @return axis label
   */
  public String getLabel( );

  /**
   * @return DataClass which is understood by this axis
   */
  public Class< ? > getDataClass( );

  /**
   * TODO: consider to move the dataComparater to the range class
   */
  public Comparator<T> getDataComparator( );

  /**
   * @return Axis property - discrete or continous
   */
  public IAxisConstants.PROPERTY getProperty( );

  /**
   * @return axis position - left, right, top, bottom
   */
  public IAxisConstants.POSITION getPosition( );

  /**
   * @return axis direction - positive or negative
   */
  public IAxisConstants.DIRECTION getDirection( );

  /** Same as getDirection() == NEGATIVE */
  public boolean isInverted( );

  /**
   * @return minimal displayable value
   */
  public T getFrom( );

  /**
   * sets minimal displayable value
   */
  public void setFrom( T min );

  /**
   * @return maximum displayable value
   */
  public T getTo( );

  /**
   * sets maximum displayable value
   */
  public void setTo( T max );

  public int logicalToScreen( T value );

  /**
   * Gibt ein Intervall von Bildschrimkoordinaten mit der logischen Breite zurück, in dem der übergebene Wert liegt
   * 
   * @param value
   *          ein Wert, der im zurückgegebenen Intervall liegt
   * @param fixedPoint
   *          ein fixer Punkt auf der Achse, von dem aus die Intervallegemessen werden
   * @param intervalSize
   *          Die Größe des Intervalls; die muss von einzelnen Implementationen entsprechend Interpretiert werden;
   * @return Point, dessen x/y-Werte den Anfang- und Endwert des Intervalls auf der Achse zurückgeben
   */
  public Point logicalToScreenInterval( T value, T fixedPoint, double intervalSize );

  public T screenToLogical( Integer value );

  public void setRegistry( IAxisRegistry axisRegistry );

  public IAxisRegistry getRegistry( );

  public IAxisRenderer<T> getRenderer( );
}
