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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.performance.TimeLogger;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.MapPanel;
import org.kalypso.ogc.gml.map.ThemePainter;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.selection.FeatureSelectionManager2;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.template.gismapview.Gismapview;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Not for automatik testing. Test methods changed to 'tost' for this reason.
 * 
 * @author Gernot Belger
 */
public class GisMapPerfTest extends TestCase
{
  public static String[][] RESOURCES = { //
  { "dgm2m.zip", "dgm2m.gmt", "dgm2m.gml" }, //
      { "points.zip", "points.gmt", "Shapes/point" }, //
      { "tin.zip", "tin.gmt", "tin_DEPTH.gml" }, //
      { "tin_isolines.zip", "tin.gmt", "tin_DEPTH.gml" }, //
      { "polyWithHoles.zip", "poly.gmt", "Shapes/polygon" } //
  };

  public void testZips( ) throws Exception
  {
    for( final String[] resource : RESOURCES )
    {
      final URL zipResource = getClass().getResource( "resources/" + resource[0] );
      final URL gmtResource = new URL( "jar:" + zipResource.toExternalForm() + "!/" + resource[1] );

      mapLoadingAndVisualization( gmtResource );

      // final URL gmlResource = new URL( "jar:" + zipResource.toExternalForm() + "!/" + resource[2] );
      // gmlLoading( gmlResource );
    }
  }

  private void mapLoadingAndVisualization( final URL gmtResource ) throws JAXBException, IOException, CoreException, SAXException, ParserConfigurationException
  {
    final TimeLogger logger = new TimeLogger( "Starting test: " + gmtResource.getPath() );

    final String crs = "EPSG:31467";

    logger.takeInterimTime();
    logger.printCurrentInterim( "Loading map: " );

    final InputStream inputStream = gmtResource.openStream();
    final InputSource source = new InputSource( inputStream );
    source.setEncoding( "Cp1252" );
    final Gismapview gismapview = GisTemplateHelper.loadGisMapView( source );
    inputStream.close();

    logger.takeInterimTime();
    logger.printCurrentInterim( "Map was loaded. Creating map modell on map: " );

    final FeatureSelectionManager2 featureSelectionManager2 = new FeatureSelectionManager2();
    final GisTemplateMapModell modell = loadMapAndWaitForData( gismapview, gmtResource, crs, logger, featureSelectionManager2 );
    if( modell == null )
      fail( "Map was not loaded" );

    logger.takeInterimTime();
    logger.printCurrentInterim( "Map data was loaded: " );

    final MapPanel panel = new MapPanel( null, featureSelectionManager2 );
    panel.setMapModell( modell );
    panel.setBoundingBox( modell.getFullExtentBoundingBox() );
    final GeoTransform transform = panel.getProjection();
    transform.setDestRect( 0, 0, 1024, 1024, null );

    final ThemePainter themePainter = new ThemePainter( panel );

    final BufferedImage image = new BufferedImage( 1024, 1024, BufferedImage.TYPE_INT_ARGB );

    for( int i = 0; i < 5; i++ )
    {
      logger.takeInterimTime();

      paintPanel( image, themePainter );

      logger.takeInterimTime();
      logger.printCurrentInterim( "Rendered map #" + i + ": " );

      if( i == 0 )
        ImageIO.write( image, "png", new File( "C:/tmp/image.png" ) );
    }

    logger.printCurrentTotal( "Total: " );

    modell.dispose();
    panel.dispose();
  }

  private void paintPanel( final BufferedImage image, final ThemePainter painter ) throws CoreException
  {
    Graphics2D gr = null;
    try
    {
      gr = (Graphics2D) image.getGraphics();

      painter.paintThemes( gr, false, null );
    }
    finally
    {
      if( gr != null )
        gr.dispose();
    }
  }

  /** @return null, iff not all data was loaded in time. */
  private GisTemplateMapModell loadMapAndWaitForData( final Gismapview gismapview, final URL resource, final String crs, final TimeLogger logger, final IFeatureSelectionManager selectionManager )
  {
    logger.takeInterimTime();
    logger.printCurrentInterim( "Loading map modell..." );

    final GisTemplateMapModell mapModell = new GisTemplateMapModell( resource, crs, null, selectionManager );
    try
    {
      mapModell.createFromTemplate( gismapview );
    }
    catch( final Exception e1 )
    {
      e1.printStackTrace();
    }

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
    logger.takeInterimTime();
    logger.printCurrentInterim( "Map modell and data loaded." );

    return mapModell;
  }

  private boolean isLoaded( final IMapModell m_modell )
  {
    if( m_modell == null )
      return false;

    final IKalypsoTheme[] themes = m_modell.getAllThemes();
    for( final IKalypsoTheme theme : themes )
    {
      if( !theme.isLoaded() )
        return false;
    }
    return true;
  }

  private void gmlLoading( final URL gmlResource ) throws Exception
  {
    final TimeLogger logger = new TimeLogger( "Starting test" );

    logger.takeInterimTime();
    logger.printCurrentInterim( "Loading workspace first time: " );

    GmlSerializer.createGMLWorkspace( gmlResource, null );

    logger.takeInterimTime();
    logger.printCurrentInterim( "Workspace was loaded: " );

    // now test it several times
    for( int i = 0; i < 10; i++ )
    {
      logger.takeInterimTime();

      GmlSerializer.createGMLWorkspace( gmlResource, null );

      logger.takeInterimTime();
      logger.printCurrentInterim( "Workspace # " + i + " loaded: " );
    }

    logger.takeInterimTime();
    logger.printCurrentTotal( "TOTAL: " );
  }
}