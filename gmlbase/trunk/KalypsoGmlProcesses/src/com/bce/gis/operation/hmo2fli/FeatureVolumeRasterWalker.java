package com.bce.gis.operation.hmo2fli;

import java.util.List;

import javax.xml.namespace.QName;

import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.IGeoGrid;
import org.kalypso.grid.IGeoGridWalker;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Summiert das Gesamtvolumen des Rasters sowie die Einzelvolumina der Features.
 * 
 * @author belger
 */
public class FeatureVolumeRasterWalker implements IGeoGridWalker
{
  private final QName m_volumeProperty;

  private final FeatureList m_featureIndex;

  private double m_cellArea = 0.0;

  private int m_count = 0;

  /**
   * @param featureIndex
   */
  public FeatureVolumeRasterWalker( final FeatureList featureIndex, final QName volumeProperty )
  {
    m_featureIndex = featureIndex;
    m_volumeProperty = volumeProperty;
  }

  /**
   * @see org.kalypso.gis.doubleraster.DoubleRasterWalker#start(org.kalypso.gis.doubleraster.DoubleRaster)
   */
  public void start( final IGeoGrid r ) throws GeoGridException
  {
    final Coordinate offsetX = r.getOffsetX();
    final Coordinate offsetY = r.getOffsetY();
    m_cellArea = offsetX.x * offsetY.y - offsetX.y * offsetY.x;
  }

  /**
   * @see org.kalypso.gis.doubleraster.DoubleRasterWalker#operate(int, int, com.vividsolutions.jts.geom.Coordinate)
   */
  public void operate( final int x, final int y, final Coordinate c )
  {
    final double cellVolume = m_cellArea * c.z;

    if( !Double.isNaN( cellVolume ) )
    {
      final GM_Position position = GeometryFactory.createGM_Position( c.x, c.y );

      final List< ? > results = m_featureIndex.query( position, null );
      final Feature[] features = results.toArray( new Feature[] {} );
      // final Feature[] features = m_featureIndex.query( position );

      for( final Feature f : features )
      {
        if( f.getDefaultGeometryProperty().contains( position ) )
        {
          final Object oldVolumeObj = f.getProperty( m_volumeProperty );
          if( oldVolumeObj instanceof Double )
          {
            final double oldVolume = (Double) oldVolumeObj;

            final Double newVolume;
            if( Double.isNaN( oldVolume ) )
              newVolume = new Double( cellVolume );
            else
            {
              if( Math.signum( oldVolume ) != Math.signum( cellVolume ) )
              {
                m_count++;
              }

              newVolume = new Double( oldVolume + cellVolume );
            }

            // final FeatureProperty fp = m_featureFactory.createFeatureProperty( m_volumeProperty, newVolume );
            f.setProperty( m_volumeProperty, newVolume );
          }
        }
      }
    }
  }

  /**
   * @see org.kalypso.gis.doubleraster.DoubleRasterWalker#getResult()
   */
  public Object finish( )
  {
    return null;
  }
}
