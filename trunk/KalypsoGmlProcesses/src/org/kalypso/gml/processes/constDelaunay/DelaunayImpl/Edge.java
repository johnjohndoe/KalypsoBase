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
    s = pIntStart;
    t = pIntTarget;
  }

  public final int getS( )
  {
    return s;
  }

  public final int getT( )
  {
    return t;
  }

  public final int getL( )
  {
    return l;
  }

  public final int getR( )
  {
    return r;
  }

  public final void setS( final int pIntS )
  {
    s = pIntS;
  }

  public final void setT( final int pIntT )
  {
    t = pIntT;
  }

  public final void setL( final int pIntL )
  {
    l = pIntL;
  }

  public final void setR( final int pIntR )
  {
    r = pIntR;
  }

}