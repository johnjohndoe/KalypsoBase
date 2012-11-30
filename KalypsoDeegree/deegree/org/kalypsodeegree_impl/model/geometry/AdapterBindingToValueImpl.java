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
package org.kalypsodeegree_impl.model.geometry;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.kalypso.contribs.org.xml.sax.DelegateXmlReader;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler2;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * Factory class to wrap from binding geometries to GM_Object geometries and visa versa
 *
 * @author doemming
 */
public class AdapterBindingToValueImpl implements AdapterBindingToValue
{
  @Override
  // Still used for RectifiedGridDomain and spatial-ops
  public Object wrapFromNode( final Node node ) throws Exception
  {
    final ITypeRegistry<IMarshallingTypeHandler> registry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    final QName nodeName = new QName( node.getNamespaceURI(), node.getLocalName() );
    final IMarshallingTypeHandler typeHandler = registry.getTypeHandlerForTypeName( nodeName );

    if( typeHandler instanceof IMarshallingTypeHandler2 )
    {
      final Object[] result = new Object[1];
      final UnmarshallResultEater eater = new UnmarshallResultEater()
      {
        @Override
        public void unmarshallSuccesful( final Object value )
        {
          result[0] = value;
        }
      };

      final DelegateXmlReader xmlReader = new DelegateXmlReader();
      final ContentHandler contentHandler = ((IMarshallingTypeHandler2) typeHandler).createContentHandler( xmlReader, null, eater );
      xmlReader.setContentHandler( contentHandler );

      final TransformerFactory tf = TransformerFactory.newInstance();
      final Transformer transformer = tf.newTransformer();

      transformer.transform( new DOMSource( node ), new SAXResult( xmlReader ) );
      return result[0];
    }

    // we should never come here, all Geometries and the envelope are using sax parsers now
    throw new UnsupportedOperationException();
  }
}
