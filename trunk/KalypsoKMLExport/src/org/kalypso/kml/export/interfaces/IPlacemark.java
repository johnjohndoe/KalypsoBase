package org.kalypso.kml.export.interfaces;

public interface IPlacemark
{
  String getName( );

  String getDescription( );

  Double getX( String targetCRS ) throws Exception;

  Double getY( String targetCRS ) throws Exception;

}
