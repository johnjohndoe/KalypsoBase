package org.kalypso.gml.processes.constDelaunay.DelaunayImpl;

/*
 * Vector class.  A few elementary vector operations.
 */
class Vector
{
  private double u, v;

  Vector( )
  {
    u = v = 0.0f;
  }

  Vector( final RealPoint p1, final RealPoint p2 )
  {
    u = p2.getX() - p1.getX();
    v = p2.getY() - p1.getY();
  }

  Vector( final double pDoubleU, final double pDoubleV )
  {
    this.u = pDoubleU;
    this.v = pDoubleV;
  }

  double dotProduct( final Vector pVector )
  {
    return u * pVector.u + this.v * pVector.v;
  }

  static double dotProduct( final RealPoint p1, final RealPoint p2, final RealPoint p3 )
  {
    double u1, v1, u2, v2;

    u1 = p2.getX() - p1.getX();
    v1 = p2.getY() - p1.getY();
    u2 = p3.getX() - p1.getX();
    v2 = p3.getY() - p1.getY();

    return u1 * u2 + v1 * v2;
  }

  double crossProduct( final Vector pVector )
  {
    return u * pVector.v - this.v * pVector.u;
  }

  static double crossProduct( final RealPoint p1, final RealPoint p2, final RealPoint p3 )
  {
    double u1, v1, u2, v2;

    u1 = p2.getX() - p1.getX();
    v1 = p2.getY() - p1.getY();
    u2 = p3.getX() - p1.getX();
    v2 = p3.getY() - p1.getY();

    return u1 * v2 - v1 * u2;
  }

  void setRealPoints( final RealPoint p1, final RealPoint p2 )
  {
    u = p2.getX() - p1.getX();
    v = p2.getY() - p1.getY();
  }
  
  double getLength(){
    return Math.sqrt( dotProduct( new Vector( u, v ) ) );
  }
}