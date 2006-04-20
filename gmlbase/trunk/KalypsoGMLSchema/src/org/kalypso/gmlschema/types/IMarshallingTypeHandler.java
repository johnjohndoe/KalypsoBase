package org.kalypso.gmlschema.types;

import java.net.URL;
import java.text.ParseException;

import javax.xml.namespace.QName;

import org.kalypso.contribs.java.net.IUrlResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author belger
 */
public interface IMarshallingTypeHandler extends ITypeHandler
{
  /**
   * Serialize object to xml inclusive the propertynode
   * 
   * @param node
   *          serialize the object into this node
   * @param context
   *          use this context for relative url
   * @param object
   *          object to serialize, it must be instanceof {@link #getClassName()}.
   * @param propQName
   *          name of the propertynode, must be full prefixed !!
   */
  public void marshal( QName propQName, Object value, ContentHandler contentHandler, LexicalHandler lexicalHandler, final URL context ) throws TypeRegistryException;

  /**
   * creates an object of type {@link #getClassName()}from node.
   * 
   * @param node
   *          unmnarshall this node to an object of type {@link #getClassName()}
   * @param context
   *          use this context for relative url
   */
  public void unmarshal( XMLReader xmlReader, final IUrlResolver urlResolver, UnMarshallResultEater marshalResultEater ) throws TypeRegistryException;

  /** Ein Kurzname des behandelten Typ, wird z.B: für Beschriftungen benutzt */
  public String getShortname( );

  public Object cloneObject( final Object objectToClone ) throws CloneNotSupportedException;

  /** Creates an instance of my type from a string */
  public Object parseType( final String text ) throws ParseException;

}