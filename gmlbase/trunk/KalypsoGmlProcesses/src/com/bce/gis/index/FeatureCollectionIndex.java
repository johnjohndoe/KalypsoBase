package com.bce.gis.index;

import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.geometry.GM_Position;
import org.deegree_impl.model.geometry.GeometryFactory;

/**
 * Baut einen Index über einer FeatureCollection auf. Der index ist fix, also ändert sich nicht bei Änderungen in der
 * FeatureCollection.
 * 
 * @author belger
 */
public class FeatureCollectionIndex implements IFeatureIndex
{
  private SplitSort m_sort;

  public FeatureCollectionIndex( final FeatureCollection fc )
  {
    m_sort = new SplitSort( SplitSort.getEnvelope( new GeometryFactory(), fc.getFeature( 0 ) ) );
    final Feature[] allFeatures = fc.getAllFeatures();
    for( int i = 0; i < allFeatures.length; i++ )
    {
      final Feature feature = allFeatures[i];
      m_sort.add( feature );
    }

    m_sort.resort();
  }

  public Feature[] query( final GM_Position point )
  {
    return m_sort.query( point, null ).toArray( new Feature[0] );
  }
}