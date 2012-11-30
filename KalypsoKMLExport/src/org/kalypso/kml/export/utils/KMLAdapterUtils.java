package org.kalypso.kml.export.utils;

import org.kalypso.kml.export.interfaces.IKMLAdapter;
import org.kalypsodeegree.model.feature.Feature;

public class KMLAdapterUtils
{
  public static String getFeatureName( final Feature feature, final IKMLAdapter[] providers )
  {
    for( final IKMLAdapter adapter : providers )
    {
      final String name = adapter.getFeatureName( feature );
      if( name != null )
        return name;
    }

    return feature.getId();
  }
}