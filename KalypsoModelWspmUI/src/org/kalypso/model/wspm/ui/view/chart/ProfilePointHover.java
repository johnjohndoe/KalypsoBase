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

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;

import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * Helper that finds a hover info for profile data.
 * 
 * @author Gernot Belger
 */
public class ProfilePointHover
{
  private final AbstractProfilePointsLayer m_layer;

  private final IPointStyle m_hoverStyle;

  public ProfilePointHover( final AbstractProfilePointsLayer layer, final IPointStyle hoverStyle )
  {
    m_layer = layer;
    m_hoverStyle = hoverStyle;
  }

  // FIXME: instead, build hover info during paint, use HoverIndex
  public EditInfo getHover( final Point pos )
  {
    final IProfile profil = m_layer.getProfil();

    if( profil == null )
      return null;

    final IProfileRecord[] profilPoints = profil.getPoints();
    // FIXME: heavy operation: linear search through all points...
    for( int i = 0; i < profilPoints.length; i++ )
    {
      final IProfileRecord record = profilPoints[i];

      final Point screen = m_layer.toScreen( record );
      if( screen == null )
        continue;

      final int width = m_hoverStyle.getWidth();

      final Rectangle hover = RectangleUtils.buffer( screen, width );

      if( hover.contains( pos ) )
      {
        final PointFigure hoverFigure = new PointFigure( m_hoverStyle );
        hoverFigure.setCenterPoint( screen.x, screen.y );

        return new EditInfo( m_layer, hoverFigure, null, i, m_layer.getTooltipInfo( record ), screen );
      }
    }
    return null;
  }
}
