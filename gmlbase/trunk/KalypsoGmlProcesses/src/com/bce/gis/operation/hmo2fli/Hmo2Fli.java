package com.bce.gis.operation.hmo2fli;

import java.awt.Component;
import java.io.File;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deegree.model.feature.FeatureCollection;
import org.deegree_impl.io.shpapi.ShapeFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.bce.gis.doubleraster.DoubleFakeRaster;
import com.bce.gis.doubleraster.DoubleRaster;
import com.bce.gis.doubleraster.walker.FeatureVolumeRasterWalker;
import com.bce.gis.index.FeatureCollectionIndex;
import com.bce.gis.io.hmo.HMOReader;
import com.bce.gis.operation.raster2vector.Raster2Lines;
import com.bce.gis.operation.raster2vector.SegmentCollector;
import com.bce.gis.operation.raster2vector.collector.LineStringCollector;
import com.bce.gis.operation.raster2vector.collector.PolygonCollector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * Operation um aus 2 HMO eine Tiefenkartierung zu erzeugen
 * 
 * @author belger
 */
public class Hmo2Fli
{
  private final static GeometryFactory GF = new GeometryFactory();

  private final static Logger LOG = Logger.getLogger( Hmo2Fli.class.getName() );

  public static void transform( final Component parent, final File hmoUntenFile, final File hmoObenFile, final double rasterSize, final boolean bIsolines, final String shapeBase, final double[] grenzen, final boolean calcVolume ) throws Exception
  {
    LOG.entering( Hmo2Fli.class.getName(), "transform" );

//    final ProgressMonitor pable = parent != null ? new ProgressMonitor( parent, "Fliesstiefenerstellung" ) : null;
    // TODO: show progress dialog 
    final IProgressMonitor pable = new NullProgressMonitor();

    final HMOReader hmoReader = new HMOReader( GF );
    final LinearRing[] dgmUnten = hmoReader.readFile( hmoUntenFile, null );
    final LinearRing[] dgmOben = hmoReader.readFile( hmoObenFile, null );

    final Geometry gUnten = GF.createGeometryCollection( dgmUnten ).getEnvelope();
    final Geometry gOben = GF.createGeometryCollection( dgmOben ).getEnvelope();
    final Envelope e = gUnten.intersection( gOben ).getEnvelopeInternal();

//    if( pable != null )
//      pable.setNote( "Index für Geländemodell wird generiert" );
    final GetRingsOperation groUnten = new GetRingsOperation( dgmUnten, null );

//    if( pable != null )
//      pable.setNote( "Index für Wasserspiegel wird generiert" );
    final GetRingsOperation groOben = new GetRingsOperation( dgmOben, null );

    final int xCount = (int) (e.getWidth() / rasterSize) + 1;
    final int yCount = (int) (e.getHeight() / rasterSize) + 1;
    final Coordinate origin = new Coordinate( e.getMinX(), e.getMinY() );

    final DoubleProvider dgmDiffer = new DGMDiffer( groUnten, groOben );
    final Coordinate offsetX = new Coordinate( rasterSize, 0 );
    final Coordinate offsetY = new Coordinate( 0, rasterSize );
    final DoubleRaster raster = new DoubleFakeRaster( xCount, yCount, origin, offsetX, offsetY, dgmDiffer );

    final boolean bSimple = false;
    final SegmentCollector gc = bIsolines ? (SegmentCollector) new LineStringCollector( GF, grenzen, bSimple ) : new PolygonCollector( GF, grenzen, bSimple );
    final Raster2Lines r2l = new Raster2Lines( gc, grenzen );
//    if( pable != null )
//      pable.setNote( "Fliesstiefen werden ermittelt" );

    LOG.info( "Start creating flow depths" );
    raster.walk( r2l, pable );
    LOG.info( "Finished creating flow depths" );

    final NumberFormat numberInstance = NumberFormat.getNumberInstance();
    numberInstance.setGroupingUsed( true );
    numberInstance.setMinimumFractionDigits( 1 );
    numberInstance.setMaximumFractionDigits( 1 );
    final double volumen = r2l.getSum();
    LOG.info( "Total volume of flooded area is: " + numberInstance.format( volumen ) + " [m³]" );

    // Shape schreiben
    final FeatureCollection fc = gc.getFeatures();
    if( fc.getSize() == 0 )
      LOG.log( Level.WARNING, "No features created" );
    else
    {
      if( gc instanceof PolygonCollector && calcVolume )
      {
//        if( pable != null )
//          pable.setNote( "Volumen wird ermittelt" );
        LOG.info( "Start calculation of shape volumes" );
        final FeatureCollectionIndex featureIndex = new FeatureCollectionIndex( fc );
        final FeatureVolumeRasterWalker volumeWalker = new FeatureVolumeRasterWalker( featureIndex, PolygonCollector.VOLUME_PROP.getName() );
        raster.walk( volumeWalker, pable );
        LOG.info( "Finished calculation of shape volumes" );
      }

      LOG.info( "Start writing shape: " + shapeBase );
      // transform geometries to shapes and write it
      final ShapeFile sf = new ShapeFile( shapeBase, "rw" );
      sf.writeShape( fc );
      sf.close();
      LOG.info( "Finished writing shape: " + shapeBase );
    }

//    if( pable != null )
//      pable.cancel();

    LOG.exiting( Hmo2Fli.class.getName(), "transform" );
  }
}
