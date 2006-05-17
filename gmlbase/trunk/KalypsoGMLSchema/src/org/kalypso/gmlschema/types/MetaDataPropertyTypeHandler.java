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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.kalypso.commons.metadata.MetadataObject;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.w3c.dom.Node;

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
  
  public MetaDataPropertyTypeHandler()
  {
    super( "metaDataProperty", QNAME, false );
  }
  
  /**
   * @see org.kalypso.gmlschema.types.SimpleDOMTypeHandler#internalUnmarshall(org.w3c.dom.Node, java.lang.Object, org.kalypso.contribs.java.net.IUrlResolver)
   */
  @Override
  protected Object internalUnmarshall( Node node, IUrlResolver urlResolver ) throws TypeRegistryException
  {
    // TODO Node --> MetadataObject(s)
    
    final List<MetadataObject> list = new ArrayList<MetadataObject>();
    list.add( new MetadataObject() );
    
    return list;
  }
}
