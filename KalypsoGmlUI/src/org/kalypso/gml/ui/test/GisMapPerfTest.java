/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.commons.performance.TimeLogger;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.selection.FeatureSelectionManager2;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.template.gismapview.Gismapview;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.graphics.transformation.WorldToScreenTransform;
import org.xml.sax.InputSource;

/**
 * Not for automatic testing.
 *
 * @author Gernot Belger
 */
public class GisMapPerfTest extends TestCase
{
  public static String[][] RESOURCES = { //
// { "wms2.zip", "wms.gmt" }, //
  { "dgm2m.zip", "dgm2m.gmt" }, // //$NON-NLS-1$ //$NON-NLS-2$
      { "points.zip", "points.gmt" }, // //$NON-NLS-1$ //$NON-NLS-2$
      { "tin.zip", "tin.gmt" }, // //$NON-NLS-1$ //$NON-NLS-2$
      { "tin_isolines.zip", "tin.gmt" }, // //$NON-NLS-1$ //$NON-NLS-2$
      { "polyWithHoles.zip", "poly.gmt" } // //$NON-NLS-1$ //$NON-NLS-2$
  };

  public void testZips( ) throws Exception
  {
    for( final String[] resource : RESOURCES )
    {
      final URL zipResource = getClass().getResource( "/etc/test/resources/" + resource[0] ); //$NON-NLS-1$
      final URL gmtResource = new URL( "jar:" + zipResource.toExternalForm() + "!/" + resource[1] ); //$NON-NLS-1$ //$NON-NLS-2$

      mapLoadingAndVisualization( gmtResource );
    }
  }

  private void mapLoadingAndVisualization( final URL gmtResource ) throws Exception
  {
    final TimeLogger logger = new TimeLogger( "Starting test: " + gmtResource.getPath() ); //$NON-NLS-1$

    final String crs = "EPSG:31467"; //$NON-NLS-1$

    logger.takeInterimTime();
    logger.printCurrentInterim( "Loading map: " ); //$NON-NLS-1$

    final URLConnection connection = gmtResource.openConnection();
    connection.setUseCaches( false );
    final InputStream inputStream = connection.getInputStream();
    final InputSource source = new InputSource( inputStream );
    source.setEncoding( "Cp1252" ); //$NON-NLS-1$
    final Gismapview gismapview = GisTemplateHelper.loadGisMapView( source );
    inputStream.close();

    logger.takeInterimTime();
    logger.printCurrentInterim( "Map was loaded. Creating map modell on map: " ); //$NON-NLS-1$

    final FeatureSelectionManager2 featureSelectionManager2 = new FeatureSelectionManager2();
    final GisTemplateMapModell modell = loadMap( gismapview, gmtResource, crs, logger, featureSelectionManager2 );
    if( modell == null )
      fail( "Map was not loaded" ); //$NON-NLS-1$

    logger.takeInterimTime();
    logger.printCurrentInterim( "Map data was loaded: " ); //$NON-NLS-1$

    final int width = 1024;
    final int heigth = 512;

    final GM_Envelope boundingBox = GisTemplateHelper.getBoundingBox( gismapview );
    final WorldToScreenTransform worldToScreen = new WorldToScreenTransform();
    worldToScreen.setSourceRect( boundingBox );
    worldToScreen.setDestRect( 0, 0, width, heigth, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );

    final BufferedImage image = new BufferedImage( width, heigth, BufferedImage.TYPE_INT_ARGB );
    for( int i = 0; i < 5; i++ )
    {
      logger.takeInterimTime();

      paintModel( image, modell, worldToScreen );

      logger.takeInterimTime();
      logger.printCurrentInterim( "Rendered map #" + i + ": " ); //$NON-NLS-1$ //$NON-NLS-2$

      if( i == 0 )
        ImageIO.write( image, "png", FileUtilities.getNewTempFile( "image", "png" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    logger.printCurrentTotal( "Total: " ); //$NON-NLS-1$

    modell.dispose();
  }

  private void paintModel( final BufferedImage image, final IMapModell model, final GeoTransform worldToScreen )
  {
    Graphics2D gr = null;
    try
    {
      gr = image.createGraphics();
      model.paint( gr, worldToScreen, new NullProgressMonitor() );
    }
    finally
    {
      if( gr != null )
        gr.dispose();
    }
  }

  private GisTemplateMapModell loadMap( final Gismapview gismapview, final URL resource, final String crs, final TimeLogger logger, final IFeatureSelectionManager selectionManager ) throws Exception
  {
    logger.takeInterimTime();
    logger.printCurrentInterim( "Loading map modell..." ); //$NON-NLS-1$

    final GisTemplateMapModell mapModell = new GisTemplateMapModell( resource, crs, selectionManager );
    mapModell.createFromTemplate( gismapview );

    return mapModell;
  }
}