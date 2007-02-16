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
package org.kalypso.gml.ui.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.kalypso.commons.performance.TimeLogger;
import org.kalypso.loader.IPooledObject;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.MapPanel;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.MapModellHelper;
import org.kalypso.ogc.gml.selection.FeatureSelectionManager2;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.template.gismapview.Gismapview;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.model.cs.ConvenienceCSFactory;
import org.opengis.cs.CS_CoordinateSystem;
import org.xml.sax.InputSource;

/**
 * Not for automatik testing. Test methods changed to 'tost' for this reason.
 * 
 * @author Gernot Belger
 */
public class GisMapPerfTest extends TestCase
{
  public void tostMapLoadingAndVisualization( ) throws JAXBException, IOException
  {
    final TimeLogger logger = new TimeLogger( "Starting test" );

    final CS_CoordinateSystem crs = ConvenienceCSFactory.getInstance().getOGCCSByName( "EPSG:31467" );

    logger.showElapsedTime( "Loading map" );

    final URL resource = getClass().getResource( "resources/triangles/triangle.gmt" );
    final InputStream inputStream = resource.openStream();
    final InputSource source = new InputSource( inputStream );
    source.setEncoding( "Cp1252" );
    final Gismapview gismapview = GisTemplateHelper.loadGisMapView( source );
    inputStream.close();

    logger.showElapsedTime( "Map was loaded. Creating map modell on map" );

    final FeatureSelectionManager2 featureSelectionManager2 = new FeatureSelectionManager2();
    GisTemplateMapModell modell0 = loadMapAndWaitForData( gismapview, resource, crs, logger, featureSelectionManager2 );
    if( modell0 == null )
      fail( "Map was not loaded" );

    logger.showElapsedTime( "Map data was loaded" );

    final MapPanel panel = new MapPanel( null, crs, featureSelectionManager2, null );
    panel.setMapModell( modell0 );
    panel.setBoundingBox( modell0.getFullExtentBoundingBox() );
    final GeoTransform transform = panel.getProjection();
    final GM_Envelope bbox = panel.getBoundingBox();
    final BufferedImage image = new BufferedImage( 1024, 1024, BufferedImage.TYPE_INT_ARGB );

    for( int i = 0; i < 5; i++ )
    {
      logger.showElapsedTime( "Rendering map #" + i );
      paintPanel( image, panel, transform, bbox );
      logger.showElapsedTime( "Rendered map #" + i );
    }

//    logger.showElapsedTime( "Render map to file" );
//    FileOutputStream stream = new FileOutputStream( new File( "C:/tmp/image.png" ) );
//    new ExportableMap( panel, 1024, 800, "png" ).exportObject( stream, new NullProgressMonitor(), new BaseConfiguration() );

    logger.showElapsedTime( "Map was rendered" );
  }

  private void paintPanel( final BufferedImage image, final MapPanel panel, GeoTransform transform, GM_Envelope bbox )
  {
    final int width = image.getWidth();
    final int height = image.getHeight();

    final Graphics gr = image.getGraphics();

    gr.setColor( Color.white );
    gr.fillRect( 0, 0, width, height );
    gr.setColor( Color.black );
    gr.setClip( 0, 0, width, height );

    transform.setDestRect( 0, 0, width, height );

    final IMapModell model = panel.getMapModell();
    final double scale = MapModellHelper.calcScale( model, bbox, width, height );
    model.paint( gr, transform, bbox, scale, false );
  }

  /** @return null, iff not all data was loaded in time. */
  private GisTemplateMapModell loadMapAndWaitForData( final Gismapview gismapview, final URL resource, final CS_CoordinateSystem crs, final TimeLogger logger, IFeatureSelectionManager selectionManager )
  {
    logger.showElapsedTime( "Loading map modell..." );

    final GisTemplateMapModell mapModell = new GisTemplateMapModell( resource, crs, null, selectionManager );
    mapModell.createFromTemplate( gismapview );

    int maxWait = 100000;
    while( true )
    {
      if( isLoaded( mapModell ) )
        break;
      try
      {
        Thread.sleep( 1000 );
      }
      catch( final InterruptedException e )
      {
        e.printStackTrace();
      }
      if( maxWait-- < 0 ) // do not wait for ever
      {
        System.out.println( "Waited too long for map to load. Stopping now." );
        return null;
      }
    }
    logger.showElapsedTime( "Map modell and data loaded." );

    return mapModell;
  }

  private boolean isLoaded( final IMapModell m_modell )
  {
    if( m_modell == null )
      return false;

    final IKalypsoTheme[] themes = m_modell.getAllThemes();

    for( int i = 0; i < themes.length; i++ )
    {
      final IKalypsoTheme theme = themes[i];

      if( theme instanceof IPooledObject )
      {
        if( !((IPooledObject) theme).isLoaded() )
          return false;
      }
    }
    return true;
  }

  public void tostGMLLoading( ) throws Exception
  {
    final TimeLogger logger = new TimeLogger( "Starting test" );

    logger.showElapsedTime( "Loading workspace first time" );

    final URL resource = getClass().getResource( "resources/triangles/triangle.gml" );
    GmlSerializer.createGMLWorkspace( resource, null );

    logger.showElapsedTime( "Workspace was loaded" );

    // now test it several times
    for( int i = 0; i < 10; i++ )
    {
      logger.showElapsedTime( "Loading Workspace #" + i );
      GmlSerializer.createGMLWorkspace( resource, null );
      logger.showElapsedTime( "Workspace #" + i + "loaded" );
    }

    logger.showElapsedTime( "Test finished" );
  }

}
