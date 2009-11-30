package org.kalypso.kml.export.utils;

import java.util.List;

import javax.xml.bind.JAXBElement;

import net.opengis.kml.AbstractFeatureType;
import net.opengis.kml.FolderType;
import net.opengis.kml.GroundOverlayType;
import net.opengis.kml.PlacemarkType;

public class FolderUtil
{

  public static void removeEmptyFolders( final FolderType folderType )
  {
    final List<JAXBElement< ? extends AbstractFeatureType>> features = folderType.getAbstractFeatureGroup();
    final Object[] myFeatures = features.toArray();

    for( final Object obj : myFeatures )
    {
      if( !(obj instanceof JAXBElement) )
      {
        continue;
      }
      final JAXBElement< ? > element = (JAXBElement< ? >) obj;

      final AbstractFeatureType featureType = (AbstractFeatureType) element.getValue();
      if( featureType instanceof FolderType )
      {
        final FolderType myFolderType = (FolderType) featureType;
        if( isEmptyFolder( myFolderType ) )
          features.remove( obj );
      }
    }

  }

  private static boolean isEmptyFolder( final FolderType base )
  {
    final List<JAXBElement< ? extends AbstractFeatureType>> features = base.getAbstractFeatureGroup();

    boolean isEmpty = true;

    final Object[] myFeatures = features.toArray();
    for( final Object obj : myFeatures )
    {
      if( !(obj instanceof JAXBElement) )
      {
        continue;
      }
      final JAXBElement< ? > element = (JAXBElement< ? >) obj;

      final AbstractFeatureType featureType = (AbstractFeatureType) element.getValue();
      if( featureType instanceof FolderType )
      {
        final FolderType myFolderType = (FolderType) featureType;
        final boolean empty = isEmptyFolder( myFolderType );
        if( empty == false )
          isEmpty = false;
        else
        {
          features.remove( element );
        }
      }
      else if( featureType instanceof PlacemarkType )
        return false;
      else if( featureType instanceof GroundOverlayType )
        return false;
    }

    return isEmpty;
  }

}