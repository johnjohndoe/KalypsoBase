/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.simulation.core;

/**
 * Sammelt die Ergebnisse der {@link org.kalypso.simulation.core.ISimulation}.
 * 
 * @author belger
 */
public interface ISimulationResultEater
{
  /**
   * Für die {@link ISimulation}: wird aufgerufen, um ein Ergebnis an den Client zurückzugeben
   * 
   * @param id
   *            Eine ID aus der Model-Spec
   * @param result
   *            Das Ergebnis, dass gespeichert werden soll. Dies kann eine Datei, ein Literal, usw. sein.
   *            <ol>
   *            <li><strong>ComplexValueType:</strong> Zum Beispiel ein Bild. Verwenden Sie hierzu den Typ
   *            ComplexValueType von OGC.</li>
   *            <li><strong>LiteralValueType:</strong> Ein Literal (String, Integer, Double, Boolean).</li>
   *            <li><strong>ComplexValueReference:</strong> Eine beliebige Datei oder ein Verzeichnis. Wenns ein
   *            Verzeichnis ist, wird der gesamte Inhalt (auch rekursiv) zum Client zurückgeschrieben.</li>
   *            <li><strong>BoundingBoxType:</strong> Eine Bounding-Box. Verwenden Sie hierzu den Typ BoundingBoxType
   *            von OGC.</li>
   *            </ol>
   */
  public void addResult( final String id, final Object result ) throws SimulationException;
}