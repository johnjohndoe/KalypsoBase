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
package org.kalypso.ogc.gml.serialize.test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.commons.performance.TimeLogger;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.ogc.gml.serialize.ShapeSerializer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.gml.binding.shape.ShapeCollection;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.geometry.GM_Envelope_Impl;

/**
 * @author Gernot Belger
 */
public class SpatialIndexTest extends TestCase
{
  private final List<File> m_filesToDelete = new ArrayList<File>();

  @Override
  protected void tearDown( ) throws Exception
  {
    for( final File file : m_filesToDelete )
      FileUtils.forceDelete( file );

    super.tearDown();
  }

  public void testBigShape( ) throws Exception
  {
    doTheTest( "resources/bigShape.zip", "mod", ShapeCollection.MEMBER_FEATURE ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  // invalid test, depends on KalypsoModel1d2d
// public void testBigGml( ) throws Exception
// {
//    doTheTest( "resources/bigGml.zip", "nodeResult.gml", new QName( "http://www.tu-harburg.de/wb/kalypso/schemata/1d2dResults", "nodeResultMember" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
// }

  private void doTheTest( final String zipResourcePath, final String filename, final QName propQName ) throws Exception
  {
    final TimeLogger logger = new TimeLogger( "Start spatial index test" ); //$NON-NLS-1$
    final GMLWorkspace workspace = loadWorkspace( zipResourcePath, filename );
    logger.takeInterimTime();
    logger.printCurrentInterim( "File loaded in: " ); //$NON-NLS-1$

    final FeatureList sort = FeatureFactory.createFeatureList( null, null );

    final FeatureList featureList = (FeatureList) workspace.getRootFeature().getProperty( propQName );
    for( final Object object : featureList )
      sort.add( object );

    logger.takeInterimTime();
    logger.printCurrentInterim( "Index built in: " ); //$NON-NLS-1$

    final GM_Envelope boundingBox = featureList.getBoundingBox();

    sort.query( boundingBox, null );
    logger.takeInterimTime();
    logger.printCurrentInterim( "Index queried in: " ); //$NON-NLS-1$

    sort.query( boundingBox, null );
    logger.takeInterimTime();
    logger.printCurrentInterim( "Index queried again in: " ); //$NON-NLS-1$

    sort.invalidate( featureList.first() );
    logger.takeInterimTime();
    logger.printCurrentInterim( "Index invalidated in: " ); //$NON-NLS-1$

    sort.query( boundingBox, null );
    logger.takeInterimTime();
    logger.printCurrentInterim( "Index queried again in: " ); //$NON-NLS-1$

    queryOften( sort, boundingBox );
    logger.takeInterimTime();
    logger.printCurrentInterim( "Index queried often in: " ); //$NON-NLS-1$

    logger.printCurrentTotal( "Total: " ); //$NON-NLS-1$
  }

  private void queryOften( final FeatureList sort, final GM_Envelope boundingBox )
  {
    final double bMinX = boundingBox.getMinX();
    final double bMinY = boundingBox.getMinY();
    final double bMaxX = boundingBox.getMaxX();
    final double bMaxY = boundingBox.getMaxY();

    final double xRange = Math.abs( bMaxX - bMinX );
    final double yRange = Math.abs( bMaxY - bMinY );

    int maxSize = 0;
    final int numberOfQueries = 1000;

    for( int i = 0; i < numberOfQueries; i++ )
    {
      final double minX = bMinX + Math.random() * xRange;
      final double minY = bMinY + Math.random() * yRange;
      final double maxX = bMinX + Math.random() * xRange;
      final double maxY = bMinY + Math.random() * yRange;

      final GM_Envelope_Impl env = new GM_Envelope_Impl( minX, minY, maxX, maxY, boundingBox.getCoordinateSystem() );
      final List< ? > result = sort.query( env, null );
      maxSize = Math.max( result.size(), maxSize );
    }

    System.out.println( "Max result size: " + maxSize );
  }

  private GMLWorkspace loadWorkspace( final String relativeResourcePath, final String filename ) throws Exception
  {
    final URL resource = getClass().getResource( relativeResourcePath );

    final File unzipDir = FileUtilities.createNewTempDir( "unzip" ); //$NON-NLS-1$

    ZipUtilities.unzip( resource, unzipDir );

    final File fileBase = new File( unzipDir, filename );

    if( filename.toLowerCase().endsWith( ".gml" ) ) //$NON-NLS-1$
      return GmlSerializer.createGMLWorkspace( fileBase.toURI().toURL(), null );

    final ShapeCollection shapes = ShapeSerializer.deserialize( fileBase.getAbsolutePath(), KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );

    FileUtils.deleteDirectory( unzipDir );

    return shapes.getWorkspace();
  }
}
