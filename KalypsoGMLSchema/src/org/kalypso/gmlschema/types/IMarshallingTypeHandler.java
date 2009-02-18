package org.kalypso.gmlschema.types;

import java.net.URL;
import java.text.ParseException;

import javax.xml.namespace.QName;

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
   * @param context
   *          use this context for relative url
   * @param object
   *          object to serialize, it must be instanceof {@link #getClassName()}.
   * @param propQName
   *          name of the propertynode, must be full prefixed !!
   */
  public void marshal( QName propQName, Object value, ContentHandler contentHandler, LexicalHandler lexicalHandler, final URL context, final String gmlVersion ) throws TypeRegistryException;

  /**
   * creates an object of type {@link #getClassName()}from node.
   * 
   * @param context
   *          use this context for relative url
   */
  public void unmarshal( final XMLReader xmlReader, final URL context, UnMarshallResultEater marshalResultEater, final String gmlVersion ) throws TypeRegistryException;

  /** Ein Kurzname des behandelten Typ, wird z.B: f�r Beschriftungen benutzt */
  public String getShortname( );

  public Object cloneObject( final Object objectToClone, final String gmlVersion ) throws CloneNotSupportedException;

  /**
   * Creates an instance of my type from a string.
   * <p>
   * Remark this should not be used for gui purposes. Use IGuiTypeHandler#fromText instead.
   * </p>
   * <p>TODO: Check if this should be deprecated</p>
   */
  public Object parseType( final String text ) throws ParseException;

}