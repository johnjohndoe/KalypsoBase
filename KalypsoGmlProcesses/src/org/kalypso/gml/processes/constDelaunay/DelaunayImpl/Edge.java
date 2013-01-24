package org.kalypso.gml.processes.constDelaunay.DelaunayImpl;

/* 
 * Edge class. Edges have two vertices, s and t, and two faces,
 * l (left) and r (right). The triangulation representation and
 * the Delaunay triangulation algorithms require edges.
 */
class Edge
{
  private int s, t;

  private int l, r;

  Edge( )
  {
    s = t = 0;
  }

  Edge( final int pIntStart, final int pIntTarget )
  {
    this.s = pIntStart;
    this.t = pIntTarget;
  }

  public final int getS( )
  {
    return this.s;
  }

  public final int getT( )
  {
    return this.t;
  }

  public final int getL( )
  {
    return this.l;
  }

  public final int getR( )
  {
    return this.r;
  }

  public final void setS( final int pIntS )
  {
    this.s = pIntS;
  }

  public final void setT( final int pIntT )
  {
    this.t = pIntT;
  }

  public final void setL( final int pIntL )
  {
    this.l = pIntL;
  }

  public final void setR( final int pIntR )
  {
    this.r = pIntR;
  }
  
  
}