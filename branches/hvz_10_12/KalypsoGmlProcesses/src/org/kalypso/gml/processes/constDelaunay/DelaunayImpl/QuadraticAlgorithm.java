package org.kalypso.gml.processes.constDelaunay.DelaunayImpl;

import java.util.ArrayList;
import java.util.List;

/*
 * QuadraticAlgorithm class. O(n^2) algorithm.
 */
final public class QuadraticAlgorithm extends TriangulationAlgorithm
{
  private int s, t, u, bP;

  private Circle m_Circle = new Circle();

  final static String STR_ALG_NAME = "QuadraticAlgorithm class. O(n^2)";

//  int nFaces;

  public QuadraticAlgorithm(){
    super( STR_ALG_NAME );
    init();
  }
  
  private void init(){
    //
  }

  @Override
  public void reset( )
  {
    super.reset();
  }

  /**
   * @see org.kalypso.gml.processes.constDelaunay.impl.TriangulationAlgorithm#triangulate(org.kalypso.gml.processes.constDelaunay.impl.TriangulationDT)
   */
  @Override
  public void triangulate( final TriangulationDT pTriangulationDT ) // synchronized
  {
    int currentEdge;
    Integer lIntNumberOfFaces;

    lIntNumberOfFaces = 0;

    // Find closest neighbours and add edge to triangulation.
    List<Integer> lListValues = new ArrayList<Integer>( 2 );
    findClosestNeighbours( pTriangulationDT.getPoints(), pTriangulationDT.getAmountOfPoints(), lListValues );// s, t );

    pTriangulationDT.addEdge( lListValues.get( 0 ), lListValues.get( 1 ), TriangulationDT.Undefined, TriangulationDT.Undefined );

    currentEdge = 0;
    while( currentEdge < pTriangulationDT.getNumberOfEdges() )
    {
      if( pTriangulationDT.getEdges()[currentEdge].getL() == TriangulationDT.Undefined )
      {
        completeFacet( currentEdge, pTriangulationDT, lIntNumberOfFaces );
        pTriangulationDT.addTriangleToList( s, t, bP );
      }
      if( pTriangulationDT.getEdges()[currentEdge].getR() == TriangulationDT.Undefined )
      {
        completeFacet( currentEdge, pTriangulationDT, lIntNumberOfFaces );
        pTriangulationDT.addTriangleToList( s, t, bP );
      }
      currentEdge++;
    }
  }

  // Find the two closest points.
  public void findClosestNeighbours( final RealPoint p[], final int nPoints, final List< Integer > pListInts )
  {
    int lIntIteratorIndexI;
    int lIntIteratorIndexJ;
    double d, min;
    int lIntResultS;
    int lIntResultT;

    lIntResultS = lIntResultT = 0;
    min = Double.MAX_VALUE;
    for( lIntIteratorIndexI = 0; lIntIteratorIndexI < nPoints - 1; lIntIteratorIndexI++ )
      for( lIntIteratorIndexJ = lIntIteratorIndexI + 1; lIntIteratorIndexJ < nPoints; lIntIteratorIndexJ++ )
      {
        try
        {
          d = p[lIntIteratorIndexI].distanceSq2d( p[lIntIteratorIndexJ] );
          if( d < min )
          {
            lIntResultS = lIntIteratorIndexI;
            lIntResultT = lIntIteratorIndexJ;
            min = d;
          }
        }
        catch( Exception e )
        {
          System.out.println( "findClosestNeighbours: on " + lIntIteratorIndexI + ", " + lIntIteratorIndexJ + ": " + e );
          // TODO: handle exception
        }
      }
    pListInts.add( 0, lIntResultS );
    pListInts.add( 1, lIntResultT );

  }

  /*
   * Complete a facet by looking for the circle free point to the left of the edge "e_i". Add the facet to the
   * triangulation. This function is a bit long and may be better split.
   */
  public void completeFacet( int pIntEdgeIndex, final TriangulationDT pTriangulationDT, int pIntNumberOfFaces )
  {
    double cP;
    Edge lEdges[] = pTriangulationDT.getEdges();
    RealPoint lPoints[] = pTriangulationDT.getPoints();

    // Cache s and t.
    if( lEdges[pIntEdgeIndex].getL() == TriangulationDT.Undefined )
    {
      s = lEdges[pIntEdgeIndex].getS();
      t = lEdges[pIntEdgeIndex].getT();
    }
    else if( lEdges[pIntEdgeIndex].getR() == TriangulationDT.Undefined )
    {
      s = lEdges[pIntEdgeIndex].getT();
      t = lEdges[pIntEdgeIndex].getS();
    }
    else
      // Edge already completed.
      return;

    // Find a point on left of edge.
    for( u = 0; u < pTriangulationDT.getAmountOfPoints(); u++ )
    {
      if( u == s || u == t )
        continue;
      if( Vector.crossProduct( lPoints[s], lPoints[t], lPoints[u] ) > 0.0 )
        break;
    }
    
    // Find best point on left of edge.
    bP = u;
    if( bP < pTriangulationDT.getAmountOfPoints() )
    {
      m_Circle.circumCircle( lPoints[s], lPoints[t], lPoints[bP] );

      for( u = bP + 1; u < pTriangulationDT.getAmountOfPoints(); u++ )
      {
        if( u == s || u == t )
          continue;

        cP = Vector.crossProduct( lPoints[s], lPoints[t], lPoints[u] );

        if( cP > 0.0 )
          if( m_Circle.isInside( lPoints[u] ) )
          {
            bP = u;
            m_Circle.circumCircle( lPoints[s], lPoints[t], lPoints[u] );
          }
      }
    }

    // Add new triangle or update edge info if s-t is on hull.
    if( bP < pTriangulationDT.getAmountOfPoints() )
    {
      // Update face information of edge being completed.
      pTriangulationDT.updateLeftFace( pIntEdgeIndex, s, t, pIntNumberOfFaces );
      pIntNumberOfFaces++;

      // Add new edge or update face info of old edge.
      pIntEdgeIndex = pTriangulationDT.findEdge( bP, s );
      if( pIntEdgeIndex == TriangulationDT.Undefined )
      {
        // New edge.
        pIntEdgeIndex = pTriangulationDT.addEdge( bP, s, pIntNumberOfFaces, TriangulationDT.Undefined );
      }
      else
      {
        // Old edge.
        pTriangulationDT.updateLeftFace( pIntEdgeIndex, bP, s, pIntNumberOfFaces );
      }

      // Add new edge or update face info of old edge.
      pIntEdgeIndex = pTriangulationDT.findEdge( t, bP );
      if( pIntEdgeIndex == TriangulationDT.Undefined )
      { 
        // New edge.
        pIntEdgeIndex = pTriangulationDT.addEdge( t, bP, pIntNumberOfFaces, TriangulationDT.Undefined );
      }
      else
      {
        // Old edge.
        pTriangulationDT.updateLeftFace( pIntEdgeIndex, t, bP, pIntNumberOfFaces );
      }
    }
    else
      pTriangulationDT.updateLeftFace( pIntEdgeIndex, s, t, TriangulationDT.Universe );
    
  }

}
