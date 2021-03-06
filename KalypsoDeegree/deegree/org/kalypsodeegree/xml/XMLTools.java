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

package org.kalypsodeegree.xml;

// JDK 1.3
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML Tools based on JAXP 1.1 for parsing documents and retrieving node values/node attributes.
 * <p>
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @version $Revision$
 */
public final class XMLTools
{
  private static DocumentBuilderFactory DOCUMENT_FACTORY;
  static
  {
    try
    {
      DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
      DOCUMENT_FACTORY.setNamespaceAware( true );
      DOCUMENT_FACTORY.setValidating( false );
      DOCUMENT_FACTORY.setFeature( "http://apache.org/xml/features/allow-java-encodings", true );
    }
    catch( final Throwable e )
    {
      e.printStackTrace();
    }
  }

  /**
   * Checks if a given CDATA-value has to be escaped if it is used as a text value in an XML element. If the submitted
   * string contains a character that have to be escaped or if the string is made of more than 1500 characters it is
   * encapsulated into a CDATA-section. Returns a version that is safe to be used.
   * <p>
   * The method is just proofed for a UTF-8 character encoding.
   * <p>
   * 
   * @param cdata
   *          value to be used
   * @return the very same value (but escaped if necessary)
   */
  public static StringBuffer validateCDATA( final String cdata )
  {
    StringBuffer sb = null;
    if( cdata != null && (cdata.length() > 1000 || cdata.indexOf( '<' ) >= 0 || cdata.indexOf( '>' ) >= 0 || cdata.indexOf( '&' ) >= 0 || cdata.indexOf( '"' ) >= 0 || cdata.indexOf( "'" ) >= 0) )
    {
      sb = new StringBuffer( cdata.length() + 15 );
      sb.append( "<![CDATA[" ).append( cdata ).append( "]]>" );
    }
    else
    {
      if( cdata != null )
      {
        sb = new StringBuffer( cdata );
      }
    }
    return sb;
  }

  /**
   * Returns the specified child element of the given elemen. If there are more than one with the same name, the first
   * one is returned.
   * <p>
   * 
   * @param name
   *          name of the child element
   * @param namespace
   *          namespace of the child element
   * @param node
   *          current element
   * @return the element or null, if it is missing
   * @throws XMLParsingException
   *           specified child element is missing and required is true
   */
  public static Element getRequiredChildByName( final String name, final String namespace, final Node node ) throws XMLParsingException
  {
    final NodeList nl = node.getChildNodes();
    Element element = null;
    Element result = null;

    if( nl != null && nl.getLength() > 0 )
    {
      for( int i = 0; i < nl.getLength(); i++ )
      {
        if( nl.item( i ) instanceof Element )
        {
          element = (Element)nl.item( i );
          final String s = element.getNamespaceURI();
          if( s == null && namespace == null || namespace != null && namespace.equals( s ) )
          {
            if( element.getLocalName().equals( name ) )
            {
              result = element;
              break;
            }
          }
        }
      }
    }

    if( result == null )
    {
      throw new XMLParsingException( "Required child-element '" + name + "' of element '" + node.getNodeName() + "' is missing!" );
    }

    return result;
  }

  /**
   * Returns the specified child element of the given elemen. If there are more than one with the same name, the first
   * one is returned.
   * <p>
   * 
   * @param name
   *          name of the child element
   * @param namespace
   *          namespace of the child element
   * @param node
   *          current element
   * @return the element or null, if it is missing
   */
  public static Element getChildByName( final String name, final String namespace, final Node node )
  {

    final NodeList nl = node.getChildNodes();

    if( nl != null && nl.getLength() > 0 )
    {
      for( int i = 0; i < nl.getLength(); i++ )
      {
        if( nl.item( i ) instanceof Element )
        {
          final Element element = (Element)nl.item( i );

          final String s = element.getNamespaceURI();
          if( s == null && namespace == null || namespace != null && namespace.equals( s ) )
          {
            if( element.getLocalName().equals( name ) )
              return element;
          }
        }
      }
    }

    return null;
  }

  /**
   * Returns the specified child elements of the given element.
   * <p>
   * 
   * @param name
   *          name of the child elements
   * @param namespace
   *          namespace of the child elements
   * @param node
   *          current element
   * @return the list of matching child elements
   */
  public static ElementList getChildElementsByName( final String name, final String namespace, final Node node )
  {

    final NodeList nl = node.getChildNodes();
    Element element = null;
    final ElementList elementList = new ElementList();

    if( nl != null && nl.getLength() > 0 )
    {
      for( int i = 0; i < nl.getLength(); i++ )
      {
        if( nl.item( i ) instanceof Element )
        {
          element = (Element)nl.item( i );

          final String s = element.getNamespaceURI();

          if( s == null && namespace == null || namespace != null && namespace.equals( s ) )
          {
            if( element.getLocalName().equals( name ) )
            {
              elementList.addElement( element );
            }
          }
        }
      }
    }
    return elementList;
  }

  /**
   * Returns the text contained in the specified element. The returned value is trimmed by calling the trim() method of
   * java.lang.String
   * <p>
   * 
   * @param node
   *          current element
   * @return the textual contents of the element or null, if it is missing
   */
  public static String getStringValue( final Node node )
  {
    if( node.getNodeType() == Node.TEXT_NODE )
      return node.getNodeValue();
    final NodeList children = node.getChildNodes();
    final StringBuffer sb = new StringBuffer( children.getLength() * 500 );

    for( int i = 0; i < children.getLength(); i++ )
    {
      if( children.item( i ).getNodeType() == Node.TEXT_NODE || children.item( i ).getNodeType() == Node.CDATA_SECTION_NODE )
      {
        sb.append( children.item( i ).getNodeValue() );
      }
    }

    return sb.toString().trim();
  }

  /**
   * Returns the text contained in the specified child element of the given element.
   * <p>
   * 
   * @param name
   *          name of the child element
   * @param namespace
   *          namespace of the child element
   * @param node
   *          current element
   * @param defaultValue
   *          default value if element is missing
   * @return the textual contents of the element or the given default value, if missing
   */
  public static String getStringValue( final String name, final String namespace, final Node node, final String defaultValue )
  {
    final Element element = getChildByName( name, namespace, node );
    if( element == null )
      return defaultValue;

    final String value = getValue( element );
    // REMARK: empty string is not the same as no tag!
    if( value == null /* || value.equals( "" ) */)
      return defaultValue;

    return value;
  }

  /**
   * @param name
   * @param namespace
   * @param node
   */
  public static QName getQNameValue( final Element element )
  {
    if( element == null )
      return null;

    final String value = getValue( element );
    if( StringUtils.isBlank( value ) )
      return null;

    // hack to build a qName from string presentation (QName.toString())
    // this is needed as the XML-SchemaType is still xs:string and not xs:QName
    // according to Markus U. M�ller (OGC SLD-Editor) this will change in the next version in SLD Standard
    final int pos = value.indexOf( '}' );
    if( value.startsWith( "{" ) && pos > 0 )
      return QName.valueOf( value );

    QName prefixedName = parsePrefixedQName( value );
    String prefix = prefixedName.getPrefix();
    final String namespaceURI = element.lookupNamespaceURI( prefix );
    return new QName( namespaceURI, prefixedName.getLocalPart(), prefix );
  }

  /**
   * Parses the submitted {@link String} as a {@link QName}.<br/>
   * The string must be of form <code>prefix:localPart</code>.
   * The reuturned qname alsways has namespace {@link XMLConstants#NULL_NS_URI}.
   */
  public static QName parsePrefixedQName( String prefixed )
  {
    String[] tmp = StringUtils.split( prefixed, ":", 2 ); //$NON-NLS-1$
    if( tmp.length == 2 )
      return new QName( XMLConstants.NULL_NS_URI, tmp[1], tmp[0] );
    else
      return new QName( prefixed );
  }

  /**
   * Returns the text contained in the specified child element of the given element.
   * <p>
   * 
   * @param name
   *          name of the child element
   * @param namespace
   *          namespace of the child element
   * @param node
   *          current element
   * @return the textual contents of the element or null, if it is missing
   */
  public static String getRequiredStringValue( final String name, final String namespace, final Node node ) throws XMLParsingException
  {
    final Element element = getRequiredChildByName( name, namespace, node );
    return getValue( element );
  }

  /**
   * Returns the numerical value of the text contained in the specified child element of the given element as a double
   * (if it denotes a double).
   * <p>
   * 
   * @param name
   *          name of the child element
   * @param namespace
   *          namespace of the child element
   * @param node
   *          current element
   * @param defaultValue
   *          value to be used if the specified element is missing or it's value is not numerical
   * @return the textual contents of the element as a double-value
   */
  public static double getDoubleValue( final String name, final String namespace, final Node node, final double defaultValue )
  {
    double value = defaultValue;
    final String textValue = getStringValue( name, namespace, node, null );

    if( textValue != null )
    {
      try
      {
        value = Double.parseDouble( textValue );
      }
      catch( final NumberFormatException e )
      {
        e.printStackTrace();
      }
    }

    return value;
  }

  /**
   * Returns the numerical value of the text contained in the specified child element of the given element as a double
   * (if it denotes a double).
   * <p>
   * 
   * @param name
   *          name of the child element
   * @param namespace
   *          namespace of the child element
   * @param node
   *          current element
   * @return the textual contents of the element as a double-value
   * @throws XMLParsingException
   *           specified child element is missing or the contained text does not denote a double value
   */
  public static double getRequiredDoubleValue( final String name, final String namespace, final Node node ) throws XMLParsingException
  {
    double value;
    final String textValue = getRequiredStringValue( name, namespace, node );

    try
    {
      value = Double.parseDouble( textValue );
    }
    catch( final NumberFormatException e )
    {
      throw new XMLParsingException( "Value ('" + textValue + "') of element '" + name + "' does not denote a valid double value." );
    }
    return value;
  }

  /**
   * Returns the value of the specified node attribute or null if it is missing.
   * <p>
   * 
   * @param name
   *          (local) name of attribute
   * @param node
   *          current element
   * @return the textual contents of the attribute or null
   */
  public static String getAttrValue( final String name, final Node node )
  {
    String value = null;
    final NamedNodeMap atts = node.getAttributes();

    if( atts != null )
    {
      final Attr attribute = (Attr)atts.getNamedItem( name );

      if( attribute != null )
      {
        value = attribute.getValue();
      }
    }
    return value;
  }

  /**
   * Returns the value of the specified node attribute.
   * <p>
   * 
   * @param name
   *          (local) name of attribute
   * @param node
   *          current element
   * @return the textual contents of the attribute
   * @throws XMLParsingException
   *           if specified attribute is missing
   */
  public static String getRequiredAttrValue( final String name, final Node node ) throws XMLParsingException
  {
    String value = null;
    final NamedNodeMap atts = node.getAttributes();

    if( atts != null )
    {
      final Attr attribute = (Attr)atts.getNamedItem( name );

      if( attribute != null )
      {
        value = attribute.getValue();
      }
    }
    if( value == null )
    {
      throw new XMLParsingException( "Required attribute '" + name + "' of element '" + node.getNodeName() + "' is missing." );
    }
    return value;
  }

  /**
   * Returns the value of the specified node attribute. // * FIXME: Due to apparent bugs in getNamedItemNS (name,
   * namespace), // * when used to find attribute nodes, the current implementation // * uses a workaround.
   * <p>
   * 
   * @param name
   *          (local) name of attribute
   * @param namespace
   *          namespace of attribute
   * @param node
   *          current element
   * @return the textual contents of the attribute
   * @throws XMLParsingException
   *           if specified attribute is missing
   */
  public static String getRequiredAttrValue( final String name, final String namespace, final Node node ) throws XMLParsingException
  {
    String value = null;

    final NamedNodeMap atts = node.getAttributes();

    if( atts != null )
    {
      final Attr attribute = (Attr)atts.getNamedItemNS( namespace, name );

      if( attribute != null )
      {
        value = attribute.getValue();
      }
    }

    // for (int i = 0; i < atts.getLength (); i++) {
    // Node n = atts.item (i);
    // System.out.println ("Nodename: " + toLocalName (n.getNodeName ()));
    // System.out.println ("NamespaceURI: " + n.getNamespaceURI ());
    // if (n.getNamespaceURI ().equals (namespace) && toLocalName
    // (n.getNodeName
    // ()).equals (name)) {
    // System.out.println ("Me is here!");
    // value = ((Attr) n).getValue ();
    // break;
    // }
    // }

    if( value == null )
    {
      throw new XMLParsingException( "Required attribute '" + namespace + ":" + name + "' of element '" + node.getNodeName() + "' is missing." );
    }
    return value;
  }

  /**
   * creates a new and empty dom document
   */
  public static Document create( )
  {
    try
    {
      final DocumentBuilder builder = DOCUMENT_FACTORY.newDocumentBuilder();
      return builder.newDocument();
    }
    catch( final ParserConfigurationException ex )
    {
      System.out.println( ex );
      return null;
    }
  }

  /**
   * Returns the attribute value of the given node.
   */
  public static String getAttrValue( final Node node, final String attrName )
  {
    // get attr name and dtype
    final NamedNodeMap atts = node.getAttributes();

    if( atts == null )
    {
      return null;
    }

    final Attr a = (Attr)atts.getNamedItem( attrName );

    if( a != null )
    {
      return a.getValue();
    }

    return null;
  }

  /**
   * Returns the attribute value of the given node.
   */
  public static String getAttrValue( final Node node, final String namespace, final String attrName )
  {
    // get attr name and dtype
    final NamedNodeMap atts = node.getAttributes();

    if( atts == null )
    {
      return null;
    }

    final Attr a = (Attr)atts.getNamedItemNS( namespace, attrName );

    if( a != null )
    {
      return a.getValue();
    }

    return null;
  }

  // FIXME: file encoding?!
  public static Document parse( final IFile file ) throws IOException, SAXException, CoreException
  {
    InputStream stream = null;

    try
    {
      stream = new BufferedInputStream( file.getContents() );
      final Document doc = parse( stream );
      stream.close();
      return doc;
    }
    finally
    {
      IOUtils.closeQuietly( stream );
    }
  }

  /**
   * Parses a XML document and returns a DOM object.
   * 
   * @param fileName
   *          the filename of the XML file to be parsed
   * @return a DOM object
   * @throws IOException
   * @throws SAXException
   */
  public static Document parse( final String fileName ) throws IOException, SAXException
  {
    final Reader reader = new InputStreamReader( new FileInputStream( fileName ) );
    return parse( reader );
  }

  /**
   * @param resource
   * @return dom from url
   * @throws IOException
   * @throws SAXException
   */
  public static Document parse( final URL resource ) throws IOException, SAXException
  {
    InputStream stream = null;

    try
    {
      stream = resource.openStream();
      final Document doc = parse( stream );
      stream.close();
      return doc;
    }
    finally
    {
      IOUtils.closeQuietly( stream );
    }
  }

  public static Document parse( final File file ) throws SAXException, IOException
  {
    InputStream stream = null;

    try
    {
      stream = new BufferedInputStream( new FileInputStream( file ) );
      final Document doc = parse( stream );
      stream.close();
      return doc;
    }
    finally
    {
      IOUtils.closeQuietly( stream );
    }
  }

  /**
   * Parses a XML document and returns a DOM object.
   * 
   * @param reader
   *          accessing the resource to parse
   * @return a DOM object
   * @throws IOException
   * @throws SAXException
   * @deprecated Probably code which uses this method is not safe for two reasons: the reader only gets closed if no
   *             exception is thrown, second probably the charset is not correctly set. Use {@link #parse(InputStream)} instead, charset encoding is set inside the xml-file.
   */
  @Deprecated
  public static Document parse( final Reader reader ) throws IOException, SAXException
  {
    javax.xml.parsers.DocumentBuilder parser = null;

    try
    {
      parser = DOCUMENT_FACTORY.newDocumentBuilder();
    }
    catch( final ParserConfigurationException ex )
    {
      ex.printStackTrace();
      throw new IOException( "Unable to initialize DocumentBuilder: " + ex.getMessage() );
    }

    final Document doc = parser.parse( new InputSource( reader ) );
    reader.close();

    return doc;
  }

  /**
   * Parses a XML document and returns a DOM object.
   * <p>
   * The stream is NOT closed by this method.
   * 
   * @param reader
   *          accessing the resource to parse
   * @return a DOM object
   * @throws IOException
   * @throws SAXException
   */
  public static Document parse( final InputStream is ) throws IOException, SAXException
  {
    try
    {
      final DocumentBuilder parser = DOCUMENT_FACTORY.newDocumentBuilder();
      return parser.parse( new InputSource( is ) );
    }
    catch( final ParserConfigurationException ex )
    {
      ex.printStackTrace();

      throw new IOException( "Unable to initialize DocumentBuilder: " + ex.getMessage(), ex );
    }
  }

  /**
   * copies one node to another node (of a different dom document).
   */
  public static Node copyNode( final Node source, final Node dest )
  {
    // Debug.debugMethodBegin( "XMLTools", "copyNode" );
    if( source.getNodeType() == Node.TEXT_NODE )
    {
      final Text tn = dest.getOwnerDocument().createTextNode( source.getNodeValue() );
      return tn;
    }

    final NamedNodeMap attr = source.getAttributes();

    if( attr != null )
    {
      for( int i = 0; i < attr.getLength(); i++ )
      {
        ((Element)dest).setAttribute( attr.item( i ).getNodeName(), attr.item( i ).getNodeValue() );
      }
    }

    final NodeList list = source.getChildNodes();

    for( int i = 0; i < list.getLength(); i++ )
    {
      if( !(list.item( i ) instanceof Text) )
      {
        final Element en = dest.getOwnerDocument().createElementNS( list.item( i ).getNamespaceURI(), list.item( i ).getNodeName() );

        if( list.item( i ).getNodeValue() != null )
        {
          en.setNodeValue( list.item( i ).getNodeValue() );
        }

        final Node n = copyNode( list.item( i ), en );
        dest.appendChild( n );
      }
      else if( list.item( i ) instanceof CDATASection )
      {
        final CDATASection cd = dest.getOwnerDocument().createCDATASection( list.item( i ).getNodeValue() );
        dest.appendChild( cd );
      }
      else
      {
        final Text tn = dest.getOwnerDocument().createTextNode( list.item( i ).getNodeValue() );
        dest.appendChild( tn );
      }
    }

    // Debug.debugMethodEnd();
    return dest;
  }

  /**
   * inserts a node into a dom element (of a different dom document)
   */
  public static Node insertNodeInto( final Node source, final Node dest )
  {

    Document dDoc = null;
    final Document sDoc = source.getOwnerDocument();

    if( dest instanceof Document )
    {
      dDoc = (Document)dest;
    }
    else
    {
      dDoc = dest.getOwnerDocument();
    }

    if( dDoc.equals( sDoc ) )
    {
      dest.appendChild( source );
    }
    else
    {
      final Element element = dDoc.createElementNS( source.getNamespaceURI(), source.getNodeName() );
      dest.appendChild( element );

      copyNode( source, element );
    }

    return dest;
  }

  /**
   * returns the first child element of the submitted node
   */
  public static Element getFirstElement( final Node node )
  {

    final NodeList nl = node.getChildNodes();
    Element element = null;

    if( nl != null && nl.getLength() > 0 )
    {
      for( int i = 0; i < nl.getLength(); i++ )
      {
        if( nl.item( i ) instanceof Element )
        {
          element = (Element)nl.item( i );

          break;
        }
      }
    }

    return element;
  }

  /**
   * removes all direct child nodes of the submitted node with the also submitted name
   */
  public static Node removeNamedChildNodes( final Node node, final String nodeName )
  {

    final NodeList nl = node.getChildNodes();

    if( nl != null && nl.getLength() > 0 )
    {
      for( int i = 0; i < nl.getLength(); i++ )
      {
        if( nl.item( i ).getNodeName().equals( nodeName ) )
        {
          node.removeChild( nl.item( i ) );
        }
      }
    }

    return node;
  }

  /**
   * returns the first child element of the submitted node
   */
  public static Element getNamedChild( final Node node, final String name )
  {
    // Debug.debugMethodBegin( "XMLTools", "getNamedChild" );
    final NodeList nl = node.getChildNodes();
    Element element = null;
    Element return_ = null;

    if( nl != null && nl.getLength() > 0 )
    {
      for( int i = 0; i < nl.getLength(); i++ )
      {
        if( nl.item( i ) instanceof Element )
        {
          element = (Element)nl.item( i );

          if( element.getNodeName().equals( name ) )
          {
            return_ = element;

            break;
          }
        }
      }
    }

    // Debug.debugMethodEnd();
    return return_;
  }

  /**
   * Returns the first child element of the submitted node that matches the given namespace and name.
   */
  public static Element getNamedChild( final Node node, final String namespace, final String name )
  {
    // Debug.debugMethodBegin( "XMLTools", "getNamedChild" );
    final NodeList nl = node.getChildNodes();
    Element element = null;
    Element return_ = null;

    if( nl != null && nl.getLength() > 0 )
    {
      for( int i = 0; i < nl.getLength(); i++ )
      {
        if( nl.item( i ) instanceof Element )
        {
          element = (Element)nl.item( i );

          final String s = element.getNamespaceURI();

          if( namespace == null && s == null || namespace.equals( s ) )
          {
            if( element.getLocalName().equals( name ) )
            {
              return_ = element;

              break;
            }
          }
        }
      }
    }

    // Debug.debugMethodEnd();
    return return_;
  }

  /**
   * Returns the concatenated Strings of all direct children that are TEXT_NODEs.
   */
  public static String getValue( final Node node )
  {
    final NodeList children = node.getChildNodes();
    final StringBuffer sb = new StringBuffer( children.getLength() * 500 );

    for( int i = 0; i < children.getLength(); i++ )
    {
      if( children.item( i ).getNodeType() == Node.TEXT_NODE || children.item( i ).getNodeType() == Node.CDATA_SECTION_NODE )
      {
        sb.append( children.item( i ).getNodeValue() );
      }
    }

    return sb.toString().trim();
  }

  /**
   * Returns all child ELEMENTs.
   */
  public static ElementList getChildElements( final Node node )
  {
    final NodeList children = node.getChildNodes();
    final ElementList list = new ElementList();

    for( int i = 0; i < children.getLength(); i++ )
    {
      if( children.item( i ).getNodeType() == Node.ELEMENT_NODE )
        list.addElement( (Element)children.item( i ) );
    }

    return list;
  }

  /**
   * Appends a node and it's children to the given StringBuffer. Indentation is added on recursion.
   */
  public static void appendNode( final Node node, final String indent, final StringBuffer sb )
  {
    switch( node.getNodeType() )
    {
      case Node.DOCUMENT_NODE:
      {
        sb.append( "<?xml version=\"1.0\"?>\n" );

        final Document doc = (Document)node;
        appendNode( doc.getDocumentElement(), "", sb );

        break;
      }

      case Node.ELEMENT_NODE:
      {
        final String name = node.getNodeName();
        sb.append( indent + "<" + name );

        final NamedNodeMap attributes = node.getAttributes();

        for( int i = 0; i < attributes.getLength(); i++ )
        {
          final Node current = attributes.item( i );
          sb.append( " " + current.getNodeName() + "=\"" + current.getNodeValue() + "\"" );
        }

        sb.append( ">" );

        // Kinder durchgehen
        final NodeList children = node.getChildNodes();

        if( children != null )
        {
          for( int i = 0; i < children.getLength(); i++ )
          {
            appendNode( children.item( i ), indent, sb );
          }
        }

        sb.append( indent + "</" + name + ">" );

        break;
      }

      case Node.TEXT_NODE:
      case Node.CDATA_SECTION_NODE:
      {
        final String trimmed = node.getNodeValue().trim();

        if( !trimmed.equals( "" ) )
        {
          sb.append( indent + trimmed );
        }

        break;
      }

      case Node.PROCESSING_INSTRUCTION_NODE:
        break;

      case Node.ENTITY_REFERENCE_NODE:
        break;

      case Node.DOCUMENT_TYPE_NODE:
        break;
    }
  }

  /**
   * extracts the local name from a node name
   */
  public static String toLocalName( String nodeName )
  {
    final int pos = nodeName.lastIndexOf( ':' );

    if( pos > -1 )
    {
      nodeName = nodeName.substring( pos + 1, nodeName.length() );
    }

    return nodeName;
  }
}