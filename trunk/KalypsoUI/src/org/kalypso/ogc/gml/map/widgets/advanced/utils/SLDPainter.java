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
package org.kalypso.ogc.gml.map.widgets.advanced.utils;

import java.awt.Graphics;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypsodeegree.graphics.displayelements.DisplayElement;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.xml.XMLTools;
import org.kalypsodeegree_impl.graphics.displayelements.DisplayElementFactory;
import org.kalypsodeegree_impl.graphics.sld.SLDFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;
import org.w3c.dom.Document;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Dirk Kuch
 */
public class SLDPainter
{

  private final GeoTransform m_projection;

  private final String m_crs;

  public SLDPainter( final GeoTransform projection, final String crs )
  {
    m_projection = projection;
    m_crs = crs;
  }

  public void paint( final Graphics g, final URL sld, final Geometry geometry ) throws CoreException
  {
    try
    {
      final InputStream inputStream = sld.openStream();
      final Document document = XMLTools.parse( inputStream );
      inputStream.close();

      final Symbolizer symbolizer = SLDFactory.createSymbolizer( null, document.getDocumentElement(), 0.0, Double.MAX_VALUE );

      paint( g, symbolizer, geometry );
    }
    catch( final Exception e )
    {
      throw new CoreException( StatusUtilities.createErrorStatus( "Painting sld failed.", e ) );
    }
  }

  public void paint( final Graphics g, final Symbolizer symbolizer, final Geometry geometry ) throws CoreException
  {
    try
    {
      final GM_Object gmo = JTSAdapter.wrap( geometry, m_crs );

      final DisplayElement lde = DisplayElementFactory.buildDisplayElement( null, symbolizer, gmo );
      lde.paint( g, m_projection, new NullProgressMonitor() );
    }
    catch( final Exception e )
    {
      throw new CoreException( StatusUtilities.createErrorStatus( "Painting sld failed.", e ) );
    }
  }

}
