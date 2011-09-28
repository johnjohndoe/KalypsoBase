package org.kalypso.ogc.gml.serialize;

import org.kalypsodeegree.model.feature.FeatureList;

public interface IShapeWorkspace
{
  FeatureList getFeatureList( );

  void dispose( );
}
