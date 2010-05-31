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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.widgets.advanced.utils.SLDPainter;
import org.kalypso.ogc.gml.map.widgets.builders.sld.rules.IGeometryBuilderValidationRule;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.graphics.sld.LineSymbolizer;
import org.kalypsodeegree.graphics.sld.PointSymbolizer;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.xml.XMLTools;
import org.kalypsodeegree_impl.graphics.sld.SLDFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;
import org.w3c.dom.Document;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractSldGeometryBuilder implements ISldGeometryBuilder
{
  private final Set<IGeometryBuilderValidationRule> m_rules = new LinkedHashSet<IGeometryBuilderValidationRule>();

  private final Set<Symbolizer> m_symbolizers = new HashSet<Symbolizer>();

  private final Set<Coordinate> m_coordinates = new LinkedHashSet<Coordinate>();

  private final IMapPanel m_panel;

  private String m_tooltip;

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
        m_symbolizers.add( symbolizer );
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

  /**
   * @see org.kalypso.ogc.gml.map.widgets.builders.sld.ISldGeometryBuilder#addPoint(com.vividsolutions.jts.geom.Coordinate)
   */
  @Override
  public void addPoint( final Coordinate coordinate )
  {
    m_coordinates.add( coordinate );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.builders.sld.ISldGeometryBuilder#addPoint(org.kalypsodeegree.model.geometry.GM_Position)
   */
  @Override
  public void addPoint( final GM_Position position )
  {
    addPoint( JTSAdapter.export( position ) );
  }

  @Override
  public void addPoint( final Point point )
  {
    m_coordinates.add( point.getCoordinate() );
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

  protected abstract Geometry buildGeometry( Point point );

  public void paint( final Graphics g, final GeoTransform projection, final GM_Point current ) throws GM_Exception, CoreException
  {
    final Point p = (Point) JTSAdapter.export( current );
    final Geometry geometry = buildGeometry( p );
    if( geometry == null )
      return;

    final SLDPainter painter = new SLDPainter( projection, getCrs() );

    final IGeometryBuilderValidationRule[] rules = getRules();
    for( final IGeometryBuilderValidationRule rule : rules )
    {
      if( !rule.isValid( geometry, p ) )
      {
        final URL sld = rule.getSld( geometry );
        if( sld == null )
          return;

        setTooltip( rule.getTooltip() );
        painter.paint( g, sld, geometry );

        return;
      }
    }

    /* reset vailidation tooltip */
    setTooltip( null );

    final Symbolizer symbolizer = getSymbolizer( geometry );
    painter.paint( g, symbolizer, geometry );
  }

  private void setTooltip( final String tooltip )
  {
    m_tooltip = tooltip;
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.builders.sld.ISldGeometryBuilder#getTooltip()
   */
  @Override
  public String getTooltip( )
  {
    return m_tooltip;
  }

  protected Symbolizer getSymbolizer( final Geometry geometry )
  {
    for( final Symbolizer symbolizer : m_symbolizers )
    {
      if( geometry instanceof Point && symbolizer instanceof PointSymbolizer )
        return symbolizer;
      else if( geometry instanceof LineString && symbolizer instanceof LineSymbolizer )
        return symbolizer;
      else if( geometry instanceof Polygon && symbolizer instanceof PolygonSymbolizer )
        return symbolizer;
    }

    throw new NotImplementedException();
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

  /**
   * @see org.kalypso.ogc.gml.map.widgets.builders.sld.ISldGeometryBuilder#removeLastCoordinate()
   */
  @Override
  public synchronized Coordinate removeLastCoordinate( )
  {
    if( m_coordinates.size() > 0 )
    {
      final Coordinate[] coordinates = m_coordinates.toArray( new Coordinate[] {} );
      final Coordinate remove = coordinates[coordinates.length - 1];
      m_coordinates.remove( remove );

      return remove;
    }

    return null;
  }

  @Override
  public synchronized void removeLastPoint( )
  {
    removeLastCoordinate();
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.builders.sld.ISldGeometryBuilder#addRule(org.kalypso.ogc.gml.map.widgets.builders.sld.IGeometryBuilderValidationRule)
   */
  @Override
  public void addRule( final IGeometryBuilderValidationRule rule )
  {
    m_rules.add( rule );
  }

  protected IGeometryBuilderValidationRule[] getRules( )
  {
    return m_rules.toArray( new IGeometryBuilderValidationRule[] {} );
  }

}
