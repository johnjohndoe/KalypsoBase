package org.kalypso.gml.processes.constDelaunay.DelaunayImpl;


/*
 * TriangulationAlgorithm class. Absract. Superclass for actual algorithms. Has several abstract function members -
 * including the triangulation member which actually computes the triangulation.
 */
abstract class TriangulationAlgorithm
{
  @SuppressWarnings("unused")
  private String algName;

  // Variables and constants for animation state.
  final int m_intNumberOfStates = 5;

  boolean state[] = new boolean[m_intNumberOfStates];

  static final int triangulationState = 0;

  static final int pointState = 1;

  static final int triangleState = 2;

  static final int insideState = 4;

  static final int edgeState = 5;

  public TriangulationAlgorithm( String name )
  {
    algName = name;

    for( int s = 0; s < m_intNumberOfStates; s++ )
      state[s] = false;
  }

  public void setAlgorithmState( final int stateVar, final boolean value )
  {
    state[stateVar] = value;
  }

  public void reset( )
  {
    for( int s = 0; s < m_intNumberOfStates; s++ )
      state[s] = false;
  }

  public synchronized void nextStep( )
  {
    notify();
  }

  abstract public void triangulate( final TriangulationDT t );

}