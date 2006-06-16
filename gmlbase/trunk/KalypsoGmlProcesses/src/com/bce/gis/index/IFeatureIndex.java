package com.bce.gis.index;

import org.deegree.model.feature.Feature;
import org.deegree.model.geometry.GM_Position;

/**
 * Index über eine FeatureCollection
 * 
 * @author belger
 */
public interface IFeatureIndex
{
  /** Return all feature, which geometry contains this point */
  public Feature[] query( final GM_Position point );
}
