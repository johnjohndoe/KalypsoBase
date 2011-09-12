package org.kalypso.contribs.java.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author doemming
 */
public class NodeList_Impl implements NodeList
{
  private final List<Node> nodes = new ArrayList<Node>();

  public NodeList_Impl()
  {
  // nothing
  }

  public NodeList_Impl( NodeList nl )
  {
    add( nl );
  }

  @Override
  public int getLength()
  {
    return nodes.size();
  }

  public void add( NodeList nl )
  {
    for( int i = 0; i < nl.getLength(); i++ )
      nodes.add( nl.item( i ) );
  }

  public void add( Node node )
  {
    nodes.add( node );
  }

  @Override
  public Node item( int pos )
  {
    return nodes.get( pos );
  }
}