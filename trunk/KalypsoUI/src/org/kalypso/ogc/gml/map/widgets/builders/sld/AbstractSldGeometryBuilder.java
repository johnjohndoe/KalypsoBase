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
package org.kalypso.ogc.gml.map.widgets.builders.sld;

import java.awt.Graphics;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.graphics.sld.LineSymbolizer;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.xml.XMLTools;
import org.kalypsodeegree_impl.graphics.sld.SLDFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;
import org.w3c.dom.Document;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractSldGeometryBuilder implements ISldGeometryBuilder
{
  private final Map<Class, Symbolizer> m_symbolizers = new HashMap<Class, Symbolizer>();

  private final List<Coordinate> m_coordinates = new ArrayList<Coordinate>();

  private final IMapPanel m_panel;

  public AbstractSldGeometryBuilder( final IMapPanel panel )
  {
    m_panel = panel;

    final List<URL> urls = new ArrayList<URL>();
    urls.add( AbstractSldGeometryBuilder.class.getResource( "resources/default.line.sld" ) );
    urls.add( AbstractSldGeometryBuilder.class.getResource( "resources/default.polygon.sld" ) );

    init( urls.toArray( new URL[] {} ) );
  }

  public AbstractSldGeometryBuilder( final IMapPanel panel, final URL[] slds )
  {
    m_panel = panel;
    init( slds );
  }

  private void init( final URL[] slds )
  {
    for( final URL sld : slds )
    {
      try
      {
        final InputStream inputStream = sld.openStream();
        final Document document = XMLTools.parse( inputStream );
        inputStream.close();

        final Symbolizer symbolizer = SLDFactory.createSymbolizer( null, document.getDocumentElement(), 0.0, Double.MAX_VALUE );
        if( symbolizer instanceof LineSymbolizer )
        {
          m_symbolizers.put( LineSymbolizer.class, symbolizer );
        }
        else if( symbolizer instanceof PolygonSymbolizer )
        {
          m_symbolizers.put( PolygonSymbolizer.class, symbolizer );
        }
        else
          throw new NotImplementedException();
      }
      catch( final Exception e )
      {
        KalypsoGisPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }
  }

  @Override
  public GM_Object addPoint( final GM_Point point )
  {
    try
    {
      final Point p = (Point) JTSAdapter.export( point );
      addPoint( p );

      return finish();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return null;
  }

  private void addPoint( final Point point )
  {
    m_coordinates.add( point.getCoordinate() );
  }

  @Override
  public void removeLastPoint( )
  {
    if( m_coordinates.size() > 0 )
      m_coordinates.remove( m_coordinates.size() - 1 );
  }

  protected String getCrs( )
  {
    return m_panel.getMapModell().getCoordinatesSystem();
  }

  protected Coordinate[] getCoordinates( )
  {
    return m_coordinates.toArray( new Coordinate[] {} );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.builders.IGeometryBuilder#paint(java.awt.Graphics,
   *      org.kalypsodeegree.graphics.transformation.GeoTransform, java.awt.Point)
   */
  @Override
  public void paint( final Graphics g, final GeoTransform projection, final java.awt.Point p )
  {
    try
    {
      final GM_Point point = MapUtilities.transform( m_panel, p );
      paint( g, projection, point );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  public void paint( final Graphics g, final GeoTransform projection, final GM_Point current ) throws GM_Exception, CoreException
  {
    final Point p = (Point) JTSAdapter.export( current );
    paint( g, projection, p );
  }

  public abstract void paint( final Graphics g, final GeoTransform projection, final Point current ) throws CoreException;

  protected Symbolizer getSymbolizer( final Class clazz )
  {
    return m_symbolizers.get( clazz );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.builders.IGeometryBuilder#reset()
   */
  @Override
  public void reset( )
  {
    m_coordinates.clear();

  }

  @Override
  public int size( )
  {
    return m_coordinates.size();
  }

}
