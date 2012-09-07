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
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;

import de.openali.odysseus.chart.framework.model.figure.impl.MarkerFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * Helper that finds a hover info for profile data.
 *
 * @author Gernot Belger
 */
public class ProfilePointHover
{
  private final AbstractProfilLayer m_layer;

  public ProfilePointHover( final AbstractProfilLayer layer )
  {
    m_layer = layer;
  }

  public EditInfo getHover( final Point pos )
  {
    final IProfil profil = m_layer.getProfil();

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

      final Rectangle hover = RectangleUtils.buffer( screen, 5 );

      if( hover.contains( pos ) )
      {
        final IPointStyle pointStyleHover = m_layer.getPointStyleHover();
        final MarkerFigure hoverFigure = new MarkerFigure( pointStyleHover );
        hoverFigure.setCenterPoint( screen.x, screen.y );

        return new EditInfo( m_layer, hoverFigure, null, i, m_layer.getTooltipInfo( record ), screen );
      }
    }
    return null;
  }
}
