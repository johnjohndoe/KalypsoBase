package com.bce.gis.index;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.model.feature.Feature;
import org.deegree.model.geometry.GM_Envelope;
import org.deegree_impl.model.geometry.GeometryFactory;

public class SplitSortContainer
{
  private static final int MAX_OBJECTS = 100;

  private SplitSortContainer[] mySubContainer = { null, null, null, null };

  private final GM_Envelope myEnvelope;

  private final List<Feature> m_features = new ArrayList<Feature>();

  private static final int LEFT_BOTTOM = 0;

  private static final int RIGHT_BOTTOM = 1;

  private static final int RIGHT_TOP = 2;

  private static final int LEFT_TOP = 3;

  public GM_Envelope getEnvelope( )
  {
    return myEnvelope;
  }

  public void setParent( final SplitSortContainer container )
  {
    myParent = container;
  }

  public int size( )
  {
    return m_features.size();
  }

  public int rsize( )
  {
    int size = size();
    for( int i = 0; i < 4; i++ )
      if( mySubContainer[i] != null )
        size += mySubContainer[i].rsize();
    return size;
  }

  private SplitSortContainer myParent = null;

  private GeometryFactory m_geometryFactory;

  public SplitSortContainer( final SplitSortContainer parent, final GM_Envelope env )
  {
    myEnvelope = env;
    myParent = parent;
  }

  private void add( final Feature object )
  {
    m_features.add( object );
  }

  private void resort( )
  {
    final List<Feature> oldObjects = new ArrayList<Feature>();
    oldObjects.addAll( m_features );
    m_features.clear();
    for( int i = 0; i < oldObjects.size(); i++ )
    {
      final GM_Envelope env = SplitSort.getEnvelope( m_geometryFactory, oldObjects.get( i ) );
      if( env != null )
        add( env, oldObjects.get( i ) );
      else
        add( oldObjects.get( i ) );
    }
  }

  public void add( final GM_Envelope env, final Feature object )
  {
    if( !myEnvelope.contains( env ) )
    {
      // Debug.println("ERROR: Container.add() does not contain envelope :-(");
    }
    else if( !hasSubContainers() && m_features.size() < MAX_OBJECTS )
    {
      add( object );
    }
    /*
     * else if(!hasSubContainers() && myObjects.size() <MAX_OBJECTS) { createSubContainers(); add(env,object); }
     */
    else if( hasSubContainers() )
    {
      if( mySubContainer[0].getEnvelope().contains( env ) )
        mySubContainer[0].add( env, object );
      else if( mySubContainer[1].getEnvelope().contains( env ) )
        mySubContainer[1].add( env, object );
      else if( mySubContainer[2].getEnvelope().contains( env ) )
        mySubContainer[2].add( env, object );
      else if( mySubContainer[3].getEnvelope().contains( env ) )
        mySubContainer[3].add( env, object );
      else
      {
        add( object );
      }
    }
    else
    // has no subContainer && myObjects.size() >=MAX_OBEJCTS
    {
      createSubContainers();
      // if(size()>RESORT_SIZE)
      resort();
      add( env, object );
    }
  }

  /*
   * public boolean contains(GM_Envelope env) { return myEnvelope.contains(env); }
   */
  public boolean intersects( final GM_Envelope env )
  {
    return myEnvelope.intersects( env );
  }

  public void createSubContainers( final SplitSortContainer container )
  {

    double midX = 0d;
    double midY = 0d;
    boolean midXset = false;
    boolean midYset = false;
    final GM_Envelope subEnv = container.getEnvelope();

    final double maxX = myEnvelope.getMax().getX();
    final double maxY = myEnvelope.getMax().getY();
    final double minX = myEnvelope.getMin().getX();
    final double minY = myEnvelope.getMin().getY();

    final double maxXsub = subEnv.getMax().getX();
    final double maxYsub = subEnv.getMax().getY();
    final double minXsub = subEnv.getMin().getX();
    final double minYsub = subEnv.getMin().getY();

    if( maxX == maxXsub )
    {
      midX = minXsub;
      midXset = true;
    }
    if( minX == minXsub )
    {
      midX = maxXsub;
      midXset = true;
    }
    if( maxY == maxYsub )
    {
      midY = minYsub;
      midYset = true;
    }
    if( minY == minYsub )
    {
      midY = maxYsub;
      midYset = true;
    }
    if( midXset && midYset )
    {
      createSubContainers( midX, midY );
      for( int i = 0; i < 4; i++ )
      {
        if( mySubContainer[i].getEnvelope().equals( container.getEnvelope() ) )
        {
          mySubContainer[i] = container;
        }
      }
    }
    else
    {
      midX = (minX + maxX) / 2d;
      midY = (minY + maxY) / 2d;
      switch( bestPoint( midX, midY, minXsub, minYsub, maxXsub, maxYsub ) )
      {
        case LEFT_BOTTOM:
          createSubContainers( minXsub, minYsub );
          break;
        case RIGHT_BOTTOM:
          createSubContainers( maxXsub, minYsub );
          break;
        case RIGHT_TOP:
          createSubContainers( maxXsub, maxYsub );
          break;
        case LEFT_TOP:
          createSubContainers( minXsub, maxYsub );
          break;
      }
      for( int i = 0; i < 4; i++ )
      {
        if( mySubContainer[i].getEnvelope().contains( container.getEnvelope() ) )
        {
          mySubContainer[i].createSubContainers( container );
        }
      }
    }
  }

  private int bestPoint( final double midX, final double midY, final double minX, final double minY, final double maxX, final double maxY )
  {
    final double dist[] = new double[4];
    dist[LEFT_BOTTOM] = Math.pow( minX - midX, 2d ) + Math.pow( minY - midY, 2d );
    dist[RIGHT_BOTTOM] = Math.pow( maxX - midX, 2d ) + Math.pow( minY - midY, 2d );
    dist[RIGHT_TOP] = Math.pow( maxX - midX, 2d ) + Math.pow( maxY - midY, 2d );
    dist[LEFT_TOP] = Math.pow( minX - midX, 2d ) + Math.pow( maxY - midY, 2d );
    int result = 0;
    for( int i = 0; i < 4; i++ )
    {
      if( dist[i] < dist[result] )
        result = i;
    }
    return result;
  }

  private void createSubContainers( )
  {
    final double maxX = myEnvelope.getMax().getX();
    final double maxY = myEnvelope.getMax().getY();

    final double minX = myEnvelope.getMin().getX();
    final double minY = myEnvelope.getMin().getY();

    final double midX = (minX + maxX) / 2d;
    final double midY = (minY + maxY) / 2d;
    createSubContainers( midX, midY );
  }

  private void createSubContainers( final double midX, final double midY )
  {
    final double maxX = myEnvelope.getMax().getX();
    final double maxY = myEnvelope.getMax().getY();

    final double minX = myEnvelope.getMin().getX();
    final double minY = myEnvelope.getMin().getY();

    m_geometryFactory = new GeometryFactory();
    final GM_Envelope env[] = { m_geometryFactory.createGM_Envelope( minX, minY, midX, midY ), m_geometryFactory.createGM_Envelope( midX, minY, maxX, midY ),
        m_geometryFactory.createGM_Envelope( midX, midY, maxX, maxY ), m_geometryFactory.createGM_Envelope( minX, midY, midX, maxY ) };
    for( int i = 0; i < 4; i++ )
      mySubContainer[i] = new SplitSortContainer( this, env[i] );
  }

  private boolean hasSubContainers( )
  {
    for( int i = 0; i < 4; i++ )
      if( mySubContainer[i] == null )
        return false;
    return true;
  }

  public List<Feature> query( final GM_Envelope env, List<Feature> result )
  {
    if( result == null )
      result = new ArrayList<Feature>();
    if( env == null )
      return result;
    for( int i = 0; i < m_features.size(); i++ )
    {
      final Feature object = m_features.get( i );
      final GM_Envelope envObject = SplitSort.getEnvelope( m_geometryFactory, object );
      if( envObject == null || env.intersects( envObject ) )
        result.add( object );
    }
    // boolean queried=false;
    if( hasSubContainers() )
    {
      for( int i = 0; i < 4; i++ )
      {
        if( mySubContainer[i].getEnvelope().contains( env ) )
        {
          result = mySubContainer[i].query( env, result );
          return result;
        }
      }
      for( int i = 0; i < 4; i++ )
      {
        if( env.intersects( mySubContainer[i].getEnvelope() ) )
          mySubContainer[i].query( env, result );
      }
    }
    return result;
  }

  public void queryAll( final List<Feature> result )
  {
    if( hasSubContainers() )
    {
      for( int i = 0; i < 4; i++ )
        mySubContainer[i].queryAll( result );
    }
    result.addAll( m_features );
  }

  public void remove( final GM_Envelope env, final Object object )
  {
    boolean removed = false;
    for( int i = 0; i < 4; i++ )
    {
      if( mySubContainer[i] != null && mySubContainer[i].getEnvelope().contains( env ) )
      {
        mySubContainer[i].remove( env, object );
        removed = true;
      }
    }
    for( int i = 0; i < 4; i++ )
    {
      if( mySubContainer[i] != null && mySubContainer[i].getEnvelope().intersects( env ) )
        mySubContainer[i].remove( env, object );
    }
    /*
     * env.contains(mySubContainer[i].getEnvelope()) //performance || mySubContainer[i].getEnvelope().contains(env)
     * //performance || env.intersects(mySubContainer[i].getEnvelope())
     */
    if( !removed )
    {
      m_features.remove( object );
      if( myParent != null )
        myParent.optimize();
    }
  }

  public void remove( final Object object )
  {
    if( m_features.contains( object ) )
    {
      m_features.remove( object );
      if( myParent != null )
        myParent.optimize();
    }
    else if( hasSubContainers() )
      for( int i = 0; i < 4; i++ )
        mySubContainer[i].remove( object );

  }

  private void optimize( )
  {
    if( hasSubContainers() )
    {
      for( int i = 0; i < 4; i++ )
      {
        if( mySubContainer[i].hasSubContainers() )
          return;
        if( mySubContainer[i].size() > 0 )
          return;
      }
      mySubContainer = new SplitSortContainer[] { null, null, null, null };
    }
    if( hasSubContainers() )
      return;
    if( size() > 0 )
      return;
    if( myParent != null )
      myParent.optimize();
  }

  public void paint( final Graphics g, final GeoTransform geoTransform )
  {
    final double g1x = geoTransform.getDestX( myEnvelope.getMin().getX() );
    final double g1y = geoTransform.getDestY( myEnvelope.getMin().getY() );
    final double g2x = geoTransform.getDestX( myEnvelope.getMax().getX() );
    final double g2y = geoTransform.getDestY( myEnvelope.getMax().getY() );

    g.drawRect( (int) (g1x < g2x ? g1x : g2x), (int) (g1y < g2y ? g1y : g2y), (int) Math.abs( (g2x - g1x) ), (int) Math.abs( (g2y - g1y) ) );

    if( hasSubContainers() )
      for( int i = 0; i < 4; i++ )
        mySubContainer[i].paint( g, geoTransform );
  }
}