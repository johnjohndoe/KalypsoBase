package org.kalypso.kml.export.utils;

import org.apache.commons.lang.NotImplementedException;

import de.micromata.opengis.kml.v_2_2_0.Folder;

public class FolderUtil
{

  public static void removeEmptyFolders( final Folder folderType )
  {
    throw new NotImplementedException();
// final List<JAXBElement< ? extends AbstractFeatureType>> features = folderType.getAbstractFeatureGroup();
// final Object[] myFeatures = features.toArray();
//
// for( final Object obj : myFeatures )
// {
// if( !(obj instanceof JAXBElement) )
// {
// continue;
// }
// final JAXBElement< ? > element = (JAXBElement< ? >) obj;
//
// final AbstractFeatureType featureType = (AbstractFeatureType) element.getValue();
// if( featureType instanceof FolderType )
// {
// final FolderType myFolderType = (FolderType) featureType;
// if( isEmptyFolder( myFolderType ) )
// features.remove( obj );
// }
// }

  }

  private static boolean isEmptyFolder( final Folder base )
  {
    throw new NotImplementedException();
// final List<JAXBElement< ? extends AbstractFeatureType>> features = base.getAbstractFeatureGroup();
//
// boolean isEmpty = true;
//
// final Object[] myFeatures = features.toArray();
// for( final Object obj : myFeatures )
// {
// if( !(obj instanceof JAXBElement) )
// {
// continue;
// }
// final JAXBElement< ? > element = (JAXBElement< ? >) obj;
//
// final AbstractFeatureType featureType = (AbstractFeatureType) element.getValue();
// if( featureType instanceof FolderType )
// {
// final FolderType myFolderType = (FolderType) featureType;
// final boolean empty = isEmptyFolder( myFolderType );
// if( empty == false )
// isEmpty = false;
// else
// {
// features.remove( element );
// }
// }
// else if( featureType instanceof PlacemarkType )
// return false;
// else if( featureType instanceof GroundOverlayType )
// return false;
// }
//
// return isEmpty;
  }

}