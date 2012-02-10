package org.kalypso.gmlschema.types;

import java.net.URL;
import java.text.ParseException;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Gernot Belger
 */
public interface IMarshallingTypeHandler extends ITypeHandler
{
  /**
   * Serialize object to xml inclusive the propertynode
   * 
   * @param context
   *            use this context for relative url
   * @param object
   *            object to serialize, it must be instanceof {@link #getClassName()}.
   * @param propQName
   *            name of the propertynode, must be full prefixed !!
   * @param XMLReader
   *            The XMLReader to serialize to. Write into its {@link ContentHandler} to do so.
   */
  public void marshal( final Object value, final XMLReader reader, final URL context, final String gmlVersion ) throws SAXException;

  /**
   * creates an object of type {@link #getClassName()}from node.
   * 
   * @param context
   *            use this context for relative url
   */
  public void unmarshal( final XMLReader reader, final URL context, UnmarshallResultEater marshalResultEater, final String gmlVersion ) throws TypeRegistryException;

  /** Ein Kurzname des behandelten Typ, wird z.B: für Beschriftungen benutzt */
  public String getShortname( );

  public Object cloneObject( final Object objectToClone, final String gmlVersion ) throws CloneNotSupportedException;

  /**
   * Creates an instance of my type from a string.
   * <p>
   * Remark this should not be used for gui purposes. Use IGuiTypeHandler#fromText instead.
   * </p>
   * <p>
   * TODO: Check if this should be deprecated
   * </p>
   */
  public Object parseType( final String text ) throws ParseException;

}