/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.commons.image;

import java.util.Date;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.formats.tiff.TiffDirectory;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.apache.sanselan.formats.tiff.constants.GPSTagConstants;
import org.apache.sanselan.formats.tiff.constants.TagInfo;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Helper code for exif metadata of images.
 * 
 * @author Gernot Belger
 */
public final class ExifUtils
{
  private ExifUtils( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a value as double from the exif metadata.<br/>
   * Returns <code>null</code> is anything fails.
   */
  public static Double getQuietDouble( final TiffImageMetadata exif, final TagInfo tag )
  {
    try
    {
      final TiffField field = findField( exif, tag );
      if( field == null )
        return null;

      return field.getDoubleValue();
    }
    catch( final ClassCastException e )
    {
      // ignored
      return null;
    }
    catch( final ImageReadException e )
    {
      // e.printStackTrace();
      // ignored
      return null;
    }
  }

  /**
   * Searches for a field, first by directory (if defined in tag), than directly.
   */
  public static TiffField findField( final TiffImageMetadata exif, final TagInfo tag ) throws ImageReadException
  {
    if( tag.directoryType == null )
      return exif.findField( tag );

    // REMARK: exif.findField() may return a tag from the wrong directory
    final TiffDirectory directory = exif.findDirectory( tag.directoryType.directoryType );
    if( directory == null )
      return null;

    return directory.findField( tag );
  }

  /**
   * Gets a value as string from the exif metadata.<br/>
   * Returns <code>null</code> is anything fails.
   */
  public static String getQuietString( final TiffImageMetadata exif, final TagInfo tag )
  {
    try
    {
      final TiffField field = exif.findField( tag );
      if( field == null )
        return null;

      return field.getStringValue();
    }
    catch( final ImageReadException e )
    {
      // e.printStackTrace();
      // ignored
      return null;
    }
  }

  /**
   * Gets a value as inetegr from the exif metadata.<br/>
   * Returns <code>null</code> is anything fails.
   */
  public static Integer getQuietInteger( final TiffImageMetadata exif, final TagInfo tag )
  {
    try
    {
      final TiffField field = exif.findField( tag );
      if( field == null )
        return null;

      return field.getIntValue();
    }
    catch( final ImageReadException e )
    {
      // e.printStackTrace();
      // ignored
      return null;
    }
  }

  /**
   * Tries to retreive the location of the image as wgs84-coordinate from the exif metadata.<br/>
   * 
   * @return <code>null</code>, if either lat or lon cannot be retreived. Else a (possibly 3d) coordinate is returned,
   *         including the altitude.
   */
  public static Coordinate parseLocation( final TiffImageMetadata exif )
  {
    final Double lat = ExifUtils.getQuietDouble( exif, GPSTagConstants.GPS_TAG_GPS_LATITUDE );
    final String latRef = ExifUtils.getQuietString( exif, GPSTagConstants.GPS_TAG_GPS_LATITUDE_REF );
    final Double lon = ExifUtils.getQuietDouble( exif, GPSTagConstants.GPS_TAG_GPS_LONGITUDE );
    final String lonRef = ExifUtils.getQuietString( exif, GPSTagConstants.GPS_TAG_GPS_LATITUDE_REF );
    final Double height = ExifUtils.getQuietDouble( exif, GPSTagConstants.GPS_TAG_GPS_ALTITUDE );
    final Integer heightRef = ExifUtils.getQuietInteger( exif, GPSTagConstants.GPS_TAG_GPS_ALTITUDE_REF );

    final double latNormalized = normalizeLatLon( lat, latRef, GPSTagConstants.GPS_TAG_GPS_LATITUDE_REF_VALUE_SOUTH );
    final double lonNormalized = normalizeLatLon( lon, lonRef, GPSTagConstants.GPS_TAG_GPS_LONGITUDE_REF_VALUE_WEST );
    final double heightNormalized = normalizeLatLon( height, heightRef, GPSTagConstants.GPS_TAG_GPS_ALTITUDE_REF_VALUE_BELOW_SEA_LEVEL );

    if( Double.isNaN( latNormalized ) || Double.isNaN( lonNormalized ) )
      return null;

    return new Coordinate( lonNormalized, latNormalized, heightNormalized );
  }

  private static double normalizeLatLon( final Double latOrLon, final Object reference, final Object minusReference )
  {
    if( latOrLon == null || Double.isNaN( latOrLon ) )
      return Double.NaN;

    /* If we are south or west, invert sign */
    if( minusReference.equals( reference ) )
    {
      /* Paranoia: if already negative, leave sign alone */
      if( latOrLon < 0 )
        return latOrLon;

      return -latOrLon;
    }

    return latOrLon;
  }

  public static Double parseDirection( final TiffImageMetadata exif )
  {
    final Double direction = getQuietDouble( exif, GPSTagConstants.GPS_TAG_GPS_IMG_DIRECTION );
    if( direction == null )
      return null;

    final String reference = getQuietString( exif, GPSTagConstants.GPS_TAG_GPS_IMG_DIRECTION_REF );
    if( reference == null )
      return direction;

    if( reference.toUpperCase().startsWith( GPSTagConstants.GPS_TAG_GPS_IMG_DIRECTION_REF_VALUE_TRUE_NORTH ) )
      return direction;

    // TODO: will this ever happen?
    // is it possible to transform this value?

    return null;
  }

  // FOV = 2*arctan((SQRT(a*a + b*b)/2)/f);
  // Where SQRT = square root
  // a = lenght of sensor in mm
  // b = width of sensor in mm
  // f = focal length in mm
  public static Double parseAngleOfView( final TiffImageMetadata exif )
  {
    final Double width = getQuietDouble( exif, ExifTagConstants.EXIF_TAG_IMAGE_WIDTH );
    final Double height = getQuietDouble( exif, ExifTagConstants.EXIF_TAG_IMAGE_HEIGHT );
    final Double focalLength = getQuietDouble( exif, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH );

    if( width == null || height == null || focalLength == null )
      return null;

    return 2 * Math.atan( Math.sqrt( width * width + height * height ) / 2 / focalLength );
  }

  public static Date getQuietDate( final TiffImageMetadata exif, final TagInfo tag )
  {
    try
    {
      final TiffField dateField = ExifUtils.findField( exif, tag );
      if( dateField == null )
        return null;

      final org.apache.sanselan.formats.tiff.constants.TagInfo.Date fieldAsDate = new org.apache.sanselan.formats.tiff.constants.TagInfo.Date( tag.name, tag.tag, tag.dataTypes[0], tag.length );

      return (Date) fieldAsDate.getValue( dateField );
    }
    catch( final ImageReadException e )
    {
      // e.printStackTrace();
      // ignored
      return null;
    }
  }
}