/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.model.wspm.ui.view.chart;

import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.util.resource.IPair;

/**
 * General helper code for profil layers.
 * 
 * @author Gernot Belger
 */
public final class ProfilLayerUtils
{
  private ProfilLayerUtils( )
  {
    throw new UnsupportedOperationException();
  }

  // TODO: has nothing to do with profile -> move into chart api
  // TODO: rename to 'screenToNumeric'
  public final static Point2D toNumeric( final ICoordinateMapper cm, final Point point )
  {
    if( point == null )
      return null;

    if( cm == null )
      return null;

    final IPair<Number, Number> numeric = cm.screenToNumeric( point );

    return new Point2D.Double( numeric.getDomain().doubleValue(), numeric.getTarget().doubleValue() );
  }
}