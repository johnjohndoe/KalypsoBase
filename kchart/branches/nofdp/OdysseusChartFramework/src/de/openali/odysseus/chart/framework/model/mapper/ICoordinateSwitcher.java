package de.openali.odysseus.chart.framework.model.mapper;

import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.Point;

/**
 * @author alibu switch to return x- or y-coordinates according to the an orientation
 */
public interface ICoordinateSwitcher
{
  public int toInt( );

  public double getX( final Point2D point );

  public double getY( final Point2D point );

  public int getX( final Point point );

  public int getY( final Point point );
}
