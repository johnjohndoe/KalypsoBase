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

import org.kalypsodeegree.filterencoding.FilterConstructionException;
import org.w3c.dom.Element;

/**
 * Encapsulates the information of a <FeatureId>element as defined in the FeatureId DTD. The <FeatureId>element is used
 * to encode the unique identifier for any feature instance. Within a filter expression, the <FeatureId>is used as a
 * reference to a particular feature instance.
 * 
 * @author Markus Schneider
 * @version 06.08.2002
 */
public class FeatureId
{

  /** The FeatureId's value. */
  private String m_value;

  /** Constructs a new FeatureId. */
  public FeatureId( final String value )
  {
    m_value = value;
  }

  /**
   * Given a DOM-fragment, a corresponding Expression-object is built. This method recursively calls other buildFromDOM
   * () - methods to validate the structure of the DOM-fragment.
   * 
   * @throws FilterConstructionException
   *           if the structure of the DOM-fragment is invalid
   */
  public static FeatureId buildFromDOM( final Element element ) throws FilterConstructionException
  {

    // check if root element's name equals 'FeatureId'
    if( !element.getLocalName().toLowerCase().equals( "featureid" ) )
      throw new FilterConstructionException( "Name of element does not equal 'FeatureId'!" );

    // determine the value of the FeatureId
    final String fid = element.getAttribute( "fid" );
    if( fid == null )
      throw new FilterConstructionException( "<FeatureId> requires 'fid'-attribute!" );

    return new FeatureId( fid );
  }

  /**
   * Returns the feature id. A feature id is built from it's feature type name and it's id separated by a ".". e.g.
   * Road.A565
   */
  public String getValue( )
  {
    return m_value;
  }

  /**
   * @see org.kalypsodeegree_impl.filterencoding.FeatureId#getValue()
   */
  public void setValue( final String value )
  {
    m_value = value;
  }

  /** Produces an indented XML representation of this object. */
  public StringBuffer toXML( )
  {
    final StringBuffer sb = new StringBuffer();
    sb.append( "<ogc:FeatureId fid=\"" ).append( m_value ).append( "\"/>" );
    return sb;
  }
}