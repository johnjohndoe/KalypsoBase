package com.bce.gis.operation.hmo2fli;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Gibt zu einer Geo-Coordinate ein Double zurück.
 * 
 * @author belger
 */
public interface DoubleProvider
{
	public double getDouble( final Coordinate crd );
}
