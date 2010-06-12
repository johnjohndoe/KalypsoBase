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
package org.kalypsodeegree_impl.filterencoding;

import java.io.ByteArrayInputStream;

import org.kalypso.contribs.java.xml.XMLHelper;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.filterencoding.FilterConstructionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Abstract superclass representing <Filter>elements (as defined in the Filter DTD). A <Filter>element either consists
 * of (one or more) FeatureId-elements or one operation-element. This is reflected in the two implementations
 * FeatureFilter and ComplexFilter.
 * 
 * @author Markus Schneider
 * @version 06.08.2002
 */
public abstract class AbstractFilter implements Filter
{
  /**
   * Builds a filter from the contents of a 'xs:anyType' element. Tries to interpret the given object either as filter
   * element, or, if not, it's direct nodes as filter. The first filter encountered wins.
   */
  public static Filter buildFromAnyType( final Object anyType ) throws FilterConstructionException
  {
    if( !(anyType instanceof Element) )
      return null;

    // Is the element itself the filter?
    final Element filterElement = (Element) anyType;
    if( filterElement.getLocalName().equals( "Filter" ) )
      return buildFromDOM( filterElement );

    // Try the direct children
    final NodeList childNodes = filterElement.getChildNodes();
    for( int i = 0; i < childNodes.getLength(); i++ )
    {
      final Node item = childNodes.item( i );
      if( item instanceof Element && item.getLocalName().equals( "Filter" ) )
        return AbstractFilter.buildFromDOM( (Element) item );
    }

    return null;
  }

  
  
  /**
   * Given a DOM-fragment, a corresponding Filter-object is built. This method recursively calls other buildFromDOM () -
   * methods to validate the structure of the DOM-fragment.
   * 
   * @throws FilterConstructionException
   *           if the structure of the DOM-fragment is invalid
   */
  public static Filter buildFromDOM( final Element element ) throws FilterConstructionException
  {
    Filter filter = null;

    // check if root element's name equals 'filter'
    if( !element.getLocalName().equals( "Filter" ) )
      throw new FilterConstructionException( "Name of element does not equal 'Filter'!" );

    // determine type of Filter (FeatureFilter / ComplexFilter)
    Element firstElement = null;
    NodeList children = element.getChildNodes();
    for( int i = 0; i < children.getLength(); i++ )
    {
      if( children.item( i ).getNodeType() == Node.ELEMENT_NODE )
      {
        firstElement = (Element) children.item( i );
      }
    }
    if( firstElement == null )
      throw new FilterConstructionException( "Filter Node is empty!" );

    if( firstElement.getLocalName().equals( "FeatureId" ) )
    {
      // must be a FeatureFilter
      final FeatureFilter fFilter = new FeatureFilter();
      children = element.getChildNodes();
      for( int i = 0; i < children.getLength(); i++ )
      {
        if( children.item( i ).getNodeType() == Node.ELEMENT_NODE )
        {
          final Element fid = (Element) children.item( i );
          if( !fid.getLocalName().equals( "FeatureId" ) )
            throw new FilterConstructionException( "Unexpected Element encountered: " + fid.getLocalName() );
          fFilter.addFeatureId( FeatureId.buildFromDOM( fid ) );
        }
      }
      filter = fFilter;
    }
    else
    {
      // must be a ComplexFilter
      children = element.getChildNodes();
      boolean justOne = false;
      for( int i = 0; i < children.getLength(); i++ )
      {
        if( children.item( i ).getNodeType() == Node.ELEMENT_NODE )
        {
          final Element operator = (Element) children.item( i );
          if( justOne )
            throw new FilterConstructionException( "Unexpected element encountered: " + operator.getLocalName() );
          final ComplexFilter cFilter = new ComplexFilter( AbstractOperation.buildFromDOM( operator ) );
          filter = cFilter;
          justOne = true;
        }
      }
    }
    return filter;
  }

  /** Produces an indented XML representation of this object. */
  @Override
  public abstract StringBuffer toXML( );

  public Filter clone( final Filter filter ) throws FilterConstructionException
  {
    final StringBuffer buffer = filter.toXML();
    final ByteArrayInputStream input = new ByteArrayInputStream( buffer.toString().getBytes() );
    Document asDOM = null;
    try
    {
      asDOM = XMLHelper.getAsDOM( input, true );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
    final Element element = asDOM.getDocumentElement();

    return AbstractFilter.buildFromDOM( element );
  }

  @Override
  public abstract Filter clone( ) throws CloneNotSupportedException;
}