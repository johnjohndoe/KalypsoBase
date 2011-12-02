/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.ogc.gml.map.layer;

import java.awt.Graphics;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * This is the most straight implementation of the layer interface: it just calls the theme to render itself, no
 * buffering or tiling happens.
 * 
 * @author Gernot Belger
 */
public class DirectMapLayer extends AbstractMapLayer
{
  public DirectMapLayer( final IMapPanel panel, final IKalypsoTheme theme )
  {
    super( panel, theme );
  }

  /**
   * @see org.kalypso.ogc.gml.map.tiles.IMapLayer#paint(java.awt.Graphics,
   *      org.kalypsodeegree.graphics.transformation.GeoTransform, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void paint( final Graphics g, final GeoTransform world2screen, final IProgressMonitor monitor ) throws CoreException
  {
    final IStatus result = getTheme().paint( g, world2screen, null, monitor );
    if( !result.isOK() )
      throw new CoreException( result );
  }

  /**
   * @see org.kalypso.ogc.gml.map.layer.AbstractMapLayer#invalidate(org.kalypsodeegree.model.geometry.GM_Envelope)
   */
  @Override
  protected void invalidate( final GM_Envelope invalidExtent )
  {
    // nothing to do: we have no state to invalidate

    // only invalidate the map, so the paint method will be called
    getMapPanel().invalidateMap();
  }

  /**
   * @see org.kalypso.ogc.gml.map.layer.AbstractMapLayer#handleExtentChanged(org.kalypsodeegree.graphics.transformation.GeoTransform)
   */
  @Override
  protected void handleExtentChanged( final GeoTransform world2screen )
  {
  }

  /**
   * @see org.kalypso.ogc.gml.map.layer.AbstractMapLayer#stopPainting()
   */
  @Override
  protected void stopPainting( )
  {
  }

}