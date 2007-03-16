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
 *  ---------------------------------------------------------------------------*/
package org.kalypso.gmlschema.types;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.kalypso.commons.java.xml.DomHelper;
import org.kalypso.commons.metadata.MetadataObject;
import org.kalypso.commons.xml.NS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * For serializing the gml type MetaDataPropertyType.
 * <p>
 * <em>
 * &lt;...<br>
 * Attribute group reference (not shown): xlink:simpleLink
 * <br>
 * gml:remoteSchema=&quot;[0..1]&quot;<br>
 * about=&quot;anyURI [0..1]&quot;&gt;<br>
 * Start Sequence [0..1]<br>
 * Allow any elements from any namespace (lax validation). [1]<br>
 * End Sequence
 * &lt;/...&gt;
 * </em>
 * <p>
 * The remoteSchema attribute is used to find the GML-Schema that describes the contents of the sequence.
 * 
 * @author schlienger
 */
public class MetaDataPropertyTypeHandler extends SimpleDOMTypeHandler
{
  private final static QName QNAME = new QName( NS.GML3, "MetaDataPropertyType" );

  public MetaDataPropertyTypeHandler( )
  {
    super( "metaDataProperty", QNAME, false );
  }

  /**
   * @see org.kalypso.gmlschema.types.SimpleDOMTypeHandler#internalUnmarshall(org.w3c.dom.Node, java.lang.Object,
   *      org.kalypso.contribs.java.net.IUrlResolver)
   */
  @Override
  protected Object internalUnmarshall( final Node node ) throws TypeRegistryException
  {
    if( node != null && node.hasChildNodes() )
    {
      final Element elt = (Element) node.getChildNodes().item( 0 );

      final Element eltName = (Element) elt.getElementsByTagName( "name" ).item( 0 );
      final String name = eltName.getFirstChild().getTextContent();

      final Element eltValue = (Element) elt.getElementsByTagName( "value" ).item( 0 );

      // TODO currently value is a string. Better would be to make a type handler
      // concept for metadataobject, according to the type attribute of the
      // value element, one would use a specific type handler to serialize
      // the value.
      String value = null;
      if( eltValue.getFirstChild() != null )
        value = DomHelper.collectChildrenAsString( eltValue );
      final String type = eltValue.getAttribute( "type" );
      final String[] strings = type.split( ":" );
      final QName qnType;
      if( strings.length == 1 )
      {
        final String namespace = eltValue.getNamespaceURI();
        qnType = new QName( namespace, strings[0] );
      }
      else if( strings.length == 2 )
      {
        final String namespace = eltValue.lookupNamespaceURI( strings[0] );
        qnType = new QName( namespace, strings[1] );
      }
      else
        // should never happen, because this is not valid according to the kom-Schema
        qnType = MetadataObject.DEFAULT_TYPE;

//      final List<MetadataObject> list = new ArrayList<MetadataObject>();
//      list.add( new MetadataObject( name, value, qnType ) );
      return  new MetadataObject( name, value, qnType );

//      return list;
    }
    else
      throw new TypeRegistryException( "Empty or non-existent representation: cannot create RepresentationType instance" );
  }

  /**
   * @see org.kalypso.gmlschema.types.SimpleDOMTypeHandler#internalMarshall(java.lang.Object, org.w3c.dom.Element,
   *      java.net.URL)
   */
  @Override
  protected void internalMarshall( final Object value, final Element element, final URL context )
  {
    // final List<MetadataObject> list = (List<MetadataObject>) value;
    //
    // if( list.size() == 0 )
    // return;
    //
    // if( list.size() > 1 )
    // throw new TypeRegistryException( "List is too big. Metadata property can only contain one list item." );

    // final MetadataObject md = list.get( 0 );
    final MetadataObject md = (MetadataObject) value;

    final Document doc = element.getOwnerDocument();

    final Element eltMd = doc.createElementNS( NS.KALYPSO_OM, "metaData" );
    final Element eltName = doc.createElementNS( NS.KALYPSO_OM, "name" );

    final Text txtName = doc.createTextNode( md.getName() );
    eltName.appendChild( txtName );
    eltMd.appendChild( eltName );

    final Element eltValue = doc.createElementNS( NS.KALYPSO_OM, "value" );
    eltMd.appendChild( eltValue );

    final String prefix = element.lookupPrefix( md.getType().getNamespaceURI() );
    // TODO: this is probably null here, because the prefix 'xs' is not yet known
    // How can we tell the element that xs maps to the schema namespace?
    eltValue.setAttribute( "type", prefix + ":" + md.getType().getLocalPart() );
    if( md.getValue() != null )
    {
      // TODO: see also above concerning the remark on the type
      // of the value. We cannot always say that it's a string
      final Text txtValue = doc.createTextNode( (String) md.getValue() );
      eltValue.appendChild( txtValue );
    }

    element.appendChild( eltMd );
  }

  /**
   * @see org.kalypso.gmlschema.types.SimpleDOMTypeHandler#getValueClass()
   */
  @Override
  public Class getValueClass( )
  {
    return List.class;
  }
}
