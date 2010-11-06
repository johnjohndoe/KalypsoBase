package de.openali.odysseus.chart.framework.model.mapper;

import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.Point;

/**
 * @author alibu switch to return x- or y-coordinates according to the an orientation
 */
public interface ICoordinateSwitcher
{
  int toInt( );

  double getX( final Point2D point );

  double getY( final Point2D point );

  int getX( final Point point );

  int getY( final Point point );
}
