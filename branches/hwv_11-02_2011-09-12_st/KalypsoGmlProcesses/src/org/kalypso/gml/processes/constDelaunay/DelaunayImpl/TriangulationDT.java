/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  The Implementation of "Delaunay TriangulationDT" based on Geoff's Leach (gl@cs.rmit.edu.au) Triangulator plugin
 *  RMIT
 *  ---------------------------------------------------------------------------*/


package org.kalypso.gml.processes.constDelaunay.DelaunayImpl;

import java.util.ArrayList;
import java.util.List;

import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/*
 * TriangulationDT class.  A triangulation is represented as a set of
 * points and the edges which form the triangulation.
 */
final public class TriangulationDT
{
  static final int Undefined = -1;

  static final int Universe = 0;

  private int m_intAmountOfPoints;

  private RealPoint m_arrayPoints[];

  private int m_intNumberOfEdges;

  private int m_intMaxEdges;

  private List<GM_Triangle> m_listGMTriangles;

  private Edge m_arrayEdges[];

  private String m_crs;

  public TriangulationDT( final GM_Position[] pPositions, final String pCrs )
  {
    init( pPositions, pCrs );
  }

  private void init( final GM_Position[] pPositions, final String pCrs )
  {
    List<GM_Position> lListTmp = new ArrayList<GM_Position>();
    int i = 0;
    while( i < pPositions.length )
    {
      GM_Position lGM_Position = pPositions[i];
      if( !lListTmp.contains( lGM_Position ) )
      {
        lListTmp.add( lGM_Position );
      }
      ++i;
    }

    this.m_intAmountOfPoints = lListTmp.size();
    this.m_arrayPoints = new RealPoint[m_intAmountOfPoints];
    i = 0;
    for( final GM_Position lGM_Position : lListTmp )
    {
// point[i] = new RealPoint( lGM_Position.getX(), lGM_Position.getY(), lGM_Position.getZ(), 0 );
      m_arrayPoints[i] = new RealPoint( lGM_Position, 0 );
      ++i;
    }

    m_crs = pCrs;

    // Allocate edges.
    m_intMaxEdges = 3 * m_intAmountOfPoints - 6; // Max number of edges.
    m_arrayEdges = new Edge[m_intMaxEdges];

    i = 0;
    for( ; i < m_intMaxEdges; i++ )
    {
      m_arrayEdges[i] = new Edge();
    }
    m_intNumberOfEdges = 0;
    m_listGMTriangles = new ArrayList<GM_Triangle>();
  }

  /*
   * Copies a set of points.
   */
  public void copyPoints( final TriangulationDT t )
  {
    int n;

    if( t.m_intAmountOfPoints < m_intAmountOfPoints )
      n = t.m_intAmountOfPoints;
    else
      n = m_intAmountOfPoints;

    for( int i = 0; i < n; i++ )
    {
      m_arrayPoints[i].set( t.m_arrayPoints[i].getX(), t.m_arrayPoints[i].getY(), t.m_arrayPoints[i].getZ() );
    }

    m_intNumberOfEdges = 0;
  }

  public final int getAmountOfPoints( )
  {
    return m_intAmountOfPoints;
  }

  public final void setAmountOfPoints( final int pIntAmountOfPoints )
  {
    m_intAmountOfPoints = pIntAmountOfPoints;
  }

  void addTriangle( int s, int t, int u )
  {
    int lIntTmpRes = 0;
    lIntTmpRes += addEdge( s, t );
    lIntTmpRes += addEdge( t, u );
    lIntTmpRes += addEdge( u, s );
    addTriangleToList( s, t, u );
  }

  void addTriangleToList( final int s, final int t, final int u )
  {

    if( u == Undefined || s == Undefined || t == Undefined || s == t || t == u || s == u )
    {
      return;
    }

    try
    {
      GM_Triangle lTriangle = createTriangle( s, t, u );
      if( lTriangle != null )
        m_listGMTriangles.add( lTriangle );
    }
    catch( Throwable e )
    {
      //
    }

  }

  private GM_Triangle createTriangle( final int s, final int t, final int u ) throws GM_Exception
  {
    return createTriangle( m_arrayPoints[s], m_arrayPoints[t], m_arrayPoints[u] );
  }

  private GM_Triangle createTriangle( final RealPoint pPoint0, final RealPoint pPoint1, final RealPoint pPoint2 ) throws GM_Exception
  {
    GM_Triangle lTriangle = null;
    GM_Position lPosition0 = GeometryFactory.createGM_Position( pPoint0.getX(), pPoint0.getY(), pPoint0.getZ() );
    GM_Position lPosition1 = GeometryFactory.createGM_Position( pPoint1.getX(), pPoint1.getY(), pPoint1.getZ() );
    GM_Position lPosition2 = GeometryFactory.createGM_Position( pPoint2.getX(), pPoint2.getY(), pPoint2.getZ() );
    {
      lTriangle = GeometryFactory.createGM_Triangle( lPosition0, lPosition1, lPosition2, m_crs );
    }
    return lTriangle;
  }

  public int addEdge( final int s, final int t )
  {
    return addEdge( s, t, Undefined, Undefined );
  }

  /*
   * Adds an edge to the triangulation. Store edges with lowest vertex first (easier to debug and makes no other
   * difference).
   */
  public int addEdge( final int s, final int t, final int l, final int r )
  {
    int e;

    // Add edge if not already in the triangulation.
    e = findEdge( s, t );
    if( e == Undefined )
    {
      if( s < t )
      {
        m_arrayEdges[m_intNumberOfEdges].setS( s );
        m_arrayEdges[m_intNumberOfEdges].setT( t );
        m_arrayEdges[m_intNumberOfEdges].setL( l );
        m_arrayEdges[m_intNumberOfEdges].setR( r );
        
      }
      else
      {
        m_arrayEdges[m_intNumberOfEdges].setS( t );
        m_arrayEdges[m_intNumberOfEdges].setT( s );
        m_arrayEdges[m_intNumberOfEdges].setL( r );
        m_arrayEdges[m_intNumberOfEdges].setR( l );

      }

      return ++m_intNumberOfEdges;
    }
    else
      return Undefined;
  }

  public int findEdge( final int s, final int t )
  {
    boolean edgeExists = false;
    int i;

    for( i = 0; i < m_intNumberOfEdges; i++ )
      if( m_arrayEdges[i].getS() == s && m_arrayEdges[i].getT() == t || m_arrayEdges[i].getS() == t && m_arrayEdges[i].getT() == s )
      {
        edgeExists = true;
        break;
      }

    if( edgeExists )
      return i;
    else
      return Undefined;
  }

  /*
   * Update the left face of an edge.
   */
  public void updateLeftFace( final int eI, final int s, final int t, final int f )
  {
    if( !((m_arrayEdges[eI].getS() == s && m_arrayEdges[eI].getT() == t) || (m_arrayEdges[eI].getS() == t && m_arrayEdges[eI].getT() == s)) )
    {
      System.out.println( "updateLeftFace: adj. matrix and edge table mismatch" );
      return;
    }
    if( m_arrayEdges[eI].getS() == s && m_arrayEdges[eI].getL() == TriangulationDT.Undefined )
    {
      m_arrayEdges[eI].setL( f );
    }
    else if( m_arrayEdges[eI].getT() == s && m_arrayEdges[eI].getR() == TriangulationDT.Undefined )
    {
      m_arrayEdges[eI].setR( f );
    }
    else
      System.out.println( "updateLeftFace: attempt to overwrite edge info" );
  }

  public final List<GM_Triangle> getListGMTriangles( )
  {
    return m_listGMTriangles;
  }

  public final RealPoint[] getPoints( )
  {
    return m_arrayPoints;
  }

  public final int getNumberOfEdges( )
  {
    return m_intNumberOfEdges;
  }

  public final int getMaxEdges( )
  {
    return m_intMaxEdges;
  }

  public final Edge[] getEdges( )
  {
    return m_arrayEdges;
  }
  
  

}
