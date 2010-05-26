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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.net.IUrlResolver2;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypsodeegree.graphics.displayelements.DisplayElement;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.xml.XMLTools;
import org.kalypsodeegree_impl.graphics.displayelements.DisplayElementFactory;
import org.kalypsodeegree_impl.graphics.sld.SLDFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;
import org.w3c.dom.Document;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Dirk Kuch
 */
public class SLDPainter
{
  Map<URL, Symbolizer> m_symbolizerMap = new HashMap<URL, Symbolizer>();

  private final GeoTransform m_projection;

  private final String m_crs;

  public SLDPainter( final GeoTransform projection, final String crs )
  {
    m_projection = projection;
    m_crs = crs;
  }

  public void paint( final Graphics g, final URL sld, final Coordinate[] coordinates ) throws CoreException
  {
    final List<IStatus> statis = new ArrayList<IStatus>();

    for( final Coordinate coordinate : coordinates )
    {
      try
      {
        paint( g, sld, coordinate );
      }
      catch( final Exception e )
      {
        final String msg = String.format( "Painting coordinate (x=%d, y=%d) failed", coordinate.x, coordinate.y );
        StatusUtilities.createErrorStatus( msg, e );
      }
    }

    if( !statis.isEmpty() )
      throw new CoreException( StatusUtilities.createStatus( statis, "Paintig of one ore more coordinates failed." ) );
  }

  public void paint( final Graphics g, final URL sld, final Coordinate coordinate ) throws CoreException
  {
    final Point point = JTSAdapter.jtsFactory.createPoint( coordinate );
    paint( g, sld, point );
  }

  public void paint( final Graphics g, final URL sld, final Geometry geometry ) throws CoreException
  {
    Symbolizer symbolizer = m_symbolizerMap.get( sld );
    if( symbolizer == null )
    {
      try
      {
        final InputStream inputStream = sld.openStream();
        final Document document = XMLTools.parse( inputStream );
        inputStream.close();

        final IUrlResolver2 resolver = new IUrlResolver2()
        {

          @Override
          public URL resolveURL( final String relativeOrAbsolute ) throws MalformedURLException
          {
            return UrlResolverSingleton.resolveUrl( sld, relativeOrAbsolute );
          }
        };

        symbolizer = SLDFactory.createSymbolizer( resolver, document.getDocumentElement(), 0.0, Double.MAX_VALUE );
        m_symbolizerMap.put( sld, symbolizer );
      }
      catch( final Exception e )
      {
        throw new CoreException( StatusUtilities.createErrorStatus( "Painting sld failed.", e ) );
      }
    }

    paint( g, symbolizer, geometry );

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
