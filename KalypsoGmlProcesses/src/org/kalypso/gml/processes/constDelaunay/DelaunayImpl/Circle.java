package org.kalypso.gml.processes.constDelaunay.DelaunayImpl;

/*
 * Circle class. Circles are fundamental to computation of Delaunay 
 * triangulations.  In particular, an operation which computes a 
 * circle defined by three points is required.
 */
class Circle
{
  private RealPoint m_realPointCenterc;

  private double m_doubleRadius;

  Circle( )
  {
    m_realPointCenterc = new RealPoint();
    m_doubleRadius = 0.0f;
  }

  Circle( final RealPoint pRealPointCenter, final double pDoubleRadius )
  {
    this.m_realPointCenterc = pRealPointCenter;
    this.m_doubleRadius = pDoubleRadius;
  }

  public RealPoint getCenterPoint( )
  {
    return m_realPointCenterc;
  }

  public double getRadius( )
  {
    return m_doubleRadius;
  }

  public void set( final RealPoint c, final double r )
  {
    this.m_realPointCenterc = c;
    this.m_doubleRadius = r;
  }

  /*
   * Tests if a point lies inside the circle instance.
   */
  public boolean isInside( final RealPoint p )
  {
    if( m_realPointCenterc.distanceSq2d( p ) < m_doubleRadius * m_doubleRadius )
      return true;
    else
      return false;
  }

  /*
   * Compute the circle defined by three points (circumcircle).
   */
  public void circumCircle( final RealPoint p1, final RealPoint p2, final RealPoint p3 )
  {
    double cp;

    cp = Vector.crossProduct( p1, p2, p3 );
    if( cp != 0.0 )
    {
      double p1Sq, p2Sq, p3Sq;
      double num;//, den;
      double cx, cy;

      p1Sq = p1.getX() * p1.getX() + p1.getY() * p1.getY();
      p2Sq = p2.getX() * p2.getX() + p2.getY() * p2.getY();
      p3Sq = p3.getX() * p3.getX() + p3.getY() * p3.getY();
      num = p1Sq * (p2.getY() - p3.getY()) + p2Sq * (p3.getY() - p1.getY()) + p3Sq * (p1.getY() - p2.getY());
      cx = num / (2.0f * cp);
      num = p1Sq * (p3.getX() - p2.getX()) + p2Sq * (p1.getX() - p3.getX()) + p3Sq * (p2.getX() - p1.getX());
      cy = num / (2.0f * cp);

      m_realPointCenterc.set( cx, cy );
    }

    // Radius
    m_doubleRadius = m_realPointCenterc.distance2d( p1 );
  }
}
