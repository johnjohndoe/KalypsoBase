/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.ogc.gml.wms.provider.images;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Font;
import org.kalypso.ogc.gml.wms.loader.ICapabilitiesLoader;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * Fetches images from a OpenStreepMap-Tile-Server
 *
 * @author Gernot Belger
 */
public class OsmImageProvider implements IKalypsoImageProvider
{
  @Override
  public void init( final String themeName, final String[] layers, final String[] styles, final String service, final String localSRS, final String sldBody )
  {
  }

  @Override
  public String getLabel( )
  {
    return "OpenStreeMap"; //$NON-NLS-1$
  }

  @Override
  public GM_Envelope getFullExtent( )
  {
    return null;
  }

  @Override
  public IStatus checkInitialize( final IProgressMonitor monitor )
  {
    return Status.OK_STATUS;
  }

  @Override
  public Image getImage( final int width, final int height, final GM_Envelope bbox )
  {
    // TODO:
    // - guess best zoom level
    // - calculate bbox in latlong
    // - translate to osm coordinates
    // - build request url
    // - request image
    // - draw image

    final BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );

    final Graphics2D g = bi.createGraphics();
    g.setColor( Color.cyan );
    g.fillRect( 0, 0, width, height );
    g.dispose();

    return bi;
  }

  @Override
  public ICapabilitiesLoader createCapabilitiesLoader( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public org.eclipse.swt.graphics.Image getLegendGraphic( final Font font )
  {
    return null;
  }

  @Override
  public WMSCapabilities getCapabilities( )
  {
    return null;
  }

  @Override
  public boolean isLayerVisible( final String name )
  {
    return false;
  }

  @Override
  public void setLayerVisible( final String name, final boolean visible )
  {

  }

  @Override
  public String getStyle( final Layer layer )
  {
    return null;
  }
}