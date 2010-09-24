package org.kalypso.kml.export.utils;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.kml.export.KalypsoKMLPlugin;
import org.kalypso.kml.export.Messages;
import org.kalypso.kml.export.interfaces.IKMLAdapter;
import org.kalypso.kml.export.interfaces.IPlacemark;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;

public class PlacemarkUtil
{

  public static void addAdditional( final Folder base, final IKMLAdapter[] provider )
  {
    /* add additional place marks and clean up providers */
    final List<IPlacemark> placemarks = new ArrayList<IPlacemark>();
    for( final IKMLAdapter adapter : provider )
    {
      final IPlacemark[] placemarkers = adapter.getAdditionalPlacemarkers();
      for( final IPlacemark placemark : placemarkers )
      {
        placemarks.add( placemark );
      }

      adapter.cleanUp();
    }

    // add additional layer
    final Folder folder = base.createAndAddFolder();
    folder.setName( Messages.PlacemarkUtil_0 );

    for( final IPlacemark placemark : placemarks )
    {
      try
      {
        final Placemark kmlPlaceMark = folder.createAndAddPlacemark();
        kmlPlaceMark.setName( placemark.getName() );
        kmlPlaceMark.setDescription( placemark.getDescription() );

        final Point point = new Point();
        point.addToCoordinates( placemark.getX( GoogleEarthUtils.GOOGLE_EARTH_CS ), placemark.getY( GoogleEarthUtils.GOOGLE_EARTH_CS ) );
      }
      catch( final Exception e )
      {
        KalypsoKMLPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }

    }
  }
}
