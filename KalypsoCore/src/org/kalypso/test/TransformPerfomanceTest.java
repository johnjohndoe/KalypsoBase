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
package org.kalypso.test;

import java.io.File;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;
import org.kalypso.contribs.eclipse.ui.progress.SystemOutProgressMonitor;
import org.kalypso.ogc.gml.serialize.GmlSerializeException;
import org.kalypso.ogc.gml.serialize.ShapeSerializer;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Object;

/**
 * This test compares perfomance of transformations.
 * 
 * @author Holger Albert
 */
public class TransformPerfomanceTest
{
  /**
   * Test function.
   */
  @Test
  public void testPerfomance( ) throws Exception
  {
    /* Monitor. */
    SystemOutProgressMonitor monitor = new SystemOutProgressMonitor( new NullProgressMonitor() );
    monitor.beginTask( "Transformations Perfomance Test:", 500 );

    /* Load the test shape in its original coordinate system. */
    File sourceFile = new File( "C:/temp/kalypsohwv/dwd/shapes/hydrotope_mulde" );
    String sourceCRS = "EPSG:31467";
    String targetCRS = "EPSG:4326";

    /* Monitor. */
    monitor.subTask( String.format( "Loading test shape '%s'...", sourceFile.getAbsolutePath() ) );

    /* Load the workspace. */
    GMLWorkspace workspace = loadShape( sourceFile, sourceCRS );

    /* Monitor. */
    monitor.worked( 250 );
    monitor.subTask( "Transforming..." );

    /* Transform. */
    transformGeometries( workspace, targetCRS );

    /* Monitor. */
    monitor.worked( 250 );
    monitor.done();
  }

  private GMLWorkspace loadShape( File file, String coordinateSystem ) throws GmlSerializeException
  {
    return ShapeSerializer.deserialize( file.getAbsolutePath(), coordinateSystem );
  }

  private void transformGeometries( GMLWorkspace workspace, String targetCRS ) throws Exception
  {
    IGeoTransformer transformer = GeoTransformerFactory.getGeoTransformer( targetCRS );

    Feature rootFeature = workspace.getRootFeature();
    FeatureList features = (FeatureList) rootFeature.getProperty( ShapeSerializer.PROPERTY_FEATURE_MEMBER );
    for( Object object : features )
    {
      Feature feature = (Feature) object;
      GM_Object geometry = (GM_Object) feature.getProperty( new QName( feature.getFeatureType().getQName().getNamespaceURI(), ShapeSerializer.PROPERTY_GEOM ) );
      transformer.transform( geometry );
    }
  }
}