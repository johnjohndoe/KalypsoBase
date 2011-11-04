package org.kalypso.gml.processes.constDelaunay.DelaunayImpl;

import org.kalypsodeegree.model.geometry.GM_Position;

/*
 * Point class.  RealPoint to avoid clash with java.awt.Point.
 */
class RealPoint
{
  private double x;

  private double y;

  private double z;

  private int m_intAdditionalInfo;

  RealPoint( )
  {
    x = y = 0.0f;
  }

  RealPoint( final GM_Position pPosition, final int pIntAdditionalInfo )
  {
    x = pPosition.getX();
    y = pPosition.getY();
    z = pPosition.getZ();
    m_intAdditionalInfo = pIntAdditionalInfo;
  }

  RealPoint( final double pDoubleX, final double pDoubleY, final double pDoubleZ, final int pIntAdditionalInfo )
  {
    x = pDoubleX;
    y = pDoubleY;
    z = pDoubleZ;
    m_intAdditionalInfo = pIntAdditionalInfo;
  }

  RealPoint( final RealPoint p )
  {
    x = p.getX();
    y = p.getY();
    z = p.getZ();
    m_intAdditionalInfo = p.getIntAdditionalInfo();
  }

  public final int getIntAdditionalInfo( )
  {
    return m_intAdditionalInfo;
  }

  public final void setIntAdditionalInfo( final int pIntAdditionalInfo )
  {
    m_intAdditionalInfo = pIntAdditionalInfo;
  }

  public double getX( )
  {
    return x;
  }

  public double getY( )
  {
    return y;
  }

  public final double getZ( )
  {
    return z;
  }

  public final void setZ( final double pDoubleZ )
  {
    z = pDoubleZ;
  }

  public void set( final double pDoubleX, final double pDoubleY )
  {
    x = pDoubleX;
    y = pDoubleY;
  }

  public void set( final double pDoubleX, final double pDoubleY, final double pDoubleZ )
  {
    x = pDoubleX;
    y = pDoubleY;
    z = pDoubleZ;
  }

  public double distance2d( final RealPoint p )
  {
    double dx, dy;

    dx = p.getX() - x;
    dy = p.getY() - y;
    return Math.sqrt( dx * dx + dy * dy );
  }

  public double distance3d( final RealPoint p )
  {
    double dx, dy, dz;

    dx = p.getX() - x;
    dy = p.getY() - y;
    dz = p.getZ() - z;
    return Math.sqrt( dx * dx + dy * dy + dz * dz );
  }

  public double distanceSq2d( final RealPoint p )
  {
    double dx, dy;

    dx = p.getX() - x;
    dy = p.getY() - y;
    return dx * dx + dy * dy;
  }

  public double distanceSq3d( final RealPoint p )
  {
    double dx, dy, dz;

    dx = p.getX() - x;
    dy = p.getY() - y;
    dz = p.getZ() - z;
    return dx * dx + dy * dy + dz * dz;
  }
}