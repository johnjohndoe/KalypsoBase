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
package org.kalypso.contribs.java.xml;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author doemming, schlienger
 */
public final class XMLUtilities
{
  /** denotes the beginning of an XML-Header (Value is: &lt;?xml) */
  public final static String XML_HEADER_BEGIN = "<?xml";

  /** denotes the beginning of a CDATA section (Value is: &lt;![CDATA[) */
  public final static String CDATA_BEGIN = "<![CDATA[";

  public final static String CDATA_BEGIN_REGEX = "<\\!\\[CDATA\\[";

  /** denotes the end of a CDATA section (Value is: ]]&gt;) */
  public final static String CDATA_END = "]]>";

  public final static String CDATA_END_REGEX = "\\]\\]>";

  /**
   * Encapsulates the given string into a CDATA section.
   * <p>
   *
   * @param string
   * @return &lt;![CDATA[...string...]]&gt;
   */
  public static String encapsulateInCDATA( final String string )
  {
    return CDATA_BEGIN + string + CDATA_END;
  }

  public static String removeXMLHeader( final String xmlString )
  {
    return xmlString.replaceFirst( "<\\?.+?\\?>", "" );
  }

  /**
   * TODO Andreas, diese Methode wird scheinbar nur von WQFilterUtilities.createWQFilterInline() und
   * UpdateHelper.createInterpolationFilter() aufgerufen, und beinhaltet ausserdem 'filter'-spezifische Tags. Sollte es
   * nicht besser in WQFilterUtilities verschoben werden?
   *
   * @param xmlString
   */
  public static String prepareInLine( final String xmlString )
  {
    String result = xmlString.replaceAll( "\n", "" );
    result = removeXMLHeader( result );
    return "<filter>" + result + "</filter>";
  }

  public static void setTextNode( final Node node, final String value )
  {
    final NodeList cn = node.getChildNodes();
    for( int _n = 0; _n < cn.getLength(); _n++ )
    {
      final Node cnode = cn.item( _n );
      final short nodeType = cnode.getNodeType();
      if( nodeType == Node.TEXT_NODE )
        cnode.setNodeValue( value );
    }
    if( cn.getLength() == 0 ) // text node does not exist
    {
      final Text text = node.getOwnerDocument().createTextNode( value );
      node.appendChild( text );
    }
  }

  /**
   * Returns a NamespaceContext based on the given node.
   * <p>
   * The returned implementation does not support {@link NamespaceContext#getPrefixes(String)}.
   */
  public static NamespaceContext createNamespaceContext( final Node node )
  {
    // sure there is no existing implementation?
    return new NamespaceContext()
    {
      @Override
      public String getNamespaceURI( final String prefix )
      {
        return node.lookupNamespaceURI( prefix );
      }

      @Override
      public String getPrefix( final String namespaceURI )
      {
        return node.lookupPrefix( namespaceURI );
      }

      @Override
      public Iterator<String> getPrefixes( final String namespaceURI )
      {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static String getNameSpaceForPrefix( final Element context, final String prefix )
  {
    // TODO: why not just the next line?
// return context.lookupNamespaceURI( prefix );

    final String prefixDeclarationNamespace = "http://www.w3.org/2000/xmlns/";
    if( prefix.equals( context.getPrefix() ) )
      return context.getNamespaceURI();
    final String namespace = context.getAttributeNS( prefixDeclarationNamespace, prefix );
    if( namespace != null && namespace.length() > 0 )
      return namespace;
    // test
// final NamedNodeMap attributes = context.getAttributes();
// int length = attributes.getLength();
// for( int i = 0; i < length; i++ )
// {
// final Node node = attributes.item( i );
// String prefix2 = node.getPrefix();
// String namespaceURI = node.getNamespaceURI();
// String nodeName = node.getNodeName();
// String nodeValue = node.getNodeValue();
// System.out.println( prefix2 + ":" + nodeName + " {" + namespaceURI + "}=" + nodeValue );
// }
    final Node parentNode = context.getParentNode();
    if( parentNode != null && parentNode instanceof Element )
      return getNameSpaceForPrefix( (Element) parentNode, prefix );
    return null;
  }

  /**
   * Creates a xml-header line with the given parameters.
   */
  public static String createXMLHeader( final String encoding )
  {
    return String.format( "<?xml version='1.0' encoding='%s'?>", encoding ); //$NON-NLS-1$
  }
}
