package org.kalypso.contribs.java.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author doemming
 */
public class XMLHelper
{
  /** Performance: instantiate this factory only once, this is expensive. */
  private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

  private static final String DEFAULT_ENCODING = "UTF-8";

  public static Document getAsDOM( final File file, final boolean namespaceaware ) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException
  {
    return getAsDOM( new FileInputStream( file ), namespaceaware );
  }

  public static Document getAsDOM( final InputStream inStream, final boolean namespaceaware ) throws ParserConfigurationException, SAXException, IOException
  {
    return getAsDOM( new InputSource( inStream ), namespaceaware );
  }

  public static Document getAsDOM( final InputSource inputSource, final boolean namespaceaware ) throws ParserConfigurationException, SAXException, IOException
  {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware( namespaceaware );

    final DocumentBuilder docuBuilder = factory.newDocumentBuilder();
    return docuBuilder.parse( inputSource );
  }

  public static Document getAsDOM( final URL url, final boolean namespaceaware ) throws Exception
  {
    InputStream inputStream = null;

    try
    {
      final URLConnection connection = url.openConnection();
      inputStream = new BufferedInputStream( connection.getInputStream() );
      final InputSource source = new InputSource( inputStream );
      final String contentEncoding = connection.getContentEncoding();

      if( contentEncoding != null )
        source.setEncoding( contentEncoding );
      else
        source.setEncoding( DEFAULT_ENCODING );
      // TODO set default encoding to "UTF-8" is this correct???
      return getAsDOM( source, namespaceaware );
    }
    finally
    {
      if( inputStream != null )
        inputStream.close();
    }
  }

  public static void writeDOM( final Document xmlDOM, final String charset, final OutputStream os ) throws TransformerException
  {
    writeDOM( xmlDOM, charset, new StreamResult( os ), true  );
  }

  public static void writeDOM( final Node xmlDOM, final String charset, final Writer writer ) throws TransformerException
  {
    writeDOM( xmlDOM, charset, writer, true );
  }

  public static void writeDOM( final Node xmlDOM, final String charset, final Writer writer, final boolean indent ) throws TransformerException
  {
    // sollte nichte benutzt werden, wenn das charset nicht bekannt ist,
    // da sonst Mist rauskommt
    if( charset == null )
      throw new NullPointerException( "charset is null" );

    writeDOM( xmlDOM, charset, new StreamResult( writer ), indent );
  }

  public static void writeDOM( final Node xmlDOM, final String charset, final StreamResult streamResult, final boolean indent ) throws TransformerException
  {
    final Transformer t = TRANSFORMER_FACTORY.newTransformer();

    if( indent )
    {
      t.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
      t.setOutputProperty( OutputKeys.INDENT, "yes" );
    }
    if( charset != null )
      t.setOutputProperty( OutputKeys.ENCODING, charset );

    t.transform( new DOMSource( xmlDOM ), streamResult );
  }

  public static Node getAttributeNode( final Node node, final String attributeName )
  {
    try
    {
      final NamedNodeMap nodeMap = node.getAttributes();

      return nodeMap.getNamedItem( attributeName );
    }
    catch( final Exception e )
    {
      return null;
    }
  }

  public static String getAttributeValue( final Node node, final String attributeName )
  {
    final Node attributeNode = getAttributeNode( node, attributeName );
    if( attributeNode == null )
    {
      System.out.println( "attributenode not found for name: " + attributeName );
      System.out.println( XMLHelper.toString( attributeNode ) );
    }
    return attributeNode.getNodeValue();
  }

  public static Document post( final String url, final String data, final boolean namespaceaware ) throws Exception
  {
    return post( new URL( url ), data, namespaceaware );
  }

  public static Document post( final URL url, final String data, final boolean namespaceaware ) throws Exception
  {
    final URLConnection connect = url.openConnection();

    if( connect instanceof HttpURLConnection )
    {
      final HttpURLConnection uc = (HttpURLConnection) connect;
      uc.setRequestMethod( "POST" );
      uc.setDoInput( true );
      uc.setDoOutput( true );
      uc.setUseCaches( false );

      final PrintWriter pw = new PrintWriter( uc.getOutputStream() );
      pw.print( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + data );
      pw.flush();
      pw.close();

      return getAsDOM( uc.getInputStream(), namespaceaware );
    }

    throw new Exception( "uups, no http connection" );
  }

  public static NodeList reduceByAttribute( final NodeList nl, final String attributeName, final String attributeValue )
  {
    final NodeList_Impl result = new NodeList_Impl();

    for( int i = 0; i < nl.getLength(); i++ )
    {
      try
      {
        final NamedNodeMap nodeMap = nl.item( i ).getAttributes();

        final Node namedItem = nodeMap.getNamedItem( attributeName );
        if( namedItem != null && attributeValue.equals( namedItem.getNodeValue() ) )
          result.add( nl.item( i ) );
      }
      catch( final Exception e )
      {
        // nothing to do
      }
    }

    return result;
  }

  public static String toString( final NodeList nl )
  {
    final StringBuffer result = new StringBuffer();

    for( int i = 0; i < nl.getLength(); i++ )
      result.append( toString( nl.item( i ) ) );

    return result.toString();
  }

  public static String toString( final Node node )
  {
    try
    {
      final Transformer t = TRANSFORMER_FACTORY.newTransformer();
      final DOMSource src = new DOMSource( node );
      final StringWriter sw = new StringWriter();
      final StreamResult result = new StreamResult( sw );
      t.transform( src, result );

      return sw.toString();
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      return "sorry: " + e.getMessage();
    }
  }

  public static String xslTransform( final Node domNode, final String outputMethod, final String xslTemplateString )
  {
    try
    {
      final String xslString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<xsl:stylesheet version=\"1.0\" " + " xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" + "<xsl:output method=\""
      + outputMethod + "\" />" + xslTemplateString + "</xsl:stylesheet>";

      final DOMSource xmlSource = new DOMSource( domNode );
      final StreamSource xslSource = new StreamSource( new StringReader( xslString ) );

      return xslTransform( xmlSource, xslSource );

    }
    catch( final Exception e )
    {
      e.printStackTrace();

      return null;
    }
  }

  public static String xslTransform( final Source xmlSource, final Source xslSource )
  {
    try
    {
      final Transformer transformer = TRANSFORMER_FACTORY.newTransformer( xslSource );
      final StringWriter resultSW = new StringWriter();
      transformer.transform( xmlSource, new StreamResult( resultSW ) );

      return resultSW.toString();

    }
    catch( final Exception e )
    {
      e.printStackTrace();

      return null;
    }
  }

  public static void xslTransform( final InputStream xmlInputStream, final InputStream xslInputStream, final Writer writer ) throws TransformerException, ParserConfigurationException, SAXException, IOException
  {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware( true );
    final DocumentBuilder docuBuilder = factory.newDocumentBuilder();
    final Document xmlDOM = docuBuilder.parse( xmlInputStream );
    final Document xslDOM = docuBuilder.parse( xslInputStream );
    final Transformer transformer = TRANSFORMER_FACTORY.newTransformer( new DOMSource( xslDOM ) );
    transformer.transform( new DOMSource( xmlDOM ), new StreamResult( writer ) );
    writer.close();
  }

  public static String xslTransform( final File xmlFile, final File xslFile ) throws Exception
  {
    return xslTransform( new FileInputStream( xmlFile ), new FileInputStream( xslFile ) );
  }

  public static String xslTransform( final InputStream xmlFile, final InputStream xslFile ) throws Exception
  {

    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware( true );

    final DocumentBuilder docuBuilder = factory.newDocumentBuilder();
    final Document xmlDOM = docuBuilder.parse( xmlFile );
    final Document xslDOM = docuBuilder.parse( xslFile );

    return xslTransform( new DOMSource( xmlDOM ), new DOMSource( xslDOM ) );
  }

  public static boolean isAbstractElementDefinition( final Node node )
  {
    final String abstractStatus = ((Element) node).getAttribute( "abstract" );
    if( abstractStatus == null )
      return false;
    if( "false".equals( abstractStatus ) || "0".equals( abstractStatus ) || "".equals( abstractStatus ) )
      return false;
    return true;
  }

  /**
   * Helper methode for easy handling obects in switch blocks
   * 
   * @return position of object in objectArray TODO move to a general HelperClass
   */
  public static int indexOf( final Object object, final Object[] objectArray )
  {
    for( int i = 0; i < objectArray.length; i++ )
      if( object.equals( objectArray[i] ) )
        return i;
    return -1;
  }

  public static String getStringFromChildElement( final Element elt, final String namespace, final String eltName )
  {
    final NodeList nlL = elt.getElementsByTagNameNS( namespace, eltName );
    if( nlL.getLength() > 0 )
    {
      final Element innerElt = (Element) nlL.item( 0 );
      return getStringValue( innerElt );
    }

    return null;
  }

  public static Node getFirstChildElement( final Node parentNode, final String ns, final String name, final int maxDepth )
  {
    final NodeList childNodes = parentNode.getChildNodes();
    for( int i = 0; i < childNodes.getLength(); i++ )
    {
      final Node node = childNodes.item( i );
      switch( node.getNodeType() )
      {
        case Node.ELEMENT_NODE:
          if( ns != null && ns.equals( node.getNamespaceURI() ) && name.equals( node.getLocalName() ) )
            return node;
          else if( ns == null && name.equals( node.getLocalName() ) )
            return node;
          if( maxDepth > 0 )
          {
            final Node subNode = getFirstChildElement( node, ns, name, maxDepth - 1 );
            if( subNode != null )
              return subNode;
          }
          break;
        default:
          continue;
      }
    }
    return null;
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
}