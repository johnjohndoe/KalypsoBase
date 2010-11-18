package com.bce.gis.operation.hmo2fli;

import java.awt.Component;
import java.io.File;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.gml.processes.i18n.Messages;
import org.kalypso.grid.DiffGeoValueProvider;
import org.kalypso.grid.DoubleFakeRaster;
import org.kalypso.grid.IGeoGrid;
import org.kalypso.grid.IGeoValueProvider;
import org.kalypso.ogc.gml.serialize.ShapeSerializer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;

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

  public static void transform( @SuppressWarnings("unused") //$NON-NLS-1$
  final Component parent, final File hmoUntenFile, final File hmoObenFile, final double rasterSize, final boolean bIsolines, final String shapeBase, final double[] grenzen, final boolean calcVolume ) throws Exception
  {
    LOG.entering( Hmo2Fli.class.getName(), "transform" ); //$NON-NLS-1$

// final ProgressMonitor pable = parent != null ? new ProgressMonitor( parent, "Fliesstiefenerstellung" ) : null;
    // TODO: show progress dialog
    final IProgressMonitor pable = new NullProgressMonitor();

    final HMOReader hmoReader = new HMOReader( GF );
    final LinearRing[] dgmUnten = hmoReader.readFile( hmoUntenFile, null );
    final LinearRing[] dgmOben = hmoReader.readFile( hmoObenFile, null );

    final Geometry gUnten = GF.createGeometryCollection( dgmUnten ).getEnvelope();
    final Geometry gOben = GF.createGeometryCollection( dgmOben ).getEnvelope();
    final Envelope e = gUnten.intersection( gOben ).getEnvelopeInternal();

// if( pable != null )
// pable.setNote( "Index für Geländemodell wird generiert" );
    final GetRingsOperation groUnten = new GetRingsOperation( dgmUnten, null );

// if( pable != null )
// pable.setNote( "Index für Wasserspiegel wird generiert" );
    final GetRingsOperation groOben = new GetRingsOperation( dgmOben, null );

    final int xCount = (int) (e.getWidth() / rasterSize) + 1;
    final int yCount = (int) (e.getHeight() / rasterSize) + 1;
    final Coordinate origin = new Coordinate( e.getMinX(), e.getMinY() );

    final IGeoValueProvider dgmDiffer = new DiffGeoValueProvider( groUnten, groOben );
    final Coordinate offsetX = new Coordinate( rasterSize, 0 );
    final Coordinate offsetY = new Coordinate( 0, rasterSize );
    final IGeoGrid raster = new DoubleFakeRaster( xCount, yCount, origin, offsetX, offsetY, dgmDiffer, null );

    final boolean bSimple = false;
    final SegmentCollector gc = bIsolines ? (SegmentCollector) new LineStringCollector( GF, grenzen, bSimple ) : new PolygonCollector( GF, grenzen, bSimple );
    final Raster2Lines r2l = new Raster2Lines( gc, grenzen );
// if( pable != null )
// pable.setNote( "Fliesstiefen werden ermittelt" );

    LOG.info( Messages.getString("com.bce.gis.operation.hmo2fli.Hmo2Fli.2") ); //$NON-NLS-1$
    raster.getWalkingStrategy().walk( raster, r2l, null, pable );
    LOG.info( Messages.getString("com.bce.gis.operation.hmo2fli.Hmo2Fli.3") ); //$NON-NLS-1$

    final NumberFormat numberInstance = NumberFormat.getNumberInstance();
    numberInstance.setGroupingUsed( true );
    numberInstance.setMinimumFractionDigits( 1 );
    numberInstance.setMaximumFractionDigits( 1 );
    final double volumen = r2l.getSum();
    LOG.info( Messages.getString("com.bce.gis.operation.hmo2fli.Hmo2Fli.4") + numberInstance.format( volumen ) + Messages.getString("com.bce.gis.operation.hmo2fli.Hmo2Fli.5") ); //$NON-NLS-1$ //$NON-NLS-2$

    // Shape schreiben
    final Feature fc = gc.getFeatures();
    final Object property = fc.getProperty( ShapeSerializer.PROPERTY_FEATURE_MEMBER );
    final FeatureList list = (FeatureList) property;

    if( list.size() == 0 )
      LOG.log( Level.WARNING, Messages.getString("com.bce.gis.operation.hmo2fli.Hmo2Fli.6") ); //$NON-NLS-1$
    else
    {
      if( gc instanceof PolygonCollector && calcVolume )
      {
// if( pable != null )
// pable.setNote( "Volumen wird ermittelt" );
        LOG.info( Messages.getString("com.bce.gis.operation.hmo2fli.Hmo2Fli.7") ); //$NON-NLS-1$
        // final FeatureCollectionIndex featureIndex = new FeatureCollectionIndex( fc );
        final FeatureVolumeRasterWalker volumeWalker = new FeatureVolumeRasterWalker( list, PolygonCollector.VOLUME_PROP.getQName() );
        raster.getWalkingStrategy().walk( raster, volumeWalker, null, pable );
        LOG.info( Messages.getString("com.bce.gis.operation.hmo2fli.Hmo2Fli.8") ); //$NON-NLS-1$
      }

      LOG.info( Messages.getString("com.bce.gis.operation.hmo2fli.Hmo2Fli.9") + shapeBase ); //$NON-NLS-1$
      // transform geometries to shapes and write it
      ShapeSerializer.serialize( fc.getWorkspace(), shapeBase, null );
// final ShapeFile sf = new ShapeFile( shapeBase, "rw" );
// sf.writeShape( fc );
// sf.close();
      LOG.info( Messages.getString("com.bce.gis.operation.hmo2fli.Hmo2Fli.10") + shapeBase ); //$NON-NLS-1$
    }

// if( pable != null )
// pable.cancel();

    LOG.exiting( Hmo2Fli.class.getName(), "transform" ); //$NON-NLS-1$
  }
}
