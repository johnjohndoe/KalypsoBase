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
package org.kalypso.gml.processes.floodDepth;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.commons.xml.NS;
import org.kalypso.gml.processes.i18n.Messages;
import org.kalypso.gml.processes.schemata.GmlProcessesUrlCatalog;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.grid.DoubleDiffDoubleRaster;
import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.IGeoGrid;
import org.kalypso.grid.IGeoValueProvider;
import org.kalypso.grid.MinMaxRasterWalker;
import org.kalypso.grid.RectifiedGridCoverageGeoGrid;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.ogc.gml.serialize.ShapeSerializer;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.ISimulationDataProvider;
import org.kalypso.simulation.core.ISimulationMonitor;
import org.kalypso.simulation.core.ISimulationResultEater;
import org.kalypso.simulation.core.SimulationException;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;

import com.bce.gis.operation.hmo2fli.FeatureVolumeRasterWalker;
import com.bce.gis.operation.hmo2fli.TriangleDoubleProvider;
import com.bce.gis.operation.raster2vector.Raster2Lines;
import com.bce.gis.operation.raster2vector.SegmentCollector;
import com.bce.gis.operation.raster2vector.collector.LineStringCollector;
import com.bce.gis.operation.raster2vector.collector.PolygonCollector;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * @author Gernot Belger
 */
public class FloodDepthCalcJob implements ISimulation
{
  public enum TYPE
  {
    ISOPOLYGONES,
    ISOLINES;
  }

  /**
   * @see org.kalypso.simulation.core.ISimulation#getSpezifikation()
   */
  public URL getSpezifikation( )
  {
    return getClass().getResource( "FloodDepthCalcJob_spec.xml" ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.simulation.core.ISimulation#run(java.io.File, org.kalypso.simulation.core.ISimulationDataProvider,
   *      org.kalypso.simulation.core.ISimulationResultEater, org.kalypso.simulation.core.ISimulationMonitor)
   */
  public void run( final File tmpdir, final ISimulationDataProvider inputProvider, final ISimulationResultEater resultEater, final ISimulationMonitor monitor ) throws SimulationException
  {
    final URL triangleGmlURL = (URL) inputProvider.getInputForID( "TRIANGLES_GML" ); //$NON-NLS-1$
    final URL dtmGmlURL = (URL) inputProvider.getInputForID( "DTM_GML" ); //$NON-NLS-1$

    // zwischenschicht...

    //
    // Dreiecke
    //
    try
    {
      final File file = new File( tmpdir, "outshape" ); //$NON-NLS-1$
      final String shapeBase = FileUtilities.nameWithoutExtension( file.getAbsolutePath() );

      doFloodDepthIntersection( shapeBase, new NullProgressMonitor(), triangleGmlURL, dtmGmlURL, TYPE.ISOPOLYGONES, 0 );

      resultEater.addResult( "ResultShapeShp", new File( tmpdir, "outshape.shp" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      resultEater.addResult( "ResultShapeShx", new File( tmpdir, "outshape.shx" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      resultEater.addResult( "ResultShapeDbf", new File( tmpdir, "outshape.dbf" ) ); //$NON-NLS-1$ //$NON-NLS-2$

      // if( pable != null )
      // pable.cancel();

      // LOG.exiting( Hmo2Fli.class.getName(), "transform" );

    }
    catch( final Exception e )
    {
      throw new SimulationException( Messages.getString("org.kalypso.gml.processes.floodDepth.FloodDepthCalcJob.10"), e ); //$NON-NLS-1$
      // TODO: handle exception
    }

    // - Fake-Raster auf Raster+DoubleProvider machen

    // fliesstiefentool

    // evtl. min/max höhe rausfinden und grenzen generieren

    // ausgabe

    System.out.println( triangleGmlURL );
    System.out.println( dtmGmlURL );

  }

  /**
   * @param floodDepthStep
   *            Schrittweite
   */
  public static void doFloodDepthIntersection( final String shapeBase, final IProgressMonitor monitor, final URL triangleGmlURL, final URL dtmGmlURL, final TYPE floodDepthType, final double floodDepthStep ) throws Exception, GeoGridException, IOException, SimulationException
  {
    monitor.beginTask( Messages.getString("org.kalypso.gml.processes.floodDepth.FloodDepthCalcJob.11"), 12 ); //$NON-NLS-1$

    // - dreiecke lesen und als DoubleProvider wrappen
    final GMLWorkspace triangleWorkspace = GmlSerializer.createGMLWorkspace( triangleGmlURL, null );
    final Feature rootFeature = triangleWorkspace.getRootFeature();
    if( !GMLSchemaUtilities.substitutes( rootFeature.getFeatureType(), new QName( GmlProcessesUrlCatalog.NS_MESH, "TriangleCollection" ) ) ) //$NON-NLS-1$
      throw new IllegalArgumentException( Messages.getString("org.kalypso.gml.processes.floodDepth.FloodDepthCalcJob.13") + rootFeature ); //$NON-NLS-1$
    // TODO: retrieve xpath to feature list from user
    final FeatureList triangleList = (FeatureList) rootFeature.getProperty( new QName( GmlProcessesUrlCatalog.NS_MESH, "triangleMember" ) ); //$NON-NLS-1$

    final IGeoValueProvider triangleProvider = new TriangleDoubleProvider( triangleList );

    monitor.subTask( Messages.getString("org.kalypso.gml.processes.floodDepth.FloodDepthCalcJob.15") ); //$NON-NLS-1$
    // - raster lesen und als DoubleRaster wrappen
    final GMLWorkspace dtmWorkspace = GmlSerializer.createGMLWorkspace( dtmGmlURL, null );
    final Feature rgcFeature = dtmWorkspace.getRootFeature();
    // TODO: retrieve xpath to rgc from user
    if( !GMLSchemaUtilities.substitutes( rgcFeature.getFeatureType(), new QName( NS.GML3, Messages.getString("org.kalypso.gml.processes.floodDepth.FloodDepthCalcJob.16") ) ) ) //$NON-NLS-1$
      throw new IllegalArgumentException( Messages.getString("org.kalypso.gml.processes.floodDepth.FloodDepthCalcJob.17") + rgcFeature ); //$NON-NLS-1$

    final IGeoGrid raster = new RectifiedGridCoverageGeoGrid( rgcFeature );

    final IGeoGrid diffRaster = new DoubleDiffDoubleRaster( raster, triangleProvider );

    monitor.worked( 1 );

    // TODO: retrieve parameters from user via process input
    final boolean calcVolume = false;
    final boolean bSimple = false;
    // final double[] grenzen = new double[] { -1, 0, 1, 2, 3, 4, 5, 6 };
    final double[] grenzen;

    /* Min/Max Raster */
    if( floodDepthStep == 0 )
    {
      grenzen = new double[] { 0 };
      monitor.worked( 2 );
    }
    else
    {
      monitor.subTask( Messages.getString("org.kalypso.gml.processes.floodDepth.FloodDepthCalcJob.18") ); //$NON-NLS-1$
      final MinMaxRasterWalker minMaxWalker = new MinMaxRasterWalker();
      diffRaster.getWalkingStrategy().walk( diffRaster, minMaxWalker, null, new SubProgressMonitor( monitor, 2 ) );

      final double min = minMaxWalker.getMin();
      final double max = minMaxWalker.getMax();

      final Set<Double> grenzenSet = new HashSet<Double>();
      for( int i = 0; i >= min; i -= floodDepthStep )
        grenzenSet.add( new Double( i ) );

      for( int i = 0; i <= max; i += floodDepthStep )
        grenzenSet.add( new Double( i ) );

      grenzen = ArrayUtils.toPrimitive( grenzenSet.toArray( new Double[grenzenSet.size()] ) );
      // System.out.println( "Grenzen: " + ArrayUtils.toString( grenzen ) );
    }

    monitor.subTask( Messages.getString("org.kalypso.gml.processes.floodDepth.FloodDepthCalcJob.19") ); //$NON-NLS-1$
    final GeometryFactory GF = new GeometryFactory();

    final SegmentCollector gc = floodDepthType == TYPE.ISOLINES ? (SegmentCollector) new LineStringCollector( GF, grenzen, bSimple ) : new PolygonCollector( GF, grenzen, bSimple );
    final Raster2Lines r2l = new Raster2Lines( gc, grenzen );

    // LOG.info( "Start creating flow depths" );
    diffRaster.getWalkingStrategy().walk( diffRaster, r2l, null, new SubProgressMonitor( monitor, 5 ) );
    // LOG.info( "Finished creating flow depths" );

    monitor.subTask( Messages.getString("org.kalypso.gml.processes.floodDepth.FloodDepthCalcJob.20") ); //$NON-NLS-1$
    final NumberFormat numberInstance = NumberFormat.getNumberInstance();
    numberInstance.setGroupingUsed( true );
    numberInstance.setMinimumFractionDigits( 1 );
    numberInstance.setMaximumFractionDigits( 1 );
    // final double volumen = r2l.getSum();
    // LOG.info( "Total volume of flooded area is: " + numberInstance.format( volumen ) + " [m³]" );

    // Shape schreiben
    // TODO: we still use deegree api here, switch to kalypso-deegree
    final Feature fc = gc.getFeatures();
    final Object property = fc.getProperty( ShapeSerializer.PROPERTY_FEATURE_MEMBER );
    final FeatureList list = (FeatureList) property;

    if( list.size() == 0 )
    {
      // LOG.log( Level.WARNING, "No features created" );
      throw new SimulationException( Messages.getString("org.kalypso.gml.processes.floodDepth.FloodDepthCalcJob.21"), null ); //$NON-NLS-1$
    }
    else
    {
      if( gc instanceof PolygonCollector && calcVolume )
      {
        // if( pable != null )
        // pable.setNote( "Volumen wird ermittelt" );
        // LOG.info( "Start calculation of shape volumes" );
        // final FeatureCollectionIndex featureIndex = new FeatureCollectionIndex( fc );
        final FeatureVolumeRasterWalker volumeWalker = new FeatureVolumeRasterWalker( list, PolygonCollector.VOLUME_PROP.getQName() );
        raster.getWalkingStrategy().walk( raster, volumeWalker, null, new SubProgressMonitor( monitor, 3 ) );
        // LOG.info( "Finished calculation of shape volumes" );
      }

      // LOG.info( "Start writing shape: " + shapeBase );
      // transform geometries to shapes and write it

      monitor.subTask( Messages.getString("org.kalypso.gml.processes.floodDepth.FloodDepthCalcJob.22") ); //$NON-NLS-1$
      ShapeSerializer.serialize( fc.getWorkspace(), shapeBase, null );
      // final ShapeFile sf = new ShapeFile( shapeBase, "rw" );
      // sf.writeShape( fc );
      // sf.close();
      // LOG.info( "Finished writing shape: " + shapeBase );
    }
    monitor.done();
  }
}