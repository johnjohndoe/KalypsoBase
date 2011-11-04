/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 * 
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always. 
 * 
 * If you intend to use this software in other ways than in kalypso 
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree, 
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.io.rtree;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

/**
 * Implementierung eines R-Baumes nach den Algorithmen von Antonio Guttman.
 * 
 * @version 1.4
 * @author Wolfgang Bär
 */
public class RTree
{
  public PageFile file;

  /**
   * Erzeugt einen leeren R-Baum. Mit MemoryPageFile und leerem RootNode.
   * 
   * @param dimension
   *          number of dimensions of all data
   * @param capacity
   *          maximum load per node (page) plus 1 for overflow
   */
  public RTree( final int dimension, final int capacity ) throws RTreeException
  {
    file = new MemoryPageFile();

    try
    {
      file.initialize( dimension, capacity );

      final Node rootNode = new LeafNode( 0, file );
      file.writeNode( rootNode );
    }
    catch( final PageFileException e )
    {
      e.fillInStackTrace();
      throw new RTreeException( "PageFileException in constructor (writeNode(rootNode)" );
    }
  }

  /**
   * Erzeugt neuen R-Baum abgespeichert in Persistenter PageFile.
   * 
   * @param dimension
   *          number of dimensions of all data
   * @param capacity
   *          maximum load per node (page) plus 1 for overflow
   * @param fileName
   *          filename of the persistent pagefile
   */
  public RTree( final int dimension, final int capacity, final String fileName ) throws RTreeException
  {
    try
    {
      file = new PersistentPageFile( fileName );
      file.initialize( dimension, capacity );

      final Node rootNode = new LeafNode( 0, file );
      file.writeNode( rootNode );
    }
    catch( final PageFileException e )
    {
      e.fillInStackTrace();
      throw new RTreeException( "PageFileException in constructor" );
    }
  }

  /**
   * Erzeugt R-Baum basierend auf übergebener Persistenter PageFile.
   * 
   * @param fileName
   *          filename of an existing persistent pagefile
   */
  public RTree( final String fileName ) throws RTreeException
  {
    file = new PersistentPageFile( fileName );

    try
    {
      file.initialize( -999, -999 );
    }
    catch( final PageFileException e )
    {
      e.fillInStackTrace();
      throw new RTreeException( "PageFileException in constructor (file.initialize())" );
    }
  }

  /**
   * Sucht alle Einträge, deren HyperBoundingBoxes mit der übergebenen überlappen.
   * 
   * @param box
   *          für Überlappung
   * @return Object[] Array von gefundenen Objekten (Integer-Objekte)
   */
  public Object[] intersects( final HyperBoundingBox box ) throws RTreeException
  {
    if( box.getDimension() != file.getDimension() )
    {
      throw new IllegalArgumentException( "HyperBoundingBox hat falsche Dimension !" );
    }

    final Vector v = new Vector();

    // ruft die eigentliche suche auf
    try
    {
      intersectsSearch( file.readNode( 0 ), v, box );
    }
    catch( final PageFileException e )
    {
      e.fillInStackTrace();
      throw new RTreeException( "PageFileException RTree.search() - readNode()" );
    }

    return v.toArray();
  }

  /**
   * Sucht alle Einträge, deren HyperBoundingBoxes die die übergebene enthalten.
   * 
   * @param box
   *          die enthalten sein soll
   * @return Object[] Array von gefundenen Objekten (Integer-Objekte)
   */
  public Object[] contains( final HyperBoundingBox box ) throws RTreeException
  {
    if( box.getDimension() != file.getDimension() )
    {
      throw new IllegalArgumentException( "HyperBoundingBox hat falsche Dimension !" );
    }

    final Vector v = new Vector();

    // ruft die eigentliche suche auf
    try
    {
      containsSearch( file.readNode( 0 ), v, box );
    }
    catch( final PageFileException e )
    {
      e.fillInStackTrace();
      throw new RTreeException( "PageFileException RTree.search() - readNode() " );
    }

    return v.toArray();
  }

  /**
   * @param node1
   * @param v
   * @param box
   */
  private void containsSearch( final Node node1, final Vector v, final HyperBoundingBox box )
  {
    if( node1 instanceof LeafNode )
    {
      final LeafNode node = (LeafNode) node1;

      for( int i = 0; i < node.getUsedSpace(); i++ )
      {
        // wenn einträge enthalten diese in Vechtor aufnehmen;
        if( node.hyperBBs[i].contains( box ) )
        {
          v.addElement( node.getData( i ) );
        }
      }

      return;
    }
    final NoneLeafNode node = (NoneLeafNode) node1;

    // node ist kein LeafNode
    // alle eintraäge auf überlappung durchsuchen
    for( int i = 0; i < node.getUsedSpace(); i++ )
    {
      // wenn enthalten rekursiv search mit diesem node aufrufen
      if( node.hyperBBs[i].contains( box ) )
      {
        containsSearch( (Node) node.getData( i ), v, box );
      }
    }
  }

  /**
   * @param node1
   * @param v
   * @param box
   */
  private void intersectsSearch( final Node node1, final Vector v, final HyperBoundingBox box )
  {
    if( node1 instanceof LeafNode )
    {
      final LeafNode node = (LeafNode) node1;

      for( int i = 0; i < node.getUsedSpace(); i++ )
      {
        // wenn einträge überlappen diese in Vechtor aufnehmen;
        if( node.hyperBBs[i].overlaps( box ) )
        {
          v.addElement( node.getData( i ) );
        }
      }

      return;
    }
    final NoneLeafNode node = (NoneLeafNode) node1;

    // node ist kein LeafNode
    // alle eintraäge auf überlappung durchsuchen
    for( int i = 0; i < node.getUsedSpace(); i++ )
    {
      // wenn überlappung rekursiv search mit diesem node aufrufen
      if( node.hyperBBs[i].overlaps( box ) )
      {
        intersectsSearch( (Node) node.getData( i ), v, box );
      }
    }
  }

  /**
   * Fügt ein Object mit seiner HyperBoundingBox in den R-Baum ein.
   * 
   * @param obj
   *          das einzufügende Object (Integer-Object)
   * @param box
   *          dessen HyperBoundingBox
   * @return boolean true, wenn erfolgreich
   */
  public boolean insert( final Object obj, final HyperBoundingBox box ) throws RTreeException
  {
    try
    {
      Node[] newNodes = new Node[] { null, null };
      // Find position for new record
      LeafNode node;
      node = chooseLeaf( file.readNode( 0 ), box );

      // Add record to leaf node
      if( node.getUsedSpace() < file.getCapacity() - 1 )
      {
        node.insertData( obj, box );
        file.writeNode( node );
      }
      else
      {
        // invoke SplitNode
        node.insertData( obj, box );
        file.writeNode( node );
        newNodes = splitNode( node );
      }

      if( newNodes[0] != null )
      {
        adjustTree( newNodes[0], newNodes[1] );
      }
      else
      {
        adjustTree( node, null );
      }
    }
    catch( final PageFileException e )
    {
      e.fillInStackTrace();
      throw new RTreeException( "PageFileException occured" );
    }

    return true;
  }

  private Node[] splitNode( final Node node ) throws PageFileException
  {
    // Neuer Knoten
    Node newNode = null;
    // Zwischenspeicher für Knoten node
    Node helpNode = null;

    // bestimmen der anfangseinträge
    final int[] seeds = pickSeeds( node );

    // Beginn neu Implementierung
    if( node instanceof LeafNode )
    {
      newNode = new LeafNode( file );
      helpNode = new LeafNode( node.getPageNumber(), file );
    }
    else
    {
      newNode = new NoneLeafNode( -1, file );
      helpNode = new NoneLeafNode( node.getPageNumber(), file );
    }

    // newNode wird PageNumber zugewiesen
    file.writeNode( newNode );

    node.counter = 0;
    node.unionMinBB = HyperBoundingBox.getNullHyperBoundingBox( file.getDimension() );

    // Einfügen der Anfangseinträge
    helpNode.insertData( node.getData( seeds[0] ), node.getHyperBoundingBox( seeds[0] ) );
    newNode.insertData( node.getData( seeds[1] ), node.getHyperBoundingBox( seeds[1] ) );

    // Markieren der eingefügten Einträge
    final boolean[] marker = new boolean[file.getCapacity()];

    for( int i = 0; i < file.getCapacity(); i++ )
      marker[i] = false;

    marker[seeds[0]] = true;
    marker[seeds[1]] = true;

    // noch 2 zu erledigen
    int doneCounter = file.getCapacity() - 2;

    // machen bis alle zugewisen oder wenn eine gruppe so wenige
    // hat, das der rest zugewiesen werden muß
    while( doneCounter > 0 )
    {
      int[] entry;
      entry = pickNext( node, marker, helpNode, newNode );
      doneCounter--;

      if( entry[0] == 1 )
      {
        helpNode.insertData( node.getData( entry[1] ), node.getHyperBoundingBox( entry[1] ) );
      }
      else
      {
        newNode.insertData( node.getData( entry[1] ), node.getHyperBoundingBox( entry[1] ) );
      }

      if( file.getMinimum() - helpNode.getUsedSpace() == doneCounter )
      {
        // System.out.println("Rest zu Gruppe 1 !");
        for( int i = 0; i < file.getCapacity(); i++ )
          if( marker[i] == false )
          {
            helpNode.insertData( node.getData( i ), node.getHyperBoundingBox( i ) );
          }

        break;
      }

      if( file.getMinimum() - newNode.getUsedSpace() == doneCounter )
      {
        // System.out.println("Rest zu Gruppe 2 !");
        for( int i = 0; i < file.getCapacity(); i++ )
          if( marker[i] == false )
          {
            newNode.insertData( node.getData( i ), node.getHyperBoundingBox( i ) );
          }

        break;
      }
    }

    // Übertragen der Zwischenspeicherung zum Knoten AbstractNode
    for( int x = 0; x < helpNode.getUsedSpace(); x++ )
      node.insertData( helpNode.getData( x ), helpNode.getHyperBoundingBox( x ) );

    file.writeNode( node );
    file.writeNode( newNode );

    return new Node[] { node, newNode };
  }

  private int[] pickSeeds( final Node node )
  {
    double max = 0.0;
    int e1 = 0;
    int e2 = 0;

    // durchlauf aller combinationen und bestimmen der kombination
    // mit dem größten flächenzuwachs
    for( int i = 0; i < file.getCapacity(); i++ )
      for( int j = 0; j < file.getCapacity(); j++ )
      {
        if( i != j )
        {
          final double d = node.getHyperBoundingBox( i ).unionBoundingBox( node.getHyperBoundingBox( j ) ).getArea() - node.getHyperBoundingBox( i ).getArea()
              - node.getHyperBoundingBox( j ).getArea();

          if( d > max )
          {
            max = d;
            e1 = i;
            e2 = j;
          }
        }
      }

    return new int[] { e1, e2 };
  }

  private int[] pickNext( final Node node, final boolean[] marker, final Node group1, final Node group2 )
  {
    double d0 = 0;
    double d1 = 0;
    double diff = -1;
    double max = -1;
    int entry = 99;
    int group = 99;

    for( int i = 0; i < file.getCapacity(); i++ )
    {
      if( marker[i] == false )
      {
        d0 = group1.getUnionMinBB().unionBoundingBox( node.getHyperBoundingBox( i ) ).getArea() - group1.getUnionMinBB().getArea();

        d1 = group2.getUnionMinBB().unionBoundingBox( node.getHyperBoundingBox( i ) ).getArea() - group2.getUnionMinBB().getArea();
        diff = Math.abs( d0 - d1 );

        if( diff > max )
        {
          if( d0 < d1 )
          {
            group = 1;
          }
          else
          {
            group = 2;
          }

          max = diff;
          entry = i;
        }

        if( diff == max )
        {
          if( d0 < d1 )
          {
            group = 1;
          }
          else
          {
            group = 2;
          }

          max = diff;
          entry = i;
        }
      }
    }

    marker[entry] = true;
    return new int[] { group, entry };
  }

  private LeafNode chooseLeaf( final Node node, final HyperBoundingBox box )
  {
    if( node instanceof LeafNode )
    {
      return (LeafNode) node;
    }

    final NoneLeafNode node1 = (NoneLeafNode) node;
    final int least = node1.getLeastEnlargement( box );
    return chooseLeaf( (Node) node1.getData( least ), box );
  }

  /**
   * Holt den nächsten Nachbarn zum angegebenen Suchpunkt.
   * 
   * @param point
   *          Suchpunkt
   * @return double[] Stelle 0 = Distanz, Stelle 1 Data-Pointer (als double)
   */
  public double[] nearestNeighbour( final HyperPoint point ) throws RTreeException
  {
    try
    {
      return nearestNeighbour( file.readNode( 0 ), point, new double[] { Double.POSITIVE_INFINITY, -1.0 } );
    }
    catch( final PageFileException e )
    {
      e.fillInStackTrace();
      throw new RTreeException( "PageFileException - nearestNeighbour - readNode(0)" );
    }
  }

  private double[] nearestNeighbour( final Node node, final HyperPoint point, double[] temp )
  {
    if( node instanceof LeafNode )
    {
      // wenn mindist this < tempDist
      for( int i = 0; i < node.getUsedSpace(); i++ )
      {
        final double dist = node.getHyperBoundingBox( i ).minDist( point );

        if( dist < temp[0] )
        {
          // dann this = nearest Neighbour - update tempDist
          temp[1] = ((LeafNode) node).data[i];
          temp[0] = dist;
        }
      }
      // ansonsten nichts
    }
    else
    {
      /**
       * @version $Revision$
       * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
       */
      class ABL implements Comparable
      {
        Node m_node;

        double minDist;

        /**
         * Creates a new ABL object.
         * 
         * @param n
         * @param minDist
         */
        public ABL( final Node n, final double minDist )
        {
          m_node = n;
          this.minDist = minDist;
        }

        @Override
        public int compareTo( final Object obj )
        {
          final ABL help = (ABL) obj;

          if( minDist < help.minDist )
            return -1;

          if( minDist > help.minDist )
            return 1;

          return 0;
        }
      }

      // generate ActiveBranchList of node
      final ABL[] abl = new ABL[node.getUsedSpace()];

      for( int i = 0; i < node.getUsedSpace(); i++ )
      {
        final Node help = (Node) node.getData( i );
        abl[i] = new ABL( help, help.getUnionMinBB().minDist( point ) );
      }

      // sort activebranchlist
      Arrays.sort( abl );

      for( final ABL element : abl )
      {
        // apply heuristic 3
        if( element.minDist <= temp[0] )
        {
          temp = nearestNeighbour( element.m_node, point, temp );
        }
      }
    }

    return temp;
  }

  /**
   * Closes the rtree and frees the ressources.
   * 
   * @throws RTreeException
   *           if an error occures.
   */
  public void close( ) throws RTreeException
  {
    try
    {
      file.close();
    }
    catch( final PageFileException e )
    {
      e.fillInStackTrace();
      throw new RTreeException( "PageFileException - close()" );
    }
  }

  /**
   * Löscht einen Eintrag aus dem R-Baum
   * 
   * @param box
   *          BoundingBox des Eintrages
   * @param objID
   *          Objekt-ID zur genauen Identifizierung.
   * @return boolean true, wenn erfolgreich
   */
  public boolean delete( final HyperBoundingBox box, final int objID ) throws RTreeException
  {
    final Vector v = new Vector();

    try
    {
      findLeaf( file.readNode( 0 ), box, objID, v );
    }
    catch( final PageFileException e )
    {
      e.fillInStackTrace();
      throw new RTreeException( "PageFileException - delete()" );
    }

    if( v.size() < 1 )
    {
      return false;
    }

    if( v.size() == 1 )
    {
      final LeafNode leaf = (LeafNode) v.elementAt( 0 );

      for( int i = 0; i < leaf.getUsedSpace(); i++ )
      {
        if( leaf.getHyperBoundingBox( i ).equals( box ) && leaf.data[i] == objID )
        {
          leaf.deleteData( i );

          try
          {
            file.writeNode( leaf );
          }
          catch( final PageFileException e )
          {
            e.fillInStackTrace();
            throw new RTreeException( "PageFileException - delete()" );
          }
        }
      }

      final Stack stack = new Stack();

      try
      {
        condenseTree( leaf, stack );
      }
      catch( final PageFileException e )
      {
        e.fillInStackTrace();
        throw new RTreeException( "PageFileException - condenseTree()" );
      }

      while( !stack.empty() )
      {
        final Node node = (Node) stack.pop();

        if( node instanceof LeafNode )
        {
          for( int i = 0; i < node.getUsedSpace(); i++ )
            insert( ((LeafNode) node).getData( i ), ((LeafNode) node).getHyperBoundingBox( i ) );
        }
        else
        {
          for( int i = 0; i < node.getUsedSpace(); i++ )
            stack.push( ((NoneLeafNode) node).getData( i ) );
        }

        try
        {
          file.deleteNode( node.pageNumber );
        }
        catch( final PageFileException e )
        {
          e.fillInStackTrace();
          throw new RTreeException( "PageFileException - delete() - deleteNode(0)" );
        }
      }
    }

    return true;
  }

  /**
   * Löscht alle Eintrag aus dem R-Baum
   * 
   * @param box
   *          BoundingBox der Eintragungen
   * @return boolean true, wenn erfolgreich
   */
  public boolean delete( final HyperBoundingBox box ) throws RTreeException
  {
    final Vector v = new Vector();

    try
    {
      findLeaf( file.readNode( 0 ), box, -99, v );
    }
    catch( final PageFileException e )
    {
      e.fillInStackTrace();
      throw new RTreeException( "PageFileException - delete()" );
    }

    if( v.size() < 1 )
      return false;

    LeafNode leaf;

    for( final Enumeration en = v.elements(); en.hasMoreElements(); )
    {
      leaf = (LeafNode) en.nextElement();

      for( int i = 0; i < leaf.getUsedSpace(); i++ )
      {
        if( leaf.getHyperBoundingBox( i ).equals( box ) )
        {
          leaf.deleteData( i );

          try
          {
            file.writeNode( leaf );
          }
          catch( final PageFileException e )
          {
            e.fillInStackTrace();
            throw new RTreeException( "PageFileException - delete()" );
          }
        }
      }

      final Stack stack = new Stack();

      try
      {
        condenseTree( leaf, stack );
      }
      catch( final PageFileException e )
      {
        e.fillInStackTrace();
        throw new RTreeException( "PageFileException - condenseTree()" );
      }

      while( !stack.empty() )
      {
        final Node node = (Node) stack.pop();

        if( node instanceof LeafNode )
        {
          for( int i = 0; i < node.getUsedSpace(); i++ )
            insert( ((LeafNode) node).getData( i ), ((LeafNode) node).getHyperBoundingBox( i ) );
        }
        else
        {
          for( int i = 0; i < node.getUsedSpace(); i++ )
            stack.push( ((NoneLeafNode) node).getData( i ) );
        }

        try
        {
          file.deleteNode( node.pageNumber );
        }
        catch( final PageFileException e )
        {
          e.fillInStackTrace();
          throw new RTreeException( "PageFileException - delete() - deleteNode(0)" );
        }
      }
    }

    return true;
  }

  /**
   * Findet alle Eintrag aus dem R-Baum
   * 
   * @param box
   *          BoundingBox der Eintragungen
   * @return Object[] Array von gefundenen Objekten (Integer-Objekte)
   */
  public Object[] find( final HyperBoundingBox box ) throws RTreeException
  {
    if( box.getDimension() != file.getDimension() )
    {
      throw new IllegalArgumentException( "HyperBoundingBox hat falsche Dimension !" );
    }

    final Vector v = new Vector();

    // ruft die eigentliche suche auf
    try
    {
      findSearch( file.readNode( 0 ), v, box );
    }
    catch( final PageFileException e )
    {
      e.fillInStackTrace();
      throw new RTreeException( "PageFileException RTree.search() - readNode()" );
    }

    return v.toArray();
  }

  /**
   * @param node1
   * @param v
   * @param box
   */
  private void findSearch( final Node node1, final Vector v, final HyperBoundingBox box )
  {
    if( node1 instanceof LeafNode )
    {
      final LeafNode node = (LeafNode) node1;

      for( int i = 0; i < node.getUsedSpace(); i++ )
      {
        // wenn einträge enthalten diese in Vechtor aufnehmen;
        if( node.hyperBBs[i].equals( box ) )
        {
          v.addElement( node.getData( i ) );
        }
      }

      return;
    }

    final NoneLeafNode node = (NoneLeafNode) node1;

    // node ist kein LeafNode
    // alle eintraäge auf überlappung durchsuchen
    for( int i = 0; i < node.getUsedSpace(); i++ )
    {
      // wenn enthalten rekursiv search mit diesem node aufrufen
      if( node.hyperBBs[i].contains( box ) )
      {
        findSearch( (Node) node.getData( i ), v, box );
      }
    }
  }

  /**
   * @param node
   * @param box
   * @param objID
   * @param v
   */
  private void findLeaf( final Node node, final HyperBoundingBox box, final int objID, final Vector v )
  {
    if( node instanceof LeafNode )
    {
      for( int i = 0; i < node.getUsedSpace(); i++ )
      {
        if( objID == -99 )
        {
          if( node.getHyperBoundingBox( i ).equals( box ) )
          {
            v.addElement( node );
          }
          else if( node.getHyperBoundingBox( i ).equals( box ) && ((LeafNode) node).data[i] == objID )
          {
            v.addElement( node );
          }
        }
      }
    }
    else
    {
      for( int i = 0; i < node.getUsedSpace(); i++ )
      {
        if( node.getHyperBoundingBox( i ).overlaps( box ) )
        {
          findLeaf( (Node) node.getData( i ), box, objID, v );
        }
      }
    }
  }

  /**
   * @param n
   * @param stack
   * @throws PageFileException
   */
  private void condenseTree( final Node n, final Stack stack ) throws PageFileException
  {
    if( !n.isRoot() )
    {
      final Node p = n.getParent();

      if( n.getUsedSpace() < file.getMinimum() )
      {
        p.deleteData( n.place );
        stack.push( n );
      }
      else
      {
        p.hyperBBs[n.place] = n.getUnionMinBB();
        p.updateNodeBoundingBox();
      }

      file.writeNode( p );

      condenseTree( p, stack );
    }
    else
    {
      if( n.getUsedSpace() == 1 && n instanceof NoneLeafNode )
      {
        final Node kind = (Node) n.getData( 0 );
        Node newRoot = null;

        if( kind instanceof LeafNode )
        {
          newRoot = new LeafNode( 0, file );

          for( int i = 0; i < kind.getUsedSpace(); i++ )
            newRoot.insertData( kind.getData( i ), kind.getHyperBoundingBox( i ) );
        }
        else
        {
          newRoot = new NoneLeafNode( 0, file );

          for( int i = 0; i < kind.getUsedSpace(); i++ )
            newRoot.insertData( kind.getData( i ), kind.getHyperBoundingBox( i ) );
        }

        file.writeNode( newRoot );
      }
    }
  }

  /**
   * @param n1
   * @param n2
   * @throws PageFileException
   */
  private void adjustTree( final Node n1, final Node n2 ) throws PageFileException
  {
    if( n1.isRoot() )
    {
      // Neuer Root AbstractNode bei Root-Split
      if( n2 != null && n1.isRoot() )
      {
        // Knoten muß von 0 PageNumber auf neue umgeschrieben werden
        n1.setPageNumber( -1 );

        int pagenumber;

        pagenumber = file.writeNode( n1 );

        for( int x = 0; x < n1.getUsedSpace(); x++ )
        {
          Object obj = n1.getData( x );

          if( obj instanceof Node )
          {
            final Node node = (Node) obj;
            node.parentNode = pagenumber;
            file.writeNode( node );
          }

          obj = null;
        }

        final NoneLeafNode newRoot = new NoneLeafNode( 0, file );

        newRoot.insertData( n1, n1.getUnionMinBB() );
        newRoot.insertData( n2, n2.getUnionMinBB() );
        newRoot.parentNode = 0;

        file.writeNode( newRoot );
      }

      return;
    }

    // Aktualisierung der BoundingBox beim Parent von AbstractNode n1
    final NoneLeafNode p = (NoneLeafNode) n1.getParent();
    p.hyperBBs[n1.place] = n1.getUnionMinBB();
    p.unionMinBB = p.getUnionMinBB().unionBoundingBox( n1.getUnionMinBB() );

    file.writeNode( p );

    // Weitergabe zum Aktualisieren nach oben
    if( n2 == null )
    {
      adjustTree( p, null );
    }
    else
    {
      // Bei Split muß der zweite AbstractNode noch eingefügt werden
      Node[] newNodes = new Node[] { null, null };

      if( p.getUsedSpace() < file.getCapacity() - 1 )
      {
        // Split muß erfolgen
        p.insertData( n2, n2.getUnionMinBB() );
        file.writeNode( p );
        newNodes[0] = p;
      }
      else
      {
        p.insertData( n2, n2.getUnionMinBB() );
        file.writeNode( p );
        newNodes = splitNode( p );
      }

      adjustTree( newNodes[0], newNodes[1] );
    }
  }
}