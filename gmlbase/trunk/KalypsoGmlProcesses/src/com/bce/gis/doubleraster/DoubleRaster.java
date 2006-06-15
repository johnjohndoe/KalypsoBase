package com.bce.gis.doubleraster;

import com.bce.util.progressbar.Progressable;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author belger
 */
public interface DoubleRaster
{
  public double getValue( final int x, final int y );

  public double getValueChecked( final int x, final int y );

  public Coordinate getOrigin( );

  public double getRasterSize( );

  public int getSizeX( );

  public int getSizeY( );

  public Object walk( final DoubleRasterWalker pwo, final Progressable pable ) throws DoubleRasterException;
}
