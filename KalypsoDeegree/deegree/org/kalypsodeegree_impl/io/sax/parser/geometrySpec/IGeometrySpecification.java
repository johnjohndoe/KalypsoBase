package org.kalypsodeegree_impl.io.sax.parser.geometrySpec;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.xml.sax.XMLReader;

/**
 * This class tells us by which properties a geometry may be specified, setting a TypeHandler for each of these
 * properties.
 * <br>
 * For now it will support only for properties parsed via SAX ({@link IPropertyMarshallingTypeHandler})
 * 
 * @author Felipe Maximino
 */
public interface IGeometrySpecification
{ 
  IGmlContentHandler getHandler( QName property, XMLReader reader, IGmlContentHandler parent, IGmlContentHandler receiver, String defaultSrs );
}
